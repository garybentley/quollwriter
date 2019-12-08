package com.quollwriter.text;

import java.text.*;
import java.util.*;

import com.quollwriter.*;
import com.quollwriter.ui.components.*;

public class TextSnippet implements TextBlock<Sentence, TextBlock, Word>
{

    private int start = -1;
    private Sentence parent = null;
    private String snippet = null;
    private List<Word> words = new ArrayList<> ();

    public TextSnippet (String   snippet,
                        Sentence parent,
                        int      start)
    {

        this.snippet = snippet;
        this.parent = parent;
        this.start = start;
        this.words = TextUtilities.getAsWords (this.snippet);

    }

    @Override
    public int getAllTextStartOffset ()
    {

        return this.parent.getAllTextStartOffset () + this.start;

    }

    @Override
    public int getAllTextEndOffset ()
    {

        return this.getAllTextStartOffset () + this.snippet.length ();

    }

    @Override
    public int getEnd ()
    {

        return this.start + this.snippet.length ();

    }

    @Override
    public int getStart ()
    {

        return this.start;

    }

    @Override
    public String getText ()
    {

        return this.snippet;

    }

    @Override
    public Sentence getParent ()
    {

        return this.parent;

    }

    @Override
    public TextSnippet getPrevious ()
    {

        return null;

    }

    @Override
    public TextSnippet getNext ()
    {

        return null;

    }

    @Override
    public List<Word> getChildren ()
    {

        return this.words;

    }

}
