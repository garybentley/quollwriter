package com.quollwriter.text;

import java.text.*;

import java.util.*;

import com.quollwriter.text.rules.*;

@Deprecated

public class SentenceIterator implements Iterator<String>
{

    private BreakIterator sentenceIter = null;
    private String        text = null;
    private String        current = null;
    private int           start = 0;
    private int           end = 0;
    private boolean       inDialogue = false;

    public SentenceIterator(String text)
    {

        this.reinit (text);
/*
        this.text = text;

        this.sentenceIter = BreakIterator.getSentenceInstance ();

        this.sentenceIter.setText (text);

        this.start = this.sentenceIter.first ();
*/
    }

    private void reinit (String text)
    {

        this.text = text;

        if (text == null)
        {

            return;

        }

        this.sentenceIter = BreakIterator.getSentenceInstance ();

        this.sentenceIter.setText (text);

        this.init (this.start);

    }

    public SentenceIterator clone ()
    {

        SentenceIterator si = new SentenceIterator (this.text);
        si.current = this.current;
        si.start = this.start;
        si.end = this.end;
        si.inDialogue = this.inDialogue;

        return si;

    }

    public boolean hasNext ()
    {

        return this.end != BreakIterator.DONE;

    }

    public void remove ()
    {

        throw new UnsupportedOperationException ("Not supported.");

    }

    public boolean inDialogue ()
    {

        return this.inDialogue;

    }

    public String last ()
    {

        if (this.text == null)
        {

            return null;

        }

        // Get the current, check to see if we are in dialogue.
        if (this.current != null)
        {

            //List<String> swords = TextUtilities.getAsWords (this.current);
/*
            this.inDialogue = TextUtilities.stillInDialogue (swords,
                                                             this.inDialogue);
*/
        }

        this.start = this.sentenceIter.last ();

        if (this.start == BreakIterator.DONE)
        {

            return null;

        }

        // Go back one.
        return this.previous ();

    }

    public String next ()
    {

        if (this.text == null)
        {

            return null;

        }

        // Get the current, check to see if we are in dialogue.
        if (this.current != null)
        {

            //List<String> swords = TextUtilities.getAsWords (this.current);
/*
            this.inDialogue = TextUtilities.stillInDialogue (swords,
                                                             this.inDialogue);
*/
        }

        if (this.end == BreakIterator.DONE)
        {

            return null;

        }

        this.start = this.end;
/*
        if (this.start == BreakIterator.DONE)
        {

            this.start = 0;

        }
*/
        // Get the sentence.
        this.end = this.sentenceIter.next ();

        if (this.end == BreakIterator.DONE)
        {

            if (this.start == this.text.length ())
            {

                return null;

            }

            return this.text.substring (this.start);
            //return null;

        }

        String s = this.text.substring (this.start,
                                        this.end);

        //this.start = this.end;

        this.current = s;

        return s;

    }

    public String previous ()
    {

        if (this.text == null)
        {

            return this.text;

        }

        if (this.start == BreakIterator.DONE)
        {

            return null;

        }

        this.end = this.start;

        // Get the sentence.
        this.start = this.sentenceIter.previous ();

        if (this.start == BreakIterator.DONE)
        {

            // Just go to the start.
            return null;

        }

        String s = this.text.substring (this.start,
                                        this.end);

        // this.end = this.start;

        this.current = s;

        return s;

    }

    public void setText (String t)
    {

        this.text = t;
        this.sentenceIter.setText (t);
        this.start = this.sentenceIter.first ();

    }

    public int init (int offset)
    {

        if (offset == BreakIterator.DONE)
        {

            offset = 0;

        }

        if (offset == 0)
        {

            this.start = this.sentenceIter.first ();
            this.end = this.start;
            return this.start;

        }

        if (offset >= this.text.length ())
        {

            offset = this.text.length ();

        }
/*
        if (offset >= this.sentenceIter.last ())
        {

            return this.init (0);

        }
*/
        int pi = this.sentenceIter.preceding (offset);
        int ni = this.sentenceIter.following (pi);

        if ((pi < offset)
            &&
            (ni > offset)
           )
        {

            this.sentenceIter.preceding (offset);

        } else {

            this.sentenceIter.following (this.sentenceIter.preceding (offset));

        }

        this.start = this.sentenceIter.current ();

        this.end = this.start;

        return this.start;

    }

    public String current ()
    {

        return this.current;

    }

    public int getEndOffset ()
    {

        return this.end;

    }

    public int getOffset ()
    {

        return this.start;

    }

}
