# Modulo Convidado — o que mudou e o que ainda falta

## Arquivos seus que vieram completos neste zip (substitua os atuais)
- `usuario/Perfil.java` — adiciona `CONVIDADO` (o enum real e ADMIN, OPERADOR, USUARIO: **morador = USUARIO**).
- `auth/JwtService.java` — adiciona a claim `nome`.
- `config/SecurityConfig.java` — regras do convidado/convites e `anyRequest()` agora exige ADMIN/OPERADOR/USUARIO
  (CONVIDADO so enxerga `/api/convidado/**` e o chat). Se alguma rota existente precisa ficar aberta a todo
  usuario logado, liste-a antes do `anyRequest()`.

## Duas linhas nos repositories (nao recebi esses arquivos)
```java
// UsuarioRepository
boolean existsByCpf(String cpf);

// RegistroAcessoRepository
Page<RegistroAcesso> findByUsuarioId(Long usuarioId, Pageable pageable);
```

## application.yml
```yaml
mainpass:
  convite:
    token-secret: ${CONVITE_TOKEN_SECRET}   # >= 32 bytes aleatorios; a aplicacao nao sobe sem ele
```
(usa `@Value`; se preferir, mova para o seu `AppProperties`). Trocar o segredo invalida os QR Codes emitidos.

## Migration
`V4__adicionar_funcionalidades_convidado.sql` referencia `usuario` e `ambiente` (nomes no singular, como a sua
entidade `Usuario`). Confira no fim do arquivo: se `usuario.perfil` foi criado como ENUM/CHECK, rode o ALTER indicado.

## Ligar o QR de convite ao `POST /api/acessos/validar` (ultimo passo, 5 linhas no seu codigo)
`ConviteAcessoService` (novo) faz tudo do lado dos convites: valida o token `MPC.`, consome o uso,
atualiza o status do convite e grava o `RegistroAcesso` (usuario = convidado, operador, ambiente, resultado,
motivo) na mesma transacao. No service/controller de validacao que ja existe, ANTES da busca atual do QR:
```java
if (conviteAcesso.ehTokenDeConvite(req.token())) {
    var r = conviteAcesso.validarERegistrar(req.token(), req.ambienteId(), operador);   // operador = usuario logado
    // monte a MESMA resposta que voce devolve hoje para um QR normal:
    //   permitido = r.permitido(); motivo = r.registro().getMotivo();
    //   pessoa = r.convidado() (pode ser null se o token for invalido); registro = r.registro()
    return suaResposta;
}
```
Os enums `ResultadoAcesso`/`MotivoRecusa` nao foram enviados, entao `ConviteAcessoService` acha as constantes pelo
NOME (listas de palavras no topo da classe). Na primeira subida, leia o log `Convites -> ...`: ele mostra qual
constante foi escolhida para cada situacao e avisa (WARN) as que ficaram sem correspondencia (gravadas sem motivo).
Para a resposta continuar compativel com o front, ela deve ter os campos que o `portariaHttp.js` le: `permitido`
(ou `resultado`), `motivo`/`mensagem` e, de preferencia, o registro (`usuario.nome`, `ambiente`, `dataHora`).

## Ainda assumido
- `Ambiente#getId()` / `getNome()` (entidade nao recebida).
- `auth.getName()` = e-mail do usuario: depende do `JwtFilter` (se ele colocar o `Usuario` como principal em vez do
  e-mail, `getName()` devolve `toString()` e os controllers de chamados e convites quebram). Envie o `JwtFilter` para eu confirmar.
- `ResultadoAcesso` / `MotivoRecusa`: ver o mapeamento por nome em `ConviteAcessoService` (e `ConvidadoService`, que trata AUTORIZ*/PERMIT* como AUTORIZADO).

## Front-end
Morador e `USUARIO`: defina `VITE_PERFIL_MORADOR=USUARIO` no `.env` (o default do front passou a ser esse).

## Endpoints
| Quem | Metodo e rota |
|---|---|
| Morador (USUARIO) | `POST /api/convites` · `GET /api/convites?status=&page=&size=` · `GET /api/convites/{id}` · `PATCH /api/convites/{id}/cancelar` · `PATCH /api/convites/{id}/renovar` (extra) |
| Convidado | `GET /api/convidado/perfil` · `GET /api/convidado/qrcodes` · `GET /api/convidado/entradas?page=&size=` · `POST /api/convidado/convites/{id}/solicitar-renovacao` |
| Ambos | `GET /api/convites/{id}/mensagens?apos=` · `POST /api/convites/{id}/mensagens` · `PUT /api/convites/{id}/mensagens/lidas` (extra) |

---

## Portaria: moradores e convidados (`/api/portaria/**`)

Novos arquivos em `portaria/` (repository, service, controller, 3 DTOs). Alterados: `ConviteRepository` (2 queries), `SecurityConfig` (`/api/portaria/**` → OPERADOR/ADMIN).

| Endpoint | Descrição |
|---|---|
| `GET /api/portaria/moradores?q=&page=&size=` | Lista paginada (size máx. 100, ordem por nome). `q` busca nome, e-mail, bloco, unidade ou CPF (com/sem máscara). |
| `GET /api/portaria/moradores/{id}` | Morador + convidados. |
| `GET /api/portaria/moradores/{id}/convidados` | Somente convidados/convites. |

Pontos de atenção:
- Só aparecem usuários com `Perfil.USUARIO`; outro id → 404.
- DTO sem `senhaHash` e sem token do convite. O CPF do convidado vai completo (a tela mascara).
- `convitesVigentes` vem de uma única query agrupada (sem N+1); convites carregados com `JOIN FETCH` (sem LazyInitializationException).
- Assume `Ambiente#getNome()`. Ajuste se o getter tiver outro nome.
- Java não foi compilado neste ambiente (Maven Central bloqueado): rode `./mvnw compile` e me envie o que quebrar.

---

## Aprovação do convite pela portaria

Fluxo: morador cria → **PENDENTE_APROVACAO** (sem QR) → porteiro **aprova** (ATIVO, QR liberado) ou **recusa** (RECUSADO, sem QR).
Morador ainda pode cancelar um convite pendente (vira CANCELADO).

**Migration `V6__aprovacao_convite_portaria.sql`** (rode antes de subir; `ddl-auto=validate`):
- `convites.status` passa de `VARCHAR(15)` para `VARCHAR(20)` (PENDENTE_APROVACAO tem 18 caracteres) e o default vira `PENDENTE_APROVACAO`.
- Novas colunas: `decidido_por_id` (FK `usuario`), `decidido_em`, `motivo_recusa`.
- Convites já existentes **não mudam** (continuam ATIVO etc.).
- Se a V4 já tiver sido aplicada em outro ambiente com CHECK em `status`, remova/ajuste o CHECK (na V4 original não há).

**Endpoints novos** (somente OPERADOR/ADMIN, já cobertos por `/api/portaria/**` no `SecurityConfig` + `@PreAuthorize`):
| Endpoint | Efeito |
|---|---|
| `GET /api/portaria/convites/pendentes` | fila (mais antigas primeiro, só dentro do prazo) |
| `POST /api/portaria/convites/{id}/aprovar` | PENDENTE → ATIVO |
| `POST /api/portaria/convites/{id}/recusar` | PENDENTE → RECUSADO; corpo opcional `{"motivo":"..."}` (máx. 255) |

Erros: `404` inexistente, `409 SOLICITACAO_JA_DECIDIDA`, `409 SOLICITACAO_EXPIRADA` (prazo do convite já terminou). A decisão trava a linha (`PESSIMISTIC_WRITE`): duas decisões simultâneas não se sobrepõem.

**Onde o QR/token é bloqueado**
- `ConviteResponse` e `ConvidadoQrCodeResponse` só trazem `token` com status ATIVO (pendente/recusado/cancelado/expirado nunca).
- `ConviteValidacaoService`: novos motivos `PENDENTE_APROVACAO` e `RECUSADO` (checados antes de cancelado/expirado/janela/ambiente; **nunca consomem uso**). Mesmo com um token válido forjado/vazado, a leitura é negada.
- `ConviteAcessoService`: os dois motivos novos são mapeados por palavra-chave para o seu `MotivoRecusa` (PENDENT/APROV/NAO_AUTORIZ/INVALID e RECUS/NEGAD/NAO_AUTORIZ/INVALID). **Confira o log de subida**: ele mostra o mapeamento e avisa se algum motivo ficou sem constante.
- Renovar convite pendente/recusado → `409 CONVITE_NAO_APROVADO`.
- Limite de `20` solicitações pendentes por morador (`LIMITE_DE_SOLICITACOES`).

**Pontos de atenção**
- `auth.getName()` precisa ser o e-mail do usuário (mesma premissa dos outros controllers) para registrar quem decidiu.
- O convidado já é cadastrado (com senha provisória, devolvida uma vez ao morador) no momento da criação; ele só enxerga o QR após a aprovação.
- A lista de ambientes para o formulário do morador não existe no pacote: o front tenta `GET /api/ambientes` e, sem ele, usa `VITE_AMBIENTE_ID`.
- O CORS do seu `SecurityConfig` precisa liberar `PATCH` (cancelar convite) se o front estiver em outra origem.

---

## Passo 1 — Cadastro público = CONVIDADO

**Novos** (`auth/`): `CadastroRequest`, `CadastroResponse`, `CadastroService`, `CadastroController`.

- `POST /api/auth/cadastro` (público) recebe `{nome, email, senha}`. **Não existe campo `perfil`**: o serviço fixa `Perfil.CONVIDADO` e `ativo=true`; qualquer `perfil` enviado no JSON é ignorado.
- E-mail normalizado para minúsculas; duplicado → 409 `EMAIL_EM_USO` (inclusive em corrida, via `DataIntegrityViolationException`).
- Senha: 8–72 caracteres com minúscula, maiúscula e número (igual ao front).
- Sem CPF no cadastro (`usuario.cpf` fica `null`).

**`SecurityConfig`**: `/api/auth/cadastro` em `permitAll`; `POST /api/usuarios/**` restrito a OPERADOR/ADMIN.

### Verifique no seu projeto
1. Se você já tem um endpoint de cadastro/registro público, force `Perfil.CONVIDADO` nele também (ou remova-o).
2. O caminho `/api/usuarios` é suposição: ajuste ao controller real com que a portaria cria Morador e teste que continua funcionando.
3. Java não foi compilado aqui (Maven Central bloqueado); rode `mvn -q compile`.
