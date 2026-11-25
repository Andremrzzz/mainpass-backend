package br.mainpass.acesso.usuario;

/**
 * No Spring Security vira a authority "ROLE_" + nome (ex.: ROLE_ADMIN).
 * USUARIO = morador (anfitriao); CONVIDADO = visitante com convite de um morador.
 */
public enum Perfil {
    ADMIN,
    OPERADOR,
    USUARIO,
    CONVIDADO
}
