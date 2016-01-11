package org.jboss.windup.tests.bootstrap;

import org.apache.commons.io.output.TeeOutputStream;
import org.jboss.windup.bootstrap.Bootstrap;
import org.junit.After;
import org.junit.Before;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public abstract class AbstractBootstrapTest {
    // see tests/pom.xml
    protected static final String ADDON_REPOSITORY = "target/forge-addons-for-bootstrap-tests";

    private PrintStream originalStdout;
    private PrintStream originalStderr;

    private ByteArrayOutputStream capturedOutputBytes;

    @Before
    public final void captureStdout() {
        originalStdout = System.out;
        originalStderr = System.err;

        capturedOutputBytes = new ByteArrayOutputStream();
        System.setOut(new PrintStream(new TeeOutputStream(originalStdout, capturedOutputBytes)));
        System.setErr(new PrintStream(new TeeOutputStream(originalStderr, capturedOutputBytes)));
    }

    @After
    public final void restoreStdout() {
        System.out.flush();
        System.err.flush();

        System.setOut(originalStdout);
        System.setErr(originalStderr);
    }

    protected final String capturedOutput() {
        try {
            return capturedOutputBytes.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 must be supported");
        }
    }

    protected final void bootstrap(String... args) {
        int newArgumentsCount = 3;
        String[] realArgs = new String[args.length + newArgumentsCount];
        realArgs[0] = "--batchMode";
        realArgs[1] = "--immutableAddonDir";
        realArgs[2] = ADDON_REPOSITORY;
        System.arraycopy(args, 0, realArgs, newArgumentsCount, args.length);

        Bootstrap.main(realArgs);
    }
}
