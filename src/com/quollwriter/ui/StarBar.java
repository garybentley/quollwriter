package com.quollwriter.ui;

import java.awt.event.*;

import java.util.*;

import javax.swing.*;

import com.quollwriter.*;
import com.quollwriter.events.*;

public class StarBar extends JToolBar
{

    public static final String RATING = StarBar.class.getName () + "/rating";

    private class StarBarButtonListener extends MouseEventHandler
    {

        public int      level = -1;
        private StarBar starBar = null;
        public boolean  selected = false;

        public StarBarButtonListener(int     level,
                                     StarBar starBar)
        {

            this.level = level;
            this.starBar = starBar;

        }

        public void mouseEntered (MouseEvent ev)
        {

            for (int i = 0; i < this.starBar.buttons.size (); i++)
            {

                this.starBar.buttons.get (i).setEnabled (i < this.level);

            }

        }

        public void mouseExited (MouseEvent ev)
        {

            for (int i = 0; i < this.level; i++)
            {

                this.starBar.buttons.get (i).setEnabled (false);

            }

            if (this.starBar.selected > -1)
            {

                for (int i = 0; i < this.starBar.selected; i++)
                {

                    this.starBar.buttons.get (i).setEnabled (true);

                }

            }

        }

        public void handlePress (MouseEvent ev)
        {

            if (this.starBar.selected == this.level)
            {

                this.starBar.setSelected (-1);

            } else
            {

                this.starBar.setSelected (this.level);

            }

        }

    }

    private List<JButton> buttons = new ArrayList ();
    private MouseAdapter  mouseListener = null;
    private int           selected = -1;

    public StarBar()
    {

        this (5,
              null);

    }

    public StarBar(int    maxLevel,
                   String iconType)
    {
    
        this.setOpaque (false);
        this.setFloatable (false);

        for (int i = 0; i < maxLevel; i++)
        {

            JButton b = new JButton (Environment.getIcon (((iconType == null) ? "star" : iconType),
                                                          Constants.ICON_MENU));

            b.setDisabledIcon (Environment.getIcon (((iconType == null) ? "star-disabled" : iconType),
                                                    Constants.ICON_MENU));
            b.setEnabled (false);
            b.setOpaque (false);
            UIUtils.setAsButton (b);

            this.add (b);

            this.buttons.add (b);

            this.addMouseListener (b,
                                   i + 1);

        }

    }

    public void setSelected (int selected)
    {

        for (int i = 0; i < this.buttons.size (); i++)
        {

            this.buttons.get (i).setEnabled (i < selected);

        }

        int old = this.selected;

        this.selected = selected;

        this.firePropertyChange (StarBar.RATING,
                                 old,
                                 this.selected);

    }

    private void addMouseListener (JButton b,
                                   int     level)
    {

        b.addMouseListener (new StarBarButtonListener (level,
                                                       this));

    }

}
