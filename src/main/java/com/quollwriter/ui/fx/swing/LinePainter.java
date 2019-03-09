package com.quollwriter.ui.fx.swing;

import java.awt.*;

import javax.swing.text.*;


public class LinePainter extends DefaultHighlighter.DefaultHighlightPainter implements Highlighter.HighlightPainter
{

    private final BasicStroke DOTTED_LINE = new BasicStroke (1.3f,
                                                             BasicStroke.CAP_ROUND,
                                                             BasicStroke.JOIN_ROUND,
                                                             1f,
                                                             new float[] { 1f },
                                                             0f);

    private Color c = null;

    public LinePainter(Color c)
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
                                         Position.Bias.Backward,
                                         shape);

            Rectangle rect = (sh instanceof Rectangle) ? (Rectangle) sh : sh.getBounds ();

            int        x1 = rect.x;
            int        x2 = x1 + rect.width - 1;
            int        y = rect.y + rect.height - 1;
            Graphics2D g2 = (Graphics2D) g;

            g2.setStroke (this.DOTTED_LINE);
            g2.drawLine (x1,
                         y,
                         x2,
                         y);

            return rect;

        } catch (Exception e)
        {

        }

        return null;

    }

}
