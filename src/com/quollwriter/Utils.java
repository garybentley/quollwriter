package com.quollwriter;

import java.io.*;
import java.awt.event.*;

import java.text.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;
import java.nio.file.*;
import java.nio.charset.*;

import javax.swing.*;

import org.jdom.*;

import com.gentlyweb.xml.*;

import com.gentlyweb.utils.*;
import com.quollwriter.ui.*;

import com.quollwriter.data.*;

public class Utils
{

    public static String getStreamAsString (InputStream in,
                                            Charset     ch)
                                     throws Exception
    {

        BufferedInputStream _in = new BufferedInputStream (in);

        ByteArrayOutputStream out = new ByteArrayOutputStream ();

        int r = -1;

        byte[] bytes = new byte[65536];

        while ((r = _in.read (bytes, 0, 65536)) > 0)
        {

            out.write (bytes,
                       0,
                       r);

        }

        out.flush ();
        out.close ();
        _in.close ();

        return (ch != null ? new String (bytes, ch) : new String (bytes));

    }

    public static String getFileAsString (File    f,
                                          Charset ch)
                                   throws Exception
    {

        return Utils.getStreamAsString (new FileInputStream (f),
                                        ch);

    }

    public static String keyStrokeToString (KeyStroke k)
    {

        if (k == null)
        {

            return null;

        }

        StringBuilder b = new StringBuilder ();

        String m = KeyEvent.getKeyModifiersText (k.getModifiers ());

        if (m.length () > 0)
        {

            b.append (StringUtils.replaceString (m.toLowerCase (),
                                                 "+",
                                                 " "));
            b.append (" ");

        }

        b.append (KeyEvent.getKeyText (k.getKeyCode ()).toUpperCase ());

        return b.toString ();

    }

    public static List<String> splitString (String str,
                                            String separator)
    {

        List<String> ret = new ArrayList ();

        if (str == null)
        {

            return ret;

        }

        StringTokenizer t = new StringTokenizer (str,
                                                 separator);

        while (t.hasMoreTokens ())
        {

            ret.add (t.nextToken ());

        }

        return ret;

    }

    public static String joinStrings (Collection<String> items,
                                      String             separator)
    {

        if ((items == null)
            ||
            (items.size () == 0)
           )
        {

            return null;

        }

        StringBuilder b = new StringBuilder ();

        Iterator<String> iter = items.iterator ();

        while (iter.hasNext ())
        {

            b.append (iter.next ());

            if (iter.hasNext ())
            {

                b.append (separator != null ? separator : ", ");

            }

        }

        return b.toString ();

    }

    public static boolean isSubDir (File parent,
                                    File sub)
    {

        File p = sub.getParentFile ();

        if (p == null)
        {

            return false;

        }

        if (p.equals (parent))
        {

            return true;

        }

        return Utils.isSubDir (parent,
                               p);

    }

    public static String checkEmail (String em)
    {

        String err = Environment.getUIString (LanguageStrings.form,
                                              LanguageStrings.errors,
                                              LanguageStrings.invalidemail);
        //"Email does not appear to be valid.";

        if ((em == null)
            ||
            (em.trim ().equals (""))
           )
        {

            return null;

        }

        int ind = em.indexOf ("@");

        if (ind < 1)
        {

            return err;

        }

        int ind2 = em.indexOf (".",
                               ind + 1);

        if (ind2 < 1)
        {

            return err;

        } else {

            if (em.length () - 1 == ind2)
            {

                return err;

            }

        }

        return null;

    }

    public static String getStatisticsAsXML (Map<ProjectInfo.Statistic, Object> stats)
    {

        if (stats == null)
        {

            return null;

        }

        if (stats.size () == 0)
        {

            return null;

        }

        Element root = new Element (Environment.XMLConstants.stats);

        Iterator<ProjectInfo.Statistic> iter = stats.keySet ().iterator ();

        while (iter.hasNext ())
        {

            ProjectInfo.Statistic s = iter.next ();

            Object v = stats.get (s);

            if ((s != null)
                &&
                (v != null)
               )
            {

                Element stat = new Element (Environment.XMLConstants.stat);

                root.addContent (stat);

                stat.setAttribute (Environment.XMLConstants.id,
                                   s.getType ());
                stat.addContent (v.toString ());

                if (v instanceof String)
                {

                    stat.setAttribute (Environment.XMLConstants.type,
                                       "string");

                }

                if (v instanceof Number)
                {

                    stat.setAttribute (Environment.XMLConstants.type,
                                       "number");

                }

            }

        }

        try
        {

            return JDOMUtils.getElementAsString (root);

        } catch (Exception e) {

            Environment.logError ("Unable to convert element to string for statistics: " +
                                  stats,
                                  e);

            return null;

        }

    }

    public static Map<ProjectInfo.Statistic, Object> getStatisticsFromXML (String t)
    {

        Map<ProjectInfo.Statistic, Object> ret = new HashMap ();

        if (t == null)
        {

            return ret;

        }

        Element root = null;

        try
        {

            root = JDOMUtils.getStringAsElement (t);

        } catch (Exception e) {

            Environment.logError ("Unable to convert string: " +
                                  t +
                                  " to an element",
                                  e);

            return ret;

        }

        List els = null;

        try
        {

            els = JDOMUtils.getChildElements (root,
                                              Environment.XMLConstants.stat,
                                              false);

        } catch (Exception e) {

            Environment.logError ("Unable to get child stat elements: " +
                                  e);

            return ret;

        }

        for (int i = 0; i < els.size (); i++)
        {

            Element el = (Element) els.get (i);

            try
            {

                String val = JDOMUtils.getChildContent (el);

                String id = JDOMUtils.getAttributeValue (el,
                                                         Environment.XMLConstants.id,
                                                         true);

                String type = JDOMUtils.getAttributeValue (el,
                                                           Environment.XMLConstants.type,
                                                           true);

                if (type.equals ("number"))
                {

                    ret.put (ProjectInfo.Statistic.valueOf (id),
                             Double.parseDouble (val));

                }

                if (type.equals ("string"))
                {

                    ret.put (ProjectInfo.Statistic.valueOf (id),
                             val);

                }

            } catch (Exception e) {

                Environment.logError ("Unable to get stats info from element: " +
                                      JDOMUtils.getPath (el),
                                      e);

            }

        }

        return ret;

    }

    public static Set<File> getFilesFromXML (String t)
    {

        Set<File> ret = new LinkedHashSet ();

        if (t == null)
        {

            return ret;

        }

        Element root = null;

        try
        {

            root = JDOMUtils.getStringAsElement (t);

        } catch (Exception e) {

            Environment.logError ("Unable to convert string: " +
                                  t +
                                  " to an element",
                                  e);

            return ret;

        }

        List els = null;

        try
        {

            els = JDOMUtils.getChildElements (root,
                                              Environment.XMLConstants.file,
                                              false);

        } catch (Exception e) {

            Environment.logError ("Unable to get child file elements: " +
                                  e);

            return ret;

        }

        // Get the user ones.
        for (int i = 0; i < els.size (); i++)
        {

            Element el = (Element) els.get (i);

            try
            {

                String tf = JDOMUtils.getChildContent (el);

                File f = new File (tf);

                ret.add (f);

            } catch (Exception e) {

                Environment.logError ("Unable to get file info from element: " +
                                      JDOMUtils.getPath (el),
                                      e);

            }

        }

        return ret;

    }

    public static String getFilesAsXML (Set<File> files)
    {

        if (files == null)
        {

            return null;

        }

        if (files.size () == 0)
        {

            return null;

        }

        Element root = new Element (Environment.XMLConstants.files);

        for (File f : files)
        {

            Element fel = new Element (Environment.XMLConstants.file);

            root.addContent (fel);

            fel.addContent (f.getPath ());

        }

        try
        {

            return JDOMUtils.getElementAsString (root);

        } catch (Exception e) {

            Environment.logError ("Unable to convert element to string for files: " +
                                  files,
                                  e);

            return null;

        }

    }

    public static void getContentFromURL (final URL     url,
                                          final Map<String, String> headers,
                                          final ActionListener      onSuccess,
                                          final ActionListener      onError,
                                          final ActionListener      onFailure)
    {

        new Thread (new Runnable ()
        {

            public void run ()
            {

                try
                {

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection ();

                    if (headers != null)
                    {

                        Iterator<String> iter = headers.keySet ().iterator ();

                        while (iter.hasNext ())
                        {

                            String n = iter.next ();

                            conn.setRequestProperty (n,
                                                     headers.get (n));

                        }

                    }

                    conn.setRequestMethod ("GET");

                    conn.setDoInput (true);
                    conn.setDoOutput (true);
                    conn.connect ();

                    // Try and get input stream, not all responses allow it.
                    InputStream in = null;

                    final int resCode = conn.getResponseCode ();

                    if (resCode != HttpURLConnection.HTTP_OK)
                    {

                        in = conn.getErrorStream ();

                    } else {

                        in = conn.getInputStream ();

                    }

                    byte[] content = null;

                    if (in != null)
                    {

                        ByteArrayOutputStream bout = new ByteArrayOutputStream ();

                        // Read everything.
                        byte[] buf = new byte[8192];

                        int count = 0;

                        while ((count = in.read (buf,
                                                 0,
                                                 8192)) != -1)
                        {

                            bout.write (buf,
                                        0,
                                        count);

                        }

                        bout.flush ();
                        bout.close ();
                        in.close ();

                        content = bout.toByteArray ();

                    }

                    final byte[] ret = content;

                    conn.disconnect ();

                    if (resCode != HttpURLConnection.HTTP_OK)
                    {

                        if (onError != null)
                        {

                            UIUtils.doLater (new ActionListener ()
                                             {

                                                public void actionPerformed (ActionEvent ev)
                                                {

                                                    onError.actionPerformed (new ActionEvent (ret, resCode, "error"));

                                                }

                                             });

                        }

                    } else {

                        if (onSuccess != null)
                        {

                            UIUtils.doLater (new ActionListener ()
                                             {

                                                public void actionPerformed (ActionEvent ev)
                                                {

                                                    onSuccess.actionPerformed (new ActionEvent (ret, resCode, "success"));

                                                }

                                             });

                        }

                    }

                } catch (final Exception e) {

                    if (onFailure != null)
                    {

                        UIUtils.doLater (new ActionListener ()
                                         {

                                            public void actionPerformed (ActionEvent ev)
                                            {

                                                onFailure.actionPerformed (new ActionEvent (e, 0, "exception"));

                                            }

                                         });

                    }

                }

            }

        }).start ();

    }

    public static void postToURL (final URL                 url,
                                  final Map<String, String> headers,
                                  final String              content,
                                  final ActionListener      onSuccess,
                                  final ActionListener      onError,
                                  final ActionListener      onFailure)
    {

        new Thread (new Runnable ()
        {

            public void run ()
            {

                try
                {

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection ();

                    if (headers != null)
                    {

                        Iterator<String> iter = headers.keySet ().iterator ();

                        while (iter.hasNext ())
                        {

                            String n = iter.next ();

                            conn.setRequestProperty (n,
                                                     headers.get (n));

                        }

                    }

                    byte[] cb = content.getBytes ();
                    conn.setRequestProperty ("content-length",
                                             cb.length + "");

                    conn.setRequestMethod ("POST");

                    conn.setDoInput (true);
                    conn.setDoOutput (true);
                    conn.connect ();

                    BufferedWriter out = new BufferedWriter (new OutputStreamWriter (conn.getOutputStream (), StandardCharsets.UTF_8));
                    out.write (content);

                    out.flush ();
                    out.close ();

                    // Try and get input stream, not all responses allow it.
                    InputStream in = null;

                    final int resCode = conn.getResponseCode ();

                    if (resCode != HttpURLConnection.HTTP_OK)
                    {

                        in = conn.getErrorStream ();

                    } else {

                        in = conn.getInputStream ();

                    }

                    String r = null;

                    if (in != null)
                    {

                        StringBuilder b = new StringBuilder ();

                        BufferedReader bin = new BufferedReader (new InputStreamReader (in));

                        // Read everything.
                        char chars[] = new char[8192];

                        int count = 0;

                        while ((count = bin.read (chars,
                                                  0,
                                                  8192)) != -1)
                        {

                            b.append (chars,
                                      0,
                                      count);

                        }

                        String s = b.toString ();

                        String pref = Constants.JSON_RETURN_PREFIX; //"for(;;);";

                        if (s.startsWith (pref))
                        {

                            s = s.substring (pref.length ());

                        }

                        r = s;

                    }

                    final String ret = r;

                    conn.disconnect ();

                    if (resCode != HttpURLConnection.HTTP_OK)
                    {

                        if (onError != null)
                        {

                            UIUtils.doLater (new ActionListener ()
                                             {

                                                public void actionPerformed (ActionEvent ev)
                                                {

                                                    onError.actionPerformed (new ActionEvent (ret, resCode, "error"));

                                                }

                                             });

                        }

                    } else {

                        if (onSuccess != null)
                        {

                            UIUtils.doLater (new ActionListener ()
                                             {

                                                public void actionPerformed (ActionEvent ev)
                                                {

                                                    onSuccess.actionPerformed (new ActionEvent (ret, resCode, "success"));

                                                }

                                             });

                        }

                    }

                } catch (final Exception e) {

                    if (onFailure != null)
                    {

                        UIUtils.doLater (new ActionListener ()
                                         {

                                            public void actionPerformed (ActionEvent ev)
                                            {

                                                onFailure.actionPerformed (new ActionEvent (e, 0, "exception"));

                                            }

                                         });

                    }

                }

            }

        }).start ();

    }

    public static void addDirToZip (File f,
                                    File dirToWrite)
                             throws GeneralException
    {

        if ((dirToWrite == null)
            ||
            (f == null)
           )
        {

            // Naughty but shouldn't happen.
            throw new IllegalArgumentException ("Invalid args");

        }

        if (dirToWrite.isFile ())
        {

            throw new IllegalArgumentException ("To write directory is actually a file: " +
                                                dirToWrite);

        }

        if (f.isDirectory ())
        {

            throw new IllegalArgumentException ("File to write to is a directory: " +
                                                f);

        }

        try
        {

            Map<String, String> env = new HashMap ();
            env.put ("create", "true");

            FileSystem fs = FileSystems.newFileSystem (Paths.get (f.getPath ()),
                                                       null);

            File[] files = dirToWrite.listFiles ();

            for (int i = 0; i < files.length; i++)
            {

                Path fp = Paths.get (files[i].toURI ());

                Path zp = fs.getPath ("/" + files[i].getParentFile ().getName () + "/" + files[i].getName ());

                Files.createDirectories (zp.getParent ());

                Files.copy (fp,
                            zp,
                            StandardCopyOption.REPLACE_EXISTING);

            }

        } catch (Exception e) {

            throw new GeneralException ("Unable to write dir to file: " +
                                        dirToWrite +
                                        ", file: " +
                                        f,
                                        e);

        }

    }

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

    public static boolean isToday (Date d)
    {

        if (d == null)
        {

            return false;

        }

        Calendar now = new GregorianCalendar ();

        Calendar dCal = new GregorianCalendar ();
        dCal.setTime (d);

        return (now.get (Calendar.DAY_OF_MONTH) == dCal.get (Calendar.DAY_OF_MONTH))
               &&
               (now.get (Calendar.MONTH) == dCal.get (Calendar.MONTH))
               &&
               (now.get (Calendar.YEAR) == dCal.get (Calendar.YEAR));

    }

    public static boolean isYesterday (Date d)
    {

        if (d == null)
        {

            return false;

        }

        Calendar now = new GregorianCalendar ();

        // Take off one day.
        now.add (Calendar.DAY_OF_MONTH,
                 -1);

        Calendar dCal = new GregorianCalendar ();
        dCal.setTime (d);

        return (now.get (Calendar.DAY_OF_MONTH) == dCal.get (Calendar.DAY_OF_MONTH))
               &&
               (now.get (Calendar.MONTH) == dCal.get (Calendar.MONTH))
               &&
               (now.get (Calendar.YEAR) == dCal.get (Calendar.YEAR));

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

        if (v == null)
        {

            return 0;

        }

        // Is this a legacy value, pre 2.6.5?
        if (v.indexOf (" ") > 0)
        {

            long min = 60000;

            if (v.equals ("5 mins"))
            {

                return 5 * min;

            }

            if (v.equals ("10 mins"))
            {

                return 10 * min;

            }

            if (v.equals ("20 mins"))
            {

                return 20 * min;

            }

            if (v.equals ("30 mins"))
            {

                return 30 * min;

            }

            long hour = 60 * min;

            if (v.equals ("1 hour"))
            {

                return 1 * hour;

            }

            if (v.equals ("12 hours"))
            {

                return 12 * hour;

            }

            if (v.equals ("24 hours"))
            {

                return 24 * hour;

            }

            long day = hour * 24;

            if (v.equals ("2 days"))
            {

                return 2 * day;

            }

            if (v.equals ("5 days"))
            {

                return 5 * day;

            }

            if (v.equals ("1 week"))
            {

                return 7 * day;

            }

        }

        try
        {

            return Long.parseLong (v);

        } catch (Exception e) {

            Environment.logError ("Unable to parse: " + v + " into a long.",
                                  e);

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
                                   false);

    }

    public static boolean isDirectoryEmpty (File d)
    {

        if (d == null)
        {

            return true;

        }

        if (!d.exists ())
        {

            return true;

        }

        if (d.isFile ())
        {

            return false;

        }

        File[] files = d.listFiles ();

        if ((files != null)
            &&
            (files.length > 0)
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

    public static void copyFilesToDir (Set<File> files,
                                       File      to)
                                throws IOException
    {

        if (!to.exists ())
        {

            throw new IllegalArgumentException ("To dir must exist.");

        }

        if (to.isFile ())
        {

            throw new IllegalArgumentException ("To dir is a file.");

        }

        File tof = new File (to.getPath () + "/" + Constants.QUOLLWRITER_DIR_FILE_NAME);

        if (!tof.exists ())
        {

            throw new IllegalArgumentException ("To dir doesn't look like a Quoll Writer dir.");

        }

        for (File f : files)
        {

            if (f.isFile ())
            {

                // Create the new file.
                File nf = new File (to.getPath (),
                                    f.getName ());

                if (nf.exists ())
                {

                    // Don't overwrite.
                    continue;

                }

                IOUtils.copyFile (f,
                                  nf,
                                  4096);

            }

        }

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

        if (d == null)
        {

            return;

        }

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

                if (!f.delete ())
                {

                    Environment.logError ("Unable to delete file: " + f);

                }

            } else
            {

                Utils.deleteDir (f);

            }

        }

        d.delete ();

    }

    public static Date zeroTimeFields (Date d)
    {

        GregorianCalendar gc = new GregorianCalendar ();
        gc.setTime (d);

        gc.set (Calendar.HOUR_OF_DAY,
                0);
        gc.set (Calendar.MINUTE,
                0);
        gc.set (Calendar.SECOND,
                0);
        gc.set (Calendar.MILLISECOND,
                0);

        return gc.getTime ();

    }

    public static String formatAsDuration (double duration)
    {

        long millis = (long) duration;

        long days = 0;
        long hours = 0;
        long mins = 0;

        long min = 60 * 1000;
        long hour = 60 * min;
        long day = 24 * hour;

        days = millis / day;

        millis = millis - (day * days);

        hours = millis / hour;

        millis = millis - (hour * hours);

        mins = millis / min;

        StringBuilder b = new StringBuilder ();

        if (days > 0)
        {

            b.append (days);
            b.append ("d");

        }

        if (hours > 0)
        {

            if (b.length () > 0)
            {

                b.append (" ");

            }

            b.append (hours);
            b.append ("h");

        }

        if (mins > 0)
        {

            if (b.length () > 0)
            {

                b.append (" ");

            }

            b.append (mins);
            b.append ("m");

        }

        if (b.length () == 0)
        {

            b.append ("0m");

        }

        return b.toString ();

    }

}
