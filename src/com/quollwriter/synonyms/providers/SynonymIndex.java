package com.quollwriter.synonyms.providers;

public class SynonymIndex implements Comparable<SynonymIndex>
{

    public String word = null;
    public String parts = null;
    public long   index = 0;

    public int compareTo (SynonymIndex si)
    {

        return this.word.compareTo (si.word);

    }

}
