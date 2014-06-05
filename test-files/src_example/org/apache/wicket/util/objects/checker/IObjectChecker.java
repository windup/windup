package org.apache.wicket.util.objects.checker;

import java.util.*;
import org.apache.wicket.util.lang.*;

public interface IObjectChecker{
    Result check(Object p0);
    List<Class<?>> getExclusions();
    public static class Result{
        public static final Result SUCCESS;
        public final Status status;
        public final String reason;
        public final Throwable cause;
        public Result(Status status,String reason){
            this(status,reason,null);
        }
        public Result(Status status,String reason,Throwable cause){
            super();
            if(status==Status.FAILURE){
                Args.notEmpty((CharSequence)reason,"reason");
            }
            this.status=status;
            this.reason=reason;
            this.cause=cause;
        }
        public String toString(){
            return "Result{reason='"+this.reason+'\''+", status="+this.status+'}';
        }
        static{
            SUCCESS=new Result(Status.SUCCESS,"");
        }
        public enum Status{
            SUCCESS,FAILURE;
        }
    }
}
