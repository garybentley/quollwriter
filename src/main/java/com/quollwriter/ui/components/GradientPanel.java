package com.quollwriter.ui.components;

import java.awt.*;

import javax.swing.*;


public class GradientPanel extends JPanel
{

    public static Color   defaultPaintLeftColor = UIUtils.getColor ("#516CA3");
    public static Color   defaultPaintRightColor = null;
    private PaintProvider paintProvider = null;

    public GradientPanel()
    {

        this.paintProvider = new GradientPainter (GradientPanel.defaultPaintLeftColor,
                                                  GradientPanel.defaultPaintRightColor);

    }

    public GradientPanel(PaintProvider pp)
    {

        this.paintProvider = pp;

    }

    public PaintProvider getPaintProvider ()
    {

        return this.paintProvider;

    }

    public void setPaintProvider (PaintProvider p)
    {

        this.paintProvider = p;

    }

    protected void paintComponent (Graphics g)
    {

        if (this.paintProvider != null)
        {

            Paint p = this.paintProvider.getPaint (this);

            Graphics2D g2d = (Graphics2D) g;

            int w = this.getWidth ();
            int h = this.getHeight ();

            g2d.setPaint (p);
            g2d.fillRect (0,
                          0,
                          w,
                          h);

            this.setOpaque (false);

        }

        super.paintComponent (g);

        this.setOpaque (true);

    }

}
