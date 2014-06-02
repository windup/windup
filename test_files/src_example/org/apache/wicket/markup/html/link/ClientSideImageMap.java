package org.apache.wicket.markup.html.link;

import org.apache.wicket.markup.html.panel.*;
import org.apache.wicket.markup.repeater.*;
import org.apache.wicket.markup.html.image.*;
import org.apache.wicket.behavior.*;
import org.apache.wicket.*;
import java.awt.*;
import java.awt.geom.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.model.*;
import java.io.*;

public class ClientSideImageMap extends Panel{
    private static final long serialVersionUID=1L;
    private static final String CIRCLE="circle";
    private static final String POLYGON="polygon";
    private static final String RECTANGLE="rect";
    private RepeatingView areas;
    public ClientSideImageMap(final String id,final Image image){
        super(id);
        this.setOutputMarkupId(true);
        this.add(AttributeModifier.replace("name",new PropertyModel<Object>(this,"markupId")));
        image.add(AttributeModifier.replace("usemap",new UsemapModel()));
        this.areas=new RepeatingView("area");
        this.add(this.areas);
    }
    private String circleCoordinates(final int x,final int y,final int radius){
        return x+","+y+","+radius;
    }
    private String polygonCoordinates(final int... coordinates){
        final StringBuilder buffer=new StringBuilder();
        for(int i=0;i<coordinates.length;++i){
            buffer.append(coordinates[i]);
            if(i<coordinates.length-1){
                buffer.append(',');
            }
        }
        return buffer.toString();
    }
    private String rectangleCoordinates(final int x1,final int y1,final int x2,final int y2){
        return x1+","+y1+","+x2+","+y2;
    }
    private String shapeCoordinates(final Shape shape){
        final StringBuilder sb=new StringBuilder();
        final PathIterator pi=shape.getPathIterator(null,1.0);
        final float[] coords=new float[6];
        final float[] lastMove=new float[2];
        while(!pi.isDone()){
            switch(pi.currentSegment(coords)){
                case 0:{
                    if(sb.length()!=0){
                        sb.append(",");
                    }
                    sb.append(Math.round(coords[0]));
                    sb.append(",");
                    sb.append(Math.round(coords[1]));
                    lastMove[0]=coords[0];
                    lastMove[1]=coords[1];
                    break;
                }
                case 1:{
                    if(sb.length()!=0){
                        sb.append(",");
                    }
                    sb.append(Math.round(coords[0]));
                    sb.append(",");
                    sb.append(Math.round(coords[1]));
                    break;
                }
                case 4:{
                    if(sb.length()!=0){
                        sb.append(",");
                    }
                    sb.append(Math.round(lastMove[0]));
                    sb.append(",");
                    sb.append(Math.round(lastMove[1]));
                    break;
                }
            }
            pi.next();
        }
        return sb.toString();
    }
    protected void onComponentTag(final ComponentTag tag){
        this.checkComponentTag(tag,"map");
        super.onComponentTag(tag);
    }
    public String newChildId(){
        return this.areas.newChildId();
    }
    public ClientSideImageMap addCircleArea(final AbstractLink link,final int x,final int y,final int radius){
        this.areas.add(link);
        link.add(new Area(this.circleCoordinates(x,y,radius),"circle"));
        return this;
    }
    public ClientSideImageMap addPolygonArea(final AbstractLink link,final int... coordinates){
        this.areas.add(link);
        link.add(new Area(this.polygonCoordinates(coordinates),"polygon"));
        return this;
    }
    public ClientSideImageMap addRectangleArea(final AbstractLink link,final int x1,final int y1,final int x2,final int y2){
        this.areas.add(link);
        link.add(new Area(this.rectangleCoordinates(x1,y1,x2,y2),"rect"));
        return this;
    }
    public ClientSideImageMap addShapeArea(final AbstractLink link,final Shape shape){
        this.areas.add(link);
        link.add(new Area(this.shapeCoordinates(shape),"polygon"));
        return this;
    }
    private static class Area extends Behavior{
        private static final long serialVersionUID=1L;
        private final String coordinates;
        private final String type;
        protected Area(final String coordinates,final String type){
            super();
            this.coordinates=coordinates;
            this.type=type;
        }
        public void onComponentTag(final Component component,final ComponentTag tag){
            tag.put("shape",(CharSequence)this.type);
            tag.put("coords",(CharSequence)this.coordinates);
        }
    }
    private class UsemapModel extends Model<String>{
        private static final long serialVersionUID=1L;
        public String getObject(){
            return "#"+ClientSideImageMap.this.getMarkupId();
        }
    }
}
