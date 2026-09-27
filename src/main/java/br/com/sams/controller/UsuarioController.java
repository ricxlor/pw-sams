package br.com.sams.controller;

import br.com.sams.model.bo.UsuarioBO;
import br.com.sams.model.dto.CadastroUsuarioDTO;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import static java.util.Objects.requireNonNull;

@Path("/usuarios")
public class UsuarioController {

    private final UsuarioBO usuarioBO;

    public UsuarioController(UsuarioBO usuarioBO) {
        this.usuarioBO = requireNonNull(usuarioBO, "usuarioBO is required");
    }

    @GET
    @Path("/cadastro")
    @Produces(MediaType.TEXT_HTML)
    public Response getCadastroPage() {
        return usuarioBO.paginaCadastro();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response cadastrar(CadastroUsuarioDTO dto) {
        return usuarioBO.cadastrar(dto);
    }
}
