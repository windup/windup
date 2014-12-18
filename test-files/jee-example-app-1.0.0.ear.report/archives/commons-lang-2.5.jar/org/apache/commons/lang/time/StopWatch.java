package org.apache.commons.lang.time;

import org.apache.commons.lang.time.DurationFormatUtils;

public class StopWatch{
    private static final int STATE_UNSTARTED=0;
    private static final int STATE_RUNNING=1;
    private static final int STATE_STOPPED=2;
    private static final int STATE_SUSPENDED=3;
    private static final int STATE_UNSPLIT=10;
    private static final int STATE_SPLIT=11;
    private int runningState;
    private int splitState;
    private long startTime;
    private long stopTime;
    public StopWatch(){
        super();
        this.runningState=0;
        this.splitState=10;
        this.startTime=-1L;
        this.stopTime=-1L;
    }
    public void start(){
        if(this.runningState==2){
            throw new IllegalStateException("Stopwatch must be reset before being restarted. ");
        }
        if(this.runningState!=0){
            throw new IllegalStateException("Stopwatch already started. ");
        }
        this.stopTime=-1L;
        this.startTime=System.currentTimeMillis();
        this.runningState=1;
    }
    public void stop(){
        if(this.runningState!=1&&this.runningState!=3){
            throw new IllegalStateException("Stopwatch is not running. ");
        }
        if(this.runningState==1){
            this.stopTime=System.currentTimeMillis();
        }
        this.runningState=2;
    }
    public void reset(){
        this.runningState=0;
        this.splitState=10;
        this.startTime=-1L;
        this.stopTime=-1L;
    }
    public void split(){
        if(this.runningState!=1){
            throw new IllegalStateException("Stopwatch is not running. ");
        }
        this.stopTime=System.currentTimeMillis();
        this.splitState=11;
    }
    public void unsplit(){
        if(this.splitState!=11){
            throw new IllegalStateException("Stopwatch has not been split. ");
        }
        this.stopTime=-1L;
        this.splitState=10;
    }
    public void suspend(){
        if(this.runningState!=1){
            throw new IllegalStateException("Stopwatch must be running to suspend. ");
        }
        this.stopTime=System.currentTimeMillis();
        this.runningState=3;
    }
    public void resume(){
        if(this.runningState!=3){
            throw new IllegalStateException("Stopwatch must be suspended to resume. ");
        }
        this.startTime+=System.currentTimeMillis()-this.stopTime;
        this.stopTime=-1L;
        this.runningState=1;
    }
    public long getTime(){
        if(this.runningState==2||this.runningState==3){
            return this.stopTime-this.startTime;
        }
        if(this.runningState==0){
            return 0L;
        }
        if(this.runningState==1){
            return System.currentTimeMillis()-this.startTime;
        }
        throw new RuntimeException("Illegal running state has occured. ");
    }
    public long getSplitTime(){
        if(this.splitState!=11){
            throw new IllegalStateException("Stopwatch must be split to get the split time. ");
        }
        return this.stopTime-this.startTime;
    }
    public long getStartTime(){
        if(this.runningState==0){
            throw new IllegalStateException("Stopwatch has not been started");
        }
        return this.startTime;
    }
    public String toString(){
        return DurationFormatUtils.formatDurationHMS(this.getTime());
    }
    public String toSplitString(){
        return DurationFormatUtils.formatDurationHMS(this.getSplitTime());
    }
}
