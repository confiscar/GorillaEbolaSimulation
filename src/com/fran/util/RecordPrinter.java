package com.fran.util;

import com.fran.sim.Ape;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class RecordPrinter {
  private ArrayList<String> interactionRecords;
  File file;

  public RecordPrinter() {
    interactionRecords = new ArrayList<>();
  }

  public void reset() {
    interactionRecords = new ArrayList<>();
  }

  public void addInteractionApe(Ape ape1, Ape ape2) {
    interactionRecords.add(ape1.hashCode() + " " + ape2.hashCode());
  }

  public void printToFile() {
  }
}
