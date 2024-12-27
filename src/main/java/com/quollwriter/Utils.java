package com.quollwriter;

import java.io.*;
import java.awt.event.*;
import java.time.*;
import java.time.temporal.*;
import java.nio.file.*;
import java.text.*;
import java.net.*;
import java.util.jar.*;
import java.util.*;
import java.util.function.*;
import java.util.zip.*;
import java.util.stream.*;
import java.nio.file.*;
import java.nio.charset.*;

import javax.swing.*;

import javafx.beans.property.*;

import org.dom4j.*;
import org.dom4j.tree.*;

import com.quollwriter.ui.*;

import com.quollwriter.data.*;
import com.quollwriter.events.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class Utils
{

    public static <T> List<T> newList (Collection<T> l,
                                       T...    els)
    {

        List<T> ret = new ArrayList<> ();

        if (l != null)
        {

            ret.addAll (l);

        }

        if (els != null)
        {

            ret.addAll (Arrays.asList (els));

        }

        return ret;

    }

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

        return (ch != null ? new String (out.toByteArray (), ch) : new String (out.toByteArray ()));

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

        String m = KeyEvent.getModifiersExText (k.getModifiers ());

        if (m.length () > 0)
        {

            b.append (Utils.replaceString (m.toLowerCase (),
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

        List<String> ret = new ArrayList<> ();

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

    // TODO Remove.
    public static String checkEmail2 (String em)
    {

        StringProperty p = Utils.checkEmail (em);

        if (p != null)
        {

            return p.getValue ();

        }

        return null;

    }

    public static StringProperty checkEmail (String em)
    {

        StringProperty err = getUILanguageStringProperty (LanguageStrings.form,
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

        Element root = new DefaultElement (Environment.XMLConstants.stats);

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

                Element stat = new DefaultElement (Environment.XMLConstants.stat);

                root.add (stat);

                stat.addAttribute (Environment.XMLConstants.id,
                                   s.getType ());
                stat.add (new DefaultCDATA (v.toString ()));

                if (v instanceof String)
                {

                    stat.addAttribute (Environment.XMLConstants.type,
                                       "string");

                }

                if (v instanceof Number)
                {

                    stat.addAttribute (Environment.XMLConstants.type,
                                       "number");

                }

            }

        }

        try
        {

            return Utils.getAsXML (root);

        } catch (Exception e) {

            Environment.logError ("Unable to convert element to string for statistics: " +
                                  stats,
                                  e);

            return null;

        }

    }

    public static String getAsXML (Element el)
    {

        Document doc = DocumentHelper.createDocument ();
        doc.setRootElement (el);
        return doc.asXML ();

    }

    public static Map<ProjectInfo.Statistic, Object> getStatisticsFromXML (String t)
    {

        Map<ProjectInfo.Statistic, Object> ret = new HashMap<> ();

        if (t == null)
        {

            return ret;

        }

        Element root = null;

        try
        {

            root = DOM4JUtils.stringAsElement (t);

        } catch (Exception e) {

            Environment.logError ("Unable to convert string: " +
                                  t +
                                  " to an element",
                                  e);

            return ret;

        }

        root.elements (Environment.XMLConstants.stat).stream ()
            .forEach (el ->
            {

                try
                {

                    String val = el.getTextTrim ();

                    String id = DOM4JUtils.attributeValue (el,
                                                           Environment.XMLConstants.id,
                                                           true);

                    String type = DOM4JUtils.attributeValue (el,
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
                                          DOM4JUtils.getPath (el),
                                          e);

                }

            });

        return ret;

    }

    public static Set<Path> getFilesFromXML (String t)
    {

        Set<Path> ret = new LinkedHashSet<> ();

        if (t == null)
        {

            return ret;

        }

        Element root = null;

        try
        {

            root = DOM4JUtils.stringAsElement (t);

        } catch (Exception e) {

            Environment.logError ("Unable to convert string: " +
                                  t +
                                  " to an element",
                                  e);

            return ret;

        }

        ret.addAll (root.elements (Environment.XMLConstants.file).stream ()
            .map (el -> Paths.get (el.getTextTrim ()))
            .collect (Collectors.toList ()));
/*
TODO Remove
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

                ret.add (Paths.get (tf));

            } catch (Exception e) {

                Environment.logError ("Unable to get file info from element: " +
                                      JDOMUtils.getPath (el),
                                      e);

            }

        }
*/
        return ret;

    }

/*
TODO Remove
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
*/
    public static String getFilesAsXML (Set<Path> files)
    {

        if (files == null)
        {

            return null;

        }

        if (files.size () == 0)
        {

            return null;

        }

        Element root = new DefaultElement (Environment.XMLConstants.files);

        for (Path f : files)
        {

            Element fel = new DefaultElement (Environment.XMLConstants.file);

            root.add (fel);

            fel.add (new DefaultCDATA (f.toString ()));

        }

        try
        {

            return Utils.getAsXML (root);

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

    public static void postToURL (final URL                                      url,
                                  final Map<String, String>                      headers,
                                  final String                                   content,
                                  final BiConsumer<String, Integer>              onSuccess,
                                  final BiConsumer<String, Integer>              onError,
                                  final Consumer<Exception>                      onFailure,
                                  final UpdateEventListener<UploadProgressEvent> progressListener)
    {

        byte[] bytes = content.getBytes (StandardCharsets.UTF_8);

        Utils.postToURL (url,
                         headers,
                         "text/plain",
                         bytes.length,
                         new ByteArrayInputStream (bytes),
                         onSuccess,
                         onError,
                         onFailure,
                         progressListener);

    }

    public static void postToURL (final URL                                      url,
                                  final Map<String, String>                      headers,
                                  final Path                                     file,
                                  final BiConsumer<String, Integer>              onSuccess,
                                  final BiConsumer<String, Integer>              onError,
                                  final Consumer<Exception>                      onFailure,
                                  final UpdateEventListener<UploadProgressEvent> progressListener)
                           throws IOException
    {

        Utils.postToURL (url,
                         headers,
                         "application/octet-steam",
                         Files.size (file),
                         Files.newInputStream (file),
                         onSuccess,
                         onError,
                         onFailure,
                         progressListener);

    }

    public static void postToURL (final URL                 url,
                                  final Map<String, String> headers,
                                  final String              contentType,
                                  final long                contentLength,
                                  final InputStream         content,
                                  final BiConsumer<String, Integer> onSuccess,
                                  final BiConsumer<String, Integer> onError,
                                  final Consumer<Exception>         onFailure,
                                  final UpdateEventListener<UploadProgressEvent> progressListener)
    {

        Environment.scheduleImmediately (() ->
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

                conn.setRequestProperty ("content-type",
                                         contentType);
                conn.setRequestProperty ("content-length",
                                         contentLength + "");

                conn.setRequestMethod ("POST");

                conn.setDoInput (true);
                conn.setDoOutput (true);
                conn.connect ();

                int count = 0;
                int sent = 0;

                OutputStream out = conn.getOutputStream ();

                byte[] bytes = new byte[8192];

                while ((count = content.read (bytes,
                                              0,
                                              8192)) != -1)
                {

                    out.write (bytes,
                               0,
                               count);

                    sent += count;

                    if (progressListener != null)
                    {

                        int _sent = sent;

                        Environment.scheduleImmediately (() ->
                        {

                            progressListener.valueUpdated (new UploadProgressEvent (Environment.class,
                                                                                    _sent,
                                                                                    contentLength));

                        });

                    }

                }

                out.flush ();
                out.close ();

                if (progressListener != null)
                {

                    Environment.scheduleImmediately (() ->
                    {

                        progressListener.valueUpdated (new UploadProgressEvent (Environment.class,
                                                                                contentLength,
                                                                                contentLength));

                    });

                }

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

                    count = 0;

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

                        Environment.scheduleImmediately (() ->
                        {

                            onError.accept (ret, resCode);

                        });

                    }

                } else {

                    if (onSuccess != null)
                    {

                        Environment.scheduleImmediately (() ->
                        {

                            onSuccess.accept (ret, resCode);

                        });

                    }

                }

            } catch (final Exception e) {

                if (onFailure != null)
                {

                    Environment.scheduleImmediately (() ->
                    {

                        onFailure.accept (e);

                    });

                }

            }

        });

    }

    // TODO Use Paths.
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

            Map<String, String> env = new HashMap<> ();
            env.put ("create", "true");

            // JDK 12 - FileSystem fs = FileSystems.newFileSystem (f.toPath (), null);
            FileSystem fs = FileSystems.newFileSystem (f.toPath ());
            //FileSystem fs = FileSystems.newFileSystem (Paths.get (f.getPath ()).toUri (), env);

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

            fs.close ();

        } catch (Exception e) {

            throw new GeneralException ("Unable to write dir to file: " +
                                        dirToWrite +
                                        ", file: " +
                                        f,
                                        e);

        }

    }

    // TODO Move to paths.
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

                        Utils.streamTo (in,
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

    public static String getFileType (Path f)
    {

        if (f == null)
        {

            return null;

        }

        if (Files.isDirectory (f))
        {

            return null;

        }

        String fn = f.getFileName ().toString ();

        int ind = fn.lastIndexOf (".");

        if (ind > -1)
        {

            return fn.substring (ind + 1);

        }

        return null;

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

        b.append (e.getMessage ());

        for (int i = 0; i < els.length; i++)
        {

            b.append (els[i]);
            b.append (String.valueOf ('\n'));

        }

        Throwable c = e.getCause ();

        if (c != null)
        {

            b.append ("Caused by: " + c.getMessage () + "\n");
            b.append (Utils.getStackTrace (c));

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

        return new SimpleDateFormat (UserProperties.get (Constants.DATE_FORMAT_PROPERTY_NAME)).format (d);

    }

    public static Date getDate (String d)
    {

        try
        {

            return new SimpleDateFormat (UserProperties.get (Constants.DATE_FORMAT_PROPERTY_NAME)).parse (d);

        } catch (Exception e)
        {

            Environment.logError ("Unable to parse date: " +
                                  d +
                                  ", using format: " +
                                  UserProperties.get (Constants.DATE_FORMAT_PROPERTY_NAME),
                                  e);

            return null;

        }

    }

    public static String sanitizeForFilenameKeepCase (String n)
    {

        char[] chars = n.toCharArray ();

        StringBuilder b = new StringBuilder ();

        for (int i = 0; i < chars.length; i++)
        {

            if ((Character.isLetterOrDigit (chars[i]))
                ||
                (chars[i] == ' ')
                ||
                (chars[i] == '-')
               )
            {

                b.append (chars[i]);

            }

        }

        return b.toString ().trim ();

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

    @Deprecated
    public static void createQuollWriterDirFile (File d)
                                          throws GeneralException
    {

        Utils.createQuollWriterDirFile (d.toPath ());

    }

    public static void createQuollWriterDirFile (Path d)
                                          throws GeneralException
    {

        if (Files.notExists (d))
        {

            return;

        }

        if (!Files.isDirectory (d))
        {

            return;

        }

        Utils.writeStringToFile (d.resolve (Constants.QUOLLWRITER_DIR_FILE_NAME),
                                 "This file indicates to quollwriter that the parent directory can be safely deleted or copied.");

    }

    public static boolean isDirectoryEmpty (Path d)
    {

        try
        {

            if (Files.notExists (d))
            {

                return true;

            }
/*
            if (!Files.isDirectory (d))
            {
System.out.println ("RET1: " + d);
                return false;

            }
*/
            try (Stream<Path> ls = Files.list (d))
            {

                Path p = ls.findFirst ()
                        .orElse (null);

                return p == null;

            }

        } catch (Exception e) {

            Environment.logError ("Unable to determine if path is empty: " + d,
                                  e);

            return false;

        }

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
/*
TODO REmove
    public static File getQuollWriterDirFile (File parent)
    {

        return new File (parent.getPath () + "/" + Constants.QUOLLWRITER_DIR_FILE_NAME);

    }
*/
    public static Path getQuollWriterDirFile (Path parent)
    {

        return parent.resolve (Constants.QUOLLWRITER_DIR_FILE_NAME);

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

                Files.copy (f.toPath (),
                            nf.toPath ());

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

                Files.copy (f.toPath (),
                            nf.toPath ());

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

    public static void deleteDir (Path d)
                           throws GeneralException
    {

        if (d == null)
        {

            return;

        }

        if (!Files.isDirectory (d))
        {

            return;

        }

        // See if there is a file that indicates we can delete this directory and its contents.
        if (Files.notExists (d.resolve (Constants.QUOLLWRITER_DIR_FILE_NAME)))
        {

            return;

        }

        try
        {

            // Need the try(){} to auto close the stream.
            try (Stream<Path> ls = Files.list (d))
            {

                ls.forEach (f ->
                {

                    if (Files.notExists (f))
                    {

                        return;

                    }

                    try
                    {

                        if (Files.isDirectory (f))
                        {

                            Utils.deleteDir (f);

                        } else {

                            Files.delete (f);

                        }

                    } catch (Exception e) {

                        throw new RuntimeException ("Unable to delete file: " + f,
                                                    e);

                    }

                });

            }

            Files.delete (d);

        } catch (Exception e) {

            throw new GeneralException ("Unable to delete: " + d,
                                        e);

        }

    }
/*
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
*/
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

    public static Path getAsPath (URI u)
                           throws IOException
    {

        try
        {

            FileSystems.getFileSystem (u);

        } catch (FileSystemNotFoundException e) {

            Map<String, String> env = new HashMap<> ();
            env.put ("create", "true");
            FileSystems.newFileSystem (u, env);

        } catch (IllegalArgumentException e) {

            FileSystems.getDefault ();

        }

        return Paths.get (u);

    }

    public static void createZipFile (Path                file,
                                      Map<String, Object> entries)
                               throws GeneralException
    {

        URI uri = URI.create ("jar:" + file.toFile ().toURI ().toString ());

        Map<String, String> env = new HashMap<>();
        env.put ("create", "true");
        env.put ("encoding", "UTF-8");

        try
        {

            Files.deleteIfExists (file);

        } catch (Exception e) {

            throw new GeneralException ("Unable to delete file: " + file,
                                        e);

        }

        try (FileSystem zipfs = FileSystems.newFileSystem (uri, env))
        {

            for (String k : entries.keySet ())
            {

                Object o = entries.get (k);

                Path zk = zipfs.getPath ((k.startsWith ("/") ? "" : "/") + k);

                Path pzk = zk.getParent ();

                if (pzk != null)
                {

                    Files.createDirectories (pzk);

                }

                if (o instanceof String)
                {

                    String s = (String) o;

                    Files.copy (new ByteArrayInputStream (s.getBytes (StandardCharsets.UTF_8)),
                                zk);

                }

                if (o instanceof File)
                {

                    File f = (File) o;

                    Files.copy (Paths.get (f.toURI ()),
                                zk);

                }

                if (o instanceof Path)
                {

                    Path p = (Path) o;

                    Files.copy (p,
                                zk);

                }

            }

        } catch (Exception e) {

            throw new GeneralException ("Unable to create/write to zip file: " + file,
                                        e);

        }

    }

    public static String getFileContentAsString (Path f)
                                          throws GeneralException
    {

        try
        {

            return new String (Files.readAllBytes (f),
                               StandardCharsets.UTF_8);

        } catch (Exception e) {

            throw new GeneralException ("Unable to get content of file: " + f,
                                        e);

        }

    }

    public static void writeStringToFile (Path   f,
                                          String v)
                                   throws GeneralException
    {

        try
        {

            try (BufferedWriter bw = Files.newBufferedWriter (f,
                                                              StandardCharsets.UTF_8))
            {

                bw.write (v,
                          0,
                          v.length ());
                bw.flush ();
                bw.close ();

            }

        } catch (Exception e) {

            throw new GeneralException ("Unable to write string to: " + f,
                                        e);

        }

    }

    public static File writeStreamToTempFile (InputStream in)
                                       throws IOException
    {

        // TODO Change to use a path.
        File f = File.createTempFile ("___" + Constants.QUOLL_WRITER_DIR_NAME + System.currentTimeMillis (),
                                      null);

        BufferedOutputStream bout = new BufferedOutputStream (new FileOutputStream (f));

        Utils.streamTo (new BufferedInputStream (in),
                          bout,
                          4096);

        bout.flush ();
        bout.close ();

        return f;

    }

    public static void extractResourceToFile (String name,
                                              File   outFile)
                                       throws GeneralException
    {

        try
        {

            BufferedInputStream bin = new BufferedInputStream (Environment.class.getResourceAsStream (name));

            BufferedOutputStream bout = new BufferedOutputStream (new FileOutputStream (outFile));

            Utils.streamTo (bin,
                              bout,
                              4096);

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to stream resource: " +
                                        name +
                                        " to output file: " +
                                        outFile,
                                        e);

        }

    }

    public static Date zeroTimeFieldsForDate (Date d)
    {

        GregorianCalendar gc = new GregorianCalendar ();
        gc.setTime (d);

        Utils.zeroTimeFields (gc);

        return gc.getTime ();

    }

    public static void zeroTimeFields (GregorianCalendar gc)
    {

        // Zero-out the non date fields.
        gc.set (Calendar.HOUR_OF_DAY,
                0);
        gc.set (Calendar.MINUTE,
                0);
        gc.set (Calendar.SECOND,
                0);
        gc.set (Calendar.MILLISECOND,
                0);

    }

    public static URL getResourceUrl (String path)
                               throws Exception
    {

        URL u = Environment.class.getResource (path);

        URI ui = null;

        try
        {

            ui = u.toURI ();

        } catch (Exception e) {

            throw new IllegalArgumentException ("Unable to convert path: " + path + " to a uri.",
                                                e);

        }
/*
        try
        {

            FileSystems.getFileSystem (ui);

        } catch (Exception e) {

            Map<String, String> env = new HashMap<> ();
            env.put ("create", "true");
            FileSystems.newFileSystem (ui, env);

        }
*/
        return u;

    }

    /**
     * List directory contents for a resource folder. Not recursive.
     * This is basically a brute-force implementation.
     * Works for regular files and also JARs.
     *
     * @author Greg Briggs
     * @param clazz Any java class that lives in the same place as the resources you want.
     * @param path Should end with "/", but not start with one.
     * @return Just the name of each member item, not the full paths.
     * @throws URISyntaxException
     * @throws IOException
     */
    public static Set<String> getResourceListing (String path)
                                           throws URISyntaxException,
                                                  GeneralException,
                                                  IOException
    {

        URL dirURL = Environment.class.getResource (path);

        if (dirURL == null)
        {

            throw new GeneralException ("Unable to find resource: " + path);

        }

        if (dirURL.getProtocol ().equals ("jar"))
        {

            /* A JAR path */
            String jarPath = dirURL.getPath ().substring (5,
                                                          dirURL.getPath ().indexOf ("!")); // strip out only the JAR file

            JarFile jar = new JarFile (URLDecoder.decode (jarPath,
                                                          "UTF-8"));

            Enumeration<JarEntry> entries = jar.entries (); // gives ALL entries in jar

            Set<String> result = new HashSet<String> (); // avoid duplicates in case it is a subdirectory

            while (entries.hasMoreElements ())
            {

                String name = "/" + entries.nextElement ().getName ();

                if (name.startsWith (path))
                { // filter according to the path

                    String entry = name.substring (path.length ());

                    if (entry.length () == 0)
                    {

                        continue;

                    }

                    int checkSubdir = entry.indexOf ("/");

                    if (checkSubdir >= 0)
                    {

                        // if it is a subdirectory, we just return the directory name
                        entry = entry.substring (0,
                                                 checkSubdir);

                    }

                    result.add (entry);
                }

            }

            return result;

        }

        throw new UnsupportedOperationException ("Cannot list files for URL " + dirURL);
    }

    public static InputStream getResourceStream (String name)
    {

        return Environment.class.getResourceAsStream (name);

    }

    public static String getResourceFileAsString (String name)
                                           throws GeneralException
    {

        // TODO Change this...
        InputStream is = Utils.getResourceStream (name);

        if (is == null)
        {

            return null;

        }

        StringBuilder b = new StringBuilder ();

        BufferedReader r = null;

        try
        {

            r = new BufferedReader (new InputStreamReader (is, "utf-8"));

            String line = r.readLine ();

            while (line != null)
            {

                b.append (line);
                b.append ('\n');

                line = r.readLine ();

            }

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to read from resource: " +
                                        name,
                                        e);

        } finally
        {

            // Oh how I hate this...
            try
            {

                if (r != null)
                {

                    r.close ();

                }

            } catch (Exception e)
            {

                // Ignore...  What could we do otherwise?

            }

        }

        return b.toString ();

    }

    // TODO: Change to use a new thread.
    public static String getUrlFileAsString (URL    url)
                                             throws Exception
    {

        URLConnection c = url.openConnection ();

        InputStream bin = c.getInputStream ();

        return Utils.getStreamAsString (bin,
                                        StandardCharsets.UTF_8);

    }

    public static int getPercent (float t,
                                  float b)
    {

        if (b == 0)
        {

            return 0;

        }

        return (int) ((t / b) * 100);

    }

    public static LocalDate dateToLocalDate (Date d)
    {

        LocalDate ld = null;

        if (d != null)
        {

            ld = Instant.ofEpochMilli (d.getTime ())
                .atZone (ZoneId.systemDefault ())
                .toLocalDate ();
            //ld = LocalDate.from (i);

        }

        return ld;

    }

    public static Date localDateToDate (LocalDate d)
    {

        if (d == null)
        {

            return null;

        }

        return Date.from (d.atStartOfDay (ZoneId.systemDefault ()).toInstant ());

    }

    public static boolean isProjectDir (Path p)
    {

        Path pf = p.resolve (Constants.PROJECT_DB_FILE_NAME_PREFIX + Constants.H2_DB_FILE_SUFFIX);

        if ((Files.exists (pf))
            &&
            (!Files.isDirectory (pf))
           )
        {

            return true;

        }

        return false;

    }

    public static boolean isBackupFile (Path p)
                                 throws IOException
    {

        if (Files.isDirectory (p))
        {

            return false;

        }

        if (Files.notExists (p))
        {

            return false;

        }

        String fn = p.getFileName ().toString ();

        if  ((!fn.startsWith (Constants.BACKUP_FILE_NAME_PREFIX))
             ||
             (!fn.endsWith (Constants.BACKUP_FILE_NAME_SUFFIX))
            )
        {

            return false;

        }

        // JDK 12 - FileSystem fs = FileSystems.newFileSystem (p, null);
        FileSystem fs = FileSystems.newFileSystem (p);
        Path pp = fs.getPath ("/" + Constants.PROJECT_DB_FILE_NAME_PREFIX + Constants.H2_DB_FILE_SUFFIX);

        if (Files.notExists (pp))
        {

            return false;

        }

        return true;

    }

    public static int clamp (int v,
                             int min,
                             int max)
    {

        return Math.min (Math.max (v, min), max);

    }

    public static String replaceString (String  text,
					                    String  str,
					                    String  replace)
	                            throws  NullPointerException
    {

    	if (text == null)
    	{

    	    throw new NullPointerException ("text parm (arg 1).");

    	}

    	if (str == null)
    	{

    	    throw new NullPointerException ("str parm (arg 2).");

    	}

    	if (replace == null)
    	{

    	    throw new NullPointerException ("replace parm (arg3).");

    	}

    	StringBuilder buf = new StringBuilder (text);

    	int index = buf.indexOf (str);

    	while (index != -1)
    	{

    	    buf.replace (index,
    			 index + str.length (),
    			 replace);

    	    index = buf.indexOf (str,
    				 index + replace.length ());

    	}

    	return buf.toString ();

    }

    public static void streamTo (InputStream  in,
				 OutputStream out,
				 int          bufSize)
				 throws       IOException
    {

    	byte buf[] = new byte[bufSize];

    	int bRead = -1;

    	while ((bRead = in.read (buf)) != -1)
    	{

    	    out.write (buf,
    		           0,
    		           bRead);

    	}

    }

    public static String getExceptionTraceAsString (Throwable e)
    {

        StringWriter sw = new StringWriter ();
        PrintWriter pw = new PrintWriter (sw);
        e.printStackTrace (pw);
        pw.flush ();
        pw.close ();
        return sw.toString ();

    }

    public static String encodeParms (Map<String, String> parms)
    {

        StringBuilder b = new StringBuilder ();

        for (String k : parms.keySet ())
        {

            if (b.length () > 0)
            {

                b.append ("&");

            }

            b.append (k);
            b.append ("=");
            b.append (parms.get (k));

        }

        b.insert (0,
                  "?");

        return b.toString ();

    }

    /**
     * Determines if LocalTime.now is between the from and to.
     * If to > from then now is between if now.isAfter (from) && now.isBefore (to)
     * If from > to then now is between if now.isAfter (from) || now.isBefore (to)
        from 20:00, to 10:00, now 21:00 = true
        from 20:00, to 10:00, now 11:00 = false
        from 20:00, to 10:00, now 9:00 = true
    */
    public static boolean isNowBetween (LocalTime from,
                                        LocalTime to)
    {

        LocalTime now = LocalTime.now ();

        if (from.isBefore (to))
        {

            return (now.isAfter (from) && (now.isBefore (to)));

        } else {

            return (now.isAfter (from) || (now.isBefore (to)));

        }

    }

    public static long getMillisFromNowToNearestNextTime (LocalTime from,
                                                          LocalTime to)
    {

        LocalDateTime now = LocalDateTime.now ();
        now = now.with (ChronoField.MILLI_OF_SECOND, 0);

        if (from.isBefore (to))
        {

            if (now.toLocalTime ().isAfter (to))
            {

                // Get the next day with the from time.
                LocalDateTime f = LocalDateTime.now ();
                f = f.with (from);
                f = f.plusDays (1);
                return Math.abs (ChronoUnit.MILLIS.between (now, f));

            }

            if (now.toLocalTime ().isAfter (from))
            {

                // Get to with current date.
                LocalDateTime t = LocalDateTime.now ();
                t = t.with (to);
                return Math.abs (ChronoUnit.MILLIS.between (now, t));

            }

            // Get the from time with current date.
            LocalDateTime f = LocalDateTime.now ();
            f = f.with (from);
            return Math.abs (ChronoUnit.MILLIS.between (now, f));

        }

        // From is after the to.
        if (now.toLocalTime ().isAfter (from))
        {

            // Need to on next day.
            LocalDateTime t = LocalDateTime.now ();
            t = t.with (to);
            t = t.plusDays (1);
            return Math.abs (ChronoUnit.MILLIS.between (now, t));

        }

        if (now.toLocalTime ().isAfter (to))
        {

            // Need from on current date.
            LocalDateTime f = LocalDateTime.now ();
            f = f.with (from);
            return Math.abs (ChronoUnit.MILLIS.between (now, f));

        }

        if (now.toLocalTime ().isBefore (to))
        {

            // Need to on current date.
            LocalDateTime t = LocalDateTime.now ();
            t = t.with (to);
            return Math.abs (ChronoUnit.MILLIS.between (now, t));

        }

        throw new IllegalStateException ("Unable to find the least.");

    }

}
