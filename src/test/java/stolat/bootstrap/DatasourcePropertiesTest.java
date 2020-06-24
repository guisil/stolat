package stolat.bootstrap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;

@ExtendWith(SpringExtension.class)
@AutoConfigureEmbeddedDatabase(beanName = "dataSource")
public class DatasourcePropertiesTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void contextLoads() {
    }
}
