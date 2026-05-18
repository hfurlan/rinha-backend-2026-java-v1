CREATE EXTENSION IF NOT EXISTS vector;

create table frauds (
    fraud_vector vector(14) not null,
    ic_fraud boolean not null
);

create table load_control (
    id int primary key,
    status varchar(50) not null
);

--ivfflat
--CREATE INDEX ON frauds USING ivfflat (fraud_vector vector_cosine_ops) WITH (lists = 100);

--hnsw
--CREATE INDEX ON frauds USING hnsw (fraud_vector vector_l2_ops) WITH (m = 16, ef_construction = 64);
