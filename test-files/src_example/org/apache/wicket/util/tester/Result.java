package org.apache.wicket.util.tester;

public class Result{
    private static final Result PASS;
    private final boolean failed;
    private final String message;
    private Result(final boolean failed){
        this(failed,"");
    }
    private Result(final boolean failed,final String message){
        super();
        this.failed=failed;
        this.message=message;
    }
    static Result fail(final String message){
        return new Result(true,message);
    }
    static Result pass(){
        return Result.PASS;
    }
    public boolean wasFailed(){
        return this.failed;
    }
    public String getMessage(){
        return this.message;
    }
    static{
        PASS=new Result(false);
    }
}
