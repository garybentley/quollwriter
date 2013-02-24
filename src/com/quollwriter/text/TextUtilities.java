package com.quollwriter.text;

import java.text.*;

import java.util.*;

import com.quollwriter.*;


public class TextUtilities
{

    public static final String ANY_WORD = "*";

    private static Map<String, Integer> wordSyllableCounts = null;
    private static Map<String, String>  contractionEnds = new HashMap ();

    static
    {

        Map m = TextUtilities.contractionEnds;

        m.put ("m",
               "");
        m.put ("re",
               "");
        m.put ("s",
               "");
        m.put ("t",
               "");
        m.put ("ll",
               "");

    }

    // Open double quotation
    // "
    // \u201C
    // \u2033
    // \u201E

    // Close double quotation
    // "
    // \u201D
    // \u201F
    // \u2036

    public static boolean isCloseQ (char c)
    {

        return (c == '\u201d') ||
               (c == '\u2036') ||
               (c == '\u201f') ||
               (c == '"');

    }

    public static boolean isOpenQ (char c)
    {

        return (c == '\u201c') ||
               (c == '\u2033') ||
               (c == '\u201e') ||
               (c == '"');

    }

    public static int getWordPosition (String text,
                                       Issue  i)
    {

        BreakIterator iter = BreakIterator.getWordInstance ();
        iter.setText (text);

        int start = iter.first ();

        int c = 0;

        boolean singleQ = false;

        for (int end = iter.next (); end != BreakIterator.DONE; start = end, end = iter.next ())
        {

            String w = text.substring (start,
                                       end);

            if (w.trim ().equals (""))
            {

                continue;

            }

            if (singleQ)
            {

                singleQ = false;

                // Last character was a ' so check to see if this is a contraction.
                if (TextUtilities.contractionEnds.containsKey (w))
                {

                    continue;

                } else
                {

                    // The previous ' and now this word are separate.
                    c++;

                }

            }

            if (w.length () == 1)
            {

                if ((w.charAt (0) == '\'') ||
                    (w.charAt (0) == '\u2019'))
                {

                    singleQ = true;

                    continue;

                }

            }

            if (c == i.getStartWordPosition ())
            {

                return start;

            }

            c++;

        }

        return -1;

    }

    public static boolean isWord (String w)
    {

        return Character.isLetterOrDigit (w.charAt (0));

    }

    public static List<String> stripPunctuation (List<String> words)
    {

        List<String> ret = new ArrayList ();

        for (String w : words)
        {

            if (!Character.isLetterOrDigit (w.charAt (0)))
            {

                if (w.length () > 1)
                {

                    if (!Character.isLetterOrDigit (w.charAt (1)))
                    {

                        continue;

                    }

                } else
                {

                    continue;

                }

            }

            ret.add (w);

        }

        return ret;

    }

    public static List<String> getAsWords (String l)
    {

        List<String> ret = new ArrayList ();

        if (l == null)
        {

            return ret;

        }

        BreakIterator iter = BreakIterator.getWordInstance ();
        iter.setText (l);

        boolean singleQ = false;

        int start = iter.first ();

        for (int end = iter.next (); end != BreakIterator.DONE; start = end, end = iter.next ())
        {

            String w = l.substring (start,
                                    end);

            if (w.trim ().equals (""))
            {

                continue;

            }

            if (singleQ)
            {

                singleQ = false;

                // Last character was a ' so check to see if this is a contraction.
                if (TextUtilities.contractionEnds.containsKey (w))
                {

                    int rs = ret.size ();

                    // Remove the quote.
                    String q = ret.remove (rs - 1);

                    ret.set (rs - 2,
                             ret.get (rs - 2) + q + w);

                    continue;

                }

            }

            if (w.length () == 1)
            {

                if ((w.charAt (0) == '\'') ||
                    (w.charAt (0) == '\u2019'))
                {

                    singleQ = true;

                }

            }

            ret.add (w);

        }

        return ret;

    }

    public static int charCount (String l,
                                 char   c)
    {

        if (l == null)
        {

            return 0;

        }

        int co = 0;

        char[] chars = l.toCharArray ();

        for (int i = 0; i < chars.length; i++)
        {

            if (chars[i] == c)
            {

                co++;

            }

        }

        return co;

    }

    public static boolean hasWord (String  sentence,
                                   String  word,
                                   boolean ignoreDialogue)
    {

        if (sentence.indexOf (word) != -1)
        {

            return true;

        }

        return false;

    }

    /**
     * This method is based on:
     *    http://english.glendale.cc.ca.us/phonics.rules.html
     * and is a reimplementation of the algorithm found in:
     *   http://sourceforge.net/projects/flesh/ (net.sourceforge.flesh.countSyllables(String))
     */
    public static int getSyllableCountForWord (String word)
    {

        if (TextUtilities.wordSyllableCounts == null)
        {

            // Init the word counts.
            TextUtilities.wordSyllableCounts = new HashMap ();

            String c = null;

            try
            {

                c = Environment.getResourceFileAsString (Constants.WORD_SYLLABLE_COUNTS_FILE);

            } catch (Exception e)
            {

                Environment.logError ("Unable to init syllable counts",
                                      e);

                return 0;

            }

            StringTokenizer t = new StringTokenizer (c,
                                                     String.valueOf ('\n'));

            while (t.hasMoreTokens ())
            {

                String l = t.nextToken ();

                StringTokenizer tt = new StringTokenizer (l,
                                                          "|");

                while (tt.hasMoreTokens ())
                {

                    TextUtilities.wordSyllableCounts.put (tt.nextToken ().trim ().toLowerCase (),
                                                          Integer.valueOf (tt.nextToken ().trim ()));

                }

            }

        }

        word = word.toLowerCase ();

        Integer in = TextUtilities.wordSyllableCounts.get (word);

        if ((in != null) &&
            (in.intValue () > 0))
        {

            return in.intValue ();

        }

        // 'words' that are web addresses (like www.ncsu.edu/students/reg_records/tracks_link" should
        // not be counted to have something like 15 syllables, as this can really inflate
        // the score of a document. So words that seem to be web addresses are counted as 1 syllable,
        // as are email addresses (as many of those will reciev a syllable count > 10 syllables)
        if ((word.indexOf ("www") == 0) ||
            (word.indexOf ("http") == 0) ||
            (word.indexOf ("@") > 0) ||
            (word.indexOf (".co") > 0))
        {

            return 1;

        }

        int count = 0;

        int l = word.length ();

        char[] chars = word.toCharArray ();

        for (int i = 0; i < l; i++)
        {

            char c = chars[i];

            if (TextUtilities.isVowel (c))
            {

                // Check to see if the word will end with "ely" in which case add another.
                if (c == 'e')
                {

                    // The range check here prevents unnecessary computation.
                    if (((i + 3) == l) &&
                        (word.endsWith ("ely")))
                    {

                        count++;

                        return count;

                    }

                }

                // Are we at the end of the word?
                if ((i + 1) == l)
                {

                    // An "e" at the end of a word is silent but not other
                    // vowels such as i and y.
                    if (c != 'e')
                    {

                        count++;

                    } else
                    {

                        // Words that end with "le" get an extra syllable.
                        if (word.endsWith ("le"))
                        {

                            count++;

                        }

                    }

                } else
                {

                    // Look for the next consonant, i.e. something like ak, aem.
                    while (i < (l - 1))
                    {

                        // Ensure we don't check this char again.
                        i++;

                        c = chars[i];

                        if ((!TextUtilities.isVowel (c)) &&
                            (Character.isLetter (c)))
                        {

                            count++;

                            // Return to find another.
                            break;

                        }

                    }

                }

            }

        }

        if (count == 0)
        {

            count++;

        }

        return count;

    }

    public static boolean isVowel (char c)
    {

        return ((c == 'a') ||
                (c == 'e') ||
                (c == 'i') ||
                (c == 'o') ||
                (c == 'u') ||
                (c == 'y'));

    }

    public static List<Integer> indexOf (List<String>        words,
                                         List<String>        find,
                                         boolean             inDialogue,
                                         DialogueConstraints cons)
    {

        List<Integer> ret = new ArrayList ();

        int fc = find.size ();

        int wc = words.size ();

        int firstWord = -1;
        
        for (int i = 0; i < wc; i++)
        {

            String w = words.get (i);

            if (w.length () == 1)
            {

                inDialogue = TextUtilities.stillInDialogue (w,
                                                            inDialogue);

                // Need to skip to the next word if this is the start of dialogue, i.e. "
                if ((inDialogue) &&
                    (TextUtilities.isOpenQ (w.charAt (0))))
                {

                    continue;

                }

            }

            if ((cons.ignoreInDialogue) &&
                (inDialogue))
            {

                continue;

            }

            if ((cons.onlyInDialogue) &&
                (!inDialogue))
            {
                
                continue;

            }

            int wfc = 0;

            if ((i + fc) < (wc + 1))
            {

                boolean match = false;

                for (int j = 0; j < fc; j++)
                {

                    String fw = find.get (j);

                    if (!fw.equals (TextUtilities.ANY_WORD))
                    {

                        String w2 = words.get (i + wfc);

                        if (w2.length () == 1)
                        {

                            inDialogue = TextUtilities.stillInDialogue (w2,
                                                                        inDialogue);

                        }

                        if ((cons.ignoreInDialogue) &&
                            (inDialogue))
                        {

                            wfc++;

                            continue;

                        }

                        if ((cons.onlyInDialogue) &&
                            (!inDialogue))
                        {

                            wfc++;
                            
                            continue;

                        }

                        if (!w2.equals (fw))
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

                    if (cons.where.equals (DialogueConstraints.START))
                    {

                        // Make sure i is 0 or perhaps 1 ignoring a punctuation character.
                        if (i > 0)
                        {

                            for (int m = i - 1; m > -1; m--)
                            {

                                if (TextUtilities.isWord (words.get (m)))
                                {

                                    // Can't match.
                                    return new ArrayList ();

                                }

                            }

                        }

                    } else
                    {

                        if (cons.where.equals (DialogueConstraints.END))
                        {

                            // Check to make sure that the match is at the end, ignoring a trailing punctation char.
                            if (i < (wc - 1))
                            {

                                for (int m = i + find.size (); m < wc; m++)
                                {

                                    if (TextUtilities.isWord (words.get (m)))
                                    {

                                        // Can't match.
                                        return new ArrayList ();

                                    }

                                }

                            }

                        }

                    }

                    ret.add (i + wfc - find.size ());

                }

            } else
            {

                // Couldn't possibly match.
                return ret;

            }

        }

        return ret;

    }

    public static boolean stillInDialogue (List<String> words,
                                           boolean      inDialogue)
    {

        for (String w : words)
        {

            if (w.length () == 1)
            {

                inDialogue = TextUtilities.stillInDialogue (w,
                                                            inDialogue);

            }

        }

        return inDialogue;

    }

    public static boolean stillInDialogue (String  word,
                                           boolean inDialogue)
    {

        char c = word.charAt (0);

        if (c == '"')
        {

            return !inDialogue;

        }

        if (TextUtilities.isOpenQ (c))
        {

            return true;

        }

        if (TextUtilities.isCloseQ (c))
        {

            return false;


        }

        return inDialogue;

    }

}
