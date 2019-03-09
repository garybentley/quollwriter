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

import com.quollwriter.data.*;

public class ProjectDictionaryProvider implements DictionaryProvider2
{

    private List                           listeners = new ArrayList ();
    private QWSpellDictionaryHashMap       projDict = null;
    private SpellChecker                   checker = null;
    private com.swabunga.spell.event.SpellChecker spellChecker = null;

    private DictionaryProvider2 parent = null;

    public ProjectDictionaryProvider (List<String> projWords,
                                      DictionaryProvider2 prov)
                               throws Exception
    {

        this.parent = prov;

        final ProjectDictionaryProvider _this = this;

        this.checker = new SpellChecker ()
        {

            public synchronized boolean isCorrect (Word word)
            {

                String w = word.getText ();

                if (_this.spellChecker.isCorrect (w))
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

                List jsuggestions = _this.spellChecker.getSuggestions (wt,
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

        this.spellChecker = new com.swabunga.spell.event.SpellChecker ();

        String init = "";

        if (projWords != null)
        {

            StringBuilder b = new StringBuilder ();

            for (String i : projWords)
            {

                b.append (i);
                b.append ('\n');

            }

            init = b.toString ();

        }

        this.projDict = new QWSpellDictionaryHashMap (new StringReader (init));

        this.spellChecker.addDictionary (this.projDict);

    }

    @Override
    public void addDictionaryChangedListener (DictionaryChangedListener l)
    {

        if (this.listeners.contains (l))
        {

            return;

        }

        this.listeners.add (l);

        if (this.parent != null)
        {

            this.parent.addDictionaryChangedListener (l);

        }

    }

    @Override
    public void removeDictionaryChangedListener (DictionaryChangedListener l)
    {

        this.listeners.remove (l);

        if (this.parent != null)
        {

            this.parent.removeDictionaryChangedListener (l);

        }

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

    public void addNamedObject (NamedObject o)
    {

        for (String nn : o.getAllNames ())
        {

            if (nn == null)
            {

                continue;

            }

            if (nn.trim ().length () == 0)
            {

                continue;

            }

            // Add the name.
            try
            {

                this.projDict.addWord (nn);

            } catch (Exception e) {}

        }

    }

    public void removeObjectNames (Set<String> oldNames)
    {

        for (String nn : oldNames)
        {

            if (nn == null)
            {

                continue;

            }

            if (nn.trim ().length () == 0)
            {

                continue;

            }

            try
            {

                this.projDict.removeWord (nn);

            } catch (Exception e) {}

        }

    }

    @Override
    public void removeWord (String word)
    {

        if (this.parent != null)
        {

            this.parent.removeWord (word);

        }

        this.fireDictionaryEvent (new DictionaryChangedEvent (this,
                                                              DictionaryChangedEvent.WORD_REMOVED,
                                                              word));

    }

    @Override
    public void addWord (String word)
    {

        if (this.parent != null)
        {

            this.parent.addWord (word);

        }

        this.fireDictionaryEvent (new DictionaryChangedEvent (this,
                                                              DictionaryChangedEvent.WORD_ADDED,
                                                              word));

    }

}
