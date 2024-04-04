package net.woggioni.wson.cli;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

@Slf4j
@CommandLine.Command(
        name = "wcfg",
        versionProvider = VersionProvider.class,
        subcommands = {WsonCommand.class, WcfgCommand.class})
public class WsonCli implements Runnable {
    public static void main(String[] args) {
        final CommandLine commandLine = new CommandLine(new WsonCli());
        commandLine.setExecutionExceptionHandler((ex, cl, parseResult) -> {
            log.error(ex.getMessage(), ex);
            return CommandLine.ExitCode.SOFTWARE;
        });
        System.exit(commandLine.execute(args));
    }

    @Getter
    @CommandLine.Option(names = {"-V", "--version"}, versionHelp = true)
    private boolean versionHelp;

    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @Override
    public void run() {
        spec.commandLine().usage(System.out);
    }
}
