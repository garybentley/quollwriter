package com.quollwriter.data;

import java.util.*;

public class CollectionEvent<T>
{

    public enum Type
    {

        add,
        remove
    }

    private Type type = null;
    private T source = null;
    private Collection<? extends T> col = null;

    public CollectionEvent (Collection<? extends T>  col,
                            T    source,
                            Type type)
    {

        this.type = type;
        this.source = source;
        this.col = col;

    }

    public Type getType ()
    {

        return this.type;

    }

    public T getSource ()
    {

        return this.source;

    }

}
