package com.quollwriter.data;

import java.util.*;

import javafx.beans.value.*;
import javafx.beans.property.*;

import com.quollwriter.data.comparators.*;

public abstract class ChapterItem extends NamedObject
{

    protected Chapter chapter = null;
    private SimpleIntegerProperty positionProp = new SimpleIntegerProperty (0);
    private SimpleIntegerProperty endPositionProp = new SimpleIntegerProperty (-1);

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

        this.setPosition (at);
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

        this.setEndPosition (end);

    }

    public SimpleIntegerProperty positionProperty ()
    {

        return this.positionProp;

    }

    public SimpleIntegerProperty endPositionProperty ()
    {

        return this.endPositionProp;

    }

    @Override
    public void fillToStringProperties (Map<String, Object> props)
    {

        super.fillToStringProperties (props);

        this.addToStringProperties (props,
                                    "position",
                                    this.positionProp.getValue ());
        this.addToStringProperties (props,
                                    "endPosition",
                                    this.endPositionProp.getValue ());
        this.addToStringProperties (props,
                                    "chapter",
                                    this.chapter);

    }

    public Set<NamedObject> getAllNamedChildObjects ()
    {

        return new HashSet<> (this.getNotes ());

    }

    public Chapter getChapter ()
    {

        return this.chapter;

    }

    public void setChapter (Chapter c)
    {

        if ((this.chapter != null)
            &&
            (this.chapter.equals (c))
           )
        {

            // Already set.
            return;

        }

        if ((this.chapter != null)
            &&
            (!this.chapter.equals (c))
           )
        {

            // Remove this item from the existing chapter.
            this.chapter.removeChapterItem (this);

        }

        this.chapter = c;

        if (this.getKey () == null)
        {

            // Don't try and add the item if there is no key.
            return;

        }

        this.chapter.addChapterItem (this);

    }

    public void setEndPosition (int p)
    {

        if (p <= this.positionProp.getValue ())
        {

            this.endPositionProp.setValue (-1);

            return;

        }

        if (p > -1)
        {

            this.endPositionProp.setValue (p);

        } else
        {

            this.endPositionProp.setValue (-1);

        }

    }

    public int getEndPosition ()
    {

        return this.endPositionProp.getValue ();

    }

    public int getStartPosition ()
    {

        return this.getPosition ();

    }

    public void setPosition (int p)
    {

        if (p < 0)
        {

            p = 0;

        }

        int op = this.positionProp.getValue ();

        this.positionProp.setValue (p);

        if (this.endPositionProp.getValue () > -1)
        {

            this.endPositionProp.setValue (this.endPositionProp.getValue () + (p - op));

        }

    }

    public void shiftPositionBy (int p)
    {

        this.setPosition (this.getPosition () + p);

        this.setEndPosition (this.getEndPosition () + p);

    }

    public int getPosition ()
    {

        return this.positionProp.getValue ();

    }

    public void setSummary (String s)
    {

        this.setName (s);

    }

    public String getSummary ()
    {

        return this.getName ();

    }

    public static class ChapterItemEvent
    {

        public enum Type
        {
            positionchange
        }

        private ChapterItem item = null;
        private Type type = null;
        private ObservableValue<? extends Number> pos = null;
        private Integer oldV = null;
        private Integer newV = null;

        public ChapterItemEvent (ChapterItem     i,
                                 Type            t,
                                 ObservableValue<? extends Number> pos,
                                 Integer         oldV,
                                 Integer         newV)
        {

            this.item = i;
            this.type = t;
            this.pos = pos;
            this.oldV = oldV;
            this.newV = newV;

        }

        public ChapterItem getItem ()
        {

            return this.item;

        }

        public Integer getOld ()
        {

            return this.oldV;

        }

        public Integer getNew ()
        {

            return this.newV;

        }

    }

}
