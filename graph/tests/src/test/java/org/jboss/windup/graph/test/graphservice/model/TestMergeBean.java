package org.jboss.windup.graph.test.graphservice.model;


/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class TestMergeBean extends AbstractModelBean implements TestMergeModel
{
    private String prop1;
    private String prop2;
    private String prop3;


    public String getProp1() {
        return prop1;
    }


    public void setProp1( String prop1 ) {
        this.prop1 = prop1;
    }


    public String getProp2() {
        return prop2;
    }


    public TestMergeBean setProp2( String prop2 ) {
        this.prop2 = prop2;
        return this;
    }


    public String getProp3() {
        return prop3;
    }


    public TestMergeBean setProp3( String prop3 ) {
        this.prop3 = prop3;
        return this;
    }

}
