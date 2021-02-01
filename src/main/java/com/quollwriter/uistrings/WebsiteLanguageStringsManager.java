package com.quollwriter.uistrings;

import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.util.stream.*;

import javafx.beans.property.*;
import javafx.beans.binding.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class WebsiteLanguageStringsManager
{

    private static WebsiteLanguageStringsManager instance = null;

    private WebsiteLanguageStringsManager ()
    {

    }

    public static WebsiteLanguageStringsEditor editWebsiteLanguageStrings (WebsiteLanguageStrings userStrings)
    {

        WebsiteLanguageStringsEditor lse = WebsiteLanguageStringsManager.getWebsiteLanguageStringsEditor (userStrings);

        if (lse != null)
        {

            lse.toFront ();

            return lse;

        }

         try
         {

             WebsiteLanguageStringsEditor _ls = new WebsiteLanguageStringsEditor (userStrings);
             _ls.createViewer ();
             _ls.init (null);

             return _ls;

         } catch (Exception e) {

             Environment.logError ("Unable to create website language strings editor",
                                   e);

             ComponentUtils.showErrorMessage (Environment.getFocusedViewer (),
                                              getUILanguageStringProperty (uilanguage,edit,actionerror));

            return null;

         }

    }

    public static WebsiteLanguageStringsEditor getWebsiteLanguageStringsEditor (WebsiteLanguageStrings ls)
    {

        for (AbstractViewer v : Environment.getOpenViewers ())
        {
            /*
            TODO

            if (v instanceof WebsiteLanguageStringsEditor)
            {
                WebsiteLanguageStringsEditor lse = (WebsiteLanguageStringsEditor) v;

                if (lse.getUserLanguageStrings ().equals (ls))
                {

                    return lse;

                }

            }
*/
        }

        return null;

    }

    public static WebsiteLanguageStringsManager getInstance ()
                                                      throws Exception
    {

        if (WebsiteLanguageStringsManager.instance == null)
        {

            WebsiteLanguageStringsManager.instance = new WebsiteLanguageStringsManager ();
            WebsiteLanguageStringsManager.instance.init ();

        }

        return WebsiteLanguageStringsManager.instance;

    }

    public void init ()
               throws Exception
    {

    }

    public static void saveWebsiteLanguageStrings (WebsiteLanguageStrings ls)
                                            throws Exception
    {

        ls.setUser (true);

        Path f = WebsiteLanguageStringsManager.getWebsiteLanguageStringsFilePath (ls.getId ());

        Files.createDirectories (f.getParent ());

        String json = JSONEncoder.encode (ls.getAsJSON ());

        Writer out = Files.newBufferedWriter (f, StandardCharsets.UTF_8);

        char[] chars = json.toCharArray ();

        out.write (chars, 0, chars.length);
    	out.flush ();
    	out.close ();

    }

    public static WebsiteLanguageStrings getWebsiteLanguageStrings (String id)
                                                             throws Exception
    {

        Path f = WebsiteLanguageStringsManager.getWebsiteLanguageStringsFilePath (id);

        if (Files.exists (f))
        {

            WebsiteLanguageStrings ls = new WebsiteLanguageStrings (f.toFile ());

            return ls;

        }

        return null;

    }

    public static Path getWebsiteLanguageStringsDirPath ()
                                                  throws IOException
    {

        Path d = Environment.getUserPath (Constants.USER_WEBSITE_LANGUAGES_DIR_NAME);

        Files.createDirectories (d);

        return d;

    }

    public static Path getWebsiteLanguageStringsFilePath (String id)
                                                   throws IOException
    {

        return WebsiteLanguageStringsManager.getWebsiteLanguageStringsDirPath ().resolve (id);

    }

    // TODO Move to the UI language strings manager?
    public static Set<WebsiteLanguageStrings> getAllWebsiteLanguageStrings ()
                                                                     throws Exception
    {

        Set<WebsiteLanguageStrings> s = new TreeSet<> ();

        Path d = Environment.getUserPath (Constants.USER_WEBSITE_LANGUAGES_DIR_NAME);

        return Files.walk (d, 0)
        // Map the file to a WebsiteLanguageStrings instance.
        .map (f ->
        {

            try
            {

                WebsiteLanguageStrings ls = new WebsiteLanguageStrings (f.toFile ());

                if (ls.isEnglish ())
                {

                    ls = null;

                }

                return ls;

            } catch (Exception e) {

                throw new RuntimeException ("Unable to create website language strings from: " +
                                            f,
                                            e);

            }

        })
        // Remove nulls.
        .filter (f ->
        {

            return f != null;

        })
        // Collect to a set.
        .collect (Collectors.toSet ());
/*
TODO Remove
        File[] files = d.listFiles ();

        if (files != null)
        {

            for (int i = 0; i < files.length; i++)
            {

                File fd = files[i];

                WebsiteLanguageStrings ls = new WebsiteLanguageStrings (fd);

                if (ls.isEnglish ())
                {

                    continue;

                }

                s.add (ls);

            }

        }

        return s;
*/
    }

    public static WebsiteLanguageStrings getWebsiteLanguageStringsFromServer ()
                                                                       throws Exception
    {

        String url = UserProperties.get (Constants.QUOLL_WRITER_GET_WEBSITE_LANGUAGE_STRINGS_URL_PROPERTY_NAME);

         String data = Environment.getJSONFileAsString (url);

         return new WebsiteLanguageStrings (data);

    }

    public static void deleteWebsiteLanguageStrings (final WebsiteLanguageStrings ls)
    {

        if (!ls.isUser ())
        {

            throw new IllegalArgumentException ("Can only delete user website language strings.");

        }

        // TODO Change to use Runnable.
        java.awt.event.ActionListener remFile = new java.awt.event.ActionListener ()
        {

            @Override
            public void actionPerformed (java.awt.event.ActionEvent ev)
            {

                try
                {

                    Path f = WebsiteLanguageStringsManager.getWebsiteLanguageStringsFilePath (ls.getId ());

                    Files.deleteIfExists (f);

                } catch (Exception e) {

                    Environment.logError ("Unable to delete website language strings file: " + ls.getId (),
                                          e);

                    // TODO Handle for user.

                }

            }

        };

        for (AbstractViewer v : Environment.getOpenViewers ())
        {
/*
TODO
            if (v instanceof WebsiteLanguageStringsEditor)
            {

                WebsiteLanguageStringsEditor lse = (WebsiteLanguageStringsEditor) v;

                if ((lse.getUserLanguageStrings ().getId ().equals (ls.getId ()))
                    &&
                    (lse.getUserLanguageStrings ().getStringsVersion () == ls.getStringsVersion ())
                   )
                {

                    lse.close (false,
                               remFile);

                    return;

                }

            }
*/
        }

        remFile.actionPerformed (new java.awt.event.ActionEvent (ls, 0, "do"));

    }

    public static Path getEnglishWebsiteLanguageStringsPreviousFilePath ()
                                                                  throws Exception
    {

        return WebsiteLanguageStringsManager.getWebsiteLanguageStringsFilePath (WebsiteLanguageStrings.ENGLISH_ID + "-prev");

    }

    public static WebsiteLanguageStrings getPreviousEnglishWebsiteLanguageStrings ()
                                                                            throws Exception
    {

        Path f = WebsiteLanguageStringsManager.getEnglishWebsiteLanguageStringsPreviousFilePath ();

        if (Files.exists (f))
        {

            WebsiteLanguageStrings ls = new WebsiteLanguageStrings (f.toFile ());

            return ls;

        }

        return null;

    }

    public static void moveWebsiteLanguageStringsToPrevious ()
                                                      throws Exception
    {

        Path p = WebsiteLanguageStringsManager.getWebsiteLanguageStringsFilePath (WebsiteLanguageStrings.ENGLISH_ID);

        Path np = WebsiteLanguageStringsManager.getEnglishWebsiteLanguageStringsPreviousFilePath ();

        Files.move (p, np,
                    StandardCopyOption.REPLACE_EXISTING);

    }

}
