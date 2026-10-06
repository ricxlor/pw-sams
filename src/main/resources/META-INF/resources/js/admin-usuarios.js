document.addEventListener('DOMContentLoaded', () => {
    const modalEditar = document.getElementById('modalEditar');
    const formEditar = document.getElementById('formEditar');
    const erroEditar = document.getElementById('editarErro');
    const botaoSalvar = formEditar.querySelector('button[type="submit"]');
    const campoTipo = document.getElementById('editarTipo');
    const idUsuarioLogado = modalEditar.dataset.idUsuarioLogado;

    const modalExcluir = document.getElementById('modalExcluir');
    const erroExcluir = document.getElementById('excluirErro');
    const botaoConfirmarExcluir = document.getElementById('confirmarExcluir');

    let usuarioSelecionado = null;

    const mostrarErro = (elemento, mensagem) => {
        elemento.textContent = mensagem;
        elemento.hidden = false;
    };

    const lerErro = async (response, padrao) => {
        const corpo = await response.json().catch(() => ({}));
        return corpo.error || padrao;
    };

    // Sem usuário = modo de criação; com usuário = modo de edição
    const abrirFormulario = (usuario) => {
        usuarioSelecionado = usuario;
        const criando = usuario === null;
        erroEditar.hidden = true;
        document.getElementById('editarEyebrow').textContent = criando ? 'Novo usuário' : 'Editar usuário';
        document.getElementById('editarTitulo').textContent = criando ? 'Cadastrar usuário' : usuario.nome;
        document.getElementById('editarSenhaLabel').textContent = criando ? 'Senha' : 'Nova senha';
        document.getElementById('editarSenha').placeholder = criando
            ? 'Mínimo de 6 caracteres'
            : 'Deixe em branco para manter a atual';
        document.getElementById('editarNome').value = criando ? '' : usuario.nome;
        document.getElementById('editarEmail').value = criando ? '' : usuario.email;
        document.getElementById('editarSenha').value = '';
        campoTipo.value = criando ? 'CLIENTE' : usuario.tipo;
        // O admin não pode remover o próprio acesso de administrador
        campoTipo.disabled = !criando && usuario.id === idUsuarioLogado;
        modalEditar.showModal();
    };

    document.getElementById('btnNovoUsuario').addEventListener('click', () => abrirFormulario(null));

    const abrirExclusao = (usuario) => {
        usuarioSelecionado = usuario;
        erroExcluir.hidden = true;
        document.getElementById('excluirNome').textContent = usuario.nome;
        modalExcluir.showModal();
    };

    document.querySelector('.admin-table').addEventListener('click', (event) => {
        const botao = event.target.closest('button[data-acao]');
        if (!botao) {
            return;
        }

        const usuario = {...botao.closest('tr').dataset};
        if (botao.dataset.acao === 'editar') {
            abrirFormulario(usuario);
        } else if (botao.dataset.acao === 'excluir') {
            abrirExclusao(usuario);
        }
    });

    document.querySelectorAll('[data-fechar]').forEach((botao) => {
        botao.addEventListener('click', () => botao.closest('dialog').close());
    });

    formEditar.addEventListener('submit', async (event) => {
        event.preventDefault();
        erroEditar.hidden = true;

        const dados = {
            nome: document.getElementById('editarNome').value.trim(),
            email: document.getElementById('editarEmail').value.trim(),
            tipo: campoTipo.value,
            senha: document.getElementById('editarSenha').value
        };

        if (!dados.nome || !dados.email) {
            mostrarErro(erroEditar, 'Preencha nome e e-mail.');
            return;
        }
        const criando = usuarioSelecionado === null;
        if (criando && !dados.senha) {
            mostrarErro(erroEditar, 'Informe uma senha.');
            return;
        }
        if (dados.senha && dados.senha.length < 6) {
            mostrarErro(erroEditar, 'A senha deve ter pelo menos 6 caracteres.');
            return;
        }

        botaoSalvar.disabled = true;
        try {
            const response = await fetch(criando ? '/admin/usuarios' : `/admin/usuarios/${usuarioSelecionado.id}`, {
                method: criando ? 'POST' : 'PUT',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(dados)
            });

            if (response.ok) {
                window.location.reload();
                return;
            }

            mostrarErro(erroEditar, await lerErro(response, 'Não foi possível salvar. Tente novamente.'));
        } catch (e) {
            console.error('Erro na requisição de salvar usuário:', e);
            mostrarErro(erroEditar, 'Não foi possível conectar. Tente novamente.');
        } finally {
            botaoSalvar.disabled = false;
        }
    });

    botaoConfirmarExcluir.addEventListener('click', async () => {
        erroExcluir.hidden = true;
        botaoConfirmarExcluir.disabled = true;
        try {
            const response = await fetch(`/admin/usuarios/${usuarioSelecionado.id}`, {
                method: 'DELETE'
            });

            if (response.ok) {
                window.location.reload();
                return;
            }

            mostrarErro(erroExcluir, await lerErro(response, 'Não foi possível excluir. Tente novamente.'));
        } catch (e) {
            console.error('Erro na requisição de exclusão:', e);
            mostrarErro(erroExcluir, 'Não foi possível conectar. Tente novamente.');
        } finally {
            botaoConfirmarExcluir.disabled = false;
        }
    });
});
