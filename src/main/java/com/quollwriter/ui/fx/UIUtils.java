package com.quollwriter.ui.fx;

import java.net.*;
import java.util.*;
import java.util.function.*;
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
import javafx.event.*;
import javafx.util.*;
import javafx.collections.*;

import javax.imageio.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.db.*;
import com.quollwriter.data.Prompt;
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

    public static final String PROJECT_INFO_STATUS_TAG = "{s}";
	public static final String PROJECT_INFO_WORDS_TAG = "{wc}";
	public static final String PROJECT_INFO_CHAPTERS_TAG = "{ch}";
	public static final String PROJECT_INFO_LAST_EDITED_TAG = "{le}";
	public static final String PROJECT_INFO_EDIT_COMPLETE_TAG = "{ec}";
	public static final String PROJECT_INFO_READABILITY_TAG = "{r}";
	public static final String PROJECT_INFO_EDITOR_TAG = "{ed}";

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

        if (r == null)
        {

            return;

        }

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

    public static HBox createButtonBar (Set<Button> buttons)
    {

        // TODO Make this configurable, esp for other OSes
        ButtonBar bar = new ButtonBar ("OC");
        //"OC+");
        bar.getButtons ().addAll (buttons);

        HBox h = new HBox ();
        h.getStyleClass ().add (StyleClassNames.BUTTONS);
        h.getChildren ().add (bar);
        return h;

    }

    public static void askForPasswordForProject (final ProjectInfo            proj,
                                                       ValueValidator<String> validator,
                                                 final Consumer<String>       onProvided,
                                                 final Runnable               onCancel,
                                                 final AbstractViewer         parentViewer)
    {

        AbstractProjectViewer pv = Environment.getProjectViewer (proj);

        if ((pv == null)
            &&
            (proj != null)
            &&
            (proj.isEncrypted ())
           )
        {

            if (validator == null)
            {

                validator = new ValueValidator<String> ()
                {

                    public StringProperty isValid (String v)
                    {

                        java.util.List<String> prefix = Arrays.asList (project,actions,openproject,enterpasswordpopup,errors);

                        if ((v == null)
                            ||
                            (v.trim ().equals (""))
                           )
                        {

                            return getUILanguageStringProperty (Utils.newList (prefix,novalue));
                            //"Please enter the password.";

                        }

                        ObjectManager om = null;

                        try
                        {

                            om = Environment.getProjectObjectManager (proj,
                                                                      v);

                        } catch (Exception e) {

                            if (ObjectManager.isDatabaseAlreadyInUseException (e))
                            {

                                return getUILanguageStringProperty (Utils.newList (prefix,projectalreadyopen));
                                //"Sorry, the {project} appears to already be open in Quoll Writer.  Please close all other instances of Quoll Writer first before trying to open the {project}.";

                            }

                            if (ObjectManager.isEncryptionException (e))
                            {

                                return getUILanguageStringProperty (Utils.newList (prefix,invalidpassword));
                                //"Password is not valid.";

                            }

                            Environment.logError ("Cant open project: " +
                                                  proj,
                                                  e);

                            ComponentUtils.showErrorMessage (parentViewer,
                                                             getUILanguageStringProperty (Utils.newList (prefix,general)));
                                                      //"Sorry, the {project} can't be opened.  Please contact Quoll Writer support for assistance.");

                            return null;

                        } finally {

                            if (om != null)
                            {

                                om.closeConnectionPool ();

                            }

                        }

                        return null;

                    }

                };

            }

            java.util.List<String> prefix = Arrays.asList (project,actions,openproject,enterpasswordpopup);

            ComponentUtils.createPasswordEntryPopup (getUILanguageStringProperty (Utils.newList (prefix,title)),
                                                     StyleClassNames.PROJECT,
                                                     getUILanguageStringProperty (Utils.newList (prefix,text),
                                                                                  //"{Project} <b>%s</b> is encrypted, please enter the password to unlock it below.",
                                                                                  proj.getName ()),
                                                     null,
                                                     validator,
                                                     getUILanguageStringProperty (Utils.newList (prefix,buttons,open)),
                                                     //"Open",
                                                     getUILanguageStringProperty (Utils.newList (prefix,buttons,cancel)),
                                                     //Constants.CANCEL_BUTTON_LABEL_ID,
                                                     onProvided,
                                                     onCancel,
                                                     null,
                                                     parentViewer);

        } else {

            UIUtils.runLater (() ->
            {

                // No password for the project.
                onProvided.accept (null);

            });

        }

    }

    public static String getFormattedProjectInfo (ProjectInfo project)
    {

        return UIUtils.getFormattedProjectInfo (project,
                                                null);

    }

    public static String getFormattedProjectInfo (ProjectInfo project,
                                                  String      format)
    {

        List<String> prefix = Arrays.asList (allprojects,LanguageStrings.project,view,labels);

        String lastEd = "";

        if (project.getLastEdited () != null)
        {

            lastEd = String.format (getUIString (Utils.newList (prefix, LanguageStrings.lastedited)),
                                    //"Last edited: %s",
                                    Environment.formatDate (project.getLastEdited ()));

        } else {

            lastEd = getUIString (Utils.newList (prefix, LanguageStrings.notedited));
                                            //"Not yet edited.";

        }

        String text = (format != null ? format : UserProperties.getProjectInfoFormat ());

        String nl = String.valueOf ('\n');

        while (text.endsWith (nl))
        {

            text = text.substring (0,
                                   text.length () - 1);

        }

        text = text.toLowerCase ();

        text = StringUtils.replaceString (text,
                                          " ",
                                          "&nbsp;");
        text = StringUtils.replaceString (text,
                                          nl,
                                          "<br />");

        text = StringUtils.replaceString (text,
                                          PROJECT_INFO_STATUS_TAG,
                                          (project.getStatus () != null ? project.getStatus () : getUIString (LanguageStrings.project,status,novalue)));
                                          //"No status"));

        text = StringUtils.replaceString (text,
                                          PROJECT_INFO_WORDS_TAG,
                                          String.format (getUIString (prefix, LanguageStrings.words),
                                                        //"%s words",
                                                         Environment.formatNumber (project.getWordCount ())));

        text = StringUtils.replaceString (text,
                                          PROJECT_INFO_CHAPTERS_TAG,
                                          String.format (getUIString (prefix, LanguageStrings.chapters),
                                            //"%s ${objectnames.%s.chapter}",
                                                         Environment.formatNumber (project.getChapterCount ())));

        text = StringUtils.replaceString (text,
                                          PROJECT_INFO_LAST_EDITED_TAG,
                                          lastEd);
        text = StringUtils.replaceString (text,
                                          PROJECT_INFO_EDIT_COMPLETE_TAG,
                                          String.format (getUIString (prefix, LanguageStrings.editcomplete),
                                                        //"%s%% complete",
                                                         Environment.formatNumber (Utils.getPercent (project.getEditedWordCount (), project.getWordCount ()))));
        text = StringUtils.replaceString (text,
                                          PROJECT_INFO_READABILITY_TAG,
                                          String.format (getUIString (prefix, LanguageStrings.readability),
                                                        //"GL: %s, RE: %s, GF: %s",
                                                         Environment.formatNumber (Math.round (project.getFleschKincaidGradeLevel ())),
                                                         Environment.formatNumber (Math.round (project.getFleschReadingEase ())),
                                                         Environment.formatNumber (Math.round (project.getGunningFogIndex ()))));

        return text;

    }

    public static void showDeleteProjectPopup (ProjectInfo    proj,
                                               Runnable       onDelete,
                                               AbstractViewer viewer)
    {

        StringProperty warning = getUILanguageStringProperty (Arrays.asList (LanguageStrings.project,actions,deleteproject,LanguageStrings.warning,normal),
                                                              (proj.isEditorProject () ? getUILanguageStringProperty (Arrays.asList (LanguageStrings.project,actions,deleteproject,LanguageStrings.warning,editor,
                                                                                                                                     proj.getForEditor ().getShortName ()))
                                                                                            : ""));

        UIUtils.showDeleteObjectPopup (getUILanguageStringProperty (project,actions,deleteproject,deletetype),
                                       proj.nameProperty (),
                                       StyleClassNames.PROJECT,
                                       warning,
                                       ev ->
                                       {

                                           if (proj.isEditorProject ())
                                           {

                                               EditorsEnvironment.sendProjectEditStopMessage (proj,
                                                // TODO Change.
                                                                                              new java.awt.event.ActionListener ()
                                               {

                                                   @Override
                                                   public void actionPerformed (java.awt.event.ActionEvent ev)
                                                   {

                                                       UIUtils.runLater (() ->
                                                       {

                                                           Environment.deleteProject (proj,
                                                                                      onDelete);

                                                           ComponentUtils.showMessage (viewer,
                                                                                       getUILanguageStringProperty (project,actions,deleteproject,editorproject,confirmpopup,title),
                                                                                       getUILanguageStringProperty (Arrays.asList (project,actions,deleteproject,editorproject,confirmpopup,text),
                                                                                                                    proj.getForEditor ().getShortName ()));

                                                       });

                                                   }

                                               });

                                           } else {

                                               Environment.deleteProject (proj,
                                                                          onDelete);

                                           }

                                       },
                                       null,
                                       viewer);

    }

    public static void showDeleteObjectPopup (StringProperty               deleteType,
                                              StringProperty               objName,
                                              String                       style,
                                              StringProperty               extraMessage,
                                              EventHandler<Form.FormEvent> onConfirm,
                                              EventHandler<Form.FormEvent> onCancel,
                                              AbstractViewer               viewer)
    {

        StringProperty message = getUILanguageStringProperty (Arrays.asList (deleteitem,text),
                                                              deleteType,
                                                              objName,
                                                              extraMessage != null ? extraMessage : new SimpleStringProperty (""));

        ComponentUtils.createYesConfirmPopup (getUILanguageStringProperty (Arrays.asList (deleteitem,title),
                                                                           deleteType),
                                              style,
                                              message,
                                              null,
                                              getUILanguageStringProperty (deleteitem,confirm),
                                              getUILanguageStringProperty (deleteitem,cancel),
                                              onConfirm,
                                              onCancel,
                                              null,
                                              viewer);

    }

    public static StringProperty formatPrompt (Prompt p)
    {

        if (p == null)
        {

            return getUILanguageStringProperty (Arrays.asList (warmups,prompt,view,unavailable));
            //"Prompt no longer available.  Usually this is due to it's removal at the request of the author.";

        }

        String link = "";

        if (p.isUserPrompt ())
        {

            link = getUIString (warmups,prompt,view,ownprompt);
            //"by You";

        } else
        {

            link = String.format (getUIString (warmups,prompt,view, LanguageStrings.link),
                                //"<a title='Click to visit the website' href='%s'>%s by %s</a>",
                                                p.getURL (),
                                                p.getStoryName (),
                                                p.getAuthor ());

        }

        return new SimpleStringProperty (p.getText () + "<br /> - " + link);


    }

    public static ChoiceBox<StringProperty> getTimeOptions (Supplier<Integer> defValue,
                                                            Consumer<Integer> onSelected)
    {

        Map<String, StringProperty> vals = new LinkedHashMap<> ();

        StringProperty unlim = getUILanguageStringProperty (times,unlimited);
        vals.put (unlim.getValue (), unlim);

        StringProperty mins10 = getUILanguageStringProperty (times,LanguageStrings.mins10);
        vals.put (mins10.getValue (), mins10);

        StringProperty mins20 = getUILanguageStringProperty (times,LanguageStrings.mins20);
        vals.put (mins20.getValue (), mins20);

        StringProperty mins30 = getUILanguageStringProperty (times,LanguageStrings.mins30);
        vals.put (mins30.getValue (), mins30);

        StringProperty hour1 = getUILanguageStringProperty (times,LanguageStrings.hour1);
        vals.put (hour1.getValue (), hour1);

        ChoiceBox<StringProperty> box = new ChoiceBox<> (FXCollections.observableList (new ArrayList<> (vals.values ())));

        box.setConverter (new StringConverter<StringProperty> ()
        {

            @Override
            public StringProperty fromString (String s)
            {

                return vals.get (s);

            }

            @Override
            public String toString (StringProperty s)
            {

                return s.getValue ();

            }

        });

        int minsC = Constants.DEFAULT_MINS;

        if (defValue != null)
        {

            minsC = defValue.get ();

        }

        StringProperty sel = unlim;

        if (minsC == 60)
        {

            sel = hour1;

        }

        if (minsC == 30)
        {

            sel = mins30;

        }

        if (minsC == 20)
        {

            sel = mins20;

        }

        if (minsC == 10)
        {

            sel = mins10;

        }

        box.setValue (sel);

        box.setOnAction (ev ->
        {

            int val = 0;

            StringProperty v = box.getValue ();

            if (v == mins10)
            {

                val = 10;

            }

            if (v == mins20)
            {

                val = 20;

            }

            if (v == mins30)
            {

                val = 30;

            }

            if (v == hour1)
            {

                val = 60;

            }

            if (onSelected != null)
            {

                onSelected.accept (val);

            }

        });

        return box;

    }

    public static ChoiceBox getWordsOptions (Supplier<Integer> defValue,
                                             Consumer<Integer> onSelected)
    {

        Map<String, StringProperty> vals = new LinkedHashMap<> ();

        StringProperty unlim = getUILanguageStringProperty (words,unlimited);
        vals.put (unlim.getValue (), unlim);

        StringProperty words100 = getUILanguageStringProperty (words,LanguageStrings.words100);
        vals.put (words100.getValue (), words100);

        StringProperty words250 = getUILanguageStringProperty (words,LanguageStrings.words250);
        vals.put (words250.getValue (), words250);

        StringProperty words500 = getUILanguageStringProperty (words,LanguageStrings.words500);
        vals.put (words500.getValue (), words500);

        StringProperty words1000 = getUILanguageStringProperty (words,LanguageStrings.words1000);
        vals.put (words1000.getValue (), words1000);

        ChoiceBox<StringProperty> box = new ChoiceBox<> (FXCollections.observableList (new ArrayList<> (vals.values ())));

        box.setConverter (new StringConverter<StringProperty> ()
        {

            @Override
            public StringProperty fromString (String s)
            {

                return vals.get (s);

            }

            @Override
            public String toString (StringProperty s)
            {

                return s.getValue ();

            }

        });

        int wordsC = 0;

        if (defValue != null)
        {

            wordsC = defValue.get ();

        }

        StringProperty sel = unlim;

        if (wordsC == 100)
        {

            sel = words100;

        }

        if (wordsC == 250)
        {

            sel = words250;

        }

        if (wordsC == 500)
        {

            sel = words500;

        }

        if (wordsC == 1000)
        {

            sel = words1000;

        }

        box.setValue (sel);

        box.setOnAction (ev ->
        {

            int val = 0;

            StringProperty v = box.getValue ();

            if (v == words100)
            {

                val = 100;

            }

            if (v == words250)
            {

                val = 250;

            }

            if (v == words500)
            {

                val = 500;

            }

            if (v == words1000)
            {

                val = 1000;

            }

            if (onSelected != null)
            {

                onSelected.accept (val);

            }

        });

        return box;

    }

}
