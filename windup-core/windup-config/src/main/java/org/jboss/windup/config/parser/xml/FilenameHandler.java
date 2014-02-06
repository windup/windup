package org.jboss.windup.config.parser.xml;

import static org.joox.JOOX.$;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.condition.FilenameCondition;
import org.jboss.windup.config.parser.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.HandlerManager;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName="filename", namespace="http://windup.jboss.org/v1/xml")
public class FilenameHandler implements ElementHandler<FilenameCondition<?>> {

	@Override
	public FilenameCondition<?> processElement(HandlerManager handlerManager, Element element) throws ConfigurationException {
		FilenameCondition<?> condition = new FilenameCondition();
		String regex = $(element).attr("matches");
		if(StringUtils.isBlank(regex)) {
			throw new ConfigurationException("filename tag requires 'matches' attribute.");
		}
		Pattern regexPattern = Pattern.compile(regex);
		condition.setPattern(regexPattern);
		return condition;
	}

}
