package org.jboss.windup.decompilers.jad;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.jboss.windup.decompilers.api.DecompilationConf;
import org.jboss.windup.decompilers.api.DecompilationException;
import org.jboss.windup.decompilers.api.DecompilationPathException;
import org.jboss.windup.decompilers.api.Decompiler;
import org.jboss.windup.decompilers.api.JarDecompilationResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decompiles Java classes with Jad Decompiler.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class JadRetroDecompiler implements Decompiler.Conf<JadConf>, Decompiler.Jar, Decompiler.Type
{
    private static final Logger log = LoggerFactory.getLogger(JadRetroDecompiler.class);

    private static final String APP_NAME = SystemUtils.IS_OS_WINDOWS ? "jad.exe" : "jad";

    private static final int DECOMPILATION_TIMEOUT_MS = 60000;

    /**
     * Decompiles a single .class file.
     * 
     * @param classFile Path to the .class file.
     * @param destDir Destination directory. Classes will be decompiled here - under dir tree as per package structure.
     * @param conf_ Configuration specific for this decompiler.
     */
    @Override
    public void decompileClassFile(File classFile, File destDir, DecompilationConf conf_)
                throws DecompilationPathException
    {

        final JadConf conf = this.retypeConf(conf_);

        // Pre-process with JadRetro - to make new .class files (v50) be like older.
        try
        {
            log.debug("===========================");
            log.debug("Retroansforming: " + classFile.getPath());
            net.sf.jadretro.Main.main(new String[] { classFile.getAbsolutePath() });
        }
        catch (Throwable ex)
        {
            String msg = "Failed running JadRetro on " + classFile.getPath() + "\n    " + ex.getMessage();
            throw new DecompilationPathException(msg, classFile.getPath(), ex);
        }

        // Decompile with Jad
        log.debug("Decompiling: " + classFile.getPath());
        executeJad(classFile, destDir);
    }

    /**
     * Extracts the archive and decompiles all .class files found.
     */
    @Override
    public JarDecompilationResults decompileJar(File jarFile, File destDir, DecompilationConf conf_)
                throws DecompilationException
    {

        log.info("Decompiling .jar '" + jarFile.getPath() + "' to '" + destDir + "'...");

        // Verify input.
        if (jarFile == null)
            throw new DecompilationException("Param jarFile is null.");
        if (destDir == null)
            throw new DecompilationException("Param destDir is null.");
        if (!jarFile.exists())
            throw new DecompilationException(".jar file not found: " + jarFile.getPath());
        if (destDir.exists() && !destDir.isDirectory())
            throw new DecompilationException("Destination path is not a directory: " + destDir.getAbsolutePath());

        // Load the .jar
        final JarFile jar;
        try
        {
            jar = new JarFile(jarFile);
        }
        catch (IOException ex)
        {
            throw new DecompilationException("Can't load .jar: " + jarFile.getPath(), ex);
        }

        // Settings
        JadConf conf = this.retypeConf(conf_);

        int classesDecompiled = 0;
        // List<Throwable> exs = new LinkedList<>();
        JarDecompilationResults res = new JarDecompilationResults();

        // For each entry in the archive...
        final Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements())
        {
            final JarEntry entry = entries.nextElement();
            final String subPath = entry.getName();

            if (!subPath.endsWith(".class"))
                continue;

            // Extract
            File tmpClassFile;
            try
            {
                tmpClassFile = Files.createTempFile("Windup-Jad-", "-" + FilenameUtils.getName(subPath)).toFile();
                FileUtils.copyInputStreamToFile(jar.getInputStream(entry), tmpClassFile);
            }
            catch (IOException ex)
            {
                throw new DecompilationPathException("Error extracting .class: " + ex.getMessage(), subPath, ex);
            }

            // final String classFile = StringUtils.removeEnd(name, ".class");

            try
            {
                this.decompileClassFile(tmpClassFile, destDir, conf);
                res.addDecompiled(subPath);
            }
            catch (Throwable th)
            {
                String msg = "* Error during decompilation of " + jarFile.getPath() + "!" + subPath + ":\n    "
                            + th.getMessage();
                DecompilationPathException ex = new DecompilationPathException(msg, subPath, th);
                log.error(msg, ex);
                res.addFailed(ex);
            }
            // Throw a compound exception?
        }

        return res;
    }// decompileJar

    /**
     * Decompiles given .class file to a .java file at the given destination path, using jad decompiler which is
     * expected to be at system PATH.
     */
    private static void executeJad(File classFile, File destDir) throws DecompilationPathException
    {
        StringBuilder sb = new StringBuilder();
        try
        {
            // Build command array
            CommandLine cmdLine = new CommandLine(APP_NAME);
            cmdLine.addArgument("-d"); // -d <dir> - directory for output files
            cmdLine.addArgument("${outputLocation}");
            cmdLine.addArgument("-r"); // -r - restore package directory structure
            cmdLine.addArgument("-f"); // -f - generate fully qualified names (fullnames)
            cmdLine.addArgument("-o"); // -o - overwrite output files without confirmation
            cmdLine.addArgument("-s"); // -s <ext> - output file extension (default: .jad)
            cmdLine.addArgument("java");
            cmdLine.addArgument("${classLocation}");

            Map<String, Object> argMap = new HashMap();
            argMap.put("outputLocation", destDir);
            argMap.put("classLocation", classFile);
            cmdLine.setSubstitutionMap(argMap);

            log.debug("Command: " + StringUtils.join(cmdLine.toStrings(), ' '));

            DefaultExecutor executor = new DefaultExecutor();
            executor.setExitValue(0);
            executor.setWorkingDirectory(FileUtils.getUserDirectory());
            ExecuteWatchdog watchdog = new ExecuteWatchdog(DECOMPILATION_TIMEOUT_MS);
            executor.setWatchdog(watchdog);
            executor.setStreamHandler(new ToStringBuilderExecuteStreamHandler(sb));

            // Execute
            int exitValue = executor.execute(cmdLine, EnvironmentUtils.getProcEnvironment());
            if (!destDir.exists())
            {
                log.error("Didn't find expected decompiled source: " + destDir.getAbsolutePath()
                            + "\n    This likely means that the decompiler did not successfully decompile the class.");
                log.error("Decompiler exited with exit code: " + exitValue);
                String out = ("Jad output:\n" + sb.toString()).replace(".class...", ".class...\n");
                log.error(out);
            }
            else
            {
                log.trace("Decompiled to: {}", destDir.getAbsolutePath());
            }
        }
        catch (ExecuteException ex)
        {
            String out = ("Jad output:\n" + sb.toString()).replace(".class...", ".class...\n");
            String msg = "Error running the decompiler on: " + classFile.getAbsolutePath()
                        + "\n    " + ex.getMessage();
            log.error(out);
            log.error(msg);
            throw new DecompilationPathException(msg + "\n" + out, classFile.getPath(), ex);
        }
        catch (IOException ex)
        {
            String out = ("Jad output:\n" + sb.toString()).replace(".class...", ".class...\n");
            String msg = "Error running the decompiler on: " + classFile.getAbsolutePath()
                        + "\n    Validate that " + APP_NAME + " is on your PATH.\n    " + ex.getMessage();
            log.error(msg);
            throw new DecompilationPathException(msg, classFile.getPath(), ex);
        }
        catch (Throwable ex)
        {
            String out = "Jad output:\n" + sb.toString();
            String msg = "Error running the decompiler on: " + classFile.getAbsolutePath()
                        + "\n    " + ex.getMessage();
            log.error(out);
            log.error(msg);
            throw new DecompilationPathException(msg + "\n" + out, classFile.getPath(), ex);
        }
    }

    /**
     * Convenience - check the type and retypes the conf parameter. It could also validate.
     */
    @Override
    public JadConf retypeConf(DecompilationConf conf)
    {
        if (conf instanceof JadConf)
            return (JadConf) conf;
        throw new IllegalArgumentException(String.format("Configuration for % has to be %s, was %s",
                    JadRetroDecompiler.class.getSimpleName(),
                    JadConf.class.getSimpleName(), conf.getClass().getName()
                    ));
    }

}// class

/**
 * Helper class to write app's stdout and stderr to the provided StringBuilder.
 */
class ToStringBuilderExecuteStreamHandler implements ExecuteStreamHandler
{

    final StringBuilder sb;
    OutputStream os;

    public ToStringBuilderExecuteStreamHandler(StringBuilder sb)
    {
        this.sb = sb;
        this.os = new WriterOutputStream(new StringBuilderWriter(this.sb));
    }

    @Override
    public void setProcessInputStream(OutputStream os) throws IOException
    {
        // We will not write to the process'es input.
    }

    @Override
    public void setProcessErrorStream(InputStream is) throws IOException
    {
        IOUtils.copy(is, this.os);
    }

    @Override
    public void setProcessOutputStream(InputStream is) throws IOException
    {
        IOUtils.copy(is, this.os);
    }

    @Override
    public void start() throws IOException
    {
    }

    @Override
    public void stop() throws IOException
    {
        this.os.flush();
        this.os.close();
    }
}