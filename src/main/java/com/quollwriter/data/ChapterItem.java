package com.quollwriter.data;

import java.util.*;

import javafx.beans.value.*;
import javafx.beans.property.*;

import javax.swing.text.Position;

import com.quollwriter.data.comparators.*;

public abstract class ChapterItem extends NamedObject
{

    //private int       position = -1;
    //private int       endPosition = -1;
    private Position  textPos = null;
    private Position  endTextPos = null;
    //private com.quollwriter.ui.fx.components.TextEditor.Position textPos2 = null;
    //private com.quollwriter.ui.fx.components.TextEditor.Position endTextPos2 = null;
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

    public void dispose ()
    {

        this.positionProp.unbind ();
        this.endPositionProp.unbind ();
/*
        if (this.textPos2 != null)
        {

            this.textPos2.dispose ();

        }

        if (this.endTextPos2 != null)
        {

            this.endTextPos2.dispose ();

        }
*/
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
                                    /*
        this.addToStringProperties (props,
                                    "textPosition",
                                    (this.textPos != null ? this.textPos.getOffset () : null));
        this.addToStringProperties (props,
                                    "endTextPosition",
                                    (this.endTextPos != null ? this.endTextPos.getOffset () : null));
                                    */
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
/*
    public void bindPosition (com.quollwriter.ui.fx.components.TextEditor.Position t)
    {

        this.unbindPosition ();
        this.positionProp.bindBidirectional (t.positionProperty ());

    }

    public void unbindPosition ()
    {

        this.positionProp.unbindBidirectional ();

    }

    public void bindEndPosition (com.quollwriter.ui.fx.components.TextEditor.Position t)
    {

        this.unbindBidirectional ();
        this.endPositionProp.bindBidirectional (t.positionProperty ());

    }


    public void unbindEndPosition ()
    {

        this.endPositionProp.unbindBidirectional ();

    }
*/
/*
    public void setTextPosition2 (com.quollwriter.ui.fx.components.TextEditor.Position t)
    {

        if (this.textPos2 != null)
        {

            this.textPos2.dispose ();

        }

        this.textPos2 = t;
        this.positionProp.unbindBidirectional ();
        this.positionProp.bindBidirectional (t.positionProperty ());

    }
*/
    public void setTextPosition (Position t)
    {

        this.textPos = t;

    }

    public void setEndTextPosition (Position t)
    {

        this.endTextPos = t;

    }
/*
    public void setEndTextPosition2 (com.quollwriter.ui.fx.components.TextEditor.Position t)
    {

        if (this.endTextPos2 != null)
        {

            this.endTextPos2.dispose ();

        }

        this.endTextPos2 = t;
        this.endPositionProp.unbindBidirectional ();
        this.endPositionProp.bindBidirectional (t.positionProperty ());

    }
*/
    public Position getTextPosition ()
    {

        return this.textPos;

    }

    public void setEndPosition (int p)
    {

        if (p <= this.positionProp.getValue ())
        {

            this.endPositionProp.setValue (-1);
            //this.endPosition = -1;

            //this.endTextPos = null;

            return;

        }

        if (p > -1)
        {

            this.endPositionProp.setValue (p);

        } else
        {

            this.endPositionProp.setValue (-1);

        }

        //this.endTextPos = null;

    }

    public int getEndPosition ()
    {
/*
        if (this.endTextPos != null)
        {

            return this.endTextPos.getOffset ();

        }
*/
        return this.endPositionProp.getValue ();

    }

    public int getStartPosition ()
    {

        return this.getPosition ();

    }

    public void setPosition (int p)
    {

        //this.position = p;
        //this.positionProp.unbind ();
        this.positionProp.setValue (p);
/*
        this.textPos = null;

        if (this.textPos2 != null)
        {

            this.textPos2.dispose ();

        }

        this.textPos2 = null;
*/
    }

    public void shiftPositionBy (int p)
    {

        this.setPosition (this.getPosition () + p);

        this.setEndPosition (this.getEndPosition () + p);

    }

    public int getPosition ()
    {
/*
        if (this.textPos2 != null)
        {

            return this.textPos2.getOffset ();

        }
*/
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
