####
# Imagem do banco de dados do SAMS (PostgreSQL 16).
#
# Construir e subir (a partir da raiz do projeto):
#   docker compose up -d --build
#
# As tabelas são criadas/atualizadas pelo Hibernate ao subir a aplicação
# (quarkus.hibernate-orm.schema-management.strategy=update).
####
FROM postgres:16

# Locale pt_BR para ordenação e comparação corretas de textos com acentos
RUN localedef -i pt_BR -c -f UTF-8 -A /usr/share/locale/locale.alias pt_BR.UTF-8

ENV LANG=pt_BR.utf8 \
    TZ=America/Sao_Paulo \
    PGTZ=America/Sao_Paulo \
    POSTGRES_DB=sams \
    POSTGRES_USER=postgres \
    POSTGRES_PASSWORD=postgres

# Scripts executados apenas na primeira inicialização (volume vazio)
COPY init/ /docker-entrypoint-initdb.d/

HEALTHCHECK --interval=5s --timeout=3s --start-period=10s --retries=10 \
    CMD pg_isready -U "$POSTGRES_USER" -d "$POSTGRES_DB" || exit 1
