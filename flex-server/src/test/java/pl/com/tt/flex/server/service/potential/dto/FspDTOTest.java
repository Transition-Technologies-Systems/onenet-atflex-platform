package pl.com.tt.flex.server.service.potential.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import pl.com.tt.flex.server.service.fsp.dto.FspDTO;
import pl.com.tt.flex.server.web.rest.TestUtil;

public class FspDTOTest {

    @Test
    public void dtoEqualsVerifier() throws Exception {
//        TestUtil.equalsVerifier(FspDTO.class);
        FspDTO fspDTO1 = new FspDTO();
        fspDTO1.setId(1L);
        FspDTO fspDTO2 = new FspDTO();
        assertThat(fspDTO1).isNotEqualTo(fspDTO2);
        fspDTO2.setId(fspDTO1.getId());
        assertThat(fspDTO1).isEqualTo(fspDTO2);
        fspDTO2.setId(2L);
        assertThat(fspDTO1).isNotEqualTo(fspDTO2);
        fspDTO1.setId(null);
        assertThat(fspDTO1).isNotEqualTo(fspDTO2);
    }
}
