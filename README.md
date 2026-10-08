# A Caminho - Monorepo

Sistema de gestão e acompanhamento de transporte universitário municipal.

Este repositório é organizado no formato **Monorepo**, contendo os módulos independentes de Backend (API REST) e Frontend (Web SPA), além dos arquivos de orquestração via Docker.

---

## Estrutura do Repositório

```text
a-caminho/
├── backend/                  # API REST com Spring Boot 3 + Java 21
│   ├── src/
│   ├── pom.xml
│   ├── mvnw
│   ├── Dockerfile
│   └── README.md
├── frontend/                 # Aplicação Web SPA com Angular 19 + TypeScript
│   ├── src/
│   ├── package.json
│   ├── angular.json
│   └── README.md
├── docker-compose.yml        # Orquestração do PostgreSQL e aplicação
├── .gitignore                # Regras de versionamento compartilhadas
└── README.md                 # Documentação principal do Monorepo
```

---

## Tecnologias

### Backend
- **Java 21**
- **Spring Boot 3.5** (Spring Web, Spring Data JPA, Spring Security, Validation)
- **PostgreSQL 18** + **Flyway Migrations**
- **Autenticação:** JWT (Stateless) e OAuth2 (Google)
- **Mapeamento & Modelagem:** MapStruct e Lombok
- **Documentação:** OpenAPI 3 / Swagger (SpringDoc OpenAPI)
- **Testes:** JUnit 5, Mockito, Spring Security Test, AssertJ

### Frontend
- **Angular 19**
- **TypeScript**
- **RxJS**

---

## Como Executar

### 1. Banco de Dados (Docker Compose)

Para subir apenas a instância do PostgreSQL configurada para o projeto:

```bash
docker compose up -d postgres
```

O banco de dados estará acessível em `localhost:5439` (mapeado para `5432` no container).

Para subir o ambiente completo (banco de dados + backend containerizado):

```bash
docker compose up --build
```

---

### 2. Backend (Desenvolvimento Local)

Certifique-se de que o banco de dados PostgreSQL esteja rodando (`docker compose up -d postgres`).

Navegue até a pasta do backend:

```bash
cd backend
```

Execute a aplicação via Maven Wrapper:

```bash
./mvnw clean spring-boot:run
```

A API estará disponível em: `http://localhost:8080`
- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

#### Executando Testes do Backend

```bash
cd backend
./mvnw clean test
```

---

### 3. Frontend (Desenvolvimento Local)

Navegue até a pasta do frontend:

```bash
cd frontend
```

Instale as dependências (se ainda não tiver instalado):

```bash
npm install
```

Inicie o servidor de desenvolvimento:

```bash
npm start
# ou: npx ng serve
```

A aplicação estará disponível em: `http://localhost:4200`

#### Compilando o Frontend para Produção

```bash
cd frontend
npm run build
```

