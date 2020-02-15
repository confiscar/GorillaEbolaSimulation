package com.fran.util;

import com.fran.sim.Ape;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.Buffer;
import java.util.ArrayList;

public class RecordPrinter {
  private ArrayList<String> interactionRecords;
  File file;
  String fileName;

  public RecordPrinter() {
    interactionRecords = new ArrayList<>();
    fileName = "test";
  }

  public void reset() {
    interactionRecords = new ArrayList<>();
  }

  public void addInteractionApe(Ape ape1, Ape ape2) {
    interactionRecords.add(Integer.toHexString(ape1.hashCode()) + " " + Integer.toHexString(ape2.hashCode()));
  }

  public void printToFile() throws IOException {
    if (!interactionRecords.isEmpty()) {
      BufferedWriter writer = new BufferedWriter(new FileWriter(fileName + ".txt"));
      for (String interactionRecord : interactionRecords) {
        writer.write(interactionRecord);
        writer.newLine();
      }
      writer.close();
      }
  }
}
