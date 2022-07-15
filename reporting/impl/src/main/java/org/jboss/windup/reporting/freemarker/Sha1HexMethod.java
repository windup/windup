package org.jboss.windup.reporting.freemarker;

import java.util.List;

import freemarker.ext.beans.StringModel;
import org.apache.commons.codec.digest.DigestUtils;
import org.jboss.windup.config.GraphRewrite;

import freemarker.template.TemplateModelException;
import org.jboss.windup.graph.model.resource.FileModel;

/**
 * Returns the SHA1 Hex hash for the provided {@link FileModel}.<br/>
 * <p>
 * sha1Hex(FileModel):String
 */
public class Sha1HexMethod implements WindupFreeMarkerMethod {
    @Override
    public String exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        if (arguments.size() != 1) {
            throw new TemplateModelException(
                    "Error, method expects one argument (FileModel)");
        }
        StringModel stringModel = (StringModel) arguments.get(0);
        FileModel fileModel = (FileModel) stringModel.getWrappedObject();
        return !fileModel.isDirectory() ? fileModel.getSHA1Hash() : DigestUtils.sha1Hex(fileModel.getFileName());
    }

    @Override
    public String getMethodName() {
        return "sha1Hex";
    }

    @Override
    public String getDescription() {
        return "Returns the SHA1 Hex hash for the provided " + FileModel.class.getSimpleName() + ".";
    }

    @Override
    public void setContext(GraphRewrite event) {

    }
}
