package samples.ejb;

import java.io.Serializable;

public class NiceThings 
                        {
                        
                        
    private String food;
    private int luckyNumber;
    private String colour;

    public NiceThings(String food, int luckyNumber, String colour) {
        this.food = food;
        this.luckyNumber = luckyNumber;
        this.colour = colour;
    }
    
    public String getFood() {
        return food;
    }
    
    public int getLuckyNumber() {
        return luckyNumber;
    }
    
    public String getColour() {
        return colour;
    }
    
    public void setFood(String food) { 
        this.food = food; 
    }
    
    public void setLuckyNumber(int luckyNumber) {
        this.luckyNumber = luckyNumber;
    }
    
    public void setColour(String colour) {
        this.colour = colour;
    }
    
}
