package com.quollwriter.data;

import java.text.*;

import java.util.*;
import java.util.stream.*;

import javafx.collections.*;
import javafx.beans.property.*;

import org.reactfx.*;

import com.quollwriter.data.comparators.*;

import com.quollwriter.*;
import com.quollwriter.text.*;

public class Scene extends ChapterItem
{

    public static final String OBJECT_TYPE = "scene";

    private ObservableSet<OutlineItem> outlineItems = FXCollections.observableSet (new TreeSet<> (new ChapterItemSorter ()));
    private EventSource<CollectionEvent<OutlineItem>> outlineItemsEventSource = new EventSource<> ();
    private EventSource<ChapterItem.ChapterItemEvent> outlineItemsPositionEventSource = new EventSource<> ();

    public Scene()
    {

        super (Scene.OBJECT_TYPE);

        this._init ();

    }

    public Scene(int     at,
                 Chapter c)
    {

        super (Scene.OBJECT_TYPE,
               at,
               c);

        this._init ();

    }

    private void _init ()
    {

        this.outlineItems.addListener ((SetChangeListener<OutlineItem>) ev ->
        {

            if (ev.wasAdded ())
            {

                OutlineItem it = ev.getElementAdded ();

                this.outlineItemsEventSource.push (new CollectionEvent<OutlineItem> (this.outlineItems,
                                                                                      it,
                                                                                      CollectionEvent.Type.add));

                it.addChangeListener (it.positionProperty (),
                                      (pr, oldv, newv) ->
                {

                    this.outlineItemsPositionEventSource.push (new ChapterItem.ChapterItemEvent (it,
                                                                                    ChapterItem.ChapterItemEvent.Type.positionchange,
                                                                                    pr,
                                                                                    oldv.intValue (),
                                                                                    newv.intValue ()));

                });

            }

            if (ev.wasRemoved ())
            {

                OutlineItem it = ev.getElementRemoved ();

                it.dispose ();

                this.outlineItemsEventSource.push (new CollectionEvent<OutlineItem> (this.outlineItems,
                                                                                     it,
                                                                                     CollectionEvent.Type.remove));

            }

        });

    }

    public EventStream<ChapterItem.ChapterItemEvent> outlineItemsPositionEvents ()
    {

        return this.outlineItemsPositionEventSource;

    }

    public EventStream<CollectionEvent<OutlineItem>> outlineItemsEvents ()
    {

        return this.outlineItemsEventSource;

    }

    @Override
    public void fillToStringProperties (Map<String, Object> props)
    {

        super.fillToStringProperties (props);

        this.addToStringProperties (props,
                                    "outlineItems",
                                    this.outlineItems.size ());

    }

    public Set<ChapterItem> getChapterItemsWithPositionGreaterThan (int pos)
    {

        Set<ChapterItem> items = new TreeSet<> (new ChapterItemSorter ());

        for (OutlineItem it : this.outlineItems)
        {

            if (it.getPosition () > pos)
            {

                items.add (it);

            }

        }

        return items;

    }

    public void getChanges (NamedObject      old,
                            org.jdom.Element root)
    {

    }

    public List<OutlineItem> getOutlineItemsAt (int pos)
    {

        List<OutlineItem> its = new ArrayList<> ();

        for (OutlineItem it : this.getOutlineItems ())
        {

            if (it.getPosition () == pos)
            {

                its.add (it);

            }

        }

        return its;

    }

    public OutlineItem getOutlineItemAt (int pos)
    {

        for (OutlineItem it : this.outlineItems)
        {

            if (it.getPosition () == pos)
            {

                return it;

            }

        }

        return null;

    }

    public DataObject getObjectForReference (ObjectReference r)
    {

        DataObject d = super.getObjectForReference (r);

        if (d != null)
        {

            return d;

        }

        for (OutlineItem i : this.outlineItems)
        {

            d = i.getObjectForReference (r);

            if (d != null)
            {

                return d;

            }

        }

        return null;

    }

    public ObservableSet<OutlineItem> getOutlineItems ()
    {

        return this.outlineItems;
/*
        TreeSet t = new TreeSet (new ChapterItemSorter ());

        t.addAll (this.outlineItems);

        return t;
*/
        //return new TreeSet (this.outlineItems);

    }

    public void setChapter (Chapter c)
    {

        // Handle our outline items first.
        for (OutlineItem it : this.outlineItems)
        {

            it.setChapter (c);

        }

        super.setChapter (c);

        this.setParent (c);

    }

    public void removeOutlineItem (OutlineItem i)
    {

        this.outlineItems.remove (i);

        i.dispose ();

    }

    public void addOutlineItem (OutlineItem i)
    {

        if (!i.getChapter ().equals (this.getChapter ()))
        {

            throw new IllegalStateException ("Chapters differ.");

        }

        if (this.outlineItems.contains (i))
        {

            return;

        }

        Scene s = i.getScene ();

        if (s != null)
        {

            if (!s.equals (this))
            {

                s.removeOutlineItem (i);

            }

        } else
        {

            this.getChapter ().removeOutlineItem (i);

        }

        i.setScene (this);

        this.outlineItems.add (i);

    }

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

            // Take the first sentence and use that as the "name".
            Paragraph p = new Paragraph (t,
                                         0);

            if (p.getSentenceCount () > 0)
            {

                this.setName (p.getFirstSentence ().getText ());

            }

        }

        super.setDescription (d);

    }

    public void shiftPositionBy (int p)
    {

        super.shiftPositionBy (p);

        for (OutlineItem i : this.getOutlineItems ())
        {

            i.shiftPositionBy (p);

        }

    }

    public int getLength ()
    {

        Scene next = this.getChapter ().getNextScene (this);

        if (next != this)
        {

            return next.getPosition () - this.getPosition ();

        }

        return -1;

    }

    @Override
    public void dispose ()
    {

        this.outlineItems.stream ()
            .forEach (o -> o.dispose ());

        super.dispose ();

    }

}
