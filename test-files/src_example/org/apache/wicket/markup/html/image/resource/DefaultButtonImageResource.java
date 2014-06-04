package org.apache.wicket.markup.html.image.resource;

import java.awt.font.*;
import java.text.*;
import java.util.*;
import java.awt.*;

public class DefaultButtonImageResource extends RenderedDynamicImageResource{
    private static final long serialVersionUID=1L;
    private static int defaultHeight;
    private static int defaultWidth;
    private static final int DEFAULT_COLOR;
    private static final int DEFAULT_BACKGROUND_COLOR;
    private static final int DEFAULT_TEXT_COLOR;
    private static final Map<TextAttribute,Object> DEFAULT_FONT;
    private int arcHeight;
    private int arcWidth;
    private int backgroundColorRgb;
    private int colorRgb;
    private Map<TextAttribute,Object> fontAttributes;
    private int textColorRgb;
    private final String label;
    public static void setDefaultHeight(final int defaultHeight){
        DefaultButtonImageResource.defaultHeight=defaultHeight;
    }
    public static void setDefaultWidth(final int defaultWidth){
        DefaultButtonImageResource.defaultWidth=defaultWidth;
    }
    public DefaultButtonImageResource(final int width,final int height,final String label){
        super(width,height,"png");
        this.arcHeight=10;
        this.arcWidth=10;
        this.backgroundColorRgb=DefaultButtonImageResource.DEFAULT_BACKGROUND_COLOR;
        this.colorRgb=DefaultButtonImageResource.DEFAULT_COLOR;
        this.fontAttributes=DefaultButtonImageResource.DEFAULT_FONT;
        this.textColorRgb=DefaultButtonImageResource.DEFAULT_TEXT_COLOR;
        this.label=label;
        this.setWidth((width==-1)?DefaultButtonImageResource.defaultWidth:width);
        this.setHeight((height==-1)?DefaultButtonImageResource.defaultHeight:height);
    }
    public DefaultButtonImageResource(final String label){
        this(DefaultButtonImageResource.defaultWidth,DefaultButtonImageResource.defaultHeight,label);
    }
    public synchronized int getArcHeight(){
        return this.arcHeight;
    }
    public synchronized int getArcWidth(){
        return this.arcWidth;
    }
    public synchronized Color getBackgroundColor(){
        return new Color(this.backgroundColorRgb);
    }
    public synchronized Color getColor(){
        return new Color(this.colorRgb);
    }
    public synchronized Font getFont(){
        return new Font(this.fontAttributes);
    }
    public synchronized Color getTextColor(){
        return new Color(this.textColorRgb);
    }
    public synchronized void setArcHeight(final int arcHeight){
        this.arcHeight=arcHeight;
        this.invalidate();
    }
    public synchronized void setArcWidth(final int arcWidth){
        this.arcWidth=arcWidth;
        this.invalidate();
    }
    public synchronized void setBackgroundColor(final Color backgroundColor){
        this.backgroundColorRgb=backgroundColor.getRGB();
        this.invalidate();
    }
    public synchronized void setColor(final Color color){
        this.colorRgb=color.getRGB();
        this.invalidate();
    }
    public synchronized void setFont(final Font font){
        this.fontAttributes=(Map<TextAttribute,Object>)new HashMap(font.getAttributes());
        this.invalidate();
    }
    public synchronized void setTextColor(final Color textColor){
        this.textColorRgb=textColor.getRGB();
        this.invalidate();
    }
    protected boolean render(final Graphics2D graphics){
        final int width=this.getWidth();
        final int height=this.getHeight();
        graphics.setFont(this.getFont());
        final FontMetrics fontMetrics=graphics.getFontMetrics();
        final int dxText=fontMetrics.stringWidth(this.label);
        final int dxMargin=10;
        if(dxText>width-10){
            this.setWidth(dxText+10);
            return false;
        }
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        final Color bgColor=this.getBackgroundColor();
        graphics.setColor(bgColor);
        graphics.fillRect(0,0,width,height);
        graphics.setColor(this.getColor());
        graphics.setBackground(bgColor);
        graphics.fillRoundRect(0,0,width,height,this.arcWidth,this.arcHeight);
        graphics.setColor(this.getTextColor());
        final int x=(width-dxText)/2;
        final int y=(this.getHeight()-fontMetrics.getHeight())/2;
        graphics.drawString(this.label,x,y+fontMetrics.getAscent());
        return true;
    }
    static{
        DefaultButtonImageResource.defaultHeight=26;
        DefaultButtonImageResource.defaultWidth=74;
        DEFAULT_COLOR=new Color(233,96,26).getRGB();
        DEFAULT_BACKGROUND_COLOR=Color.WHITE.getRGB();
        DEFAULT_TEXT_COLOR=Color.WHITE.getRGB();
        DEFAULT_FONT=new HashMap(new Font("Helvetica",1,16).getAttributes());
    }
}
