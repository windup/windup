package org.apache.wicket.util.tester;

import org.apache.wicket.markup.html.*;
import org.apache.wicket.markup.html.link.*;
import org.apache.wicket.*;

public class DummyHomePage extends WebPage{
    private static final long serialVersionUID=1L;
    private transient ITestPageSource testPageSource;
    private final Link<?> testPageLink;
    public DummyHomePage(){
        super();
        this.testPageLink=new TestLink("testPage");
        this.add(this.testPageLink);
    }
    public void setTestPageSource(final ITestPageSource testPageSource){
        this.testPageSource=testPageSource;
    }
    public Link<?> getTestPageLink(){
        return this.testPageLink;
    }
    public class TestLink extends Link<Void>{
        private static final long serialVersionUID=1L;
        public TestLink(final String id){
            super(id);
        }
        public void onClick(){
            this.setResponsePage(DummyHomePage.this.testPageSource.getTestPage());
        }
    }
}
