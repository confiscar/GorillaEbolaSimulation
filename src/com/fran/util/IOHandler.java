package com.fran.util;

import com.fran.sim.Settings;

import java.io.*;
import java.util.ArrayList;

public class IOHandler {

  public static File input;
  public static File output;
  static final int skip = 4;

  public static ArrayList<ArrayList<Double>> factors = new ArrayList<>(Settings.numberOfRuns);
  public static String[] factorNames = {"DeceasedRatio"};
  public static int counter = 0;

  static public void writeHeader() throws IOException {
    if (!output.exists()) {
      output.createNewFile();
    }

    FileWriter fWriter = new FileWriter(output, true);
    BufferedWriter bWriter = new BufferedWriter(fWriter);
    bWriter.write("1");
    bWriter.newLine();
    for(String s : factorNames){
      bWriter.write(s);
      bWriter.newLine();
    }
    bWriter.write("time = no");
    bWriter.newLine();
    bWriter.write("" + Settings.numberOfRuns);
    bWriter.newLine();

    bWriter.close();
    fWriter.close();
  }

  static public void write(String s) throws IOException {
    FileWriter fWriter = new FileWriter(output, true);
    BufferedWriter bWriter = new BufferedWriter(fWriter);
    bWriter.write(s);
    bWriter.newLine();

    bWriter.close();
    fWriter.close();
  }

  static public void read() throws IOException {
    FileReader fReader = new FileReader(input);
    BufferedReader bReader = new BufferedReader(fReader);

    String line;
    for (int i = 0; i < skip; i++) {
      line = bReader.readLine();
    }

    for (int i = 0; i < Settings.numberOfRuns; i++) {
      line = bReader.readLine();
      String[] split = line.split("\t");
      ArrayList<Double> list = new ArrayList<>();
      for (String s : split) {
        list.add(Double.parseDouble(s));
      }
      factors.add(list);
    }

    bReader.close();
    fReader.close();
  }
}
