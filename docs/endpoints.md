# API Endpoints

## Base path: */api/v1*


### **Auth:**

| Método | Endpoint              | Descrição            | Auth   | Roles  | Status Code         |
| :----- |:----------------------| :------------------- | :----- | :----- | :------------------ |
| `POST` | `/auth/register`      | Cadastro de usuários | Public | -      | `201`, `400`, `409` |
| `POST` | `/auth/login`         | Login de usuários    | Public | -      | `200`, `401`        |
| `POST` | `/auth/refresh-token` | Renovar token        | Public | -      | `200`, `401`        |
| `GET`  | `/auth/me`            | Usuário autenticado  | Bearer | `USER` | `200`, `401`        |



### **Users:**

| Método | Endpoint      | Descrição               | Auth   | Roles   | Status Code         |
| :----- | :------------ | :---------------------- | :----- | :------ | :------------------ |
| `GET`  | `/users/me`   | Dados do usuário logado | Bearer | `USER`  | `200`, `401`        |
| `PUT`  | `/users/me`   | Atualiza usuário        | Bearer | `USER`  | `200`, `400`, `401` |
| `GET`  | `/users`      | Lista usuários          | Bearer | `ADMIN` | `200`, `403`        |
| `GET`  | `/users/{id}` | Busca usuário por ID    | Bearer | `ADMIN` | `200`, `404`, `403` |



### **Categories:**

| Método   | Endpoint           | Descrição          | Auth   | Roles   | Status Code                |
| :------- | :----------------- | :----------------- | :----- | :------ | :------------------------- |
| `GET`    | `/categories`      | Lista categorias   | Public | -       | `200`                      |
| `GET`    | `/categories/{id}` | Busca categoria    | Public | -       | `200`, `404`               |
| `POST`   | `/categories`      | Cria categoria     | Bearer | `ADMIN` | `201`, `400`, `403`        |
| `PUT`    | `/categories/{id}` | Atualiza categoria | Bearer | `ADMIN` | `200`, `400`, `404`, `403` |
| `DELETE` | `/categories/{id}` | Remove categoria   | Bearer | `ADMIN` | `204`, `404`, `403`        |

### **Products:**

| Método   | Endpoint         | Descrição        | Auth   | Roles   | Status Code                |
| :------- | :--------------- | :--------------- | :----- | :------ | :------------------------- |
| `GET`    | `/products`      | Lista produtos   | Public | -       | `200`                      |
| `GET`    | `/products/{id}` | Busca produto    | Public | -       | `200`, `404`               |
| `POST`   | `/products`      | Cria produto     | Bearer | `ADMIN` | `201`, `400`, `403`        |
| `PUT`    | `/products/{id}` | Atualiza produto | Bearer | `ADMIN` | `200`, `400`, `404`, `403` |
| `DELETE` | `/products/{id}` | Remove produto   | Bearer | `ADMIN` | `204`, `404`, `403`        |



### **Cart:**
| Método   | Endpoint               | Descrição              | Auth   | Roles  | Status Code         |
| :------- | :--------------------- | :--------------------- | :----- | :----- | :------------------ |
| `GET`    | `/cart`                | Retorna carrinho       | Bearer | `USER` | `200`, `401`        |
| `POST`   | `/cart/items`          | Adiciona item          | Bearer | `USER` | `200`, `400`, `404` |
| `PUT`    | `/cart/items/{itemId}` | Atualiza item          | Bearer | `USER` | `200`, `400`, `404` |
| `DELETE` | `/cart/items/{itemId}` | Remove item específico | Bearer | `USER` | `204`, `404`        |
| `DELETE` | `/cart/items`          | Limpa carrinho         | Bearer | `USER` | `204`               |


### **Orders:**
| Método  | Endpoint              | Descrição       | Auth   | Roles   | Status Code                |
| :------ | :-------------------- | :-------------- | :----- | :------ | :------------------------- |
| `POST`  | `/orders/checkout`    | Finaliza compra | Bearer | `USER`  | `201`, `400`               |
| `GET`   | `/orders`             | Lista pedidos   | Bearer | `USER`  | `200`, `401`               |
| `GET`   | `/orders/{id}`        | Busca pedido    | Bearer | `USER`  | `200`, `404`, `401`        |
| `PATCH` | `/orders/{id}/cancel` | Cancela pedido  | Bearer | `USER`  | `200`, `400`, `404`        |
| `PATCH` | `/orders/{id}/status` | Atualiza status | Bearer | `ADMIN` | `200`, `400`, `404`, `403` |



### **Payments:**
| Método | Endpoint                     | Descrição                  | Auth   | Roles  | Status Code         |
| :----- | :--------------------------- | :------------------------- | :----- | :----- | :------------------ |
| `POST` | `/payments/intent`           | Cria intenção de pagamento | Bearer | `USER` | `200`, `400`        |
| `POST` | `/payments/confirm`          | Confirma pagamento         | Bearer | `USER` | `200`, `400`, `402` |
| `GET`  | `/payments/{id}`             | Busca pagamento            | Bearer | `USER` | `200`, `404`        |
| `GET`  | `/orders/{orderId}/payments` | Pagamentos do pedido       | Bearer | `USER` | `200`, `404`        |
| `POST` | `/payments/webhook/stripe`   | Webhook Stripe             | Public | -      | `200`               |



### **Addresses:**
| Método   | Endpoint          | Descrição         | Auth   | Roles  | Status Code         |
| :------- | :---------------- | :---------------- | :----- | :----- | :------------------ |
| `GET`    | `/addresses`      | Lista endereços   | Bearer | `USER` | `200`, `401`        |
| `POST`   | `/addresses`      | Cria endereço     | Bearer | `USER` | `201`, `400`        |
| `PUT`    | `/addresses/{id}` | Atualiza endereço | Bearer | `USER` | `200`, `400`, `404` |
| `DELETE` | `/addresses/{id}` | Remove endereço   | Bearer | `USER` | `204`, `404`        |



