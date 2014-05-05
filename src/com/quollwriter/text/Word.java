package com.quollwriter.text;

import java.util.*;

public class Word implements TextBlock<Sentence, Word, NoTextBlock>
{
    
    private Sentence parent = null;
    private int start = -1;
    private String word = null;
    private Word next = null;
    private Word previous = null;
    private boolean inDialogue = false;
    private int syllableCount = -1;
    private boolean isPunctuation = false;
    
    public Word (Sentence parent,
                 String   word,
                 int      start,
                 boolean  inDialogue)
    {
        
        this.parent = parent;
        this.word = word;
        this.start = start;
        this.inDialogue = inDialogue;
        
        if (word.length () == 1)
        {
            
            this.isPunctuation = !Character.isLetterOrDigit (word.charAt (0));

            if (this.isPunctuation)
            {
                
                this.syllableCount = 0;
                
            }
            
        }
                
    }

    public boolean isPunctuation ()
    {
        
        return this.isPunctuation;
        
    }
    
    public int getSyllableCount ()
    {

        if (this.syllableCount == -1)
        {
            
            this.syllableCount = TextUtilities.getSyllableCountForWord (this.word);
            
        }
  
        return this.syllableCount;
        
    }
    
    public Word getWordsAhead (int count)
    {
        
        Word w = this;
        
        for (int i = 0; i < count; i++)
        {
            
            w = w.getNext ();
            
            if (w == null)
            {
                
                return null;
                
            }
            
        }
        
        return w;
        
    }
    
    public boolean isInDialogue ()
    {
        
        return this.inDialogue;
        
    }
    
    public int getAllTextStartOffset ()
    {
        
        return this.parent.getAllTextStartOffset () + this.start;
        
    }
    
    public int getAllTextEndOffset ()
    {
        
        return this.parent.getAllTextStartOffset () + this.getEnd ();
        
    }
        
    public List<NoTextBlock> getChildren ()
    {
        
        return null;
        
    }
    
    public String getText ()
    {
        
        return this.word;
        
    }
    
    public int getEnd ()
    {
        
        return this.start + this.word.length ();
        
    }
    
    public int getStart ()
    {
        
        return this.start;
        
    }
    
    public Sentence getParent ()
    {
        
        return this.parent;
        
    }
    
    public Sentence getSentence ()
    {
        
        return this.getParent ();
        
    }
    
    public Word getNext ()
    {
        
        return this.next;
        
    }
    
    public boolean textEquals (String w)
    {
        
        if (w == null)
        {
            
            return false;
            
        }
        
        return this.word.equalsIgnoreCase (w);
        
    }
    
    public void setNext (Word w)
    {
        
        this.next = w;
        
    }
    
    public Word getPrevious ()
    {
        
        return this.previous;
        
    }
    
    public void setPrevious (Word w)
    {
        
        this.previous = w;
        
    }
    
    public String toString ()
    {
        
        return "[" + this.start + ", " + this.word + "]";
        
    }
    
}