package com.quollwriter.data;

import java.text.*;

import java.util.*;

import javax.swing.text.*;

import com.quollwriter.data.comparators.*;

public class Scene extends ChapterItem
{

    public static final String OBJECT_TYPE = "scene";

    private TreeSet<OutlineItem> outlineItems = new TreeSet (new ChapterItemSorter ());

    public Scene()
    {

        super (Scene.OBJECT_TYPE);

    }

    public Scene(int     at,
                 Chapter c)
    {

        super (Scene.OBJECT_TYPE,
               at,
               c);

    }

    public synchronized void reindex ()
    {
        
        super.reindex ();
        
        TreeSet<OutlineItem> ooutlineItems = new TreeSet (new ChapterItemSorter ());
        
        ooutlineItems.addAll (this.outlineItems);
        this.outlineItems = ooutlineItems;
        
        for (OutlineItem it : this.outlineItems)
        {
            
            it.reindex ();
            
        }
        
    }
    
    public Set<ChapterItem> getChapterItemsWithPositionGreaterThan (int pos)
    {
        
        Set<ChapterItem> items = new TreeSet (new ChapterItemSorter ());

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

        List<OutlineItem> its = new ArrayList ();

        for (OutlineItem it : this.outlineItems)
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

    public Set<OutlineItem> getOutlineItems ()
    {

        return new TreeSet (this.outlineItems);

    }

    public void setChapter (Chapter c)
    {

        // Handle our outline items first.
        for (OutlineItem it : this.outlineItems)
        {
            
            it.setChapter (c);
            
        }
    
        super.setChapter (c);
        
    }    
    
    public void removeOutlineItem (OutlineItem i)
    {

        this.outlineItems.remove (i);

    }

    public void addOutlineItem (OutlineItem i)
    {

        Scene s = i.getScene ();

        if (s != null)
        {

            if (s != this)
            {

                s.removeOutlineItem (i);

            }

        } else
        {

            this.getChapter ().removeOutlineItem (i);

        }

        i.setScene (this);

        i.setChapter (this.getChapter ());

        this.outlineItems.add (i);

    }

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

    public void shiftPositionBy (int p)
    {

        super.shiftPositionBy (p);

        for (OutlineItem i : this.outlineItems)
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

}
