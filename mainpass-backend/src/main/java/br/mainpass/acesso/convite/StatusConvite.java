package br.mainpass.acesso.convite;

/**
 * Estado PERSISTIDO do convite. EXPIRADO tambem e calculado em leitura
 * (ATIVO com dataFim vencida): veja {@link Convite#statusEfetivo}.
 */
public enum StatusConvite {
    ATIVO,
    UTILIZADO,
    EXPIRADO,
    CANCELADO
}
