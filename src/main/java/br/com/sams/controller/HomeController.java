package br.com.sams.controller;

import br.com.sams.model.bo.AuthBO;
import br.com.sams.model.bo.HomeBO;

import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import static java.util.Objects.requireNonNull;

@Path("/home")
public class HomeController {

    private final HomeBO homeBO;

    public HomeController(HomeBO homeBO) {
        this.homeBO = requireNonNull(homeBO, "homeBO is required");
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response index(@CookieParam(AuthBO.COOKIE_USER_ID) String userId) {
        return homeBO.paginaHome(userId);
    }
}
