package com.quollwriter.data;

import java.io.*;

import java.util.*;

import javafx.collections.*;

import com.gentlyweb.xml.*;

import com.quollwriter.*;

import com.quollwriter.data.comparators.*;

import com.quollwriter.text.*;

import com.quollwriter.ui.events.*;

import org.jdom.*;


public class Book extends NamedObject
{

    public static final String OBJECT_TYPE = "book";

    public class XMLConstants
    {

        public static final String root = Book.OBJECT_TYPE;
        public static final String lastModified = "lastModified";
        public static final String chapters = "chapters";

    }

    // TODO Change to use a set.
    private ObservableList<Chapter> chapters = FXCollections.observableList (new ArrayList<> ());
    private ChapterSorter sort = null;

    public Book()
    {

        super (Book.OBJECT_TYPE);

    }

    public Book(Project p)
    {

        super (Book.OBJECT_TYPE);

        this.setProject (p);

    }

    public Book(Project p,
                String  name)
    {

        this (p);

        this.setName (name);

    }

    @Override
    public void fillToStringProperties (Map<String, Object> props)
    {

        super.fillToStringProperties (props);

        this.addToStringProperties (props,
                                    "chapterCount",
                                    this.chapters.size ());

    }

    public void setChapterSorter (ChapterSorter c)
    {

        this.sort = c;

        if (this.sort != null)
        {

            Collections.sort (this.chapters,
                              c);

        }

    }

    public void getChanges (NamedObject old,
                            Element     root)
    {

    }

    public Set<NamedObject> getAllNamedChildObjects ()
    {

        Set<NamedObject> ret = new TreeSet (NamedObjectSorter.getInstance ());

        for (Chapter c : this.chapters)
        {

            ret.add (c);

            ret.addAll (c.getAllNamedChildObjects ());

        }

        return ret;

    }

    public void removeAllChapters ()
    {

        synchronized (this)
        {

            for (Chapter c : this.chapters)
            {

                c.setBook (null);

            }

            this.chapters.clear ();

        }

    }
/*
TODO Remove
    private void setChapters (List<Chapter> c)
    {

        this.chapters = c;

    }
*/

    public Chapter getFirstChapter ()
    {

        if (this.chapters == null)
        {

            return null;

        }

        if (this.chapters.size () == 0)
        {

            return null;

        }

        return this.chapters.get (0);

    }

    public Chapter getLastChapter ()
    {

        if (this.chapters == null)
        {

            return null;

        }

        if (this.chapters.size () == 0)
        {

            return null;

        }

        return this.chapters.get (this.chapters.size () - 1);

    }

    public ObservableList<Chapter> getChapters ()
    {

        return this.chapters;

    }

    public int getChapterWordCount ()
    {

        int i = 0;

        for (Chapter c : this.chapters)
        {

            String t = (c.getText () != null ? c.getText ().getText () : null);

            i += TextUtilities.getWordCount (t);

        }

        return i;

    }

    public int getChapterIndex (Chapter c)
    {

        // This can happen for imports when no key has been assigned yet.
        if (c.getKey () == null)
        {

            for (int i = 0; i < this.chapters.size (); i++)
            {

                Chapter cc = this.chapters.get (i);

                if (cc.getName ().equals (c.getName ()))
                {

                    return i;

                }

            }

            return -1;

        }

        int ind = this.chapters.indexOf (c);

        if (ind > -1)
        {

            ind++;

        }

        return ind;

    }

    public Chapter getChapterByKey (long k)
    {

        for (Chapter c : this.chapters)
        {

            if (c.getKey ().longValue () == k)
            {

                return c;

            }

        }

        return null;

    }

    public Chapter getChapter (int i)
    {

        return (Chapter) this.chapters.get (i);

    }

    public void removeChapter (Chapter c)
    {

        c.setBook (null);

        this.chapters.remove (c);

    }

    public void moveChapter (Chapter c,
                             int     newIndex)
    {

        Book b = c.getBook ();

        this.removeChapter (c);

        c.setBook (b);

        this.chapters.add (newIndex,
                           c);

    }

    public Chapter getChapter (Long key)
    {

        for (int i = 0; i < this.chapters.size (); i++)
        {

            Chapter c = (Chapter) this.chapters.get (i);

            if (c.getKey ().equals (key))
            {

                return c;

            }

        }

        return null;

    }

    public Set<Chapter> getAllChaptersWithName (String name)
    {

        Set<Chapter> ret = new LinkedHashSet ();

        for (int i = 0; i < this.chapters.size (); i++)
        {

            Chapter c = (Chapter) this.chapters.get (i);

            if (c.getName ().equalsIgnoreCase (name))
            {

                ret.add (c);

            }

        }

        return ret;

    }

    public Chapter getChapterByName (String name)
    {

        for (int i = 0; i < this.chapters.size (); i++)
        {

            Chapter c = (Chapter) this.chapters.get (i);

            if (c.getName ().equalsIgnoreCase (name))
            {

                return c;

            }

        }

        return null;

    }

    public Chapter createChapterAfter (Chapter c,
                                       String  name)
    {

        int ind = -1;

        if (c != null)
        {

            ind = this.chapters.indexOf (c);

        }

        ind++;

        Chapter ch = new Chapter (this,
                                  name);

        this.addChapter (ch,
                         ind);

        return ch;

    }

    public Chapter createChapterAfter (Chapter after,
                                       Chapter c)
    {

        int ind = -1;

        if (after != null)
        {

            ind = this.chapters.indexOf (after);

        }

        ind++;

        this.addChapter (c,
                         ind);

        return c;

    }

    public void addChapter (Chapter c)
    {

        this.addChapter (c,
                         -1);

    }

    private void addChapter (Chapter c,
                             int     where)
    {

        if (this.chapters.contains (c))
        {

            return;

        }

        if ((where > this.chapters.size ()) ||
            (where < 0))
        {

            where = this.chapters.size ();

        }

        c.setBook (this);

        this.chapters.add (where,
                           c);

        if (this.sort != null)
        {

            Collections.sort (this.chapters,
                              this.sort);

        }

    }

    public DataObject getObjectForReference (ObjectReference r)
    {

        DataObject d = super.getObjectForReference (r);

        if (d != null)
        {

            return d;

        }

        for (Chapter c : this.chapters)
        {

            d = c.getObjectForReference (r);

            if (d != null)
            {

                return d;

            }

        }

        return null;

    }

}
