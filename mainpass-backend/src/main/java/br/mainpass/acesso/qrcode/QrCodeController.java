package br.mainpass.acesso.qrcode;
import java.util.List;

import br.mainpass.acesso.qrcode.dto.QrCodeRequest;
import br.mainpass.acesso.qrcode.dto.QrCodeResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import br.mainpass.acesso.qrcode.dto.*;
import jakarta.validation.Valid;
@RestController @RequestMapping("/api/qrcodes")
public class QrCodeController {
 private final QrCodeService service; public QrCodeController(QrCodeService s){service=s;}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public QrCodeResponse gerar(Authentication a, @Valid @RequestBody QrCodeRequest r){return service.gerar(a.getName(),r);}
 @GetMapping("/meus") public List<QrCodeResponse> meus(Authentication a){return service.meus(a.getName());}
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void revogar(Authentication a,@PathVariable Long id){service.revogar(a.getName(),id);}
}
