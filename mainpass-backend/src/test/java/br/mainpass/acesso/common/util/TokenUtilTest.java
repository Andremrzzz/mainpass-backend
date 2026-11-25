package br.mainpass.acesso.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class TokenUtilTest {

    @Test
    void tokenTem43CaracteresUrlSafe() {
        String token = TokenUtil.gerarToken();
        assertThat(token).hasSize(43).matches("[A-Za-z0-9_-]+");
    }

    @Test
    void tokensNaoSeRepetem() {
        Set<String> tokens = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            tokens.add(TokenUtil.gerarToken());
        }
        assertThat(tokens).hasSize(1000);
    }

    @Test
    void hashSha256TemValorConhecidoE64Caracteres() {
        assertThat(TokenUtil.sha256Hex("abc"))
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }
}
