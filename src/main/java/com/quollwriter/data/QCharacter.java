package com.quollwriter.data;

import java.util.*;

import org.dom4j.*;


public class QCharacter extends LegacyAsset
{

    public static final String OBJECT_TYPE = "character";

    public QCharacter(UserConfigurableObjectType objType)
    {

        super (objType);

    }

    @Override
    public void getChanges (NamedObject old,
                            Element     root)
    {

    }

}
