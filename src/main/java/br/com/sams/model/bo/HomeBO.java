package br.com.sams.model.bo;

import br.com.sams.model.Category;
import br.com.sams.model.entity.Usuario;
import io.quarkus.qute.Template;
import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.core.Response;

import java.util.List;

import static java.util.Objects.requireNonNull;

@Dependent
public class HomeBO {

    private final Template home;
    private final AuthBO authBO;

    public HomeBO(Template home, AuthBO authBO) {
        this.home = requireNonNull(home, "home is required");
        this.authBO = requireNonNull(authBO, "authBO is required");
    }

    public Response paginaHome(String userId) {
        Usuario usuarioLogado = authBO.usuarioDoCookie(userId);
        if (usuarioLogado == null) {
            return authBO.redirecionarParaLogin().build();
        }

        return Response.ok(home.data("storeName", "SAMS")
                        .data("tagline", "Alta Costura Parisiense desde 1954")
                        .data("categories", categorias())
                        .data("nomeDoUsuario", usuarioLogado.getNome()))
                .build();
    }

    private List<Category> categorias() {
        return List.of(
                new Category("Prêt-à-Porter",
                        "Peças autorais que unem alfaiataria clássica e ousadia contemporânea."),
                new Category("Acessórios",
                        "Bolsas, lenços e joias que assinam cada look com identidade única."),
                new Category("Alta Costura",
                        "Criações sob medida, confeccionadas à mão em nosso ateliê exclusivo.")
        );
    }
}
