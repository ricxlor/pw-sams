document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('cadastroForm');
    const erro = document.getElementById('cadastroErro');
    const botao = form.querySelector('button[type="submit"]');

    const mostrarErro = (mensagem) => {
        erro.textContent = mensagem;
        erro.hidden = false;
    };

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        erro.hidden = true;

        const dados = {
            nome: document.getElementById('nome').value.trim(),
            email: document.getElementById('email').value.trim(),
            senha: document.getElementById('senha').value,
            confirmacaoSenha: document.getElementById('confirmacaoSenha').value
        };

        if (!dados.nome || !dados.email || !dados.senha || !dados.confirmacaoSenha) {
            mostrarErro('Preencha todos os campos.');
            return;
        }
        if (dados.senha !== dados.confirmacaoSenha) {
            mostrarErro('As senhas não coincidem.');
            return;
        }

        botao.disabled = true;
        try {
            const response = await fetch('/usuarios', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(dados)
            });

            if (response.ok) {
                window.location.href = '/auth/login?cadastro=ok';
                return;
            }

            const corpo = await response.json().catch(() => ({}));
            mostrarErro(corpo.error || 'Não foi possível criar a conta. Tente novamente.');
        } catch (e) {
            console.error('Erro na requisição de cadastro:', e);
            mostrarErro('Não foi possível conectar. Tente novamente.');
        } finally {
            botao.disabled = false;
        }
    });
});
