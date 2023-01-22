package com.quollwriter.data;

import org.dom4j.*;

public class Location extends LegacyAsset
{

    public static final String OBJECT_TYPE = "location";

    public Location ()
    {

        super (Location.OBJECT_TYPE);

    }

    @Override
    public void getChanges (NamedObject old,
                            Element     root)
    {

    }

}
