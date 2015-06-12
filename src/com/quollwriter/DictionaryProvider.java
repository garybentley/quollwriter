package com.quollwriter;

import java.io.*;

import java.util.*;

import com.quollwriter.ui.events.*;

import com.quollwriter.ui.components.*;

import com.softcorporation.suggester.util.Constants;
import com.softcorporation.suggester.util.SpellCheckConfiguration;
import com.softcorporation.suggester.Suggestion;
import com.softcorporation.suggester.dictionary.BasicDictionary;
import com.softcorporation.suggester.BasicSuggester;

public class DictionaryProvider
{

    private List                           listeners = new ArrayList ();
    //private List<QWSpellDictionaryHashMap> dicts = new ArrayList ();
    private QWSpellDictionaryHashMap       userDict = null;
    private QWSpellDictionaryHashMap       projDict = null;
    private File                           userDictFile = null;
    private SpellChecker                   checker = null;
    private com.swabunga.spell.event.SpellChecker jazzySpellChecker = null;
    private String language = null;

    public DictionaryProvider (String lang,
                               List<String> projWords,
                               File         userDict)
                               throws       Exception
    {
        
        this.language = lang;
        
        this.jazzySpellChecker = new com.swabunga.spell.event.SpellChecker ();
        
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
                
        final DictionaryProvider _this = this;
                
        this.checker = new SpellChecker ()
        {
            
            public synchronized boolean isCorrect (String word)
            {
                
                try
                {
                
                    if (_this.jazzySpellChecker.isCorrect (word))
                    {
                        
                        return true;
                        
                    }
                
                    if (suggester.hasExactWord (word))
                    {
                        
                        return true;
                        
                    }
                    
                    int result = suggester.hasWord (word);
                    if (result == Constants.RESULT_ID_MATCH ||
                        result == Constants.RESULT_ID_MATCH_EXACT)
                    {
                      return true;
                    }                

                }catch (Exception e) {
                    
                    e.printStackTrace ();
                    
                }
                
                return false;
                
            }
            
            public synchronized boolean isIgnored (String word)
            {
                
                return false;
                
            }
            
            public synchronized List<String> getSuggestions (String word)
            {
                
                List<String> ret = new ArrayList ();
                
                List suggestions = null;
                
                List jsuggestions = _this.jazzySpellChecker.getSuggestions (word,
                                                                            1);
                
                if (jsuggestions != null)
                {
                    
                    for (int i = 0; i < jsuggestions.size (); i++)
                    {
                                                
                        ret.add (((com.swabunga.spell.engine.Word) jsuggestions.get (i)).getWord ());
                        
                    }
                    
                }
                
                try
                {
                    
                    suggestions = suggester.getSuggestions (word, 20);
                    
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
                
                if (Character.isUpperCase (word.charAt (0)))
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

        if (projWords != null)
        {

            StringBuilder b = new StringBuilder ();

            for (String i : projWords)
            {

                b.append (i);
                b.append ('\n');

            }

            this.projDict = new QWSpellDictionaryHashMap (new StringReader (b.toString ()));

            this.jazzySpellChecker.addDictionary (this.projDict);
            
            //this.dicts.add (this.projDict);

        }

        this.userDict = new QWSpellDictionaryHashMap (userDict);

        this.userDictFile = userDict;

        this.jazzySpellChecker.setUserDictionary (this.userDict);
        
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

}
