package org.jboss.windup.tooling.quickfix;

import org.jboss.windup.reporting.quickfix.QuickfixTransformation;
import org.jboss.windup.reporting.quickfix.QuickfixTransformationRegistry;
import org.jboss.windup.util.exception.WindupException;

import javax.inject.Inject;
import java.rmi.RemoteException;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class QuickfixServiceImpl implements QuickfixService {
    @Inject
    private QuickfixTransformationRegistry transformationRegistry;

    @Override
    public String transform(String transformationID, QuickfixLocationDTO locationDTO) throws RemoteException {
        QuickfixTransformation transformation = this.transformationRegistry.getByID(transformationID);
        if (transformation == null)
            throw new WindupException("Unrecognized quickfix type: " + transformationID);
        org.jboss.windup.reporting.quickfix.QuickfixLocationDTO reportingLocationDTO = new org.jboss.windup.reporting.quickfix.QuickfixLocationDTO();
        reportingLocationDTO.setReportDirectory(locationDTO.getReportDirectory());
        reportingLocationDTO.setLine(locationDTO.getLine());
        reportingLocationDTO.setColumn(locationDTO.getColumn());
        reportingLocationDTO.setLength(locationDTO.getLength());
        reportingLocationDTO.setFile(locationDTO.getFile());
        return transformation.transform(reportingLocationDTO);
    }
}
