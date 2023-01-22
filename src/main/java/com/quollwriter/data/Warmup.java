package com.quollwriter.data;

import java.util.*;

import org.dom4j.*;


public class Warmup extends NamedObject
{

    public static final String OBJECT_TYPE = "warmup";

    private Chapter chapter = null;
    private Prompt  prompt = null;
    private int     words = 0;
    private int     mins = 0;

    public Warmup()
    {

        super (Warmup.OBJECT_TYPE);

    }

    public String toString ()
    {

        return this.getObjectType () + "(" + this.chapter + ", id: " + this.getKey () + ", mins: " + this.mins + ", " + this.words + ", prompt: " + this.prompt + ")";

    }

    public int getMins ()
    {

        return this.mins;

    }

    public void setMins (int m)
    {

        this.mins = m;

    }

    public int getWords ()
    {

        return this.words;

    }

    public void setWords (int w)
    {

        this.words = w;

    }

    @Override
    public Set<NamedObject> getAllNamedChildObjects ()
    {

        return new HashSet<> (this.getNotes ());

    }

    @Override
    public void getChanges (NamedObject old,
                            Element     root)
    {

    }

    public Chapter getChapter ()
    {

        return this.chapter;

    }

    public void setChapter (Chapter c)
    {

        this.chapter = c;
        this.nameProperty ().bind (c.nameProperty ());

        this.setParent (c);

    }

    public Prompt getPrompt ()
    {

        return this.prompt;

    }

    public void setPrompt (Prompt p)
    {

        this.prompt = p;

    }

    @Override
    public DataObject getObjectForReference (ObjectReference r)
    {

        return null;

    }

}
