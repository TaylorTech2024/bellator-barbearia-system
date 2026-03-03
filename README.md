# Bellator Barbearia (Fullstack) — Frontend V5 + API Java

Repositório **completo** (monorepo) pronto para subir no GitHub:
- `frontend/` = Bellator Barbearia V5 (HTML/CSS/JS) já integrado com API
- `backend/` = Spring Boot (JWT, Swagger, H2 dev + PostgreSQL prod, Docker)

## ✅ Rodar tudo com Docker (recomendado)
```bash
docker compose up --build
```

- Frontend: http://localhost:5173
- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui/index.html

### Contas demo (DEV)
No modo docker (prod), não tem seed automático.
Para testar rápido sem docker, rode o backend em `dev` (H2) e use:
- Admin: admin@bellator.dev / 123456
- Barbeiro: barber@bellator.dev / 123456
- Cliente: client@bellator.dev / 123456

## Rodar local sem Docker
### Backend (DEV)
```bash
cd backend
mvn -Dspring-boot.run.profiles=dev spring-boot:run
```

### Frontend (servidor simples)
```bash
cd frontend
python -m http.server 5173
```

## Configurar URL da API no Frontend
O frontend usa por padrão `http://localhost:8080`.

Se você for hospedar a API em outro lugar, abra o console do navegador e rode:
```js
localStorage.setItem("bellator_api_base", "https://SUA-API-AQUI")
```

## Integração importante
- O frontend consulta horários ocupados via endpoint público:
  - `GET /agenda?barbeiroId=ID&data=YYYY-MM-DD`
- Agendamento é criado com auth JWT:
  - `POST /agendamentos`

## Subir no GitHub
É só dar push da pasta inteira.
