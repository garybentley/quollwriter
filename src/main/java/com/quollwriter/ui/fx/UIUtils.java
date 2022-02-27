package com.quollwriter.ui.fx;

import java.net.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import java.io.*;
import java.nio.file.*;

import java.awt.image.*;
//import java.awt.Desktop;

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
import javafx.scene.text.*;
import javafx.event.*;
import javafx.util.*;
import javafx.collections.*;
import javafx.css.*;

import javax.imageio.*;

import org.imgscalr.Scalr;

import com.quollwriter.*;
import com.quollwriter.db.*;
import com.quollwriter.text.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.data.ChapterCounts;
import com.quollwriter.data.ReadabilityIndices;
import com.quollwriter.data.Prompt;
import com.quollwriter.data.ObjectReference;
import com.quollwriter.data.Project;
import com.quollwriter.data.ProjectInfo;
import com.quollwriter.data.Chapter;
import com.quollwriter.data.UserConfigurableObject;
import com.quollwriter.data.IPropertyBinder;
import com.quollwriter.data.Tag;
import com.quollwriter.data.NamedObject;
import com.quollwriter.data.UserConfigurableObjectType;
import com.quollwriter.data.Note;
import com.quollwriter.data.BlankNamedObject;
import com.quollwriter.data.Book;
import com.quollwriter.data.ChapterItem;
import com.quollwriter.data.Asset;
import com.quollwriter.data.NamedObjectNameWrapper;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.sidebars.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.popups.*;
import com.quollwriter.editors.*;
import com.quollwriter.editors.messages.*;
import com.quollwriter.uistrings.UILanguageStrings;
import com.quollwriter.uistrings.UILanguageStringsManager;
import com.quollwriter.uistrings.WebsiteLanguageStrings;
import com.quollwriter.uistrings.WebsiteLanguageStringsManager;

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

    public static final FileChooser.ExtensionFilter imageFileFilter = new FileChooser.ExtensionFilter ("Image files: jpg, png, gif",
                                                                                               "*.jpg",
                                                                                               "*.jpeg",
                                                                                               "*.gif",
                                                                                               "*.png");

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
        return -1;

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
     * Force the runnable to run later on the fx thread.
     */
    public static void forceRunLater (Runnable r)
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

    /**
     * Run the passed in Runnable on the event thread, if already on the event thread then run immediately.
     *
     * @param r The runnable to run.
     */
    public static void runLater (Runnable r)
    {

        if (r == null)
        {

            return;

        }

        if (Platform.isFxApplicationThread ())
        {

            try
            {

                r.run ();

            } catch (Exception e) {

                Environment.logError ("Unable to run: " + r,
                                      e);

            }

            return;

        }

        UIUtils.forceRunLater (r);

    }

    public static TextFlow createTextFlowForHtml (StringProperty text)
    {

        return BasicHtmlTextFlow.builder ()
            .text (text)
            .build ();

    }

    public static Tooltip createTooltip (StringProperty text)
    {

        Tooltip t = new Tooltip ();

        t.setContentDisplay (ContentDisplay.GRAPHIC_ONLY);

        t.setOnShowing (ev ->
        {

            QuollTextView tf = QuollTextView.builder ()
                .text (text)
                .build ();
            t.setGraphic (tf);

        });

        t.minWidthProperty ().bind (t.prefWidthProperty ());

        return t;

    }

    public static Tooltip getTooltip (Node node)
    {

        Object o = node.getProperties ().get ("tooltip");

        if ((o != null)
            &&
            (o instanceof Tooltip)
           )
        {

            return (Tooltip) o;

        }

        if (node instanceof Control)
        {

            return ((Control) node).getTooltip ();

        }

        return null;

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

        if (prop == null)
        {

            Tooltip.uninstall (node,
                               (Tooltip) node.getProperties ().get ("tooltip"));
            return null;

        }

        Tooltip t = new Tooltip ();

        t.setContentDisplay (ContentDisplay.GRAPHIC_ONLY);

        t.setOnShowing (ev ->
        {

            QuollTextView tf = QuollTextView.builder ()
                .text (prop)
                .build ();
                /*
            tf.setPrefWidth (300);
            tf.setMinWidth (300);
            tf.setMaxWidth (300);
            */
            t.setGraphic (tf);

        });

        node.getProperties ().put ("tooltip", t);
        t.minWidthProperty ().bind (t.prefWidthProperty ());

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
                             u,
                             null);

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
                                String         url,
                                MouseEvent     ev)
    {

        if ("#".equals (url))
        {

            return;

        }

        URL u = null;

        try
        {

            u = new URL (url);

            UIUtils.openURL (viewer,
                             u,
                             ev);

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
                                URL            url,
                                MouseEvent     ev)
                         throws Exception
    {

        if ((viewer != null)
            &&
            (viewer instanceof AbstractProjectViewer)
           )
        {

            AbstractProjectViewer pv = (AbstractProjectViewer) viewer;

            if (url.getProtocol ().equals (Constants.OBJECTNAME_PROTOCOL))
            {

                String n = url.getHost ();

                try
                {

                    n = URLDecoder.decode (url.getHost (), "utf-8");

                } catch (Exception e) {

                    // Ignore.

                }

                Set<NamedObject> objs = pv.getProject ().getAllNamedObjectsByName (n);

                if ((objs == null)
                    ||
                    (objs.size () == 0)
                   )
                {

                    return;

                }

                if (objs.size () == 1)
                {

                    pv.viewObject (objs.iterator ().next ());

                    return;

                } else {
// TODO Check this.

                     Point2D p = viewer.screenToPopupLocal (ev.getScreenX (),
                                                            ev.getScreenY ());

                     ShowObjectSelectPopup.<NamedObject>builder ()
                         .withViewer (viewer)
                         .title (getUILanguageStringProperty (selectitem,popup,title))
                         .styleClassName (StyleClassNames.OBJECTSELECT)
                         .headerIconClassName (StyleClassNames.VIEW)
                         .popupId ("showobject")
                         .objects (FXCollections.observableList (new ArrayList<> (objs)))
                         .cellProvider ((obj, popupContent) ->
                         {

                            QuollLabel l = QuollLabel.builder ()
                                 .label (obj.nameProperty ())
                                 .styleClassName (obj.getObjectType ())
                                 .build ();

                            UIUtils.setTooltip (l,
                                                getUILanguageStringProperty (LanguageStrings.actions,clicktoview));

                             if (obj instanceof UserConfigurableObject)
                             {

                                 UserConfigurableObject uobj = (UserConfigurableObject) obj;

                                 ImageView iv = new ImageView ();
                                 iv.imageProperty ().bind (uobj.getUserConfigurableObjectType ().icon16x16Property ());

                                 l.setGraphic (iv);

                             }

                             l.setOnMouseClicked (eev ->
                             {

                                 if (eev.getButton () != MouseButton.PRIMARY)
                                 {

                                     return;

                                 }

                                 pv.viewObject (obj);

                                 popupContent.close ();

                             });

                             return l;

                         })
                         .build ()
                         .show (p.getX (),
                                p.getY ());

                }

                return;

            }

            if (url.getProtocol ().equals (Constants.OBJECTREF_PROTOCOL))
            {

                pv.viewObject (pv.getProject ().getObjectForReference (ObjectReference.parseObjectReference (url.getHost ())));

                return;

            }

        }

        UIUtils.openURL ((URLActionHandler) viewer,
                         url);

    }

    public static void openURL (URLActionHandler handler,
                                URL              url)
                         throws Exception
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

                throw new GeneralException ("Unable to open url: " +
                                            u,
                                            e);

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

                throw new GeneralException ("Unable to open url: " +
                                            u,
                                            e);

            }

            if (Environment.getFocusedViewer () != null)
            {

                Environment.fireUserProjectEvent (new ProjectEvent (Environment.getFocusedViewer (),
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

                throw new GeneralException ("Unable to get project for id: " + projId,
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

                throw new GeneralException ("Unable to get message for key: " + key,
                                            e);

            }

            if (mess != null)
            {

                // Need to work out what to do.
                // TODO?
                //EditorsEnvironment.openEditorMessage (mess);

            }

            return;

        }

        if (url.getProtocol ().equals (Constants.ACTION_PROTOCOL))
        {

            String action = url.getPath ();

            if (handler != null)
            {

                StringTokenizer t = new StringTokenizer (action,
                                                         ",;");

                while (t.hasMoreTokens ())
                {

                    handler.handleURLAction (t.nextToken ().trim (),
                                             null);

                }

                return;

            }

        }

        if (url.getProtocol ().equals ("mailto"))
        {

            return;

        }

        if (url.getProtocol ().equals ("file"))
        {

            Environment.openURL (url);

            //Desktop.getDesktop ().browse (url.toURI ());

            return;

        }

        try {
          //URI uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), url.getRef ());

          Environment.openURL (url);
          //Desktop.getDesktop ().browse (uri);

        } catch (Exception e) {

            // Handle?
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

    public static Button createHelpPageButton (AbstractViewer viewer,
                                               String         helpPage,
                                               StringProperty tooltip)
    {

        QuollButton b = QuollButton.builder ()
            .iconName (StyleClassNames.HELP)
            .tooltip (tooltip != null ? tooltip : getUILanguageStringProperty (help,button,LanguageStrings.tooltip))
            .onAction (ev ->
            {

                UIUtils.openURL (viewer,
                                 "help://" + helpPage);

            })
            .build ();

        return b;

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

        if ((f == null)
            ||
            (Files.notExists (f))
           )
        {

            return;

        }

        try
        {

            Desktop.open (f.toFile ());

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

    public static Image getImage (Path p)
                           throws IOException
    {

        if (p == null)
        {

            return null;

        }

        return new Image (Files.newInputStream (p));

    }

    public static Image getImage (byte[] bytes)
                           throws GeneralException
    {

        if (bytes == null)
        {

            return null;

        }

        try
        {

            return SwingFXUtils.toFXImage (ImageIO.read (new ByteArrayInputStream (bytes)),
                                           null);

        } catch (Exception e) {

            throw new GeneralException ("Unable to convert bytes to an image",
                                        e);

        }

    }

    public static byte[] getImageBytes (Image im)
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

    public static BufferedImage getScaledImage (BufferedImage img,
                                                int           targetWidth)
    {

        if (img == null)
        {

            return null;

        }

        img = Scalr.resize (img,
                           Scalr.Method.QUALITY,
                           Scalr.Mode.FIT_TO_WIDTH,
                           targetWidth,
                           Scalr.OP_ANTIALIAS);

        return img;

    }

    public static Image getScaledImage (Image img,
                                        int   targetWidth)
    {

        if (img == null)
        {

            return null;

        }

        // TODO Redo to stop using BufferedImage.
        BufferedImage bim = new BufferedImage ((int) img.getWidth (),
                                               (int) img.getHeight (),
                                               BufferedImage.TYPE_INT_ARGB_PRE);

        BufferedImage bi = SwingFXUtils.fromFXImage (img,
                                                     bim);

        bi = Scalr.resize (bi,
                           Scalr.Method.QUALITY,
                           Scalr.Mode.FIT_TO_WIDTH,
                           targetWidth,
                           Scalr.OP_ANTIALIAS);

        return SwingFXUtils.toFXImage (bi, null);

    }

    public static WritableImage getImageOfNode (Node n)
    {

        SnapshotParameters parms = new SnapshotParameters ();
/*
        double w = n.getWidth ();

        if (w < 0)
        {

            w = n.prefWidth (-1);

        }

        double h = n.getHeight ();

        if (h < 0)
        {

            h = n.prefHeight (w);

        }
*/
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

    public static void addDoOnReturnPressed (TextArea f,
                                             Runnable r)
    {

        f.addEventHandler (KeyEvent.KEY_PRESSED,
                           ev ->
        {

            if ((ev.getCode () == KeyCode.ENTER)
                &&
                (ev.isShortcutDown ())
               )
            {

                r.run ();

            }

        });

    }

    public static void addDoOnReturnPressed (QuollTextArea f,
                                             Runnable      r)
    {

        f.getTextEditor ().addEventHandler (KeyEvent.KEY_PRESSED,
                                          ev ->
        {

            if ((ev.getCode () == KeyCode.ENTER)
                &&
                (ev.isShortcutDown ())
               )
            {

                r.run ();

            }

        });

    }

    public static void scrollIntoView (Node node,
                                       VPos pos)
    {

        if (node == null)
        {

            return;

        }

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
                                            Node node,
                                            Bounds b)
    {

        Node p = node.getParent ();

        b = node.localToParent (b);

        while (p != parent)
        {

            if (p == null)
            {

                return b;

            }

            b = p.localToParent (b);
            p = p.getParent ();

        }

        return b;

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

                return b;

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

        if (pos == null)
        {

            pos = VPos.CENTER;

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

            //diff = vh + diff;
            //diff -= vh;

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

        boolean v = UIUtils.isSelected (node);

        UIUtils.unselectChildren (parent);

        node.pseudoClassStateChanged (StyleClassNames.SELECTED_PSEUDO_CLASS, !v);

    }

    public static void unselectChildren (Parent parent)
    {

        parent.getChildrenUnmodifiable ().stream ()
            // Switch off the selected class for all elements.
            .forEach (c -> c.pseudoClassStateChanged (StyleClassNames.SELECTED_PSEUDO_CLASS, false));

    }

    public static boolean isSelected (Node node)
    {

        return node.getPseudoClassStates ().contains (StyleClassNames.SELECTED_PSEUDO_CLASS);

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

            List<String> prefix = Arrays.asList (project,actions,openproject,enterpasswordpopup);

            QuollPopup.passwordEntryBuilder ()
                .withViewer (parentViewer)
                .headerIconClassName (StyleClassNames.ENCRYPTED)
                .title (getUILanguageStringProperty (Utils.newList (prefix,title)))
                .description (getUILanguageStringProperty (Utils.newList (prefix,text),
                                                           proj.nameProperty ()))
                .validator (validator)
                .confirmButtonLabel (getUILanguageStringProperty (Utils.newList (prefix,buttons,open)))
                .onConfirm (ev ->
                {

                    String pwd = ((PasswordField) ev.getForm ().lookup ("#password")).getText ();

                    onProvided.accept (pwd);

                })
                .onCancel (ev ->
                {

                    if (onCancel != null)
                    {

                        onCancel.run ();

                    }

                })
                .build ();

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

        String text = (format != null ? format : UserProperties.getProjectInfoFormat ());

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

        String nl = String.valueOf ('\n');

        while (text.endsWith (nl))
        {

            text = text.substring (0,
                                   text.length () - 1);

        }

        text = text.toLowerCase ();
/*
        text = StringUtils.replaceString (text,
                                          " ",
                                          "&nbsp;");
        text = StringUtils.replaceString (text,
                                          nl,
                                          "<br />");
*/
        text = Utils.replaceString (text,
                                          PROJECT_INFO_STATUS_TAG,
                                          (project.getStatus () != null ? project.getStatus () : getUIString (LanguageStrings.project,status,novalue)));
                                          //"No status"));

        text = Utils.replaceString (text,
                                          PROJECT_INFO_WORDS_TAG,
                                          String.format (getUIString (prefix, LanguageStrings.words),
                                                        //"%s words",
                                                         Environment.formatNumber (project.getWordCount ())));

        text = Utils.replaceString (text,
                                          PROJECT_INFO_CHAPTERS_TAG,
                                          String.format (getUIString (prefix, LanguageStrings.chapters),
                                            //"%s ${objectnames.%s.chapter}",
                                                         Environment.formatNumber (project.getChapterCount ())));

        text = Utils.replaceString (text,
                                          PROJECT_INFO_LAST_EDITED_TAG,
                                          lastEd);
        text = Utils.replaceString (text,
                                          PROJECT_INFO_EDIT_COMPLETE_TAG,
                                          String.format (getUIString (prefix, LanguageStrings.editcomplete),
                                                        //"%s%% complete",
                                                         Environment.formatNumber (Utils.getPercent (project.getEditedWordCount (), project.getWordCount ()))));
        text = Utils.replaceString (text,
                                          PROJECT_INFO_READABILITY_TAG,
                                          String.format (getUIString (prefix, LanguageStrings.readability),
                                                        //"GL: %s, RE: %s, GF: %s",
                                                         Environment.formatNumber (Math.round (project.getFleschKincaidGradeLevel ())),
                                                         Environment.formatNumber (Math.round (project.getFleschReadingEase ())),
                                                         Environment.formatNumber (Math.round (project.getGunningFogIndex ()))));

        return text;

    }

    public static void showDeleteProjectPopup (Project        proj,
                                               Runnable       onDelete,
                                               AbstractViewer viewer)
    {

        ProjectInfo pi = Environment.getProjectInfo (proj);

        if (pi == null)
        {

            throw new IllegalArgumentException ("Unable to find project info for project: " + proj);

        }

        UIUtils.showDeleteProjectPopup (pi,
                                        onDelete,
                                        viewer);

    }

    public static void showDeleteProjectPopup (ProjectInfo    proj,
                                               Runnable       onDelete,
                                               AbstractViewer viewer)
    {

        StringProperty warning = getUILanguageStringProperty (Arrays.asList (LanguageStrings.project,actions,deleteproject,LanguageStrings.warning,normal),
                                                              (proj.isEditorProject () ? getUILanguageStringProperty (Arrays.asList (LanguageStrings.project,actions,deleteproject,LanguageStrings.warning,editor,
                                                                                                                                     proj.getForEditor ().getMainName ()))
                                                                                            : ""));

        UIUtils.showDeleteObjectPopup (getUILanguageStringProperty (project,actions,deleteproject,deletetype),
                                       proj.nameProperty (),
                                       StyleClassNames.DELETE,
                                       warning,
                                       ev ->
                                       {

                                           if (proj.isEditorProject ())
                                           {

                                               EditorsEnvironment.sendProjectEditStopMessage (proj,
                                                // TODO Change.
                                                                                              () ->
                                               {

                                                   UIUtils.runLater (() ->
                                                   {

                                                       Environment.deleteProject (proj,
                                                                                  onDelete);

                                                       QuollPopup.messageBuilder ()
                                                        .withViewer (viewer)
                                                        .title (project,actions,deleteproject,editorproject,confirmpopup,title)
                                                        .message (getUILanguageStringProperty (Arrays.asList (project,actions,deleteproject,editorproject,confirmpopup,text),
                                                                                               proj.getForEditor ().getMainName ()))
                                                        .build ();

                                                   });

                                               });

                                           } else {

                                               Environment.deleteProject (proj,
                                                                          onDelete);

                                           }

                                       },
                                       null,
                                       viewer);

    }

    // TODO Move to QuollPopup as a builder.
    public static QuollPopup showDeleteObjectPopup (StringProperty               deleteType,
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

        QuollPopup qp = QuollPopup.yesConfirmTextEntryBuilder ()
            .title (getUILanguageStringProperty (Arrays.asList (deleteitem,title),
                                                 deleteType))
            .description (message)
            .confirmButtonLabel (deleteitem,confirm)
            .cancelButtonLabel (deleteitem,cancel)
            .onConfirm (onConfirm)
            .onCancel (onCancel)
            //.withHandler (viewer)
            .withViewer (viewer)
            .styleClassName (style)
            .build ();

        return qp;

            /*
            TODO Remove
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
*/
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

    public static ChoiceBox<StringProperty> getWordsOptions (Supplier<Integer> defValue,
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

    public static void doOnKeyReleased (Node     n,
                                        KeyCode  c,
                                        Runnable r)
    {

        n.addEventHandler (KeyEvent.KEY_RELEASED,
                           ev ->
        {

            if (ev.getCode () == c)
            {

                UIUtils.runLater (r);
                ev.consume ();

            }

        });

    }

    public static Node getChildNodeAt (Parent p,
                                       double x,
                                       double y)
    {

        if ((p.getChildrenUnmodifiable () == null)
            ||
            (p.getChildrenUnmodifiable ().size () == 0)
           )
        {

            if (p.getBoundsInParent ().contains (x, y))
            {

                return p;

            }

        }

        for (Node n : p.getChildrenUnmodifiable ())
        {

            if (n.getBoundsInParent ().contains (x, y))
            {

                if (n instanceof Parent)
                {

                    Parent np = (Parent) n;

                    Point2D pp = np.parentToLocal (x, y);

                    return UIUtils.getChildNodeAt (np,
                                                   pp.getX (),
                                                   pp.getY ());

                }

            }

        }

        return null;

    }

    public static <E> void expandAllNodesWithChildren (TreeItem<E> t)
    {

        t.setExpanded (true);

        if (t.getChildren () != null)
        {

            t.getChildren ().stream ()
                .forEach (c -> UIUtils.expandAllNodesWithChildren (c));

        }

    }

    public static String getChapterInfoPreview (Chapter               c,
                                                StringWithMarkup      format,
                                                AbstractProjectViewer viewer)
    {

        // If there is no key or null chapter then return.
        if ((c == null)
            ||
            (c.getKey () == null)
           )
        {

            return null;

        }

        String lastEd = "";

        if (c.getLastModified () != null)
        {

            lastEd = String.format (getUIString (project,sidebar,chapters,preview,lastedited),
                                    //"Last edited: %s",
                                    Environment.formatDate (c.getLastModified ()));

        } else {

            lastEd = getUIString (project,sidebar,chapters,preview,notedited);
            //"Not yet edited.";

        }

        // TODO
        String text = format.getText ();

        if (text == null)
        {

            text = UserProperties.get (Constants.CHAPTER_INFO_PREVIEW_FORMAT,
                                       Constants.DEFAULT_CHAPTER_INFO_PREVIEW_FORMAT);

        }

        String nl = String.valueOf ('\n');

        while (text.endsWith (nl))
        {

            text = text.substring (0,
                                   text.length () - 1);

        }

        text = text.toLowerCase ();

        String desc = c.getDescriptionText ();
        String descFirstLine = null;

        if ((desc == null)
            ||
            (desc.length () == 0)
           )
        {

            desc = getUIString (project,sidebar,chapters,preview,nodescription);
            //"<b>No description.</b>";
            descFirstLine = desc;

        } else {

            descFirstLine = new TextIterator (desc).getFirstSentence ().getText ();

        }

        String chapText = viewer.getCurrentChapterText (c);

        if (chapText != null)
        {

            chapText = chapText.trim ();

        } else {

            chapText = "";

        }

        if (chapText.length () > 0)
        {

            chapText  = new TextIterator (chapText).getFirstSentence ().getText ();

        } else {

            chapText = getUIString (project,sidebar,chapters,preview,emptychapter);
            //"{Chapter} is empty.");

        }

        text = Utils.replaceString (text,
                                          " ",
                                          "&nbsp;");
        text = Utils.replaceString (text,
                                          nl,
                                          "<br />");

        text = Utils.replaceString (text,
                                          Constants.DESCRIPTION_TAG,
                                          desc);

        text = Utils.replaceString (text,
                                          Constants.DESCRIPTION_FIRST_LINE_TAG,
                                          descFirstLine);

        text = Utils.replaceString (text,
                                          Constants.CHAPTER_FIRST_LINE_TAG,
                                          chapText);

        ChapterCounts cc = viewer.getChapterCounts (c);

        if (cc == null)
        {

            // Get the current text instead.
            //cc = new ChapterCounts (c.getChapterText ());
            return null;

        }

        text = Utils.replaceString (text,
                                          Constants.WORDS_TAG,
                                          String.format (getUIString (project,sidebar,chapters,preview,words),
                                                        //"%s words",
                                                         Environment.formatNumber (cc.getWordCount ())));

        text = Utils.replaceString (text,
                                          Constants.LAST_EDITED_TAG,
                                          lastEd);

        int ep = c.getEditPosition ();

        if (c.isEditComplete ())
        {

            ep = 100;

        } else {

            if (ep > 0)
            {

                if (ep > chapText.length () - 1)
                {

                    ep = chapText.length ();

                }

                ChapterCounts ecc = new ChapterCounts (chapText.substring (0,
                                                                           ep));

                ep = Utils.getPercent (ecc.getWordCount (), cc.getWordCount ());

            }

        }

        if (ep < 0)
        {

            ep = 0;

        }

        text = Utils.replaceString (text,
                                          Constants.EDIT_COMPLETE_TAG,
                                          String.format (getUIString (project,sidebar,chapters,preview,editcomplete),
                                          //"%s%% complete",
                                                         Environment.formatNumber (ep)));

        if (text.contains (Constants.PROBLEM_FINDER_PROBLEM_COUNT_TAG))
        {

            text = Utils.replaceString (text,
                                              Constants.PROBLEM_FINDER_PROBLEM_COUNT_TAG,
                                              String.format (getUIString (project,sidebar,chapters,preview,problemcount),
                                                            //"%s problems",
                                                             Environment.formatNumber (cc.getProblemFinderProblemsCount ())));

        }

        if (text.contains (Constants.SPELLING_ERROR_COUNT_TAG))
        {

            text = Utils.replaceString (text,
                                              Constants.SPELLING_ERROR_COUNT_TAG,
                                              String.format (getUIString (project,sidebar,chapters,preview,spellingcount),
                                                            //"%s spelling errors",
                                                             Environment.formatNumber (cc.getSpellingErrorCount ())));
                                                             //viewer.getSpellingErrors (c).size ())));

        }

        ReadabilityIndices ri = cc.getReadabilityIndices ();
        //viewer.getReadabilityIndices (c);

        if (ri == null)
        {

            ri = new ReadabilityIndices ();
            ri.add (c.getChapterText ());

        }

        String na = getUIString (project,sidebar,chapters,preview,notapplicable);

        String GL = na; //"N/A";
        String RE = na; //"N/A";
        String GF = na; //"N/A";

        if (cc.getWordCount () > Constants.MIN_READABILITY_WORD_COUNT)
        {

            GL = Environment.formatNumber (Math.round (ri.getFleschKincaidGradeLevel ()));
            RE = Environment.formatNumber (Math.round (ri.getFleschReadingEase ()));
            GF = Environment.formatNumber (Math.round (ri.getGunningFogIndex ()));

        }

        text = Utils.replaceString (text,
                                          Constants.READABILITY_TAG,
                                          String.format (getUIString (project,sidebar,chapters,preview,readability),
                                          //"GL: %s, RE: %s, GF: %s",
                                                         GL,
                                                         RE,
                                                         GF));

         return text;

    }

    public static void showAddNewUILanguageStringsPopup (final AbstractViewer viewer)
    {

        String none = "[NONE]";

        List<String> prefix = Arrays.asList (uilanguage,_new,popup);

        QuollTextField name = QuollTextField.builder ()
            .build ();

        ComboBox<String> spellcheckLang = new ComboBox<> ();
        spellcheckLang.setDisable (true);

        final QuollCheckBox defLang = QuollCheckBox.builder ()
            .label (new SimpleStringProperty ("Set as default"))
            .build ();
        defLang.managedProperty ().bind (defLang.visibleProperty ());

        Consumer<String> downloadDictFiles = lang ->
        {

            DownloadPanel langDownload = DownloadPanel.builder ()
                .title (getUILanguageStringProperty (Arrays.asList (dictionary,download,notification),
                                                     getUILanguageStringProperty (languagenames,lang)))
                .styleClassName (StyleClassNames.DOWNLOAD)
                .showStop (true)
                .build ();
            langDownload.managedProperty ().bind (langDownload.visibleProperty ());

            Set<Node> controls = new LinkedHashSet<> ();
            controls.add (langDownload.getStopButton ());

            Notification n = viewer.addNotification (langDownload,
                                                     StyleClassNames.DOWNLOAD,
                                                     -1,
                                                     controls);

            UrlDownloader dl = UIUtils.downloadDictionaryFiles (lang,
                                             viewer,
                                             // On progress
                                             p ->
                                             {

                                                 langDownload.setProgress (p);

                                             },
                                             // On complete
                                             () ->
                                             {

                                                 viewer.removeNotification (n);

                                                 // Add a notification that the files have been downloaded.
                                                 viewer.addNotification (getUILanguageStringProperty (Arrays.asList (options,editingchapters,downloaddictionaryfiles,notification,text),
                                                                             //"The language files for <b>%s</b> have been downloaded and the project language set.",
                                                                                                           lang),
                                                                              StyleClassNames.INFORMATION,
                                                                              -1);

                                             },
                                             // On error
                                             ex ->
                                             {

                                                 viewer.removeNotification (n);

                                                 ComponentUtils.showErrorMessage (viewer,
                                                                                  getUILanguageStringProperty (Arrays.asList (dictionary,download,actionerror),
                                                                                                               getUILanguageStringProperty (languagenames,spellcheckLang.valueProperty ().getValue ())));

                                             });

        };

        Node downloadFiles = QuollHyperlink.builder ()
            .label (options,editingchapters,labels,downloadlanguagefiles)
            .styleClassName (StyleClassNames.DOWNLOAD)
            .onAction (ev ->
            {

                String lang = spellcheckLang.valueProperty ().getValue ();

                downloadDictFiles.accept (lang);

            })
            .build ();
        downloadFiles.managedProperty ().bind (downloadFiles.visibleProperty ());

        String defl = UserProperties.getDefaultUILanguageStringsSpellCheckLanguage ();

        if (defl != null)
        {

            downloadFiles.setVisible (!DictionaryProvider.isLanguageInstalled (defl));

        } else {

            downloadFiles.setVisible (false);

        }

        spellcheckLang.valueProperty ().addListener ((pr, oldv, newv) ->
        {

            final String lang = newv;

            String def = UserProperties.getDefaultUILanguageStringsSpellCheckLanguage ();

            final String currLang = def;

            if (def != null)
            {

                if (!def.equals (lang))
                {

                    defLang.setSelected (false);

                } else {

                    defLang.setSelected (true);

                }

            }

            downloadFiles.setVisible (false);

            // Check to see if the files are available.
            try
            {

                if ((!lang.equals (none))
                    &&
                    (!DictionaryProvider.isLanguageInstalled (lang))
                   )
                {

                    downloadFiles.setVisible (true);

                    List<String> _prefix = Arrays.asList (options,editingchapters,downloaddictionaryfiles,popup);

                    QuollPopup.questionBuilder ()
                        .withViewer (viewer)
                        .styleClassName (StyleClassNames.DOWNLOAD)
                        .title (getUILanguageStringProperty (Utils.newList (_prefix,title)))
                        .message (getUILanguageStringProperty (Utils.newList (_prefix,text),
                                                               lang))
                        .confirmButtonLabel (getUILanguageStringProperty (Utils.newList (_prefix,buttons,confirm)))
                        .cancelButtonLabel (getUILanguageStringProperty (Utils.newList (_prefix,buttons,cancel)))
                        .onConfirm (ev ->
                        {

                            downloadDictFiles.accept (lang);

                        })
                        .onCancel (ev ->
                        {

                            spellcheckLang.getSelectionModel ().select (currLang);

                        })
                        .build ();

                    return;

                }

            } catch (Exception e) {

                Environment.logError ("Unable to get language files for: " +
                                      lang,
                                      e);

                ComponentUtils.showErrorMessage (viewer,
                                                 getUILanguageStringProperty (options,editingchapters,downloaddictionaryfiles,actionerror));
                                          //"Unable to check for dictionary files, please contact Quoll Writer support.");

                return;

            }

        });

        Callback<ListView<String>, ListCell<String>> langCellFactory = (lv ->
        {

            return new ListCell<String> ()
            {

                @Override
                protected void updateItem (String  item,
                                           boolean empty)
                {

                    super.updateItem (item,
                                      empty);

                    if (empty || item == null)
                    {

                        this.textProperty ().unbind ();
                        setText ("");

                    } else {

                        if (item.equals (none))
                        {

                            this.textProperty ().bind (new SimpleStringProperty ("Do not spellcheck"));

                        } else {

                            StringProperty textProp = getUILanguageStringProperty (Arrays.asList (languagenames,item));

                            this.textProperty ().bind (textProp);

                        }

                    }

                }

            };

        });

        spellcheckLang.setCellFactory (langCellFactory);
        spellcheckLang.setButtonCell (langCellFactory.call (null));

        // Get the languages supported by the spellchecker.
        Environment.schedule (() ->
        {

            String l = null;

            try
            {

                l = Utils.getUrlFileAsString (new URL (Environment.getQuollWriterWebsite () + "/" + UserProperties.get (Constants.QUOLL_WRITER_SUPPORTED_LANGUAGES_URL_PROPERTY_NAME)));

            } catch (Exception e) {

                Environment.logError ("Unable to get language files url",
                                      e);

            }

            StringTokenizer t = new StringTokenizer (l,
                                                     String.valueOf ('\n'));

            final List<String> langs = new ArrayList<> ();

            langs.add (none);

            while (t.hasMoreTokens ())
            {

                String lang = t.nextToken ().trim ();

                if (lang.equals (Constants.ENGLISH))
                {

                    continue;

                }

                if (lang.equals (""))
                {

                    continue;

                }

                langs.add (lang);

            }

            UIUtils.runLater (() ->
            {

                spellcheckLang.getItems ().addAll (langs);

                String def = UserProperties.getDefaultUILanguageStringsSpellCheckLanguage ();

                spellcheckLang.getSelectionModel ().select (def);

                spellcheckLang.setDisable (false);

            });

        },
        1,
        -1);

        defLang.selectedProperty ().addListener ((pr, oldv, newv) ->
        {

            if (defLang.isSelected ())
            {

                UserProperties.setDefaultUILanguageStringsSpellCheckLanguage (spellcheckLang.valueProperty ().getValue ());

            }

        });

        Form f = Form.builder ()
            .description (prefix,text)
            .confirmButton (getUILanguageStringProperty (Utils.newList (prefix,buttons,create)))
            .cancelButton (getUILanguageStringProperty (Utils.newList (prefix,buttons,cancel)))
            .item (getUILanguageStringProperty (Utils.newList (prefix,labels,LanguageStrings.name)),
                   name)
            .item (getUILanguageStringProperty (Utils.newList (prefix,labels,LanguageStrings.spellcheck)),
                   spellcheckLang)
            .item (defLang)
            .item (downloadFiles)
            .build ();

        f.addEventHandler (Form.FormEvent.CONFIRM_EVENT,
                           ev ->
        {

             String v = name.getText ().trim ();
             f.hideError ();

             if ((v == null)
                 ||
                 (v.trim ().length () == 0)
                )
             {

                 // This can be English because if the creator doesn't know English then they can't create a set of strings.
                 f.showError (new SimpleStringProperty ("Please enter the language name"));
                 return;

             }

             UILanguageStrings ls = new UILanguageStrings (UILanguageStringsManager.getDefaultUILanguageStrings ());
             ls.setNativeName (v);
             ls.setUser (true);
             ls.setQuollWriterVersion (Environment.getQuollWriterVersion ());
             ls.setSpellcheckLanguage (spellcheckLang.getValue ().equals (none) ? null : spellcheckLang.getValue ());

             try
             {

                 LanguageStringsEditor lse = new LanguageStringsEditor (ls);
                 lse.createViewer ();
                 lse.init (null);

             } catch (Exception e) {

                 Environment.logError ("Unable to create language strings editor",
                                       e);

                 ComponentUtils.showErrorMessage (viewer,
                                                  "Unable to create strings editor.");

             }

        });

        QuollPopup.formBuilder ()
            .withViewer (viewer)
            .title (prefix,title)
            //.description (prefix,text)
            .styleClassName (StyleClassNames.ADDNEWUILANGSTRINGS)
            .headerIconClassName (StyleClassNames.EDIT)
            //.confirmButtonLabel (prefix,buttons,create)
            //.cancelButtonLabel (prefix,buttons,cancel)
            .form (f)
            /*
            .onConfirm (ev ->
            {

                // TODO Improve this...
                TextField tf = (TextField) ev.getForm ().lookup ("#text");

                String v = tf.getText ().trim ();

                 UILanguageStrings ls = new UILanguageStrings (UILanguageStringsManager.getDefaultUILanguageStrings ());
                 ls.setNativeName (v);
                 ls.setUser (true);
                 ls.setQuollWriterVersion (Environment.getQuollWriterVersion ());

                 try
                 {

                     LanguageStringsEditor lse = new LanguageStringsEditor (ls);
                     lse.createViewer ();
                     lse.init (null);

                 } catch (Exception e) {

                     Environment.logError ("Unable to create language strings editor",
                                           e);

                     ComponentUtils.showErrorMessage (viewer,
                                                      "Unable to create strings editor.");

                 }

            })
            */
            /*
            .validator (v ->
            {

                if ((v == null)
                    ||
                    (v.trim ().length () == 0)
                   )
                {

                    // This can be English because if the creator doesn't know English then they can't create a set of strings.
                    return new SimpleStringProperty ("Please enter the language name");

                }

                return null;

            })
            */
            .build ();
            /*
            TODO Remove
        ComponentUtils.createTextEntryPopup (getUILanguageStringProperty (Utils.newList (prefix,title)),
                                                            StyleClassNames.ADDNEWUILANGSTRINGS,
                                                            getUILanguageStringProperty (Utils.newList (prefix,text)),
                                                            null,
                                                            // Validator.
                                                            v ->
                                                            {

                                                                if ((v == null)
                                                                    ||
                                                                    (v.trim ().length () == 0)
                                                                   )
                                                                {

                                                                    // This can be English because if the creator doesn't know English then they can't create a set of strings.
                                                                    return new SimpleStringProperty ("Please enter the language name");

                                                                }

                                                                return null;

                                                            },
                                                            getUILanguageStringProperty (Utils.newList (prefix,buttons,create)),
                                                            getUILanguageStringProperty (Utils.newList (prefix,buttons,cancel)),
                                                            // On confirm.
                                                            ev ->
                                                            {

                                                                // TODO Improve this...
                                                                TextField tf = (TextField) ev.getForm ().lookup ("#text");

                                                                String v = tf.getText ().trim ();

                                                                 UILanguageStrings ls = new UILanguageStrings (UILanguageStringsManager.getDefaultUILanguageStrings ());
                                                                 ls.setNativeName (v);
                                                                 ls.setUser (true);

                                                                 try
                                                                 {

                                                                     // TODO new LanguageStringsEditor (ls).init ();

                                                                 } catch (Exception e) {

                                                                     Environment.logError ("Unable to create language strings editor",
                                                                                           e);

                                                                     ComponentUtils.showErrorMessage (viewer,
                                                                                                      "Unable to create strings editor.");

                                                                 }

                                                             },
                                                             // On cancel
                                                             null,
                                                             // On close
                                                             null,
                                                             viewer);
*/
    }

    public static void showEditUILanguageStringsSelectorPopup (final AbstractViewer viewer)
    {

        String popupId = StyleClassNames.UILANGSTRINGSSELECT;

        QuollPopup qp = viewer.getPopupById (popupId);

        if (qp != null)
        {

            qp.toFront ();
            return;

        }

        Set<UILanguageStrings> objs = null;

        try
        {

            objs = UILanguageStringsManager.getAllUserUILanguageStrings ();

        } catch (Exception e) {

            Environment.logError ("Unable to get all user language strings.",
                                  e);

            ComponentUtils.showErrorMessage (viewer,
                                             getUILanguageStringProperty (uilanguage,edit,actionerror));

            return;

        }

        if (objs.size () == 0)
        {

            QuollPopup.messageBuilder ()
                .title (uilanguage,edit,novalue,title)
                .message (uilanguage,edit,novalue,text)
                .withViewer (viewer)
                .build ();

            return;

        }

        objs = objs.stream ()
            .sorted ((o1, o2) ->
            {

                if (o1.getQuollWriterVersion ().equals (o2.getQuollWriterVersion ()))
                {

                    return o1.getNativeName ().compareTo (o2.getNativeName ());

                }

                return -1 * o1.getQuollWriterVersion ().compareTo (o2.getQuollWriterVersion ());

            })
            //.collect (Collectors.toSet ());
            .collect (Collectors.toCollection (() -> new LinkedHashSet<> ()));

        ShowObjectSelectPopup.<UILanguageStrings>builder ()
            .withViewer (viewer)
            .title (getUILanguageStringProperty (uilanguage,edit,popup,title))
            .styleClassName (StyleClassNames.UILANGSTRINGSSELECT)
            .styleSheet (StyleClassNames.UILANGSTRINGSSELECT)
            .headerIconClassName (StyleClassNames.EDIT)
            .popupId (popupId)
            .objects (FXCollections.observableList (new ArrayList<> (objs)))
            .cellProvider ((obj, popupContent) ->
            {

               QuollLabel l = QuollLabel.builder ()
                    .label (getUILanguageStringProperty (Arrays.asList (uilanguage,edit,popup,item),
                                                         obj.getName (),
                                                         obj.getQuollWriterVersion ().toString (),
                                                         Environment.formatNumber (obj.getPercentComplete ())))
                    .build ();

                l.setOnMouseClicked (ev ->
                {

                    if (ev.getButton () != MouseButton.PRIMARY)
                    {

                        return;

                    }

                    UILanguageStringsManager.editUILanguageStrings (obj,
                                                                    obj.getQuollWriterVersion ());

                    popupContent.close ();

                });

                return l;

            })
            .build ()
            .show ();

    }

    public static void showAddNewWebsiteLanguageStringsPopup (final AbstractViewer viewer)
    {

        java.util.List<String> prefix = Arrays.asList (websiteuilanguage,_new,popup);

        QuollPopup.textEntryBuilder ()
            .withViewer (viewer)
            //.withHandler (viewer)
            .title (prefix,title)
            .description (prefix,text)
            .styleClassName (StyleClassNames.ADDNEWWEBSITELANGSTRINGS)
            .confirmButtonLabel (prefix,buttons,create)
            .cancelButtonLabel (prefix,buttons,cancel)
            .onConfirm (ev ->
            {

                // TODO Improve this...
                TextField tf = (TextField) ev.getForm ().lookup ("#text");

                String v = tf.getText ().trim ();

                try
                {

                    WebsiteLanguageStrings enStrs = WebsiteLanguageStringsManager.getWebsiteLanguageStringsFromServer ();

                    WebsiteLanguageStringsManager.saveWebsiteLanguageStrings (enStrs);

                    WebsiteLanguageStrings ls = new WebsiteLanguageStrings (enStrs);
                    ls.setNativeName (v);
                    ls.setUser (true);

                } catch (Exception e) {

                    Environment.logError ("Unable to create website language strings editor",
                                          e);

                    ComponentUtils.showErrorMessage (viewer,
                                                     "Unable to create website strings editor.");

                }

                // TODO new WebsiteLanguageStringsEditor (ls).init ();

            })
            .validator (v ->
            {

                if ((v == null)
                    ||
                    (v.trim ().length () == 0)
                   )
                {

                    // This can be English because if the creator doesn't know English then they can't create a set of strings.
                    return new SimpleStringProperty ("Please enter the language name");

                }

                return null;

            })
            .build ();

    }

    public static void showEditWebsiteLanguageStringsSelectorPopup (final AbstractViewer viewer)
    {

        String popupId = StyleClassNames.WEBSITELANGSTRINGSSELECT;

        QuollPopup qp = viewer.getPopupById (popupId);

        if (qp != null)
        {

            qp.toFront ();
            return;

        }

        Set<WebsiteLanguageStrings> objs = null;

        try
        {

            objs = WebsiteLanguageStringsManager.getAllWebsiteLanguageStrings ();

        } catch (Exception e) {

            Environment.logError ("Unable to get all user website language strings.",
                                  e);

            ComponentUtils.showErrorMessage (viewer,
                                             getUILanguageStringProperty (uilanguage,edit,actionerror));

            return;

        }

        if (objs.size () == 0)
        {

            QuollPopup.messageBuilder ()
                .title (uilanguage,edit,novalue,title)
                .message (uilanguage,edit,novalue,text)
                .withViewer (viewer)
                .build ();

            return;

        }

        ShowObjectSelectPopup.<WebsiteLanguageStrings>builder ()
            .withViewer (viewer)
            .title (getUILanguageStringProperty (uilanguage,edit,popup,title))
            .styleClassName (StyleClassNames.WEBSITELANGSTRINGSSELECT)
            .popupId (popupId)
            .objects (FXCollections.observableList (new ArrayList<> (objs)))
            .cellProvider ((obj, popupContent) ->
            {

               QuollLabel l = QuollLabel.builder ()
                    .label (getUILanguageStringProperty (Arrays.asList (uilanguage,edit,popup,item),
                                                         obj.getDisplayName (),
                                                         Environment.formatNumber (obj.getPercentComplete ())))
                    .build ();

                l.setOnMouseClicked (ev ->
                {

                    if (ev.getButton () != MouseButton.PRIMARY)
                    {

                        return;

                    }

                    WebsiteLanguageStringsManager.editWebsiteLanguageStrings (obj);

                    popupContent.close ();

                });

                return l;

            })
            .build ()
            .show ();

    }

    public static UrlDownloader downloadDictionaryFiles (String              lang,
                                                         AbstractViewer      viewer,
                                                         Consumer<Double>    onProgress,
                                                         Runnable            onComplete,
                                                         Consumer<Exception> onError)
    {

        if (UILanguageStrings.isEnglish (lang))
        {

            lang = Constants.ENGLISH;

        }

        final String langOrig = lang;
        final String language = lang;

        String fileLang = lang;

        // Legacy, if the user doesn't have the language file but DOES have a thesaurus then just
        // download the English-dictionary-only.zip.
        if ((UILanguageStrings.isEnglish (lang))
            &&
            (!Files.exists (DictionaryProvider.getDictionaryFilePath (lang)))
            &&
            (Environment.hasSynonymsDirectory (lang))
           )
        {

            fileLang = "English-dictionary-only";

        }

        URL url = null;

        try
        {

            url = new URL (Environment.getQuollWriterWebsite () + "/" + Utils.replaceString (UserProperties.get (Constants.QUOLL_WRITER_LANGUAGE_FILES_URL_PROPERTY_NAME),
                                                                                                   "[[LANG]]",
                                                                                                   Utils.replaceString (fileLang,
                                                                                                                              " ",
                                                                                                                              "%20")));

        } catch (Exception e) {

            Environment.logError ("Unable to download language files, cant create url",
                                  e);

            if (onError != null)
            {

                UIUtils.runLater (() ->
                {

                    onError.accept (e);

                });

            }
/*
TODO Remove
            ComponentUtils.showErrorMessage (viewer,
                                             getUILanguageStringProperty (dictionary,download,actionerror));
                                      //"Unable to download language files");
*/
            return null;

        }

        Environment.logDebugMessage ("Downloading language file(s) from: " + url + ", for language: " + lang);

        File _file = null;

        // Create a temp file for it.
        try
        {

            _file = File.createTempFile ("quollwriter-language-" + fileLang,
                                         null);

        } catch (Exception e) {

            Environment.logError ("Unable to download language files, cant create temp file",
                                  e);

            if (onError != null)
            {

                UIUtils.runLater (() ->
                {

                    onError.accept (e);

                });

            }

            /*
             TODO Remove
            ComponentUtils.showErrorMessage (viewer,
                                             getUILanguageStringProperty (dictionary,download,actionerror));
                                    //"Unable to download language files");
*/
            return null;

        }

        _file.deleteOnExit ();

        final File file = _file;

        final UrlDownloader downloader = new UrlDownloader (url,
                                                            file,
                                                            new DownloadListener ()
                                                            {

                                                                @Override
                                                                public void handleError (Exception e)
                                                                {

                                                                    Environment.logError ("Unable to download language files for: " +
                                                                                          langOrig,
                                                                                          e);

                                                                    if (onError != null)
                                                                    {

                                                                        UIUtils.runLater (() ->
                                                                        {

                                                                            onError.accept (e);

                                                                        });

                                                                    }

                                                                    // TODO Remove UIUtils.runLater (() -> n.removeNotification ());
/*
 TODO Remove
                                                                    ComponentUtils.showErrorMessage (viewer,
                                                                                                     getUILanguageStringProperty (Arrays.asList (dictionary,download,actionerror),
                                                                                                                                  getUILanguageStringProperty (languagenames,langOrig)));
                                                                                                                                  */
                                                                                              //"A problem has occurred while downloading the language files for <b>" + langOrig + "</b>.<br /><br />Please contact Quoll Writer support for assistance.");

                                                                }

                                                                @Override
                                                                public void onStop ()
                                                                {

                                                                    file.delete ();

                                                                }

                                                                @Override
                                                                public void progress (final int downloaded,
                                                                                      final int total)
                                                                {

                                                                    if (onProgress != null)
                                                                    {

                                                                        UIUtils.runLater (() ->
                                                                        {

                                                                            onProgress.accept ((double) downloaded / (double) total);

                                                                        });

                                                                    }

                                                                }

                                                                @Override
                                                                public void finished (int total)
                                                                {

                                                                    if (onProgress != null)
                                                                    {

                                                                        UIUtils.runLater (() ->
                                                                        {

                                                                            onProgress.accept (1d);

                                                                        });

                                                                    }

                                                                    Environment.schedule (() ->
                                                                    {

                                                                        // Now extract the file into the relevant directory.
                                                                        try
                                                                        {

                                                                            Utils.extractZipFile (file,
                                                                                                  Environment.getUserQuollWriterDirPath ().toFile ());

                                                                        } catch (Exception e) {

                                                                            Environment.logError ("Unable to extract language zip file: " +
                                                                                                  file +
                                                                                                  " to: " +
                                                                                                  Environment.getUserQuollWriterDirPath (),
                                                                                                  e);

                                                                            if (onError != null)
                                                                            {

                                                                                UIUtils.runLater (() ->
                                                                                {

                                                                                    onError.accept (e);

                                                                                });

                                                                            }
/*
TODO Remove
                                                                             ComponentUtils.showErrorMessage (viewer,
                                                                                                              getUILanguageStringProperty (Arrays.asList (dictionary,download,actionerror),
                                                                                                                                           getUILanguageStringProperty (languagenames, langOrig)));
*/
                                                                            return;

                                                                        } finally {

                                                                            file.delete ();

                                                                        }

                                                                        UIUtils.runLater (onComplete);

                                                                        // TODO Remove UIUtils.runLater (() -> n.removeNotification ());

                                                                    },
                                                                    1,
                                                                    -1);

                                                                }

                                                            });

/*
TODO Remove
        content.setOnStop (() ->
        {

            downloader.stop ();

            file.delete ();

        });
*/
        downloader.start ();

        return downloader;

    }

    public static MenuButton createTagsMenuButton (NamedObject           obj,
                                                   StringProperty        tooltip,
                                                   AbstractProjectViewer viewer)
    {

        return QuollMenuButton.builder ()
            .iconName (StyleClassNames.TAG)
            .tooltip (tooltip)
            .buttonId ("tags")
            .items (UIUtils.createTagsMenuItemsSupplier (obj,
                                                         viewer))
            .build ();

    }

    public static Supplier<Set<MenuItem>> createTagsMenuItemsSupplier (NamedObject           obj,
                                                                       AbstractProjectViewer viewer)
    {

        return () ->
        {

            Set<Tag> allTags = null;

            try
            {

                allTags = Environment.getAllTags ();

            } catch (Exception e) {

                Environment.logError ("Unable to get all tags",
                                      e);

                return null;

            }

            Set<MenuItem> items = new LinkedHashSet<> ();

            for (Tag t : allTags)
            {

                CheckMenuItem cmi = new CheckMenuItem ();
                cmi.setSelected (obj.hasTag (t));
                cmi.textProperty ().bind (t.nameProperty ());
                cmi.setOnAction (ev ->
                {

                    if (cmi.isSelected ())
                    {

                        obj.addTag (t);

                    } else {

                        obj.removeTag (t);

                    }

                    try
                    {

                        viewer.saveObject (obj,
                                           false);

                    } catch (Exception e) {

                        Environment.logError ("Unable to update object: " +
                                              obj,
                                              e);

                        ComponentUtils.showErrorMessage (viewer,
                                                         getUILanguageStringProperty (tags,actions,apply,actionerror));
                                                  //"Unable to add/remove tag.");

                        return;

                    }

                });

                items.add (cmi);

            }

            if (allTags.size () > 0)
            {

                items.add (new SeparatorMenuItem ());

            }

            items.add (QuollMenuItem.builder ()
                .iconName (StyleClassNames.ADD)
                .label (getUILanguageStringProperty (tags,popupmenu,_new))
                .onAction (ev ->
                {

                    viewer.showAddNewTagPopup (obj,
                                               null);

                })
                .build ());

            return items;

        };

    }

    public static Menu createTagsMenu (final NamedObject           obj,
                                       final AbstractProjectViewer viewer)
    {

        Menu tagMenu = QuollMenu.builder ()
            .label (getUILanguageStringProperty (tags,popupmenu,title))
            .styleClassName (StyleClassNames.TAG)
            .items (UIUtils.createTagsMenuItemsSupplier (obj,
                                                         viewer))
            .build ();
        //"Tags");

        return tagMenu;

    }

    public static Menu createMoveMenu (Consumer<String>      onMove,
                                       StringProperty        label,
                                       List<String>          prefix)
    {

        Set<MenuItem> items = new LinkedHashSet<> ();
        items.add (QuollMenuItem.builder ()
                    .iconName (StyleClassNames.MOVETOP)
                    .label (getUILanguageStringProperty (Utils.newList (prefix,top)))
                    .onAction (ev ->
                    {

                        onMove.accept (StyleClassNames.MOVETOP);

                    })
                    .build ());

        items.add (QuollMenuItem.builder ()
                    .iconName (StyleClassNames.MOVEUP)
                    .label (getUILanguageStringProperty (Utils.newList (prefix,up)))
                    .onAction (ev ->
                    {

                        onMove.accept (StyleClassNames.MOVEUP);

                    })
                    .build ());

        items.add (QuollMenuItem.builder ()
                    .iconName (StyleClassNames.MOVEDOWN)
                    .label (getUILanguageStringProperty (Utils.newList (prefix,down)))
                    .onAction (ev ->
                    {

                        onMove.accept (StyleClassNames.MOVEDOWN);

                    })
                    .build ());

        items.add (QuollMenuItem.builder ()
                    .iconName (StyleClassNames.MOVEBOTTOM)
                    .label (getUILanguageStringProperty (Utils.newList (prefix,bottom)))
                    .onAction (ev ->
                    {

                        onMove.accept (StyleClassNames.MOVEBOTTOM);

                    })
                    .build ());

        Menu m = QuollMenu.builder ()
            .label (label)
            .styleClassName (StyleClassNames.MOVEVERT)
            .items (items)
            .build ();

        return m;

    }

    public static ImageView getImageView (BufferedImage i)
    {

        return new ImageView (SwingFXUtils.toFXImage (i, null));

    }

    public static ImageView getImageView (Image i)
    {

        return new ImageView (i);

    }

    public static Set<MenuItem> getNewAssetMenuItems (final ProjectViewer viewer)
    {

        return UIUtils.getNewAssetMenuItems (viewer,
                                             null,
                                             null);

    }

    public static Set<MenuItem> getNewAssetMenuItems (final ProjectViewer viewer,
                                                      String              name,
                                                      StringWithMarkup    description)
    {

        String pref = getUIString (general,shortcut);
        //"Shortcut: ";

        Set<UserConfigurableObjectType> types = Environment.getAssetUserConfigurableObjectTypes (true);

        Set<MenuItem> ret = new LinkedHashSet<> ();

        for (UserConfigurableObjectType type : types)
        {

            QuollMenuItem i = QuollMenuItem.builder ()
                .label (type.getObjectTypeName ())
                .icon (type.icon16x16Property ())
                .onAction (ev ->
                {

                    Asset asset = null;

                    try
                    {

                        asset = Asset.createAsset (type);

                    } catch (Exception e) {

                        Environment.logError ("Unable to create new asset for object type: " +
                                              type,
                                              e);

                        ComponentUtils.showErrorMessage (viewer,
                                                         getUILanguageStringProperty (Arrays.asList (assets,add,actionerror),
                                                                                      type.getObjectTypeName ()));
                                                  //"Unable to create new asset type.");

                        return;

                    }

                    if (name != null)
                    {

                        asset.setName (name);

                    }

                    asset.setDescription (description);

                    viewer.showAddNewAsset (asset);

                })
                .build ();

            ret.add (i);

/*
TODO
            KeyStroke k = type.getCreateShortcutKeyStroke ();

            if (k != null)
            {

                mi.setMnemonic (k.getKeyChar ());
                mi.setToolTipText (pref + Utils.keyStrokeToString (k));

            }
*/
        }

        return ret;

    }

    public static void createSelectableTree (TreeItem         root,
                                             Chapter          chap,
                                             Set<NamedObject> selected,
                                             Set<NamedObject> exclude)
    {

        if (exclude.contains (chap))
        {

            return;

        }

        CheckBoxTreeItem<NamedObject> ci = new CheckBoxTreeItem<> (chap);
        ci.setSelected ((selected == null ? false : selected.contains (chap)));
        ci.setIndependent (true);

        root.getChildren ().add (ci);

        root = ci;

        List<ChapterItem> items = new ArrayList<> (chap.getScenes ());

        items.addAll (chap.getOutlineItems ());

        Collections.sort (items,
                          new ChapterItemSorter ());

        root.getChildren ().addAll (items.stream ()
            .filter (i -> (exclude == null ? true : !exclude.contains (i)))
            .map (chi ->
            {

                CheckBoxTreeItem<NamedObject> cii = new CheckBoxTreeItem<> (chi);
                cii.setIndependent (true);
                cii.setSelected ((selected == null ? false : selected.contains (chi)));

                if (chi instanceof com.quollwriter.data.Scene)
                {

                    com.quollwriter.data.Scene s = (com.quollwriter.data.Scene) chi;

                    cii.getChildren ().addAll (s.getOutlineItems ().stream ()
                        .filter (i -> !exclude.contains (i))
                        .map (oitem ->
                        {

                            CheckBoxTreeItem<NamedObject> oii = new CheckBoxTreeItem<> (oitem);
                            oii.setIndependent (true);
                            oii.setSelected ((selected == null ? false : selected.contains (oitem)));

                            return oii;

                        })
                        .collect (Collectors.toList ()));

                }

                return cii;

            })
            .collect (Collectors.toList ()));

    }

    public static TreeItem<NamedObject> createTree (Chapter          chap,
                                                    Set<NamedObject> include)
    {

        if ((include != null)
            &&
            (!include.contains (chap))
           )
        {

            return null;

        }

        TreeItem<NamedObject> ci = new TreeItem<> (chap);

        List<ChapterItem> items = new ArrayList<> (chap.getScenes ());

        items.addAll (chap.getOutlineItems ());

        Collections.sort (items,
                          new ChapterItemSorter ());

        ci.getChildren ().addAll (items.stream ()
            .filter (i -> (include == null ? true : include.contains (i)))
            .map (chi ->
            {

                TreeItem<NamedObject> cii = new TreeItem<> (chi);

                if (chi instanceof com.quollwriter.data.Scene)
                {

                    com.quollwriter.data.Scene s = (com.quollwriter.data.Scene) chi;

                    cii.getChildren ().addAll (s.getOutlineItems ().stream ()
                        .filter (i -> (include == null ? true : include.contains (i)))
                        .map (oitem ->
                        {

                            TreeItem<NamedObject> oii = new TreeItem<> (oitem);

                            return oii;

                        })
                        .collect (Collectors.toList ()));

                }

                return cii;

            })
            .collect (Collectors.toList ()));

        return ci;

    }

    public static QuollPopup createChaptersSelectableTreePopup (Project                proj,
                                                                AbstractViewer         viewer,
                                                                Set<Chapter>     selected,
                                                                Set<Chapter>     exclude,
                                                                StringProperty   title,
                                                                StringProperty         closeButtonLabel,
                                                                Consumer<Set<Chapter>> onClose)
    {

        QuollTreeView tree = new QuollTreeView<> ();
        tree.setShowRoot (false);
        tree.getStyleClass ().add (StyleClassNames.CHAPTER);

        TreeItem<NamedObject> root = new TreeItem<> ();
        root.setValue (proj);
        tree.setRoot (root);

        proj.getBook (0).getChapters ().stream ()
            .forEach (c ->
            {

                if (exclude != null)
                {

                    if (exclude.contains (c))
                    {

                        return;

                    }

                }

                CheckBoxTreeItem<NamedObject> ci = new CheckBoxTreeItem<> (c);
                ci.setSelected ((selected == null ? false : selected.contains (c)));
                ci.setIndependent (true);

                root.getChildren ().add (ci);

            });

        Function<TreeItem<Object>, Node> cellProvider = (treeItem) ->
        {

            Object n = treeItem.getValue ();

            if (n instanceof Project)
            {

                return new Label ();

            }

            if (n instanceof Chapter)
            {

                Chapter c = (Chapter) n;

                QuollLabel l = QuollLabel.builder ()
                    .styleClassName (c.getObjectType ())
                    .label (c.nameProperty ())
                    .build ();

                return l;

            }

            throw new IllegalStateException ("How did we get here? " + n);

        };

        tree.setCellProvider (cellProvider);

        VBox b = new VBox ();
        VBox.setVgrow (tree,
                       Priority.ALWAYS);
        QuollCheckBox cb = QuollCheckBox.builder ()
            .label (getUILanguageStringProperty (actions,selectall))
            .styleClassName (StyleClassNames.SELECT)
            .build ();
        cb.setOnAction (ev ->
        {

            tree.walkTree (ti ->
            {

                if (ti instanceof CheckBoxTreeItem)
                {

                    CheckBoxTreeItem<NamedObject> cti = (CheckBoxTreeItem<NamedObject>) ti;

                    cti.setSelected (cb.isSelected ());

                }

            });

        });
        b.getChildren ().addAll (cb, new QScrollPane (tree));

        QuollPopup qp = QuollPopup.messageBuilder ()
            .title (title)
            .message (b)
            .inViewer (viewer)
            .styleSheet ("chapterselect")
            .headerIconClassName (Chapter.OBJECT_TYPE)
            .hideOnEscape (true)
            .removeOnClose (true)
            .closeButton (closeButtonLabel, null)
            .build ();

        qp.addEventHandler (QuollPopup.PopupEvent.CLOSED_EVENT,
                            ev ->
        {

            Set<Chapter> sel = new LinkedHashSet<> ();

            tree.walkTree (ti ->
            {

                if (ti instanceof CheckBoxTreeItem)
                {

                    CheckBoxTreeItem<NamedObject> cti = (CheckBoxTreeItem<NamedObject>) ti;

                    if (cti.isSelected ())
                    {

                        sel.add ((Chapter) cti.getValue ());

                    }

                }

            });

            onClose.accept (sel);

        });

        return qp;

    }

    public static void createChaptersSelectableTree (TreeItem         root,
                                                     Project          proj,
                                                     Set<NamedObject> selected,
                                                     Set<NamedObject> exclude)
    {

        BlankNamedObject bdo = new BlankNamedObject (Chapter.OBJECT_TYPE);
        bdo.nameProperty ().bind (Environment.getObjectTypeNamePlural (Chapter.OBJECT_TYPE));

        TreeItem<NamedObject> croot = new TreeItem<> (bdo);
        root.getChildren ().add (croot);

        proj.getBooks ().get (0).getChapters ().stream ()
            .filter (c -> (exclude == null ? true : !exclude.contains (c)))
            .forEach (c ->
            {

                UIUtils.createSelectableTree (croot,
                                              c,
                                              selected,
                                              exclude);

            });

    }

    public static void createChaptersTree (TreeItem         root,
                                           Project          proj,
                                           Set<NamedObject> include)
    {

        List<TreeItem<NamedObject>> its = proj.getBooks ().get (0).getChapters ().stream ()
            .filter (c -> (include == null ? true : include.contains (c)))
            .map (c ->
            {

                return UIUtils.createTree (c,
                                           include);

            })
            .filter (ti -> ti != null)
            .collect (Collectors.toList ());

        if (its.size () > 0)
        {

            BlankNamedObject bdo = new BlankNamedObject (Chapter.OBJECT_TYPE);
            bdo.nameProperty ().bind (Environment.getObjectTypeNamePlural (Chapter.OBJECT_TYPE));

            TreeItem<NamedObject> croot = new TreeItem<> (bdo);
            root.getChildren ().add (croot);

            croot.getChildren ().addAll (its);

        }

    }

    public static void createAssetsSelectableTree (TreeItem         root,
                                                   UserConfigurableObjectType type,
                                                   Set<Asset>       assets,
                                                   Set<NamedObject> selected,
                                                   Set<NamedObject> exclude)
    {

        if ((assets == null)
            ||
            (assets.size () == 0)
           )
        {

            return;

        }

        List<TreeItem<NamedObject>> its = assets.stream ()
            .filter (a -> (exclude == null ? true : !exclude.contains (a)))
            .map (a ->
            {

                CheckBoxTreeItem<NamedObject> ci = new CheckBoxTreeItem<> (a);
                ci.setSelected ((selected == null ? false : selected.contains (a)));
                return ci;

            })
            .collect (Collectors.toList ());

        if (its.size () > 0)
        {

            TreeItem<NamedObject> nroot = new TreeItem<> (type);
            root.getChildren ().add (nroot);

            nroot.getChildren ().addAll (its);

        }

    }

    public static void createAssetsTree (TreeItem                   root,
                                         UserConfigurableObjectType type,
                                         Set<Asset>                 assets,
                                         Set<NamedObject>           include)
    {

        if ((assets == null)
            ||
            (assets.size () == 0)
           )
        {

            return;

        }

        List<TreeItem<NamedObject>> its = assets.stream ()
            .filter (a -> (include == null ? true : include.contains (a)))
            .map (a ->
            {

                TreeItem<NamedObject> ci = new TreeItem<> (a);
                return ci;

            })
            .collect (Collectors.toList ());

        if (its.size () > 0)
        {

            TreeItem<NamedObject> nroot = new TreeItem<> (type);

            root.getChildren ().add (nroot);

            nroot.getChildren ().addAll (its);

        }

    }

    public static void createNotesSelectableTree (TreeItem         root,
                                                  Project          proj,
                                                  Set<NamedObject> selected,
                                                  Set<NamedObject> exclude)
    {

        Map<String, Set<Note>> allNotes = new TreeMap<> ();

        for (Book b : proj.getBooks ())
        {

            for (Chapter c : b.getChapters ())
            {

                for (Note n : c.getNotes ())
                {

                    String t = n.getType ();

                    if (t == null)
                    {

                        // TODO Handle?
                        continue;

                    }

                    Set<Note> l = allNotes.get (t);

                    if (l == null)
                    {

                        l = new TreeSet (new ChapterItemSorter ());

                        allNotes.put (t,
                                      l);

                    }

                    l.add (n);

                }

            }

        }

        root.getChildren ().addAll (allNotes.keySet ().stream ()
            .map (t ->
            {

                BlankNamedObject bdo = new BlankNamedObject (Note.OBJECT_TYPE,
                                                             t);

                TreeItem<NamedObject> nroot = new TreeItem<> (bdo);

                nroot.getChildren ().addAll (allNotes.get (t).stream ()
                    .filter (n -> (exclude == null ? true : !exclude.contains (n)))
                    .map (n ->
                    {

                        CheckBoxTreeItem<NamedObject> ni = new CheckBoxTreeItem<> (n);
                        ni.setSelected (selected.contains (n));
                        return ni;

                    })
                    .collect (Collectors.toList ()));

                return nroot;

            })
            .collect (Collectors.toList ()));

    }

    public static void createNotesTree (TreeItem         root,
                                        Project          proj,
                                        Set<NamedObject> limitTo)
    {

        Map<String, Set<Note>> allNotes = new TreeMap<> ();

        for (Book b : proj.getBooks ())
        {

            for (Chapter c : b.getChapters ())
            {

                for (Note n : c.getNotes ())
                {

                    String t = n.getType ();

                    if (t == null)
                    {

                        // TODO Handle?
                        continue;

                    }

                    Set<Note> l = allNotes.get (t);

                    if (l == null)
                    {

                        l = new TreeSet (new ChapterItemSorter ());

                        allNotes.put (t,
                                      l);

                    }

                    l.add (n);

                }

            }

        }

        root.getChildren ().addAll (allNotes.keySet ().stream ()
            .map (t ->
            {

                List<TreeItem<NamedObject>> its = allNotes.get (t).stream ()
                    .filter (n -> (limitTo == null ? true : limitTo.contains (n)))
                    .map (n ->
                    {

                        TreeItem<NamedObject> ni = new TreeItem<> (n);
                        return ni;

                    })
                    .collect (Collectors.toList ());

                if (its.size () > 0)
                {

                    BlankNamedObject bdo = new BlankNamedObject (Note.OBJECT_TYPE,
                                                                 t);

                    TreeItem<NamedObject> nroot = new TreeItem<> (bdo);

                    nroot.getChildren ().addAll (its);

                    return nroot;

                }

                return null;

            })
            .filter (ti -> ti != null)
            .collect (Collectors.toList ()));

    }

    public static CustomMenuItem createCompressedMenuItem (StringProperty title,
                                                           List<Node>...  rows)
    {

        HBox _row = new HBox ();
        _row.getChildren ().add (QuollLabel.builder ()
            .label (title)
            .styleClassName (StyleClassNames.COMPRESSEDMENUTITLE)
            .build ());

        CustomMenuItem mi = new CustomMenuItem (_row);
        mi.getStyleClass ().add ("compressedmenu");

        VBox chItemRows = new VBox ();
        _row.getChildren ().add (chItemRows);

        for (List<Node> r : rows)
        {

            if ((r == null)
                ||
                (r.size () == 0)
               )
            {

                continue;

            }

            ToolBar tb = new ToolBar ();
            tb.getStyleClass ().add (StyleClassNames.TOOLBAR);

            tb.getItems ().addAll (r);

            chItemRows.getChildren ().add (tb);

        }

        return mi;

    }

    public static boolean clipboardHasContent ()
    {

        return Clipboard.getSystemClipboard ().hasString ();

    }

    public static ColorSelectorSwatch createColorSelectorSwatch (AbstractViewer  viewer,
                                                  String          popupId,
                                                  StringProperty  popupTitle,
                                                  Color           initColor,
                                                  Consumer<Color> onColorSelected)
    {

        return ColorSelectorSwatch.builder ()
            .inViewer (viewer)
            .popupTitle (popupTitle)
            .initialColor (initColor)
            .onColorSelected (onColorSelected)
            .build ();
/*
        Region swatch = new Region ();
        swatch.setBackground (new Background (new BackgroundFill (initColor, null, null)));
        swatch.getStyleClass ().add (StyleClassNames.COLORSWATCH);

        swatch.setOnMouseClicked (ev ->
        {

            if (ev.getButton () != MouseButton.PRIMARY)
            {

                return;

            }

            QuollPopup qp = viewer.getPopupById (popupId);

            if (qp != null)
            {

                qp.toFront ();
                return;

            }

            ColorChooserPopup p = new ColorChooserPopup (viewer,
                                                         initColor,
                                                         true);
            p.getPopup ().setTitle (popupTitle);
            p.getPopup ().setPopupId (popupId);
            p.getChooser ().setOnColorSelected (eev ->
            {

                Color c = p.getChooser ().colorProperty ().getValue ();

                swatch.setBackground (new Background (new BackgroundFill (c, null, null)));
                p.close ();

                onColorSelected.accept (c);

            });

            p.show ();

        });

        return swatch;
*/
    }

    public static ComboBox<Font> getFontSelector (AbstractViewer viewer,
                                                  Font           def)
    {

        ComboBox<Font> font = new ComboBox<> ();
        font.getStyleClass ().add (StyleClassNames.FONTFAMILY);
        font.setEditable (false);
        Callback<ListView<Font>, ListCell<Font>> fact = lv ->
        {

            return new ListCell<Font> ()
            {

                @Override
                protected void updateItem (Font f, boolean empty)
                {

                    super.updateItem (f, empty);

                    if (f == null)
                    {

                        return;

                    }

                    this.setText (f.getName ());
                    this.setFont (f);

                }

            };

        };

        viewer.schedule (() ->
        {

            ObservableList<Font> fs = FXCollections.observableList (Font.getFamilies ().stream ()
                .map (n ->
                {

                    try
                    {

                        Font f = Font.font (n);

                        if (f == null)
                        {

                            return null;

                        }

                        return f;

                    } catch (Exception e) {

                        return null;

                    }

                })
                .filter (n -> n != null)
                .collect (Collectors.toList ()));

            UIUtils.runLater (() ->
            {

                fs.add (0,
                        Font.getDefault ());

                font.setItems (fs);

                font.getSelectionModel ().select (def);

            });

        },
        -1,
        -1);

        font.setButtonCell (fact.call (null));
        font.setCellFactory (fact);

        return font;

    }

    public static String markupText (StringWithMarkup      text,
                                     AbstractProjectViewer viewer,
                                     NamedObject           ignore)
    {

        String t = text.getMarkedUpText ();

        t = UIUtils.markupLinks (t);

        if (viewer != null)
        {

            t = UIUtils.formatObjectNamesAsLinks (viewer,
                                                  ignore,
                                                  t);

        }

        return t;

    }

    public static QuollTextView getAsBulletPoints (StringWithMarkup text)
    {

        return UIUtils.getAsBulletPoints (text,
                                          null,
                                          null);

    }

    public static QuollTextView getAsText (StringWithMarkup      text,
                                           AbstractProjectViewer viewer,
                                           NamedObject           ignore)
    {

        String t = UIUtils.markupText (text,
                                       viewer,
                                       ignore);

        QuollTextView tv = QuollTextView.builder ()
           //.withViewer (viewer)
           .text (t)
           .build ();

        return tv;

    }

    public static String formatAsHtmlBulletPoints (StringWithMarkup      text,
                                                   AbstractProjectViewer viewer,
                                                   NamedObject           ignore)
    {

        Markup m = text.getMarkup ();
        TextIterator iter = new TextIterator (text.getText ());

        return iter.getParagraphs ().stream ()
                .map (p ->
                {

                    StringWithMarkup sm = new StringWithMarkup (p.getText (),
                                                                new Markup (m,
                                                                            p.getAllTextStartOffset (),
                                                                            p.getAllTextEndOffset ()));

                    String t = UIUtils.markupText (sm,
                                                   viewer,
                                                   ignore);

                    return String.format ("<li>%1$s</li>",
                                          t);

                })
                .collect (Collectors.joining (""));

    }

    public static QuollTextView getAsBulletPoints (StringWithMarkup      text,
                                                   AbstractProjectViewer viewer,
                                                   NamedObject           ignore)
    {

        Markup m = text.getMarkup ();
        TextIterator iter = new TextIterator (text.getText ());

        QuollTextView tv = QuollTextView.builder ()
            //.withViewer (viewer)
            .text (UIUtils.formatAsHtmlBulletPoints (text,
                                                     viewer,
                                                     ignore))
            .build ();

        return tv;

    }

    // Replace [icon,|;action] with <img src="icon" /><a href="action:[action]"><img src="icon" /></a>
    // TODO Is this used for the tips?
    public static String markupHelpTextActions (String text)
    {

        int ind = text.indexOf ("[");

        while (ind > -1)
        {

          int end = text.indexOf ("]",
                                  ind + 1);

          if (end < 0)
          {

              ind = text.indexOf ("[",
                                  ind + 1);

              continue;

          }

          if (end > ind + 1)
          {

              String v = text.substring (ind + 1,
                                         end);

              StringTokenizer st = new StringTokenizer (v,
                                                        ",;");

              String icon = st.nextToken ().trim ().toLowerCase ();
              String action = null;

              if (st.hasMoreTokens ())
              {

                  action = st.nextToken ().trim ().toLowerCase ();

              }

              v = "<img src=\"" + icon + "\" />";

              if (action != null)
              {

                  v = "<a href=\"action:" + action + "\">" + v + "</a>";

              }

              // Split up the value.
              text = text.substring (0,
                                     ind) + v + text.substring (end + 1);

              ind = text.indexOf ("[",
                                  ind + v.length ());


          }

        }

        return text;

    }

    public static String markupLinks (String s)
    {

        if (s == null)
        {

            return s;

        }

        //s = Environment.replaceObjectNames (s);

        s = UIUtils.markupLinks ("http://",
                                 s);

        s = UIUtils.markupLinks ("https://",
                                 s);

        return s;

    }

    public static String markupLinks (String urlPrefix,
                                      String s)
    {

        int ind = 0;

        while (true)
        {

            ind = s.indexOf (urlPrefix,
                             ind);

            if (ind != -1)
            {

                // Now check the previous character to make sure it's not " or '.
                if (ind > 0)
                {

                    char c = s.charAt (ind - 1);

                    if ((c == '"') ||
                        (c == '\''))
                    {

                        ind += 1;

                        continue;

                    }

                }

                // Find the first whitespace char after...
                char[] chars = s.toCharArray ();

                StringBuilder b = new StringBuilder ();

                for (int i = ind + urlPrefix.length (); i < chars.length; i++)
                {

                    if ((!Character.isWhitespace (chars[i]))
                        &&
                        (chars[i] != '<')
                       )
                    {

                        b.append (chars[i]);

                    } else {

                        break;

                    }

                }

                // Now replace whatever we got...
                String st = b.toString ().trim ();

                if ((st.length () == 0)
                    ||
                    (st.equals (urlPrefix))
                   )
                {

                    ind = ind + urlPrefix.length ();

                    continue;

                }

                // Not sure about this but "may" be ok.
                if ((st.endsWith (";")) ||
                    (st.endsWith (",")) ||
                    (st.endsWith (".")))
                {

                    st = st.substring (0,
                                       st.length () - 1);

                }

                String w = null;

                try
                {

                    w = "<a href='" + urlPrefix + st /*URLEncoder.encode (st, "utf-8")*/ + "'>" + urlPrefix + st + "</a>";

                } catch (Exception e) {

                    // Won't happen.

                }

                StringBuilder ss = new StringBuilder (s);

                s = ss.replace (ind,
                                ind + st.length () + urlPrefix.length (),
                                w).toString ();

                ind = ind + w.length ();

            } else
            {

                // No more...
                break;

            }

        }

        return s;

    }

    public static String formatObjectNamesAsLinks (AbstractProjectViewer viewer,
                                                   NamedObject           ignore,
                                                   String                t)
    {

        if ((t == null)
            ||
            (t.trim ().equals (""))
           )
        {

            return t;

        }

        Project p = viewer.getProject ();

        Set<NamedObject> objs = p.getAllNamedChildObjects (Asset.class);

        Set<NamedObject> chaps = p.getAllNamedChildObjects (Chapter.class);

        if (chaps.size () > 0)
        {

            objs.addAll (chaps);

        }

        if (objs.size () == 0)
        {

            return t;

        }

        if (ignore != null)
        {

            objs.remove (ignore);

        }

        Map<String, List<NamedObjectNameWrapper>> items = new HashMap<> ();

        for (NamedObject o : objs)
        {

            NamedObjectNameWrapper.addTo (o,
                                          items);

        }

        NavigableMap<Integer, NamedObjectNameWrapper> reps = new TreeMap ();

        TextIterator ti = new TextIterator (t);

        for (NamedObject n : objs)
        {

            Set<Integer> matches = null;

            for (String name : n.getAllNames ())
            {

                matches = ti.findAllTextIndexes (name,
                                                 null);

                // TODO: This needs to be on a language basis.
                Set<Integer> matches2 = ti.findAllTextIndexes (name + "'s",
                                                               null);

                matches.addAll (matches2);

                Iterator<Integer> iter = matches.iterator ();

                while (iter.hasNext ())
                {

                    Integer ind = iter.next ();

                    // Now search back through the string to make sure
                    // we aren't actually part of a http or https string.
                    int httpInd = t.lastIndexOf ("http://",
                                                 ind);
                    int httpsInd = t.lastIndexOf ("https://",
                                                  ind);

                    if ((httpInd > -1)
                        ||
                        (httpsInd > -1)
                       )
                    {

                        // Check forward to ensure there is no white space.
                        String ss = t.substring (Math.max (httpInd, httpsInd),
                                                 ind);

                        boolean hasWhitespace = false;

                        char[] chars = ss.toCharArray ();

                        for (int i = 0; i < chars.length; i++)
                        {

                            if (Character.isWhitespace (chars[i]))
                            {

                                hasWhitespace = true;

                                break;

                            }

                        }

                        if (!hasWhitespace)
                        {

                            // This name is part of a http/https link so ignore.
                            continue;

                        }

                    }

                    // Check the char at the index, if it's uppercase then we upper the word otherwise lower.
                    if (Character.isLowerCase (t.charAt (ind)))
                    {

                        reps.put (ind,
                                  new NamedObjectNameWrapper (name.toLowerCase (),
                                                              n));

                    } else {

                        // Uppercase each of the words in the name.
                        reps.put (ind,
                                  new NamedObjectNameWrapper (TextUtilities.capitalize (name),
                                                              n));

                    }

                }

            }

        }

        NavigableMap<Integer, NamedObjectNameWrapper> nreps = new TreeMap ();

        List<Integer> mis = new ArrayList (reps.keySet ());

        // Sort by location.
        Collections.sort (mis);

        // Prune out the overlaps.
        for (int i = 0; i < mis.size (); i++)
        {

            Integer curr = mis.get (i);

            NamedObjectNameWrapper wrap = reps.get (curr);

            nreps.put (curr,
                       wrap);

            int ni = i + 1;

            if (ni < mis.size ())
            {

                Integer next = mis.get (ni);

                // Does the next match start before the end of the current match?
                if (next.intValue () <= curr.intValue () + wrap.name.length ())
                {

                    // Move to the next, when the loop goes to the next it will
                    // increment again moving past it.
                    i = ni;

                }

            }

        }

        reps = nreps;

        StringBuilder b = new StringBuilder (t);

        // We iterate backwards over the indexes so we don't have to choppy-choppy the string.
        Iterator<Integer> iter = reps.descendingKeySet ().iterator ();

        while (iter.hasNext ())
        {

            Integer ind = iter.next ();

            NamedObjectNameWrapper obj = reps.get (ind);

            String w = obj.name;

            try
            {

                w = URLEncoder.encode (obj.name, "utf-8");

            } catch (Exception e) {

                // Ignore.

            }

            b = b.replace (ind,
                           ind + obj.name.length (),
                           String.format ("<a href='%s://%s'>%s</a>",
                                          Constants.OBJECTNAME_PROTOCOL,
                                          w,
                                          obj.name));

/*
TODO
            editor.setAsLink (ind,
                              ind + obj.name.length (),
                              text ->
                              {

                                  Set<Asset> _objs = viewer.getProject ().getAllAssetsByName (obj.name,
                                                                                              null);

                                  if ((_objs == null)
                                      ||
                                      (_objs.size () == 0)
                                     )
                                  {

                                      return;

                                  }

                                  if (_objs.size () == 1)
                                  {

                                      viewer.viewObject (_objs.iterator ().next ());

                                      return;

                                  } else {

                                      String popupId = obj.name + "object-select";

                                      if (viewer.getPopupById (popupId) != null)
                                      {

                                          return;

                                      }

                                      QuollPopup.objectSelectBuilder ()
                                        .onClick (vobj -> viewer.viewObject (vobj))
                                        .popupId (popupId)
                                        .objects (_objs)
                                        .build ();// TODO .showAt (text,
                                                          // Side.BOTTOM);

                                  }

                              });
*/
        }

        return b.toString ();

    }

    public static boolean isImageFile (File f)
    {

        if (f == null)
        {

            return false;

        }

        return UIUtils.isImageFile (f.toPath ());

    }

    public static boolean isImageFile (Path f)
    {

        if (f == null)
        {

            return false;

        }

        if (Files.isDirectory (f))
        {

            return false;

        }

        String fn = f.getFileName ().toString ().toLowerCase ();

        String s = UIUtils.imageFileFilter.getExtensions ().stream ()
            .filter (ext -> fn.endsWith (ext.toLowerCase ().substring (2)))
            .findFirst ()
            .orElse (null);

        return s!= null;

    }

    public static void showContextMenu (Node          n,
                                        Set<MenuItem> items,
                                        double        x,
                                        double        y)
    {

        ContextMenu cm = new ContextMenu ();
        cm.getItems ().addAll (items);
        n.getProperties ().put ("context-menu",
                                cm);
        cm.setAutoFix (true);
        cm.setAutoHide (true);
        cm.setHideOnEscape (true);
        cm.show (n,
                 x,
                 y);
        UIUtils.addShowCSSViewerFilter (cm);

        n.getScene ().addEventFilter (MouseEvent.MOUSE_PRESSED,
                           eev ->
        {

            Object o = n.getProperties ().get ("context-menu");

            if (o != null)
            {

                ContextMenu _cm = (ContextMenu) o;
                _cm.hide ();

            }

        });

    }

    public static void makeDraggable (ScrollPane sp)
    {

        sp.setOnDragOver (ev ->
        {

            // TODO This needs more work.
            double diffy = 1d - ((sp.getViewportBounds ().getHeight () - 10) / sp.getViewportBounds ().getHeight ());
            double diffx = 1d - ((sp.getViewportBounds ().getWidth () - 10) / sp.getViewportBounds ().getWidth ());

            if (ev.getY () <= 100)
            {

                sp.setVvalue (sp.getVvalue () - diffy);

            }

            if (ev.getY () >= sp.getViewportBounds ().getHeight () - 100)
            {

                sp.setVvalue (sp.getVvalue () + diffy);

            }

            if (ev.getX () <= 100)
            {

                sp.setHvalue (sp.getHvalue () - diffx);

            }

            if (ev.getX () >= sp.getViewportBounds ().getWidth () - 100)
            {

                sp.setHvalue (sp.getHvalue () + diffx);

            }

        });

    }

    public static void addStyleSheet (Parent parent,
                                      String type,
                                      String... name)
    {

        if (name != null)
        {

            for (String n : name)
            {

                try
                {

                    URL u = Utils.getResourceUrl (String.format (Constants.STYLESHEET_PATH,
                                                                 type,
                                                                 n));

                    if (u != null)
                    {

                        parent.getStylesheets ().add (u.toExternalForm ());

                    }

                } catch (Exception e) {

                    Environment.logError (String.format ("Unable to get/apply stylesheet for: %1$s, type: %2$s, name: %3$s",
                                                         parent,
                                                         type,
                                                         n),
                                          e);

                }

            }

        }

    }

    public static void addStyleSheet (Scene parent,
                                      String type,
                                      String... name)
    {

        if (name != null)
        {

            for (String n : name)
            {

                try
                {

                    URL u = Utils.getResourceUrl (String.format (Constants.STYLESHEET_PATH,
                                                                 type,
                                                                 n));

                    if (u != null)
                    {

                        parent.getStylesheets ().add (u.toExternalForm ());

                    }

                } catch (Exception e) {

                    Environment.logError (String.format ("Unable to get/apply stylesheet for: %1$s, type: %2$s, name: %3$s",
                                                         parent,
                                                         type,
                                                         n),
                                          e);

                }

            }

        }

    }

    public static void removeStyleSheet (Parent parent,
                                         String type,
                                         String name)
    {

        if (name != null)
        {

            try
            {

                URL u = Utils.getResourceUrl (String.format (Constants.STYLESHEET_PATH,
                                                             type,
                                                             name));

                if (u != null)
                {

                    parent.getStylesheets ().remove (u.toExternalForm ());

                }

            } catch (Exception e) {

                Environment.logError (String.format ("Unable to remove stylesheet for: %1$s, type: %2$s, name: %3$s",
                                                     parent,
                                                     type,
                                                     name),
                                      e);

            }

        }

    }

    public static void showFeatureComingSoonPopup ()
    {

        QuollPopup.messageBuilder ()
            .styleClassName (StyleClassNames.WARNING)
            .title (new SimpleStringProperty ("Coming soon!"))
            .message (new SimpleStringProperty ("This fantastic feature is coming soon!  I promise."))
            .closeButton ()
            .build ();

    }

    public static void setBackgroundImage (Region r,
                                           Image  p)
    {

        Background _b = new Background (new BackgroundImage (p,
                                                             BackgroundRepeat.NO_REPEAT,
                                                             BackgroundRepeat.NO_REPEAT,
                                                             null,
                                                             new BackgroundSize (BackgroundSize.AUTO,
                                                                                 BackgroundSize.AUTO,
                                                                                 false,
                                                                                 false,
                                                                                 true,
                                                                                 false)));
        r.setBackground (_b);

    }

    public static void setBackgroundImage (Region                r,
                                           ObjectProperty<Image> p,
                                           IPropertyBinder       b)
    {

        Background _b = new Background (new BackgroundImage (p.getValue (),
                                                             BackgroundRepeat.NO_REPEAT,
                                                             BackgroundRepeat.NO_REPEAT,
                                                             null,
                                                             new BackgroundSize (BackgroundSize.AUTO,
                                                                                 BackgroundSize.AUTO,
                                                                                 false,
                                                                                 false,
                                                                                 true,
                                                                                 false)));
        r.setBackground (_b);

        b.addChangeListener (p,
                             (pr, oldv, newv) ->
        {

            Background bb = new Background (new BackgroundImage (p.getValue (),
                                                                 BackgroundRepeat.NO_REPEAT,
                                                                 BackgroundRepeat.NO_REPEAT,
                                                                 null,
                                                                 new BackgroundSize (BackgroundSize.AUTO,
                                                                                     BackgroundSize.AUTO,
                                                                                     false,
                                                                                     false,
                                                                                     true,
                                                                                     false)));
            r.setBackground (bb);

        });

    }

    public static void addUnifiedMousePressHandler (Node                     l,
                                                    EventHandler<MouseEvent> handler)
    {

        l.addEventHandler (MouseEvent.MOUSE_PRESSED,
                           ev ->
        {

            if (ev.isPopupTrigger ())
            {

                return;

            }

            handler.handle (ev);

        });

        l.addEventHandler (MouseEvent.MOUSE_RELEASED,
                           ev ->
        {

            if (ev.isPopupTrigger ())
            {

                return;

            }

            handler.handle (ev);

        });

    }

    public static void setButtonId (Node   n,
                                    String id)
    {

        n.getProperties ().put ("buttonId",
                                id);

    }

    public static String getButtonId (Node n)
    {

        if (n == null)
        {

            return null;

        }

        Object o = n.getProperties ().get ("buttonId");

        if (o != null)
        {

            return o.toString ();

        }

        return null;

    }

    public static void addShowCSSViewerFilter (Window s)
    {

        if (s == null)
        {

            return;

        }

        if (s.getProperties ().containsKey ("qw-css-event-filter"))
        {

            return;

        }

        s.getScene ().addEventFilter (javafx.scene.input.MouseEvent.MOUSE_RELEASED,
                                      eev ->
        {

            if (!Environment.isDebugModeEnabled ())
            {

                return;

            }

            if (!eev.isShortcutDown ())
            {

                return;

            }

            try
            {

                eev.consume ();

                Environment.showCSSViewer (s,
                                           (Node) eev.getTarget ());

            } catch (Exception e) {

                Environment.logError ("Unable to show css viewer for node: " + eev.getTarget (),
                                      e);

            }

        });

        s.getProperties ().put ("qw-css-event-filter", "added");

    }

    public static int getDefaultWarmupMinutes ()
    {

        String minsDef = UserProperties.get (Constants.DEFAULT_WARMUP_MINS_PROPERTY_NAME);

        int minsC = Constants.DEFAULT_MINS;

        if (minsDef != null)
        {

            minsC = Integer.parseInt (minsDef);

        }

        return minsC;

    }

    public static int getDefaultWarmupWords ()
    {

        String v = UserProperties.get (Constants.DEFAULT_WARMUP_WORDS_PROPERTY_NAME);

        try
        {

            return Integer.parseInt (v);

        } catch (Exception e)
        {

            return Constants.DEFAULT_WORDS;

        }

    }

    public static void setFirstLastPseudoClasses (Parent p,
                                                  String selector)
    {

        Set<Node> nodes = p.lookupAll (selector);

        Node _n = null;

        for (Node n : nodes)
        {

            if (_n == null)
            {

                _n = n;
                _n.pseudoClassStateChanged (StyleClassNames.FIRST_PSEUDO_CLASS, true);

            }

        }

        if (_n != null)
        {

            _n.pseudoClassStateChanged (StyleClassNames.LAST_PSEUDO_CLASS, true);

        }

    }

    public static void setFirstLastPseudoClasses (Parent p)
    {

        List<Node> nodes = p.getChildrenUnmodifiable ();

        if (nodes.size () > 0)
        {

            Node n = nodes.get (0);

            n.pseudoClassStateChanged (StyleClassNames.FIRST_PSEUDO_CLASS, true);

            n = nodes.get (nodes.size () - 1);

            n.pseudoClassStateChanged (StyleClassNames.LAST_PSEUDO_CLASS, true);

        }

    }

}
