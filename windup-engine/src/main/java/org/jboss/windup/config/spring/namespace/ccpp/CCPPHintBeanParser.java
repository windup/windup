package org.jboss.windup.config.spring.namespace.ccpp;

import org.jboss.windup.decorator.ccpp.shared.CCPPPatternHintProcessor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

//TODO Comments, Copyright
public class CCPPHintBeanParser extends AbstractBeanDefinitionParser {
	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {

		BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.rootBeanDefinition(CCPPPatternHintProcessor.class);
		beanBuilder.addPropertyValue("regexPattern", element.getAttribute("regex"));
		beanBuilder.addPropertyValue("hint", element.getAttribute("hint"));
		if (element.hasAttribute("source-type")) {
			beanBuilder.addPropertyValue("sourceType", element.getAttribute("source-type"));
		}
		if(element.hasAttribute("language")) {
			beanBuilder.addPropertyValue("language", element.getAttribute("language"));
		}
		if (element.hasAttribute("effort")) {
			beanBuilder.addPropertyValue("effort", Integer.parseInt(element.getAttribute("effort")));
		}

		return beanBuilder.getBeanDefinition();

	}
}
