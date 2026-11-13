package br.com.frota.arquivo;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class ArquivoService {

    private static final long MB = 1024L * 1024L;
    private static final long LIMITE_IMAGEM_PDF = 15 * MB;
    private static final long LIMITE_VIDEO = 100 * MB;
    private static final long LIMITE_SENSIVEL = 50 * MB;

    public record ArquivoAberto(Arquivo arquivo, InputStream conteudo) {
    }

    private final ArquivoRepository repository;
    private final AcessoArquivoRepository acessoRepository;
    private final Armazenamento armazenamento;
    private final CriptografiaService criptografia;

    public ArquivoService(ArquivoRepository repository,
                          AcessoArquivoRepository acessoRepository,
                          Armazenamento armazenamento,
                          CriptografiaService criptografia) {
        this.repository = repository;
        this.acessoRepository = acessoRepository;
        this.armazenamento = armazenamento;
        this.criptografia = criptografia;
    }

    @Transactional
    public Arquivo enviar(MultipartFile arquivo, boolean sensivel, Long usuarioId) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nenhum arquivo enviado");
        }

        String tipo = detectarTipo(arquivo);
        long limite = sensivel ? LIMITE_SENSIVEL : (tipo.startsWith("video/") ? LIMITE_VIDEO : LIMITE_IMAGEM_PDF);
        if (arquivo.getSize() > limite) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                    "Arquivo maior que o limite de " + (limite / MB) + " MB para esse tipo");
        }

        LocalDate hoje = LocalDate.now();
        String chave = hoje.getYear() + "/" + String.format("%02d", hoje.getMonthValue()) + "/" + UUID.randomUUID();
        MessageDigest digest = novoSha256();

        try (InputStream in = new DigestInputStream(arquivo.getInputStream(), digest)) {
            armazenamento.salvar(chave, sensivel ? criptografia.criptografar(in) : in);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Falha ao salvar o arquivo");
        }

        try {
            return repository.save(new Arquivo(nomeSeguro(arquivo.getOriginalFilename()), tipo, arquivo.getSize(),
                    HexFormat.of().formatHex(digest.digest()), chave, sensivel, usuarioId));
        } catch (RuntimeException e) {
            try {
                armazenamento.apagar(chave); // não deixa arquivo órfão se o banco falhar
            } catch (IOException ignorado) {
                // nada a fazer
            }
            throw e;
        }
    }

    /**
     * Regras de acesso (por enquanto):
     * - sensível: só ADMIN, e o acesso fica registrado;
     * - demais: ADMIN, SUPERVISOR, GERENTE ou quem enviou.
     * Quando os arquivos forem ligados a vistorias e abastecimentos, o técnico responsável
     * pelo veículo também passa a poder ver.
     */
    @Transactional
    public ArquivoAberto abrir(Long id, Long usuarioId, String perfil) {
        Arquivo arquivo = repository.findById(id).orElseThrow(this::naoEncontrado);

        boolean admin = "ADMIN".equals(perfil);
        boolean pode = arquivo.isSensivel()
                ? admin
                : admin || "SUPERVISOR".equals(perfil) || "GERENTE".equals(perfil)
                        || arquivo.getEnviadoPor().equals(usuarioId);
        if (!pode) {
            throw naoEncontrado(); // não revela que o arquivo existe
        }

        try {
            if (arquivo.isSensivel()) {
                acessoRepository.save(new AcessoArquivo(arquivo.getId(), usuarioId));
                return new ArquivoAberto(arquivo, criptografia.descriptografar(armazenamento.abrir(arquivo.getCaminho())));
            }
            return new ArquivoAberto(arquivo, armazenamento.abrir(arquivo.getCaminho()));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Falha ao ler o arquivo");
        }
    }

    /** Descobre o tipo real pelos primeiros bytes (não confia no que o cliente diz). */
    private String detectarTipo(MultipartFile arquivo) {
        byte[] h;
        try (InputStream in = arquivo.getInputStream()) {
            h = in.readNBytes(16);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não foi possível ler o arquivo");
        }
        if (h.length >= 3 && (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (h.length >= 4 && (h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G') {
            return "image/png";
        }
        if (h.length >= 4 && h[0] == '%' && h[1] == 'P' && h[2] == 'D' && h[3] == 'F') {
            return "application/pdf";
        }
        if (h.length >= 12 && ascii(h, 0, 4).equals("RIFF") && ascii(h, 8, 4).equals("WEBP")) {
            return "image/webp";
        }
        if (h.length >= 12 && ascii(h, 4, 4).equals("ftyp")) {
            String marca = ascii(h, 8, 4);
            if (marca.equals("qt  ")) {
                return "video/quicktime";
            }
            if (marca.equals("heic") || marca.equals("heix") || marca.equals("mif1") || marca.equals("msf1")) {
                return "image/heic";
            }
            return "video/mp4";
        }
        throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Tipo de arquivo não permitido (aceitos: JPG, PNG, WEBP, HEIC, PDF, MP4 e MOV)");
    }

    private String ascii(byte[] bytes, int inicio, int tamanho) {
        return new String(bytes, inicio, tamanho, StandardCharsets.ISO_8859_1);
    }

    private String nomeSeguro(String original) {
        if (original == null || original.isBlank()) {
            return "arquivo";
        }
        String nome = Paths.get(original.replace('\\', '/')).getFileName().toString();
        nome = nome.replaceAll("[^\\p{L}\\p{N} ._()-]", "_");
        return nome.length() > 200 ? nome.substring(nome.length() - 200) : nome;
    }

    private MessageDigest novoSha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private ResponseStatusException naoEncontrado() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Arquivo não encontrado");
    }
}
