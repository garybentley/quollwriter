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

        Box box = new Box (BoxLayout.PAGE_AXIS);

        // Create the image panel.
        ImagePanel ip = new ImagePanel (image,
                                        null);
        ip.setOpaque (false);

        ip.setLayout (new FlowLayout (FlowLayout.CENTER,
                                      0,
                                      0));
        ip.add (box);

        box.add (Box.createVerticalStrut (95));

        this.progressBar = new JProgressBar ();
        this.progressBar.setForeground (UIUtils.getColor ("#aaaaaa"));
        this.progressBar.setBorder (new LineBorder (UIUtils.getColor ("#aaaaaa"), 1));
        this.progressBar.setBorderPainted (false);
        this.progressBar.setPreferredSize (new Dimension (image.getWidth (null) - 29,
                                                          this.progressBar.getPreferredSize ().height));

        box.add (this.progressBar);

        box.setOpaque (false);

        JComponent cp = (JComponent) this.getContentPane ();

        cp.add (ip);
        cp.setOpaque (false);

        this.getRootPane ().setOpaque (false);

        this.setSize (cp.getPreferredSize ());
        this.setPreferredSize (new Dimension (449,
                                              126));

        this.pack ();

        UIUtils.setCenterOfScreenLocation (this);

        this.setVisible (true);
        this.toFront ();

    }

    public void finish ()
    {

        this.setProgress (100);

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

    }

}
