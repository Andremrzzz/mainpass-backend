package br.mainpass.acesso.permissao;
import java.time.Instant;
import java.util.List;

import br.mainpass.acesso.ambiente.Ambiente;
import br.mainpass.acesso.ambiente.AmbienteRepository;
import br.mainpass.acesso.common.exception.BusinessException;
import br.mainpass.acesso.common.exception.ResourceNotFoundException;
import br.mainpass.acesso.permissao.dto.PermissaoRequest;
import br.mainpass.acesso.permissao.dto.PermissaoResponse;
import br.mainpass.acesso.usuario.Usuario;
import br.mainpass.acesso.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.mainpass.acesso.ambiente.*;
import br.mainpass.acesso.common.exception.*;
import br.mainpass.acesso.permissao.dto.*;
import br.mainpass.acesso.usuario.*;
@Service
public class PermissaoAcessoService {
 private final PermissaoAcessoRepository repo; private final UsuarioRepository usuarios; private final AmbienteRepository ambientes;
 public PermissaoAcessoService(PermissaoAcessoRepository r,UsuarioRepository u,AmbienteRepository a){repo=r;usuarios=u;ambientes=a;}
 @Transactional(readOnly=true) public List<PermissaoResponse> listar(){return repo.findAll().stream().map(PermissaoResponse::from).toList();}
 @Transactional public PermissaoResponse conceder(PermissaoRequest req){
  Usuario u=usuarios.findById(req.usuarioId()).orElseThrow(()->new ResourceNotFoundException("Usuario",req.usuarioId()));
  Ambiente a=ambientes.findById(req.ambienteId()).orElseThrow(()->new ResourceNotFoundException("Ambiente",req.ambienteId()));
  if(repo.existsByUsuarioIdAndAmbienteId(u.getId(),a.getId())) throw BusinessException.conflict("PERMISSAO_DUPLICADA","Usuario ja possui permissao para este ambiente");
  Instant inicio=req.validoDe()==null?Instant.now():req.validoDe();
  if(req.validoAte()!=null && req.validoAte().isBefore(inicio)) throw BusinessException.badRequest("PERIODO_INVALIDO","A data final deve ser posterior a inicial");
  PermissaoAcesso p=new PermissaoAcesso();p.setUsuario(u);p.setAmbiente(a);p.setValidoDe(inicio);p.setValidoAte(req.validoAte());
  return PermissaoResponse.from(repo.save(p));
 }
 @Transactional public void revogar(Long id){if(!repo.existsById(id))throw new ResourceNotFoundException("Permissao",id);repo.deleteById(id);}
}
