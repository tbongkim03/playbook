USE playbookdb;

CREATE TABLE tb_book (
    seq_book        INT             NOT NULL AUTO_INCREMENT,
    seq_sort_second INT             NOT NULL,
    isbn_book       VARCHAR(20)     NOT NULL,
    title_book      VARCHAR(255)    NOT NULL,
    author_book     VARCHAR(20)     NOT NULL,
    publisher_book  VARCHAR(20)     NOT NULL,
    publish_date_book DATE          NOT NULL,
    barcode_book    VARCHAR(30)     NULL UNIQUE,
    cnt_book        INT             NULL,
    PRIMARY KEY (seq_book)
);

CREATE TABLE tb_user (
    seq_user        INT             NOT NULL AUTO_INCREMENT,
    seq_course      INT             NOT NULL,
    id_user         VARCHAR(30)     NOT NULL UNIQUE,
    pw_user         VARCHAR(255)    NOT NULL,
    name_user       VARCHAR(20)     NOT NULL,
    dc_user         VARCHAR(30)     NOT NULL,
    agree_terms_user TINYINT(1)     NOT NULL DEFAULT 0,
    agree_info_user  TINYINT(1)     NOT NULL DEFAULT 0,
    status_user     ENUM('stop', 'available', 'overdue') COMMENT 'stop:정지or탈퇴, available:대여, overdue:연체' NOT NULL,
    PRIMARY KEY (seq_user)
);

CREATE TABLE tb_admin (
    seq_admin         INT             NOT NULL AUTO_INCREMENT,
    id_admin          VARCHAR(30)     NOT NULL UNIQUE,
    pw_admin          VARCHAR(255)    NOT NULL,
    name_admin        VARCHAR(20)     NOT NULL,
    dc_admin          VARCHAR(30)     NOT NULL,
    agree_terms_admin TINYINT(1)      NOT NULL DEFAULT 0,
    agree_info_admin  TINYINT(1)      NOT NULL DEFAULT 0,
    status_admin      ENUM('stop', 'available', 'overdue') COMMENT 'stop:정지, available:대여, overdue:연체' NOT NULL,
    PRIMARY KEY (seq_admin)
);

CREATE TABLE tb_history (
    seq_history     INT             NOT NULL AUTO_INCREMENT,
    seq_admin       INT             NOT NULL,
    seq_user        INT             NOT NULL,
    seq_course      INT             NOT NULL,
    seq_book        INT             NOT NULL,
    book_dt         DATE            NOT NULL,
    return_dt       DATE            NULL,
    PRIMARY KEY (seq_history)
);

CREATE TABLE tb_course (
    seq_course      INT             NOT NULL AUTO_INCREMENT,
    name_course     VARCHAR(30)     NOT NULL UNIQUE,
    start_dt_course DATE            NOT NULL,
    finish_dt_course DATE           NOT NULL,
    PRIMARY KEY (seq_course)
);

CREATE TABLE tb_sort_first (
    seq_sort_first  INT             NOT NULL AUTO_INCREMENT,
    kor_sort_first  VARCHAR(255)    NOT NULL,
    name_sort_first VARCHAR(255)    NOT NULL,
    PRIMARY KEY (seq_sort_first)
);

CREATE TABLE tb_sort_second (
    seq_sort_second  INT             NOT NULL AUTO_INCREMENT,
    seq_sort_first   INT             NOT NULL,
    kor_sort_second  VARCHAR(255)    NOT NULL,
    name_sort_second VARCHAR(255)    NOT NULL,
    PRIMARY KEY (seq_sort_second)
);

-- 외래키
ALTER TABLE tb_book ADD CONSTRAINT FK_tb_sort_second_TO_tb_book
FOREIGN KEY (seq_sort_second)
REFERENCES tb_sort_second (seq_sort_second);

ALTER TABLE tb_user ADD CONSTRAINT FK_tb_course_TO_tb_user
FOREIGN KEY (seq_course)
REFERENCES tb_course (seq_course);

ALTER TABLE tb_history ADD CONSTRAINT FK_tb_admin_TO_tb_history
FOREIGN KEY (seq_admin)
REFERENCES tb_admin (seq_admin);

ALTER TABLE tb_history ADD CONSTRAINT FK_tb_user_TO_tb_history
FOREIGN KEY (seq_user)
REFERENCES tb_user (seq_user);

ALTER TABLE tb_history ADD CONSTRAINT FK_tb_book_TO_tb_history
FOREIGN KEY (seq_book)
REFERENCES tb_book (seq_book);

ALTER TABLE tb_history ADD CONSTRAINT FK_tb_course_TO_tb_history
FOREIGN KEY (seq_course)
REFERENCES tb_course (seq_course);

ALTER TABLE tb_sort_second ADD CONSTRAINT FK_tb_sort_first_TO_tb_sort_second
FOREIGN KEY (seq_sort_first)
REFERENCES tb_sort_first(seq_sort_first)

