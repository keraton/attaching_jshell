package uk.org.cinquin.attaching_jshell.internal;

import java.io.File;
import java.io.IOException;

public final class JavaProcess {

    private JavaProcess() {}

    public static Process exec(Class klass, int debugPort) throws IOException,
            InterruptedException {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome +
                File.separator + "bin" +
                File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = klass.getName();

        String debugOptions = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=localhost:" + debugPort;

        ProcessBuilder builder = new ProcessBuilder(
                javaBin, "-cp", classpath, debugOptions, className);

        return builder.inheritIO().start();
    }

}