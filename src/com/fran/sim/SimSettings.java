package com.fran.sim;

import com.fran.util.IOHandler;
import com.fran.util.Stats;

import java.util.ArrayList;

/**
 * Settings class controls the static options that will affect the simulation generation/running.
 * Settings should be accessible from a static instance anywhere its imported.
 *
 * @author Francisco Caeiro
 */
public class SimSettings {
  /**
   * Helper variable that gets set automatically depending on the entry point used for the program
   */
  public static boolean useSimlab;

  /** Seed used for the run */
  public static int seed = 10000;
  /** SimLab sample file location used to set the factors every run */
  public static String inputFile =
      "D:\\Users\\Fran\\Documents\\Modules\\Dissertation\\Testing\\v2\\TransmissionProbabilityTesting\\20000Runs_NoDispersal\\samples.sam";
  /** Output file with the outputFactors used to analyse using SimLab */
  public static String outputFile =
      "D:\\Users\\Fran\\Documents\\Modules\\Dissertation\\Testing\\v2\\TransmissionProbabilityTesting\\20000Runs_NoDispersal\\output.txt";
  /** Amount of steps per run performed */
  public static int numberOfSteps = 364;
  /** Amount of total runs scheduled if program is run in Apes entry point */
  public static int numberOfRuns = 20000;
  /** Name of the factors outputted */
  public static String[] outputFactorNames = {"DeceasedRatio"};

  /** Hides unvisited food sources */
  public static boolean hideUnusedFoodSources = true;
  /** Enables or disables heat map of food sources */
  public static boolean enableHeatMap = true;
  /** Enables or disables the printing of interaction records */
  public static boolean enableRecordPrinting = true;

  public static ArrayList<Stats> statsFromRun = new ArrayList<>(numberOfRuns);

  public static void setFactors() {
    if (SimSettings.useSimlab) {
      ArrayList<Double> list = IOHandler.getNextRowOfFactors();
      // Use list.get() to get factors and associate them to the corresponding setting
      //SimParameters.infectionTime = (int) Math.round(list.get(0));
      SimParameters.transmissionProbability = list.get(0);
    }
  }
}
