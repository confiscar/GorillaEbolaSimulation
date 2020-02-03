package com.fran.sim;

import com.fran.util.BorderedOvalPortrayal2D;
import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.field.network.Edge;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.grid.DrawPolicy;
import sim.portrayal.grid.SparseGridPortrayal2D;
import sim.portrayal.network.NetworkPortrayal2D;
import sim.portrayal.network.SimpleEdgePortrayal2D;
import sim.portrayal.network.SpatialNetwork2D;
import sim.portrayal.simple.RectanglePortrayal2D;
import sim.util.Bag;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ApesWithUI is the entry point for the GUI. Running the main of this class will open up the
 * console and visualization of the simulation.
 *
 * @author Francisco Caeiro
 */
public class ApesWithUI extends GUIState {
  /** Represents the main model with apes and food sources + grid */
  private SparseGridPortrayal2D habitatPortrayal = new SparseGridPortrayal2D();
  /** Represents the edge of interactions between apes */
  private NetworkPortrayal2D interactionsPortrayal = new NetworkPortrayal2D();
  /** Main display object, can display multiple fields */
  private Display2D display;
  /** The frame that will encapsulate the display */
  private JFrame displayFrame;

  public ApesWithUI() {
    super(new Apes(System.currentTimeMillis()));
  }

  /** @param state SimState class used for adding portrayals to frame */
  public ApesWithUI(SimState state) {
    super(state);
  }

  /** Called when the play button is pressed, just before SimState.start() is called. */
  public void start() {
    super.start();
    setupPortrayals();
  }

  /**
   * Called when a simulation is loaded from a checkpoint.
   *
   * @param state Sets global state variable and reloads portrayals
   */
  public void load(SimState state) {
    super.load(state);
    setupPortrayals();
  }

  /**
   * Called when the GUI is initially created. Function created the JFrame window and ensures to
   * register the frame so the console can find it.
   *
   * @param c Controller class used to register Frames created
   */
  public void init(Controller c) {
    super.init(c);

    /*Creates our display for the actual simulation*/
    display = new Display2D(920, 920, this);
    display.setClipping(false);

    /*Creates JFrame, configures it and registers it with the console*/
    displayFrame = display.createFrame();
    displayFrame.setTitle("Apes Display");
    c.registerFrame(displayFrame);
    displayFrame.setVisible(true);

    /*Adds specifics views to our display*/
    display.attach(habitatPortrayal, "Habitat");
    display.attach(interactionsPortrayal, "Interactions");
  }

  /** Called when the GUI is about to be destroyed. */
  public void quit() {
    super.quit();
    if (displayFrame != null) displayFrame.dispose();
    displayFrame = null;
    display = null;
  }

  /** Initial setup for the visualization */
  private void setupPortrayals() {
    setupHabitatPortrayal();
    setInteractionsPortrayal();

    display.reset();
    display.setBackdrop(new Color(168, 221, 181));
    display.repaint();
  }

  private void setupHabitatPortrayal() {
    Apes apes = (Apes) state;

    DrawPolicy drawSmallerFirst =
        (bagIn, bagOut) -> {
          /*Stores the ordered apes*/
          Bag temp = new Bag();

          /*IDEs WILL COMPLAIN ABOUT THIS. BAG IMPLEMENTS COLLECTION BUT DOESN'T USE GENERICS. CALLS TO stream()
           * WILL RETURN A RAW TYPED STREAM, THEREFORE MANUAL CASTING HAS TO BE DONE. */

          /*Anything that's not an ape gets put in first. NOTE: ORDER NOT GUARANTEED*/
          Stream<Object> stream = bagIn.stream();
          bagOut.addAll(stream.filter(obj -> !(obj instanceof Ape)).collect(Collectors.toList()));

          /*Re-open stream and get all ape objects*/
          stream = bagIn.stream();
          temp.addAll(stream.filter(obj -> obj instanceof Ape).collect(Collectors.toList()));

          /*Compare and sort the apes by population count*/
          Comparator<Ape> comparator = Comparator.comparingInt(Ape::getPopulation);
          temp.sort(comparator);

          /*addAll() cant be used because bags are ordered in reverse*/
          int size = temp.size();
          for (int i = size - 1; i >= 0; i--) {
            bagOut.add(temp.get(i));
          }

          return true;
        };

    /*Draw smaller apes first, as ovals and give unique color using hashcode*/
    habitatPortrayal.setField(apes.habitat);
    habitatPortrayal.setDrawPolicy(drawSmallerFirst);
    habitatPortrayal.setPortrayalForClass(
        Ape.class,
        new BorderedOvalPortrayal2D() {
          @Override
          public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
            if (object instanceof Ape) {
              scale = (double) ((Ape) object).getPopulation() / (double) (Settings.maxPopulation);
              paint =
                  new Color(
                      255 - (object.hashCode() % 200),
                      255 - (object.hashCode() % 201),
                      255 - (object.hashCode() % 202));
              innerPaint = Color.green;
            }

            super.draw(object, graphics, info);
          }
        });

    /*Draw food sources as blue, and if heat map enabled slowly turn red*/
    habitatPortrayal.setPortrayalForClass(
        FoodSource.class,
        new RectanglePortrayal2D() {
          @Override
          public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
            if (object instanceof FoodSource) {
              if (!((FoodSource) object).visible && Settings.hideUnusedFoodSources) return;
              if (!((FoodSource) object).visitedByChimpanzees) {
                if (Settings.enableHeatMap) {
                  paint =
                      new Color(
                          (int) ((FoodSource) object).getHeat(),
                          0,
                          255 - (int) ((FoodSource) object).getHeat());
                } else paint = new Color(67, 162, 202);
              } else {
                paint = new Color(237, 192, 28);
              }
            }
            super.draw(object, graphics, info);
          }
        });

    /*Draw red border of active area, draw grid lines as thin white lines*/
    habitatPortrayal.setBorder(true);
    habitatPortrayal.setBorderColor(Color.red);
    habitatPortrayal.setGridModulus(1);
    habitatPortrayal.setGridLines(true);
    habitatPortrayal.setGridLineFraction(0.01);
    habitatPortrayal.setGridColor(Color.white);
  }

  private void setInteractionsPortrayal() {
    Apes apes = (Apes) state;

    /*Draw edges as blue and slowly turn red as more interactions happen*/
    interactionsPortrayal.setField(new SpatialNetwork2D(apes.habitat, apes.interactions));
    interactionsPortrayal.setPortrayalForAll(
        new SimpleEdgePortrayal2D() {
          @Override
          public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
            Integer intensity = (Integer) ((Edge) info.location).info;
            if (intensity > 255) intensity = 255;
            fromPaint = new Color(intensity, 0, 255 - intensity);
            toPaint = fromPaint;
            super.draw(object, graphics, info);
          }
        });
  }

  public static String getName() {
    return "Spread of Ebola Among Apes";
  }

  public static void main(String[] args) {
    ApesWithUI vid = new ApesWithUI();
    Console c = new Console(vid);
    c.setVisible(true);
  }
}
