package stolat.bootstrap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import picocli.CommandLine;
import stolat.bootstrap.cli.BootstrapCommand;

import static picocli.CommandLine.IFactory;

@SpringBootApplication(scanBasePackages = {"stolat"})
@Slf4j
public class StoLatBootstrapApplication implements CommandLineRunner, ExitCodeGenerator {

    private final IFactory factory;
    private final BootstrapCommand bootstrapCommand;
    private int exitCode;

    StoLatBootstrapApplication(IFactory factory, BootstrapCommand bootstrapCommand) {
        this.factory = factory;
        this.bootstrapCommand = bootstrapCommand;
    }

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(StoLatBootstrapApplication.class, args)));
    }

    @Override
    public void run(String... args) throws Exception {
        exitCode = new CommandLine(bootstrapCommand, factory).execute(args);
        log.info("Launching application");
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}
