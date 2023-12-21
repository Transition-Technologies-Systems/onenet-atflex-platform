//package pl.com.tt.flex.server.service.mapper;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import pl.com.tt.flex.server.service.potential.mapper.FlexPotentialMapper;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//public class FlexPotentialMapperTest {
//
//    private FlexPotentialMapper flexPotentialMapper;
//
//    @BeforeEach
//    public void setUp() {
//        flexPotentialMapper = new FlexPotentialMapperImpl();
//    }
//
//    @Test
//    public void testEntityFromId() {
//        Long id = 1L;
//        assertThat(flexPotentialMapper.fromId(id).getId()).isEqualTo(id);
//        assertThat(flexPotentialMapper.fromId(null)).isNull();
//    }
//}
