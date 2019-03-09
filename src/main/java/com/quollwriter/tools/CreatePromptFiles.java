package com.quollwriter.tools;

import java.io.*;

import java.util.*;

import com.gentlyweb.xml.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import org.jdom.*;


public class CreatePromptFiles
{

    public static void main (String[] argv)
    {

        File promptFile = new File (argv[0]);
        File promptIdsFile = new File (argv[1]);
        File outputDir = new File (argv[2]);

        try
        {

            Map ids = new TreeMap ();

            BufferedReader r = null;
            String         l = null;

            // Read in the existing prompt ids.
            if (promptIdsFile.exists ())
            {

                r = new BufferedReader (new FileReader (promptIdsFile));

                l = null;

                while ((l = r.readLine ()) != null)
                {

                    ids.put (l.trim (),
                             "");

                }

                r.close ();

            }

            // Get all the prompts.
            r = new BufferedReader (new FileReader (promptFile));

            int c = 0;

            while ((l = r.readLine ()) != null)
            {

                if (l.trim ().equals (""))
                {

                    continue;

                }

                // Get the last "("
                int ind = l.lastIndexOf ("(");

                if (ind == -1)
                {

                    System.out.println ("LINE HAS NO (: " + l);

                    continue;

                }

                String text = l.substring (0,
                                           ind).trim ();

                // Get to the end of the line minus one.
                String parts = l.substring (ind + 1,
                                            l.length () - 1);


                // Get the website.
                String url = parts.substring (parts.indexOf ("http://"));

                parts = parts.substring (0,
                                         parts.length () - url.length () - 3);

                // Split on -.
                String[] lparts = parts.split (" - ");

                if (lparts.length != 2)
                {

                    System.out.println ("LINE HAS WRONG PARTS: " + lparts.length + " :: " + parts);

                    continue;

                }

                String storyName = lparts[0];
                String authorName = lparts[1];

                String id = CreatePromptFiles.getId ();

                while (ids.containsKey (id))
                {

                    id = CreatePromptFiles.getId ();

                }

                ids.put (id,
                         id);

                Prompt p = new Prompt (id,
                                       authorName,
                                       storyName,
                                       url,
                                       text);

                File pFile = new File (outputDir.getPath () + "/" + id + ".txt");

                Element root = p.getAsElement ();

                JDOMUtils.writeElementToFile (root,
                                              pFile,
                                              true);

                System.out.println ("STORY: " + storyName + ", AUTHOR: " + authorName + ", URL: " + url + ", ID: " + id);
                System.out.println ("WRITING TO: " + pFile);
                c++;

            }

            r.close ();

            // Write out the prompt ids.
            PrintWriter pw = new PrintWriter (promptIdsFile);

            Iterator iter = ids.keySet ().iterator ();

            while (iter.hasNext ())
            {

                pw.println ((String) iter.next ());

            }

            pw.flush ();
            pw.close ();

            /*
            PrintWriter lout = new PrintWriter (lookupFile,
                                                "utf-8");

            lout.flush ();
            lout.close ();
             */

            System.out.println ("COUNT: " + c);

        } catch (Exception e)
        {

            e.printStackTrace ();

        }

    }

    public static String getId ()
    {

        String id = UUID.randomUUID ().toString ();

        id = id.substring (id.length () - 12);

        return id;

    }
}
