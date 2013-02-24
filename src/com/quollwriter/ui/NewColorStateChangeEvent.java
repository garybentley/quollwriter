package com.quollwriter.ui;

import javax.swing.event.*;


public class NewColorStateChangeEvent extends ChangeEvent
{

    private String type = null;

    public NewColorStateChangeEvent(Object color,
                                    String type)
    {

        super (color);

        this.type = type;

    }

    public String getType ()
    {

        return this.type;

    }

}
