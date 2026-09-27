package br.com.sams.model.bo;

import br.com.sams.model.dao.UsuarioDAO;
import br.com.sams.model.dto.LoginDTO;
import br.com.sams.model.entity.Usuario;
import io.quarkus.qute.Template;
import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.net.URI;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@Dependent
public class AuthBO {

    public static final String COOKIE_USER_ID = "userId";
    private static final int DURACAO_SESSAO_SEGUNDOS = 3600;
    private static final URI PAGINA_LOGIN = URI.create("/auth/login");

    private final Template login;
    private final UsuarioDAO usuarioDAO;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthBO(Template login, UsuarioDAO usuarioDAO, BCryptPasswordEncoder passwordEncoder) {
        this.login = requireNonNull(login, "login is required");
        this.usuarioDAO = requireNonNull(usuarioDAO, "usuarioDAO is required");
        this.passwordEncoder = requireNonNull(passwordEncoder, "passwordEncoder is required");
    }

    public Response paginaLogin() {
        return Response.ok(login.data("storeName", "SAMS")).build();
    }

    public Response login(LoginDTO loginDTO) {
        Usuario usuario = loginDTO == null ? null : validarLogin(loginDTO.email(), loginDTO.senha());

        if (usuario == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "E-mail ou senha inválidos"))
                    .build();
        }

        return Response.ok(Map.of("message", "Login bem-sucedido!"))
                .cookie(cookieSessao(usuario.getId().toString(), DURACAO_SESSAO_SEGUNDOS))
                .build();
    }

    public Response logout() {
        return redirecionarParaLogin()
                .cookie(cookieSessao("", 0))
                .build();
    }

    public Usuario usuarioDoCookie(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        try {
            return usuarioDAO.find(Integer.parseInt(userId));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Response.ResponseBuilder redirecionarParaLogin() {
        return Response.seeOther(PAGINA_LOGIN);
    }

    private Usuario validarLogin(String email, String senha) {
        if (email == null || senha == null) {
            return null;
        }

        Usuario usuario = usuarioDAO.findByEmail(email.trim().toLowerCase());
        if (usuario != null && passwordEncoder.matches(senha, usuario.getSenha())) {
            return usuario;
        }
        return null;
    }

    private NewCookie cookieSessao(String valor, int maxAge) {
        return new NewCookie.Builder(COOKIE_USER_ID)
                .value(valor)
                .path("/")
                .maxAge(maxAge)
                .httpOnly(true)
                .sameSite(NewCookie.SameSite.LAX)
                .build();
    }
}
