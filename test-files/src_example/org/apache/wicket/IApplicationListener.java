package org.apache.wicket;

public interface IApplicationListener{
    void onAfterInitialized(Application p0);
    void onBeforeDestroyed(Application p0);
}
