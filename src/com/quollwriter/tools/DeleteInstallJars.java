package com.quollwriter.tools;

import java.io.*;

import java.util.*;


public class DeleteInstallJars
{

    private static int tryCount = 0;

    public static void main (String[] argv)
    {

        try
        {

            String userDir = System.getProperty ("user.dir");

            File userDirF = new File (userDir).getCanonicalFile ();

            userDir = userDirF.getPath ();

            if (!userDir.endsWith ("jars"))
            {

                return;

            }

            Thread.sleep (1000);

            List<File> files = new ArrayList ();

            for (int i = 0; i < argv.length; i++)
            {

                File f = new File (userDir + "/" + argv[i]).getCanonicalFile ();

                if ((!f.getPath ().startsWith (userDir)) ||
                    (f.isDirectory ()) ||
                    (!f.getName ().endsWith (".jar")) ||
                    (!f.getParent ().endsWith ("jars")))
                {

                    return;

                }

                files.add (f);

            }

            deleteFiles (files);

        } catch (Exception e)
        {

            // Nothing we can do...

        }

    }

    private static void deleteFiles (List<File> files)
    {

        List<File> toDelete = new ArrayList ();

        for (File f : files)
        {

            if (!f.delete ())
            {

                toDelete.add (f);

            }

        }

        if ((tryCount < 5) &&
            (toDelete.size () > 0))
        {

            tryCount++;

            try
            {

                Thread.sleep (1000);

            } catch (Exception e)
            {
            }

            deleteFiles (toDelete);

        }

    }

}
