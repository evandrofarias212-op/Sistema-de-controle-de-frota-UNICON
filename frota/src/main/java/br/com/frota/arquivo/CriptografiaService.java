package br.com.frota.arquivo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/** AES-256-GCM para arquivos marcados como sensíveis. O IV (12 bytes) vai no começo do arquivo. */
@Service
public class CriptografiaService {

    private static final Logger log = LoggerFactory.getLogger(CriptografiaService.class);

    private static final String CHAVE_DEV = "ZGV2LW9ubHktY2hhdmUtMzItYnl0ZXMtMTIzNDU2Nzg=";
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;

    private final SecretKeySpec chave;
    private final SecureRandom random = new SecureRandom();

    public CriptografiaService(@Value("${app.storage.chave-sensivel:" + CHAVE_DEV + "}") String chaveBase64) {
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(chaveBase64.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("app.storage.chave-sensivel precisa estar em Base64");
        }
        if (bytes.length != 32) {
            throw new IllegalStateException("app.storage.chave-sensivel precisa ter 32 bytes (AES-256)");
        }
        if (CHAVE_DEV.equals(chaveBase64.trim())) {
            log.warn("Usando a chave de criptografia de DESENVOLVIMENTO. Defina STORAGE_CHAVE antes de ir para produção!");
        }
        this.chave = new SecretKeySpec(bytes, "AES");
    }

    public InputStream criptografar(InputStream claro) {
        try {
            byte[] iv = new byte[IV_BYTES];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, chave, new GCMParameterSpec(TAG_BITS, iv));
            return new SequenceInputStream(new ByteArrayInputStream(iv), new CipherInputStream(claro, cipher));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Falha ao criptografar", e);
        }
    }

    public InputStream descriptografar(InputStream cifrado) throws IOException {
        try {
            byte[] iv = cifrado.readNBytes(IV_BYTES);
            if (iv.length != IV_BYTES) {
                throw new IOException("Arquivo cifrado inválido");
            }
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, chave, new GCMParameterSpec(TAG_BITS, iv));
            return new CipherInputStream(cifrado, cipher);
        } catch (GeneralSecurityException e) {
            throw new IOException("Falha ao descriptografar", e);
        }
    }
}
