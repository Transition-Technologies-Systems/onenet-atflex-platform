package pl.com.tt.flex.flex.agno.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.com.tt.flex.flex.agno.domain.EntityInterface;
import pl.com.tt.flex.flex.agno.repository.AbstractJpaRepository;
import pl.com.tt.flex.flex.agno.service.mapper.EntityMapper;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;


public interface AbstractService<ENTITY extends EntityInterface<ID>, DTO, ID extends Serializable> {

    DTO save(DTO dto);

    List<DTO> save(List<DTO> dtos);

    DTO update(DTO dto);

    void delete(ID id);

    void delete(DTO dto);

    void delete(List<DTO> dtos);

    Optional<DTO> findById(ID id);

    List<DTO> findAll(List<ID> ids);

    long count();

    boolean exists(ID id);

    List<DTO> findAll();

    Page<DTO> findAll(Pageable pageable);

    AbstractJpaRepository<ENTITY, ID> getRepository();

    EntityMapper<DTO, ENTITY> getMapper();
}
