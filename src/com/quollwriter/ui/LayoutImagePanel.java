package com.quollwriter.ui;

import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import javax.swing.*;

import com.quollwriter.data.*;
import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public class LayoutImagePanel extends JPanel implements Icon
{

    private int xoffset = 0;
    private int yoffset = 0;
    private List<String> parts = new ArrayList<> ();

    public LayoutImagePanel (String type)
    {

        if (type.startsWith ("layout-"))
        {

            type = type.substring ("layout-".length ());

        }

        StringTokenizer t = new StringTokenizer (type,
                                                 "-");

        while (t.hasMoreTokens ())
        {

            this.parts.add (t.nextToken ());

        }

        this.setBorder (UIUtils.createLineBorder ());

    }

    @Override
    public int getIconWidth ()
    {

        return 120;

    }

    @Override
    public int getIconHeight ()
    {

        return 60;

    }

    @Override
    public void paintIcon (Component c,
                           Graphics  g,
                           int       x,
                           int       y)
    {

        this.xoffset = x;
        this.yoffset = y;

        this.paintComponent (g);

    }

    @Override
    public Dimension getPreferredSize ()
    {

        return new Dimension (120, 60);

    }


    @Override
    public void paintComponent (Graphics g)
    {

        super.paintComponent (g);

        Graphics2D g2 = (Graphics2D) g;

        Font font = new Font ("Segoe UI", Font.BOLD, 12);

        int x = 0;
        int y = 0;
        int h = 60;

        Rectangle r = new Rectangle (x, y, 0, h);

        for (String p : this.parts)
        {

            if (p.equals ("ch"))
            {

                int w = 60;

                if (this.parts.size () == 2)
                {

                    w = 90;

                }

                r.width = w;

                g2.setColor (Color.white);

                g2.fillRect (r.x + this.xoffset, r.y + this.yoffset, r.width, r.height);

                g2.setColor (Color.black);

                this.drawString (g2,
                                 getUIString (options,lookandsound,labels,tabs),
                                 r,
                                 0,
                                 font);

                r.x += w;

            }

            if (p.equals ("ps"))
            {

                // Draw a project
                int w = 30;

                r.width = w;

                g2.setColor (UIUtils.hexToColor ("#C07016"));
                g2.fillRect (r.x + this.xoffset, r.y + this.yoffset, r.width, r.height);

                g2.setColor (Color.white);

                this.drawString (g2,
                                 getUIString (objectnames,singular, Project.OBJECT_TYPE),
                                 r,
                                 Math.PI/2,
                                 font);

                r.x += w;

            }

            if (p.equals ("os"))
            {

                // Draw a other
                int w = 30;

                r.width = w;

                g2.setColor (UIUtils.hexToColor ("#1732AE"));
                g2.fillRect (r.x + this.xoffset, r.y + this.yoffset, r.width, r.height);

                g2.setColor (Color.white);

                this.drawString (g2,
                                 getUIString (options,lookandsound,labels,other),
                                 r,
                                 Math.PI/2,
                                 font);

                r.x += w;

            }

        }

        g2.setColor (Color.black);
        g2.drawRect (0 + this.xoffset, 0 + this.yoffset, 119, 59);

        this.xoffset = 0;
        this.yoffset = 0;

    }

    private void drawString (Graphics2D g2,
                             String     text,
                             Rectangle  rect,
                             double     rotation,
                             Font       font)
    {

        AffineTransform trans = g2.getTransform ();

        RenderingHints rh = new RenderingHints (RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHints (rh);

        FontMetrics metrics = g2.getFontMetrics (font);
        LineMetrics lmetrics = metrics.getLineMetrics (text, g2);

        int x = rect.x + ((rect.width - metrics.stringWidth (text)) /2) + this.xoffset;
        int y = rect.y + ((rect.height - metrics.getHeight ()) / 2) + metrics.getAscent () + this.yoffset;

        if (rotation != 0)
        {

            y = -1 * (rect.x + ((rect.width - (int) lmetrics.getHeight () + - metrics.getDescent () + metrics.getAscent ()) / 2) + this.xoffset);
            x = rect.y + ((rect.height - metrics.stringWidth (text)) / 2) + this.yoffset;

        }

        g2.setFont (font);
        g2.rotate (rotation);
        g2.drawString (text, x, y);

        g2.setTransform (trans);

    }

}
