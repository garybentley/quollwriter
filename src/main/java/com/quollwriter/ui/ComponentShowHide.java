package com.quollwriter.ui;

import java.awt.event.*;

import javax.swing.*;

import com.quollwriter.ui.components.ActionAdapter;

public class ComponentShowHide extends MouseAdapter 
{
 
    private ActionListener onShow = null;
    private ActionListener onHide = null;
    private Timer show = null;
    private Timer hide = null;
    
    public ComponentShowHide (ActionListener onShow,
                              ActionListener onHide)
    {

        this.show = new Timer (500,
                               onShow);
        this.hide = new Timer (750,
                               onHide);

    }
    
    private void stopTimers ()
    {

        this.hide.setRepeats (false);
        this.show.setRepeats (false);
        this.hide.stop ();
        this.show.stop ();

    }

    public void mouseEntered (MouseEvent ev)
    {

        this.stopTimers ();

        this.show.start ();

    }

    public void mouseExited (MouseEvent ev)
    {

        this.stopTimers ();

        this.hide.start ();

    }
    
}