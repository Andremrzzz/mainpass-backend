# MainPass — API de controle de acesso por QR Code

Backend do TCC para controle de acesso por convites digitais/QR Code. Implementado com Java 21, Spring Boot 4, Spring Security, JWT, Spring Data JPA, MySQL 8.4, Flyway e ZXing.

## Requisitos

- Java 21 e Maven 3.9+ para executar localmente; ou
- Docker Desktop com Docker Compose para executar API e banco em conjunto.

## Opção A — executar tudo com Docker (mais simples)

1. Copie `.env.example` para `.env` e altere os segredos antes de qualquer uso fora do ambiente local.
2. Na pasta do projeto, execute:

   ```bash
   docker compose up --build -d
   docker compose ps
   ```

3. API: http://localhost:8080
4. Swagger: http://localhost:8080/swagger-ui/index.html
5. Saúde: http://localhost:8080/actuator/health

Para acompanhar a inicialização: `docker compose logs -f api`.
Para parar: `docker compose down`. Para apagar também o banco local: `docker compose down -v` (ATENÇÃO: apaga os dados).

## Opção B — executar pelo terminal/IDE

1. Inicie apenas o MySQL: `docker compose up -d db`.
2. Garanta Java 21 e Maven 3.9+ instalados.
3. Execute `mvn spring-boot:run` na pasta que contém `pom.xml`.
4. Para gerar o JAR: `mvn clean package`; execute com `java -jar target/acesso-qrcode-0.0.1-SNAPSHOT.jar`.

O perfil `dev` é o padrão e carrega os dados de demonstração. Se estiver usando IDE, importe o projeto como **Maven Project**, não como Gradle.

## Contas de demonstração (somente desenvolvimento)

| Perfil | E-mail | Senha |
|---|---|---|
| ADMIN | admin@acesso.com | admin123 |
| OPERADOR | operador@acesso.com | operador123 |
| USUARIO | usuario@acesso.com | usuario123 |

Ambientes de demonstração: `Laboratorio 1` (usuário de teste tem permissão) e `Auditorio` (sem permissão).

**Troque/remova essas contas antes de qualquer implantação real.** Não use credenciais de demonstração em produção.

## Fluxo principal

1. O usuário autentica em `POST /api/auth/login` e recebe um JWT.
2. Com o token, consulta ambientes e gera QR Code em `POST /api/qrcodes`.
3. A resposta da geração contém `token` e `imagemBase64` (PNG em Base64). O token puro não é armazenado no banco.
4. O operador autentica e envia o token lido para `POST /api/acessos/validar`.
5. A API verifica existência, estado, validade, usuário, ambiente e permissão. Um QR Code válido é consumido uma única vez.
6. Todas as tentativas são registradas, inclusive códigos inexistentes, recusados e autorizados.

## Endpoints

| Método | Rota | Acesso |
|---|---|---|
| POST | `/api/auth/login` | Público |
| GET | `/api/auth/me` | Autenticado |
| GET/POST/PUT/PATCH | `/api/usuarios` | ADMIN |
| GET/POST/PUT/PATCH | `/api/ambientes` | Leitura autenticada; escrita ADMIN |
| GET/POST/DELETE | `/api/permissoes` | ADMIN |
| POST | `/api/qrcodes` | Autenticado com permissão vigente no ambiente |
| GET | `/api/qrcodes/meus` | Autenticado |
| DELETE | `/api/qrcodes/{id}` | Dono do QR ou ADMIN |
| POST | `/api/acessos/validar` | OPERADOR ou ADMIN |
| GET | `/api/acessos` | OPERADOR ou ADMIN |

Use o botão **Authorize** do Swagger e informe `Bearer <JWT>` (ou apenas o token, conforme a interface do Swagger).

### Exemplos de requisição

Login:

```json
{"email":"usuario@acesso.com","senha":"usuario123"}
```

Gerar QR Code:

```json
{"ambienteId":1}
```

Validar na portaria:

```json
{"token":"TOKEN_LIDO_DO_QR","ambienteId":1}
```

A resposta da validação informa `AUTORIZADO`, `RECUSADO` ou `CODIGO_INVALIDO`, além do motivo quando aplicável.

## Configuração

As variáveis podem ser definidas no `.env` (Docker) ou no ambiente do sistema:

- `DB_URL`, `DB_USER`, `DB_PASSWORD`
- `JWT_SECRET` (mínimo de 32 bytes), `JWT_EXPIRATION_MINUTES`
- `QRCODE_EXPIRATION_MINUTES`
- `CORS_ORIGINS` (origem do frontend, por padrão `http://localhost:5173`)
- `SPRING_PROFILES_ACTIVE` (`dev` por padrão)

## Estrutura

- `auth`: login, emissão e validação JWT
- `usuario`: usuários, perfis e status
- `ambiente`: ambientes controlados
- `permissao`: concessão e vigência de permissões
- `qrcode`: geração, listagem e revogação
- `registro`: validação de acesso e histórico
- `common`: tratamento padronizado de erros e utilitários
- `db/migration`: schema versionado pelo Flyway
- `db/dev`: dados de demonstração exclusivos do perfil dev

## Limitações e próximos passos

Este ZIP contém a API/backend. A interface web ou aplicativo Android, leitura da câmera, telas e integração visual não estão incluídos neste pacote. A leitura da câmera deve extrair o token do QR e enviá-lo ao endpoint de validação. Antes de produção, adicionar HTTPS, gestão segura de segredos, política de backup, rate limiting, auditoria administrativa e testes de integração com banco real.
