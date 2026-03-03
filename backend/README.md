# Bellator Barbearia API (Java / Spring Boot)

Backend **pronto para subir no GitHub** e integrar com a sua Bellator Barbearia V5 (SPA).
- Auth JWT (Cliente/Barbeiro/Admin)
- Serviços
- Barbeiros
- Agendamentos com bloqueio de conflito
- Relatório admin (faturamento básico)
- Swagger UI

## Requisitos
- Java 17
- Maven 3.9+

---

## Rodar em DEV (H2, mais rápido)
```bash
mvn -Dspring-boot.run.profiles=dev spring-boot:run
```

- Swagger: http://localhost:8080/swagger-ui/index.html  
- H2 console: http://localhost:8080/h2

### Contas demo (DEV)
- **Admin**: admin@bellator.dev / 123456
- **Barbeiro**: barber@bellator.dev / 123456
- **Cliente**: client@bellator.dev / 123456

---

## Rodar em PROD (PostgreSQL via Docker)
```bash
docker compose up --build
```

Swagger: http://localhost:8080/swagger-ui/index.html

> Troque `JWT_SECRET` no `docker-compose.yml`.

---

## Como autenticar
1) `POST /auth/login`  
2) Copie `token`  
3) Use nos endpoints:
```
Authorization: Bearer SEU_TOKEN
```

---

## CORS (para GitHub Pages)
Defina:
- `CORS_ALLOWED_ORIGINS="http://localhost:5173,https://taylortech2024.github.io"`

---

## Endpoints principais
- `GET /servicos`
- `GET /barbeiros`
- `POST /agendamentos` (CLIENTE)
- `GET /agendamentos/me` (CLIENTE)
- `GET /agendamentos/barbeiro?data=2026-03-03` (BARBEIRO)
- `PUT /agendamentos/{id}/concluir` (BARBEIRO)
- `PUT /agendamentos/{id}/cancelar` (CLIENTE/ADMIN)
- `GET /admin/relatorio` (ADMIN)

