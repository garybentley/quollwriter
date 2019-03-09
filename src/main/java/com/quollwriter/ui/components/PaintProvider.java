package com.quollwriter.ui.components;

import java.awt.*;


public interface PaintProvider
{

    public Paint getPaint (Component c);

    public PaintProvider getClone ();

}
