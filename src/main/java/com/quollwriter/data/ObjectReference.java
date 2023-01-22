package com.quollwriter.data;

import java.io.*;

import java.util.StringTokenizer;


public class ObjectReference extends DataObject
{

    private ObjectReference child = null;
    // private Long key = null;

    public ObjectReference(String          objType,
                           Long            key,
                           ObjectReference parent)
    {

        super (objType);

        this.setKey (key);

        // this.key = key;

        this.setParent (parent);

        if (parent != null)
        {

            parent.setChild (this);

        }

    }

    public static ObjectReference parseObjectReference (String ref)
    {

        // Object references.
        ObjectReference r = null;

        // Split on -
        int ind = ref.indexOf ("-");

        if (ind != -1)
        {

            String objType = ref.substring (0,
                                            ind);
            String id = ref.substring (ind + 1);

            ObjectReference cor = new ObjectReference (objType,
                                                       Long.parseLong (id),
                                                       r);

            r = cor;

        }
        
        /*
         *OLD, why did we split on :  Makes no sense since the last item would become the reference...
         
        // Split on :
        StringTokenizer tt = new StringTokenizer (ref,
                                                  ":");

        while (tt.hasMoreTokens ())
        {

            String ttok = tt.nextToken ().trim ();

            // Split on -
            int ind = ttok.indexOf ("-");

            if (ind != -1)
            {

                String objType = ttok.substring (0,
                                                 ind);
                String id = ttok.substring (ind + 1);

                ObjectReference cor = new ObjectReference (objType,
                                                           Long.parseLong (id),
                                                           r);

                r = cor;

            }

        }
*/
        return r;

    }

/*
    public Long getKey ()
    {

        return this.key;

    }
*/
    public DataObject getObjectForReference (ObjectReference r)
    {

        return null;

    }

    /*
    public ObjectReference getTopLevel ()
    {

    ObjectReference p = this;

    while (true)
    {

        if (p.parent == null)
        {

            return p;

        }

        p = p.getParent ();

    }

    }
     */
    public ObjectReference getChild ()
    {

        return this.child;

    }

    public void setChild (ObjectReference c)
    {

        this.child = c;

    }

    public ObjectReference getParent ()
    {

        return (ObjectReference) this.parent;

    }

    public String toString ()
    {

        return "object-ref(" + this.getObjectType () + ", key: " + this.key + ", child: " + this.child + ")";

    }

    public String asString ()
    {

        String t = this.getObjectType () + "-" + this.key;

        if (this.child != null)
        {

            t = t + ":" + this.child.asString ();

        }

        return t;

    }

}
