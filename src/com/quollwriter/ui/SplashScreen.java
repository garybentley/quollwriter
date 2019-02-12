package com.quollwriter.ui;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import com.quollwriter.*;
import com.quollwriter.ui.components.*;

public class SplashScreen extends JWindow
{

    private JProgressBar progressBar = null;
    private ImagePanel   imageP = null;

    public SplashScreen(Image  image,
                        String initMessage,
                        Color  backgroundColor)
    {

        Box box = new Box (BoxLayout.Y_AXIS);

        box.setAlignmentX (Component.LEFT_ALIGNMENT);

        JLabel l = new JLabel (new ImageIcon (image));
        l.setAlignmentX (Component.LEFT_ALIGNMENT);
        box.add (l);

        this.progressBar = new JProgressBar ();
        this.progressBar.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.progressBar.setForeground (UIUtils.getColor ("#4d4d4f"));
        this.progressBar.setBorder (null);
        this.progressBar.setPreferredSize (new Dimension (image.getWidth (null) - 2,
                                                          this.progressBar.getPreferredSize ().height));

        box.add (this.progressBar);

        box.setOpaque (false);
        box.setBorder (UIUtils.createLineBorder ());
        box.setBorder (new MatteBorder (1, 1, 1, 1, UIUtils.getColor ("#4d4d4f")));

        JComponent cp = (JComponent) this.getContentPane ();

        cp.add (box);//ip);
        cp.setOpaque (false);

        this.getRootPane ().setOpaque (false);

        this.setSize (cp.getPreferredSize ());
        this.pack ();

        UIUtils.setCenterOfScreenLocation (this);

        this.setVisible (true);
        this.toFront ();

        Environment.startupProgressProperty ().addListener ((p, oldv, newv) ->
        {

            this.setProgress (newv.intValue ());

        });

    }

    public void finish ()
    {

        this.progressBar.setValue (100);

        try
        {

            Thread.sleep (500);

        } catch (Exception e)
        {

            // Ignore

        }

        this.setVisible (false);
        this.dispose ();

    }

    public void incr (int v)
    {

        int pv = this.progressBar.getValue ();
        int nv = pv + v;

        if (nv >= 100)
        {

            this.finish ();

            return;

        }

        this.setProgress (nv);

    }

    public void setProgress (int v)
    {

        this.progressBar.setValue (v);

        if (v >= 100)
        {

            this.finish ();

        }

    }

}
