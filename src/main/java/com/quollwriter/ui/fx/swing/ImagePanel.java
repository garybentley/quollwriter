package com.quollwriter.ui.fx.swing;

import java.awt.*;

import javax.swing.*;

public class ImagePanel extends JLabel //JPanel
{

    //private Image display = null;
    private Image img = null;
    private Image hideImage = null;

    public ImagePanel (String img,
                       String transparentImg)
    {

        this (new ImageIcon (img).getImage (),
              new ImageIcon (transparentImg).getImage ());

    }

    public ImagePanel (ImageIcon img,
                       ImageIcon transparentImg)
    {

        this (img.getImage (),
              (transparentImg == null ? null : transparentImg.getImage ()));

    }

    public ImagePanel (Image img,
                       Image transparentImg)
    {

        this.setImage (img);

        this.hideImage = transparentImg;

    }
/*
    public void setIcon (ImageIcon i)
    {

        super.setIcon (i);

        //this.img = i.getImage ();
        //this.setImage (i.getImage ());

    }
*/
    public void setImage (Image i)
    {

        this.img = i;

        if (this.img != null)
        {

            this.setIcon (new ImageIcon (i));
            /*
            Dimension size = new Dimension (this.img.getWidth (null),
                                            this.img.getHeight (null));
            this.setOpaque (false);
            this.setPreferredSize (size);
            this.setMinimumSize (size);
            this.setMaximumSize (size);
            this.setSize (size);
            this.setLayout (null);
*/
        }

    }

    public void showImage (boolean v)
    {

        if (v)
        {

            this.setImage (this.img);

            this.setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));

        } else {

            this.setIcon (new ImageIcon (this.hideImage));

            this.setCursor (Cursor.getPredefinedCursor (Cursor.DEFAULT_CURSOR));

        }

        // Force a repaint to ensure that the image is displayed.
        this.repaint ();

    }

}
