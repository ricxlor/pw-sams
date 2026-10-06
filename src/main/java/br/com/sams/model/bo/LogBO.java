package br.com.sams.model.bo;

import br.com.sams.model.dao.LogDAO;
import br.com.sams.model.dto.LogListDTO;
import br.com.sams.model.entity.Log;
import br.com.sams.model.entity.Usuario;
import jakarta.enterprise.context.Dependent;
import jakarta.transaction.Transactional;

import java.util.List;

import static java.util.Objects.requireNonNull;

@Dependent
public class LogBO {

    // Limita o texto vindo do usuário (ex.: e-mail digitado no login) para não inchar o log
    private static final int TAMANHO_MAXIMO_ACAO = 500;

    private final LogDAO logDAO;

    public LogBO(LogDAO logDAO) {
        this.logDAO = requireNonNull(logDAO, "logDAO is required");
    }

    @Transactional
    public void registrarAcao(Usuario usuario, String acao) {
        Log log = new Log();
        if (usuario != null) {
            log.setUsuarioId(usuario.getId());
            log.setUsuarioEmail(usuario.getEmail());
        }
        log.setAcao(acao.length() > TAMANHO_MAXIMO_ACAO ? acao.substring(0, TAMANHO_MAXIMO_ACAO) + "…" : acao);
        logDAO.save(log);
    }

    public List<LogListDTO> listarLogs() {
        return logDAO.listAll().stream()
                .map(LogListDTO::new)
                .toList();
    }
}
