package com.quollwriter;

import java.io.*;

import java.text.*;

import java.util.*;
import java.util.zip.*;

import javax.swing.*;

import com.gentlyweb.utils.*;

import com.quollwriter.data.*;

public class Utils
{

    public static void extractZipFile (File   f,
                                       File   toDir)
                                       throws GeneralException
    {
        
        if ((toDir == null)
            ||
            (f == null)
           )
        {
            
            // Naughty but shouldn't happen.
            throw new IllegalArgumentException ("Invalid args");
            
        }
        
        if (toDir.isFile ())
        {
            
            throw new IllegalArgumentException ("To directory is actually a file: " +
                                                toDir);
            
        }
        
        if (f.isDirectory ())
        {
            
            throw new IllegalArgumentException ("File to extract is a directory: " +
                                                f);
            
        }

        if (!f.exists ())
        {
            
            throw new IllegalArgumentException ("File doesn't exist: " +
                                                f);
            
        }
        
        if (!toDir.exists ())
        {
            
            toDir.mkdirs ();
            
        }

        ZipFile zf = null;
        
        try
        {
            
            zf = new ZipFile (f);
                
        } catch (Exception e) {
            
            throw new GeneralException ("Unable to open file as a zip file: " +
                                        f,
                                        e);
            
        }
        
        try
        {
        
            Enumeration en = zf.entries ();
            
            while (en.hasMoreElements ())
            {
                
                ZipEntry ze = (ZipEntry) en.nextElement ();
                
                String n = ze.getName ();
                
                File zFile = new File (toDir.getPath () + "/" + n).getCanonicalFile ();
                
                // Make sure it is a subdirectory.
                if (!zFile.getPath ().startsWith (toDir.getCanonicalFile ().getPath ()))
                {
                    
                    throw new GeneralException ("Invalid zip entry: " + n + ", leads to a new file/directory that is outside of specified to dir: " +
                                                toDir);
                    
                }
    
                if (ze.isDirectory ())
                {
                                    
                    if (zFile.isFile ())
                    {
                        
                        throw new GeneralException ("Invalid zip entry: " + n + ", cannot create directory, a file already exists with that name: " +
                                                    zFile);
                        
                    }
                    
                    if (!zFile.exists ())
                    {

                        zFile.mkdirs ();
                        
                    }
                    
                } else {
                                                            
                    zFile.getParentFile ().mkdirs ();
                    
                    InputStream in = null;
                    
                    try
                    {
                    
                        in = zf.getInputStream (ze);
                    
                        BufferedOutputStream bout = new BufferedOutputStream (new FileOutputStream (zFile));                
                        
                        IOUtils.streamTo (in,
                                          bout,
                                          4096);
    
                        bout.flush ();
                        bout.close ();
    
                    } catch (Exception e) {
                        
                        throw new GeneralException ("Unable to write zip file entry: " + n + ", to: " +
                                                    zFile,
                                                    e);
                        
                    } finally {
                        
                        if (in != null)
                        {
                            
                            in.close ();
                            
                        }
                        
                    }
                    
                }
                
            }
            
        } catch (Exception e) {

            throw new GeneralException ("Unable to extract file: " +
                                        f +
                                        " to: " +
                                        toDir,
                                        e);
                    
        } finally {
            
            try
            {
            
                zf.close ();
                
            } catch (Exception e) {
                
                // Not worth recording, what are we gonna do?
                
            }
            
        }
        
    }

    public static String toString (Collection c,
                                   String     sep)
    {

        StringBuilder b = new StringBuilder ();
        
        Iterator iter = c.iterator ();
        
        while (iter.hasNext ())
        {
            
            Object o = iter.next ();
            
            String s = o + "";
            
            if (b.length () > 0)
            {
                
                b.append (sep);
                
            }
            
            b.append (s);
            
        }
        
        return b.toString ();
        
    }

    public static void writeBytesToFile (File   f,
                                         byte[] bytes)
                                         throws IOException
    {
        
        FileOutputStream fos = new FileOutputStream (f);
        fos.write (bytes);
        fos.close ();        
        
    }

    public static String getFileType (File f)
    {
        
        if (f == null)
        {
            
            return null;
            
        }
        
        if (f.isDirectory ())
        {
            
            return null;
            
        }
        
        int ind = f.getName ().lastIndexOf (".");
        
        if (ind > -1)
        {
            
            return f.getName ().substring (ind + 1);
            
        }
        
        return null;
        
    }

    public static String stripEnd (String s)
    {
        
        if (s == null)
        {
            
            return s;
        
        }

        int end = s.length () - 1;
        
        for (int i = end; i > -1; i--)
        {
            
            if (!Character.isWhitespace (s.charAt (i)))
            {
                
                return s.substring (0,
                                    i + 1);
                
            }
            
        }
        
        return s;
        
    }

    public static void printStackTrace ()
    {
        
        new Exception ().printStackTrace ();
        
    }
    
    public static com.gentlyweb.properties.Properties findPropertyInChain (String                              name,
                                                                           com.gentlyweb.properties.Properties props)
    {

        if (props.getPropertyNoParent (name) != null)
        {

            return props;

        }

        props = props.getParentProperties ();

        while (props != null)
        {

            if (props.getPropertyNoParent (name) != null)
            {

                return props;

            }

            props = props.getParentProperties ();

        }

        return null;

    }

    public static String getPropertiesIdChain (com.gentlyweb.properties.Properties props)
    {

        StringBuilder b = new StringBuilder ();

        b.append (props.getId ());

        com.gentlyweb.properties.Properties parent = props.getParentProperties ();

        while (parent != null)
        {

            b.append (" -> ");
            b.append (parent.getId ());

            parent = parent.getParentProperties ();

        }

        return b.toString ();

    }

    public static String getStackTrace (Throwable e)
    {
        
        StackTraceElement[] els = (e != null ? e.getStackTrace () : new Exception ().getStackTrace ());
        
        StringBuilder b = new StringBuilder ();
        
        for (int i = 0; i < els.length; i++)
        {
            
            b.append (els[i]);
            b.append (String.valueOf ('\n'));
            
        }
        
        return b.toString ();
        
    }

    public static String formatDate (Date d)
    {

        return new SimpleDateFormat (Environment.getProperty (Constants.DATE_FORMAT_PROPERTY_NAME)).format (d);

    }

    public static Date getDate (String d)
    {

        try
        {

            return new SimpleDateFormat (Environment.getProperty (Constants.DATE_FORMAT_PROPERTY_NAME)).parse (d);

        } catch (Exception e)
        {

            Environment.logError ("Unable to parse date: " +
                                  d +
                                  ", using format: " +
                                  Environment.getProperty (Constants.DATE_FORMAT_PROPERTY_NAME),
                                  e);

            return null;

        }

    }

    public static String sanitizeForFilename (String n)
    {

        char[] chars = n.toLowerCase ().toCharArray ();

        StringBuilder b = new StringBuilder ();

        for (int i = 0; i < chars.length; i++)
        {

            if ((Character.isLetterOrDigit (chars[i])) ||
                (chars[i] == ' '))
            {

                b.append (chars[i]);

            }

        }

        return b.toString ().trim ();

    }

    public static long getTimeAsMillis (String v)
    {

        if (v.equals (Constants.MINS_5))
        {

            return 5 * 60000;

        }

        if (v.equals (Constants.MINS_10))
        {

            return 10 * 60000;

        }

        if (v.equals (Constants.MINS_20))
        {

            return 20 * 60000;

        }

        if (v.equals (Constants.MINS_30))
        {

            return 30 * 60000;

        }

        if (v.equals (Constants.HOUR_1))
        {

            return 60 * 60000;

        }

        if (v.equals (Constants.HOURS_12))
        {

            return 12 * 60 * 60000;

        }

        if (v.equals (Constants.HOURS_24))
        {

            return 24 * 60 * 60000;

        }

        if (v.equals (Constants.DAYS_2))
        {

            return 2 * 24 * 60 * 60000;

        }

        if (v.equals (Constants.DAYS_5))
        {

            return 5 * 24 * 60 * 60000;

        }

        if (v.equals (Constants.WEEK_1))
        {

            return 7 * 24 * 60 * 60000;

        }

        return 0;

    }

    public static void createQuollWriterDirFile (File d)
                                          throws IOException
    {

        if (!d.exists ())
        {

            return;

        }

        if (d.isFile ())
        {

            return;

        }

        IOUtils.writeStringToFile (new File (d.getPath () + "/" + Constants.QUOLLWRITER_DIR_FILE_NAME),
                                   "This file indicates to quollwriter that the parent directory can be safely deleted or copied.",
                                   true);

    }

    public static boolean isDirectoryEmpty (File d)
    {
        
        File[] files = d.listFiles ();
        
        if ((files != null)
            ||
            (files.length == 0)
           )
        {
            
            return false;
            
        }
        
        return true;
        
    }
    
    public static File getQuollWriterDirFile (File parent)
    {
        
        return new File (parent.getPath () + "/" + Constants.QUOLLWRITER_DIR_FILE_NAME);
        
    }
    
    public static void copyDir (File from,
                                File to)
                         throws IOException
    {

        // Only copy directories that have the specified file in it.
        File f = new File (from.getPath () + "/" + Constants.QUOLLWRITER_DIR_FILE_NAME);

        if (!f.exists ())
        {

            return;

        }

        if (to.exists ())
        {

            return;

        }

        to.mkdirs ();

        File[] files = from.listFiles ();

        for (int i = 0; i < files.length; i++)
        {

            f = files[i];

            if (f.isFile ())
            {

                // Create the new file.
                File nf = new File (to.getPath () + "/" + f.getName ());

                if (nf.exists ())
                {

                    // Don't overwrite.
                    continue;

                }

                IOUtils.copyFile (f,
                                  nf,
                                  4096);

            } else
            {

                // Create a new directory.
                File nd = new File (to.getPath () + "/" + f.getName ());

                if (nd.exists ())
                {

                    // Ignore.
                    continue;

                }

                Utils.copyDir (f,
                               nd);

            }

        }

    }

    public static void deleteDir (File d)
    {

        if (d.isFile ())
        {

            return;

        }

        // See if there is a file that indicates we can delete this directory and it's contents.
        File canDeleteF = new File (d.getPath () + "/" + Constants.QUOLLWRITER_DIR_FILE_NAME);

        if (!canDeleteF.exists ())
        {

            return;

        }

        // If we are here then we can delete this directory and it's contents.
        File[] files = d.listFiles ();

        for (int i = 0; i < files.length; i++)
        {

            File f = files[i];

            if (f.isFile ())
            {

                f.delete ();

            } else
            {

                Utils.deleteDir (f);

            }

        }

        d.delete ();

    }

}
