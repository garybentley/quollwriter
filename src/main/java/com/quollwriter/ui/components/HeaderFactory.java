package com.quollwriter.ui.components;

import java.awt.*;

import javax.swing.*;


public class HeaderFactory
{

    private Color         gradientLeftColor = null;
    private Color         gradientRightColor = null;
    private PaintProvider paintProvider = null;
    private Font          font = null;
    private Color         titleColor = null;
    private Insets        padding = null;
    private boolean       isGradientLeftColorSet = false;
    private boolean       isGradientRightColorSet = false;

    public Header getHeader ()
    {

        Header h = new Header ();
        h.setTitleColor (this.titleColor);
        h.setFont (this.font);
        h.setPadding (((this.padding == null) ? Header.defaultPadding : this.padding));

        // Create a new
        if (this.paintProvider != null)
        {

            h.setPaintProvider (this.paintProvider);

        } else
        {

            h.setPaintProvider (new GradientPainter ((this.isGradientLeftColorSet ? this.gradientLeftColor : Header.defaultPaintLeftColor),
                                                     (this.isGradientRightColorSet ? this.gradientRightColor : Header.defaultPaintRightColor)));

        }
        
        return h;

    }

    public Header getHeader (String title,
                             Icon   icon)
    {

        Header h = this.getHeader ();
        h.setTitle (title);
        h.setIcon (icon);

        return h;

    }

    public void setPaintProvider (PaintProvider p)
    {

        this.paintProvider = p.getClone ();

    }

    public void setGradientLeftColor (Color c)
    {

        this.isGradientLeftColorSet = true;
        this.gradientLeftColor = UIUtils.cloneColor (c);

    }

    public void setGradientRightColor (Color c)
    {

        this.isGradientRightColorSet = true;
        this.gradientRightColor = UIUtils.cloneColor (c);

    }

    public void setPadding (Insets p)
    {

        if (p == null)
        {

            p = new Insets (0,
                            0,
                            0,
                            0);

        }

        this.padding = (Insets) p.clone ();

    }

    public void setTitleColor (Color c)
    {

        if (c == null)
        {

            c = Header.defaultTitleColor;

        }

        this.titleColor = UIUtils.cloneColor (c);

    }

    public void setFont (Font f)
    {

        if (f == null)
        {

            JPanel p = new JPanel ();
            f = p.getFont ().deriveFont (Font.BOLD,
                                         14);

            // f = Header.defaultFont;

        }

        this.font = UIUtils.cloneFont (f);

    }

}
