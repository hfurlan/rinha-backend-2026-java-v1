# Resumo
Programa para participar da Rinha backend 2026 (https://github.com/zanfranceschi/rinha-de-backend-2026/)

# Stack 

```
Banco de Dados - PostgreSQL
Linguagem - Java
```

# Estratégia

?

# Premissas

?

# Exemplos

## Payments

```
curl -d '{"correlationId": "XYZ", "amount" : 10.50}' -H "Content-Type: application/json" -X POST http://localhost:8080/payments
```

## Payments Summary

```
curl -H "Content-Type: application/json" -X GET http://localhost:8080/payments-summary
```

# Build imagem Docker

docker build -t rinha-backend-2026-java-v1 .

# Executar imagem Docker

docker run -it --rm --net="host" rinha-backend-2026-java-v1

# Push to Docker Hub

docker tag rinha-backend-2026-java-v1 hfurlan/rinha-backend-2026-java-v1:0.0.1
docker push hfurlan/rinha-backend-2026-java-v1:0.0.1

