package br.mainpass.acesso.registro;

import java.time.Instant;

import br.mainpass.acesso.ambiente.Ambiente;
import br.mainpass.acesso.ambiente.AmbienteRepository;
import br.mainpass.acesso.qrcode.QrCode;
import br.mainpass.acesso.qrcode.QrCodeRepository;
import br.mainpass.acesso.qrcode.StatusQrCode;
import br.mainpass.acesso.registro.dto.ValidarAcessoResponse;
import br.mainpass.acesso.usuario.Usuario;
import br.mainpass.acesso.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.mainpass.acesso.common.util.TokenUtil;
import br.mainpass.acesso.permissao.PermissaoAcessoRepository;
import br.mainpass.acesso.ambiente.*;
import br.mainpass.acesso.qrcode.*;
import br.mainpass.acesso.registro.dto.*;
import br.mainpass.acesso.usuario.*;

@Service
public class RegistroAcessoService {
 private final RegistroAcessoRepository registros; private final QrCodeRepository qrs; private final UsuarioRepository usuarios;
 private final AmbienteRepository ambientes; private final PermissaoAcessoRepository permissoes;
 public RegistroAcessoService(RegistroAcessoRepository r,QrCodeRepository q,UsuarioRepository u,AmbienteRepository a,PermissaoAcessoRepository p){
  registros=r;qrs=q;usuarios=u;ambientes=a;permissoes=p;
 }
 @Transactional
 public ValidarAcessoResponse validar(String token, Long ambienteId, String emailOperador){
  Usuario operador=usuarios.findByEmail(emailOperador).orElseThrow();
  Ambiente ambiente=ambientes.findById(ambienteId).orElse(null);
  QrCode qr=qrs.findByTokenHash(TokenUtil.sha256Hex(token)).orElse(null);
  RegistroAcesso log=new RegistroAcesso();log.setOperador(operador);log.setAmbiente(ambiente);
  if(qr==null){log.setResultado(ResultadoAcesso.CODIGO_INVALIDO);log.setMotivo(MotivoRecusa.NAO_ENCONTRADO);registros.save(log);return new ValidarAcessoResponse(log.getResultado(),log.getMotivo(),null);}
  log.setQrCode(qr);log.setUsuario(qr.getUsuario());
  MotivoRecusa motivo=null; Instant now=Instant.now();
  if(qr.getStatus()== StatusQrCode.UTILIZADO) motivo=MotivoRecusa.JA_UTILIZADO;
  else if(qr.getStatus()==StatusQrCode.REVOGADO) motivo=MotivoRecusa.REVOGADO;
  else if(qr.getStatus()==StatusQrCode.EXPIRADO || !now.isBefore(qr.getExpiraEm())) { motivo=MotivoRecusa.EXPIRADO; if(qr.getStatus()==StatusQrCode.ATIVO)qr.setStatus(StatusQrCode.EXPIRADO); }
  else if(!qr.getUsuario().isAtivo()) motivo=MotivoRecusa.USUARIO_INATIVO;
  else if(ambiente==null || !ambiente.isAtivo()) motivo=MotivoRecusa.AMBIENTE_INATIVO;
  else if(!qr.getAmbiente().getId().equals(ambienteId)) motivo=MotivoRecusa.AMBIENTE_DIFERENTE;
  else if(permissoes.findByUsuarioIdAndAmbienteId(qr.getUsuario().getId(),ambienteId).filter(p->p.vigenteEm(now)).isEmpty()) motivo=MotivoRecusa.SEM_PERMISSAO;
  else if(qrs.atualizarStatus(qr.getId(),StatusQrCode.ATIVO,StatusQrCode.UTILIZADO,now)==0) motivo=MotivoRecusa.JA_UTILIZADO;
  if(motivo==null){log.setResultado(ResultadoAcesso.AUTORIZADO);log.setMotivo(null);registros.save(log);return new ValidarAcessoResponse(ResultadoAcesso.AUTORIZADO,null,qr.getUsuario().getNome());}
  log.setResultado(ResultadoAcesso.RECUSADO);log.setMotivo(motivo);registros.save(log);
  return new ValidarAcessoResponse(log.getResultado(),motivo,null);
 }
}
