package com.quollwriter;

import java.io.*;
import java.nio.file.*;

import java.util.*;

import com.quollwriter.ui.events.*;

import com.quollwriter.ui.components.*;
import com.quollwriter.text.*;

import com.softcorporation.suggester.util.Constants;
import com.softcorporation.suggester.util.SpellCheckConfiguration;
import com.softcorporation.suggester.Suggestion;
import com.softcorporation.suggester.dictionary.BasicDictionary;
import com.softcorporation.suggester.BasicSuggester;

public class UserDictionaryProvider implements DictionaryProvider2
{

    private static List                           listeners = new ArrayList ();

    // TODO Use a path
    private static File                           file = null;
    private static QWSpellDictionaryHashMap       dict = null;
    private static SpellChecker                   checker = null;
    private static com.swabunga.spell.event.SpellChecker spellChecker = null;
    private DictionaryProvider2 parent = null;

    public UserDictionaryProvider (String lang)
                            throws Exception
    {

        this (LanguageDictionaryProvider.getInstance (lang));

    }

    public UserDictionaryProvider ()
                            throws Exception
    {

        final UserDictionaryProvider _this = this;

        if (UserDictionaryProvider.file == null)
        {

            Path userDictFile = DictionaryProvider.getUserDictionaryFilePath ();

            if (Files.notExists (userDictFile))
            {

                Files.createFile (userDictFile);

            }

            UserDictionaryProvider.dict = new QWSpellDictionaryHashMap (userDictFile.toFile ());

            UserDictionaryProvider.file = userDictFile.toFile ();

            UserDictionaryProvider.spellChecker = new com.swabunga.spell.event.SpellChecker ();

            UserDictionaryProvider.spellChecker.setUserDictionary (this.dict);

        }

        this.checker = new SpellChecker ()
        {

            public synchronized boolean isCorrect (Word word)
            {

                String w = word.getText ();

                if (UserDictionaryProvider.spellChecker.isCorrect (w))
                {

                    return true;

                }

                if (_this.parent != null)
                {

                    return _this.parent.getSpellChecker ().isCorrect (word);

                }

                return false;

            }

            public synchronized boolean isIgnored (Word word)
            {

                if (_this.parent != null)
                {

                    return _this.parent.getSpellChecker ().isIgnored (word);

                }

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

                List jsuggestions = UserDictionaryProvider.spellChecker.getSuggestions (wt,
                                                                                        1);

                if (jsuggestions != null)
                {

                    for (int i = 0; i < jsuggestions.size (); i++)
                    {

                        ret.add (((com.swabunga.spell.engine.Word) jsuggestions.get (i)).getWord ());

                    }

                }

                if (_this.parent != null)
                {

                    ret.addAll (_this.parent.getSpellChecker ().getSuggestions (word));

                }

                return ret;

            }

        };

    }

    public UserDictionaryProvider (DictionaryProvider2 parent)
                               throws       Exception
    {

        this ();

        this.parent = parent;

    }

    @Override
    public void addDictionaryChangedListener (DictionaryChangedListener l)
    {

        if (UserDictionaryProvider.listeners.contains (l))
        {

            return;

        }

        UserDictionaryProvider.listeners.add (l);

        if (this.parent != null)
        {

            this.parent.addDictionaryChangedListener (l);

        }

    }

    @Override
    public void removeDictionaryChangedListener (DictionaryChangedListener l)
    {

        UserDictionaryProvider.listeners.remove (l);

        if (this.parent != null)
        {

            this.parent.removeDictionaryChangedListener (l);

        }

    }

    protected void fireDictionaryEvent (DictionaryChangedEvent ev)
    {

        for (int i = 0; i < UserDictionaryProvider.listeners.size (); i++)
        {

            DictionaryChangedListener dcl = (DictionaryChangedListener) UserDictionaryProvider.listeners.get (i);

            dcl.dictionaryChanged (ev);

        }

    }

    @Override
    public SpellChecker getSpellChecker ()
    {

        return this.checker;

    }

    @Override
    public synchronized void removeWord (String word)
    {

        UserDictionaryProvider.dict.removeWord (word);

        try
        {

            UserDictionaryProvider.dict.saveDictionaryToFile (UserDictionaryProvider.file);

        } catch (Exception e)
        {

            Environment.logError ("Unable to save user dictionary file",
                                  e);

        }

        this.fireDictionaryEvent (new DictionaryChangedEvent (this,
                                                              DictionaryChangedEvent.WORD_REMOVED,
                                                              word));

    }

    @Override
    public synchronized void addWord (String word)
    {

        if (!UserDictionaryProvider.dict.isCorrect (word))
        {

            UserDictionaryProvider.dict.addWord (word);

            try
            {

                UserDictionaryProvider.dict.saveDictionaryToFile (UserDictionaryProvider.file);

            } catch (Exception e)
            {

                Environment.logError ("Unable to save user dictionary file",
                                      e);

            }

            this.fireDictionaryEvent (new DictionaryChangedEvent (this,
                                                                  DictionaryChangedEvent.WORD_ADDED,
                                                                  word));

        }

    }

}
