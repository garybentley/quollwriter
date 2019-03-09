package com.quollwriter.ui.components;

import java.awt.*;

import javax.swing.*;


public class ProgressBar extends Box
{

    private GradientPanel gp = null;

    public ProgressBar()
    {

        super (BoxLayout.X_AXIS);

        this.setMaximumSize (new Dimension (100,
                                            20));

        this.gp = new GradientPanel (new GradientPainter (UIUtils.getColor ("#516CA3"),
                                                          Color.RED));

        this.gp.setMaximumSize (new Dimension (0,
                                               20));

        this.add (this.gp);
        this.add (Box.createHorizontalGlue ());

    }

    public void setValue (int v)
    {

        this.gp.setPreferredSize (new Dimension (v,
                                                 20));

    }

}
