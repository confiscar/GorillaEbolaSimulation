package com.fran;

import sim.engine.SimState;
import sim.field.grid.SparseGrid2D;
import sim.util.Bag;
import sim.util.Int2D;
import sim.field.network.*;

/**
 * Apes is the subclass implementation of the SimState module. This class will take care of the logic/model view of
 * the simulation.
 * @author Francisco Caeiro
 * */
public class Apes extends SimState {
    /**How big the y axis of the simulation will be*/
    static public final int simulationWidth = (int) (Settings.foodSpreadingIntensity * 2.5);
    /**How big the x axis of the simulation will be*/
    static public final int simulationHeight = (int) (Settings.foodSpreadingIntensity * 2.5);
    /**Helper variables that give the center of x axis of the screen*/
    static int centreX = simulationWidth / 2;
    /**Helper variables that give the center of y axis of the screen*/
    static int centreY = simulationHeight / 2;
    /**Habitat represents the living space that the apes inhabit. Currently has a trivial width and height*/
    public SparseGrid2D habitat = new SparseGrid2D(simulationWidth,simulationHeight);
    /**Network that represents the interactions between the apes*/
    public Network interactions = new Network(false);
    /**Aids the generation of ape groups. Used to shuffle food sources and assign to ape group*/
    private Bag foodSources = new Bag(Settings.amountFoodSources);

    /**
     * Constructor that takes in seed and feeds in to super SimState constructor
     * */
    public Apes(long seed){
        /* Seed is used when random number generator is initialized*/
        super(seed);
    }

    /**
     * Constructor that automatically sets seed to the current time and feeds to SimState constructor
     * */
    public Apes(){
        this(System.currentTimeMillis());
    }

    /**
     * Function called at the start of running the model. Clears and initializes the objects in the simulation.
     * */
    public void start(){
        /*Initializes RNG generator as well as resetting things like scheduler */
        super.start();

        /*Clear the hash map in the habitat when restarted*/
        habitat.clear();
        interactions.clear();
        foodSources.clear();

        /*Initialize food and apes*/
        initializeFoodSource();
        initializeApeGroups();
    }
    /**
     * Initializes food sources by randomly clustering them around the centre. The foodSpreadingIntensity variable in
     * the Settings object adjusts how much it spreads around the centre.
     * */
    public void initializeFoodSource(){
        /*Creates n amount of food sources*/
        for(int i = 0; i < Settings.amountFoodSources; i++){
            int x, y;
            /*Keeps looping until find two unique pairs of x and y for the food source*/
            do{
                x = centreX + (random.nextInt() % Settings.foodSpreadingIntensity);
                y = centreY + (random.nextInt() % Settings.foodSpreadingIntensity);
            }while(x < 0 || x >= simulationWidth || y < 0 || y >= simulationHeight || habitat.getObjectsAtLocation(x, y) != null );

            /*Initializes new food source, adds it to habitat and foodSource bag*/
            Int2D location = new Int2D(x, y);
            FoodSource food = new FoodSource(location);
            habitat.setObjectLocation(food, location);
            foodSources.add(food);
        }
    }

    /**
     * Initializes the Ape agents by assigning them a food source that will act as the centre of their home range.
     */
    public void initializeApeGroups(){
        /*Creates a copy and randomly shuffles our foodSources bag*/
        Bag foodSourceLocations = new Bag(foodSources);
        foodSourceLocations.shuffle(random);

        /*Throws an error if more gorillas then food sources. This is problem due to not being able to assign all
          gorilla groups a unique food source*/
        if(Settings.groupsOfGorillas > Settings.amountFoodSources){
            System.out.println("Settings Error: Amount of gorilla groups can't be larger than amount of food sources.");
            System.exit(-1);
        }

        /*Loop creates n amount of groups*/
        for(int i = 0; i < Settings.groupsOfGorillas; i++){
            /*Pops random food source and sets Apes initial location to it*/
            FoodSource fs = (FoodSource) foodSourceLocations.pop();
            Ape ape = new Ape(this, fs.location);
            habitat.setObjectLocation(
                    ape,
                    fs.location.x,
                    fs.location.y
            );
            /*Links a network node to each individual ape*/
            interactions.addNode(ape);
            /*Adds each agent to the scheduler* to be stepped*/
            schedule.scheduleRepeating(ape);
        }

        double gorillaDensity = (double) Settings.groupsOfGorillas
                / (double)(Settings.foodSpreadingIntensity * Settings.foodSpreadingIntensity);
        System.out.println("Gorilla Density (per cell) : " + gorillaDensity);

    }

    public static void main(String[] args){
        /* doLoop steps:
        Create instance of SimState subclass and initialize random number generator ->
        Call start() from your subclass ->
        Repeatedly call step() ->
        When schedule entirely empty of agents call finish() to clean up
         */
        doLoop(Apes.class, args);

        /* Since the framework uses threads, exit(0) has to be called, just in case
        you forget to turn your user threads into daemon threads. Daemon threads do not prevent
        the JVM from shutting down, whilst user threads do.
         */
        System.exit(0 );
    }


}
