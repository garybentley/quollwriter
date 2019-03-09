package com.quollwriter.data;

import java.util.*;

import org.jdom.*;


public class Link extends NamedObject
{

    public static final String OBJECT_TYPE = "link";

    private NamedObject obj1 = null;
    private NamedObject obj2 = null;

    Link()
    {

        super (Link.OBJECT_TYPE);

    }

    public Link(NamedObject obj1,
                NamedObject obj2)
    {

        super (Link.OBJECT_TYPE);

        this.setObject1 (obj1);
        this.setObject2 (obj2);

    }

    public void getChanges (NamedObject old,
                            Element     root)
    {

    }

    public Set<NamedObject> getAllNamedChildObjects ()
    {

        return new HashSet ();

    }

    public NamedObject getObject2 ()
    {

        return this.obj2;

    }

    public NamedObject getObject1 ()
    {

        return this.obj1;

    }

    public void setObject1 (NamedObject o)
    {

        this.obj1 = o;

    }

    public void setObject2 (NamedObject o)
    {

        this.obj2 = o;

    }

    public String toString ()
    {

        return Link.OBJECT_TYPE + "(obj1: " + this.obj1 + ", obj2: " + this.obj2 + ", id: " + this.getKey () + ")";

    }

    public NamedObject getOtherObject (NamedObject o)
    {

        if (this.obj1 == o)
        {

            return this.obj2;

        }

        if (this.obj2 == o)
        {

            return this.obj1;

        }

        return null;

    }

    public boolean equals (Object o)
    {

        if ((o == null) || (!(o instanceof Link)))
        {

            return false;

        }

        Link l = (Link) o;

        if (((this.obj1 == l.getObject1 ()) &&
             (this.obj2 == l.getObject2 ())) ||
            ((this.obj2 == l.getObject1 ()) &&
             (this.obj1 == l.getObject2 ())))
        {

            return true;

        }

        return false;

    }

    public int hashCode ()
    {

        int hash = 7;
        hash = (31 * hash) + ((null == this.obj1) ? 0 : this.obj1.hashCode ());
        hash = (31 * hash) + ((null == this.obj2) ? 0 : this.obj2.hashCode ());

        return hash;

    }

}
