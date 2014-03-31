package org.jboss.windup.config.spring.namespace.simple;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.jboss.windup.hint.HintProcessor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class HintBeanParser extends AbstractBeanDefinitionParser {

	@Override
	protected AbstractBeanDefinition parseInternal(Element element,
			ParserContext parserContext) {

		BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.rootBeanDefinition(HintProcessor.class);

		if(element.hasAttribute("hint")) {
			beanBuilder.addPropertyValue("hint", element.getAttribute("hint"));
		}
		else {
			String markdown = element.getTextContent();
			
			String lines[] = markdown.split("\\r?\\n");
			StringBuilder markdownRebuilder = new StringBuilder();
			
			for(String line : lines) {
				line = StringUtils.trim(line);
				if(line != null) {
					markdownRebuilder.append(line).append(SystemUtils.LINE_SEPARATOR);
				}
			}
			beanBuilder.addPropertyValue("hint", markdownRebuilder.toString());
		}
		return beanBuilder.getBeanDefinition();
	
	}
	

}
