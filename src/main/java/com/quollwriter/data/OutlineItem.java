package com.quollwriter.data;

import java.text.*;
import java.util.*;

import com.quollwriter.*;
import com.quollwriter.text.*;

public class OutlineItem extends ChapterItem
{

    public static final String OBJECT_TYPE = "outlineitem";

    protected Scene   scene = null;
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

    @Override
    public void dispose ()
    {

        this.scene = null;
        super.dispose ();

    }

    public void getChanges (NamedObject      old,
                            org.jdom.Element root)
    {

    }

    public Scene getScene ()
    {

        return this.scene;

    }

    public void setScene (Scene s)
    {

        if ((this.scene != null)
            &&
            (this.scene.equals (s))
           )
        {

            return;

        }

        if (s == null)
        {

            if (this.scene != null)
            {

                this.scene.removeOutlineItem (this);

            }

        }

        //s.addOutlineItem (this);

        this.scene = s;
        this.setParent (s);

    }

    @Override
    public void setDescription (StringWithMarkup d)
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

            String t = d.getText ();

            TextIterator ti = new TextIterator (t);

            if (ti.getSentenceCount () > 0)
            {

                this.setName (ti.getFirstSentence ().getText ());

            }

        }

        super.setDescription (d);

    }

    @Override
    public void fillToStringProperties (Map<String, Object> props)
    {

        super.fillToStringProperties (props);

        this.addToStringProperties (props,
                                    "scene",
                                    this.scene);

    }

}
