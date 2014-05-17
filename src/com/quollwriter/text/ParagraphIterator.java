package com.quollwriter.text;

import java.text.*;

import java.util.*;

import com.quollwriter.text.rules.*;

@Deprecated 

public class ParagraphIterator implements Iterator<String>
{

    private String        text = null;
    private String        current = null;
    private int           start = 0;
    private int           end = 0;
    private boolean       inDialogue = false;

    public ParagraphIterator (String text)
    {

        this.text = text;

    }

    public void reinit (String text)
    {
        
        this.text = text;
                        
    }

    public ParagraphIterator clone ()
    {

        ParagraphIterator si = new ParagraphIterator (this.text);
        si.start = this.start;
        si.end = this.end;
        si.inDialogue = this.inDialogue;
                
        si.reinit (this.text);

        si.initCurrent ();
        
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

    private int getNextPosition (int start)
    {
        
        if (start == this.text.length () - 1)
        {
            
            return BreakIterator.DONE;
            
        }
        
        int i = this.text.indexOf ('\n',
                                   start + 1);
        
        return (i > -1 ? i : this.getLastPosition ());        
        
    }
    
    private int getPreviousPosition ()
    {
        
        return this.getPreviousPosition (this.start);
        
    }
    
    private int getPreviousPosition (int start)
    {
        
        if (this.text.length () == 0)
        {
            
            return BreakIterator.DONE;
            
        }
        
        if (start < 0)
        {
            
            return 0;
            
        }
        
        for (int i = start - 1; i > -1; i--)
        {
            
            if (this.text.charAt (i) == '\n')
            {
                
                return i;
                
            }
            
        }
        
        return 0;
        
    }
    
    private int getNextPosition ()
    {
        
        return this.getNextPosition (this.start);
        
    }
    
    private int getLastPosition ()
    {
        
        if (this.text.length () == 0)
        {
            
            return BreakIterator.DONE;
            
        }
        
        return this.text.length () - 1;
    /*
                
        // Cycle back from the end to find the first \n.
        int l = this.text.length ();
        
        for (int i = l - 1; i > -1; i--)
        {
            
            if (this.text.charAt (i) == '\n')
            {
                
                return i + 1;
                
            }
            
        }
        
        return BreakIterator.DONE;
      */  
    }
    
    private int getFirstPosition ()
    {
        
        return 0;
        
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

        this.start = this.getLastPosition ();

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

        this.start = this.end;

        if (this.start == BreakIterator.DONE)
        {

            return null;

        }

        // Get the sentence.
        this.end = this.getNextPosition ();//iter.next ();
        
        if (this.end == BreakIterator.DONE)
        {

            if (this.start < this.text.length () - 1)
            {
                
                this.end = this.text.length ();

            } else {

                return null;
            
            }

        }

        return this.initCurrent ();

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
        this.start = this.getPreviousPosition ();

        if (this.start == BreakIterator.DONE)
        {

            this.start = 0;

            if (this.start == this.end)
            {
                
                return null;
                
            }
        
        }
                
        if (this.start == this.end)
        {
            
            return null;
                
        }
        
        return this.initCurrent ();

    }

    public void setText (String t)
    {

        this.text = t;
        this.init (0);

    }

    public int init (int offset)
    {
/*
        if (offset == 0)
        {
            xxx
            this.start = this.getFirstPosition (); 
            this.end = this.start;
            return this.start;
            
        }
*/

        if (offset >= this.getLastPosition ())
        {

            start = this.getLastPosition ();
        
            this.end = this.start;

            return this.start;
            
        }

        this.start = this.getPreviousPosition (offset);
        this.end = this.getNextPosition (this.start);

        if (this.start < 0)
        {
            
            return 0;
            
        }
        
        this.initCurrent ();
        
        return this.start;
        /*
        int pi = this.getPreviousPosition (offset);
        int ni = this.getNextPosition (pi);

        if ((pi < offset)
            &&
            (ni > offset)
           )
        {
            
            this.start = pi;

        } else {
            
            this.start = ni;

        }
            
        if (pi == offset)
        {
            
            this.start = pi;
            
        }
        
        if (this.start < 0)
        {
            
            this.start = 0;
            
        }
        
        this.end = this.start;

        return this.start;
*/
    }

    private String initCurrent ()
    {
        
        if (this.end < this.start)
        {
            
            return null;
            
        }
        
        if (this.start == this.end)
        {
            
            return null;
            
        }

        this.current = this.text.substring (this.start + 1,
                                            this.end);
        
        return this.current;
        
    }
    
    public String current ()
    {
                
/*
        if (this.current == null)
        {
            
            int n = this.getNextPosition ();

            if ((n > this.text.length ())
                ||
                (n == BreakIterator.DONE)
               )
            {
                
                n = this.text.length ();
                
            }
            
            this.current = this.text.substring (this.start,
                                                n);
            
        }
  */  
        return this.current;

    }

    public int getEndOffset ()
    {
        
        return this.end - 1;
        
    }
    
    public int getOffset ()
    {

        return this.start + 1;

    }

}
