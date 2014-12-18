package org.apache.log4j.lf5.viewer;

import java.awt.Adjustable;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

public class TrackingAdjustmentListener implements AdjustmentListener{
    protected int _lastMaximum;
    public TrackingAdjustmentListener(){
        super();
        this._lastMaximum=-1;
    }
    public void adjustmentValueChanged(final AdjustmentEvent e){
        final Adjustable bar=e.getAdjustable();
        final int currentMaximum=bar.getMaximum();
        if(bar.getMaximum()==this._lastMaximum){
            return;
        }
        final int bottom=bar.getValue()+bar.getVisibleAmount();
        if(bottom+bar.getUnitIncrement()>=this._lastMaximum){
            bar.setValue(bar.getMaximum());
        }
        this._lastMaximum=currentMaximum;
    }
}
