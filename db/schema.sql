CREATE TABLE customer (
    id         BIGINT GENERATED ALWAYS AS IDENTITY,
    first_name VARCHAR(100)        NOT NULL,
    last_name  VARCHAR(100)        NOT NULL,
    email      VARCHAR(255)        NOT NULL,
    created_at TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP           NOT NULL DEFAULT NOW(),
    blocked    BOOLEAN             NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_customer PRIMARY KEY (id),
    CONSTRAINT uq_customer_email UNIQUE (email)
);

CREATE TABLE orders (
    id           BIGINT         GENERATED ALWAYS AS IDENTITY,
    order_status varchar(16)    NOT NULL check (order_status in ('PENDING','PLACED', 'PAID', 'CANCELLED')),
    order_date   TIMESTAMP      NOT NULL DEFAULT NOW(),
    customer_id  BIGINT         NOT NULL,

    CONSTRAINT pk_order PRIMARY KEY (id),
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES customer(id)
);

CREATE TABLE article (
    id                  BIGINT          GENERATED ALWAYS AS IDENTITY,
    article_name        VARCHAR(100)    NOT NULL,
    price               NUMERIC(10,2)   NOT NULL,

    CONSTRAINT pk_article PRIMARY KEY (id)
);

CREATE TABLE order_positions (
    id           BIGINT         GENERATED ALWAYS AS IDENTITY,
    quantity     BIGINT         NOT NULL,
    price        NUMERIC(10,2)  NOT NULL,
    order_id     BIGINT         NOT NULL,
    article_id   BIGINT         NOT NULL,

    CONSTRAINT pk_order_positions PRIMARY KEY (id),
    CONSTRAINT fk_order_positions_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_order_positions_article FOREIGN KEY (article_id) REFERENCES article(id)
);


CREATE TABLE payment ( 
    id          BIGINT    GENERATED ALWAYS AS IDENTITY,
    amount      NUMERIC(10,2)    NOT NULL,
    date        TIMESTAMP NOT NULL DEFAULT NOW(),
    customer_id BIGINT NOT NULL,

    CONSTRAINT pk_payment PRIMARY KEY (id),
    CONSTRAINT fk_payment_customer FOREIGN KEY (customer_id) REFERENCES customer(id)
); 
