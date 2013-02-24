package com.quollwriter.importer;

import java.io.*;

import java.net.*;

import java.util.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.components.*;


public class Importer
{

    private static Map<String, Class>  handlers = new HashMap ();
    private static Map<String, String> initDocs = new HashMap ();

    static
    {

        Map m = Importer.handlers;

        m.put (Constants.DOC_FILE_EXTENSION,
               MSWordDocumentImporter.class);
        m.put (Constants.DOCX_FILE_EXTENSION,
               MSWordDocumentImporter.class);

        /*
        m.put (Constants.HTML_FILE_EXTENSION,
               HTMLDocumentImporter.class);
        m.put (Constants.HTM_FILE_EXTENSION,
               HTMLDocumentImporter.class);
         */
        m = Importer.initDocs;

        m.put (Constants.DOC_FILE_EXTENSION,
               Constants.DATA_DIR + "doc-import-init.doc");
        m.put (Constants.DOCX_FILE_EXTENSION,
               Constants.DATA_DIR + "docx-import-init.docx");

    }

    public static void init ()
    {

        // Create a new instance of each handler.
        Iterator<String> iter = Importer.handlers.keySet ().iterator ();

        while (iter.hasNext ())
        {

            String t = iter.next ();

            Class c = Importer.handlers.get (t);

            try
            {

                DocumentImporter di = (DocumentImporter) c.newInstance ();

                di.convert (Environment.getResourceStream (Importer.initDocs.get (t)),
                            t);

            } catch (Exception e)
            {

                Environment.logError ("Unable to create new instance of: " +
                                      c.getName (),
                                      e);

            }

        }

    }

    public static void importProject (final URI            u,
                                      final ImportCallback callback)
                               throws Exception
    {

        final URL url = u.toURL ();

        URLConnection uc = null;

        try
        {

            uc = url.openConnection ();

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to open file at: " +
                                        u,
                                        e);

        }

        final InputStream in = new BufferedInputStream (uc.getInputStream ());

        String f = url.getFile ();

        final String ext = f.substring (f.lastIndexOf (".")).toLowerCase ();

        Class c = Importer.handlers.get (ext);

        if (c == null)
        {

            throw new GeneralException ("Unable to find import handler for extension: " +
                                        ext);

        }

        try
        {

            final DocumentImporter di = (DocumentImporter) c.newInstance ();

            Thread t = new Thread (new Runner ()
                {

                    public void run ()
                    {

                        try
                        {

                            Project p = di.convert (in,
                                                    ext);

                            in.close ();

                            if (p.getName () == null)
                            {

                                // Use the file name.
                                String fn = url.getFile ().substring (0,
                                                                      url.getFile ().length () - Constants.DOCX_FILE_EXTENSION.length ());

                                p.setName (fn);
                                p.getBooks ().get (0).setName (fn);

                            }

                            callback.projectCreated (p,
                                                     u);

                        } catch (Exception e)
                        {

                            callback.exceptionOccurred (new GeneralException ("Unable to import project for uri: " +
                                                                              u +
                                                                              " using instance: " +
                                                                              di.getClass ().getName (),
                                                                              e),
                                                        u);

                        }

                    }

                });

            t.setDaemon (true);
            t.start ();

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to create setup importer instance: " +
                                        c.getName (),
                                        e);

        }

    }

}
