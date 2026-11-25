package br.mainpass.acesso.qrcode;

import java.time.Instant;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

import br.mainpass.acesso.ambiente.Ambiente;
import br.mainpass.acesso.ambiente.AmbienteRepository;
import br.mainpass.acesso.common.exception.BusinessException;
import br.mainpass.acesso.common.exception.ResourceNotFoundException;
import br.mainpass.acesso.qrcode.dto.QrCodeRequest;
import br.mainpass.acesso.qrcode.dto.QrCodeResponse;
import br.mainpass.acesso.usuario.Perfil;
import br.mainpass.acesso.usuario.Usuario;
import br.mainpass.acesso.usuario.UsuarioRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.mainpass.acesso.ambiente.*;
import br.mainpass.acesso.common.exception.*;
import br.mainpass.acesso.common.util.TokenUtil;
import br.mainpass.acesso.config.AppProperties;
import br.mainpass.acesso.permissao.PermissaoAcessoRepository;
import br.mainpass.acesso.qrcode.dto.*;
import br.mainpass.acesso.usuario.*;

@Service
public class QrCodeService {
 private final QrCodeRepository qrs; private final UsuarioRepository usuarios; private final AmbienteRepository ambientes;
 private final PermissaoAcessoRepository permissoes; private final long validade;
 public QrCodeService(QrCodeRepository q,UsuarioRepository u,AmbienteRepository a,PermissaoAcessoRepository p,AppProperties props){
  qrs=q;usuarios=u;ambientes=a;permissoes=p;validade=props.qrcode().expirationMinutes();
 }
 @Transactional public QrCodeResponse gerar(String email, QrCodeRequest req){
  Usuario u=usuarios.findByEmail(email).filter(Usuario::isAtivo).orElseThrow(()->new ResourceNotFoundException("Usuario"));
  Ambiente a=ambientes.findById(req.ambienteId()).orElseThrow(()->new ResourceNotFoundException("Ambiente",req.ambienteId()));
  Instant now=Instant.now();
  if(!a.isAtivo())throw BusinessException.badRequest("AMBIENTE_INATIVO","Este ambiente esta inativo");
  boolean autorizado=permissoes.findByUsuarioIdAndAmbienteId(u.getId(),a.getId()).filter(p->p.vigenteEm(now)).isPresent();
  if(!autorizado)throw new BusinessException(org.springframework.http.HttpStatus.FORBIDDEN,"SEM_PERMISSAO","Usuario nao possui permissao vigente para este ambiente");
  String token=TokenUtil.gerarToken();
  QrCode q=new QrCode();q.setUsuario(u);q.setAmbiente(a);q.setTokenHash(TokenUtil.sha256Hex(token));q.setStatus(StatusQrCode.ATIVO);q.setExpiraEm(now.plusSeconds(validade*60));
  QrCode salvo=qrs.save(q);
  return QrCodeResponse.from(salvo,token,gerarImagem(token));
 }
 @Transactional(readOnly=true) public List<QrCodeResponse> meus(String email){
  Usuario u=usuarios.findByEmail(email).orElseThrow(()->new ResourceNotFoundException("Usuario"));
  return qrs.findByUsuarioIdOrderByCriadoEmDesc(u.getId()).stream().map(QrCodeResponse::from).toList();
 }
 @Transactional public void revogar(String email,Long id){
  Usuario u=usuarios.findByEmail(email).orElseThrow(()->new ResourceNotFoundException("Usuario"));
  QrCode q=qrs.findById(id).orElseThrow(()->new ResourceNotFoundException("QR Code",id));
  if(!q.getUsuario().getId().equals(u.getId()) && u.getPerfil()!= Perfil.ADMIN)
   throw new BusinessException(org.springframework.http.HttpStatus.FORBIDDEN,"ACESSO_NEGADO","Voce nao pode revogar este QR Code");
  if(q.getStatus()==StatusQrCode.ATIVO)q.setStatus(StatusQrCode.REVOGADO);
 }

 private String gerarImagem(String token){
  try {
   var matriz=new QRCodeWriter().encode(token,BarcodeFormat.QR_CODE,320,320);
   ByteArrayOutputStream out=new ByteArrayOutputStream();
   MatrixToImageWriter.writeToStream(matriz,"PNG",out);
   return Base64.getEncoder().encodeToString(out.toByteArray());
  } catch(WriterException | java.io.IOException ex){
   throw new IllegalStateException("Nao foi possivel gerar a imagem do QR Code",ex);
  }
 }
}
