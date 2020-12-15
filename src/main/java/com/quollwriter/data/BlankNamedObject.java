package com.quollwriter.data;

import java.util.*;
import javafx.beans.property.*;

import org.dom4j.*;


public class BlankNamedObject extends NamedObject
{

    public BlankNamedObject()
    {

        super (null);

    }

    public BlankNamedObject (String objType)
    {

        super (objType);

    }

    public BlankNamedObject(String objType,
                            String name)
    {

        super (objType,
               name);

    }

    @Override
    public void getChanges (NamedObject old,
                            Element     root)
    {

    }

    @Override
    public void setObjectType (String t)
    {

        super.setObjectType (t);

    }

    @Override
    public Set<NamedObject> getAllNamedChildObjects ()
    {

        return new HashSet<> ();

    }

}
