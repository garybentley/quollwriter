package com.quollwriter.ui.events;

import java.awt.event.*;


public class VetoableActionEvent extends ActionEvent
{

    private boolean cancelled = false;

    public VetoableActionEvent(Object source,
                               int    id,
                               String command)
    {

        super (source,
               id,
               command);

    }

    public boolean isCancelled ()
    {

        return this.cancelled;

    }

    public void cancel ()
    {

        this.cancelled = true;

    }

}
