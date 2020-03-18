package com.fran.util;

import com.fran.sim.SimParameters;

public class Stats {

  private int totalInitialPopulation;
  private int totalInitialGroups;
  private int totalFoodSources;
  private int foodSpreadArea;
  private double gorillaDensity;

  private int totalInfectedGorillas;
  private int totalRecoveredGorillas;
  private int totalDeceasedGorillas;

  public Stats() {
    this.totalInitialPopulation = 0;
    this.gorillaDensity = 0;
    this.totalInitialGroups = SimParameters.groupsOfGorillas;
    this.totalFoodSources = SimParameters.amountFoodSources;
    this.foodSpreadArea = SimParameters.amountFoodSources;
    this.totalInfectedGorillas = 0;
    this.totalRecoveredGorillas = 0;
    this.totalDeceasedGorillas = 0;
  }

  public Stats(int totalInitialPopulation) {
    this();
    this.totalInitialPopulation = totalInitialPopulation;
    calculateDensity();
  }

  public void addPopulation(int groupPopulation) {
    this.totalInitialPopulation += groupPopulation;
  }

  public void calculateDensity() {
    this.gorillaDensity =
        (double) totalInitialPopulation
            / Math.pow(
                (double) (((2 * SimParameters.foodSpreadingIntensity) + 1) * SimParameters.cellSideLength)
                    / 1000,
                2);
  }

  public void incrementTotalInfectedGorillas() {
    totalInfectedGorillas++;
  }

  public void incrementTotalRecoveredGorillas() {
    totalRecoveredGorillas++;
  }

  public void incrementTotalDeceasedGorillas() {
    totalDeceasedGorillas++;
  }

  public String stringHeader() {
    return "Init Population, Init Groups, n Food Sources, "
        + "Habitat Area, Gorillas Density (km^2), Total Infected, "
        + "Total Recovered, Total Deceased";
  }

  public String toString() {
    return "["
        + totalInitialPopulation
        + ", "
        + totalInitialGroups
        + ", "
        + totalFoodSources
        + ", "
        + foodSpreadArea
        + ", "
        + gorillaDensity
        + ", "
        + totalInfectedGorillas
        + ", "
        + totalRecoveredGorillas
        + ", "
        + totalDeceasedGorillas
        + ", "
        + "]";
  }

  public int getTotalInitialPopulation() {
    return totalInitialPopulation;
  }

  public int getTotalInitialGroups() {
    return totalInitialGroups;
  }

  public int getTotalFoodSources() {
    return totalFoodSources;
  }

  public int getFoodSpreadArea() {
    return foodSpreadArea;
  }

  public double getGorillaDensity() {
    return gorillaDensity;
  }

  public int getTotalInfectedGorillas() {
    return totalInfectedGorillas;
  }

  public int getTotalRecoveredGorillas() {
    return totalRecoveredGorillas;
  }

  public int getTotalDeceasedGorillas() {
    return totalDeceasedGorillas;
  }

  public double getRatioOfDeceased(){ return (double) totalDeceasedGorillas / (double) totalInitialPopulation;}
}
