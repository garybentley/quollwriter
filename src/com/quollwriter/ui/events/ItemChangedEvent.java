package com.quollwriter.ui.events;

import java.util.EventObject;


public class ItemChangedEvent extends EventObject
{

    private String typeOfChange = null;
    private Object changed = null;

    public ItemChangedEvent(Object source,
                            Object changed,
                            String typeOfChange)
    {

        super (source);

        this.changed = changed;
        this.typeOfChange = typeOfChange;

    }

    public Object getChangedObject ()
    {

        return this.changed;

    }

    public String getTypeOfChange ()
    {

        return this.typeOfChange;

    }

}
