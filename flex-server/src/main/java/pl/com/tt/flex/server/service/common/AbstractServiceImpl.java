package pl.com.tt.flex.server.service.common;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.server.domain.EntityInterface;
import pl.com.tt.flex.server.service.AbstractService;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Transactional
public abstract class AbstractServiceImpl<ENTITY extends EntityInterface<ID>, DTO, ID extends Serializable> implements AbstractService<ENTITY, DTO, ID> {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DTO save(DTO dto) {
        if (dto != null) {
            ENTITY entity = getMapper().toEntity(dto);
            entity = getRepository().save(entity);
            return getMapper().toDto(entity);
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<DTO> save(final List<DTO> dtos) {
        List<ENTITY> entities = getMapper().toEntity(dtos);
        List<ENTITY> savedEntities = getRepository().saveAll(entities);
        return getMapper().toDto(savedEntities);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DTO update(DTO dto) {
        return save(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(ID id) {
        getRepository().deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(DTO dto) {
        getRepository().delete(getMapper().toEntity(dto));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<DTO> dtos) {
        getRepository().deleteAll(getMapper().toEntity(dtos));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DTO> findById(ID id) {
        return getRepository().findById(id).map(getMapper()::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DTO> findAll(final List<ID> ids) {
        return getMapper().toDto(getRepository().findAllById(ids));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DTO> findAll() {
        return getMapper().toDto(getRepository().findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return getRepository().count();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(ID id) {
        return getRepository().existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DTO> findAll(Pageable pageable) {
        return getRepository().findAll(pageable).map(getMapper()::toDto);
    }
}
