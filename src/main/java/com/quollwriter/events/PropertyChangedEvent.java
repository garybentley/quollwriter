package com.quollwriter.events;

import java.util.EventObject;


public class PropertyChangedEvent extends EventObject
{

    private Object oldValue = null;
    private Object newValue = null;
    private String changeType = null;

    // TODO Change to take a JavaFX Property, have the property set a bean name.
    public PropertyChangedEvent(Object source,
                                String changeType,
                                Object oldValue,
                                Object newValue)
    {

        super (source);

        this.changeType = changeType;
        this.oldValue = oldValue;
        this.newValue = newValue;

    }

    public Object getNewValue ()
    {

        return this.newValue;

    }

    public Object getOldValue ()
    {

        return this.oldValue;

    }

    public String getChangeType ()
    {

        return this.changeType;

    }

}
