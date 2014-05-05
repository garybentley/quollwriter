package com.quollwriter.data;

import java.io.*;

import java.util.*;

import javax.swing.text.Position;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.data.comparators.*;

import org.incava.util.diff.*;

import org.jdom.*;


public class Chapter extends NamedObject
{

    public static final String OBJECT_TYPE = "chapter";
    public static final String INFORMATION_OBJECT_TYPE = "chapterinformation";

    private Book              book = null;
    private String            text = null;
    private String            goals = null;
    private String            plan = null;
    private String            markup = null;
    private TreeSet<OutlineItem> outlineItems = new TreeSet (new ChapterItemSorter ());
    private TreeSet<Scene>       scenes = new TreeSet (new ChapterItemSorter ());
    private int editPosition = -1;
    private Position textEditPos = null;
    private boolean editComplete = false;

    public Chapter()
    {

        super (Chapter.OBJECT_TYPE);

    }

    public Chapter(Book b)
    {

        this ();

        this.setBook (b);

    }

    public Chapter(Book   b,
                   String name)
    {

        this (b);

        this.setName (name);

    }

    protected Chapter(String objType)
    {

        super (objType);

    }

    /**
     * If a chapter item has it's position changed (say via a drag/drop operation to move it)
     * then the sets need to be "reindexed" since the ordering is dependent on the position and
     * it is only done once when the item is added (probably for performance sake).
     *
     * This method recreates the sets backing the scenes/notes/outline items and calls the
     * same reindex method on child objects, so re-initing the entire tree.
     */
    public synchronized void reindex ()
    {
       
        super.reindex ();
       
        TreeSet<Scene> sscenes = new TreeSet (new ChapterItemSorter ());
        
        sscenes.addAll (this.scenes);
        
        this.scenes = sscenes;
        
        TreeSet<OutlineItem> ooutlineItems = new TreeSet (new ChapterItemSorter ());
        
        ooutlineItems.addAll (this.outlineItems);
        
        this.outlineItems = ooutlineItems;

        for (Scene s : this.scenes)
        {
            
            s.reindex ();
            
        }
        
        for (OutlineItem it : this.outlineItems)
        {
            
            it.reindex ();
            
        }
        
    }
    
    public boolean isEditComplete ()
    {
        
        return this.editComplete;
        
    }
    
    public void setEditComplete (boolean b)
    {
        
        this.editComplete = b;
                
    }
    
    public String getMarkup ()
    {

        return this.markup;

    }

    public void setMarkup (String m)
    {

        this.markup = m;

    }

    public void getChanges (NamedObject old,
                            Element     root)
    {

        Chapter c = (Chapter) old;

        this.addFieldChangeElement (root,
                                    "goals",
                                    ((old != null) ? c.getGoals () : null),
                                    this.goals);

        this.addFieldChangeElement (root,
                                    "plan",
                                    ((old != null) ? c.getPlan () : null),
                                    this.plan);
                                    
        if (old != null)
        {

            if (c.getText () == null)
            {

                c.setText ("");

            }

            String ot = c.getText ();

            if (ot == null)
            {

                ot = "";

            }

            String nt = this.text;

            if (nt == null)
            {

                nt = "";

            }

            String[] oldText = ot.split ("\\n");
            String[] newText = nt.split ("\\n");

            List diffs = new Diff (oldText,
                                   newText).diff ();

            if (diffs.size () > 0)
            {

                Element fieldEl = new Element ("field");

                root.addContent (fieldEl);

                fieldEl.setAttribute ("name",
                                      "text");

                fieldEl.setAttribute ("type",
                                      "diff");

                for (int i = 0; i < diffs.size (); i++)
                {

                    Difference d = (Difference) diffs.get (i);

                    if (d.getDeletedEnd () == Difference.NONE)
                    {

                        // This is an addition.
                        for (int k = d.getAddedStart (); k < (d.getAddedEnd () + 1); k++)
                        {

                            Element el = new Element ("change");
                            fieldEl.addContent (el);
                            el.setAttribute ("type",
                                             "add");
                            el.setAttribute ("line",
                                             String.valueOf (k));
                            el.addContent (newText[k]);

                        }

                        continue;

                    }

                    if (d.getAddedEnd () == Difference.NONE)
                    {

                        // This is a deletion.
                        for (int k = d.getDeletedStart (); k < (d.getDeletedEnd () + 1); k++)
                        {

                            Element el = new Element ("change");
                            fieldEl.addContent (el);
                            el.setAttribute ("type",
                                             "remove");
                            el.setAttribute ("line",
                                             String.valueOf (k));
                            el.addContent (oldText[k]);

                        }

                        continue;

                    }

                    // This is a modification.
                    for (int k = d.getAddedStart (); k < (d.getAddedEnd () + 1); k++)
                    {

                        Element el = new Element ("change");
                        fieldEl.addContent (el);
                        el.setAttribute ("type",
                                         "change");
                        el.setAttribute ("line",
                                         String.valueOf (k));

                        Element oel = new Element ("old");
                        el.addContent (oel);

                        ot = "";

                        if (k < oldText.length)
                        {

                            ot = oldText[k];

                        }

                        oel.addContent (ot);

                        Element nel = new Element ("new");
                        el.addContent (nel);

                        nt = "";

                        if (k < newText.length)
                        {

                            nt = newText[k];

                        }

                        nel.addContent (nt);

                    }

                }

            }

        } else
        {

            this.addFieldChangeElement (root,
                                        "text",
                                        null,
                                        this.getText ());

        }

    }

    public DataObject getObjectForReference (ObjectReference r)
    {

        DataObject d = super.getObjectForReference (r);

        if (d != null)
        {

            return d;

        }

        for (Scene s : this.scenes)
        {

            d = s.getObjectForReference (r);

            if (d != null)
            {

                return d;

            }

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
        
        for (Scene s : this.scenes)
        {

            if (s.getPosition () > pos)
            {

                items.add (s);
                
            }

        }

        for (Note n : this.getNotes ())
        {

            if (n.getPosition () > pos)
            {

                items.add (n);

            }

        }
        
        return items;
        
    }

    public Set<ChapterItem> getChapterItemsWithPositionBetween (int start,
                                                                int end)
    {
        
        Set<ChapterItem> items = new TreeSet (new ChapterItemSorter ());

        for (OutlineItem it : this.outlineItems)
        {

            if ((it.getPosition () >= start)
                &&
                (it.getPosition () <= end)
               )
            {

                items.add (it);

            }

        }
        
        for (Scene s : this.scenes)
        {

            if ((s.getPosition () >= start)
                &&
                (s.getPosition () <= end)
               )
            {

                items.add (s);
                
            }

        }

        for (Note n : this.getNotes ())
        {

            if ((n.getPosition () >= start)
                &&
                (n.getPosition () <= end)
               )
            {

                items.add (n);

            }

        }
        
        return items;
        
    }
    
    public Set<OutlineItem> getItemsFromPositionToNextScene (int pos)
    {

        Set<OutlineItem> items = new TreeSet (new ChapterItemSorter ());

        for (OutlineItem it : this.outlineItems)
        {

            if (it.getPosition () >= pos)
            {

                items.add (it);

            }

        }

        Scene s = this.getLastScene (pos);

        if (s != null)
        {

            for (OutlineItem it : s.getOutlineItems ())
            {

                if (it.getPosition () >= pos)
                {

                    items.add (it);

                }

            }

        }

        return items;

    }

    public Scene getSceneAt (int pos)
    {

        for (Scene s : this.scenes)
        {

            if (s.getPosition () == pos)
            {

                return s;

            }

        }

        return null;

    }

    public Set<Scene> getScenesAt (int pos)
    {

        Set<Scene> scenes = new TreeSet<Scene> (new ChapterItemSorter ());

        for (Scene s : this.scenes)
        {

            if (s.getPosition () == pos)
            {

                scenes.add (s);

            }

        }

        return scenes;

    }

    public OutlineItem getOutlineItemAt (int pos)
    {

        for (Scene s : this.scenes)
        {

            OutlineItem it = s.getOutlineItemAt (pos);

            if (it != null)
            {

                return it;

            }

        }

        for (OutlineItem it : this.outlineItems)
        {

            if (it.getPosition () == pos)
            {

                return it;

            }

        }

        return null;

    }

    public Set<OutlineItem> getOutlineItemsAt (int pos)
    {

        Set<OutlineItem> its = new TreeSet (new ChapterItemSorter ());

        for (Scene s : this.scenes)
        {

            its.addAll (s.getOutlineItemsAt (pos));

        }

        for (OutlineItem it : this.outlineItems)
        {

            if (it.getPosition () == pos)
            {

                its.add (it);

            }

        }

        return its;

    }

    public Set<NamedObject> getAllNamedChildObjects ()
    {

        Set<NamedObject> objs = new TreeSet (new ChapterItemSorter ());
    
        objs.addAll (this.scenes);
        objs.addAll (this.outlineItems);
        objs.addAll (this.getNotes ());

        return objs;

    }

    public void addChapterItem (ChapterItem item)
    {

        if (item.getObjectType ().equals (Note.OBJECT_TYPE))
        {

            this.addNote ((Note) item);

        }

        if (item.getObjectType ().equals (OutlineItem.OBJECT_TYPE))
        {

            this.addOutlineItem ((OutlineItem) item);

        }

        if (item.getObjectType ().equals (Scene.OBJECT_TYPE))
        {

            this.addScene ((Scene) item);

        }

    }

    public Set<? extends ChapterItem> getAllStructureItemsWithinRange (int min,
                                                                       int max)
    {
        
        Set<ChapterItem> items = new TreeSet (new ChapterItemSorter ());
        
        for (OutlineItem it : this.outlineItems)
        {
            
            if (it.getPosition () < min || it.getPosition () > max)
            {
                
                continue;
                
            }
            
            items.add (it);
            
        }
    
        for (Scene s : this.scenes)
        {
            
            if (s.getPosition () >= min && s.getPosition () <= max)
            {
                            
                items.add (s);
                
            }
            
            Set<OutlineItem> oitems = s.getOutlineItems ();
            
            for (OutlineItem oit : oitems)
            {
                
                if (oit.getPosition () < min || oit.getPosition () > max)
                {
                    
                    continue;
                    
                }
                
                items.add (oit);                            
                
            }            
            
        }
    
        return items;
    
    }
    
    public Set<? extends ChapterItem> getChapterItems (String objType)
    {

        if (objType.equals (OutlineItem.OBJECT_TYPE))
        {

            return this.getOutlineItems ();

        }

        if (objType.equals (Note.OBJECT_TYPE))
        {

            return this.getNotes ();

        }

        if (objType.equals (Scene.OBJECT_TYPE))
        {

            return this.getScenes ();

        }

        return null;

    }

    public void addNote (Note n)
    {
        
        if (this.getNotes ().contains (n))
        {
            
            return;
            
        }
        
        if ((n.getChapter () != null)
            &&
            (n.getChapter () != this)
           )
        {
            
            n.getChapter ().removeNote (n);
            
        }
        
        super.addNote (n);
        
    }
    
    public void addScene (Scene s)
    {

        if (this.scenes.contains (s))
        {
            
            return;
            
        }
    
        if ((s.getChapter () != null)
            &&
            (s.getChapter () != this)
           )
        {
            
            s.getChapter ().removeScene (s);
            
        }
    
        s.setChapter (this);
/*
        for (OutlineItem i : s.getOutlineItems ())
        {

            i.setChapter (this);

        }
*/
        this.scenes.add (s);

    }

    public void removeScene (Scene s)
    {

        this.scenes.remove (s);

    }

    public Set<Scene> getScenes ()
    {

        Set<Scene> items = new TreeSet (new ChapterItemSorter ());
        
        items.addAll (this.scenes);

        return items;
        
    }

    public Set<OutlineItem> getOutlineItems ()
    {

        Set<OutlineItem> items = new TreeSet (new ChapterItemSorter ());
        
        items.addAll (this.outlineItems);
        
        return items;

    }
/*
    private void setOutlineItems (List<OutlineItem> l)
    {

        this.outlineItems = l;

    }
*/
    public void removeChapterItem (ChapterItem i)
    {

        if (i.getObjectType ().equals (OutlineItem.OBJECT_TYPE))
        {

            this.removeOutlineItem ((OutlineItem) i);

        }

        if (i.getObjectType ().equals (Note.OBJECT_TYPE))
        {

            this.removeNote ((Note) i);

        }

        if (i.getObjectType ().equals (Scene.OBJECT_TYPE))
        {

            this.removeScene ((Scene) i);

        }

    }

    public void removeOutlineItem (OutlineItem i)
    {

        this.outlineItems.remove (i);

    }

    public void addOutlineItem (OutlineItem i)
    {

        if (i.getScene () != null)
        {
            
            return;
            
        }
    
        if (this.outlineItems.contains (i))
        {
            
            return;
            
        }
    
        if ((i.getChapter () != null)
            &&
            (i.getChapter () != this)
           )
        {
            
            i.getChapter ().removeOutlineItem (i);
            
        }

        i.setChapter (this);

        if (i.getScene () == null)
        {
        
            this.outlineItems.add (i);
            
        }

    }

    public String getText ()
    {

        return this.text;

    }

    public void setText (String t)
    {

        if (t != null)
        {

            t = StringUtils.replaceString (t,
                                           String.valueOf ('\r'),
                                           "");

        }

        this.text = t;

    }

    public String getGoals ()
    {

        return this.goals;

    }

    public void setGoals (String t)
    {

        if (t != null)
        {

            t = StringUtils.replaceString (t,
                                           String.valueOf ('\r'),
                                           "");

        }

        this.goals = t;

    }

    public String getPlan ()
    {

        return this.plan;

    }

    public void setPlan (String t)
    {

        if (t != null)
        {

            t = StringUtils.replaceString (t,
                                           String.valueOf ('\r'),
                                           "");

        }

        this.plan = t;

    }
    
    public String toString ()
    {

        return Chapter.OBJECT_TYPE + "(name: " + this.name + ", id: " + this.getKey () + ", length: " + ((this.text != null) ? this.text.length () : "0") + "), in: " + this.book;

    }

    public void setBook (Book b)
    {

        this.book = b;

        this.setParent (b);

    }

    public Book getBook ()
    {

        return this.book;

    }

    public Scene getNextScene (Scene s)
    {

        // Should return the next scene after s.
        return this.scenes.higher (s);

    }

    public Scene getLastScene (int position)
    {

        Scene last = null;

        for (Scene s : this.scenes)
        {

            if (s.getPosition () >= position)
            {

                return last;

            } else
            {

                last = s;

            }

        }

        return last;

    }

    public List<Note> getNotesBetween (int start,
                                       int end)
    {

        List<Note> newNotes = new ArrayList ();

        for (Note n : this.getNotes ())
        {

            if ((n.getPosition () >= start) &&
                (n.getPosition () <= end))
            {

                newNotes.add (n);

            }

        }

        return newNotes;

    }

    public void setTextEditPosition (Position p)
    {
        
        this.textEditPos = p;
        
        if (p == null)
        {
            
            this.editPosition = -1;
            
        }
        
    }
    
    public void setEditPosition (int p)
    {
        
        this.editPosition = p;
        
    }
    
    public int getEditPosition ()
    {
        
        if (this.textEditPos != null)
        {
            
            return this.textEditPos.getOffset ();
            
        }
        
        return this.editPosition;
        
    }

}
