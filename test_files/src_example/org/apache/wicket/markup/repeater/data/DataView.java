package org.apache.wicket.markup.repeater.data;

public abstract class DataView<T> extends DataViewBase<T>{
    private static final long serialVersionUID=1L;
    protected DataView(final String id,final IDataProvider<T> dataProvider){
        super(id,dataProvider);
    }
    protected DataView(final String id,final IDataProvider<T> dataProvider,final int itemsPerPage){
        super(id,dataProvider);
        this.setItemsPerPage(itemsPerPage);
    }
    public IDataProvider<T> getDataProvider(){
        return this.internalGetDataProvider();
    }
}
