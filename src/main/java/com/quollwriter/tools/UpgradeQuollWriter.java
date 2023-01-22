package com.quollwriter.tools;

import java.io.*;

import java.util.*;


public class UpgradeQuollWriter
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

                userDir += "/jars";

            }

            if (!new File (userDir).exists ())
            {

                return;

            }

            userDirF = new File (userDir).getCanonicalFile ();

            Thread.sleep (1000);

            File newDir = new File (userDirF + "/.new");

            if (!newDir.exists ())
            {

                return;

            }

            List<File> files = new ArrayList<> ();

            File toDelete = new File (newDir + "/to-delete.txt");

            if ((toDelete.exists ())
                &&
                (toDelete.isFile ())
               )
            {

                // Get the file, each line indicates a jar file name to delete.
                BufferedReader bin = new BufferedReader (new FileReader (toDelete));

                String line = null;

                while ((line = bin.readLine ()) != null)
                {

                    if (line.endsWith (".jar"))
                    {

                        File toDel = new File (userDirF + "/" + line).getCanonicalFile ();

                        if ((!toDel.exists ())
                            ||
                            (!toDel.getPath ().startsWith (userDirF.getPath ()))
                           )
                        {

                            continue;

                        }

                        files.add (toDel);

                    }

                }

                bin.close ();

                files.add (toDelete);

                deleteFiles (files);

            }

            tryCount = 0;

            File jarsDir = new File (userDir);

            files = new ArrayList<> ();

            // Move any jar files from .new to the main dir.
            if (newDir.exists ())
            {

                File[] newFiles = newDir.listFiles ();

                if (newFiles != null)
                {

                    for (int i = 0; i < newFiles.length; i++)
                    {

                        File f = newFiles[i];

                        if (!f.getName ().endsWith (".jar"))
                        {

                            continue;

                        }

                        files.add (f);

                    }

                }

                moveFiles (files,
                           jarsDir);

                newDir.deleteOnExit ();

            }

        } catch (Exception e)
        {

            // Nothing we can do...

        }

    }

    private static void moveFiles (List<File> files,
                                   File       jarsDir) throws Exception
    {

        List<File> toMove = new ArrayList<> ();

        for (File f : files)
        {

            File nFile = new File (jarsDir + "/" + f.getName ());

            if (nFile.exists ())
            {

                // Try deleting it first.
                nFile.delete ();

            }

            if (!f.renameTo (nFile))
            {

                toMove.add (f);

            }

        }

        if ((toMove.size () > 0)
            &&
            (tryCount < 5)
           )
        {

            tryCount++;

            try
            {

                Thread.sleep (1000);

            } catch (Exception e) {


            }

            moveFiles (toMove,
                       jarsDir);

        }

    }

    private static void deleteFiles (List<File> files) throws Exception
    {

        List<File> toDelete = new ArrayList<> ();

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
