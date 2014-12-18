package org.apache.commons.lang;

public class BitField{
    private final int _mask;
    private final int _shift_count;
    public BitField(final int mask){
        super();
        this._mask=mask;
        int count=0;
        int bit_pattern=mask;
        if(bit_pattern!=0){
            while((bit_pattern&0x1)==0x0){
                ++count;
                bit_pattern>>=1;
            }
        }
        this._shift_count=count;
    }
    public int getValue(final int holder){
        return this.getRawValue(holder)>>this._shift_count;
    }
    public short getShortValue(final short holder){
        return (short)this.getValue(holder);
    }
    public int getRawValue(final int holder){
        return holder&this._mask;
    }
    public short getShortRawValue(final short holder){
        return (short)this.getRawValue(holder);
    }
    public boolean isSet(final int holder){
        return (holder&this._mask)!=0x0;
    }
    public boolean isAllSet(final int holder){
        return (holder&this._mask)==this._mask;
    }
    public int setValue(final int holder,final int value){
        return (holder&~this._mask)|(value<<this._shift_count&this._mask);
    }
    public short setShortValue(final short holder,final short value){
        return (short)this.setValue(holder,value);
    }
    public int clear(final int holder){
        return holder&~this._mask;
    }
    public short clearShort(final short holder){
        return (short)this.clear(holder);
    }
    public byte clearByte(final byte holder){
        return (byte)this.clear(holder);
    }
    public int set(final int holder){
        return holder|this._mask;
    }
    public short setShort(final short holder){
        return (short)this.set(holder);
    }
    public byte setByte(final byte holder){
        return (byte)this.set(holder);
    }
    public int setBoolean(final int holder,final boolean flag){
        return flag?this.set(holder):this.clear(holder);
    }
    public short setShortBoolean(final short holder,final boolean flag){
        return flag?this.setShort(holder):this.clearShort(holder);
    }
    public byte setByteBoolean(final byte holder,final boolean flag){
        return flag?this.setByte(holder):this.clearByte(holder);
    }
}
