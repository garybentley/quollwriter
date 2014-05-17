package com.quollwriter.data;

import java.io.*;

import java.util.*;

import com.gentlyweb.xml.*;

import com.quollwriter.*;

import com.quollwriter.data.comparators.*;

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

    private Project       project = null;
    private List<Chapter> chapters = new ArrayList ();
    private int           lastChapterId = 0;
    private ChapterSorter sort = null;

    public Book()
    {

        super (Book.OBJECT_TYPE);

    }

    public Book(Project p)
    {

        super (Book.OBJECT_TYPE);

        this.project = p;

        this.setParent (p);

    }

    public Book(Project p,
                String  name)
    {

        this (p);

        this.setName (name);

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

        Set<NamedObject> ret = new TreeSet (new NamedObjectSorter ());

        for (Chapter c : this.chapters)
        {

            ret.add (c);

            ret.addAll (c.getAllNamedChildObjects ());

        }

        return ret;

    }

    private void setChapters (List<Chapter> c)
    {

        this.chapters = c;

    }

    void setProject (Project p)
    {

        this.project = p;

        this.setParent (p);

    }

    public Project getProject ()
    {

        return this.project;

    }

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

    public List<Chapter> getChapters ()
    {

        return this.chapters;

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

    public String toString ()
    {

        return this.getObjectType () + "(name: " + this.name + ", id: " + this.getKey () + "), in: " + this.project;

    }

    public void removeChapter (Chapter c)
    {

        this.chapters.remove (c);

    }

    public void moveChapter (Chapter c,
                             int     newIndex)
    {

        this.removeChapter (c);

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

        c.setBook (this);

    }

    private void addChapter (Chapter c,
                             int     where)
    {

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
