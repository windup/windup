package org.jboss.windup.rules.apps.summit.demo.rules;

import static org.joox.JOOX.$;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.reporting.quickfix.QuickfixLocationDTO;
import org.jboss.windup.reporting.quickfix.QuickfixTransformation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Quickfix for adding <sync-on-commit-only>false</sync-on-commit-only> to jbosscmp-jdbc.xml.
 */
public class JBossEjbTransactionTransformationQuickfix implements QuickfixTransformation
{
	public static final String ID = JBossEjbTransactionTransformationQuickfix.class.getSimpleName();
	
    private static Logger LOG = Logger.getLogger(JBossEjbTransactionTransformationQuickfix.class.getName());
	
	@Override
	public String getTransformationID() 
	{
		return ID;
	}
	
	@Override
	public String transform(QuickfixLocationDTO locationDTO) 
	{
		try 
		{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(locationDTO.getFile());
			List<Element> entities = $(doc).find("entity").get();
			for (Element entity : entities) 
			{
				if (updateEntity(entity, doc)) 
				{
					break;
				}
			}
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(out);
			transformer.transform(source, result);
			
			return out.toString();
		} 
		catch (Exception e) 
		{
			LOG.severe(e.getMessage());
		}
		return null;
	}
	
	private boolean updateEntity(Element entity, Document doc) 
	{
		for (Element child : $(entity).child("ejb-name").get())	
		{
			String name = StringUtils.trimToNull(child.getTextContent());
			if (StringUtils.equals("Track", name)) 
			{
				createOrUpdateSyncElement(entity, doc);
				return true;
			}
		}
		return false;
	}
	
	private void createOrUpdateSyncElement(Element entity, Document doc) 
	{
		List<Element> syncElements = $(entity).children("sync-on-commit-only").get();
		if (syncElements.isEmpty())
		{
			Element syncElement = doc.createElement("sync-on-commit-only");
			syncElement.setTextContent("false");
			entity.appendChild(syncElement);
		}
		else {
			for (Element syncElement : syncElements)
			{
				String value = StringUtils.trimToNull(syncElement.getTextContent());
				if (Boolean.valueOf(value) == true)
				{
					syncElement.setTextContent("false");
					return;
				}
			}
		}
	}
}

