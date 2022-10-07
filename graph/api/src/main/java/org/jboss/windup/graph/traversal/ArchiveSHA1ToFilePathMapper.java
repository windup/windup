package org.jboss.windup.graph.traversal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.resource.FileModel;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class ArchiveSHA1ToFilePathMapper {
    private final Map<String, List<String>> sha1ToFilenames = new HashMap<>();

    public ArchiveSHA1ToFilePathMapper(final ProjectModelTraversal traversal) {
        ProjectTraversalVisitor visitor = new ProjectTraversalVisitor() {
            @Override
            public void visit(ProjectModelTraversal traversal) {
                FileModel rootFile = traversal.getCurrent().getRootFileModel();
                if (!(rootFile instanceof ArchiveModel))
                    return;

                ArchiveModel archive = (ArchiveModel) rootFile;
                String filePath = traversal.getFilePath(archive);
                addToMap(archive.getSHA1Hash(), filePath);
            }
        };
        traversal.accept(visitor);
    }

    public List<String> getPathsBySHA1(String sha1) {
        List<String> result = sha1ToFilenames.get(sha1);
        if (result == null)
            result = Collections.emptyList();
        return result;
    }

    private void addToMap(String sha1, String filePath) {
        List<String> filepaths = sha1ToFilenames.get(sha1);
        if (filepaths == null) {
            filepaths = new ArrayList<>();
            sha1ToFilenames.put(sha1, filepaths);
        }
        filepaths.add(filePath);
    }
}
