package com.quollwriter.ui.fx;

import java.net.*;
import java.util.*;
import java.util.stream.*;
import java.io.*;
import java.nio.file.*;

import java.awt.image.*;
import java.awt.Desktop;

import javafx.application.*;
import javafx.beans.property.*;
import javafx.beans.binding.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.*;
import javafx.scene.text.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.geometry.*;
import javafx.scene.paint.*;
import javafx.embed.swing.*;

import javax.imageio.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.data.ObjectReference;
import com.quollwriter.data.Project;
import com.quollwriter.data.ProjectInfo;
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

        if (h == null)
        {

            return null;

        }

        try
        {

            return Color.web (h);

        } catch (Exception e) {

            return null;

        }

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

    public static TextFlow createTextFlowForHtml (StringProperty text)
    {

        return BasicHtmlTextFlow.builder ()
            .text (text)
            .build ();

    }

    /**
     * Set a bound tooltip on the control using the uistring ids.
     *
     * @param control The control to set the tooltip on.
     * @param ids The property to use as the source of the tooltip text.
     * @return The tooltip, it will be set on the control.
     */
    public static Tooltip setTooltip (Node           node,
                                      StringProperty prop)
    {

        Tooltip t = new Tooltip ();

        t.setContentDisplay (ContentDisplay.GRAPHIC_ONLY);
        TextFlow tf = UIUtils.createTextFlowForHtml (prop);
        t.setGraphic (tf);

        t.setPrefHeight (tf.prefHeight (10000));

        // We set the height of the tooltip when the width changes and recalc the height of the text flow otherwise
        // the tooltip height is too large.
        t.widthProperty ().addListener ((v, oldv, newv) ->
        {
            t.setPrefHeight (tf.prefHeight (newv.doubleValue ()));
        });

        Tooltip.install (node,
                         t);

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

            UIUtils.openURL (viewer,
                             u);

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

        if (parent instanceof AbstractViewer)
        {

            return (AbstractViewer) parent;

        }

        return UIUtils.getViewer (parent.getParent ());

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

    public static void showFile (AbstractViewer parent,
                                 Path           f)
    {

        try
        {

            Desktop.getDesktop ().open (f.toFile ());

        } catch (Exception e)
        {

            Environment.logError ("Unable to open: " +
                                  f,
                                  e);

            ComponentUtils.showErrorMessage (parent,
                                             getUILanguageStringProperty (Arrays.asList (general,unabletoopenfile),
                                                                          f.toString ()));
                                      //"Unable to open: " + f);

            return;

        }

    }

    public static void showManageBackups (final ProjectInfo    proj,
                                          final AbstractViewer viewer)
	{
/*
TODO
        String popupName = "managebackups" + proj.getId ();
        QPopup popup = viewer.getNamedPopup (popupName);

        if (popup == null)
        {

            popup = UIUtils.createClosablePopup (getUIString (backups,show, LanguageStrings.popup,title),
                                                 //"Current Backups",
                                                 Environment.getIcon (Constants.SNAPSHOT_ICON_NAME,
                                                                      Constants.ICON_POPUP),
                                                 null);

            BackupsManager bm = null;

            try
            {

                bm = new BackupsManager (viewer,
                                         proj);
                bm.init ();

            } catch (Exception e) {

                Environment.logError ("Unable to show backups manager",
                                      e);

                UIUtils.showErrorMessage (viewer,
                                          getUIString (backups,show,actionerror));
                                          //"Unable to show backups manager, please contact Quoll Writer support for assistance.");

                return;

            }

            bm.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                  bm.getPreferredSize ().height));
            bm.setBorder (UIUtils.createPadding (10, 10, 10, 10));

            popup.setRemoveOnClose (false);
            popup.setContent (bm);

            popup.setDraggable (viewer);

            popup.resize ();

            viewer.showPopupAt (popup,
                                UIUtils.getCenterShowPosition (viewer,
                                                               popup),
                                false);

            viewer.addNamedPopup (popupName,
                                  popup);

        } else {

            popup.setVisible (true);
            popup.toFront ();

        }
*/
        Environment.fireUserProjectEvent (viewer,
                                          ProjectEvent.Type.backups,
                                          ProjectEvent.Action.show);

	}

    public static void showCreateBackup (final Project        proj,
                                         final String         filePassword,
                                         final AbstractViewer viewer)
    {

        UIUtils.showCreateBackup (Environment.getProjectInfo (proj),
                                  filePassword,
                                  viewer);

    }

    public static void showCreateBackup (final ProjectInfo    proj,
                                         final String         filePassword,
                                         final AbstractViewer viewer)
    {

        ComponentUtils.createQuestionPopup (getUILanguageStringProperty (backups,_new,popup,title),
                                            StyleClassNames.CREATEBACKUP,
                                            getUILanguageStringProperty (Arrays.asList (backups,_new,popup,text),
                                                        //"Please confirm you wish to create a backup of {project} <b>%s</b>.",
                                                                         proj.getName ()),
                                            getUILanguageStringProperty (backups,_new,popup,buttons,confirm),
                                            //"Yes, create it",
                                            getUILanguageStringProperty (backups,_new,popup,buttons,cancel),
                                            //null,
                                            fev ->
                                            {

                                                try
                                                {

                                                    java.io.File f = Environment.createBackupForProject (proj,
                                                                                                 false);
/*
TODO
                                                    Box b = new Box (BoxLayout.Y_AXIS);

                                                    JTextPane m = UIUtils.createHelpTextPane (String.format (getUIString (backups,_new,confirmpopup,text),
                                                                                                            //"A backup has been created and written to:\n\n  <a href='%s'>%s</a>",
                                                                                                             f.getParentFile ().toURI ().toString (),
                                                                                                             f),
                                                                                              viewer);

                                                    m.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                                                              m.getPreferredSize ().height));
                                                    m.setBorder (null);

                                                    b.add (m);

                                                    b.add (Box.createVerticalStrut (10));

                                                    JLabel l = UIUtils.createClickableLabel (getUIString (backups,_new,confirmpopup,labels,view),
                                                                                            //"Click to view the backups",
                                                                                             Environment.getIcon (Constants.SNAPSHOT_ICON_NAME,
                                                                                                                  Constants.ICON_MENU),
                                                                                             new ActionListener ()
                                                                                             {

                                                                                                @Override
                                                                                                public void actionPerformed (ActionEvent ev)
                                                                                                {

                                                                                                    UIUtils.showManageBackups (proj,
                                                                                                                               viewer);

                                                                                                }

                                                                                             });

                                                    b.add (l);
*/
                                                    ComponentUtils.showMessage (viewer,
                                                                                StyleClassNames.NOTIFICATION,
                                                                                getUILanguageStringProperty (backups,_new,confirmpopup,title),
                                                                                //"Backup created",
                                                                                null,
                                                                                null);

                                                } catch (Exception e)
                                                {

                                                    Environment.logError ("Unable to create backup of project: " +
                                                                          proj,
                                                                          e);

                                                    ComponentUtils.showErrorMessage (viewer,
                                                                                     getUILanguageStringProperty (backups,_new,actionerror));
                                                                              //"Unable to create backup.");

                                                }

                                            },
                                            viewer);

    }

    public static byte[] getImageBytes (WritableImage im)
                                 throws GeneralException
    {

        if (im == null)
        {

            return null;

        }

        try
        {

            BufferedImage bim = new BufferedImage ((int) im.getWidth (),
                                                   (int) im.getHeight (),
                                                   BufferedImage.TYPE_INT_ARGB_PRE);

            SwingFXUtils.fromFXImage (im,
                                      bim);

            ByteArrayOutputStream bout = new ByteArrayOutputStream ();

            // Shouldn't use png here, it is too slow.
            if (!ImageIO.write (bim,
                                "png",
                                bout))
            {

                throw new GeneralException ("Unable to write image using png");

            }

            bout.flush ();
            bout.close ();
            return bout.toByteArray ();

        } catch (Exception e) {

            throw new GeneralException ("Unable to get bytes for image",
                                        e);

        }

    }

    public static WritableImage getImageOfNode (Node n)
    {

        SnapshotParameters parms = new SnapshotParameters ();

        WritableImage im = new WritableImage ((int) n.boundsInParentProperty ().getValue ().getWidth (), (int) n.boundsInParentProperty ().getValue ().getHeight ());

        return n.snapshot (parms,
                           im);

    }

    public static void addDoOnReturnPressed (TextField f,
                                             Runnable  r)
    {

        f.addEventHandler (KeyEvent.KEY_PRESSED,
                           ev ->
        {

            if (ev.getCode () == KeyCode.ENTER)
            {

                r.run ();

            }

        });

    }

    public static void scrollIntoView (Node node,
                                       VPos pos)
    {

        Parent p = node.getParent ();

        while (p != null)
        {

            if (p instanceof ScrollPane)
            {

                break;

            }

            p = p.getParent ();

        }

        if (p == null)
        {

            return;

        }

        UIUtils.scrollIntoView ((ScrollPane) p,
                                node,
                                pos);

    }

    public static Bounds getBoundsInParent (Node parent,
                                            Node node)
    {

        Node p = node.getParent ();
        Bounds b = node.getBoundsInParent ();

        while (p != parent)
        {

            if (p == null)
            {

                return null;

            }

            b = p.localToParent (b);
            p = p.getParent ();

        }

        return b;

    }

    public static void scrollIntoView (ScrollPane pane,
                                       Node       node,
                                       VPos       pos)
    {

        ScrollPane scrollPane = pane;

        Bounds nb = UIUtils.getBoundsInParent (pane.getContent (),
                                               node);

        Bounds vb = scrollPane.getBoundsInLocal ();
        Bounds _nb = UIUtils.getBoundsInParent (pane,
                                                node);

        if (vb.contains (_nb))
        {

            return;

        }

        double heightViewPort = scrollPane.getViewportBounds().getHeight();
        double heightScrollPane = scrollPane.getContent().getBoundsInLocal().getHeight();

        // Node is below or partially below the viewport bounds.
        if (_nb.getMaxY () > vb.getMaxY ())
        {

            // Move it up to be visible.
            double diff = _nb.getMaxY () - vb.getMaxY ();
            double vh = 0;

            if (pos == VPos.TOP)
            {

                vh = heightViewPort;
                diff = vh - (_nb.getHeight () - diff);

            }

            if (pos == VPos.CENTER)
            {

                vh = heightViewPort / 2;
                diff = vh + ((_nb.getHeight () / 2) - (_nb.getHeight () - diff));

            }

            if (pos == VPos.BOTTOM)
            {

                //diff = 

            }

System.out.println ("D: " + diff);
            //diff = vh + diff;
            //diff -= vh;
System.out.println ("D2: " + diff + ", " + vh);
            scrollPane.setVvalue (scrollPane.getVvalue () + ((diff / (heightScrollPane - heightViewPort))));
            return;
        }

        if (_nb.getMinY () < vb.getMinY ())
        {

            // Move it up to be visible.
            double diff = _nb.getMinY () - vb.getMinY ();
            scrollPane.setVvalue (scrollPane.getVvalue () + (diff / (heightScrollPane - heightViewPort)));
            return;
        }

        if (pos == VPos.TOP)
        {

        }


        //scrollPane.setVvalue (scrollPane.getVvalue () + ((diff / (heightScrollPane - heightViewPort))));


        double y = UIUtils.getBoundsInParent (pane.getContent (),
                                              node).getMaxY ();

/*
        if (pos == VPos.TOP)
        {

            scrollPane.setVvalue ((y - heightViewPort) / (heightScrollPane-heightViewPort));
            System.out.println ("SV: " + scrollPane.getVvalue ());
            scrollPane.setVvalue (y / heightScrollPane);
            return;
        }
*/
        if (y<(heightViewPort/2)) {
            scrollPane.setVvalue(0);
            // below 0 of scrollpane

        }else if ((y>=(heightViewPort/2))&(y<=(heightScrollPane-heightViewPort/2))) {
           // between 0 and 1 of scrollpane
            scrollPane.setVvalue((y-(heightViewPort/2))/(heightScrollPane-heightViewPort));
        }
        else if(y>= (heightScrollPane-(heightViewPort/2))){
            // above 1 of scrollpane
            scrollPane.setVvalue(1);

        }
/*
        double contentHeight = pane.getContent ().localToScene (pane.getContent ().getBoundsInLocal ()).getHeight ();
        double nodeMinY = node.localToScene (node.getBoundsInLocal ()).getMinY ();
        double nodeMaxY = node.localToScene (node.getBoundsInLocal ()).getMaxY ();

        pane.setVvalue (nodeMaxY / contentHeight);
*/
    }

    public static void toggleSelected (Parent parent,
                                       Node   node)
    {

        if (node == null)
        {

            return;

        }

        boolean v = node.getPseudoClassStates ().contains (StyleClassNames.SELECTED_PSEUDO_CLASS);

        parent.getChildrenUnmodifiable ().stream ()
            // Switch off the selected class for all elements.
            .forEach (c -> c.pseudoClassStateChanged (StyleClassNames.SELECTED_PSEUDO_CLASS, false));

        node.pseudoClassStateChanged (StyleClassNames.SELECTED_PSEUDO_CLASS, !v);

    }

    public static void setSelected (Parent parent,
                                    Object userData)
    {

        parent.getChildrenUnmodifiable ().stream ()
            .forEach (c ->
            {

                c.pseudoClassStateChanged (StyleClassNames.SELECTED_PSEUDO_CLASS, false);

                if (c.getUserData ().equals (userData))
                {

                    c.pseudoClassStateChanged (StyleClassNames.SELECTED_PSEUDO_CLASS, true);

                }

            });

    }

    public static List<Node> getSelected (Parent parent)
    {

        return parent.getChildrenUnmodifiable ().stream ()
            .filter (c -> c.getPseudoClassStates ().contains (StyleClassNames.SELECTED_PSEUDO_CLASS))
            .collect (Collectors.toList ());

    }

}
