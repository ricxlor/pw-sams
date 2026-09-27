package br.com.sams.bootstrap;

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

    public Bootstrap(UsuarioBO usuarioBO) {
        this.usuarioBO = requireNonNull(usuarioBO, "usuarioBO is required");
    }

    @Transactional
    void onStart(@Observes StartupEvent ev) {
        if (usuarioBO.existeEmail("admin@sams.com")) {
            return;
        }

        usuarioBO.criarUsuario("Administrador SAMS", "admin@sams.com", "1234", TipoUsuario.ADMIN);
        usuarioBO.criarUsuario("Cliente SAMS", "cliente@sams.com", "1234", TipoUsuario.CLIENTE);
        Log.info("Bootstrap - usuários iniciais criados (senha: 1234)");
    }
}
