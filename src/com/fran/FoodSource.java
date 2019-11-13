package com.fran;

import sim.util.Int2D;
/**
 * Class represents the food source object in the model.
 * */
public class FoodSource {
    /**Represents the current location of the source*/
    public Int2D location;
    public boolean visible;

    FoodSource(Int2D location){
        this.location = location;
        this.visible = false;
    }
}
