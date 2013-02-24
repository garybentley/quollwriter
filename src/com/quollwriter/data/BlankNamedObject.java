package com.quollwriter.data;

import java.util.*;

import org.jdom.*;


public class BlankNamedObject extends NamedObject
{

    public BlankNamedObject()
    {

        super (null);

    }

    public BlankNamedObject(String objType,
                            String name)
    {

        super (objType,
               name);

    }

    public void getChanges (NamedObject old,
                            Element     root)
    {

    }

    public void setObjectType (String t)
    {

        super.setObjectType (t);

    }

    public Set<NamedObject> getAllNamedChildObjects ()
    {

        return new HashSet ();

    }

}
