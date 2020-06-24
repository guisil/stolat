package stolat.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import picocli.CommandLine;

import static picocli.CommandLine.IFactory;

@SpringBootApplication
public class StoLatBootstrapApplication implements CommandLineRunner, ExitCodeGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(StoLatBootstrapApplication.class);

    private IFactory factory;
    private BootstrapCommand bootstrapCommand;
    private int exitCode;

    StoLatBootstrapApplication(IFactory factory, BootstrapCommand bootstrapCommand) {
        this.factory = factory;
        this.bootstrapCommand = bootstrapCommand;
    }

    @Override
    public void run(String... args) throws Exception {
//        exitCode = new CommandLine(bootstrapCommand, factory).execute(args);
        LOG.warn("RUNNING!!!!!!!!!!!!!!!!!!!");
    }


    @Override
    public int getExitCode() {
        return exitCode;
    }

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(StoLatBootstrapApplication.class, args)));
    }
}
