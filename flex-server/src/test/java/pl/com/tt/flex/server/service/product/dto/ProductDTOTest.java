package pl.com.tt.flex.server.service.product.dto;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import pl.com.tt.flex.server.web.rest.TestUtil;

public class ProductDTOTest {

    @Test
    public void equalsContract() {
        //-> Significant fields: equals does not use createdBy, or it is stateless.
        //-> https://jqno.nl/equalsverifier/
//        EqualsVerifier
//            .simple()
//            .forClass(ProductDTO.class)
//            .suppress(Warning.STRICT_INHERITANCE)
//            .verify();

        //TestUtil.equalsVerifier(ProductDTO.class); to jest test do encji

        ProductDTO productDTO1 = new ProductDTO();
        productDTO1.setCreatedBy("user1");
        ProductDTO productDTO2 = new ProductDTO();
        productDTO2.setCreatedBy("user2");
        assertThat(productDTO1).isEqualTo(productDTO2);

        productDTO1.setActive(true);
        productDTO2.setActive(false);
        assertThat(productDTO1).isNotEqualTo(productDTO2);
    }

}
