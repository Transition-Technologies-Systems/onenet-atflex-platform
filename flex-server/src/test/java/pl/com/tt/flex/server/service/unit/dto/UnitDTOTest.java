package pl.com.tt.flex.server.service.unit.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import pl.com.tt.flex.server.web.rest.TestUtil;

public class UnitDTOTest {

    @Test
    public void dtoEqualsVerifier() throws Exception {
//        TestUtil.equalsVerifier(UnitDTO.class);
        UnitDTO unitDTO1 = new UnitDTO();
        unitDTO1.setId(1L);
        UnitDTO unitDTO2 = new UnitDTO();
        assertThat(unitDTO1).isNotEqualTo(unitDTO2);
        unitDTO2.setId(unitDTO1.getId());
        assertThat(unitDTO1).isEqualTo(unitDTO2);
        unitDTO2.setId(2L);
        assertThat(unitDTO1).isNotEqualTo(unitDTO2);
        unitDTO1.setId(null);
        assertThat(unitDTO1).isNotEqualTo(unitDTO2);
    }
}
