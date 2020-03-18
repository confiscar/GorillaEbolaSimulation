package com.fran.sim;

import javafx.util.Pair;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.grid.SparseGrid2D;
import sim.field.network.Edge;
import sim.util.Bag;
import sim.util.Int2D;

import java.util.ArrayList;
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
  private int silverbackNumber;
  private int silverbackCounter;

  /** Integers representing the quantity of the gorillas (total, male, female) */
  private int populationCount;
  int susceptibleCount;
  int infectedCount;
  int recoveredCount;
  int deceasedCount;

  List<Integer> infectionTimer;

  private boolean hasSilverbackDied;
  private boolean groupInactive;

  /** Java Bean to display population */
  public int getPopulation() {
    return this.populationCount;
  }

  public int getSusceptible() {
    return this.susceptibleCount;
  }

  public int getInfected() {
    return this.infectedCount;
  }

  public int getRecovered() {
    return this.recoveredCount;
  }

  public int getDeceasedCount() {
    return this.deceasedCount;
  }

  /**
   * * Constructor takes the simState and an Int2D that represents the centre of the gorillas home
   * range (which will be a randomly chosen food source)
   */
  Ape(SimState simState, Int2D centerHomeRange) {
    Apes apes = (Apes) simState;
    hasSilverbackDied = false;
    groupInactive = false;
    infectionTimer = new ArrayList<>(populationCount);

    /*Creates bags that will contain locations of neighbouring food sources (one is for the memory)*/
    this.neighbourFoodSources = new Bag();
    this.memoryFoodSources = new Bag(SimParameters.gorillaMemoryLength);

    /*This will be the 'timer' for specific gorilla behaviour*/
    movementCounter = SimParameters.gorillaFoodWaitTime;

    /*Calculates population between set boundaries*/
    populationCount =
        apes.random.nextInt(SimParameters.maxPopulation - SimParameters.minPopulation)
            + SimParameters.minPopulation;

    /*Gets the moore neighbours (circle around the gorillas) */
    Bag allNeighbours =
        apes.habitat.getMooreNeighbors(
            centerHomeRange.x,
            centerHomeRange.y,
            SimParameters.homerangeRadius,
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

    silverbackNumber = simState.random.nextInt(populationCount);
    silverbackCounter = silverbackNumber;
  }

  /**
   * Called by the scheduler after each step. Used to update positions, weights and state of the
   * agents.
   */
  @Override
  public void step(SimState simState) {
    if (!groupInactive) {
      /*Get static simState instance and cast as our subclass to get functions and member vars*/
      Apes apes = (Apes) simState;
      SparseGrid2D habitat = apes.habitat;

      movementCounter--;

      /*If movementCounter runs out, search for new food source*/
      if (movementCounter <= 0) {
        /*Reset movement counter*/
        movementCounter = SimParameters.gorillaFoodWaitTime;
        /*Get new food source*/
        FoodSource fs = getNewFoodSource(simState);
        habitat.setObjectLocation(this, fs.location);
        infect(simState, infectedCount, SimParameters.transmissionProbability);

        if (fs.visitedByChimpanzees) {
          if (infect(simState, 1, fs.infectionProbability) > 0) {
            fs.infectedInCurrentStep = true;
            //System.out.print(FoodSource.infectionProbability);
          }
          fs.incrementInfectionProbability();
        }

        decreaseInfectionTimer(infectionTimer);
        checkForDeaths(simState, infectionTimer);

        if (hasSilverbackDied) {
          disperse(simState);
          if(populationCount > 0){
            hasSilverbackDied = false;
            silverbackNumber = simState.random.nextInt(populationCount);
            silverbackCounter = silverbackNumber;
          }
        }

        /*Updates the network of interactions*/
        updateNetwork(simState);

        if (populationCount <= 0) {
          groupInactive = true;
        }
      }
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

        int originalApeInfection = ((Ape) obj).infectedCount;
        ((Ape) obj).infect(simState, infectedCount, SimParameters.transmissionProbability);
        this.infect(simState, originalApeInfection, SimParameters.transmissionProbability);

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

    if (neighbourFoodSources.size() <= 1) {
      return (FoodSource) neighbourFoodSources.get(0);
    }

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
    if (memoryFoodSources.size() >= SimParameters.gorillaMemoryLength)
      memoryFoodSources.removeNondestructively(0);
    memoryFoodSources.add(returnFs);

    returnFs.incrementHeat();
    return returnFs;
  }

  private double calculateProbabilityDistance(double x1, double y1, double x2, double y2) {
    double distance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    return 1 / distance;
  }

  private int infect(SimState state, int numberOfInfectedSources, double transmissionProbability) {
    int infected = 0;

    for (int i = 0; i < numberOfInfectedSources && susceptibleCount > 0; i++) {
      for (int j = 0, k = susceptibleCount; j < k && susceptibleCount > 0; j++) {
        if (randomChoose(state, transmissionProbability)) {
          susceptibleCount--;
          infectedCount++;
          infectionTimer.add(SimParameters.infectionTime);
          infected++;
          ((Apes) state).stat.incrementTotalInfectedGorillas();
        }
      }
    }

    return infected;
  }

  private void checkForDeaths(SimState state, List<Integer> infections) {
    while (infections.remove(Integer.valueOf(0))) {
      infectedCount--;
      if (randomChoose(state, SimParameters.recoveryProbability)) {
        recoveredCount++;
        ((Apes) state).stat.incrementTotalRecoveredGorillas();
      } else {
        populationCount--;
        deceasedCount++;
        ((Apes) state).stat.incrementTotalDeceasedGorillas();
        silverbackCounter--;
        if (silverbackCounter == 0) {
          hasSilverbackDied = true;
        }
      }
    }
  }

  private void decreaseInfectionTimer(List<Integer> infections) {
    for (int i = 0; i < infections.size(); i++) {
      int originalValue = infections.get(i);
      infections.set(i, originalValue - 1);
    }
  }

  private boolean disperse(SimState state) {
    /*Get static simState instance and cast as our subclass to get functions and member vars*/
    Apes apes = (Apes) state;
    SparseGrid2D habitat = apes.habitat;

    Bag allObjects = habitat.getAllObjects();
    Bag currentApesProbabilities = new Bag(allObjects.size());
    Int2D me = habitat.getObjectLocation(this);
    double sum = 0;

    for (int i = 0; i < allObjects.size(); i++) {
      Object obj = allObjects.get(i);
      //If the object is an ape, not inactive and the probability of dispersal returns true
      if (obj != this && obj instanceof Ape && !((Ape) obj).groupInactive && randomChoose(state, SimParameters.probabilityOfDispersal)) {
        Int2D objLocation = habitat.getObjectLocation(obj);
        double probability = calculateProbabilityDistance(me.x, me.y, objLocation.x, objLocation.y);
        currentApesProbabilities.add(new Pair<>(obj, probability));
        sum += probability;
      }
    }

    if (currentApesProbabilities.isEmpty()) {
      return false;
    }

    /*Create a bag with FoodSource, Probability value pairs*/
    Bag normalisedProbabilities = new Bag(neighbourFoodSources.size());
    int size = currentApesProbabilities.size();
    for (int i = 0; i < size; i++) {
      Object obj = currentApesProbabilities.pop();
      if (obj instanceof Pair) {
        Ape ape = (Ape) ((Pair) obj).getKey();
        Double value = (Double) ((Pair) obj).getValue() / sum;
        normalisedProbabilities.add(new Pair<>(ape, value));
      }
    }

    Bag apeOrder = new Bag();

    for (int i = 0; i < populationCount; i++) {
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
            apeOrder.add(index);
            break;
          }
        }
      }
    }

    for (int i = 0; i < apeOrder.size(); i++) {
        Ape ape = (Ape) ((Pair) normalisedProbabilities.get((int) apeOrder.get(i))).getKey();
        populationCount--;
        ape.populationCount++;
        boolean chosen = false;
        do{
          double randomDouble = state.random.nextDouble();
          if (susceptibleCount != 0 && randomDouble <= 1.0 / 3) {
            ape.susceptibleCount++;
            susceptibleCount--;
            chosen = true;
          } else if(infectedCount != 0 && randomDouble <= (1.0 / 3) * 2 && randomDouble > 1.0 / 3) {
            ape.infectedCount++;
            ape.infectionTimer.add(this.infectionTimer.remove(0));
            infectedCount--;
            chosen = true;
          } else if(recoveredCount != 0 && randomDouble <= 1.0 && randomDouble > (1.0 / 3) * 2) {
            ape.recoveredCount++;
            recoveredCount--;
            chosen = true;
          }
        }while(!chosen);
    }

    return true;
  }

  private boolean randomChoose(SimState state, double probability) {
    double random = ((Apes) state).random.nextDouble();
    return random <= probability;
  }

}
