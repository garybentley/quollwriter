package com.quollwriter.data;

import java.util.*;


public class NamedObjectNameWrapper implements Comparable<NamedObjectNameWrapper>
{

    public NamedObject namedObject = null;
    public String      name = null;

    public NamedObjectNameWrapper(String      name,
                                  NamedObject n)
    {

        this.namedObject = n;
        this.name = name;

    }

    public static void addTo (NamedObject                               n,
                              Map<String, List<NamedObjectNameWrapper>> m)
    {

        List<NamedObjectNameWrapper> items = NamedObjectNameWrapper.getForAllNames (n);

        for (NamedObjectNameWrapper i : items)
        {

            List<NamedObjectNameWrapper> nitems = m.get (i.name.toLowerCase ());

            if (nitems == null)
            {

                nitems = new ArrayList ();

                m.put (i.name.toLowerCase (),
                       nitems);

            }

            nitems.add (i);

        }

    }

    public static List<NamedObjectNameWrapper> getForAllNames (NamedObject n)
    {

        List<NamedObjectNameWrapper> ret = new ArrayList ();

        for (String name : n.getAllNames ())
        {

            if (name != null)
            {
        
                ret.add (new NamedObjectNameWrapper (name,
                                                     n));

            }
                                                     
        }

        return ret;

    }

    public int compareTo (NamedObjectNameWrapper n)
    {

        return this.name.compareTo (n.name);

    }

}
