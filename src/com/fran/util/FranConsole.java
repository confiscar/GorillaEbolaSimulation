package com.fran.util;

import com.fran.sim.Apes;
import com.fran.sim.ApesWithUI;
import sim.display.Console;

import java.io.IOException;

public class FranConsole extends Console {

  ApesWithUI apesUI;

  public FranConsole(ApesWithUI simulation) {
    super(simulation);
    this.apesUI = simulation;
  }

  @Override
  public void pressStop() {
    try {
      ((Apes) apesUI.state).recordPrinter.printToFile();
      ((Apes) apesUI.state).recordPrinter.reset();
    } catch (IOException e) {
      e.printStackTrace();
    }
    super.pressStop();
  }
}
