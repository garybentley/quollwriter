package com.quollwriter.data;

import java.text.*;

import javax.swing.text.*;


public class OutlineItem extends ChapterItem
{

    public static final String OBJECT_TYPE = "outlineitem";

    // private Scene scene = null;

    public OutlineItem()
    {

        super (OutlineItem.OBJECT_TYPE);

    }

    public OutlineItem(int     at,
                       Chapter c)
    {

        super (OutlineItem.OBJECT_TYPE,
               at,
               c);

    }

    public OutlineItem(int   at,
                       Scene s)
    {

        super (OutlineItem.OBJECT_TYPE,
               at,
               s.getChapter ());

        this.scene = s;

    }

    public void getChanges (NamedObject      old,
                            org.jdom.Element root)
    {

    }

/*
    public Scene getScene ()
    {

        return this.scene;

    }

    public void setScene (Scene s)
    {

        this.scene = s;

    }
*/
    /**
     * Overridden because of legacy data.
     */
    public void setName (String s)
    {

        this.setDescription (s);

    }

    public void setDescription (String d)
    {

        // Legacy data check, can't control the order of calls that hibernate makes so ensure that it
        // doesn't overwrite the description.
        if ((this.getName () != null) &&
            (d == null))
        {

            return;

        }

        if (d != null)
        {

            // Take the first sentence and use that as the "name".
            BreakIterator iter = BreakIterator.getSentenceInstance ();

            iter.setText (d);

            int f = iter.first ();

            if (f < 0)
            {

                return;

            }

            int n = iter.next ();

            if (n < 0)
            {

                return;

            }

            // Need to set the name to prevent a loop.
            super.setName (d.substring (f,
                                        n));

        }

        super.setDescription (d);

    }

}
