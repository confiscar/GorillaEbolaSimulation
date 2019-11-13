package com.fran;

import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.field.network.Edge;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.grid.SparseGridPortrayal2D;
import sim.portrayal.network.NetworkPortrayal2D;
import sim.portrayal.network.SimpleEdgePortrayal2D;
import sim.portrayal.network.SpatialNetwork2D;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.portrayal.simple.RectanglePortrayal2D;

import javax.swing.*;
import java.awt.*;

/**
 *  ApesWithUI is the entry point for the GUI. Running the main of this class will open up the console and visualization
 *  of the simulation.
 * @author Francisco Caeiro
 * */

public class ApesWithUI extends GUIState {

    SparseGridPortrayal2D habitatPortrayal = new SparseGridPortrayal2D(); //Single field portrayal
    NetworkPortrayal2D interactionsPortrayal = new NetworkPortrayal2D(); //Network of interactions portrayal
    public Display2D display; //Main display object, can display multiple fields
    public JFrame displayFrame; //The frame that will encapsulate

    /**
     * Constructor that creates the subclass of the SimState 'Apes'.
     */
    public ApesWithUI() { super(new Apes(System.currentTimeMillis())); }

    /**
     * Constructor that passes the SimState instead of creating it.
     * @param state
     */
    public ApesWithUI(SimState state) { super(state); }

    /**
     * Called when the play button is pressed, just before SimState.start() is called.
     */
    public void start(){
        super.start();
        setupPortrayals();
    }

    /**
     * Called when a simulation is loaded from a checkpoint.
     * @param state
     */
    public void load(SimState state){
        super.load(state);
        setupPortrayals();
    }

    /**
     * Called when the GUI is initially created. Function created the JFrame window and ensures to register the frame
     * so the console can find it.
     * @param c
     */
    public void init(Controller c){
        super.init(c);

        /*Creates our display for the actual simulation*/
        display = new Display2D(Settings.simulationWidth * 8, Settings.simulationHeight * 8, this);
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

    /**
     * Called when the GUI is about to be destroyed.
     */
    public void quit(){
        super.quit();
        if( displayFrame != null)
            displayFrame.dispose();
        displayFrame = null;
        display = null;
    }

    /**
     * Initial setup for the visualization
     */
    public void setupPortrayals(){
        Apes apes = (Apes) state;

        habitatPortrayal.setField(apes.habitat);
        habitatPortrayal.setPortrayalForClass(Ape.class, new OvalPortrayal2D());
    habitatPortrayal.setPortrayalForClass(
        FoodSource.class,
        new RectanglePortrayal2D() {
          @Override
          public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
            if (object instanceof FoodSource)
                if(!((FoodSource) object).visible && Settings.hideUnusedFoodSources)
                    return;
            paint = new Color(0, 0, 255);
            super.draw(object, graphics, info);
          }
        });

        interactionsPortrayal.setField(new SpatialNetwork2D(apes.habitat, apes.interactions));
        interactionsPortrayal.setPortrayalForAll(new SimpleEdgePortrayal2D(){
            @Override
            public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
                Integer intensity = (Integer) ((Edge) info.location).info;
                if(intensity > 255) intensity = 255;
                fromPaint = new Color(intensity, 0, 255 - intensity);
                toPaint = fromPaint;
                super.draw(object, graphics, info);
            }
        });

        habitatPortrayal.setBorder(true);
        habitatPortrayal.setBorderColor(Color.red);
        habitatPortrayal.setGridModulus(1);
        habitatPortrayal.setGridLines(true);
        habitatPortrayal.setGridLineFraction(0.01);
        habitatPortrayal.setGridColor(Color.white);

        display.reset();
        display.setBackdrop(Color.green);
        display.repaint();
    }

    public static String getName() { return "Spread of Ebola Among Apes"; }

    public static void main(String[] args) {
        ApesWithUI vid = new ApesWithUI();
        Console c= new Console(vid);
        c.setVisible(true);
    }


}
