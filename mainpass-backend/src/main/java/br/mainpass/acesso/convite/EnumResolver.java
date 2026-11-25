package br.mainpass.acesso.convite;

import java.util.Optional;

/** Encontra a constante de um enum cujo nome contenha uma das palavras (sem diferenciar maiusculas). */
final class EnumResolver {

    private EnumResolver() {
    }

    static <E extends Enum<E>> Optional<E> primeiro(Class<E> tipo, String... palavras) {
        for (String palavra : palavras) {
            String alvo = palavra.toUpperCase();
            for (E constante : tipo.getEnumConstants()) {
                if (constante.name().toUpperCase().contains(alvo)) {
                    return Optional.of(constante);
                }
            }
        }
        return Optional.empty();
    }
}
