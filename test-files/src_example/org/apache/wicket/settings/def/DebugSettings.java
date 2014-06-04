package org.apache.wicket.settings.def;

import org.apache.wicket.settings.*;

public class DebugSettings implements IDebugSettings{
    private boolean ajaxDebugModeEnabled;
    private boolean componentUseCheck;
    private boolean linePreciseReportingOnAddComponentEnabled;
    private boolean linePreciseReportingOnNewComponentEnabled;
    private boolean outputMarkupContainerClassName;
    private boolean outputComponentPath;
    private boolean developmentUtilitiesEnabled;
    public DebugSettings(){
        super();
        this.ajaxDebugModeEnabled=false;
        this.componentUseCheck=true;
        this.linePreciseReportingOnAddComponentEnabled=false;
        this.linePreciseReportingOnNewComponentEnabled=false;
        this.outputMarkupContainerClassName=false;
        this.outputComponentPath=false;
        this.developmentUtilitiesEnabled=false;
    }
    public boolean getComponentUseCheck(){
        return this.componentUseCheck;
    }
    public boolean isAjaxDebugModeEnabled(){
        return this.ajaxDebugModeEnabled;
    }
    public boolean isLinePreciseReportingOnAddComponentEnabled(){
        return this.linePreciseReportingOnAddComponentEnabled;
    }
    public boolean isLinePreciseReportingOnNewComponentEnabled(){
        return this.linePreciseReportingOnNewComponentEnabled;
    }
    public boolean isOutputMarkupContainerClassName(){
        return this.outputMarkupContainerClassName;
    }
    public void setAjaxDebugModeEnabled(final boolean enable){
        this.ajaxDebugModeEnabled=enable;
    }
    public void setComponentUseCheck(final boolean componentUseCheck){
        this.componentUseCheck=componentUseCheck;
    }
    public void setLinePreciseReportingOnAddComponentEnabled(final boolean enable){
        this.linePreciseReportingOnAddComponentEnabled=enable;
    }
    public void setLinePreciseReportingOnNewComponentEnabled(final boolean enable){
        this.linePreciseReportingOnNewComponentEnabled=enable;
    }
    public void setOutputMarkupContainerClassName(final boolean enable){
        this.outputMarkupContainerClassName=enable;
    }
    public boolean isOutputComponentPath(){
        return this.outputComponentPath;
    }
    public void setOutputComponentPath(final boolean outputComponentPath){
        this.outputComponentPath=outputComponentPath;
    }
    public void setDevelopmentUtilitiesEnabled(final boolean enable){
        this.developmentUtilitiesEnabled=enable;
    }
    public boolean isDevelopmentUtilitiesEnabled(){
        return this.developmentUtilitiesEnabled;
    }
}
