package org.jboss.windup.rules.apps.java.scan.operation;

import java.io.File;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.context.EvaluationContext;


/**
 * This Operation deletes the directory to which the archives in payload (FileModel) were unzipped.
 *
 * @author Ondrej Zizka
 * @see org.jboss.windup.rules.apps.java.scan.operation.UnzipArchiveToOutputFolder
 */
public class DeleteWorkDirsOperation extends AbstractIterationOperation<ArchiveModel> {
    private static final Logger LOG = Logging.get(DeleteWorkDirsOperation.class);


    public DeleteWorkDirsOperation() {
        super();
    }

    public static DeleteWorkDirsOperation delete() {
        return new DeleteWorkDirsOperation();
    }


    @Override
    public void perform(GraphRewrite event, EvaluationContext context, ArchiveModel archive) {
        LOG.info("Deleting archive files: " + archive.getArchiveName());
        FileUtils.deleteQuietly(new File(archive.getUnzippedDirectory()));
    }


    @Override
    public String toString() {
        return DeleteWorkDirsOperation.class.getSimpleName();
    }
}
