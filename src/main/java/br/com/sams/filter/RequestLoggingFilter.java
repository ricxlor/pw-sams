package br.com.sams.filter;

import br.com.sams.model.bo.AuthBO;
import br.com.sams.model.bo.LogBO;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.ext.Provider;

import java.util.Set;

import static java.util.Objects.requireNonNull;

@Provider
public class RequestLoggingFilter implements ContainerRequestFilter {

    // Evita que a própria tela de logs gere uma linha nova a cada carregamento
    private static final Set<String> CAMINHOS_IGNORADOS = Set.of("/admin/logs/data");

    private final LogBO logBO;
    private final AuthBO authBO;

    public RequestLoggingFilter(LogBO logBO, AuthBO authBO) {
        this.logBO = requireNonNull(logBO, "logBO is required");
        this.authBO = requireNonNull(authBO, "authBO is required");
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getRequestUri().getPath();

        // Evita o log de recursos estáticos para não poluir o log
        if (path.startsWith("/css/") || path.startsWith("/js/") || CAMINHOS_IGNORADOS.contains(path)) {
            return;
        }

        Cookie cookie = requestContext.getCookies().get(AuthBO.COOKIE_USER_ID);
        String userId = cookie == null ? null : cookie.getValue();

        logBO.registrarAcao(authBO.usuarioDoCookie(userId),
                "ACESSO_PAGINA: " + requestContext.getMethod() + " " + path);
    }
}
