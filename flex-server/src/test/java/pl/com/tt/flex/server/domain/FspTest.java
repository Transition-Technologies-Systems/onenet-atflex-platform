package pl.com.tt.flex.server.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.web.rest.TestUtil;

public class FspTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(FspEntity.class);
        FspEntity fspEntity1 = new FspEntity();
        fspEntity1.setId(1L);
        FspEntity fspEntity2 = new FspEntity();
        fspEntity2.setId(fspEntity1.getId());
        assertThat(fspEntity1).isEqualTo(fspEntity2);
        fspEntity2.setId(2L);
        assertThat(fspEntity1).isNotEqualTo(fspEntity2);
        fspEntity1.setId(null);
        assertThat(fspEntity1).isNotEqualTo(fspEntity2);
    }
}
