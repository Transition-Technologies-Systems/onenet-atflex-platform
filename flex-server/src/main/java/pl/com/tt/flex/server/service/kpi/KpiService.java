package pl.com.tt.flex.server.service.kpi;

import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.model.service.dto.kpi.KpiDTO;
import pl.com.tt.flex.model.service.dto.kpi.KpiType;
import pl.com.tt.flex.server.domain.kpi.KpiEntity;
import pl.com.tt.flex.server.service.AbstractService;
import pl.com.tt.flex.server.service.kpi.generator.KpiGenerateException;

import java.util.List;
import java.util.Optional;

public interface KpiService extends AbstractService<KpiEntity, KpiDTO, Long> {
	FileDTO saveAndGenerateFile(KpiDTO kpiDTO) throws KpiGenerateException;

    Optional<FileDTO> regenerate(Long id) throws KpiGenerateException;

    List<KpiType.KpiTypeDTO> getAllTypes();
}
