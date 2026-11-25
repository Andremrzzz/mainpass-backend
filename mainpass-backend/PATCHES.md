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
