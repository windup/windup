package org.jboss.windup.reporting.quickfix;

import java.io.File;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IRegion;

public class QuickfixUtil 
{
	public static Document replace(File file, int lineNumber, String searchString, String replacement) throws Exception 
	{
		String contents = FileUtils.readFileToString(file, Charset.defaultCharset());
		Document document = new Document(contents);
		IRegion info = document.getLineInformation(lineNumber);
		FindReplaceDocumentAdapter adapter = new FindReplaceDocumentAdapter(document);
		IRegion search = adapter.find(info.getOffset(), searchString, true, true, true, false);
		if (search != null) {
			document.replace(search.getOffset(), search.getLength(), replacement);
		}
		return document;
	}
}
