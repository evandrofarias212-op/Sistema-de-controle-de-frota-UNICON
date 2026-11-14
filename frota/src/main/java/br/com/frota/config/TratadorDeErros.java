package br.com.frota.config;

import br.com.frota.vistoria.PendenciasException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/** Faz todo erro voltar como JSON com a mensagem, em vez de um 400 mudo. */
@RestControllerAdvice
public class TratadorDeErros {

    public record Erro(int status, String mensagem, List<String> detalhes) {
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Erro> aoLancarStatus(ResponseStatusException e) {
        String mensagem = e.getReason() != null ? e.getReason() : "Erro";
        return ResponseEntity.status(e.getStatusCode())
                .body(new Erro(e.getStatusCode().value(), mensagem, List.of()));
    }

    @ExceptionHandler(PendenciasException.class)
    public ResponseEntity<Erro> aoTerPendencias(PendenciasException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new Erro(422, e.getMessage(), e.getPendencias()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Erro> aoValidar(MethodArgumentNotValidException e) {
        List<String> detalhes = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .toList();
        return ResponseEntity.badRequest().body(new Erro(400, "Dados inválidos", detalhes));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Erro> aoLerCorpo(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(new Erro(400,
                "Corpo da requisição inválido (JSON malformado, campo com tipo errado ou valor desconhecido)",
                List.of()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Erro> aoExcederUpload(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new Erro(413, "Arquivo maior que o tamanho permitido", List.of()));
    }
}
