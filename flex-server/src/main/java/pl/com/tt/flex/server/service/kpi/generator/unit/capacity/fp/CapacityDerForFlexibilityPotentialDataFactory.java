package pl.com.tt.flex.server.service.kpi.generator.unit.capacity.fp;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.server.domain.potential.FlexPotentialEntity;
import pl.com.tt.flex.server.repository.potential.FlexPotentialRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static pl.com.tt.flex.server.service.kpi.generator.utils.SortUtils.sortMapWithString;

@Component
@AllArgsConstructor
@Transactional(readOnly = true)
public class CapacityDerForFlexibilityPotentialDataFactory {

    private final FlexPotentialRepository flexPotentialRepository;

    CapacityDerForFlexibilityPotentialData create() {
        List<FlexPotentialEntity> flexPotentialEntities = flexPotentialRepository.findAllByRegisteredIsTrueAndActiveIsTrueAndProductCmvcIsTrue();
        Map<String, BigDecimal> fpVolumeSumGroupingByProducts = getVolumeSumGroupingByProduct(flexPotentialEntities);
        return new CapacityDerForFlexibilityPotentialData(fpVolumeSumGroupingByProducts);
    }

    private static Map<String, BigDecimal> getVolumeSumGroupingByProduct(List<FlexPotentialEntity> flexPotentialEntities) {
        Map<String, BigDecimal> fpVolumeSumGroupingByProducts = flexPotentialEntities.stream()
                                                                                     .collect(Collectors.groupingBy(
                                                                                         fp -> fp.getProduct().getShortName(),
                                                                                         Collectors.reducing(BigDecimal.ZERO, FlexPotentialEntity::getVolume, BigDecimal::add)
                                                                                     ));
        return sortMapWithString(fpVolumeSumGroupingByProducts);
    }
}
