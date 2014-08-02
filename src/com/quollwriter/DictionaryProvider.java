package com.quollwriter;

import java.io.*;

import java.util.*;

import com.quollwriter.ui.events.*;

import com.swabunga.spell.engine.*;
import com.swabunga.spell.event.*;

public class DictionaryProvider
{

    private List                           listeners = new ArrayList ();
    //private List<QWSpellDictionaryHashMap> dicts = new ArrayList ();
    private QWSpellDictionaryHashMap       userDict = null;
    private QWSpellDictionaryHashMap       projDict = null;
    private File                           userDictFile = null;
    private SpellChecker                   checker = null;

    public DictionaryProvider (String lang,
                               List<String> projWords,
                               File         userDict)
                               throws       IOException
    {

        this.checker = new SpellChecker ();    
                        
        /*
         *OLD STYLE WHERE dictionary is in jar file
        java.util.List<InputStream> dictFiles = new ArrayList<InputStream> ();

        if (lang == null)
        {
            
            return null;
            
        }
        
        String dFiles = this.proj.getProperty (lang + "DictionaryFiles");

        if (dFiles != null)
        {

            StringTokenizer t = new StringTokenizer (dFiles,
                                                     ",");

            while (t.hasMoreTokens ())
            {

                String tok = t.nextToken ().trim ();

                InputStream in = Environment.class.getResourceAsStream (Constants.DICTIONARIES_DIR + tok);
                
                if (in != null)
                {
                    
                    dictFiles.add (in);
                    
                }

            }

        }
*/        
                        
        File dir = Environment.getDictionaryDirectory (lang);
        
        if (dir.exists ())
        {
            
            if (this.isIndexedDictionaryDirectory (dir))
            {
                
                this.checker.addDictionary (new SpellDictionaryDisk (dir,
                                                                     null,
                                                                     false));
                
                
            } else {
            
                if (Environment.isEnglish (lang))
                {

                    this.addDictionaryFiles (Constants.ENGLISH);
                                    
                }
                    
                this.addDictionaryFiles (lang);
                            
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
    
    private void addDictionaryFiles (String lang)
                                     throws IOException
    {
        
        File dir = Environment.getDictionaryDirectory (lang);        
        
        if (!dir.exists ())
        {
            
            return;
            
        }
        
        File[] files = dir.listFiles ();
        
        for (int i = 0; i < files.length; i++)
        {
            
            if (files[i].isFile ())
            {

                InputStreamReader r = new InputStreamReader (new FileInputStream (files[i]));
                
                this.checker.addDictionary (new QWSpellDictionaryHashMap (r));            
                
            }
            
        }
        
    }
        
    public static boolean isLanguageInstalled (String lang)
    {
        
        File d = Environment.getDictionaryDirectory (lang);
        
        if ((d != null)
            &&
            (d.exists ())
           )
        {
            
            return true;
                
        }

        return false;

    }
    
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
    
                //this.checker.addDictionary (new SpellDictionaryDichoDisk ())
    
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
