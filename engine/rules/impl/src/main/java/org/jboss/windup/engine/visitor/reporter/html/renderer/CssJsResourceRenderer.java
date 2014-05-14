package org.jboss.windup.engine.visitor.reporter.html.renderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.windup.addon.config.RulePhase;
import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.graph.WindupContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CssJsResourceRenderer extends AbstractGraphVisitor {
    private static final Logger LOG = LoggerFactory.getLogger(CssJsResourceRenderer.class);
    
    @Inject
    private WindupContext context;
    
    @Override
    public RulePhase getPhase()
    {
        return RulePhase.REPORTING;
    }
    
    @Override
    public void run() {
        
        File runDirectory = context.getRunDirectory();
        File resourceDirectory = new File(runDirectory, "resources");
        
        try {
            String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
            if (path.endsWith(".jar") || path.endsWith(".jar!/")) {
                path = path.replace("file:", "jar:file:");
            }
            
            File fpath = new File(path);
            if(fpath.isDirectory()) {
                Path p = Paths.get(fpath.getAbsolutePath(), "reports/resources");
                recursePath(p, resourceDirectory);
            }
            else {
                FileSystem fs = FileSystems.newFileSystem(new URI(path), new HashMap<String, String>());
                Path p = fs.getPath("reports/resources");
                recursePath(p, resourceDirectory);
            }
        }
        catch(Exception e) {
            throw new RuntimeException("Exception reading resource.", e);
        }
    }

    public void recursePath(final Path path, final File resultPath) throws IOException {
        LOG.info("Path: "+path.toString());
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String relativePath = StringUtils.substringAfter(file.toString(), path.toString());
                File resultFile = new File(resultPath, relativePath);
                
                FileUtils.forceMkdir(resultFile.getParentFile());
                FileOutputStream fos = new FileOutputStream(resultFile);
                try {
                    Files.copy(file, fos);
                }
                finally {
                    IOUtils.closeQuietly(fos);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
    
}
