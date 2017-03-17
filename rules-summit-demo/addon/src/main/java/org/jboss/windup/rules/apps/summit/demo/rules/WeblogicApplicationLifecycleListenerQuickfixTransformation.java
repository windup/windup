package org.jboss.windup.rules.apps.summit.demo.rules;

import java.nio.charset.Charset;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
import org.jboss.windup.reporting.quickfix.QuickfixLocationDTO;
import org.jboss.windup.reporting.quickfix.QuickfixTransformation;

public class WeblogicApplicationLifecycleListenerQuickfixTransformation implements QuickfixTransformation
{
	public static final String ID = WeblogicApplicationLifecycleListenerQuickfixTransformation.class.getSimpleName();
	
    private static Logger LOG = Logger.getLogger(DiscoverWeblogicApplicationLifecycleListenerRuleProvider.class.getName());
	
	@Override
	public String getTransformationID() {
		return ID;
	}
	
	@Override
	public String transform(QuickfixLocationDTO locationDTO) {
		try 
		{
			StringBuilder builder = new StringBuilder();
			builder.append("<!--");
			builder.append(getLine(locationDTO));
			builder.append("-->");
			return builder.toString();
		}	
		catch (Exception e)
		{
			LOG.severe(e.getMessage());
		}
		return null;
	}
	
	private String getLine(QuickfixLocationDTO locationDTO) throws Exception
	{
		String contents = FileUtils.readFileToString(locationDTO.getFile(), Charset.defaultCharset());
 		Document doc = new Document(contents);
		IRegion info = doc.getLineInformation(locationDTO.getLine()-1);
		return doc.get(info.getOffset(), locationDTO.getLength());
	}
}
