package com.quollwriter.uistrings;

import java.util.*;

public class ImageValue extends Value<ImageValue>
{

    private String comment = null;

    public ImageValue (String id,
                       Node   parent,
                       Map    imgData)
    {

        super (id,
               parent,
               imgData);

    }

    @Override
    public Set<String> getErrors (RefValueProvider prov)
    {

        return null;

    }

    @Override
    public Node cloneNode ()
    {

        String comment = this.comment;

        ImageValue n = new ImageValue (this.id,
                                       null,
                                       null);

        n.comment = comment;

        return n;

    }

    @Override
    public String toString ()
    {

        return (this.getId () + "(image,comment=" + this.comment + ")");

    }

    @Override
    public Set<Value> getAllValues ()
    {

        return new LinkedHashSet<> ();

    }

}
