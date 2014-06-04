package org.apache.wicket.util.tester;

import org.apache.wicket.protocol.http.*;
import javax.servlet.*;
import junit.framework.*;
import org.apache.wicket.*;
import java.io.*;
import java.util.*;
import org.apache.wicket.markup.html.panel.*;
import org.apache.wicket.feedback.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.model.*;
import org.apache.wicket.markup.html.basic.*;
import org.apache.wicket.markup.html.list.*;
import org.apache.wicket.markup.html.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.markup.html.link.*;
import org.apache.wicket.behavior.*;
import org.slf4j.*;

public class WicketTester extends BaseWicketTester{
    private static final Logger log;
    public WicketTester(){
        super();
    }
    public WicketTester(final Class<? extends Page> homePage){
        super(homePage);
    }
    public WicketTester(final WebApplication application){
        super(application);
    }
    public WicketTester(final WebApplication application,final String path){
        super(application,path);
    }
    public WicketTester(final WebApplication application,final ServletContext servletCtx){
        super(application,servletCtx);
    }
    public void assertAjaxLocation(){
        if(null!=this.getLastResponse().getHeader("Location")){
            Assert.fail("Location header should *not* be present when using Ajax");
        }
        final String ajaxLocation=this.getLastResponse().getHeader("Ajax-Location");
        if(null==ajaxLocation){
            throw new AssertionFailedError("Ajax-Location header should be present when using Ajax");
        }
        final int statusCode=this.getLastResponse().getStatus();
        if(statusCode!=200){
            throw new AssertionFailedError("Expected HTTP status code to be 200 (OK)");
        }
    }
    public void assertComponent(final String path,final Class<? extends Component> expectedComponentClass){
        this.assertResult(this.isComponent(path,expectedComponentClass));
    }
    public void assertComponentOnAjaxResponse(final Component component){
        final Result result=this.isComponentOnAjaxResponse(component);
        this.assertResult(result);
    }
    public void assertComponentOnAjaxResponse(final String componentPath){
        this.assertComponentOnAjaxResponse(this.getComponentFromLastRenderedPage(componentPath));
    }
    public void assertContains(final String pattern){
        this.assertResult(this.ifContains(pattern));
    }
    public void assertContainsNot(final String pattern){
        this.assertResult(this.ifContainsNot(pattern));
    }
    public void assertErrorMessages(final String... expectedErrorMessages){
        final List<Serializable> actualMessages=this.getMessages(400);
        final List<Serializable> msgs=(List<Serializable>)new ArrayList();
        for(final Serializable actualMessage : actualMessages){
            msgs.add(actualMessage.toString());
        }
        WicketTesterHelper.assertEquals((Collection<?>)Arrays.asList(expectedErrorMessages),(Collection<?>)msgs);
    }
    public void assertInfoMessages(final String... expectedInfoMessages){
        final List<Serializable> actualMessages=this.getMessages(200);
        WicketTesterHelper.assertEquals((Collection<?>)Arrays.asList(expectedInfoMessages),(Collection<?>)actualMessages);
    }
    public void assertFeedback(final String path,final String... messages){
        final FeedbackPanel fbp=(FeedbackPanel)this.getComponentFromLastRenderedPage(path);
        final IModel<List<FeedbackMessage>> model=fbp.getFeedbackMessagesModel();
        final List<FeedbackMessage> renderedMessages=model.getObject();
        if(renderedMessages==null){
            Assert.fail("feedback panel at path ["+path+"] returned null messages");
        }
        if(messages.length!=renderedMessages.size()){
            Assert.fail("you expected "+messages.length+" messages for the feedback panel ["+path+"], but there were actually "+renderedMessages.size());
        }
        for(int i=0;i<messages.length&&i<renderedMessages.size();++i){
            final String expected=messages[i];
            boolean found=false;
            for(final FeedbackMessage actual : renderedMessages){
                if(Objects.equal((Object)expected,(Object)actual.getMessage().toString())){
                    found=true;
                    break;
                }
            }
            if(!found){
                this.assertResult(Result.fail("Missing expected feedback message: "+expected));
            }
        }
    }
    public void assertInvisible(final String path){
        this.assertResult(this.isInvisible(path));
    }
    public void assertLabel(final String path,final String expectedLabelText){
        final Label label=(Label)this.getComponentFromLastRenderedPage(path);
        Assert.assertEquals(expectedLabelText,label.getDefaultModelObjectAsString());
    }
    public void assertModelValue(final String path,final Object expectedValue){
        final Component component=this.getComponentFromLastRenderedPage(path);
        Assert.assertEquals(expectedValue,component.getDefaultModelObject());
    }
    public void assertListView(final String path,final List<?> expectedList){
        final ListView<?> listView=(ListView<?>)this.getComponentFromLastRenderedPage(path);
        WicketTesterHelper.assertEquals((Collection<?>)expectedList,(Collection<?>)listView.getList());
    }
    public void assertNoErrorMessage(){
        final List<Serializable> messages=this.getMessages(400);
        Assert.assertTrue("expect no error message, but contains\n"+WicketTesterHelper.asLined((Collection<?>)messages),messages.isEmpty());
    }
    public void assertNoInfoMessage(){
        final List<Serializable> messages=this.getMessages(200);
        Assert.assertTrue("expect no info message, but contains\n"+WicketTesterHelper.asLined((Collection<?>)messages),messages.isEmpty());
    }
    public void assertRenderedPage(final Class<? extends Page> expectedRenderedPageClass){
        this.assertResult(this.isRenderedPage(expectedRenderedPageClass));
    }
    public void assertResultPage(final Class<?> clazz,final String filename) throws Exception{
        final String document=this.getLastResponseAsString();
        DiffUtil.validatePage(document,clazz,filename,true);
    }
    public void assertResultPage(final String expectedDocument) throws Exception{
        final String document=this.getLastResponseAsString();
        Assert.assertEquals(expectedDocument,document);
    }
    public void assertVisible(final String path){
        this.assertResult(this.isVisible(path));
    }
    public void assertEnabled(final String path){
        this.assertResult(this.isEnabled(path));
    }
    public void assertDisabled(final String path){
        this.assertResult(this.isDisabled(path));
    }
    public void assertRequired(final String path){
        this.assertResult(this.isRequired(path));
    }
    private void assertResult(final Result result){
        if(result.wasFailed()){
            throw new AssertionFailedError(result.getMessage());
        }
    }
    public void assertUsability(final Component component){
        this.checkUsability(component,true);
    }
    public void clickLink(final Component link){
        this.clickLink(link.getPageRelativePath());
    }
    public void assertBookmarkablePageLink(final String id,final Class<? extends WebPage> pageClass,final PageParameters parameters){
        BookmarkablePageLink<?> pageLink;
        try{
            pageLink=(BookmarkablePageLink<?>)this.getComponentFromLastRenderedPage(id);
        }
        catch(ClassCastException e){
            throw new IllegalArgumentException("Component with id:"+id+" is not a BookmarkablePageLink");
        }
        Assert.assertEquals("BookmarkablePageLink: "+id+" is pointing to the wrong page",pageClass,pageLink.getPageClass());
        Assert.assertEquals("One or more of the parameters associated with the BookmarkablePageLink: "+id+" do not match",parameters,pageLink.getPageParameters());
    }
    public <T extends Page> void executeTest(final Class<?> testClass,final Class<T> pageClass,final String filename) throws Exception{
        WicketTester.log.info("=== "+pageClass.getName()+" ===");
        this.startPage(pageClass);
        this.assertRenderedPage(pageClass);
        this.assertResultPage(testClass,filename);
    }
    public void executeTest(final Class<?> testClass,final Page page,final String filename) throws Exception{
        WicketTester.log.info("=== "+page.getClass().getName()+" ===");
        this.startPage(page);
        this.assertRenderedPage((Class<? extends Page>)page.getClass());
        this.assertResultPage(testClass,filename);
    }
    public void executeTest(final Class<?> testClass,final Component component,final String filename) throws Exception{
        WicketTester.log.info("=== "+component.getClass().getName()+" ===");
        this.startComponent(component);
        this.assertResultPage(testClass,filename);
    }
    public <T extends Page> void executeTest(final Class<?> testClass,final Class<T> pageClass,final PageParameters parameters,final String filename) throws Exception{
        WicketTester.log.info("=== "+pageClass.getName()+" ===");
        this.startPage(pageClass,parameters);
        this.assertRenderedPage(pageClass);
        this.assertResultPage(testClass,filename);
    }
    public void executeListener(final Class<?> testClass,final Component component,final String filename) throws Exception{
        Assert.assertNotNull(component);
        WicketTester.log.info("=== "+testClass.getName()+" : "+component.getPageRelativePath()+" ===");
        this.executeListener(component);
        this.assertResultPage(testClass,filename);
    }
    public void executeBehavior(final Class<?> testClass,final AbstractAjaxBehavior behavior,final String filename) throws Exception{
        Assert.assertNotNull(behavior);
        WicketTester.log.info("=== "+testClass.getName()+" : "+behavior.toString()+" ===");
        this.executeBehavior(behavior);
        this.assertResultPage(testClass,filename);
    }
    public void assertRedirectUrl(final String expectedRedirectUrl){
        final String actualRedirectUrl=this.getLastResponse().getRedirectLocation();
        Assert.assertEquals(expectedRedirectUrl,actualRedirectUrl);
    }
    public static String getBasedir(){
        String basedir=System.getProperty("basedir");
        if(basedir!=null){
            basedir+="/";
        }
        else{
            basedir="";
        }
        return basedir;
    }
    static{
        log=LoggerFactory.getLogger(WicketTester.class);
    }
}
