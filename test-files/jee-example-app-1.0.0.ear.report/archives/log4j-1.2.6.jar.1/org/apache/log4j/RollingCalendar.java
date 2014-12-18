package org.apache.log4j;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.GregorianCalendar;

class RollingCalendar extends GregorianCalendar{
    int type;
    RollingCalendar(){
        super();
        this.type=-1;
    }
    RollingCalendar(final TimeZone tz,final Locale locale){
        super(tz,locale);
        this.type=-1;
    }
    void setType(final int type){
        this.type=type;
    }
    public long getNextCheckMillis(final Date now){
        return this.getNextCheckDate(now).getTime();
    }
    public Date getNextCheckDate(final Date now){
        this.setTime(now);
        switch(this.type){
            case 0:{
                this.set(13,0);
                this.set(14,0);
                this.add(12,1);
                break;
            }
            case 1:{
                this.set(12,0);
                this.set(13,0);
                this.set(14,0);
                this.add(11,1);
                break;
            }
            case 2:{
                this.set(12,0);
                this.set(13,0);
                this.set(14,0);
                final int hour=this.get(11);
                if(hour<12){
                    this.set(11,12);
                    break;
                }
                this.set(11,0);
                this.add(5,1);
                break;
            }
            case 3:{
                this.set(11,0);
                this.set(12,0);
                this.set(13,0);
                this.set(14,0);
                this.add(5,1);
                break;
            }
            case 4:{
                this.set(7,this.getFirstDayOfWeek());
                this.set(11,0);
                this.set(13,0);
                this.set(14,0);
                this.add(3,1);
                break;
            }
            case 5:{
                this.set(5,1);
                this.set(11,0);
                this.set(13,0);
                this.set(14,0);
                this.add(2,1);
                break;
            }
            default:{
                throw new IllegalStateException("Unknown periodicity type.");
            }
        }
        return this.getTime();
    }
}
