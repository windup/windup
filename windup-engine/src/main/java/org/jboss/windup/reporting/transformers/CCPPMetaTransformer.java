package org.jboss.windup.reporting.transformers;

import java.io.File;

import org.jboss.windup.resource.type.CCPPMeta;

public class CCPPMetaTransformer extends GenericMetaTransformer<CCPPMeta> {

	@Override
	protected String buildSyntax() {
		return "cpp";
	}
	
	@Override
	protected String buildTitle(CCPPMeta meta, File rootDirectory) {
		return meta.getPathRelativeToArchive();
	}
	
	@Override
	protected String buildSummary(CCPPMeta meta) {
		//TODO
		return "";
	}
	
}
