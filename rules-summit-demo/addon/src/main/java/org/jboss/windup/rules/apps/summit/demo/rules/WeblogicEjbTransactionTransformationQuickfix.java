package org.jboss.windup.rules.apps.summit.demo.rules;

import java.nio.charset.Charset;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
import org.jboss.windup.reporting.quickfix.QuickfixLocationDTO;
import org.jboss.windup.reporting.quickfix.QuickfixTransformation;

/**
 * Quickfix for removing <delay-updates-until-end-of-tx> from weblogic-ejb-jar.xml.
 */
public class WeblogicEjbTransactionTransformationQuickfix implements QuickfixTransformation
{
	public static final String ID = WeblogicEjbTransactionTransformationQuickfix.class.getSimpleName();
	
    private static Logger LOG = Logger.getLogger(WeblogicEjbTransactionTransformationQuickfix.class.getName());
	
	@Override
	public String getTransformationID() {
		return ID;
	}
	
	@Override
	public String transform(QuickfixLocationDTO locationDTO) {
		try 
		{
			String contents = FileUtils.readFileToString(locationDTO.getFile(), Charset.defaultCharset());
	 		Document doc = new Document(contents);
	 		
	 		String original = getLine(doc, locationDTO);
	 		
			StringBuilder builder = new StringBuilder();
			builder.append("<!--");
			builder.append(original);
			builder.append("-->");
			
			String replacement = builder.toString();
			
			WeblogicEjbTransactionTransformationQuickfix.replace(doc, locationDTO.getLine()-1, replacement);
			
			return doc.get();
		}	
		catch (Exception e)
		{
			LOG.severe(e.getMessage());
		}
		return null;
	}
	
	private String getLine(Document doc, QuickfixLocationDTO locationDTO) throws Exception
	{
		IRegion info = doc.getLineInformation(locationDTO.getLine()-1);
		return doc.get(info.getOffset(), locationDTO.getLength());
	}
	
	/**
	 * Replaces the chunk of text represented as <code>searchString</code> with the specified <code>replacement</code>
	 * text at the given line number in the specified resource. 
	 */
	private static void replace(Document document, int lineNumber, String replacement) {
		try {
			IRegion info = document.getLineInformation(lineNumber);
			document.replace(info.getOffset(), info.getLength(), replacement);
		} catch (BadLocationException e) {
			LOG.severe(e.getMessage());
		}
	}
}