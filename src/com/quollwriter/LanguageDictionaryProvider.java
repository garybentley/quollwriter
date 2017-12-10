package com.quollwriter;

import java.io.*;

import java.util.*;

import com.quollwriter.ui.events.*;

import com.quollwriter.ui.components.*;
import com.quollwriter.text.*;

import com.softcorporation.suggester.util.Constants;
import com.softcorporation.suggester.util.SpellCheckConfiguration;
import com.softcorporation.suggester.Suggestion;
import com.softcorporation.suggester.dictionary.BasicDictionary;
import com.softcorporation.suggester.BasicSuggester;

public class LanguageDictionaryProvider implements DictionaryProvider2
{

    private static Map<String, LanguageDictionaryProvider> langProvs = new HashMap<> ();

    private List                           listeners = new ArrayList ();
    private SpellChecker                   checker = null;
    private String language = null;

    public static LanguageDictionaryProvider getInstance (String lang)
                                                   throws Exception
    {

        LanguageDictionaryProvider p = LanguageDictionaryProvider.langProvs.get (lang);

        if (p != null)
        {

            return p;

        }

        p = new LanguageDictionaryProvider (lang);

        LanguageDictionaryProvider.langProvs.put (lang,
                                                  p);

        return p;

    }

    private LanguageDictionaryProvider (String       lang)
                                throws Exception
    {

        this.language = lang;

        File dictFile = Environment.getDictionaryFile (lang);

        if (!dictFile.exists ())
        {

            throw new GeneralException ("Unable to find dictionary file: " +
                                        dictFile);

        }

        BasicDictionary dict = new BasicDictionary ("file://" + dictFile.getPath ());

        SpellCheckConfiguration config = new SpellCheckConfiguration ("/com/softcorporation/suggester/spellCheck.config");

        final BasicSuggester suggester = new BasicSuggester (config);
        suggester.attach (dict);

        final LanguageDictionaryProvider _this = this;

        this.checker = new SpellChecker ()
        {

            public synchronized boolean isCorrect (Word word)
            {

                if (word.isPunctuation ())
                {

                    return true;

                }

                String w = word.getText ();

                // See if the word is a number.
                try
                {

                    Double.parseDouble (w);

                    return true;

                } catch (Exception e) {

                    // Not a number.

                }

                if (this.isIgnored (word))
                {

                    return true;

                }

                try
                {

                    if (suggester.hasExactWord (w))
                    {

                        return true;

                    }

                    int result = suggester.hasWord (w);
                    if (result == Constants.RESULT_ID_MATCH ||
                        result == Constants.RESULT_ID_MATCH_EXACT)
                    {
                      return true;
                    }

                }catch (Exception e) {

                    Environment.logError ("Unable to check word: " +
                                          word,
                                          e);

                }

                return false;

            }

            public synchronized boolean isIgnored (Word word)
            {

                return false;

            }

            public synchronized List<String> getSuggestions (Word word)
            {

                List<String> ret = new ArrayList ();

                if (word == null)
                {

                    return null;

                }

                if (this.isCorrect (word))
                {

                    return null;

                }

                String wt = word.getText ();

                List suggestions = null;

                try
                {

                    suggestions = suggester.getSuggestions (wt, 20);

                } catch (Exception e) {

                    e.printStackTrace ();

                }

                if (suggestions != null)
                {

                    for (int i = 0; i < suggestions.size (); i++)
                    {

                        Suggestion s = (Suggestion) suggestions.get (i);

                        ret.add (s.word);

                    }

                }

                if (Character.isUpperCase (wt.charAt (0)))
                {

                    for (int i = 0; i < ret.size (); i++)
                    {

                        String w = ret.get (i);

                        ret.set (i,
                                 Character.toUpperCase (w.charAt (0)) + w.substring (1));

                    }

                }

                return ret;

            }

        };

    }

    public String getLanguage ()
    {

        return this.language;

    }

    public static boolean isLanguageInstalled (String lang)
    {

        File f = Environment.getDictionaryFile (lang);

        if ((f != null)
            &&
            (f.exists ())
           )
        {

            return true;

        }

        return false;

    }

    /**
     * Checks to see if the directory is an indexed dictionary directory which means:
     *   - It contains a db directory
     *   - It contains a words directory
     *   - It contains a contents file
     *
     * @param dir The directory to check.
     * @return If the checks pass.
     */
     /*
    private boolean isIndexedDictionaryDirectory (File dir)
    {

        if (!dir.exists ())
        {

            return false;

        }

        File dbDir = new File (dir, "db");

        if ((!dbDir.exists ())
            ||
            (dbDir.isFile ())
           )
        {

            return false;

        }

        File wordsDir = new File (dir, "words");

        if ((!wordsDir.exists ())
            ||
            (wordsDir.isFile ())
           )
        {

            return false;

        }

        return true;

    }
*/
    @Override
    public void addDictionaryChangedListener (DictionaryChangedListener l)
    {

        if (this.listeners.contains (l))
        {

            return;

        }

        this.listeners.add (l);

    }

    @Override
    public void removeDictionaryChangedListener (DictionaryChangedListener l)
    {

        this.listeners.remove (l);

    }

    protected void fireDictionaryEvent (DictionaryChangedEvent ev)
    {

        for (int i = 0; i < this.listeners.size (); i++)
        {

            DictionaryChangedListener dcl = (DictionaryChangedListener) this.listeners.get (i);

            dcl.dictionaryChanged (ev);

        }

    }

    @Override
    public SpellChecker getSpellChecker ()
    {

        return this.checker;

    }
/*
    public static void addUserWord (String word)
    {

        if (!DictionaryProvider.userDict.isCorrect (word))
        {

            DictionaryProvider.userDict.addWord (word);

        }

    }
*/
/*
    public static void removeUserWord (String word)
    {

        if (DictionaryProvider.userDict != null)
        {

            DictionaryProvider.userDict.removeWord (word);

            try
            {

                DictionaryProvider.userDict.saveDictionaryToFile (DictionaryProvider.userDictFile);

            } catch (Exception e)
            {

                Environment.logError ("Unable to save user dictionary file",
                                      e);

            }


        }

    }
*/
    @Override
    public void removeWord (String word)
    {

        throw new IllegalArgumentException ("Not supported for a language dictionary provider.");

    }

    @Override
    public void addWord (String word)
    {

        throw new IllegalArgumentException ("Not supported for a language dictionary provider.");

    }

}
