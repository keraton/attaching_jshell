package uk.org.cinquin.attaching_jshell;

import java.io.File;
import java.io.IOException;

public final class JavaProcess {

    private JavaProcess() {}

    public static Process exec(Class klass) throws IOException,
            InterruptedException {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome +
                File.separator + "bin" +
                File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = klass.getName();

        ProcessBuilder builder = new ProcessBuilder(
                javaBin, "-cp", classpath, className);

        return builder.inheritIO().start();
    }

}