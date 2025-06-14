FROM mysql:8.0

WORKDIR /app

COPY ./init/init.sql /docker-entrypoint-initdb.d/
COPY ./data/sort_label.sql /docker-entrypoint-initdb.d/
COPY ./conf/default.cnf /etc/mysql/conf.d/
