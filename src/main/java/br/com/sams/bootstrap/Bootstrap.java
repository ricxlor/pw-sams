package br.com.sams.bootstrap;

import br.com.sams.model.bo.LogBO;
import br.com.sams.model.bo.UsuarioBO;
import br.com.sams.model.entity.TipoUsuario;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;

import static java.util.Objects.requireNonNull;

@ApplicationScoped
public class Bootstrap {

    private final UsuarioBO usuarioBO;
    private final LogBO logBO;

    public Bootstrap(UsuarioBO usuarioBO, LogBO logBO) {
        this.usuarioBO = requireNonNull(usuarioBO, "usuarioBO is required");
        this.logBO = requireNonNull(logBO, "logBO is required");
    }

    @Transactional
    void onStart(@Observes StartupEvent ev) {
        if (usuarioBO.existeEmail("admin@sams.com")) {
            return;
        }

        usuarioBO.criarUsuario("Administrador SAMS", "admin@sams.com", "1234", TipoUsuario.ADMIN);
        usuarioBO.criarUsuario("Cliente SAMS", "cliente@sams.com", "1234", TipoUsuario.CLIENTE);
        logBO.registrarAcao(null, "BOOTSTRAP - Usuários iniciais (1 Admin, 1 Cliente) criados com sucesso.");
        Log.info("Bootstrap - usuários iniciais criados (senha: 1234)");
    }
}
