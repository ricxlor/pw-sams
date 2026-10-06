document.addEventListener('DOMContentLoaded', () => {
    const tabelaCorpo = document.querySelector('#logTable tbody');
    const filtro = document.getElementById('filtro');
    const erro = document.getElementById('logsErro');

    // Usa textContent (e não innerHTML): a ação pode conter texto digitado por qualquer pessoa,
    // como o e-mail de uma tentativa de login
    const criarCelula = (texto, classe) => {
        const td = document.createElement('td');
        td.textContent = texto;
        if (classe) {
            td.className = classe;
        }
        return td;
    };

    const linhaVazia = (mensagem) => {
        const tr = document.createElement('tr');
        const td = criarCelula(mensagem, 'admin-vazio');
        td.colSpan = 3;
        tr.appendChild(td);
        return tr;
    };

    const filtrarTabela = () => {
        const termo = filtro.value.trim().toLowerCase();
        tabelaCorpo.querySelectorAll('tr[data-log]').forEach((linha) => {
            linha.hidden = !linha.textContent.toLowerCase().includes(termo);
        });
    };

    const carregarLogs = async () => {
        try {
            const response = await fetch('/admin/logs/data');
            if (!response.ok) {
                throw new Error('Falha ao carregar os logs. Você tem permissão para ver esta página?');
            }

            const logs = await response.json();
            tabelaCorpo.replaceChildren();

            if (logs.length === 0) {
                tabelaCorpo.appendChild(linhaVazia('Nenhum log registrado.'));
                return;
            }

            logs.forEach((log) => {
                const tr = document.createElement('tr');
                tr.dataset.log = '';
                tr.append(
                    criarCelula(log.dataHora, 'col-data'),
                    criarCelula(log.usuarioEmail),
                    criarCelula(log.acao, 'col-acao-log')
                );
                tabelaCorpo.appendChild(tr);
            });
            filtrarTabela();
        } catch (e) {
            console.error('Erro ao carregar logs:', e);
            tabelaCorpo.replaceChildren(linhaVazia('Não foi possível carregar os logs.'));
            erro.textContent = e.message;
            erro.hidden = false;
        }
    };

    filtro.addEventListener('input', filtrarTabela);
    carregarLogs();
});
