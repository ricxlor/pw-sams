package br.com.sams.model.bo;

import br.com.sams.model.dao.UsuarioDAO;
import br.com.sams.model.dto.UsuarioAdminDTO;
import br.com.sams.model.entity.TipoUsuario;
import br.com.sams.model.entity.Usuario;
import io.quarkus.qute.Template;
import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@Dependent
public class AdminBO {

    private static final URI PAGINA_HOME = URI.create("/home");

    private final Template adminUsuarios;
    private final AuthBO authBO;
    private final UsuarioBO usuarioBO;
    private final UsuarioDAO usuarioDAO;

    public AdminBO(Template adminUsuarios, AuthBO authBO, UsuarioBO usuarioBO, UsuarioDAO usuarioDAO) {
        this.adminUsuarios = requireNonNull(adminUsuarios, "adminUsuarios is required");
        this.authBO = requireNonNull(authBO, "authBO is required");
        this.usuarioBO = requireNonNull(usuarioBO, "usuarioBO is required");
        this.usuarioDAO = requireNonNull(usuarioDAO, "usuarioDAO is required");
    }

    public Response paginaUsuarios(String userId) {
        Usuario usuarioLogado = authBO.usuarioDoCookie(userId);
        if (usuarioLogado == null) {
            return authBO.redirecionarParaLogin().build();
        }
        if (!ehAdmin(usuarioLogado)) {
            return Response.seeOther(PAGINA_HOME).build();
        }

        return Response.ok(adminUsuarios.data("storeName", "SAMS")
                        .data("nomeDoUsuario", usuarioLogado.getNome())
                        .data("idUsuarioLogado", usuarioLogado.getId())
                        .data("usuarios", usuarioDAO.findAll()))
                .build();
    }

    public Response criarUsuario(String userId, UsuarioAdminDTO dto) {
        Response negado = verificarAdmin(authBO.usuarioDoCookie(userId));
        if (negado != null) {
            return negado;
        }

        return usuarioBO.criarPeloAdmin(dto);
    }

    public Response atualizarUsuario(String userId, Integer id, UsuarioAdminDTO dto) {
        Usuario usuarioLogado = authBO.usuarioDoCookie(userId);
        Response negado = verificarAdmin(usuarioLogado);
        if (negado != null) {
            return negado;
        }
        if (usuarioLogado.getId().equals(id) && dto != null
                && !TipoUsuario.ADMIN.name().equals(dto.tipo())) {
            return erro(Response.Status.BAD_REQUEST, "Você não pode remover o seu próprio acesso de administrador.");
        }

        return usuarioBO.atualizar(id, dto);
    }

    public Response excluirUsuario(String userId, Integer id) {
        Usuario usuarioLogado = authBO.usuarioDoCookie(userId);
        Response negado = verificarAdmin(usuarioLogado);
        if (negado != null) {
            return negado;
        }
        if (usuarioLogado.getId().equals(id)) {
            return erro(Response.Status.BAD_REQUEST, "Você não pode excluir a sua própria conta.");
        }

        return usuarioBO.excluir(id);
    }

    private Response verificarAdmin(Usuario usuarioLogado) {
        if (usuarioLogado == null) {
            return erro(Response.Status.UNAUTHORIZED, "Sessão expirada. Faça login novamente.");
        }
        if (!ehAdmin(usuarioLogado)) {
            return erro(Response.Status.FORBIDDEN, "Acesso restrito a administradores.");
        }
        return null;
    }

    private static boolean ehAdmin(Usuario usuario) {
        return usuario.getTipo() == TipoUsuario.ADMIN;
    }

    private static Response erro(Response.Status status, String mensagem) {
        return Response.status(status)
                .entity(Map.of("error", mensagem))
                .build();
    }
}
