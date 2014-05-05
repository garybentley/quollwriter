package com.quollwriter;

import java.io.*;

import java.util.*;

import com.quollwriter.ui.events.*;

import com.swabunga.spell.engine.*;
import com.swabunga.spell.event.*;

public class DictionaryProvider
{

    private List                           listeners = new ArrayList ();
    private List<QWSpellDictionaryHashMap> dicts = new ArrayList ();
    private QWSpellDictionaryHashMap       userDict = null;
    private QWSpellDictionaryHashMap       projDict = null;
    private File                           userDictFile = null;
    private SpellChecker                   checker = null;

    public DictionaryProvider(List<InputStream> files,
                              List<String>      projWords,
                              File              userDict)
                       throws IOException
    {

        this.checker = new SpellChecker ();    
    
        if (files != null)
        {
    
            for (InputStream in : files)
            {
                
                if (in == null)
                {
                    
                    continue;
                    
                }
            
                InputStreamReader r = new InputStreamReader (in);
    
                this.checker.addDictionary (new QWSpellDictionaryHashMap (r));            
                    
            }

        }
            
        if (projWords != null)
        {

            StringBuilder b = new StringBuilder ();

            for (String i : projWords)
            {

                b.append (i);
                b.append ('\n');

            }

            this.projDict = new QWSpellDictionaryHashMap (new StringReader (b.toString ()));

            this.checker.addDictionary (this.projDict);
            
            //this.dicts.add (this.projDict);

        }

        this.userDict = new QWSpellDictionaryHashMap (userDict);

        this.userDictFile = userDict;

        this.checker.setUserDictionary (this.userDict);
        
    }

    public void addDictionaryChangedListener (DictionaryChangedListener l)
    {

        if (this.listeners.contains (l))
        {

            return;

        }

        this.listeners.add (l);

    }

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
    
    public SpellChecker getSpellChecker ()
    {
        
        return this.checker;
        
    }

    public void removeWord (String word,
                            String type)
    {

        if (type.equals ("project"))
        {

            if (this.projDict != null)
            {

                this.projDict.removeWord (word);

                this.fireDictionaryEvent (new DictionaryChangedEvent (this,
                                                                      DictionaryChangedEvent.WORD_REMOVED,
                                                                      word));

            }

        }

        if (type.equals ("user"))
        {

            if (this.userDict != null)
            {

                this.userDict.removeWord (word);

                try
                {

                    this.userDict.saveDictionaryToFile (this.userDictFile);

                } catch (Exception e)
                {

                    Environment.logError ("Unable to save user dictionary file",
                                          e);

                }

                this.fireDictionaryEvent (new DictionaryChangedEvent (this,
                                                                      DictionaryChangedEvent.WORD_REMOVED,
                                                                      word));

            }

        }

    }

    public void addWord (String word,
                         String type)
    {

        if (type.equals ("project"))
        {

            if (this.projDict == null)
            {

                try
                {

                    this.projDict = new QWSpellDictionaryHashMap (new StringReader (""));

                } catch (Exception e)
                {

                    Environment.logError ("Unable to create project dictionary for word: " +
                                          word,
                                          e);

                    return;

                }

            }

            this.projDict.addWord (word);

            this.fireDictionaryEvent (new DictionaryChangedEvent (this,
                                                                  DictionaryChangedEvent.WORD_ADDED,
                                                                  word));

        }

        if (type.equals ("user"))
        {

            if (!this.userDict.isCorrect (word))
            {

                this.userDict.addWord (word);

                this.fireDictionaryEvent (new DictionaryChangedEvent (this,
                                                                      DictionaryChangedEvent.WORD_ADDED,
                                                                      word));

            }

        }

    }
/*
    public QWSpellDictionaryHashMap getUserDictionary ()
    {

        return this.userDict;

    }

    public List<QWSpellDictionaryHashMap> getDictionaries ()
    {

        return this.dicts;

    }
*/
}
