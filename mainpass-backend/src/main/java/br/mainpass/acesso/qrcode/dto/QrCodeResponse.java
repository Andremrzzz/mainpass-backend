package br.mainpass.acesso.qrcode.dto;
import java.time.Instant;
import br.mainpass.acesso.qrcode.QrCode;
import br.mainpass.acesso.qrcode.StatusQrCode;
/** O token e a imagem so sao devolvidos no instante da geracao. */
public record QrCodeResponse(Long id, Long ambienteId, StatusQrCode status, Instant expiraEm,
                             String token, String imagemBase64) {
 public static QrCodeResponse from(QrCode q,String token,String imagemBase64){
  return new QrCodeResponse(q.getId(),q.getAmbiente().getId(),q.getStatus(),q.getExpiraEm(),token,imagemBase64);
 }
 public static QrCodeResponse from(QrCode q){return from(q,null,null);}
}
