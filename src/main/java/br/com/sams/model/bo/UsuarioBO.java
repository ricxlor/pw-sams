package br.com.sams.model.bo;

import br.com.sams.model.dao.UsuarioDAO;
import br.com.sams.model.dto.CadastroUsuarioDTO;
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

    public UsuarioBO(Template cadastro, UsuarioDAO usuarioDAO, BCryptPasswordEncoder passwordEncoder) {
        this.cadastro = requireNonNull(cadastro, "cadastro is required");
        this.usuarioDAO = requireNonNull(usuarioDAO, "usuarioDAO is required");
        this.passwordEncoder = requireNonNull(passwordEncoder, "passwordEncoder is required");
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

        criarUsuario(dto.nome().trim(), email, dto.senha(), TipoUsuario.CLIENTE);

        return Response.status(Response.Status.CREATED)
                .entity(Map.of("message", "Conta criada com sucesso!"))
                .build();
    }

    @Transactional
    public void criarUsuario(String nome, String email, String senha, TipoUsuario tipo) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(normalizarEmail(email));
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setTipo(tipo);
        usuarioDAO.save(usuario);
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

    private static boolean estaVazio(String valor) {
        return valor == null || valor.isBlank();
    }

    private static String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }
}
