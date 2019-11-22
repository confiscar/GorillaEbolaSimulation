package com.fran;

import sim.util.Int2D;
/**
 * Class represents the food source object in the model.
 * */
public class FoodSource {
    /**Represents the current location of the source*/
    public Int2D location;
    public boolean visible;
    public double heat;
    public double getHeat(){return heat;}

    FoodSource(Int2D location){
        this.location = location;
        this.visible = false;
        this.heat = 0.0;
    }

    public void incrementHeat(){
        heat += 0.2;
        if(heat > 255)
            heat = 255;
    }
}
