# 🛒 BasketEcommerce

Microsserviço de **carrinho de compras (basket)** desenvolvido em **Java + Spring Boot**, que consome os dados de produtos de uma **API externa** ([Platzi Fake Store API](https://api.escuelajs.co/api/v1)) através de um cliente Feign, persiste os carrinhos em **MongoDB** e utiliza **Redis** como camada de cache para reduzir chamadas repetidas à API de produtos.

---

## 📖 Sobre o projeto

A ideia central do serviço é simples: o cliente informa **quais produtos** (por `id`) e **em qual quantidade** deseja adicionar ao carrinho. O microsserviço então:

1. Busca os dados reais de cada produto (nome/título e preço) na API externa;
2. Monta o carrinho (`Basket`) com esses produtos e as quantidades informadas;
3. Calcula o **preço total** automaticamente (preço unitário × quantidade, somado entre todos os itens);
4. Persiste o carrinho no MongoDB;
5. Permite depois atualizar, consultar, "pagar" (fechar) ou remover esse carrinho.

Dessa forma, o serviço não depende de manter uma cópia própria do catálogo de produtos — ele sempre busca as informações "na fonte", cacheando o resultado para não sobrecarregar a API externa a cada requisição.

---

## 🏗️ Arquitetura e fluxo

```
Cliente
  │
  ▼
BasketController / ProductController   (camada REST)
  │
  ▼
BasketService  ───────────────►  ProductService  ───────────────►  ProductClient (Feign)
  │  (regra de negócio do          │ (cache com Redis)                     │
  │   carrinho)                    │                                       ▼
  ▼                                ▼                          Platzi Fake Store API
BasketRepository (MongoDB)   Cache Redis (products / product)   (https://api.escuelajs.co/api/v1)
```

- **`ProductClient`**: interface Feign que faz as chamadas HTTP para a API externa de produtos.
- **`ProductService`**: camada intermediária que chama o `ProductClient` e aplica cache (`@Cacheable`) para evitar requisições repetidas.
- **`BasketService`**: contém a regra de negócio do carrinho — cria, atualiza, calcula o preço total, processa pagamento e exclui carrinhos.
- **`BasketRepository`**: repositório Spring Data MongoDB para persistir os carrinhos.
- **`ControllerAdvice` / `CustomErrorDecoder`**: tratamento centralizado de erros, tanto das requisições internas quanto das falhas vindas da API externa.

---

## 🚀 Tecnologias utilizadas

| Tecnologia | Finalidade |
|---|---|
| **Java 21** | Linguagem principal |
| **Spring Boot 4.0.6** | Framework base da aplicação |
| **Spring Web (MVC)** | Exposição dos endpoints REST |
| **Spring Cloud OpenFeign** | Cliente HTTP declarativo para consumir a API externa de produtos |
| **Spring Data MongoDB** | Persistência dos carrinhos (`Basket`) |
| **Spring Data Redis** | Cache dos dados de produtos vindos da API externa |
| **Spring Cache** | Abstração de cache (`@Cacheable`) usada sobre o Redis |
| **Spring Validation** | Validação de dados de entrada |
| **Lombok** | Redução de boilerplate (getters/setters, builders, construtores) |
| **Docker Compose** | Sobe MongoDB e Redis localmente para desenvolvimento |
| **Maven** | Gerenciador de build e dependências |
| **JUnit / Spring Boot Test** | Testes automatizados |

---

## 📂 Estrutura do projeto

```
BasketEcommerce
├── docker-compose.yml
├── pom.xml
└── src
    ├── main
    │   ├── java/com/java/victor/BasketEcommerce
    │   │   ├── BasketEcommerceApplication.java     # Classe principal (@EnableFeignClients, @EnableCaching)
    │   │   ├── client
    │   │   │   ├── ProductClient.java               # Interface Feign -> API externa de produtos
    │   │   │   ├── request/
    │   │   │   │   ├── BasketRequest.java           # Payload de criação/atualização de carrinho
    │   │   │   │   ├── ProductRequest.java          # Item do carrinho (id + quantidade)
    │   │   │   │   └── PagamentoRequest.java        # Payload de pagamento (método de pagamento)
    │   │   │   └── response/
    │   │   │       └── PlatzProductResponse.java    # DTO da resposta da API externa (id, title, price)
    │   │   ├── controller
    │   │   │   ├── BasketController.java            # Endpoints /basket
    │   │   │   └── ProductController.java           # Endpoints /product
    │   │   ├── exception
    │   │   │   ├── BussinesExeption.java             # Exceção de regra de negócio (400)
    │   │   │   ├── DataNotFoundExeption.java         # Exceção de recurso não encontrado (404)
    │   │   │   ├── ControllerAdvice.java             # Handler global de exceções
    │   │   │   └── CustomErrorDecoder.java           # Decodificador de erros das chamadas Feign
    │   │   ├── model
    │   │   │   ├── Basket.java                       # Documento MongoDB do carrinho
    │   │   │   ├── Product.java                      # Item persistido dentro do carrinho
    │   │   │   ├── StatusBasket.java                 # Enum: ABERTO / VENDIDO
    │   │   │   └── MetodoDePagamento.java            # Enum: PIX / DEBITO / CREDITO
    │   │   ├── repository
    │   │   │   └── BasketRepository.java             # Spring Data MongoDB
    │   │   └── service
    │   │       ├── BasketService.java                # Regras de negócio do carrinho
    │   │       └── ProductService.java                # Busca de produtos + cache Redis
    │   └── resources
    │       └── application.yml
    └── test
        └── java/.../BasketEcommerceApplicationTests.java
```

---

## 🌐 Integração com a API externa

O microsserviço consome a **[Platzi Fake Store API](https://fakeapi.platzi.com/)**, uma API pública gratuita de e-commerce fictício, muito usada para estudos e testes. A URL base é configurada em `application.yml`:

```yaml
basket:
  client:
    url: https://api.escuelajs.co/api/v1
```

A comunicação é feita através de um **Feign Client** (`ProductClient`), que expõe dois métodos:

```java
@FeignClient(name = "PlatziStoreClient", url = "${basket.client.url}", configuration = CustomErrorDecoder)
public interface ProductClient {

    @GetMapping("/products")
    List<PlatzProductResponse> getAllProducts();

    @GetMapping("/products/{id}")
    Optional<PlatzProductResponse> getById(@PathVariable Long id);
}
```

Esses dados retornam apenas o essencial para o carrinho: `id`, `title` (nome) e `price` (preço) — mapeados no DTO `PlatzProductResponse`.

### Cache dos produtos (Redis)

Para evitar chamar a API externa a cada requisição, o `ProductService` aplica `@Cacheable`:

```java
@Cacheable(value = "products")
public List<PlatzProductResponse> getAllProducts() { ... }

@Cacheable(value = "product", key = "#id")
public PlatzProductResponse getProductId(Long id) { ... }
```

O tempo de vida (TTL) do cache é definido em `application.yml`:

```yaml
spring:
  cache:
    redis:
      time-to-live: 60000   # 60 segundos, em milissegundos
```

### Tratamento de erros da API externa

O `CustomErrorDecoder` intercepta respostas de erro do Feign e as converte em exceções da aplicação (por exemplo, status `400` vira `DataNotFoundExeption`, informando que o produto não foi encontrado).

---

## 📦 Modelo de dados

### Basket (documento MongoDB)

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | `String` | Identificador do carrinho (gerado pelo Mongo) |
| `client` | `Long` | Identificador do cliente dono do carrinho |
| `precoTotal` | `BigDecimal` | Soma do preço de todos os produtos (calculado automaticamente) |
| `produtos` | `List<Product>` | Itens do carrinho |
| `status` | `StatusBasket` | `ABERTO` ou `VENDIDO` |
| `metodoDePagamento` | `MetodoDePagamento` | `PIX`, `DEBITO` ou `CREDITO` (definido no pagamento) |

O cálculo do preço total é feito com **Java Streams**, usando `reduce`:

```java
public void calcularPrecoTotal() {
    this.precoTotal = produtos.stream()
        .map(product -> product.getPrice().multiply(BigDecimal.valueOf(product.getQuantidade())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
}
```

### Product (item dentro do carrinho)

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | `Long` | Id do produto (vindo da API externa) |
| `nome` | `String` | Nome/título do produto |
| `price` | `BigDecimal` | Preço unitário |
| `quantidade` | `Integer` | Quantidade escolhida pelo cliente |

---

## 🔌 Endpoints da API

### Produtos — `/product`

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/product` | Lista todos os produtos vindos da API externa (com cache) |
| `GET` | `/product/{id}` | Busca um produto específico pelo id (com cache) |

### Carrinho — `/basket`

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/basket` | Cria um novo carrinho para um cliente |
| `GET` | `/basket/{id}` | Busca um carrinho pelo id |
| `PUT` | `/basket/{id}` | Atualiza os produtos de um carrinho existente |
| `PUT` | `/basket/{id}/payment` | Realiza o "pagamento" do carrinho, mudando seu status para `VENDIDO` |
| `DELETE` | `/basket/{id}` | Remove um carrinho |

#### Regras de negócio importantes

- Um cliente **não pode ter dois carrinhos `ABERTO` ao mesmo tempo** — ao tentar criar um novo carrinho enquanto já existe um aberto, a API retorna erro.
- Ao criar ou atualizar um carrinho, o serviço busca cada produto na API externa (via cache) para garantir que os dados (nome e preço) estejam corretos e atualizados.
- O `precoTotal` **nunca é enviado pelo cliente** — ele é sempre recalculado no backend, evitando manipulação indevida de valores.

#### Exemplo — Criar carrinho

**Requisição:**
```http
POST /basket
Content-Type: application/json

{
  "client": 1,
  "produtos": [
    { "id": 1, "quantidade": 2 },
    { "id": 5, "quantidade": 1 }
  ]
}
```

**Resposta (`201 Created`):**
```json
{
  "id": "665f1c2e8b1e2a3f4c5d6e7f",
  "client": 1,
  "precoTotal": 359.98,
  "produtos": [
    { "id": 1, "nome": "Classic Red T-Shirt", "price": 100.00, "quantidade": 2 },
    { "id": 5, "nome": "Denim Jacket", "price": 159.98, "quantidade": 1 }
  ],
  "status": "ABERTO",
  "metodoDePagamento": null
}
```

#### Exemplo — Pagamento do carrinho

**Requisição:**
```http
PUT /basket/665f1c2e8b1e2a3f4c5d6e7f/payment
Content-Type: application/json

{
  "metodoDePagamento": "PIX"
}
```

**Resposta (`200 OK`):**
```json
{
  "id": "665f1c2e8b1e2a3f4c5d6e7f",
  "client": 1,
  "precoTotal": 359.98,
  "produtos": [ ... ],
  "status": "VENDIDO",
  "metodoDePagamento": "PIX"
}
```

---

## ⚠️ Tratamento de erros

| Exceção | Status HTTP | Quando ocorre |
|---|---|---|
| `DataNotFoundExeption` | `404 Not Found` | Carrinho ou produto não encontrado |
| `BussinesExeption` | `400 Bad Request` | Violação de regra de negócio |
| Erro `400` vindo da API externa | Convertido em `DataNotFoundExeption` | Tratado pelo `CustomErrorDecoder` no cliente Feign |

Todas as exceções são capturadas de forma centralizada pela classe `ControllerAdvice`, retornando o status HTTP apropriado junto com a mensagem de erro.

---

## ▶️ Como executar o projeto

### Pré-requisitos

- **Java 21**
- **Maven** (ou usar o `mvnw` incluso no projeto)
- **Docker** e **Docker Compose** (para subir MongoDB e Redis)

### Passo a passo

1. Clone o repositório:
   ```bash
   git clone https://github.com/victor-macalin/BasketEcommerce.git
   cd BasketEcommerce
   ```

2. Suba as dependências de infraestrutura (MongoDB e Redis):
   ```bash
   docker-compose up -d
   ```

3. Rode a aplicação:
   ```bash
   ./mvnw spring-boot:run
   ```

   Ou, no Windows:
   ```bash
   mvnw.cmd spring-boot:run
   ```

4. A aplicação sobe por padrão na porta `8080` (padrão do Spring Boot). Os endpoints ficam disponíveis em:
   - `http://localhost:8080/product`
   - `http://localhost:8080/basket`

### Infraestrutura (docker-compose.yml)

| Serviço | Imagem | Porta |
|---|---|---|
| MongoDB | `mongo:4` | `27017` |
| Redis | `redis` | `6379` |

---

## ⚙️ Configurações (`application.yml`)

```yaml
spring:
  application:
    name: basket-service

  data:
    mongodb:
      host: localhost
      port: 27017
      database: basket-service
    redis:
      host: localhost
      port: 6379
      password: sa

  cache:
    redis:
      time-to-live: 60000

basket:
  client:
    url: https://api.escuelajs.co/api/v1
```

> ⚠️ **Atenção:** a senha do Redis (`sa`) está fixa no `application.yml`. Em um ambiente de produção real, recomenda-se mover esse tipo de credencial para variáveis de ambiente ou um cofre de segredos (Vault, AWS Secrets Manager etc.), em vez de deixá-la versionada no repositório.

---

## 🧪 Testes

O projeto conta com testes usando **JUnit 5** e **Spring Boot Test**, incluindo dependências específicas de teste para cache, Redis, MongoDB e validação. Execute com:

```bash
./mvnw test
```

---

## 💡 Possíveis melhorias futuras

- Adicionar autenticação/autorização (ex: Spring Security + JWT) nos endpoints.
- Externalizar credenciais sensíveis via variáveis de ambiente.
- Adicionar testes unitários e de integração cobrindo `BasketService` e `ProductService`.
- Implementar paginação no endpoint `GET /product`.
- Adicionar documentação interativa da API (Swagger/OpenAPI).
- Adicionar circuit breaker (ex: Resilience4j) nas chamadas Feign para a API externa, aumentando a resiliência do serviço.

---

## 👤 Autor

Desenvolvido por **Victor** como projeto de estudo em Java, Spring Boot, integração com APIs externas via Feign, cache com Redis e persistência com MongoDB.
