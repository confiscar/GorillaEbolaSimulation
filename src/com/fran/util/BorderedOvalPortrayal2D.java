package com.fran.util;

import sim.portrayal.DrawInfo2D;
import sim.portrayal.simple.OvalPortrayal2D;

import java.awt.*;
import java.awt.geom.Ellipse2D;

public class BorderedOvalPortrayal2D extends OvalPortrayal2D {

    public Paint innerPaint;
    public double offset;
    public transient Ellipse2D.Double preciseEllipse;
    public double borderSize = 0.1 * scale;

    public BorderedOvalPortrayal2D() {this(1.0D);}
    public BorderedOvalPortrayal2D(boolean filled){this(1.0D, filled);}
    public BorderedOvalPortrayal2D(double scale){this(scale, true);}
    public BorderedOvalPortrayal2D(Paint paint, Paint paintBorder){this(paint, paintBorder, 1.0D);}
    public BorderedOvalPortrayal2D(double scale, boolean filled){this(Color.gray, Color.black, scale, filled);}
    public BorderedOvalPortrayal2D(Paint paint, Paint paintBorder, double scale){this(paint, paintBorder, scale, true);}
    public BorderedOvalPortrayal2D(Paint paint, Paint paintBorder, boolean filled){this(paint, paintBorder, 1.0D, filled);}

    public BorderedOvalPortrayal2D(Paint paint, Paint paintBorder, double scale, boolean filled){
        super(paintBorder, scale, filled);
        this.innerPaint = paint;
        this.offset = 0.0D;
        this.preciseEllipse = new Ellipse2D.Double();
    }

    public void draw(Object object, Graphics2D graphics, DrawInfo2D info){
        super.draw(object, graphics, info);

        java.awt.geom.Rectangle2D.Double draw = info.draw;

        double width = draw.width * (this.scale - borderSize) + this.offset;
        double height = draw.height * (this.scale - borderSize) + this.offset;
        graphics.setPaint(this.innerPaint);
        if (info.precise) {
            if (this.preciseEllipse == null) {
                this.preciseEllipse = new Ellipse2D.Double();
            }

            this.preciseEllipse.setFrame(info.draw.x - width / 2.0D, info.draw.y - height / 2.0D, width, height);
            if (this.filled) {
                graphics.fill(this.preciseEllipse);
            } else {
                graphics.draw(this.preciseEllipse);
            }

        } else {
            int x = (int)(draw.x - width / 2.0D);
            int y = (int)(draw.y - height / 2.0D);
            int w = (int)width;
            int h = (int)height;
            if (this.filled) {
                graphics.fillOval(x, y, w, h);
            } else {
                graphics.drawOval(x, y, w, h);
            }
        }
    }

}
