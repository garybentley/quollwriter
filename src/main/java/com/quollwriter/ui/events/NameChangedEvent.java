package com.quollwriter.ui.events;

import java.util.EventObject;


public class NameChangedEvent extends EventObject
{

    private String name = null;

    public NameChangedEvent(Object source,
                            String name)
    {

        super (source);

        this.name = name;

    }

    public String getName ()
    {

        return this.name;

    }

}
