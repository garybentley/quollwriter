package com.quollwriter.tools;

import java.io.*;

import java.util.*;

import com.gentlyweb.xml.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import org.jdom.*;


public class GetPromptAuthors
{

    public static void main (String[] argv)
    {

        File promptsDir = new File (argv[0]);

        List<String>        authors = new ArrayList<> ();
        Map<String, String> seen = new HashMap<> ();

        try
        {

            File[] files = promptsDir.listFiles ();

            for (int i = 0; i < files.length; i++)
            {

                Element root = null;

                try
                {

                    root = JDOMUtils.getFileAsElement (files[i],
                                                       ".zip");

                } catch (Exception e)
                {

                    System.out.println ("FILE: " + files[i]);

                    continue;

                }

                Prompt p = new Prompt (root);

                if (seen.containsKey (p.getAuthor ().toLowerCase ()))
                {

                    continue;

                }

                seen.put (p.getAuthor ().toLowerCase (),
                          "");

                authors.add (p.getAuthor ());

            }

        } catch (Exception e)
        {

            e.printStackTrace ();

        }

        Collections.sort (authors);

        for (String a : authors)
        {

            System.out.println (a);

        }

    }

}
