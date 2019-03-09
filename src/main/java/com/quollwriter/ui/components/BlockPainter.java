package com.quollwriter.ui.components;

import java.awt.*;

import javax.swing.*;
import javax.swing.text.*;


public class BlockPainter extends DefaultHighlighter.DefaultHighlightPainter
{

    private Color c = null;

    public BlockPainter(Color c)
    {

        super (c);

        this.c = c;

    }

    public Shape paintLayer (Graphics       g,
                             int            i,
                             int            j,
                             Shape          shape,
                             JTextComponent text,
                             View           view)
    {

        g.setColor (this.c);

        try
        {

            Shape sh = view.modelToView (i,
                                         Position.Bias.Forward,
                                         j,
                                         Position.Bias.Forward,
                                         shape);

            Rectangle rect = (sh instanceof Rectangle) ? (Rectangle) sh : sh.getBounds ();

            Graphics2D g2 = (Graphics2D) g;

            g2.fillRect (rect.x,
                         rect.y,
                         rect.width,
                         rect.height - 1);

            return rect;

        } catch (Exception e)
        {

        }

        return null;

    }

}
