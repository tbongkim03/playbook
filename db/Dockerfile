FROM mysql:8.0

WORKDIR /app

COPY ./init/init.sql /docker-entrypoint-initdb.d/ 
COPY ./conf/default.conf /etc/mysql/conf.d/
