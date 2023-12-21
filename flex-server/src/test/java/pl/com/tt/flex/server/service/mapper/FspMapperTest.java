package pl.com.tt.flex.server.service.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.com.tt.flex.server.service.fsp.mapper.FspMapper;
import pl.com.tt.flex.server.service.fsp.mapper.FspMapperImpl;

import static org.assertj.core.api.Assertions.assertThat;

public class FspMapperTest {

    private FspMapper fspMapper;

    @BeforeEach
    public void setUp() {
        fspMapper = new FspMapperImpl();
    }

    @Test
    public void testEntityFromId() {
        Long id = 1L;
        assertThat(fspMapper.fromId(id).getId()).isEqualTo(id);
        assertThat(fspMapper.fromId(null)).isNull();
    }
}
