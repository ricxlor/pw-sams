package br.com.sams.controller;

import br.com.sams.model.bo.AuthBO;
import br.com.sams.model.dto.LoginDTO;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import static java.util.Objects.requireNonNull;

@Path("/auth")
public class AuthController {

    private final AuthBO authBO;

    public AuthController(AuthBO authBO) {
        this.authBO = requireNonNull(authBO, "authBO is required");
    }

    @GET
    @Path("/login")
    @Produces(MediaType.TEXT_HTML)
    public Response getLoginPage() {
        return authBO.paginaLogin();
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response doLogin(LoginDTO loginDTO) {
        return authBO.login(loginDTO);
    }

    @GET
    @Path("/logout")
    public Response doLogout() {
        return authBO.logout();
    }
}
