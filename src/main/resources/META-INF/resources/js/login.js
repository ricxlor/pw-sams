document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('loginForm');
    const erro = document.getElementById('loginErro');
    const botao = form.querySelector('button[type="submit"]');

    if (new URLSearchParams(window.location.search).has('cadastro')) {
        document.getElementById('loginSucesso').hidden = false;
    }

    const mostrarErro = (mensagem) => {
        erro.textContent = mensagem;
        erro.hidden = false;
    };

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        erro.hidden = true;

        const email = document.getElementById('email').value.trim();
        const senha = document.getElementById('senha').value;

        if (!email || !senha) {
            mostrarErro('Por favor, preencha o e-mail e a senha.');
            return;
        }

        botao.disabled = true;
        try {
            const response = await fetch('/auth/login', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({email, senha})
            });

            if (response.ok) {
                window.location.href = '/home';
            } else {
                mostrarErro('E-mail ou senha inválidos. Tente novamente.');
            }
        } catch (e) {
            console.error('Erro na requisição de login:', e);
            mostrarErro('Não foi possível conectar. Tente novamente.');
        } finally {
            botao.disabled = false;
        }
    });
});
