package com.quollwriter.ui;

import javax.swing.event.*;


public class BackgroundChangeEvent extends ChangeEvent
{

    private Object value = null;

    public BackgroundChangeEvent(BackgroundSelector sel,
                                 Object             value)
    {

        super (sel);

        this.value = value;

    }

    public Object getValue ()
    {

        return this.value;

    }

}
