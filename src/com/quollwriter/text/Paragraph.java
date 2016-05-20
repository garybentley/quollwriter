package com.quollwriter.text;

import java.text.*;
import java.util.*;

import com.quollwriter.*;
import com.quollwriter.ui.components.*;

public class Paragraph implements TextBlock<NoTextBlock, Paragraph, Sentence>
{
    
    private List<Sentence> sentences = new ArrayList ();
    private int start = -1;
    private String paragraph = null;
    private Paragraph next = null;
    private Paragraph prev = null;
    private int wordCount = -1;
    private List<Word> words = null;
    private DialogueInd inDialogue = null;
    
    public Paragraph (String paragraph,
                      int    start)
    {
        
        this.start = start;
        
        if (paragraph == null)
        {
            
            return;
            
        }
        
        // Split up the sentences.
        BreakIterator bi = BreakIterator.getSentenceInstance ();
        
        bi.setText (paragraph);
        
        Sentence last = null;
        
        int st = bi.first ();
        int en = -1;
        
        int openQuoteCount = 0;
        
        this.inDialogue = new DialogueInd ();
                
        while (st != BreakIterator.DONE)
        {
            
            en = bi.next ();
            
            if (en == BreakIterator.DONE)
            {
                
                break;
                
            }
     
            String t = paragraph.substring (st,
                                            en);
            
            if (t.length () == 0)
            {
                
                st = en;
                
                continue;
                
            }
                        
            Sentence s = new Sentence (this,
                                       t,
                                       st,
                                       this.inDialogue);
                                    
            this.sentences.add (s);
            
            if (last != null)
            {
                
                s.setPrevious (last);
                last.setNext (s);
                
            }
                   
            last = s;
                        
            st = en;
            
        }
        
        this.paragraph = paragraph;//.trim ();
                        
    }
    
    @Override
    public String toString ()
    {
                
        Map<String, Object> data = new LinkedHashMap ();
        
        data.put ("type",
                  "paragraph");
        data.put ("wordCount",
                  this.getWordCount ());
        data.put ("text",
                  this.paragraph);
        data.put ("textLength",
                  this.paragraph.length ());
        data.put ("start",
                  this.getStart ());
        data.put ("end",
                  this.getEnd ());
        data.put ("sentenceCount",
                  this.getSentenceCount ());
        data.put ("syllableCount",
                  this.getSyllableCount ());
        data.put ("threeSyllableWordCount",
                  this.getThreeSyllableWordCount ());
                
        return Environment.formatObjectToStringProperties (data);        
        
    }    
    
    public List<Word> getWords ()
    {
        
        List<Word> ret = null;
                    
        if (this.words == null)
        {
            
            ret = new ArrayList ();
            
            for (Sentence s : this.sentences)
            {
                
                ret.addAll (s.getWords ());
                
            }

            this.words = ret;
            
        }
                    
        return ret;
    
    }

    public int getThreeSyllableWordCount ()
    {

        int c = 0;
        
        for (Sentence s : this.sentences)
        {
            
            c += s.getThreeSyllableWordCount ();
            
        }
        
        return c;
        
    }
    
    public int getSyllableCount ()
    {

        int c = 0;
        
        for (Sentence s : this.sentences)
        {
            
            c += s.getSyllableCount ();
            
        }
        
        return c;
        
    }
    
    public int getSentenceCount ()
    {
        
        return this.sentences.size ();
        
    }
    
    public int getWordCount ()
    {
        
        if (this.wordCount > -1)
        {
            
            return this.wordCount;
            
        }
        
        int wc = 0;
        
        for (Sentence s : this.sentences)
        {
            
            wc += s.getWordCount ();
            
        }
        
        this.wordCount = wc;
        
        return wc;
        
    }
    
    public List<Sentence> getChildren ()
    {
        
        return this.sentences;
        
    }
    
    public List<Sentence> getSentences ()
    {
        
        return this.getChildren ();
        
    }

    /**
     * For index i, find the sentence that either contains the index or the following sentence closest
     * to the index.  This uses the internal paragraph index not the all text index.
     *
     * @param i The index.
     * @return The sentence.
     */
    public Sentence getNextClosestSentenceTo (int i)
    {
        
        Sentence f = this.getSentenceAt (i);
        
        if (f != null)
        {
            
            return f;
            
        }
        
        for (Sentence s : this.sentences)
        {
            
            if (s.getStart () >= i)
            {
                
                f = s;
                break;
                
            }
            
        }
        
        return f;
        
    }
    
    /**
     * For index i, find the sentence that either contains the index or the previous sentence closest
     * to the index.  This uses the internal paragraph index not the all text index.
     *
     * @param i The index.
     * @return The sentence.
     */
    public Sentence getPreviousClosestSentenceTo (int i)
    {
        
        Sentence f = this.getSentenceAt (i);
        
        if (f != null)
        {
            
            return f;
            
        }
        
        int l = this.sentences.size () - 1;
        
        for (int j = l; j > -1; j--)
        {
            
            Sentence s = this.sentences.get (j);
            
            if (s.getStart () <= i)
            {
                
                f = s;
                break;
                
            }
            
        }
        
        return f;
        
    }

    /**
     * Return where the paragraph is within the overall block of text, this is the
     * same as this.getStart ()
     *
     * @return The offset within the overall text.
     */
    public int getAllTextStartOffset ()
    {
        
        return this.getStart ();
        
    }
    
    public int getAllTextEndOffset ()
    {
        
        return this.getEnd ();
        
    }
    
    public int getEnd ()
    {
        
        return this.start + this.paragraph.length ();
        
    }
    
    /**
     * Return where the paragraph is in the overall text.
     *
     * @return The start offset.
     */
    public int getStart ()
    {
        
        return this.start;
        
    }
    
    public Sentence getSentenceAt (int i)
    {
        
        for (Sentence s : this.sentences)
        {
            
            if ((s.getEnd () >= i)
                &&
                (s.getStart () <= i)
               )
            {
                
                return s;
                
            }
            
        }
        
        return null;
        
    }
    
    public NoTextBlock getParent ()
    {
        
        return null;
        
    }
    
    public String getText ()
    {
        
        return this.paragraph;
        
    }
    
    public Paragraph getNext ()
    {
        
        return this.next;
        
    }
    
    public Paragraph getPrevious ()
    {
        
        return this.prev;
        
    }
    
    public void setNext (Paragraph p)
    {
        
        this.next = p;
        
    }
    
    public void setPrevious (Paragraph p)
    {
        
        this.prev = p;
        
    }
    
    public boolean isWordInDialogue (Word w)
    {
        
        return false;
        
    }

    public boolean isFirst (Sentence s)
    {
        
        if (s == null)
        {
            
            return false;
            
        }
        
        Sentence f = this.getFirstSentence ();
        
        if (f != null)
        {
            
            return f == s;
            
        }
        
        return false;
        
    }
    
    public Sentence getLastSentence ()
    {
        
        return this.sentences.get (this.sentences.size () - 1);
                
    }
    
    public Sentence getFirstSentence ()
    {
                
        return this.sentences.get (0);
        
    }
    
    /**
     * Look for the collection of words in each sentence.
     *
     * @param text The text to look for.
     * @param constraints Limit the search to the specified constraints.
     * @return The matching sentences with sentence local indexes of the matches.
     */
    public Map<Sentence, NavigableSet<Integer>> findInSentences (String              text,
                                                                 DialogueConstraints constraints)
    {
        
        Map<Sentence, NavigableSet<Integer>> matches = new LinkedHashMap ();
        
        for (Sentence s : this.sentences)
        {
            
            NavigableSet<Integer> inds = s.find (text,
                                                 constraints);
            
            if ((inds != null)
                &&
                (inds.size () > 0)
               )
            {
                
                matches.put (s,
                             inds);
                
            }
                        
        }
        
        return matches;
        
    }
 
    /**
     * Get the text in this paragraph, marked up as html with the markup passed in.
     * It assumes that the markup is relative to the getAllTextStartOffset.
     *
     * @param m The markup.
     * @return The html marked up string.
     */
    public String markupAsHTML (Markup m)
    {
        
        if (m == null)
        {
            
            return this.paragraph;
            
        }
        
        Markup pm = new Markup (m,
                                this.getAllTextStartOffset (),
                                this.getAllTextEndOffset ());
                    
        pm.shiftBy (-1 * this.getAllTextStartOffset ());
 
        return pm.markupAsHTML (this.paragraph);
 
    }
        
}