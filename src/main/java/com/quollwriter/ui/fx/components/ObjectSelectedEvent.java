package com.quollwriter.ui.fx.components;

import javafx.event.*;

public class ObjectSelectedEvent extends Event
{

    public static final EventType<ObjectSelectedEvent> SELECTED_EVENT = new EventType<> ("object.selected");

    private Object obj = null;

    public ObjectSelectedEvent (Object                         obj,
                                EventType<ObjectSelectedEvent> type)
    {

        super (type);

        this.obj = obj;

    }

    public Object getObject ()
    {

        return this.obj;

    }

}
