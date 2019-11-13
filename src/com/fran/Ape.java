package com.fran;

import javafx.util.Pair;
import org.jfree.data.time.ohlc.OHLC;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.grid.SparseGrid2D;
import sim.field.network.Edge;
import sim.util.Bag;
import sim.util.Int2D;

/**
 * Ape is a steppable agent in the simulation. It represents the gorilla groups and controls the behaviour of said
 * agents.
 * @author Francisco Caeiro
 * */

public class Ape implements Steppable {

    private Int2D centerHomeRange;
    private Bag neighbourFoodSources;
    private int memoryCounter;
    private int movementCounter;
    public int populationCount;
    public int getPopulation(){return this.populationCount; }

    /***
     * Constructor takes the simState and an Int2D that represents the centre of the gorillas home range (which will
     * be a randomly chosen food source)
     */
    public Ape(SimState simState, Int2D centerHomeRange){
        Apes apes = (Apes) simState;

        /*Sets the centre, and creates bag that will contain locations of neighbouring food sources*/
        this.centerHomeRange = centerHomeRange;
        this.neighbourFoodSources = new Bag();

        /*This will be the 'timer' for specific gorilla behaviour*/
        memoryCounter = Settings.gorillaMemoryDeletionTime;
        movementCounter = Settings.gorillaFoodWaitTime;

        populationCount = apes.random.nextInt(Settings.maxPopulation - Settings.minPopulation) + Settings.minPopulation;

        /*Gets the moore neighbours (circle around the gorillas) */
        Bag allNeighbours = apes.habitat.getMooreNeighbors(
            centerHomeRange.x, centerHomeRange.y,
            Settings.homerangeRadius, SparseGrid2D.BOUNDED, true);

        /*Filters the objects to just get the food sources*/
        for(int i = 0; i < allNeighbours.size(); i++){
            Object obj = allNeighbours.get(i);
            if(obj instanceof FoodSource){
                FoodSource fs = (FoodSource) obj;
                /*Tags the food source to not be deleted*/
                fs.visible = true;
                neighbourFoodSources.add(fs);
            }
        }
    }

    /**
     * Called by the scheduler after each step. Used to update positions, weights and state of the agents.
     * */
    @Override
    public void step(SimState simState) {
        /*Get static simState instance and cast as our subclass to get functions and member vars*/
        Apes apes = (Apes) simState;
        SparseGrid2D habitat = apes.habitat;

        //memoryCounter--;
        movementCounter--;

        /*If movementCounter runs out, search for new food source*/
        if(movementCounter <= 0 && neighbourFoodSources.size() > 1){
            /*Reset movement counter*/
            movementCounter = Settings.gorillaFoodWaitTime;
            FoodSource newSource = getNewFoodSource(simState);
            habitat.setObjectLocation(this, newSource.location);
            /*Updates the network of interactions*/
            updateNetwork(simState);
        }

    }


    /**
     * Function checks to see if any other gorilla groups are in the cell, and adds 1 to the weight for every
     * interaction.
     * */
    private void updateNetwork(SimState simState) {
        /*Get static simState instance and cast as our subclass to get functions and member vars*/
        Apes apes = (Apes) simState;
        SparseGrid2D habitat = apes.habitat;

        /*Gets our location and then gets all objects at our location*/
        Int2D me = habitat.getObjectLocation(this);
        Bag cell = habitat.getObjectsAtLocation(me);

        /*Loops through every object in our location and if another gorilla group is found, updates the network*/
        for(int i = 0; i < cell.size(); i++){
            Object obj = cell.get(i);
            /*If object is gorilla group and is not the current object*/
            if(obj instanceof Ape && obj != this){
                /*If the edge doesn't exist, create it*/
                if(apes.interactions.getEdge(this, obj) == null){
                    apes.interactions.addEdge(this, obj, 1);
                }
                /*Else, get edge and add 1 to the weight, then update edge*/
                else{
                    Edge edge = apes.interactions.getEdge(this, obj);
                    Integer interactionCount = (Integer) edge.getInfo();
                    interactionCount++;
                    apes.interactions.updateEdge(edge, this, obj, interactionCount);
                }
            }
        }
    }

    private FoodSource getNewFoodSource(SimState simState){
        /*Get static simState instance and cast as our subclass to get functions and member vars*/
        Apes apes = (Apes) simState;
        SparseGrid2D habitat = apes.habitat;

        /*Create a bag with FoodSource, Probability value pairs*/
        Bag probabilities = new Bag();

        /*Loops through the neighbouring FoodSources, computes the distance to them and stores
        * pairs of (FoodSource, probability based on distance)*/
        Int2D me = habitat.getObjectLocation(this);
        for(int i = 0; i < neighbourFoodSources.size(); i++){
            FoodSource fs = (FoodSource) neighbourFoodSources.get(i);
            /*Omits the current food source*/
            if(me.x != fs.location.x && me.y != fs.location.y)
                probabilities.add(new Pair<>(fs, calculateProbabilityDistance(me.x, me.y, fs.location.x, fs.location.y)));
        }

        /*We calculate the sum so we can normalise the probabilities*/
        double sum = 0;
        for(int i = 0; i < probabilities.size(); i++){
            Object obj = probabilities.get(i);
            if(obj instanceof Pair){
                sum += (double)((Pair) obj).getValue();
            }
        }

        /*Since Pairs are immutable, pops all pairs and pushed new pairs that are divided by the sum*/
        int size = probabilities.size();
        for(int i = 0; i < size; i++){
            Object obj = probabilities.pop();
            if(obj instanceof Pair){
                FoodSource fs = (FoodSource)((Pair) obj).getKey();
                Double value = (Double) ((Pair) obj).getValue();
                probabilities.push(new Pair<>(fs, value / sum));
            }
        }

        /*Find the correct index according to probabilities*/
        double randomDouble = apes.random.nextDouble();
        double choose = 0;
        int index = 0;
        boolean found = false;

        for(int j = 0; j < probabilities.size() && !found; j++){
            Object obj = probabilities.get(j);
            if(obj instanceof Pair){
                choose += (double) ((Pair) obj).getValue();
                if(randomDouble < choose){
                    index = j;
                    found = true;
                }
            }
        }

        return (FoodSource)((Pair)probabilities.get(index)).getKey();
    }

    private double calculateProbabilityDistance(double gorillaX, double gorillaY, double foodSourceX, double foodSourceY){
        double distance = Math.sqrt(
                Math.pow(foodSourceX - gorillaX, 2) +
                Math.pow(foodSourceY - gorillaY, 2));
        return 1 / distance;
    }
}
