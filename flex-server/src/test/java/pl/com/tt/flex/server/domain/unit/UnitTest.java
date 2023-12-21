package pl.com.tt.flex.server.domain.unit;

import org.junit.jupiter.api.Test;
import pl.com.tt.flex.server.web.rest.TestUtil;

import static org.assertj.core.api.Assertions.assertThat;

public class UnitTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(UnitEntity.class);
        UnitEntity unitEntity1 = new UnitEntity();
        unitEntity1.setId(1L);
        UnitEntity unitEntity2 = new UnitEntity();
        unitEntity2.setId(unitEntity1.getId());
        assertThat(unitEntity1).isEqualTo(unitEntity2);
        unitEntity2.setId(2L);
        assertThat(unitEntity1).isNotEqualTo(unitEntity2);
        unitEntity1.setId(null);
        assertThat(unitEntity1).isNotEqualTo(unitEntity2);
    }
}
