package com.quollwriter.data;

import org.dom4j.*;

public class Location extends LegacyAsset
{

    public static final String OBJECT_TYPE = "location";

    public Location (UserConfigurableObjectType objType)
    {

        super (objType);

    }

    @Override
    public void getChanges (NamedObject old,
                            Element     root)
    {

    }

}
