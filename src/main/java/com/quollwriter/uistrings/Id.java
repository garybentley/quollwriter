package com.quollwriter.uistrings;

import java.util.*;

import com.quollwriter.*;

public class Id
{

    private int start = -1;
    private boolean partial = false;
    private String id = null;
    private List<Part> parts = null;

    public Id (int     start,
               String  id,
               boolean partial)
    {

        this.start = start;
        this.id = id;
        this.partial = partial;

        java.util.List<String> parts = Utils.splitString (this.id,
                                                          BaseStrings.ID_PART_SEP);

        this.parts = new ArrayList<> ();

        int cind = start;

        Part prevp = null;

        for (int i = 0; i < parts.size (); i++)
        {

            if (i > 0)
            {

                cind++;

            }

            String ps = parts.get (i);

            if (ps.trim ().length () != ps.length ())
            {

                //this.hasErrors = true;

            }

            Part p = new Part (this,
                               cind,
                               ps,
                               prevp);

            prevp = p;
            cind += ps.length ();

            this.parts.add (p);

        }

    }

    public String getId ()
    {

        return this.id;

    }
/*
    public boolean isIdValid (LanguageStrings baseStrings)
    {

        return baseStrings.isIdValid (this.id);

    }
*/
    public boolean isPartial ()
    {

        return this.partial;

    }

    public int getStart ()
    {

        return this.start;

    }

    public int getEnd ()
    {

        return this.getStart () + this.id.length ();

    }

    public Part getLastPart ()
    {

        if (this.parts.size () == 0)
        {

            return null;

        }

        return this.parts.get (this.parts.size () - 1);

    }

    public Set<String> getPartMatches (int         offset,
                                       BaseStrings baseStrings)
    {

        Part p = this.getPart (offset);

        if (p != null)
        {

            return baseStrings.getIdMatches (p.getFullId ());

        }

        return this.getMatches (baseStrings);

    }

    public String toString ()
    {

        return "id[start=" + this.start + ",text=" + this.id + ",partial=" + this.partial + "]";

    }

    public Set<String> getMatches (BaseStrings baseStrings)
    {

        return baseStrings.getIdMatches (this.id);

    }

    public Part getPart (int offset)
    {

        for (int i = 0; i < this.parts.size (); i++)
        {

            Part p = this.parts.get (i);

            if ((offset >= p.start)
                &&
                (offset <= p.end)
               )
            {

                return p;

            }

        }

        return null;

    }

    public class Part
    {

        public int start = -1;
        public int end = -1;
        public String part = null;
        public Id parent = null;
        public Part previous = null;

        public Part (Id     parent,
                     int    start,
                     String part,
                     Part   prev)
        {

            this.start = start;
            this.end = this.start + part.length ();
            this.parent = parent;
            this.part = part;
            this.previous = prev;

        }

        public String getFullId ()
        {

            StringBuilder b = new StringBuilder (this.part);

            Part prev = this.previous;

            while (prev != null)
            {

                b.insert (0, prev.part + ".");
                prev = prev.previous;

            }

            return b.toString ();

        }

    }

}
