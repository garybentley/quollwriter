package com.quollwriter.text;

import java.util.*;
import java.text.*;

public class Sentence implements TextBlock<Paragraph, Sentence, Word>
{
    
    private Paragraph parent = null;
    private int start = -1;
    private String sentence = null;
    private List<Word> words = new ArrayList ();
    private Sentence next = null;
    private Sentence previous = null;
    private int syllableCount = -1;
    private int wordCount = -1;
    
    public Sentence (Paragraph   parent,
                     String      sentence,
                     int         start,
                     DialogueInd inDialogue)
    {
        
        this.parent = parent;
        this.sentence = sentence;
        this.start = start;

        Word last = null;
                
        BreakIterator iter = BreakIterator.getWordInstance ();
        iter.setText (sentence);
                
        int sl = sentence.length ();
        
        int st = iter.first ();
        
        for (int end = iter.next (); end != BreakIterator.DONE; st = end, end = iter.next ())
        {

            // Peek ahead to look for a single char.
            if (end < sl)
            {
 
                char c = sentence.charAt (end);
                
                if ((c == '\'') ||
                    (c == '\u2019'))
                {

                    String con = null;
                
                    // Peek ahead again to look for a contraction.
                    if (end + 3 < sl)
                    {
                        
                        con = sentence.substring (end + 1,
                                                  end + 3);
                        
                    }
                
                    if (end + 2 < sl)
                    {
                        
                        con = sentence.substring (end + 1,
                                                  end + 2);
                        
                    }

                    if (TextUtilities.isContractionEnd (con))
                    {
                        
                        // This is a contraction.
                        end = end + 1 + con.length ();
                        
                        // Reposition the iterator.
                        iter.next ();
                        iter.next ();
                        
                    }
                
                }                
                
            }

            String word = sentence.substring (st,
                                              end);

            if (word.trim ().equals (""))
            {

                continue;

            }
                        
            char c = ' ';

            if (word.length () == 1)
            {
                
                // Should an opening quote at the start of a sentence always mean
                // that the sentence is in dialogue?  
                
                c = word.charAt (0);
                
            }
            
            Word w = new Word (this,
                               word,
                               st,
                               inDialogue.update (c));
            
            this.words.add (w);

            if (last != null)
            {
                
                w.setPrevious (last);
                last.setNext (w);
                
            }
                               
            last = w;
            
        }        
        
    }
    
    public int getSyllableCount ()
    {
        
        if (this.syllableCount == -1)
        {
                    
            int c = 0;
            
            for (Word w : this.words)
            {
                
                c += w.getSyllableCount ();
                
            }
        
            this.syllableCount = c;
        
        }

        return this.syllableCount;
        
    }
        
    public Word getLastWord ()
    {
        
        return this.words.get (this.words.size () - 1);        
        
    }
    
    public int getWordCount ()
    {
        
        int c = 0;
        
        if (this.wordCount == -1)
        {
        
            for (Word w : this.words)
            {
                
                if (w.isPunctuation ())
                {
                    
                    continue;
                    
                }
                
                c++;
                
            }
            
            this.wordCount = c;
        
        }
        
        return this.wordCount;
                
    }
    
    public Word getWordAt (int i)
    {
        
        for (Word w : this.words)
        {
            
            if ((w.getEnd () >= i)
                &&
                (w.getStart () <= i)
               )
            {
                
                return w;
                
            }
            
        }
        
        return null;
        
    }
    
    public List<Word> getChildren ()
    {
        
        return this.words;
        
    }
    
    public List<Word> getWords ()
    {
        
        return this.getChildren ();
        
    }
    
    public String getText ()
    {
        
        return this.sentence;
        
    }

    /**
     * Look for the collection of words in the sentence.
     *
     * @param find The words to find.
     * @param constraints Limit the search to the specified constraints.
     * @return A set of positions within the sentence of the specified words.
     */
    public Set<Integer> find (Collection<String>  find,
                              DialogueConstraints constraints)
    {
        
        if (constraints == null)
        {
            
            constraints = new DialogueConstraints (false,
                                                   false,
                                                   DialogueConstraints.ANYWHERE);
            
        }
        
        List<String> _find = new ArrayList (find);
        
        Set<Integer> ret = new LinkedHashSet ();

        int fc = _find.size ();

        int wc = this.words.size ();

        int firstWord = -1;
                
        for (int i = 0; i < wc; i++)
        {

            Word w = this.words.get (i);

            if ((constraints.ignoreInDialogue) &&
                (w.isInDialogue ()))
            {

                continue;

            }

            if ((constraints.onlyInDialogue) &&
                (!w.isInDialogue ()))
            {
                
                continue;

            }

            int wfc = 0;

            if ((i + fc) < (wc + 1))
            {

                boolean match = false;

                for (int j = 0; j < fc; j++)
                {

                    String fw = _find.get (j);

                    if (!fw.equals (TextUtilities.ANY_WORD))
                    {

                        Word w2 = words.get (i + wfc);

                        if ((constraints.ignoreInDialogue) &&
                            (w2.isInDialogue ()))
                        {

                            wfc++;

                            continue;

                        }

                        if ((constraints.onlyInDialogue) &&
                            (!w2.isInDialogue ()))
                        {

                            wfc++;
                            
                            continue;

                        }

                        if (!w2.textEquals (fw))
                        {

                            match = false;

                            break;

                        } else
                        {

                            match = true;

                        }

                    }

                    wfc++;

                }

                // If we are here then all words match.
                if (match)
                {

                    if (constraints.where.equals (DialogueConstraints.START))
                    {

                        // Make sure i is 0 or perhaps 1 ignoring a punctuation character.
                        if (i > 0)
                        {

                            for (int m = i - 1; m > -1; m--)
                            {

                                if (TextUtilities.isWord (this.words.get (m).getText ()))
                                {

                                    // Can't match.
                                    return ret;

                                }

                            }

                        }

                    } else
                    {

                        if (constraints.where.equals (DialogueConstraints.END))
                        {

                            // Check to make sure that the match is at the end, ignoring a trailing punctation char.
                            if (i < (wc - 1))
                            {

                                for (int m = i + fc; m < wc; m++)
                                {

                                    if (TextUtilities.isWord (this.words.get (m).getText ()))
                                    {

                                        // Can't match.
                                        return ret;

                                    }

                                }

                            }

                        }

                    }

                    ret.add (this.words.get (i).getStart ());// + wfc - find.size ());

                }

            } else
            {

                // Couldn't possibly match.
                return ret;

            }

        }

        return ret;
                
    }    
    
    /**
     * Return where the sentence is within the overall block of text, this is the
     * this.getParent ().getTextStartOffset () + this.getStart ()
     *
     * @return The offset within the overall text.
     */
    public int getAllTextStartOffset ()
    {
        
        return this.parent.getAllTextStartOffset () + this.start;
        
    }
    
    public int getAllTextEndOffset ()
    {
        
        return this.parent.getAllTextStartOffset () + this.getEnd ();
        
    }

    public int getEnd ()
    {
        
        return this.start + this.sentence.length ();
        
    }
    
    /**
     * Return where the sentence is within the parent paragraph.
     *
     * @return The start offset.
     */
    public int getStart ()
    {
        
        return this.start;
        
    }
    
    public Paragraph getParent ()
    {
        
        return this.parent;
        
    }
    
    public Paragraph getParagraph ()
    {
        
        return this.parent;
        
    }
    
    public Sentence getPrevious ()
    {
        
        return this.previous;
        
    }
    
    public void setPrevious (Sentence s)
    {
        
        this.previous = s;
        
    }
    
    public Sentence getNext ()
    {
        
        return this.next;
        
    }
    
    public void setNext (Sentence s)
    {
        
        this.next = s;
        
    }
    
    public String toString ()
    {
        
        return "[start: " + this.start + ", length: " + this.sentence.length () + ", words: " + this.getWordCount () + ", syllables: " + this.getSyllableCount () + ", text: " + this.sentence + "]";
        
    }
    
}