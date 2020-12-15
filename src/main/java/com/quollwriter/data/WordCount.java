package com.quollwriter.data;

import java.util.*;

import org.dom4j.*;


public class WordCount
{

    public static final String OBJECT_TYPE = "wordcount";

    private int     count = 0;
    private Date    start = null;
    private Date    end = null;
    private Chapter chapter = null;
    private Project project = null;

    public WordCount()
    {

    }

    public WordCount(int  count,
                     Date start,
                     Date end)
    {

        this ();

        this.count = count;
        this.start = start;
        this.end = end;

    }

    @Override
    public String toString ()
    {

        return "word-count(start: " + this.start + ", end: " + this.end + ", count: " + count + ")";

    }

    public void getChanges (NamedObject old,
                            Element     root)
    {

    }

    public Set<NamedObject> getAllNamedChildObjects ()
    {

        return new HashSet<> ();

    }

    public void setProject (Project p)
    {

        this.project = p;

    }

    public Project getProject ()
    {

        return this.project;

    }

    public void setChapter (Chapter c)
    {

        this.chapter = c;

    }

    public Chapter getChapter ()
    {

        return this.chapter;

    }

    public void setCount (int count)
    {

        this.count = count;

    }

    public int getCount ()
    {

        return this.count;

    }

    public Date getStart ()
    {

        return this.start;

    }

    public void setStart (Date d)
    {

        this.start = d;

    }

    public Date getEnd ()
    {

        return this.end;

    }

    public void setEnd (Date d)
    {

        this.end = d;

    }

}
