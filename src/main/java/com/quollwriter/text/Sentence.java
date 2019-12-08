package com.quollwriter.text;

import java.util.*;
import java.text.*;

import com.quollwriter.*;
import com.quollwriter.ui.components.*;

public class Sentence implements TextBlock<Paragraph, Sentence, Word>
{

    private Paragraph parent = null;
    private int start = 0;
    private String sentence = null;
    private List<Word> words = new ArrayList<> ();
    private Sentence next = null;
    private Sentence previous = null;
    private int syllableCount = -1;
    private int wordCount = -1;

    public Sentence (String      sentence,
                     DialogueInd inDialogue)
    {

        if (sentence == null)
        {

            return;

        }

        Word last = null;

        BreakIterator iter = BreakIterator.getWordInstance ();
        iter.setText (sentence);

        int sl = sentence.length ();

        int st = iter.first ();

        for (int end = iter.next (); end != BreakIterator.DONE; st = end, end = iter.next ())
        {

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

        this.sentence = sentence;//.trim ();

    }

    public Sentence (Paragraph   parent,
                     String      sentence,
                     int         start,
                     DialogueInd inDialogue)
    {

        this (sentence,
              inDialogue);

        this.parent = parent;
        this.start = start;

    }

    public int getThreeSyllableWordCount ()
    {

        int c = 0;

        for (Word w : this.words)
        {

            if (w.getSyllableCount () > 2)
            {

                c++;

            }

        }

        return c;

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

    /**
     * Get the nth word in the list of words this sentence models, words are indexed from 0.
     *
     * param i The word index.
     * @returns The word at index i or null if there isn't one.
     */
    public Word getWord (int i)
    {

        if (i < 0 || i > this.words.size () - 1)
        {

            return null;

        }

        return this.words.get (i);

    }

    /**
     * Get the word at the specifed offset into the sentence text.  This is the word at the nth character in the sentence,
     * characters start at 0.
     *
     * @param offset The nth character.
     * @returns The word at that character or null if there isn't one.
     */
    public Word getWordAt (int offset)
    {

        for (Word w : this.words)
        {

            if ((w.getEnd () - 1 >= offset)
                &&
                (w.getStart () <= offset)
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

    public int getIndexOfFirstTextWord ()
    {

        for (int i = 0; i < this.words.size (); i++)
        {

            if (!this.words.get (i).isPunctuation ())
            {

                return i;

            }

        }

        return -1;

    }

    public int getIndexOfLastTextWord ()
    {

        for (int i = this.words.size () - 1; i > -1; i--)
        {

            if (!this.words.get (i).isPunctuation ())
            {

                return i;

            }

        }

        return -1;

    }

    /**
     * Look for the text in the sentence.
     *
     * @param find The text to find.
     * @param constraints Limit the search to the specified constraints.
     * @return A set of positions within the sentence of the specified words, this returns the word index in the sentence.
     *         So if the 3rd and 5th words matches then 3, 5 would be returned.  Words are indexed from 0.
     */
    public NavigableSet<Integer> find (List<Word>          findWords,
                                       DialogueConstraints constraints)
    {

        if (constraints == null)
        {

            constraints = new DialogueConstraints (false,
                                                   false,
                                                   DialogueConstraints.ANYWHERE);

        }

        NavigableSet<Integer> ret = new TreeSet ();

        for (Integer ind : TextUtilities.find (this.words,
                                               findWords,
                                               true))
        {

            Word fw = this.words.get (ind);

            if ((constraints.ignoreInDialogue)
                &&
                (fw.isInDialogue ())
               )
            {

                continue;

            }

            if ((constraints.onlyInDialogue)
                &&
                (!fw.isInDialogue ())
               )
            {

                continue;

            }

            if ((constraints.where.equals (DialogueConstraints.START))
                &&
                (ind != this.getIndexOfFirstTextWord ())
               )
            {

                continue;

            }

            if ((constraints.where.equals (DialogueConstraints.END))
                &&
                ((findWords.size () + ind - 1) != this.getIndexOfLastTextWord ())
               )
            {

                continue;

            }

            ret.add (ind);

        }

        return ret;
        /*
        if (true)
        {

            return ret;

        }

        int fc = findWords.size ();

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

                    Word fw = findWords.get (j);

                    if (!fw.getText ().equals (TextUtilities.ANY_WORD))
                    {

                        Word w2 = words.get (i + wfc);

                        if (!w2.textEquals (fw))
                        {

                            match = false;

                            break;

                        } else
                        {

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
*/
    }

    /**
     * Look for the text in the sentence.
     *
     * @param find The text to find.
     * @param constraints Limit the search to the specified constraints.
     * @return A set of positions within the sentence of the specified words.
     */
    public NavigableSet<Integer> find (String              find,
                                       DialogueConstraints constraints)
    {

        List<Word> findWords = new Sentence (find,
                                             new DialogueInd ()).getWords ();

        return this.find (findWords,
                          constraints);

    }

    /**
     * Return where the sentence is within the overall block of text, this is the
     * this.getParent ().getTextStartOffset () + this.getStart ()
     *
     * @return The offset within the overall text.
     */
    public int getAllTextStartOffset ()
    {

        if (this.parent == null)
        {

            return this.start;

        }

        return this.parent.getAllTextStartOffset () + this.start;

    }

    public int getAllTextEndOffset ()
    {

        if (this.parent == null)
        {

            return this.getEnd ();

        }

        return this.parent.getAllTextStartOffset () + this.getEnd ();

    }

    public int getEnd ()
    {

        return this.start + this.sentence.length () - 1;

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

    @Override
    public String toString ()
    {

        Map<String, Object> data = new LinkedHashMap ();

        data.put ("type",
                  "sentence");
        data.put ("wordCount",
                  this.getWordCount ());
        data.put ("text",
                  this.sentence);
        data.put ("textLength",
                  this.sentence.length ());
        data.put ("start",
                  this.getStart ());
        data.put ("end",
                  this.getEnd ());
        data.put ("syllableCount",
                  this.getSyllableCount ());
        data.put ("threeSyllableWordCount",
                  this.getThreeSyllableWordCount ());

        return Environment.formatObjectToStringProperties (data);

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

            return this.sentence;

        }

        Markup pm = new Markup (m,
                                this.getAllTextStartOffset (),
                                this.getAllTextEndOffset ());

        pm.shiftBy (-1 * this.getAllTextStartOffset ());

        return pm.markupAsHTML (this.sentence);

    }

}
