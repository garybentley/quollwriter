package com.quollwriter.ui.fx;

import java.util.*;

import com.quollwriter.data.*;

import org.jdom.*;


public class TreeParentNode extends NamedObject
{

    public static final String OBJECT_TYPE = "treeparentnode";

    private String forObjectType = null;
    private int count = -1;

    public TreeParentNode (String objType,
                           String name)
    {

        this (objType,
              name,
              -1);

    }

    public TreeParentNode(String objType,
                          String name,
                          int    count)
    {

        super (TreeParentNode.OBJECT_TYPE + "_" + objType);

        this.forObjectType = objType;

        this.setKey ((long) name.toLowerCase ().hashCode ());

        this.setName (name);

        this.count = count;

    }

    public void setCount (int c)
    {

        this.count = c;

    }

    public int getCount ()
    {

        return this.count;

    }

    public void getChanges (NamedObject old,
                            Element     root)
    {

    }

    public int hashCode ()
    {

        int hash = 7;
        hash = (31 * hash) + this.forObjectType.hashCode ();
        hash = (31 * hash) + this.getName ().toLowerCase ().hashCode ();

        return hash;

    }

    public String getForObjectType ()
    {

        return this.forObjectType;

    }

    public boolean equals (Object o)
    {

        if ((o == null) || (!(o instanceof TreeParentNode)))
        {


            return false;

        }

        TreeParentNode n = (TreeParentNode) o;

        if ((n.getName ().equalsIgnoreCase (this.getName ())) &&
            (n.forObjectType.equals (this.forObjectType)))
        {

            return true;

        }

        return false;

    }

    public String toString ()
    {

        return TreeParentNode.OBJECT_TYPE + "(for: " + this.forObjectType + ", type: " + this.getName () + ")";

    }

    public Set<NamedObject> getAllNamedChildObjects ()
    {

        return new HashSet<NamedObject> ();

    }

}
