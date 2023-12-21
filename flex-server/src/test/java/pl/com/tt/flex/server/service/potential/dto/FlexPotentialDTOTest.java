package pl.com.tt.flex.server.service.potential.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import pl.com.tt.flex.server.web.rest.TestUtil;

public class FlexPotentialDTOTest {

    @Test
    public void dtoEqualsVerifier() throws Exception {
//        TestUtil.equalsVerifier(FlexPotentialDTO.class);
        FlexPotentialDTO flexPotentialDTO1 = new FlexPotentialDTO();
        flexPotentialDTO1.setId(1L);
        FlexPotentialDTO flexPotentialDTO2 = new FlexPotentialDTO();
        assertThat(flexPotentialDTO1).isNotEqualTo(flexPotentialDTO2);
        flexPotentialDTO2.setId(flexPotentialDTO1.getId());
        assertThat(flexPotentialDTO1).isEqualTo(flexPotentialDTO2);
        flexPotentialDTO2.setId(2L);
        assertThat(flexPotentialDTO1).isNotEqualTo(flexPotentialDTO2);
        flexPotentialDTO1.setId(null);
        assertThat(flexPotentialDTO1).isNotEqualTo(flexPotentialDTO2);
    }
}
