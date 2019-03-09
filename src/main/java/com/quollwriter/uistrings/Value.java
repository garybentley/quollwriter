package com.quollwriter.uistrings;

import java.util.*;

public abstract class Value<E extends Value> extends Node<E>
{

    public Value (String id,
                  Node   parent)
    {

        super (id,
               parent);

    }

    public Value (String id,
                  Node   parent,
                  Map    data)
    {

        super (id,
               parent,
               data);

    }

    public boolean match (String text)
    {

        if (BaseStrings.toId (this.getId ()).startsWith (text))
        {

            return true;

        }

        return false;

    }

    public abstract Set<String> getErrors (RefValueProvider prov);

}
