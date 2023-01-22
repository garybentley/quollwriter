package com.quollwriter.ui;

import java.awt.Dimension;

import javax.swing.*;

import com.quollwriter.*;
import com.quollwriter.ui.components.QPopup;

public class ProgressPopup extends QPopup
{

    JProgressBar progress = null;

    public ProgressPopup (AbstractViewer viewer,
                          String         title,
                          String         icon,
                          String         text)
    {

        super (title,
               Environment.getIcon (icon,
                                    -1),
               null);

        this.progress = new JProgressBar ();
        this.progress.setPreferredSize (new Dimension (300,
                                                       20));
        this.progress.setMaximumSize (new Dimension (500,
                                                     20));
        this.progress.setAlignmentX (this.LEFT_ALIGNMENT);

        Box b = new Box (BoxLayout.Y_AXIS);

        b.add (UIUtils.createHelpTextPane (text,
                                           viewer));
        b.add (Box.createVerticalStrut (10));
        b.add (this.progress);

        b.setBorder (UIUtils.createPadding (10, 10, 10, 10));

        this.setContent (b);

    }

    public void update (int perc)
    {

        this.progress.setValue (perc);

    }

}
