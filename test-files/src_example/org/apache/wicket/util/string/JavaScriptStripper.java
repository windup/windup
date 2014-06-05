package org.apache.wicket.util.string;

public class JavaScriptStripper{
    private static final int REGULAR_TEXT=1;
    private static final int STRING_SINGLE_QUOTE=2;
    private static final int STRING_DOUBLE_QUOTES=3;
    private static final int WHITE_SPACE=4;
    private static final int LINE_COMMENT=5;
    private static final int MULTILINE_COMMENT=6;
    private static final int REG_EXP=7;
    private int getPrevCount(final String s,int fromIndex,final char c){
        int count=0;
        --fromIndex;
        while(fromIndex>=0&&s.charAt(fromIndex--)==c){
            ++count;
        }
        return count;
    }
    public String stripCommentsAndWhitespace(final String original){
        final AppendingStringBuffer result=new AppendingStringBuffer(original.length()/2);
        int state=1;
        boolean wasNewLineInWhitespace=false;
        for(int i=0;i<original.length();++i){
            char c=original.charAt(i);
            final char next=(i<original.length()-1)?original.charAt(i+1):'\0';
            final char prev=(i>0)?original.charAt(i-1):'\0';
            if(state==4){
                if(c=='\n'&&!wasNewLineInWhitespace){
                    result.append("\n");
                    wasNewLineInWhitespace=true;
                }
                if(!Character.isWhitespace(next)){
                    state=1;
                }
            }
            else if(state==1){
                if(c=='/'&&next=='/'&&prev!='\\'){
                    state=5;
                }
                else if(c=='/'&&next=='*'){
                    state=6;
                    ++i;
                }
                else{
                    if(c=='/'){
                        int idx=result.length()-1;
                        while(idx>0){
                            final char tmp=result.charAt(idx);
                            if(Character.isWhitespace(tmp)){
                                --idx;
                            }
                            else{
                                if(tmp=='='||tmp=='('||tmp=='{'||tmp==':'||tmp==','||tmp=='['||tmp==';'||tmp=='!'){
                                    state=7;
                                    break;
                                }
                                break;
                            }
                        }
                    }
                    else if(Character.isWhitespace(c)&&Character.isWhitespace(next)){
                        if(c=='\n'||next=='\n'){
                            c='\n';
                            wasNewLineInWhitespace=true;
                        }
                        else{
                            c=' ';
                            wasNewLineInWhitespace=false;
                        }
                        state=4;
                    }
                    else if(c=='\''){
                        state=2;
                    }
                    else if(c=='\"'){
                        state=3;
                    }
                    result.append(c);
                }
            }
            else if(state==5&&(c=='\n'||c=='\r')){
                state=1;
                result.append(c);
            }
            else if(state==6&&c=='*'&&next=='/'){
                state=1;
                ++i;
            }
            else if(state==2){
                final int count=this.getPrevCount(original,i,'\\');
                if(c=='\''&&count%2==0){
                    state=1;
                }
                result.append(c);
            }
            else if(state==3){
                final int count=this.getPrevCount(original,i,'\\');
                if(c=='\"'&&count%2==0){
                    state=1;
                }
                result.append(c);
            }
            else if(state==7){
                final int count=this.getPrevCount(original,i,'\\');
                if(c=='/'&&count%2==0){
                    state=1;
                }
                result.append(c);
            }
        }
        return result.toString();
    }
}
