package com.quollwriter.text;

import java.text.*;
import java.util.*;

import com.quollwriter.*;

public class SentenceMatches
{

    private Set<Integer> indices = null;
    private Sentence sentence = null;
    private String match = null;

    public SentenceMatches (String       match,
                            Sentence     sent,
                            Set<Integer> indices)
    {

        this.match = match;
        this.sentence = sent;
        this.indices = indices;

    }

    public Sentence getSentence ()
    {

        return this.sentence;

    }

    public String getMatch ()
    {

        return this.match;

    }

    public Set<Integer> getIndices ()
    {

        return this.indices;

    }

}
