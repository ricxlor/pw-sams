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
    private final Template adminLogs;
    private final AuthBO authBO;
    private final UsuarioBO usuarioBO;
    private final LogBO logBO;
    private final UsuarioDAO usuarioDAO;

    public AdminBO(Template adminUsuarios, Template adminLogs, AuthBO authBO, UsuarioBO usuarioBO,
                   LogBO logBO, UsuarioDAO usuarioDAO) {
        this.adminUsuarios = requireNonNull(adminUsuarios, "adminUsuarios is required");
        this.adminLogs = requireNonNull(adminLogs, "adminLogs is required");
        this.authBO = requireNonNull(authBO, "authBO is required");
        this.usuarioBO = requireNonNull(usuarioBO, "usuarioBO is required");
        this.logBO = requireNonNull(logBO, "logBO is required");
        this.usuarioDAO = requireNonNull(usuarioDAO, "usuarioDAO is required");
    }

    public Response paginaUsuarios(String userId) {
        Usuario usuarioLogado = authBO.usuarioDoCookie(userId);
        Response negado = verificarAcessoPagina(usuarioLogado, "/admin/usuarios");
        if (negado != null) {
            return negado;
        }

        return Response.ok(adminUsuarios.data("storeName", "SAMS")
                        .data("nomeDoUsuario", usuarioLogado.getNome())
                        .data("idUsuarioLogado", usuarioLogado.getId())
                        .data("usuarios", usuarioDAO.findAll()))
                .build();
    }

    public Response paginaLogs(String userId) {
        Usuario usuarioLogado = authBO.usuarioDoCookie(userId);
        Response negado = verificarAcessoPagina(usuarioLogado, "/admin/logs");
        if (negado != null) {
            return negado;
        }

        return Response.ok(adminLogs.data("storeName", "SAMS")
                        .data("nomeDoUsuario", usuarioLogado.getNome()))
                .build();
    }

    public Response dadosLogs(String userId) {
        Response negado = verificarAdmin(authBO.usuarioDoCookie(userId), "/admin/logs/data");
        if (negado != null) {
            return negado;
        }

        return Response.ok(logBO.listarLogs()).build();
    }

    public Response criarUsuario(String userId, UsuarioAdminDTO dto) {
        Usuario usuarioLogado = authBO.usuarioDoCookie(userId);
        Response negado = verificarAdmin(usuarioLogado, "criar usuário");
        if (negado != null) {
            return negado;
        }

        Response resposta = usuarioBO.criarPeloAdmin(dto);
        if (sucesso(resposta)) {
            Usuario criado = usuarioDAO.findByEmail(dto.email().trim().toLowerCase());
            logBO.registrarAcao(usuarioLogado, "ADMIN_CRIAR_USUARIO - " + descrever(criado));
        }
        return resposta;
    }

    public Response atualizarUsuario(String userId, Integer id, UsuarioAdminDTO dto) {
        Usuario usuarioLogado = authBO.usuarioDoCookie(userId);
        Response negado = verificarAdmin(usuarioLogado, "editar usuário");
        if (negado != null) {
            return negado;
        }
        if (usuarioLogado.getId().equals(id) && dto != null
                && !TipoUsuario.ADMIN.name().equals(dto.tipo())) {
            return erro(Response.Status.BAD_REQUEST, "Você não pode remover o seu próprio acesso de administrador.");
        }

        Response resposta = usuarioBO.atualizar(id, dto);
        if (sucesso(resposta)) {
            logBO.registrarAcao(usuarioLogado, "ADMIN_EDITAR_USUARIO - " + descrever(usuarioDAO.find(id)));
        }
        return resposta;
    }

    public Response excluirUsuario(String userId, Integer id) {
        Usuario usuarioLogado = authBO.usuarioDoCookie(userId);
        Response negado = verificarAdmin(usuarioLogado, "excluir usuário");
        if (negado != null) {
            return negado;
        }
        if (usuarioLogado.getId().equals(id)) {
            return erro(Response.Status.BAD_REQUEST, "Você não pode excluir a sua própria conta.");
        }

        // Guarda os dados antes de excluir, para o log saber quem foi removido
        Usuario alvo = usuarioDAO.find(id);
        Response resposta = usuarioBO.excluir(id);
        if (sucesso(resposta)) {
            logBO.registrarAcao(usuarioLogado, "ADMIN_EXCLUIR_USUARIO - " + descrever(alvo));
        }
        return resposta;
    }

    private Response verificarAcessoPagina(Usuario usuarioLogado, String pagina) {
        if (usuarioLogado == null) {
            return authBO.redirecionarParaLogin().build();
        }
        if (!ehAdmin(usuarioLogado)) {
            logBO.registrarAcao(usuarioLogado, "ACESSO_NEGADO - " + pagina);
            return Response.seeOther(PAGINA_HOME).build();
        }
        return null;
    }

    private Response verificarAdmin(Usuario usuarioLogado, String operacao) {
        if (usuarioLogado == null) {
            logBO.registrarAcao(null, "ACESSO_NEGADO - " + operacao + " sem sessão");
            return erro(Response.Status.UNAUTHORIZED, "Sessão expirada. Faça login novamente.");
        }
        if (!ehAdmin(usuarioLogado)) {
            logBO.registrarAcao(usuarioLogado, "ACESSO_NEGADO - " + operacao);
            return erro(Response.Status.FORBIDDEN, "Acesso restrito a administradores.");
        }
        return null;
    }

    private static boolean ehAdmin(Usuario usuario) {
        return usuario.getTipo() == TipoUsuario.ADMIN;
    }

    private static boolean sucesso(Response resposta) {
        return resposta.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL;
    }

    private static String descrever(Usuario usuario) {
        return "ID " + usuario.getId() + " (" + usuario.getEmail() + ", " + usuario.getTipo() + ")";
    }

    private static Response erro(Response.Status status, String mensagem) {
        return Response.status(status)
                .entity(Map.of("error", mensagem))
                .build();
    }
}
