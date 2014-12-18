package org.apache.commons.lang.builder;

import org.apache.commons.lang.builder.ToStringStyle;

public class StandardToStringStyle extends ToStringStyle{
    private static final long serialVersionUID=1L;
    public boolean isUseClassName(){
        return super.isUseClassName();
    }
    public void setUseClassName(final boolean useClassName){
        super.setUseClassName(useClassName);
    }
    public boolean isUseShortClassName(){
        return super.isUseShortClassName();
    }
    public boolean isShortClassName(){
        return super.isUseShortClassName();
    }
    public void setUseShortClassName(final boolean useShortClassName){
        super.setUseShortClassName(useShortClassName);
    }
    public void setShortClassName(final boolean shortClassName){
        super.setUseShortClassName(shortClassName);
    }
    public boolean isUseIdentityHashCode(){
        return super.isUseIdentityHashCode();
    }
    public void setUseIdentityHashCode(final boolean useIdentityHashCode){
        super.setUseIdentityHashCode(useIdentityHashCode);
    }
    public boolean isUseFieldNames(){
        return super.isUseFieldNames();
    }
    public void setUseFieldNames(final boolean useFieldNames){
        super.setUseFieldNames(useFieldNames);
    }
    public boolean isDefaultFullDetail(){
        return super.isDefaultFullDetail();
    }
    public void setDefaultFullDetail(final boolean defaultFullDetail){
        super.setDefaultFullDetail(defaultFullDetail);
    }
    public boolean isArrayContentDetail(){
        return super.isArrayContentDetail();
    }
    public void setArrayContentDetail(final boolean arrayContentDetail){
        super.setArrayContentDetail(arrayContentDetail);
    }
    public String getArrayStart(){
        return super.getArrayStart();
    }
    public void setArrayStart(final String arrayStart){
        super.setArrayStart(arrayStart);
    }
    public String getArrayEnd(){
        return super.getArrayEnd();
    }
    public void setArrayEnd(final String arrayEnd){
        super.setArrayEnd(arrayEnd);
    }
    public String getArraySeparator(){
        return super.getArraySeparator();
    }
    public void setArraySeparator(final String arraySeparator){
        super.setArraySeparator(arraySeparator);
    }
    public String getContentStart(){
        return super.getContentStart();
    }
    public void setContentStart(final String contentStart){
        super.setContentStart(contentStart);
    }
    public String getContentEnd(){
        return super.getContentEnd();
    }
    public void setContentEnd(final String contentEnd){
        super.setContentEnd(contentEnd);
    }
    public String getFieldNameValueSeparator(){
        return super.getFieldNameValueSeparator();
    }
    public void setFieldNameValueSeparator(final String fieldNameValueSeparator){
        super.setFieldNameValueSeparator(fieldNameValueSeparator);
    }
    public String getFieldSeparator(){
        return super.getFieldSeparator();
    }
    public void setFieldSeparator(final String fieldSeparator){
        super.setFieldSeparator(fieldSeparator);
    }
    public boolean isFieldSeparatorAtStart(){
        return super.isFieldSeparatorAtStart();
    }
    public void setFieldSeparatorAtStart(final boolean fieldSeparatorAtStart){
        super.setFieldSeparatorAtStart(fieldSeparatorAtStart);
    }
    public boolean isFieldSeparatorAtEnd(){
        return super.isFieldSeparatorAtEnd();
    }
    public void setFieldSeparatorAtEnd(final boolean fieldSeparatorAtEnd){
        super.setFieldSeparatorAtEnd(fieldSeparatorAtEnd);
    }
    public String getNullText(){
        return super.getNullText();
    }
    public void setNullText(final String nullText){
        super.setNullText(nullText);
    }
    public String getSizeStartText(){
        return super.getSizeStartText();
    }
    public void setSizeStartText(final String sizeStartText){
        super.setSizeStartText(sizeStartText);
    }
    public String getSizeEndText(){
        return super.getSizeEndText();
    }
    public void setSizeEndText(final String sizeEndText){
        super.setSizeEndText(sizeEndText);
    }
    public String getSummaryObjectStartText(){
        return super.getSummaryObjectStartText();
    }
    public void setSummaryObjectStartText(final String summaryObjectStartText){
        super.setSummaryObjectStartText(summaryObjectStartText);
    }
    public String getSummaryObjectEndText(){
        return super.getSummaryObjectEndText();
    }
    public void setSummaryObjectEndText(final String summaryObjectEndText){
        super.setSummaryObjectEndText(summaryObjectEndText);
    }
}
