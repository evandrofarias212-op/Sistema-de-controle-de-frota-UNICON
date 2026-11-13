package br.com.frota.arquivo;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/arquivos")
public class ArquivoController {

    private final ArquivoService service;

    public ArquivoController(ArquivoService service) {
        this.service = service;
    }

    /** Qualquer usuário logado envia; arquivo sensível (ex.: vídeo da senha do celular) só ADMIN. */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ArquivoResponse enviar(@RequestParam("arquivo") MultipartFile arquivo,
                                  @RequestParam(name = "sensivel", defaultValue = "false") boolean sensivel,
                                  @AuthenticationPrincipal Jwt jwt) {
        if (sensivel && !"ADMIN".equals(jwt.getClaimAsString("perfil"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Só ADMIN pode enviar arquivo sensível");
        }
        return ArquivoResponse.de(service.enviar(arquivo, sensivel, jwt.<Long>getClaim("uid")));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InputStreamResource> baixar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        ArquivoService.ArquivoAberto aberto =
                service.abrir(id, jwt.<Long>getClaim("uid"), jwt.getClaimAsString("perfil"));
        Arquivo arquivo = aberto.arquivo();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(arquivo.getTipoConteudo()))
                .contentLength(arquivo.getTamanhoBytes())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(arquivo.getNomeOriginal(), StandardCharsets.UTF_8)
                                .build().toString())
                .body(new InputStreamResource(aberto.conteudo()));
    }
}
