package br.mainpass.acesso.convite;

/**
 * Estado PERSISTIDO do convite. EXPIRADO tambem e calculado em leitura
 * (ATIVO ou PENDENTE_APROVACAO com dataFim vencida): veja {@link Convite#statusEfetivo}.
 *
 * Ciclo: PENDENTE_APROVACAO -> (portaria aprova) ATIVO -> UTILIZADO | EXPIRADO | CANCELADO
 *                           -> (portaria recusa) RECUSADO
 *                           -> (morador desiste) CANCELADO
 * O QR Code (token) so existe para o convidado/morador enquanto o convite estiver ATIVO.
 */
public enum StatusConvite {
    PENDENTE_APROVACAO,
    RECUSADO,
    ATIVO,
    UTILIZADO,
    EXPIRADO,
    CANCELADO
}
