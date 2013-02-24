package com.quollwriter.tools;

import java.io.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.synonyms.*;
import com.quollwriter.synonyms.providers.SynonymIndex;


public class CreateThesaurusFiles
{

    public static void main (String[] argv)
    {

        File speechFile = new File (argv[0]);
        File thesaurusFile = new File (argv[1]);
        File outputThesaurusFile = new File (argv[2]);
        File lookupFile = new File (argv[3]);
        Map  allWords = new TreeMap ();

        try
        {

            // Read all the words in from the speech file.
            BufferedReader r = new BufferedReader (new FileReader (speechFile));

            String l = null;

            while ((l = r.readLine ()) != null)
            {

                StringTokenizer t = new StringTokenizer (l,
                                                         "×");

                if (t.countTokens () == 2)
                {

                    String word = t.nextToken ().trim ().toLowerCase ();

                    String parts = t.nextToken ().trim ();

                    allWords.put (word,
                                  parts);

                }

            }

            r.close ();

            Map words = new TreeMap ();

            // Read in the thesaurus.
            r = new BufferedReader (new FileReader (thesaurusFile));

            while ((l = r.readLine ()) != null)
            {

                StringTokenizer t = new StringTokenizer (l,
                                                         ",");

                String word = t.nextToken ().trim ().toLowerCase ();

                Map synonyms = new TreeMap ();

                while (t.hasMoreTokens ())
                {

                    String syn = t.nextToken ().trim ().toLowerCase ();

                    String parts = (String) allWords.get (syn);

                    if (parts == null)
                    {

                        parts = String.valueOf (Synonyms.OTHER);

                    }

                    synonyms.put (syn,
                                  parts);

                    // See if we already have the synonym as a word.
                    // Word: hello
                    // Syn: hi
                    Map m = (Map) words.get (syn);

                    if (m == null)
                    {

                        // We don't so add it with the word as the synonym.
                        m = new TreeMap ();

                        words.put (syn,
                                   m);

                        parts = (String) allWords.get (word);

                        if (parts == null)
                        {

                            parts = "O";

                        }

                        m.put (word,
                               parts);

                        // System.out.println ("ADDED WORD: " + word + ", AS SYN FOR: " + syn);

                    }

                }

                words.put (word,
                           synonyms);

            }

            System.out.println ("WORDS: " + words.size ());

            // Create the new thesaurus file with the parts.
            PrintWriter out = new PrintWriter (outputThesaurusFile,
                                               "utf-8");

            PrintWriter lout = new PrintWriter (lookupFile,
                                                "utf-8");

            Iterator iter = words.keySet ().iterator ();

            while (iter.hasNext ())
            {

                StringBuilder b = new StringBuilder ();

                String word = (String) iter.next ();

                String wparts = (String) allWords.get (word);

                b.append (word);
                b.append ("@");
                b.append (((wparts == null) ? Synonyms.OTHER : wparts));
                b.append (",");

                Map syns = (Map) words.get (word);

                Iterator siter = syns.keySet ().iterator ();

                while (siter.hasNext ())
                {

                    String w = (String) siter.next ();

                    String parts = (String) syns.get (w);

                    b.append (w);
                    b.append ("@");
                    b.append (parts);

                    if (siter.hasNext ())
                    {

                        b.append (",");

                    }

                }

                // See how big the thesaurus file is.
                // Output that against the word in the lookup file.
                lout.print (word);
                lout.print ("@");
                lout.print (String.valueOf ((new File (outputThesaurusFile.getPath ()).length ())));
                lout.print (String.valueOf ('\n'));

                out.print (b);

                // Want to ensure there is only one char.
                out.print (String.valueOf ('\n'));

                out.flush ();

            }

            lout.flush ();
            lout.close ();

            out.flush ();
            out.close ();

            // Read the index file back in.
            r = new BufferedReader (new FileReader (lookupFile));

            long               s = System.currentTimeMillis ();
            List<SynonymIndex> synInd = new ArrayList<SynonymIndex> ();

            while ((l = r.readLine ()) != null)
            {

                StringTokenizer t = new StringTokenizer (l,
                                                         "@");

                SynonymIndex si = new SynonymIndex ();
                si.word = t.nextToken ();

                try
                {

                    si.index = Long.parseLong (t.nextToken ());

                } catch (Exception e)
                {

                    System.out.println ("BAD LOOKUP INDEX FOR WORD: " + si.word);

                }

                synInd.add (si);

            }

            r.close ();

            System.out.println ("TOOK: " + (System.currentTimeMillis () - s) + " TO READ INDEX IN");

            words = null;
            allWords = null;

            // Do some random lookups to check.
            Random rand = new Random ();

            RandomAccessFile raf = new RandomAccessFile (outputThesaurusFile,
                                                         "r");

            int c = 0;

            s = System.currentTimeMillis ();

            System.gc ();

            while (c < 10)
            {

                int n = rand.nextInt (synInd.size () - 1);

                SynonymIndex si = synInd.get (n);

                int ind = Collections.binarySearch (synInd,
                                                    si);

                // Do a binary lookup.
                // System.out.println ("WORD: " + si.word + ", SYNIND: " + Collections.binarySearch (synInd, si) + ", IND: " + si.index);

                // Do a random lookup.
                raf.seek (si.index);

                String line = raf.readLine ();

                if (!line.startsWith (si.word + "@"))
                {

                    System.out.println ("MISMATCH: " + si.word + ":::" + line);

                }

                c++;

            }

            System.out.println ("TOOK: " + (System.currentTimeMillis () - s));

            raf.close ();

        } catch (Exception e)
        {

            e.printStackTrace ();

        }

    }

}
