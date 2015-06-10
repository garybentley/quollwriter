package com.quollwriter.ui.events;

import java.util.EventObject;


public class DictionaryChangedEvent extends EventObject
{

    public static final int WORD_ADDED = 1;
    public static final int WORD_REMOVED = 2;

    private int    type = -1;
    private String word = null;

    public DictionaryChangedEvent(Object source,
                                  int    type,
                                  String word)
    {

        super (source);

        this.type = type;
        this.word = word;

    }

    public String getWord ()
    {

        return this.word;

    }

    public int getType ()
    {

        return this.type;

    }

}
