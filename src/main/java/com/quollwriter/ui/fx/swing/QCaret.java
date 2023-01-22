package com.quollwriter.ui.fx.swing;

import java.awt.*;

import javax.swing.*;
import javax.swing.text.*;


/**
 * Taken from: http://www.java-tips.org/java-se-tips/javax.swing/how-to-implement-a-fancy-caret-for-your-applic.html
 */
public class QCaret extends DefaultCaret
{

    protected synchronized void damage (Rectangle r)
    {

        if (r == null)
        {
            return;
        }

        // give values to x,y,width,height (inherited from java.awt.Rectangle)
        x = r.x;
        y = r.y;
        height = r.height;

        // A value for width was probably set by paint(), which we leave alone.
        // But the first call to damage() precedes the first call to paint(), so
        // in this case we must be prepared to set a valid width, or else paint()
        // will receive a bogus clip area and caret will not get drawn properly.
        if (width <= 0)
        {
            width = getComponent ().getWidth ();
        }

        repaint (); // calls getComponent().repaint(x, y, width, height)
    }

    public void paint (Graphics g)
    {

        JTextComponent comp = getComponent ();

        if (comp == null)
        {
            return;
        }

        int       dot = getDot ();
        Rectangle r = null;
        char      dotChar;

        try
        {
            r = SwingUIUtils.getRectForOffset (comp, dot);

            if (r == null)
            {
                return;
            }

            dotChar = comp.getText (dot,
                                    1).charAt (0);
        } catch (BadLocationException e)
        {
            return;
        }

        if ((x != r.x) || (y != r.y))
        {

            // paint() has been called directly, without a previous call to
            // damage(), so do some cleanup. (This happens, for example, when the
            // text component is resized.)
            repaint (); // erase previous location of caret
            x = r.x; // Update dimensions (width gets set later in this method)
            y = r.y;
            height = r.height;
        }

        g.setColor (comp.getCaretColor ());
        g.setXORMode (comp.getBackground ()); // do this to draw in XOR mode

        if (this.isVisible ())
        {

            g.drawLine (x,
                        y,
                        x,
                        y + g.getFontMetrics ().getHeight ());

        }

    }

}
