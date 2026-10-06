package br.com.sams.model.dto;

import br.com.sams.model.entity.Log;

import java.time.format.DateTimeFormatter;

public record LogListDTO(String dataHora, String usuarioEmail, String acao) {

    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public LogListDTO(Log log) {
        this(log.getDataHora().format(FORMATO_DATA_HORA),
                log.getUsuarioEmail() != null ? log.getUsuarioEmail() : "Sistema/Não autenticado",
                log.getAcao());
    }
}
