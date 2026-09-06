# 🛒 E-commerce API

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.12-brightgreen?logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue?logo=postgresql)
![Go](https://img.shields.io/badge/Go-1.26-00ADD8?logo=go)
![Redis](https://img.shields.io/badge/Redis-7-red?logo=redis)
![Docker](https://img.shields.io/badge/Docker-ready-2496ED?logo=docker)
> Uma API de e-commerce orientada à produção, construída com Java e Spring Boot, simulando um backend real com autenticação, catálogo, carrinho, checkout, pedidos, pagamentos e um API Gateway próprio em Go.

Desafio base: [roadmap.sh/projects/ecommerce-api](https://roadmap.sh/projects/ecommerce-api)

---

## 📑 Índice

- [Sobre o Projeto](#-sobre-o-projeto)
- [Tecnologias Utilizadas](#-tecnologias-utilizadas)
- [Arquitetura](#-arquitetura)
- [Pré-requisitos](#-pré-requisitos)
- [Instalação e Como Executar](#-instalação-e-como-executar)
- [Variáveis de Ambiente](#-variáveis-de-ambiente)
- [Como Usar](#-como-usar)
- [Testes](#-testes)
- [Documentação da API](#-documentação-da-api)
- [Contribuindo](#-contribuindo)

---

## 📖 Sobre o Projeto

O **E-commerce API** simula um backend de e-commerce real, indo além do CRUD básico para aplicar arquitetura limpa, modelagem de domínio consistente e práticas de engenharia voltadas para produção.

### ✨ Principais Funcionalidades

- 🔐 **Autenticação e Autorização**
  - JWT (access token) + Refresh Token com rotação e detecção de reuso (token family)
  - Controle de acesso baseado em roles (`ADMIN`, `USER`) com hierarquia de permissões
- 🏠 **Gestão de Endereços**
  - CRUD de endereços com validação automática via API ViaCEP
  - Suporte a múltiplos tipos de endereço (SHIPPING, BILLING, RESIDENTIAL, etc.)
- 📦 **Catálogo de Produtos**
  - Categorias, geração automática de SKU, controle de estoque atômico
  - Máquina de estados para status do produto (DRAFT, AVAILABLE, OUT_OF_STOCK, INACTIVE)
- 🛍️ **Carrinho de Compras**
  - Adição/remoção/atualização de itens com lock pessimista para concorrência
  - Limpeza automática de carrinhos abandonados (job agendado)
- 📑 **Pedidos (Orders)**
  - Criação a partir do carrinho com validação de endereços e estoque
  - Rastreamento com timeline de eventos, cancelamento com regras de negócio
  - Eventos de domínio (`OrderCreatedEvent`, `OrderPaidEvent`, `OrderCancelledEvent`)
- 💳 **Pagamentos**
  - Múltiplos gateways via Strategy Pattern (**Stripe** e **AbacatePay/PIX**)
  - Webhooks com verificação de assinatura
  - Retry automático com backoff exponencial para falhas de checkout
  - Reembolsos (refunds) com validação de valor e status
- 🚪 **API Gateway (Go)**
  - Reverse proxy com rate limiting (sliding window) via Redis
- 🛡️ **Produção**
  - Tratamento global de exceções com respostas de erro padronizadas
  - Migrations versionadas com Flyway
  - Documentação interativa via Swagger/OpenAPI
  - Containerização com Docker

---

## 🛠 Tecnologias Utilizadas

**Backend Principal**
- Java 21
- Spring Boot 3.5.12 (Web, Security, Data JPA, Validation, Mail, Actuator)
- Hibernate / JPA
- PostgreSQL
- Flyway (migrations)
- Auth0 java-jwt
- Stripe Java SDK
- Springdoc OpenAPI (Swagger UI)
- Lombok
- Maven

**API Gateway**
- Golang
- Redis (rate limiting)
- Gin / net/http

**Infraestrutura**
- Docker & Docker Compose
- Stripe CLI (webhooks locais)

**Testes**
- JUnit 5
- Spring Boot Test
- H2 Database (in-memory)
- Spring Security Test

---

## 🏗 Arquitetura

```
├── src/main/java/com/api/e_commerce
│   ├── auth/            # Autenticação e registro
│   ├── address/          # Endereços + integração ViaCEP
│   ├── cart/             # Carrinho de compras
│   ├── order/             # Pedidos, validações e eventos
│   ├── payment/           # Pagamentos (Stripe / AbacatePay), retries, webhooks
│   ├── product/           # Catálogo e categorias
│   ├── user/              # Usuários
│   ├── role/              # Papéis (RBAC)
│   └── config/             # Segurança, exceções, Swagger, RestClient
│
├── gateway/               # API Gateway em Go (rate limiting + proxy)
│   ├── limiter/
│   ├── proxy/
│   └── redis/
│
├── docker-compose.yml
├── Dockerfile
└── pom.xml
```

A aplicação segue uma **arquitetura em camadas** (Controller → Service → Repository) com uso de **Strategy Pattern** para gateways de pagamento, **Factory Pattern** para criação de transações, e **eventos de domínio** (Spring `ApplicationEventPublisher`) para desacoplar side-effects (notificações, e-mails, etc.) do fluxo principal.

---

## ✅ Pré-requisitos

Antes de começar, você precisa ter instalado:

- [Java 21+](https://adoptium.net/)
- [Maven 3.9+](https://maven.apache.org/) (ou use o `./mvnw` incluso)
- [PostgreSQL 15+](https://www.postgresql.org/)
- [Docker e Docker Compose](https://www.docker.com/)
- [Go 1.26+](https://go.dev/) (apenas se for rodar o Gateway)
- Conta [Stripe](https://stripe.com/) (chave de teste) para pagamentos

---

## 🚀 Instalação e Como Executar

### 1. Clone o repositório

```bash
git clone https://github.com/seu-usuario/e-commerce-api.git
cd e-commerce-api
```

### 2. Configure as variáveis de ambiente

Crie um arquivo `.env` (ou exporte as variáveis no seu shell) com base na seção [Variáveis de Ambiente](#-variáveis-de-ambiente).

### 3. Suba a infraestrutura auxiliar (Redis + Stripe CLI)

```bash
docker-compose up -d
```

Isso irá subir:
- `redis` — usado pelo Gateway para rate limiting
- `stripe-cli` — encaminha webhooks do Stripe para `localhost:8080/api/v1/payments/webhook/stripe`

### 4. Configure o banco de dados

Crie o banco no PostgreSQL:

```sql
CREATE DATABASE "e-commerce";
```

As migrations do **Flyway** serão aplicadas automaticamente na subida da aplicação.

### 5. Execute a aplicação Spring Boot

```bash
# usando o wrapper do maven
./mvnw spring-boot:run

# ou, se preferir compilar antes
./mvnw clean package -DskipTests
java -jar target/e-commerce-0.0.1-SNAPSHOT.jar
```

A API estará disponível em `http://localhost:8080`.

### 6. (Opcional) Execute o API Gateway em Go

```bash
cd gateway
go run main.go
```

O Gateway ficará disponível em `http://localhost:8081` e fará proxy das requisições para a API Spring Boot com rate limiting (10 requisições/minuto por IP, configurável em `main.go`).

### 7. (Opcional) Build e execução via Docker

```bash
./mvnw clean package -DskipTests
docker build -t e-commerce-api .
docker run -p 8080:8080 --env-file .env e-commerce-api
```

---

## 🔑 Variáveis de Ambiente

| Variável                       | Descrição                                      | Exemplo                          |
|---------------------------------|-------------------------------------------------|-----------------------------------|
| `DB_USERNAME`                   | Usuário do PostgreSQL                            | `postgres`                        |
| `DB_PASSWORD`                   | Senha do PostgreSQL                              | `postgres`                        |
| `JWT_SECRET`                    | Segredo usado para assinar os tokens JWT (min. 32 caracteres) | `sua-chave-super-secreta-...`     |
| `STRIPE_KEY`                    | Chave secreta da Stripe                          | `sk_test_...`                     |
| `WEBHOOK_SECRET`                | Segredo do webhook Stripe                        | `whsec_...`                       |
| `ABACATE_PAY_KEY`               | Chave secreta da AbacatePay                      | `abc_...`                         |
| `ABACATE_PAY_WEBHOOK_SECRET`    | Segredo do webhook AbacatePay                    | `whsec_...`                       |
| `PAYMENT_CALLBACK_BASE_URL`     | URL base do frontend para redirecionamento de pagamento (opcional) | `http://localhost:3000`           |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | Configuração do Redis para o Gateway | `redis` / `6379` / `123`          |

> 💡 Dica: para desenvolvimento local, use `docker-compose.yml` (já configurado com Redis) e o [Stripe CLI](https://stripe.com/docs/stripe-cli) para testar webhooks sem expor sua máquina à internet.

---

## 💡 Como Usar

### Registrar um usuário

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Doe",
    "email": "jane@example.com",
    "password": "senha12345"
  }'
```

### Login (obter access token + refresh token)

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jane@example.com",
    "password": "senha12345"
  }'
```

**Resposta:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "3f1c4b2a-....-....",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

### Adicionar um item ao carrinho

```bash
curl -X POST http://localhost:8080/api/v1/cart/items \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "3f1c4b2a-1234-4a2b-9c3d-abcdef123456",
    "quantity": 2
  }'
```

### Criar um pedido a partir do carrinho

```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: 3f2c1a9b-..." \
  -d '{
    "cartId": "b2f1e3a4-....",
    "shippingAddressId": "c1a2b3d4-....",
    "billingAddressId": "c1a2b3d4-...."
  }'
```

### Iniciar o checkout de pagamento (Stripe ou AbacatePay)

```bash
curl -X POST http://localhost:8080/api/v1/payments/{orderId}/checkout-session \
  -H "Authorization: Bearer <accessToken>" \
  -H "Content-Type: application/json" \
  -d '{ "provider": "STRIPE" }'
```

**Resposta:**
```json
{
  "checkoutUrl": "https://checkout.stripe.com/c/pay/cs_test_...",
  "sessionId": "cs_test_..."
}
```

### Rastrear um pedido

```bash
curl -X GET http://localhost:8080/api/v1/orders/{orderId}/track \
  -H "Authorization: Bearer <accessToken>"
```

---

## 🧪 Testes

O projeto utiliza **JUnit 5**, **Spring Boot Test** e banco **H2** em memória para os testes de integração (perfil `test`).

```bash
# Rodar todos os testes
./mvnw test

# Rodar uma classe específica
./mvnw test -Dtest=ECommerceApplicationTests

# Rodar com relatório de cobertura (se plugin configurado)
./mvnw verify
```

Configuração de teste: `src/test/resources/application-test.properties` (usa H2 em modo PostgreSQL, sem Flyway).

---

## 📚 Documentação da API

Com a aplicação em execução, a documentação interativa (Swagger UI) fica disponível em:

```
http://localhost:8080/swagger-ui.html
```

E o contrato OpenAPI em formato JSON:

```
http://localhost:8080/v3/api-docs
```

---

## 🤝 Contribuindo

Contribuições são bem-vindas! Para contribuir:

1. Faça um **fork** do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/minha-feature`)
3. Faça commit das suas alterações (`git commit -m 'feat: adiciona minha feature'`)
4. Faça push para a branch (`git push origin feature/minha-feature`)
5. Abra um **Pull Request**

### Convenções

- Siga o padrão de camadas já existente (`Controller → Service → Repository`)
- Utilize **DTOs (records)** para request/response, nunca exponha entidades diretamente
- Adicione testes para novas regras de negócio
- Prefira exceções customizadas (`ValidationException`, etc.) tratadas pelo `GlobalExceptionHandler`
- Use `@Transactional` de forma consistente nos métodos de serviço que alteram estado

<p align="center">Feito com ☕ e Spring Boot</p>
