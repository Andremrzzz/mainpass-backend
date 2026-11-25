package br.mainpass.acesso.ambiente;

import java.util.List;

import br.mainpass.acesso.ambiente.dto.AmbienteRequest;
import br.mainpass.acesso.ambiente.dto.AmbienteResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.mainpass.acesso.ambiente.dto.*;
import br.mainpass.acesso.common.exception.BusinessException;
import br.mainpass.acesso.common.exception.ResourceNotFoundException;

@Service
public class AmbienteService {
    private final AmbienteRepository repository;
    public AmbienteService(AmbienteRepository repository){this.repository=repository;}
    @Transactional(readOnly=true) public List<AmbienteResponse> listar(){return repository.findAll().stream().map(AmbienteResponse::from).toList();}
    @Transactional public AmbienteResponse criar(AmbienteRequest req){
        if(repository.existsByNomeIgnoreCase(req.nome().trim())) throw BusinessException.conflict("AMBIENTE_DUPLICADO","Ja existe ambiente com este nome");
        Ambiente a=new Ambiente(); a.setNome(req.nome().trim()); a.setDescricao(req.descricao()); a.setAtivo(true);
        return AmbienteResponse.from(repository.save(a));
    }
    @Transactional public AmbienteResponse atualizar(Long id,AmbienteRequest req){
        Ambiente a=repository.findById(id).orElseThrow(()->new ResourceNotFoundException("Ambiente",id));
        repository.findByNomeIgnoreCase(req.nome().trim()).filter(x->!x.getId().equals(id)).ifPresent(x->{throw BusinessException.conflict("AMBIENTE_DUPLICADO","Ja existe ambiente com este nome");});
        a.setNome(req.nome().trim());a.setDescricao(req.descricao());return AmbienteResponse.from(a);
    }
    @Transactional public void status(Long id,boolean ativo){Ambiente a=repository.findById(id).orElseThrow(()->new ResourceNotFoundException("Ambiente",id));a.setAtivo(ativo);}
}
