package com.quollwriter.text;

import java.text.*;

import java.util.*;

import com.quollwriter.*;


public class TextUtilities
{

    public static final String ANY_WORD = "*";

    private static Map<String, Integer> wordSyllableCounts = null;
    private static Map<String, String>  contractionEnds = new HashMap ();
    private static Map<Character, Set<Character>> openCloseQs = new HashMap ();
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

        m = TextUtilities.openCloseQs;
        
        // Taken from: http://en.wikipedia.org/wiki/International_variation_in_quotation_marks
        // The assumption here is (perhaps erroneously) that any text will be language consistent
        // and that clashes (i.e. a closing quote will also be an opening quote) doesn't occur.
        // Thus if the text is something like:
        // "And I said, 'Bonjour Monsieur'."
        // Then the writer will use quotation marks that are consistent for their PRIMARY language, in this case
        // English.  Thus you should never see:
        // "And I said, «Bonjour Monsieur»"
        // In theory it could happen but then you have a potential reader confusion problem and thus
        // outside of what QW can deal with anyway.
        
        // Note: this only supports ltr/rtl writing, vertical text flow quotation marks aren't supported
        // since QW doesn't support it.
        
        // The other issue here is that due to some of the language combinations AND the fact that QW
        // doesn't know what language the text is in (it could possibly try and detect it, maybe using cue or similar)
        // then user error in matching pairs of quotations can lead to problems.
        // Such as:
        // "And I said, "Hello Sir".
        // In this case "Hello Sir" would not appear to be speech, however this kind of user error cannot be
        // fixed and the problem finder could, in theory, actually point the user to the error.  The planned "text checker"
        // would be able to detect this kind of problem though due to unbalanced quotation marks.
        
        // And sorry Lojban, you're on your own!
        
        // \u201c
        TextUtilities.addToOpenQCloseQ ('\u201c', '\u201d');
               
        // \u201d
        TextUtilities.addToOpenQCloseQ ('\u201d', '\u201d', '\u201e');

        // \u2018
        TextUtilities.addToOpenQCloseQ ('\u2018', '\u2019');
               
        // \u201e
        TextUtilities.addToOpenQCloseQ ('\u201e', '\u201d');
        
        // \u201a
        TextUtilities.addToOpenQCloseQ ('\u201a', '\u2019', '\u2018');

        // \u201e
        TextUtilities.addToOpenQCloseQ ('\u201e', '\u201c');
               
        // \u00ab
        TextUtilities.addToOpenQCloseQ ('\u00ab', '\u00bb');

        // \u00bb
        TextUtilities.addToOpenQCloseQ ('\u00bb', '\u00ab', '\u00bb');

        // \u0022
        TextUtilities.addToOpenQCloseQ ('\u0022', '\u0022');

        // \u2019
        TextUtilities.addToOpenQCloseQ ('\u2019', '\u2019', '\u201a');

        // \u2039
        TextUtilities.addToOpenQCloseQ ('\u2039', '\u203a');

        // \u300c
        TextUtilities.addToOpenQCloseQ ('\u300c', '\u300d');

        // \u300e
        TextUtilities.addToOpenQCloseQ ('\u300e', '\u300f');

        // \u203a
        TextUtilities.addToOpenQCloseQ ('\u203a', '\u2039');

        // \u2033
        TextUtilities.addToOpenQCloseQ ('\u2033', '\u2036');

        // \u301d
        TextUtilities.addToOpenQCloseQ ('\u301d', '\u301e');

        // \u301f
        TextUtilities.addToOpenQCloseQ ('\u301f', '\u301f');

        // '
        TextUtilities.addToOpenQCloseQ ('\'', '\'');
        
        // "
        TextUtilities.addToOpenQCloseQ ('"', '"');

    }
    
    private static void addToOpenQCloseQ (char    openQ,
                                          char... closeQs)
    {
        
        Set<Character> s = new HashSet ();
        
        for (int i = 0; i < closeQs.length; i++)
        {
            
            s.add (closeQs[i]);
            
        }
        
        TextUtilities.openCloseQs.put (openQ,
                                       s);
        
    }
    
    public static boolean isCloseQForOpenQ (char      c,
                                            Character openQ)
    {
        
        Set<Character> nc = TextUtilities.openCloseQs.get (openQ);
        
        if (nc == null)
        {
            
            return false;
            
        }
        
        return nc.contains (c);
        
    }
    
    public static boolean isContractionEnd (String v)
    {
                
        if (v == null)
        {
            
            return false;            
                
        }
        
        return TextUtilities.contractionEnds.containsKey (v.toLowerCase ());
        
    }
    /*
    public static boolean isCloseQ (char c)
    {
        
        return (c == '"') ||
               (c == '\'') ||
               (c == '\u00bb') ||
               (c == '\u2019') ||
               (c == '\u201b') ||
               (c == '\u201d') ||
               (c == '\u203a') ||
               (c == '\u300d') ||
               (c == '\u300f') ||
               (c == '\u301f') ||
               // These are the "reversed double prime" marks that Word seems to use.
               (c == '\u2036') ||
               (c == '\u301e');
        
    }
    */
    
    public static boolean isOpenQ (char c)
    {

        return TextUtilities.openCloseQs.containsKey (c);
    /*
        return (c == '"') ||
               (c == '\'') ||
               (c == '\u00ab') ||
               (c == '\u2018') ||
               (c == '\u201a') ||
               (c == '\u201b') ||
               (c == '\u201c') ||
               (c == '\u201e') ||
               (c == '\u2039') ||
               (c == '\u300c') ||
               (c == '\u300e') ||
               (c == '\u301d') ||
               (c == '\u301f') ||
               // These are the "double prime" marks that Word seems to use.
               (c == '\u2033') ||
               (c == '\u301d');
      */         
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

            if (c == i.getStartIssuePosition ())
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

    public static int getSentenceCount (String text)
    {
        
        Paragraph p = new Paragraph (text,
                                     0);

        return p.getSentenceCount ();
        
    }    
   
    public static int getWordCount (String l)
    {
        
        return TextUtilities.getAsWords (l).size ();
        
    }

    public static List<Word> getAsWords (String l)
    {

        return new Sentence (l,
                             new DialogueInd ()).getWords ();
    
    }
    /*
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
*/
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
    /*
    public static int getSyllableCount (String text)
    {
        
        int c = 0;
        
        List<String> words = TextUtilities.getAsWords (text);

        words = TextUtilities.stripPunctuation (words);

        for (String w : words)
        {

            int cc = TextUtilities.getSyllableCountForWord (w);

            c += cc;
            
        }

        return c;
        
    }
    */
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
/*
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
*/
/*
    public static List<Integer> indexOf (List<Word>          words,
                                         List<String>        find,
                                         DialogueConstraints cons)
    {

        List<Integer> ret = new ArrayList ();

        int fc = find.size ();

        int wc = words.size ();

        int firstWord = -1;
        
        for (int i = 0; i < wc; i++)
        {

            Word w = words.get (i);
            
            if ((cons.ignoreInDialogue) &&
                (w.isInDialogue ()))
            {

                continue;

            }

            if ((cons.onlyInDialogue) &&
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

                    String fw = find.get (j);

                    if (!fw.equals (TextUtilities.ANY_WORD))
                    {

                        Word w2 = words.get (i + wfc);
                    
                        if ((cons.ignoreInDialogue) &&
                            (w2.isInDialogue ()))
                        {

                        break;
                        }

                        if ((cons.onlyInDialogue) &&
                            (!w2.isInDialogue ()))
                        {

break;
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

                    if (cons.where.equals (DialogueConstraints.START))
                    {

                        // Make sure i is 0 or perhaps 1 ignoring a punctuation character.
                        if (i > 0)
                        {

                            for (int m = i - 1; m > -1; m--)
                            {

                                Word mw = words.get (m);
                            
                                if (!mw.isPunctuation ())
                                {
                                    
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

                                    Word mw = words.get (m);
                                
                                    if (!mw.isPunctuation ())
                                    {
                                        
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
*/
/*
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
*/
/*        
    public static boolean isQuoteChar (char c)
    {
        
        return TextUtilities.isOpenQ (c) || TextUtilities.isCloseQ (c);        
        
    }
  */
/*
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
*/
}
