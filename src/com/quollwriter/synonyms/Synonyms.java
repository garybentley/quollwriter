package com.quollwriter.synonyms;

import java.util.*;


public class Synonyms
{

    public static char VERB = 'V';
    public static char VERB_T = 't';
    public static char VERB_I = 'i';
    public static char NOUN = 'N';
    public static char NOUN_PHRASE = 'h';
    public static char PLURAL = 'p';
    public static char OTHER = 'O';
    public static char ADVERB = 'v';
    public static char ADJECTIVE = 'A';

    public class Part
    {

        public char         type = '\0';
        public List<String> words = null;

        public Part(char         type,
                    List<String> words)
        {

            this.words = words;
            this.type = type;

        }

    }

    public List                verbs = new ArrayList ();
    public List                nouns = new ArrayList ();
    public List                other = new ArrayList ();
    public List                adjectives = new ArrayList ();
    public List                adverbs = new ArrayList ();
    public List<Synonyms.Part> words = new ArrayList ();
    public String              word = null;
    private String             parts = null;

    public void setParts (String parts)
    {

        StringBuilder b = new StringBuilder ();

        char[] chars = parts.toCharArray ();

        for (int i = 0; i < chars.length; i++)
        {

            char c = chars[i];

            if ((c == VERB) ||
                (c == VERB_I) ||
                (c == VERB_T))
            {

                c = VERB;

            }

            if ((c == Synonyms.NOUN) ||
                (c == Synonyms.PLURAL) ||
                (c == Synonyms.NOUN_PHRASE))
            {

                c = NOUN;

            }

            if ((c != NOUN) &&
                (c != ADVERB) &&
                (c != ADJECTIVE) &&
                (c != VERB))
            {

                c = OTHER;

            }

            if (b.toString ().indexOf (c) == -1)
            {

                b.append (c);

            }

        }

        this.parts = b.toString ();

    }

    public String getParts ()
    {

        return this.parts;

    }

    public void addToWords (char type)
    {

        Part p = this.newPart (type);

        if (p != null)
        {

            this.words.add (p);

        }

    }

    public Part newPart (char type)
    {

        List words = this.other;

        if ((type == VERB) ||
            (type == VERB_I) ||
            (type == VERB_T))
        {

            type = VERB;
            words = this.verbs;

        }

        if ((type == Synonyms.NOUN) ||
            (type == Synonyms.PLURAL) ||
            (type == Synonyms.NOUN_PHRASE))
        {

            type = NOUN;

            words = this.nouns;

        }

        if (type == Synonyms.ADJECTIVE)
        {

            words = this.adjectives;

        }

        if (type == Synonyms.ADVERB)
        {

            words = this.adverbs;

        }

        if ((type != NOUN) &&
            (type != ADVERB) &&
            (type != ADJECTIVE) &&
            (type != VERB))
        {

            type = OTHER;

        }

        if (words.size () > 0)
        {

            return new Part (type,
                             words);

        }

        return null;

    }

    public void addNouns (List nouns)
    {

        if (nouns != null)
        {

            this.nouns.addAll (nouns);

        }

    }

    public void addAdverbs (List advs)
    {

        if (advs != null)
        {

            this.adverbs.addAll (advs);

        }

    }

    public void addAdjectives (List adjs)
    {

        if (adjs != null)
        {

            this.adjectives.addAll (adjs);

        }

    }

    public void addVerbs (List verbs)
    {

        if (verbs != null)
        {

            this.verbs.addAll (verbs);

        }

    }

}
