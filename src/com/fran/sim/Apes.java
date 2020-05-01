package com.fran.sim;

import com.fran.util.IOHandler;
import com.fran.util.RecordPrinter;
import com.fran.util.Stats;
import sim.engine.SimState;
import sim.field.grid.SparseGrid2D;
import sim.util.Bag;
import sim.util.Int2D;
import sim.field.network.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Apes is the subclass implementation of the SimState module. This class will take care of the
 * logic/model view of the simulation.
 *
 * @author Francisco Caeiro
 */
public class Apes extends SimState {
  /** How big the y axis of the simulation will be */
  private static final int simulationWidth = (SimParameters.foodSpreadingIntensity * 3);
  /** How big the x axis of the simulation will be */
  private static final int simulationHeight = (SimParameters.foodSpreadingIntensity * 3);
  /** Habitat represents the living space that the apes inhabit. */
  SparseGrid2D habitat = new SparseGrid2D(simulationWidth, simulationHeight);
  /** Network that represents the interactions between the apes */
  Network interactions = new Network(false);
  /** Aids the generation of ape groups. Used to shuffle food sources and assign to ape group */
  private Bag foodSources = new Bag(SimParameters.amountFoodSources);
  /** Object gorilla group interactions into a file */
  public RecordPrinter recordPrinter = new RecordPrinter();

  public Stats stat;

  /** Constructor that takes in seed and feeds in to super SimState constructor */
  public Apes(long seed) {
    /* Seed is used when random number generator is initialized*/
    super(seed);
  }

  /**
   * Constructor that automatically sets seed to the current time and feeds to SimState constructor
   */
  public Apes() {
    this(System.currentTimeMillis());
  }

  /**
   * Function called at the start of running the model. Clears and initializes the objects in the
   * simulation.
   */
  public void start() {
    /*Initializes RNG generator as well as resetting things like scheduler */
    super.start();

    /*Clear the hash map in the habitat when restarted*/
    habitat.clear();
    interactions.clear();
    foodSources.clear();

    SimSettings.setFactors();

    /*Initialize food and apes*/
    initializeFoodSource();
    initializeApeGroups();

    indexCase(1);
  }
  /**
   * Initializes food sources by randomly clustering them around the centre. The
   * foodSpreadingIntensity variable in the Settings object adjusts how much it spreads around the
   * centre.
   */
  private void initializeFoodSource() {

    /*Basic error checking to see if all food sources can be placed*/
    int areaOfFoodSpread = (int) Math.pow((SimParameters.foodSpreadingIntensity * 2) + 1, 2);
    if (SimParameters.amountFoodSources > areaOfFoodSpread) {
      System.out.println(
          "Settings Error: Amount of food sources bigger than possible area to place them.");
      System.exit(-1);
    }

    /*Creates n amount of food sources*/
    for (int i = 0; i < SimParameters.amountFoodSources; i++) {
      int x, y;
      /*Keeps looping until find two unique pairs of x and y for the food source*/
      do {
        int centreX = simulationWidth / 2;
        int centreY = simulationHeight / 2;
        x = centreX + (random.nextInt() % (SimParameters.foodSpreadingIntensity + 1));
        y = centreY + (random.nextInt() % (SimParameters.foodSpreadingIntensity + 1));
      } while (x < 0
          || x >= simulationWidth
          || y < 0
          || y >= simulationHeight
          || habitat.getObjectsAtLocation(x, y) != null);

      /*Initializes new food source, adds it to habitat and foodSource bag*/
      Int2D location = new Int2D(x, y);
      FoodSource food = new FoodSource(location);
      habitat.setObjectLocation(food, location);
      foodSources.add(food);
      schedule.scheduleRepeating(food);

    }
  }

  public void finish() {
    SimSettings.statsFromRun.add(stat);

    if (SimSettings.useSimlab) {
      try {
        IOHandler.write("" + stat.getRatioOfDeceased());
      } catch (IOException e) {
        e.printStackTrace();
        System.exit(-1);
      }
    }

    /*System.out.println(
        "T "
            + stat.getTotalInitialPopulation()
            + " I "
            + stat.getTotalInfectedGorillas()
            + " R "
            + stat.getTotalRecoveredGorillas()
            + " D "
            + stat.getTotalDeceasedGorillas());
    System.out.println("Ratio: " + stat.getRatioOfDeceased());*/

    super.finish();
  }

  /**
   * Initializes the Ape agents by assigning them a food source that will act as the centre of their
   * home range.
   */
  private void initializeApeGroups() {
    /*Throws an error if more gorillas then food sources. This is problem due to not being able to assign all
    gorilla groups a unique food source*/
    if (SimParameters.groupsOfGorillas > SimParameters.amountFoodSources) {
      System.out.println(
          "Settings Error: Amount of gorilla groups can't be larger than amount of food sources.");
      System.exit(-2);
    }

    /*Creates a copy and randomly shuffles our foodSources bag*/
    Bag foodSourceLocations = new Bag(foodSources);
    foodSourceLocations.shuffle(random);

    int sumOfGorillaPopulation = 0;

    /*Loop creates n amount of groups*/
    for (int i = 0; i < SimParameters.groupsOfGorillas; i++) {
      /*Pops random food source and sets Apes initial location to it*/
      FoodSource fs = (FoodSource) foodSourceLocations.pop();
      Ape ape = new Ape(this, fs.location);
      habitat.setObjectLocation(ape, fs.location.x, fs.location.y);
      /*Links a network node to each individual ape*/
      interactions.addNode(ape);
      /*Adds each agent to the scheduler* to be stepped*/
      schedule.scheduleRepeating(ape);

      sumOfGorillaPopulation += ape.getPopulation();
    }

    stat = new Stats(sumOfGorillaPopulation);

    // System.out.println("Gorilla Density per km^2 : " + stat.getGorillaDensity());
  }

  /**
   * Function called on setup to randomly place infections around the board.
   *
   * @param numberOfInitialInfections number of initial infections for the simulation
   */
  public void indexCase(int numberOfInitialInfections) {
    // TODO Ensure that each infection placement is unique
    Stream<Object> stream = habitat.getAllObjects().stream();
    Bag apes = new Bag();
    apes.addAll(stream.filter(obj -> obj instanceof Ape).collect(Collectors.toList()));
    Ape ape = (Ape) apes.get(random.nextInt(apes.size()));
    ape.susceptibleCount--;
    ape.infectedCount++;
    stat.incrementTotalInfectedGorillas();
    ape.infectionTimer.add(SimParameters.infectionTime);
  }

  public static void main(String[] args) {
    /* doLoop steps:
    Create instance of SimState subclass and initialize random number generator ->
    Call start() from your subclass ->
    Repeatedly call step() ->
    When schedule entirely empty of agents call finish() to clean up
     */
    IOHandler.input = new File(SimSettings.inputFile);
    IOHandler.output = new File(SimSettings.outputFile);
    SimSettings.useSimlab = true;

    try {
      IOHandler.writeHeader();
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(-1);
    }
    try {
      IOHandler.read();
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(-1);
    }

    String[] parameters = {
      "-until",
      "" + SimSettings.numberOfSteps,
      "-repeat",
      "" + SimSettings.numberOfRuns,
      "-seed",
      "" + SimSettings.seed
    };

    doLoop(Apes.class, parameters);

    /* Since the framework uses threads, exit(0) has to be called, just in case
    you forget to turn your user threads into daemon threads. Daemon threads do not prevent
    the JVM from shutting down, whilst user threads do.
     */
    System.exit(0);
  }
}
