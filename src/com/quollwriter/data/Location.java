package com.quollwriter.data;

import org.jdom.*;


public class Location extends Asset
{

    public static final String OBJECT_TYPE = "location";

    public Location()
    {

        super (Location.OBJECT_TYPE);

    }

    public void getChanges (NamedObject old,
                            Element     root)
    {

    }

}
