package com.fran.sim;

import javafx.util.Pair;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.grid.SparseGrid2D;
import sim.field.network.Edge;
import sim.util.Bag;
import sim.util.Int2D;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Ape is a steppable agent in the simulation. It represents the gorilla groups and controls the
 * behaviour of said agents.
 *
 * @author Francisco Caeiro
 */
public class Ape implements Steppable {

  private Bag neighbourFoodSources;
  private Bag memoryFoodSources;
  private int movementCounter;

  /** Integers representing the quantity of the gorillas (total, male, female) */
  private int populationCount;
  /** Integers representing the quantity of female gorillas (total, male, female) */
  private int femaleCount;
  /** Integers representing the quantity of male gorillas (total, male, female) */
  private int maleCount;

  private int susceptibleCount;
  private int infectedCount;
  private int recoveredCount;
  private int deceasedCount;

  private boolean hasSilverbackDied;

  /**
   * The statesMale tracks the current state in the SIR model of each male.
   * As a convention the 0 index in the statesMale list is the current status of the silver back
   * gorilla
   */
  public List<Integer> statesMale;
  /**
   * The statesFemale tracks the current state in the SIR model of each female.
   */
  public List<Integer> statesFemale;

  /** Java Bean to display population */
  public int getPopulation() {
    return this.populationCount;
  }
  /** Java Bean to display male apes */
  public int getMales() {
    return this.maleCount;
  }
  /** Java Bean to display females apes */
  public int getFemales() {
    return this.femaleCount;
  }

  /**
   * * Constructor takes the simState and an Int2D that represents the centre of the gorillas home
   * range (which will be a randomly chosen food source)
   */
  Ape(SimState simState, Int2D centerHomeRange) {
    Apes apes = (Apes) simState;
    hasSilverbackDied = false;

    /*Creates bags that will contain locations of neighbouring food sources (one is for the memory)*/
    this.neighbourFoodSources = new Bag();
    this.memoryFoodSources = new Bag(Settings.gorillaMemoryLength);

    /*This will be the 'timer' for specific gorilla behaviour*/
    movementCounter = Settings.gorillaFoodWaitTime;

    /*Calculates population between set boundaries and corresponding gender divisions*/
    populationCount =
        apes.random.nextInt(Settings.maxPopulation - Settings.minPopulation)
            + Settings.minPopulation;
    femaleCount = (int) ((double) populationCount * apes.random.nextDouble());
    maleCount = populationCount - femaleCount;

    /*Gets the moore neighbours (circle around the gorillas) */
    Bag allNeighbours =
        apes.habitat.getMooreNeighbors(
            centerHomeRange.x,
            centerHomeRange.y,
            Settings.homerangeRadius,
            SparseGrid2D.BOUNDED,
            true);

    /*Filters the objects to just get the food sources*/
    Stream<Object> stream = allNeighbours.stream();
    neighbourFoodSources.addAll(
        stream.filter(obj -> obj instanceof FoodSource).collect(Collectors.toList()));
    neighbourFoodSources.forEach(obj -> ((FoodSource) obj).setVisible());

    stream = neighbourFoodSources.stream();
    memoryFoodSources.addAll(
        stream
            .filter(obj -> ((FoodSource) obj).location == centerHomeRange)
            .collect(Collectors.toList()));

    susceptibleCount = populationCount;
    infectedCount = 0;
    recoveredCount = 0;
    deceasedCount = 0;
  }

  /**
   * Called by the scheduler after each step. Used to update positions, weights and state of the
   * agents.
   */
  @Override
  public void step(SimState simState) {
    /*Get static simState instance and cast as our subclass to get functions and member vars*/
    Apes apes = (Apes) simState;
    SparseGrid2D habitat = apes.habitat;

    movementCounter--;

    /*If movementCounter runs out, search for new food source*/
    if (movementCounter <= 0 && neighbourFoodSources.size() > 1) {
      /*Reset movement counter*/
      movementCounter = Settings.gorillaFoodWaitTime;
      /*Get new food source*/
      FoodSource fs = getNewFoodSource(simState);
      habitat.setObjectLocation(this, fs.location);
      /*Updates the network of interactions*/
      updateNetwork(simState);
    }
  }

  /**
   * Function checks to see if any other gorilla groups are in the cell, and adds 1 to the weight
   * for every interaction.
   */
  private void updateNetwork(SimState simState) {
    /*Get static simState instance and cast as our subclass to get functions and member vars*/
    Apes apes = (Apes) simState;
    SparseGrid2D habitat = apes.habitat;

    /*Gets our location and then gets all objects at our location*/
    Int2D me = habitat.getObjectLocation(this);
    Bag cell = habitat.getObjectsAtLocation(me);

    /*Loops through every object in our location and if another gorilla group is found, updates the network*/
    for (int i = 0; i < cell.size(); i++) {
      Object obj = cell.get(i);
      /*If object is gorilla group and is not the current object*/
      if (obj instanceof Ape && obj != this) {
        /*If the edge doesn't exist, create it*/
        if (apes.interactions.getEdge(this, obj) == null) {
          apes.interactions.addEdge(this, obj, 1);
        }
        /*Else, get edge and add 1 to the weight, then update edge*/
        else {
          Edge edge = apes.interactions.getEdge(this, obj);
          Integer interactionCount = (Integer) edge.getInfo();
          interactionCount++;
          apes.interactions.updateEdge(edge, this, obj, interactionCount);
        }
        apes.recordPrinter.addInteractionApe(this, (Ape) obj);
      }
    }
  }

  private FoodSource getNewFoodSource(SimState simState) {
    /*Get static simState instance and cast as our subclass to get functions and member vars*/
    Apes apes = (Apes) simState;
    SparseGrid2D habitat = apes.habitat;

    /*Create a bag with FoodSource, Probability value pairs*/
    Bag probabilities = new Bag(neighbourFoodSources.size());
    Bag normalisedProbabilities = new Bag(neighbourFoodSources.size());

    /*Loops through the neighbouring FoodSources, computes the distance to them and stores
     * pairs of (FoodSource, probability based on distance). Also calculates sums of all the probabilities*/
    Int2D me = habitat.getObjectLocation(this);
    double sum = 0;
    for (int i = 0; i < neighbourFoodSources.size(); i++) {
      FoodSource fs = (FoodSource) neighbourFoodSources.get(i);
      /*Omits current food source if at same location as agent or if in memory*/
      if (!(me.x == fs.location.x && me.y == fs.location.y) && !memoryFoodSources.contains(fs)) {
        double probabilityDistance =
            calculateProbabilityDistance(me.x, me.y, fs.location.x, fs.location.y);
        probabilities.add(new Pair<>(fs, probabilityDistance));
        sum += probabilityDistance;
      }
    }

    if (probabilities.isEmpty()) {
      memoryFoodSources = new Bag();
      return getNewFoodSource(simState);
    }

    /*Since Pairs are immutable, pops all pairs and pushed new pairs that are divided by the sum*/
    int size = probabilities.size();
    for (int i = 0; i < size; i++) {
      Object obj = probabilities.pop();
      if (obj instanceof Pair) {
        FoodSource fs = (FoodSource) ((Pair) obj).getKey();
        Double value = (Double) ((Pair) obj).getValue() / sum;
        normalisedProbabilities.add(new Pair<>(fs, value));
      }
    }

    /*Find the correct index according to probabilities*/
    double randomDouble = apes.random.nextDouble();
    double choose = 0;
    int index = 0;

    for (int j = 0; j < normalisedProbabilities.size(); j++) {
      Object obj = normalisedProbabilities.get(j);
      if (obj instanceof Pair) {
        choose += (double) ((Pair) obj).getValue();
        if (randomDouble < choose) {
          index = j;
          break;
        }
      }
    }
    FoodSource returnFs = (FoodSource) ((Pair) normalisedProbabilities.get(index)).getKey();
    if (memoryFoodSources.size() >= Settings.gorillaMemoryLength)
      memoryFoodSources.removeNondestructively(0);
    memoryFoodSources.add(returnFs);

    returnFs.incrementHeat();
    return returnFs;
  }

  private double calculateProbabilityDistance(
      double gorillaX, double gorillaY, double foodSourceX, double foodSourceY) {
    double distance =
        Math.sqrt(Math.pow(foodSourceX - gorillaX, 2) + Math.pow(foodSourceY - gorillaY, 2));
    return 1 / distance;
  }

}
