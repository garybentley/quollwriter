package com.quollwriter.ui.fx.swing;

import java.awt.*;


public interface PaintProvider
{

    public Paint getPaint (Component c);

    public PaintProvider getClone ();

}
