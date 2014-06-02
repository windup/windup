package org.apache.wicket.settings;

public interface IDebugSettings{
    void setOutputComponentPath(boolean p0);
    boolean isOutputComponentPath();
    boolean getComponentUseCheck();
    void setComponentUseCheck(boolean p0);
    void setAjaxDebugModeEnabled(boolean p0);
    boolean isAjaxDebugModeEnabled();
    void setOutputMarkupContainerClassName(boolean p0);
    boolean isOutputMarkupContainerClassName();
    boolean isLinePreciseReportingOnAddComponentEnabled();
    void setLinePreciseReportingOnAddComponentEnabled(boolean p0);
    boolean isLinePreciseReportingOnNewComponentEnabled();
    void setLinePreciseReportingOnNewComponentEnabled(boolean p0);
    void setDevelopmentUtilitiesEnabled(boolean p0);
    boolean isDevelopmentUtilitiesEnabled();
}
