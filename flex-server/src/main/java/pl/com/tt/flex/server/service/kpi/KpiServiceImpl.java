package pl.com.tt.flex.server.service.kpi;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.model.service.dto.kpi.KpiDTO;
import pl.com.tt.flex.model.service.dto.kpi.KpiType;
import pl.com.tt.flex.server.domain.kpi.KpiEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.kpi.KpiRepository;
import pl.com.tt.flex.server.service.common.AbstractServiceImpl;
import pl.com.tt.flex.server.service.kpi.generator.KpiGenerateException;
import pl.com.tt.flex.server.service.kpi.generator.KpiGenerator;
import pl.com.tt.flex.server.service.kpi.generator.factory.KpiGeneratorFactory;
import pl.com.tt.flex.server.service.kpi.mapper.KpiMapper;
import pl.com.tt.flex.server.service.mapper.EntityMapper;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class KpiServiceImpl extends AbstractServiceImpl<KpiEntity, KpiDTO, Long> implements KpiService {

    private final KpiMapper kpiMapper;
    private final KpiRepository kpiRepository;
    private final KpiGeneratorFactory kpiGeneratorFactory;

    @Override
    public AbstractJpaRepository<KpiEntity, Long> getRepository() {
        return this.kpiRepository;
    }

    @Override
    public EntityMapper<KpiDTO, KpiEntity> getMapper() {
        return this.kpiMapper;
    }

    @Override
    @Transactional
    public FileDTO saveAndGenerateFile(KpiDTO kpiDTO) throws KpiGenerateException {
        KpiGenerator kpiGenerator = kpiGeneratorFactory.getGenerator(kpiDTO.getType());
        FileDTO generate = kpiGenerator.generate(kpiDTO);
        save(kpiDTO);
        return generate;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FileDTO> regenerate(Long id) throws KpiGenerateException {
        Optional<KpiEntity> kpiOpt = kpiRepository.findById(id);
        if (kpiOpt.isEmpty()) {
            return Optional.empty();
        }

        KpiDTO kpiDTO = kpiMapper.toDto(kpiOpt.get());
        KpiGenerator kpiGenerator = kpiGeneratorFactory.getGenerator(kpiDTO.getType());
        return Optional.of(kpiGenerator.generate(kpiDTO));
    }

    @Override
    public List<KpiType.KpiTypeDTO> getAllTypes() {
        return KpiType.getTypes();
    }
}
