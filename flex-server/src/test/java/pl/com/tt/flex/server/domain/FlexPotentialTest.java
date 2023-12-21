package pl.com.tt.flex.server.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import pl.com.tt.flex.server.domain.potential.FlexPotentialEntity;
import pl.com.tt.flex.server.web.rest.TestUtil;

public class FlexPotentialTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(FlexPotentialEntity.class);
        FlexPotentialEntity flexPotentialEntity1 = new FlexPotentialEntity();
        flexPotentialEntity1.setId(1L);
        FlexPotentialEntity flexPotentialEntity2 = new FlexPotentialEntity();
        flexPotentialEntity2.setId(flexPotentialEntity1.getId());
        assertThat(flexPotentialEntity1).isEqualTo(flexPotentialEntity2);
        flexPotentialEntity2.setId(2L);
        assertThat(flexPotentialEntity1).isNotEqualTo(flexPotentialEntity2);
        flexPotentialEntity1.setId(null);
        assertThat(flexPotentialEntity1).isNotEqualTo(flexPotentialEntity2);
    }
}
