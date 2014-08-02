package com.quollwriter.text;

import java.text.*;
import java.util.*;

public class TextIterator
{
    
    private List<Paragraph> paragraphs = new ArrayList ();
    
    public TextIterator (String t)
    {
        
        //ParagraphIterator pi = new ParagraphIterator (t);
        
        //int st = pi.init (0);
        
        int l = t.length ();
                
        Paragraph last = null;
        
        int start = 0;
        
        for (int i = 0; i < l; i++)
        {
            
            if (t.charAt (i) == '\n')
            {
                
                String pt = t.substring (start,
                                         i).trim ();
                
                if (pt.length () == 0)
                {
                    
                    start = i + 1;
                    
                    continue;
                    
                }
                
                Paragraph p = new Paragraph (t.substring (start,
                                                          i),
                                             start);
                
                this.paragraphs.add (p);
                
                if (last != null)
                {
                    
                    p.setPrevious (last);
                    last.setNext (p);
                    
                }
                
                start = i + 1;                
                                
                last = p;
                                
            }            
            
        }

        String lt = t.substring (start,
                                 l).trim ();
        
        if (lt.length () > 0)
        {

            Paragraph p = new Paragraph (lt,
                                         start);
    
            if (last != null)
            {
    
                p.setPrevious (last);
                last.setNext (p);
                
            }
            
            this.paragraphs.add (p);

        }
                
    }
        
    /**
     * Look for the collection of words and return a set of all text indexes (indexes within the entire text) for the
     * matches.
     *
     * @param words The words to look for.
     * @param constraints Limit the search to the specified constraints.
     * @return A navigable set of all text indexes of the matches.
     */
    public NavigableSet<Integer> findAllTextIndexes (String              words,
                                                     DialogueConstraints constraints)
    {

        NavigableSet<Integer> matches = new TreeSet ();
        
        for (Paragraph p : this.paragraphs)
        {
            
            for (Sentence s : p.getSentences ())
            {
                
                int st = s.getAllTextStartOffset ();

                // These indexes will be sentence local.
                NavigableSet<Integer> found = s.find (words,
                                                      constraints);
                    
                if (found != null)
                {
                    
                    for (Integer i : found)
                    {
                        
                        matches.add (i + st);
                        
                    }
                    
                }
                
            }
            
        }
        
        return matches;
                
    }
    
    /**
     * Look for the collection of words in each sentence.
     *
     * @param words The words to look for.
     * @param constraints Limit the search to the specified constraints.
     * @return A set of sentences with the matching sentence local indexes.
     */
    public Map<Sentence, NavigableSet<Integer>> findInSentences (String              words,
                                                                 DialogueConstraints constraints)
    {
        
        Map<Sentence, NavigableSet<Integer>> matches = new LinkedHashMap ();
        
        for (Paragraph p : this.paragraphs)
        {
            
            matches.putAll (p.findInSentences (words,
                                               constraints));
            
        }
        
        return matches;
        
    }
        
    public Sentence getSentenceAt (int i)
    {
        
        Paragraph p = this.getParagraphAt (i);
        
        return p.getSentenceAt (i - p.getAllTextStartOffset ());
        
    }

    /**
     * For index i, find the paragraph that either contains the index or the previous paragraph closest
     * to the index.
     *
     * @param i The index.
     * @return The paragraph.
     */
    public Paragraph getPreviousClosestParagraphTo (int i)
    {
        
        Paragraph f = this.getParagraphAt (i);
        
        if (f != null)
        {
            
            return f;
            
        }
        
        int l = this.paragraphs.size () - 1;
        
        for (int j = l; j > -1; j--)
        {
            
            Paragraph p = this.paragraphs.get (j);
            
            if (p.getAllTextStartOffset () <= i)
            {
                
                f = p;
                break;
                
            }
            
        }
        
        return f;
        
    }

    /**
     * For index i, find the sentence that either contains the index or the previous sentence closest
     * to the index.
     *
     * @param i The index.
     * @return The sentence.
     */
    public Sentence getPreviousClosestSentenceTo (int i)
    {
                
        Paragraph p = this.getPreviousClosestParagraphTo (i);
        
        if (p != null)
        {
            
            return p.getPreviousClosestSentenceTo (i);
            
        }
                
        return null;
        
    }
    
    /**
     * For index i, find the paragraph that either contains the index or the following paragraph closest
     * to the index.
     *
     * @param i The index.
     * @return The paragraph.
     */
    public Paragraph getNextClosestParagraphTo (int i)
    {
        
        Paragraph f = this.getParagraphAt (i);
        
        if (f != null)
        {
            
            return f;
            
        }
        
        for (Paragraph p : this.paragraphs)
        {
            
            if ((p.getAllTextStartOffset () >= i)
                &&
                (i <= p.getAllTextEndOffset ())
               )
            {
                
                f = p;
                break;
                
            }
            
        }
        
        return f;
        
    }
    
    /**
     * For index i, find the sentence that either starts on that boundary or the following sentence closest
     * to the boundary.
     *
     * @param i The index.
     * @return The sentence.
     */
    public Sentence getNextClosestSentenceTo (int i)
    {
                
        Paragraph p = this.getNextClosestParagraphTo (i);
        
        if (p != null)
        {
            
            return p.getNextClosestSentenceTo (i);
            
        }
                
        return null;
        
    }

    public Paragraph getParagraphAt (int i)
    {
        
        for (Paragraph p : this.paragraphs)
        {
            
            if ((p.getAllTextEndOffset () >= i)
                &&
                (p.getAllTextStartOffset () <= i)
               )
            {
                
                return p;
                
            }
            
        }
        
        return null;
        
    }
    
    public List<Paragraph> getParagraphs ()
    {
        
        return this.paragraphs;
        
    }
    
    public Paragraph getLastParagraph ()
    {
        
        Paragraph p = null;
        
        for (Paragraph pp : this.paragraphs)
        {
            
            p = pp;
            
        }
        
        return p;
        
    }
    
    public Paragraph getFirstParagraph ()
    {
                
        for (Paragraph p : this.paragraphs)
        {
            
            return p;
            
        }
        
        return null;
        
    }

    public int getThreeSyllableWordCount ()
    {

        int c = 0;
        
        for (Paragraph p : this.paragraphs)
        {
            
            c += p.getThreeSyllableWordCount ();
            
        }
        
        return c;        

    }
    
    public int getSyllableCount ()
    {
        
        int c = 0;
        
        for (Paragraph p : this.paragraphs)
        {
            
            c += p.getSyllableCount ();
            
        }
        
        return c;        
        
    }
    
    public int getWordCount ()
    {
        
        int c = 0;
        
        for (Paragraph p : this.paragraphs)
        {
            
            c += p.getWordCount ();
            
        }
        
        return c;
        
    }

    public int getSentenceCount ()
    {

        int c = 0;
        
        for (Paragraph p : this.paragraphs)
        {
            
            c += p.getSentenceCount ();
            
        }
        
        return c;
        
    }
    
    public String toString ()
    {
        
        return "" + this.paragraphs.size ();
        
    }
    
}