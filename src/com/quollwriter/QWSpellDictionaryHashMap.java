package com.quollwriter;

import java.io.*;

import java.util.*;

import com.swabunga.spell.engine.SpellDictionaryHashMap;


public class QWSpellDictionaryHashMap extends SpellDictionaryHashMap
{

    public QWSpellDictionaryHashMap(Reader wordList)
                             throws IOException
    {

        super (wordList);

    }

    public QWSpellDictionaryHashMap(File wordList)
                             throws FileNotFoundException,
                                    IOException
    {

        super (wordList);

    }

    public void removeWord (String word)
    {

        String code = this.getCode (word);
        Vector list = (Vector) mainDictionary.get (code);

        if (list == null)
        {
            
            return;
            
        }

        list.removeElement (word);

        if (list.size () == 0)
        {

            mainDictionary.remove (code);

        }

    }

    public void saveDictionaryToFile (File f)
                               throws Exception
    {

        if (f == null)
        {

            throw new IllegalStateException ("No file to save dictionary to");

        }

        FileWriter w = new FileWriter (f.toString (),
                                       false);

        Collection vs = mainDictionary.values ();

        Iterator iter = vs.iterator ();

        while (iter.hasNext ())
        {

            Vector v = (Vector) iter.next ();

            for (int i = 0; i < v.size (); i++)
            {

                w.write (v.get (i).toString ());
                w.write ("\n");

            }

        }

        w.close ();

    }


}
