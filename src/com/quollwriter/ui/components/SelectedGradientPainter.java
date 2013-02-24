package com.quollwriter.ui.components;

import java.awt.Color;


public class SelectedGradientPainter extends GradientPainter
{

    private Color selLeft = null;
    private Color selRight = null;
    private Color unselLeft = null;
    private Color unselRight = null;

    public SelectedGradientPainter(Color selLeft,
                                   Color selRight,
                                   Color unselLeft,
                                   Color unselRight)
    {

        super (selLeft,
               selRight);

        this.selLeft = selLeft;
        this.selRight = selRight;
        this.unselLeft = unselLeft;
        this.unselRight = unselRight;

    }

    public void setSelected (boolean v)
    {

        if (v)
        {

            this.setColors (this.selLeft,
                            this.selRight);

        } else
        {

            this.setColors (this.unselLeft,
                            this.unselRight);

        }

    }

    public boolean isSelected ()
    {

        return this.selLeft == this.left;

    }

}
