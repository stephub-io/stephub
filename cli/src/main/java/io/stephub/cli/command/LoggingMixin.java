package io.stephub.cli.command;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import static picocli.CommandLine.Spec.Target.MIXEE;

public class LoggingMixin {
    private @CommandLine.Spec(MIXEE)
    CommandLine.Model.CommandSpec mixee;

    private boolean[] verbosity = new boolean[0];

    private boolean noColor = false;

    private static LoggingMixin getTopLevelCommandLoggingMixin(final CommandLine.Model.CommandSpec commandSpec) {
        return ((StepCtlMainCommand) commandSpec.root().userObject()).loggingMixin;
    }

    @CommandLine.Option(names = {"-v", "--verbose"}, description = {
            "Specify multiple -v options to increase verbosity.",
            "For example, `-v -v -v` or `-vvv`"})
    public void setVerbose(final boolean[] verbosity) {
        getTopLevelCommandLoggingMixin(this.mixee).verbosity = verbosity;
    }

    @CommandLine.Option(names = {"-nc", "--no-color"}, description = {
            "Disables output with coloring"})
    public void setNoColor(final boolean noColor) {
        getTopLevelCommandLoggingMixin(this.mixee).noColor = noColor;
    }

    public boolean[] getVerbosity() {
        return getTopLevelCommandLoggingMixin(this.mixee).verbosity;
    }

    public boolean isNoColor() {
        return getTopLevelCommandLoggingMixin(this.mixee).noColor;
    }

    public static void configure(final CommandLine.ParseResult parseResult) {
        getTopLevelCommandLoggingMixin(parseResult.commandSpec()).configureLoggers();
    }

    public void configureLoggers() {
        final LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        final Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(getTopLevelCommandLoggingMixin(this.mixee).calcRootLogLevel());
        final Logger own = (Logger) LoggerFactory.getLogger("io.stephub");
        own.setLevel(getTopLevelCommandLoggingMixin(this.mixee).calcOwnLogLevel());

        final PatternLayoutEncoder ple = new PatternLayoutEncoder();
        String colorPrefix = "";
        String colorSuffix = "";
        if (!this.isNoColor()) {
            colorPrefix = "%clr(";
            colorSuffix = ")";
        }
        if (this.getVerbosity().length == 0) {
            ple.setPattern(colorPrefix + "%p" + colorSuffix + ": %m%n%wEx");
        } else {
            ple.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} " + colorPrefix + "%5p" + colorSuffix + " [%15.15t] %-40.40logger{39} : %m%n%wEx");
        }
        ple.setContext(lc);
        ple.start();
        final ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setEncoder(ple);
        consoleAppender.setContext(lc);
        consoleAppender.start();

        root.detachAndStopAllAppenders();
        root.addAppender(consoleAppender);
    }

    private Level calcRootLogLevel() {
        switch (this.getVerbosity().length) {
            case 0:
                return Level.WARN;
            case 1:
                return Level.INFO;
            case 2:
                return Level.DEBUG;
            default:
                return Level.TRACE;
        }
    }

    private Level calcOwnLogLevel() {
        switch (this.getVerbosity().length) {
            case 0:
                return Level.INFO;
            case 1:
                return Level.DEBUG;
            default:
                return Level.TRACE;
        }
    }
}
