package com.quollwriter.ui.components;

import java.awt.*;

public class ColorPainter implements PaintProvider
{

    protected Color color = null;

    public ColorPainter (Color color)
    {

        this.color = color;

    }

    public void setColor (Color color)
    {

        this.color = color;

    }

    public PaintProvider getClone ()
    {

        return new ColorPainter (this.color);

    }

    public Paint getPaint (Component c)
    {

        return this.color;

    }

}
