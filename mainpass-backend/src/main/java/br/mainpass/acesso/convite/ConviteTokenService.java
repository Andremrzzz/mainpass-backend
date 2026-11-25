package br.mainpass.acesso.convite;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Token do QR Code do convite: {@code MPC.<idBase36>.<assinatura>}.
 *
 * A assinatura e um HMAC-SHA256 do id, entao o token NAO precisa ser guardado no banco: pode ser
 * recalculado para reexibir o QR ao morador/convidado e e impossivel de adivinhar a partir do id.
 * Revogacao, validade e usos sao sempre conferidos no banco (ConviteValidacaoService).
 * Trocar o segredo invalida todos os QR Codes ja emitidos.
 */
@Component
public class ConviteTokenService {

    private static final String PREFIXO = "MPC";
    private static final String ALGORITMO = "HmacSHA256";
    private static final int BYTES_ASSINATURA = 10; // 80 bits: suficiente para validacao online

    private final SecretKeySpec chave;

    public ConviteTokenService(@Value("${mainpass.convite.token-secret}") String segredo) {
        if (segredo == null || segredo.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("mainpass.convite.token-secret deve ter pelo menos 32 bytes");
        }
        this.chave = new SecretKeySpec(segredo.getBytes(StandardCharsets.UTF_8), ALGORITMO);
    }

    public String gerar(Long conviteId) {
        String id = Long.toString(conviteId, 36).toUpperCase();
        return PREFIXO + "." + id + "." + assinar(conviteId);
    }

    /** Devolve o id do convite se o token for autentico; vazio caso contrario (formato ou assinatura invalidos). */
    public Optional<Long> verificar(String token) {
        if (token == null) {
            return Optional.empty();
        }
        String[] partes = token.trim().split("\\.");
        if (partes.length != 3 || !PREFIXO.equals(partes[0]) || partes[1].isEmpty() || partes[1].length() > 13) {
            return Optional.empty();
        }
        try {
            long id = Long.parseLong(partes[1], 36);
            if (id <= 0) {
                return Optional.empty();
            }
            byte[] esperado = assinar(id).getBytes(StandardCharsets.UTF_8);
            byte[] recebido = partes[2].toUpperCase().getBytes(StandardCharsets.UTF_8);
            return MessageDigest.isEqual(esperado, recebido) ? Optional.of(id) : Optional.empty();
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private String assinar(long id) {
        try {
            Mac mac = Mac.getInstance(ALGORITMO);
            mac.init(chave);
            byte[] hash = mac.doFinal(("convite:" + id).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().withUpperCase().formatHex(hash, 0, BYTES_ASSINATURA);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC indisponivel", e);
        }
    }
}
