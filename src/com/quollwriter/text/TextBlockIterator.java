package com.quollwriter.text;

import java.util.*;

public class TextBlockIterator implements Iterator<TextBlock>
{
    
    private TextBlock current = null;
    private boolean firstDone = false;
    
    public TextBlockIterator (String t)
    {
        
        if (t == null)
        {
            
            return;
            
        }
        
        TextIterator ti = new TextIterator (t);
        
        this.current = ti.getFirstParagraph ();
        
    }
    
    public TextBlockIterator (TextIterator ti)
    {

        if (ti == null)
        {
            
            return;
            
        }
        
        this.current = ti.getFirstParagraph ();
        
        
    }
    
    public TextBlockIterator (Sentence s)
    {
        
        this.current = s;        
        
    }
    
    public TextBlockIterator (Paragraph p)
    {
        
        this.current = p;        
        
    }
        
    public boolean hasNext ()
    {
        
        return this.next () != null;
                
    }
    
    public boolean hasPrevious ()
    {
        
        return this.previous () != null;
        
    }
    
    public boolean hasNextSentence ()
    {
        
        return this.nextSentence () != null;
        
    }
    
    public boolean hasPreviousSentence ()
    {
        
        return this.previousSentence () != null;
        
    }

    public boolean hasNextParagraph ()
    {
        
        return this.nextParagraph () != null;
        
    }
    
    public boolean hasPreviousParagraph ()
    {
        
        return this.previousParagraph () != null;
        
    }
    
    public TextBlock current ()
    {
        
        return this.current;
        
    }
    
    public TextBlock next ()
    {
        
        if (!this.firstDone)
        {
            
            this.firstDone = true;
            
            return this.current;
            
        }
        
        if (this.current instanceof Sentence)
        {
            
            Sentence n = (Sentence) this.current.getNext ();
            
            if (n == null)
            {
                
                n = (Sentence) this.current;
                
                this.current = (Paragraph) n.getParagraph ().getNext ();
                                
            } else {
            
                this.current = n;
                
            }
                        
        }
        
        if (this.current instanceof Paragraph)
        {
            
            this.current = ((Paragraph) this.current).getFirstSentence ();
                        
        }
                
        return this.current;
        
    }
    
    public TextBlock previous ()
    {

        if (!this.firstDone)
        {
            
            this.firstDone = true;
            
            return this.current;
            
        }
    
        if (this.current instanceof Sentence)
        {
            
            Sentence n = ((Sentence) this.current).getPrevious ();
            
            if (n == null)
            {
                
                n = (Sentence) this.current;
                
                this.current = n.getParagraph ().getPrevious ();
                                
            } else {
            
                this.current = n;
                
            }
                        
        }
        
        if (this.current instanceof Paragraph)
        {
            
            this.current = ((Paragraph) this.current).getPrevious ().getLastSentence ();
                        
        }
                
        return this.current;
        
    }
    
    public Sentence previousSentence ()
    {

        TextBlock prev = this.previous ();
        
        if (prev instanceof Paragraph)
        {
            
            this.current = ((Paragraph) prev).getLastSentence ();
            
        }
    
        return (Sentence) this.current;
            
    }
    
    public Sentence nextSentence ()
    {
        
        TextBlock next = this.next ();
        
        if (next instanceof Paragraph)
        {
            
            this.current = ((Paragraph) next).getFirstSentence ();
            
        }
    
        return (Sentence) this.current;
        
    }
    
    public Paragraph nextParagraph ()
    {
        
        TextBlock next = this.next ();
        
        if (next instanceof Sentence)
        {
            
            this.current = ((Sentence) next).getParagraph ().getNext ();
            
        }
        
        return (Paragraph) this.current;
        
    }
        
    public Paragraph previousParagraph ()
    {
        
        TextBlock prev = this.previous ();
        
        if (prev instanceof Sentence)
        {
            
            this.current = ((Sentence) prev).getParagraph ().getPrevious ();
            
        }
        
        return (Paragraph) this.current;
        
    }

    public void remove ()
    {
        
        throw new UnsupportedOperationException ("Not supported.");
        
    }
    
}