package br.com.sams.model.bo;

import br.com.sams.model.dao.UsuarioDAO;
import br.com.sams.model.dto.CadastroUsuarioDTO;
import br.com.sams.model.dto.UsuarioAdminDTO;
import br.com.sams.model.entity.TipoUsuario;
import br.com.sams.model.entity.Usuario;
import io.quarkus.qute.Template;
import jakarta.enterprise.context.Dependent;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

@Dependent
public class UsuarioBO {

    private static final int TAMANHO_MINIMO_SENHA = 6;
    private static final int TAMANHO_MAXIMO_NOME = 120;
    private static final Pattern FORMATO_EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final Template cadastro;
    private final UsuarioDAO usuarioDAO;
    private final BCryptPasswordEncoder passwordEncoder;
    private final LogBO logBO;

    public UsuarioBO(Template cadastro, UsuarioDAO usuarioDAO, BCryptPasswordEncoder passwordEncoder, LogBO logBO) {
        this.cadastro = requireNonNull(cadastro, "cadastro is required");
        this.usuarioDAO = requireNonNull(usuarioDAO, "usuarioDAO is required");
        this.passwordEncoder = requireNonNull(passwordEncoder, "passwordEncoder is required");
        this.logBO = requireNonNull(logBO, "logBO is required");
    }

    public Response paginaCadastro() {
        return Response.ok(cadastro.data("storeName", "SAMS")).build();
    }

    @Transactional
    public Response cadastrar(CadastroUsuarioDTO dto) {
        String erro = validarCadastro(dto);
        if (erro != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", erro))
                    .build();
        }

        String email = normalizarEmail(dto.email());
        if (usuarioDAO.findByEmail(email) != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", "Já existe uma conta com este e-mail."))
                    .build();
        }

        Usuario usuario = criarUsuario(dto.nome().trim(), email, dto.senha(), TipoUsuario.CLIENTE);
        logBO.registrarAcao(usuario, "CADASTRO_USUARIO - Novo usuário: " + usuario.getEmail());

        return Response.status(Response.Status.CREATED)
                .entity(Map.of("message", "Conta criada com sucesso!"))
                .build();
    }

    @Transactional
    public Usuario criarUsuario(String nome, String email, String senha, TipoUsuario tipo) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(normalizarEmail(email));
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setTipo(tipo);
        usuarioDAO.save(usuario);
        return usuario;
    }

    @Transactional
    public Response criarPeloAdmin(UsuarioAdminDTO dto) {
        String erro = validarDadosAdmin(dto);
        if (erro == null && estaVazio(dto.senha())) {
            erro = "Informe uma senha.";
        }
        if (erro != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", erro))
                    .build();
        }

        if (existeEmail(dto.email())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", "Já existe uma conta com este e-mail."))
                    .build();
        }

        criarUsuario(dto.nome().trim(), dto.email(), dto.senha(), TipoUsuario.valueOf(dto.tipo()));

        return Response.status(Response.Status.CREATED)
                .entity(Map.of("message", "Usuário criado com sucesso!"))
                .build();
    }

    @Transactional
    public Response atualizar(Integer id, UsuarioAdminDTO dto) {
        Usuario usuario = usuarioDAO.find(id);
        if (usuario == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Usuário não encontrado."))
                    .build();
        }

        String erro = validarDadosAdmin(dto);
        if (erro != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", erro))
                    .build();
        }

        String email = normalizarEmail(dto.email());
        Usuario donoDoEmail = usuarioDAO.findByEmail(email);
        if (donoDoEmail != null && !donoDoEmail.getId().equals(id)) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", "Já existe uma conta com este e-mail."))
                    .build();
        }

        usuario.setNome(dto.nome().trim());
        usuario.setEmail(email);
        usuario.setTipo(TipoUsuario.valueOf(dto.tipo()));
        if (!estaVazio(dto.senha())) {
            usuario.setSenha(passwordEncoder.encode(dto.senha()));
        }
        usuarioDAO.save(usuario);

        return Response.ok(Map.of("message", "Usuário atualizado com sucesso!")).build();
    }

    @Transactional
    public Response excluir(Integer id) {
        if (usuarioDAO.find(id) == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Usuário não encontrado."))
                    .build();
        }

        usuarioDAO.delete(id);
        return Response.ok(Map.of("message", "Usuário excluído com sucesso!")).build();
    }

    public boolean existeEmail(String email) {
        return usuarioDAO.findByEmail(normalizarEmail(email)) != null;
    }

    private String validarCadastro(CadastroUsuarioDTO dto) {
        if (dto == null || estaVazio(dto.nome()) || estaVazio(dto.email())
                || estaVazio(dto.senha()) || estaVazio(dto.confirmacaoSenha())) {
            return "Preencha todos os campos.";
        }
        if (dto.nome().trim().length() > TAMANHO_MAXIMO_NOME) {
            return "O nome deve ter no máximo " + TAMANHO_MAXIMO_NOME + " caracteres.";
        }
        if (!FORMATO_EMAIL.matcher(dto.email().trim()).matches()) {
            return "Informe um e-mail válido.";
        }
        if (dto.senha().length() < TAMANHO_MINIMO_SENHA) {
            return "A senha deve ter pelo menos " + TAMANHO_MINIMO_SENHA + " caracteres.";
        }
        if (!dto.senha().equals(dto.confirmacaoSenha())) {
            return "As senhas não coincidem.";
        }
        return null;
    }

    private String validarDadosAdmin(UsuarioAdminDTO dto) {
        if (dto == null || estaVazio(dto.nome()) || estaVazio(dto.email()) || estaVazio(dto.tipo())) {
            return "Preencha nome, e-mail e tipo.";
        }
        if (dto.nome().trim().length() > TAMANHO_MAXIMO_NOME) {
            return "O nome deve ter no máximo " + TAMANHO_MAXIMO_NOME + " caracteres.";
        }
        if (!FORMATO_EMAIL.matcher(dto.email().trim()).matches()) {
            return "Informe um e-mail válido.";
        }
        if (!tipoValido(dto.tipo())) {
            return "Tipo de usuário inválido.";
        }
        if (!estaVazio(dto.senha()) && dto.senha().length() < TAMANHO_MINIMO_SENHA) {
            return "A senha deve ter pelo menos " + TAMANHO_MINIMO_SENHA + " caracteres.";
        }
        return null;
    }

    private static boolean tipoValido(String tipo) {
        try {
            TipoUsuario.valueOf(tipo);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static boolean estaVazio(String valor) {
        return valor == null || valor.isBlank();
    }

    private static String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }
}
