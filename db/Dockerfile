FROM mysql:8.0

WORKDIR /app

# 테이블 생성, 시간대 및 언어설정
COPY ./init/init.sql /docker-entrypoint-initdb.d/
COPY ./data/sort_label.sql /docker-entrypoint-initdb.d/
COPY ./conf/default.cnf /etc/mysql/conf.d/
