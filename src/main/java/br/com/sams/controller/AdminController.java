package br.com.sams.controller;

import br.com.sams.model.bo.AdminBO;
import br.com.sams.model.bo.AuthBO;
import br.com.sams.model.dto.UsuarioAdminDTO;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import static java.util.Objects.requireNonNull;

@Path("/admin")
public class AdminController {

    private final AdminBO adminBO;

    public AdminController(AdminBO adminBO) {
        this.adminBO = requireNonNull(adminBO, "adminBO is required");
    }

    @GET
    @Path("/usuarios")
    @Produces(MediaType.TEXT_HTML)
    public Response getUsuariosPage(@CookieParam(AuthBO.COOKIE_USER_ID) String userId) {
        return adminBO.paginaUsuarios(userId);
    }

    @POST
    @Path("/usuarios")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response criarUsuario(@CookieParam(AuthBO.COOKIE_USER_ID) String userId,
                                 UsuarioAdminDTO dto) {
        return adminBO.criarUsuario(userId, dto);
    }

    @PUT
    @Path("/usuarios/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response atualizarUsuario(@CookieParam(AuthBO.COOKIE_USER_ID) String userId,
                                     @PathParam("id") Integer id,
                                     UsuarioAdminDTO dto) {
        return adminBO.atualizarUsuario(userId, id, dto);
    }

    @DELETE
    @Path("/usuarios/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response excluirUsuario(@CookieParam(AuthBO.COOKIE_USER_ID) String userId,
                                   @PathParam("id") Integer id) {
        return adminBO.excluirUsuario(userId, id);
    }
}
