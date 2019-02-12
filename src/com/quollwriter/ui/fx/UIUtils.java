package com.quollwriter.ui.fx;

import java.net.*;
import java.util.*;

import java.awt.Desktop;

import javafx.application.*;
import javafx.beans.property.*;
import javafx.beans.binding.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.*;
import javafx.scene.text.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.scene.paint.*;

import com.quollwriter.*;
import com.quollwriter.data.ObjectReference;
import com.quollwriter.data.Project;
import com.quollwriter.data.Chapter;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.editors.*;
import com.quollwriter.editors.messages.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUIString;
import static com.quollwriter.LanguageStrings.*;

public class UIUtils
{

    public static Color hexToColor (String h)
    {

        return Color.web (h);

    }

    public static String colorToHex (Color c)
    {

        return String.format( "#%02X%02X%02X",
                    (int)( c.getRed () * 255 ),
                    (int)( c.getGreen () * 255 ),
                    (int)( c.getBlue () * 255 ) );

    }

    public static int getA4PageCountForChapter (Chapter chapter,
                                                String  text)
    {

        // TODO
        return 0;

    }

    public static URL getDefaultStyleSheetURL ()
    {

        // TODO Make this a property.
        return UIUtils.class.getResource ("/data/default-style.css");

    }

    /**
     * Creates a wrapper around the passed in Runnable to ensure that it always runs on the event thread.
     *
     * @param r The runnable to run on the event thread.
     * @return The runnable wrapper.
     */
    public static Runnable createRunLater (Runnable r)
    {

        Runnable _r = new Runnable ()
        {

            @Override
            public void run ()
            {

                UIUtils.runLater (r);

            }

        };

        return _r;

    }

    /**
     * Run the passed in Runnable on the event thread.
     *
     * @param r The runnable to run.
     */
    public static void runLater (Runnable r)
    {

        Platform.runLater (() ->
        {

            try
            {

                r.run ();

            } catch (Exception e) {

                Environment.logError ("Unable to run: " + r,
                                      e);

            }

        });

    }

    /**
     * Set a bound tooltip on the control using the uistring ids.
     *
     * @param control The control to set the tooltip on.
     * @param ids The property to use as the source of the tooltip text.
     * @return The tooltip, it will be set on the control.
     */
    public static Tooltip setTooltip (Control        control,
                                      StringProperty prop)
    {

        Tooltip t = new Tooltip ();
        t.textProperty ().bind (prop);

        control.setTooltip (t);

        return t;

    }

    /**
     * Create a button with bound text for the uistring ids.
     *
     * @param ids
     * @return The button.
     */
    public static Button createButton (String... ids)
    {

        Button b = new Button ();
        b.textProperty ().bind (Bindings.createStringBinding (() -> getUIString (ids), Environment.uilangProperty ()));
        return b;

    }

    public static void openURL (AbstractViewer viewer,
                                String         url)
    {

        URL u = null;

        try
        {

            u = new URL (url);

        } catch (Exception e)
        {

            Environment.logError ("Unable to browse to: " +
                                  url,
                                  e);

            ComponentUtils.showErrorMessage (viewer,
                                             getUILanguageStringProperty (Arrays.asList (general,unabletoopenwebpage),
                                                                          url));
                                      //"Unable to open web page: " + url);

            return;

        }

    }

    public static void openURL (AbstractViewer viewer,
                                URL            url)
                         throws GeneralException
    {

        if (url == null)
        {

            return;

        }

        if (url.getProtocol ().equals (Constants.QUOLLWRITER_PROTOCOL))
        {

            String u = Environment.getQuollWriterWebsite ();

            String p = url.getPath ();

            if ((!p.endsWith (".html"))
                &&
                // Only add if the url isn't of the form [name].html?parms
                (p.indexOf (".html?") < 1)
                &&
                // Only add if the url isn't of the form [name].html#id
                (p.indexOf (".html#") < 1)
               )
            {

                p += ".html";

            }

            u = u + "/" + p;

            if (url.getQuery () != null)
            {

                u += "?" + url.getQuery ();

            }

            if (url.getRef () != null)
            {

                u += "#" + url.getRef ();

            }

            try
            {

                url = new URL (u);

            } catch (Exception e)
            {

                Environment.logError ("Unable to open url: " +
                                      u,
                                      e);

                return;

            }

        }

        if (url.getProtocol ().equals (Constants.HELP_PROTOCOL))
        {

            // Prefix it with the website.
            String u = Environment.getQuollWriterWebsite ();

            String p = url.getPath ();

            if (p.indexOf (".html") < 0)
            {

                p += ".html";

            }

            u = u + "/user-guide/" + url.getHost () + p;

            if (url.getRef () != null)
            {

                u += "#" + url.getRef ();

            }

            try
            {

                url = new URL (u);

            } catch (Exception e)
            {

                Environment.logError ("Unable to open url: " +
                                      u,
                                      e);

                return;

            }

            if (viewer != null)
            {

                Environment.fireUserProjectEvent (new ProjectEvent (viewer,
                                                                     ProjectEvent.Type.help,
                                                                     ProjectEvent.Action.show));

            }

        }

        if (url.getProtocol ().equals (Constants.OPENPROJECT_PROTOCOL))
        {

            String projId = url.getPath ();

            Project proj = null;

            try
            {

                Environment.openProject (projId,
                                         null,
                                         null);

            } catch (Exception e) {

                Environment.logError ("Unable to get project for id: " + projId,
                                      e);

            }

            return;

        }

        if (url.getProtocol ().equals (Constants.OPENEDITORMESSAGE_PROTOCOL))
        {

            int key = 0;

            try
            {

                key = Integer.parseInt (url.getPath ());

            } catch (Exception e) {

                // Ignore?

            }

            // Get the message.
            EditorMessage mess = null;

            try
            {

                mess = EditorsEnvironment.getMessageByKey (key);

            } catch (Exception e) {

                Environment.logError ("Unable to get message for key: " + key,
                                      e);

            }

            if (mess != null)
            {

                // Need to work out what to do.
                //EditorsEnvironment.openEditorMessage (mess);

            }

            return;

        }

        if (url.getProtocol ().equals (Constants.OBJECTREF_PROTOCOL))
        {

            if (viewer != null)
            {

                if (viewer instanceof AbstractProjectViewer)
                {

                    AbstractProjectViewer pv = (AbstractProjectViewer) viewer;

                    pv.viewObject (pv.getProject ().getObjectForReference (ObjectReference.parseObjectReference (url.getHost ())));

                    return;

                }

            }

        }


        if (url.getProtocol ().equals (Constants.ACTION_PROTOCOL))
        {

            String action = url.getPath ();

            if (viewer != null)
            {

                viewer.handleHTMLPanelAction (action);

                return;

            }

        }

        if (url.getProtocol ().equals ("mailto"))
        {

            return;

        }

        try
        {

            Desktop.getDesktop ().browse (url.toURI ());

        } catch (Exception e)
        {

            Environment.logError ("Unable to browse to: " +
                                  url,
                                  e);

            ComponentUtils.showErrorMessage (viewer,
                                             getUILanguageStringProperty (Arrays.asList (general,unabletoopenwebpage),
                                                                          url));
                                      //"Unable to open web page: " + url);

        }

    }

    public static AbstractViewer getViewer (Node parent)
    {

        if (parent == null)
        {

            return null;

        }

        Node p = parent.getParent ();

        if (p instanceof AbstractViewer)
        {

            return (AbstractViewer) p;

        }

        return UIUtils.getViewer (p);

    }

    public static String getQuollWriterHelpLink (String url,
                                                 String linkText)
    {

        if (linkText == null)
        {

            return String.format ("%s:%s",
                                  Constants.HELP_PROTOCOL,
                                  url);

        }

        return String.format ("<a href='%s:%s'>%s</a>",
                              Constants.HELP_PROTOCOL,
                              url,
                              linkText);

    }

}
