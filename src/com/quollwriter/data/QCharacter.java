package com.quollwriter.data;

import java.util.*;

import org.jdom.*;


public class QCharacter extends Asset
{

    public static final String OBJECT_TYPE = "character";

    protected QCharacter()
    {

        super (QCharacter.OBJECT_TYPE);

    }

    public void getChanges (NamedObject old,
                            Element     root)
    {

    }

}
