package org.apache.log4j.lf5.viewer.categoryexplorer;

import org.apache.log4j.lf5.viewer.categoryexplorer.CategoryElement;
import java.util.StringTokenizer;
import java.util.LinkedList;

public class CategoryPath{
    protected LinkedList _categoryElements;
    public CategoryPath(){
        super();
        this._categoryElements=new LinkedList();
    }
    public CategoryPath(final String category){
        super();
        this._categoryElements=new LinkedList();
        String processedCategory=category;
        if(processedCategory==null){
            processedCategory="Debug";
        }
        processedCategory.replace('/','.');
        processedCategory=processedCategory.replace('\\','.');
        final StringTokenizer st=new StringTokenizer(processedCategory,".");
        while(st.hasMoreTokens()){
            final String element=st.nextToken();
            this.addCategoryElement(new CategoryElement(element));
        }
    }
    public int size(){
        final int count=this._categoryElements.size();
        return count;
    }
    public boolean isEmpty(){
        boolean empty=false;
        if(this._categoryElements.size()==0){
            empty=true;
        }
        return empty;
    }
    public void removeAllCategoryElements(){
        this._categoryElements.clear();
    }
    public void addCategoryElement(final CategoryElement categoryElement){
        this._categoryElements.addLast(categoryElement);
    }
    public CategoryElement categoryElementAt(final int index){
        return this._categoryElements.get(index);
    }
    public String toString(){
        final StringBuffer out=new StringBuffer(100);
        out.append("\n");
        out.append("===========================\n");
        out.append("CategoryPath:                   \n");
        out.append("---------------------------\n");
        out.append("\nCategoryPath:\n\t");
        if(this.size()>0){
            for(int i=0;i<this.size();++i){
                out.append(this.categoryElementAt(i).toString());
                out.append("\n\t");
            }
        }
        else{
            out.append("<<NONE>>");
        }
        out.append("\n");
        out.append("===========================\n");
        return out.toString();
    }
}
