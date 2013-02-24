package com.quollwriter.data;

import java.util.*;

import javax.swing.text.Position;

import com.quollwriter.data.comparators.*;

public abstract class ChapterItem extends NamedObject
{

    private int       position = -1;
    private int       endPosition = -1;
    private Position  textPos = null;
    private Position  endTextPos = null;
    protected Chapter chapter = null;
    protected Scene   scene = null;

    public static Set<? extends ChapterItem> getEmptyChapterItemSet ()
    {
        
        return new TreeSet (new ChapterItemSorter ());
        
    }

    protected ChapterItem(String objType)
    {

        super (objType);

    }

    protected ChapterItem(String  objType,
                          int     at,
                          Chapter c)
    {

        this (objType);

        this.position = at;
        this.chapter = c;

    }

    protected ChapterItem(String  objType,
                          int     start,
                          int     end,
                          Chapter c)
    {

        this (objType,
              start,
              c);

        this.endPosition = end;

    }

    public Set<NamedObject> getAllNamedChildObjects ()
    {

        return new HashSet (this.getNotes ());

    }

    public Chapter getChapter ()
    {

        return this.chapter;

    }

    public void setChapter (Chapter c)
    {

        this.chapter = c;

    }

    public Scene getScene ()
    {

        return this.scene;

    }

    public void setScene (Scene s)
    {

        this.scene = s;

    }

    public void setTextPosition (Position t)
    {

        this.textPos = t;

    }

    public void setEndTextPosition (Position t)
    {

        this.endTextPos = t;

    }

    public void setEndPosition (int p)
    {

        if (p <= this.position)
        {

            this.endPosition = -1;

            this.endTextPos = null;

            return;

        }

        if (p > -1)
        {

            this.endPosition = p;

        } else
        {

            this.endPosition = -1;

        }

        this.endTextPos = null;

    }

    public int getEndPosition ()
    {

        if (this.endTextPos != null)
        {

            return this.endTextPos.getOffset ();

        }

        return this.endPosition;

    }

    public int getStartPosition ()
    {

        return this.getPosition ();

    }

    public void setPosition (int p)
    {

        this.position = p;

        this.textPos = null;

    }

    public void shiftPositionBy (int p)
    {

        this.setPosition (this.getPosition () + p);

        for (Note n : this.getNotes ())
        {

            n.shiftPositionBy (p);

        }

    }

    public int getPosition ()
    {

        if (this.textPos != null)
        {

            return this.textPos.getOffset ();

        }

        return this.position;

    }

    public void setSummary (String s)
    {

        this.setName (s);

    }

    public String getSummary ()
    {

        return this.getName ();

    }

    public String toString ()
    {

        return this.getObjectType () + "(summary: " + this.name + ", id: " + this.getKey () + ", pos: " + this.getPosition () + ", end: " + this.getEndPosition () + "), in: " + this.chapter;

    }

}
