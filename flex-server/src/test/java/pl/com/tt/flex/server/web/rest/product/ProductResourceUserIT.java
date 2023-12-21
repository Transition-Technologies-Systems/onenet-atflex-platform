package pl.com.tt.flex.server.web.rest.product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import pl.com.tt.flex.server.FlexserverApp;
import pl.com.tt.flex.server.repository.product.ProductRepository;

import javax.persistence.EntityManager;

import static pl.com.tt.flex.model.security.permission.Authority.*;

@SpringBootTest(classes = FlexserverApp.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = {FLEX_USER_PRODUCT_VIEW, FLEX_USER_PRODUCT_MANAGE, FLEX_USER_PRODUCT_DELETE})
public class ProductResourceUserIT extends ProductResourceIT {

    @Autowired
    public ProductResourceUserIT(ProductRepository productRepository, EntityManager em, MockMvc restProductMockMvc) {
        super(productRepository, em, restProductMockMvc, "/api/user/products");
    }
}
