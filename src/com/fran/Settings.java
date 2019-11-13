package com.fran;

/**
 * Settings class controls the static options that will affect the simulation generation/running. Settings should be
 * accessible from a static instance anywhere its imported.
 * @author Francisco Caeiro
 * */

public class Settings {
    /** Amount of groups of gorillas in the habitat*/
    static public final int groupsOfGorillas = 3;
    /** Amount of food sources in grid boundary*/
    static public final int amountFoodSources = 10;
    /** How much the food sources will spread from the centre*/
    static public final int foodSpreadingIntensity = 10;
    /** Maximum quantity of gorillas per group*/
    static public final int maxPopulation = 100;
    /** Minimum quanitity of gorillas per group*/
    static public final int minPopulation = 50;
    /** Area of home range will be radius*2 by radius*2*/
    static public final int homerangeRadius = 8;
    /** Time in days each group of gorillas stays around a food source*/
    static public final int gorillaFoodWaitTime = 7;
    /**Length of gorillas memory*/
    static public final int gorillaFoodMemoryNumber = 4;
    /** Timer to delete an item off a gorillas memory*/
    static public final int gorillaMemoryDeletionTime = 10;
    /**How big the y axis of the simulation will be*/
    static public final int simulationWidth = 100;
    /**How big the x axis of the simulation will be*/
    static public final int simulationHeight = 100;
    /**Hides unvisited food sources*/
    static public final boolean hideUnusedFoodSources = true;
}
