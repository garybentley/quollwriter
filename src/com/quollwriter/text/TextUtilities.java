package com.quollwriter.text;

import java.text.*;

import java.util.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.ui.components.Markup;

public class TextUtilities
{

    public static final String ANY_WORD = "*";

    public static String DEFAULT_OPEN_Q = "\"";
    public static String DEFAULT_CLOSE_Q = "\"";
    public static String DEFAULT_APOS = "'";
    
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
    
    public static boolean isOpenQ (Word w)
    {
        
        if (w == null)
        {
            
            return false;
            
        }
        
        if (!w.isPunctuation ())
        {
            
            return false;
            
        }
        
        String t = w.getText ();
        
        if (t.length () != 1)
        {
            
            return false;
            
        }
        
        return TextUtilities.isOpenQ (t.charAt (0));
        
    }
    
    public static boolean isOpenQ (char c)
    {

        return TextUtilities.openCloseQs.containsKey (c);

    }

    public static boolean isCloseQ (Word w)
    {
        
        if (w == null)
        {
            
            return false;
            
        }
        
        if (!w.isPunctuation ())
        {
            
            return false;
            
        }
        
        String t = w.getText ();
        
        if (t.length () != 1)
        {
            
            return false;
            
        }
        
        return TextUtilities.isCloseQ (t.charAt (0));
        
    }
    
    public static boolean isCloseQ (char c)
    {

        return TextUtilities.openCloseQs.containsKey (c);

    }

    /**
     * From the markup
     */
    public static List<Markup.MarkupItem> getParagraphMarkup (Paragraph para,
                                                              Markup    mu)
    {
        
        // Get the markup from the start of the paragraph (all text) to the end of the text.
        List<Markup.MarkupItem> items = new ArrayList ();
        
        //int end = -1;
        
        int textStart = para.getAllTextStartOffset ();
        int textEnd = para.getAllTextEndOffset ();
        
        for (Markup.MarkupItem it : mu.items)
        {
            
            if (it.start >= textEnd)
            {

                break;
                
            }

            if (it.end >= textStart)
            {
                
                // This falls in this paragraph.
                int start = it.start;
                int end = it.end;
                
                if (start < textStart)
                {
                    
                    // It starts in a previous paragraph.
                    start = textStart;
                    
                    
                }
                
                if (end > textEnd)
                {
                    
                    // It ends in another paragraph.
                    end = textEnd;
                    
                }
                
                items.add (mu.createItem (start - textStart,
                                          end - textStart,
                                          it.bold,
                                          it.italic,
                                          it.underline));
                
            }
                        
        }
        
        return items;
        
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
        
        return new TextIterator (text).getSentenceCount ();
        
    }    
   
    public static int getWordCount (String l)
    {
        
        return new TextIterator (l).getWordCount ();
                
    }
    
    public static String capitalize (String l)
    {
        
        char[] chars = l.toCharArray ();
        
        boolean lastWhiteSpace = false;
        
        for (int i = 0; i < chars.length; i++)
        {
                        
            if (Character.isWhitespace (chars[i]))
            {
                
                lastWhiteSpace = true;
                
                continue;
                
            }
        
            if (lastWhiteSpace)
            {
                
                // This char should be upper cased.
                chars[i] = Character.toUpperCase (chars[i]);
                
            }
            
            lastWhiteSpace = false;
            
        }
        
        return new String (chars);
        
    }

    public static List<Word> getAsWords (String l)
    {

        return new TextIterator (l).getWords ();
    
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

    /**
     * Taken from: http://blog.mark-mclaren.info/2007/02/invalid-xml-characters-when-valid-utf8_5873.html
     * This method ensures that the output String has only
     * valid XML unicode characters as specified by the
     * XML 1.0 standard. For reference, please see
     * <a href="http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char">the
     * standard</a>. This method will return an empty
     * String if the input is null or empty.
     *
     * @param in The String whose non-valid characters we want to remove.
     * @return The in String, stripped of non-valid characters.
     */
    public static String stripNonValidXMLCharacters (String in)
    {
    
        StringBuilder out = new StringBuilder (); // Used to hold the output.
        char current; // Used to reference the current character.
  
        if (in == null || ("".equals(in)))
        {
            return ""; // vacancy test.
        
        }
        
        for (int i = 0; i < in.length(); i++) {
            current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
            if ((current == 0x9) ||
                (current == 0xA) ||
                (current == 0xD) ||
                ((current >= 0x20) && (current <= 0xD7FF)) ||
                ((current >= 0xE000) && (current <= 0xFFFD)) ||
                ((current >= 0x10000) && (current <= 0x10FFFF)))
                out.append(current);
        }
        
        return out.toString ();
    
    }       

    public static String sanitizeText (String t)
    {
        
        if (t == null)
        {
            
            return t;
            
        }

        t = StringUtils.replaceString (t,
                                       String.valueOf ('\r'),
                                       "");
        
        t = StringUtils.replaceString (t,
                                       String.valueOf ('\u201c'),
                                       DEFAULT_OPEN_Q);
        t = StringUtils.replaceString (t,
                                       String.valueOf ('\u201d'),
                                       DEFAULT_CLOSE_Q);
        t = StringUtils.replaceString (t,
                                       String.valueOf ('\u2019'),
                                       DEFAULT_APOS);
        
        return t.replaceAll ("[\\p{Cntrl}&&[^\r\n\t]]", "");
        
    }
    
}
