package org.jboss.windup.reporting.model;


import org.jboss.windup.graph.model.report.IgnoredFileRegexModel;
import org.jboss.windup.graph.model.resource.ResourceModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Report model containing all the information needed for the report listing all the ignored files during the windup process with all the ignore regexes they were matched against.
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 *
 */
@TypeValue(IgnoredFilesReportModel.TYPE)
public interface IgnoredFilesReportModel extends ApplicationReportModel
{
    public static final String TYPE = "IgnoredFilesReport";
    public static final String FILE_REGEXES = "fileRegexes";
    public static final String IGNORED_FILES = "ignoredFiles";
    
    /**
     * All the regexes used to ignore the files.
     */
    @Adjacency(label = FILE_REGEXES, direction = Direction.OUT)
    public Iterable<IgnoredFileRegexModel> getFileRegexes();

    /**
     * Add regex used to ignore the file.
     */
    @Adjacency(label = FILE_REGEXES, direction = Direction.OUT)
    public void addFileRegex(IgnoredFileRegexModel fileRegex);
    
    /**
     * Get the files that were ignored.
     */
    @Adjacency(label = IGNORED_FILES, direction = Direction.OUT)
    public Iterable<ResourceModel> getIgnoredFiles();

    /**
     * Add file that was ignored.
     */
    @Adjacency(label = IGNORED_FILES, direction = Direction.OUT)
    public void addIgnoredFile(ResourceModel fileModel);
    
    
    
}
