package stolat.bootstrap.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

@Component
public class SqlScriptRunner {

    private static final Logger LOG = LoggerFactory.getLogger(SqlScriptRunner.class);

    @Autowired
    private ApplicationContext context;

    @Autowired
    private DataSource dataSource;

    public void runInitialisationScripts(String scriptFilename) throws SQLException {
        Resource script = context.getResource(scriptFilename);
        EncodedResource encodedResource = new EncodedResource(script, StandardCharsets.UTF_8);
        ScriptUtils.executeSqlScript(dataSource.getConnection(), encodedResource);
    }
}
