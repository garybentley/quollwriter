package com.quollwriter.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import java.awt.GraphicsDevice;
import java.awt.GraphicsConfiguration;
import java.awt.FontMetrics;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.image.*;
import java.beans.*;
import javax.swing.plaf.basic.*;

import java.util.concurrent.atomic.*;

import java.net.*;

import java.io.*;

import java.text.*;

import java.util.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.tree.*;
import javax.swing.table.*;
import javax.swing.filechooser.*;

import javax.imageio.*;
import javax.activation.*;

//import javafx.scene.*;
import javafx.scene.web.*;
import javafx.scene.layout.BorderPane;
import javafx.application.*;
import javafx.embed.swing.*;

import com.gentlyweb.utils.StringUtils;

import org.imgscalr.Scalr;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import org.josql.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;

import com.quollwriter.events.*;

import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.*;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.ui.events.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.sidebars.*;
import com.quollwriter.ui.userobjects.*;
import com.quollwriter.editors.messages.*;
import com.quollwriter.editors.*;
import com.quollwriter.synonyms.*;
import com.quollwriter.db.*;
import com.quollwriter.ui.forms.Form;
import com.quollwriter.ui.forms.FormItem;

import com.quollwriter.text.*;

import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.labels.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;

import org.jfree.data.time.*;

import org.jfree.ui.*;

import org.josql.utils.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public class UIUtils
{

    private static int DEFAULT_ASSUMED_SCREEN_RESOLUTION = 96;
    private static int DEFAULT_POPUP_WIDTH = 450;
    public static int DEFAULT_SCROLL_BY_AMOUNT = 20;

    public static final Point defaultLeftCornerShowPopupAt = new Point (10, 10);

    private static final QTextEditor wordCountPerPageEditor = new QTextEditor (null,
                                                                               false);

    // Deliberately null so that the center of the window is found when the popup is shown.
    public static final Point defaultCenterShowPopupAt = null;

    public static Font headerFont = null;

    public static final Border textFieldSpacing = new EmptyBorder (3,
                                                                   3,
                                                                   3,
                                                                   3);

    public static final FileNameExtensionFilter imageFileFilter = new FileNameExtensionFilter ("Image files: jpg, png, gif",
                                                                                               "jpg",
                                                                                               "jpeg",
                                                                                               "gif",
                                                                                               "png");

    public static int getScrollByAmount ()
    {

        return DEFAULT_SCROLL_BY_AMOUNT;

    }

    public static int getScreenScaledWidth (int w)
    {

        return Math.round ((float) w * ((float) java.awt.Toolkit.getDefaultToolkit ().getScreenResolution () / (float) DEFAULT_ASSUMED_SCREEN_RESOLUTION));

    }

    public static int getPopupWidth ()
    {

        return UIUtils.getScreenScaledWidth (DEFAULT_POPUP_WIDTH);

    }

    public static Object getUserObjectForTreePath (TreePath p)
    {

        if (p == null)
        {

            return null;

        }

        Object[] objs = p.getPath ();

        DefaultMutableTreeNode n = (DefaultMutableTreeNode) objs[objs.length -1];

        return n.getUserObject ();

    }

    public static TreePath getTreePathForUserObjects (DefaultMutableTreeNode node,
                                                      TreePath               p)
    {

        Object[] objs = p.getPath ();

        for (int i = 0; i < objs.length; i++)
        {

            DefaultMutableTreeNode n = (DefaultMutableTreeNode) objs[i];

            Object nObj = n.getUserObject ();

            TreePath tp = UIUtils.getTreePathForUserObject (node,
                                                            nObj);

            if (tp == null)
            {

                continue;

            } else {

                if (i == objs.length - 1)
                {

                    return tp;

                }

            }

            node = (DefaultMutableTreeNode) tp.getPath ()[tp.getPath ().length - 1];

        }

        return null;

    }

    public static TreePath getTreePathForUserObject (DefaultMutableTreeNode node,
                                                     Object                 o)
    {

        Object nObj  = node.getUserObject ();

        if (nObj instanceof SelectableDataObject)
        {

            nObj = ((SelectableDataObject) nObj).obj;

        }

        if (nObj instanceof DataObject)
        {

            if (nObj.equals (o))
            {

                return new TreePath (node.getPath ());

            }

        }

        if ((nObj instanceof Segment)
            &&
            (o instanceof Segment)
           )
        {

            if (nObj.toString ().equals (o.toString ()))
            {

                return new TreePath (node.getPath ());

            }

        }

        if (nObj == o)
        {

            return new TreePath (node.getPath ());

        }

        Enumeration en = node.children ();

        while (en.hasMoreElements ())
        {

            node = (DefaultMutableTreeNode) en.nextElement ();

            TreePath tp = UIUtils.getTreePathForUserObject (node,
                                                            o);

            if (tp != null)
            {

                return tp;

            }

        }

        return null;

    }

    public static void createTree (Collection             items,
                                   DefaultMutableTreeNode parent)
    {

        Iterator iter = items.iterator ();

        while (iter.hasNext ())
        {

            Object v = iter.next ();

            if (v instanceof Map)
            {

                UIUtils.createTree ((Map) v,
                                    parent);

                continue;

            }

            if (v instanceof Collection)
            {

                UIUtils.createTree ((Collection) v,
                                    parent);

                continue;

            }

            DefaultMutableTreeNode n = new DefaultMutableTreeNode (v);

            parent.add (n);

        }

    }

    public static void createTree (Map                    items,
                                   DefaultMutableTreeNode parent)
    {

        Iterator iter = items.entrySet ().iterator ();

        while (iter.hasNext ())
        {

            Map.Entry item = (Map.Entry) iter.next ();

            Object k = item.getKey ();

            Object v = item.getValue ();

            // Add a node for the key.
            DefaultMutableTreeNode key = new DefaultMutableTreeNode (k);

            parent.add (key);

            if (v instanceof Map)
            {

                UIUtils.createTree ((Map) v,
                                    key);

            }

            if (v instanceof Collection)
            {

                UIUtils.createTree ((Collection) v,
                                    key);

            }

        }

    }

    public static DefaultMutableTreeNode createAssetTree (UserConfigurableObjectType  objType,
                                                          Project                     p)
    {

        return UIUtils.createAssetTree (objType,
                                        p,
                                        new Comparator<Asset> ()
        {

            @Override
            public boolean equals (Object o)
            {

                return this == o;

            }

            @Override
            public int compare (Asset o1,
                                Asset o2)
            {

                return NamedObjectSorter.getInstance ().compare (o1, o2);

            }

        });

    }

    public static DefaultMutableTreeNode createAssetTree (UserConfigurableObjectType        objType,
                                                          Project                           p,
                                                          Comparator<Asset> sorter)
    {

        List<Asset> objs = new ArrayList ();

        Set<Asset> assets = p.getAssets (objType);

        if (assets != null)
        {

            objs.addAll (assets);

        }

        DefaultMutableTreeNode root = new DefaultMutableTreeNode (p);

        Collections.sort (objs,
                          sorter);

        for (NamedObject obj : objs)
        {

            root.add (new DefaultMutableTreeNode (obj));

        }

        return root;

    }
/*
    public static DefaultMutableTreeNode createLocationsTree (Project p)
    {

        DefaultMutableTreeNode root = new DefaultMutableTreeNode (p);

        Collections.sort (p.getLocations (),
                          new NamedObjectSorter ());

        for (Location ll : p.getLocations ())
        {

            root.add (new DefaultMutableTreeNode (ll));

        }

        return root;

    }
  */
/*
    public static DefaultMutableTreeNode createAssetsTree (Project p)
    {

        DefaultMutableTreeNode root = new DefaultMutableTreeNode (p);

        TreeParentNode c = new TreeParentNode (QCharacter.OBJECT_TYPE,
                                               Environment.getObjectTypeNamePlural (QCharacter.OBJECT_TYPE));

        DefaultMutableTreeNode tn = new DefaultMutableTreeNode (c);

        root.add (tn);

        Collections.sort (p.getCharacters (),
                          new NamedObjectSorter ());

        for (int i = 0; i < p.getCharacters ().size (); i++)
        {

            tn.add (new DefaultMutableTreeNode (p.getCharacters ().get (i)));

        }

        TreeParentNode l = new TreeParentNode (Location.OBJECT_TYPE,
                                               Environment.getObjectTypeNamePlural (Location.OBJECT_TYPE));

        tn = new DefaultMutableTreeNode (l);

        Collections.sort (p.getLocations (),
                          new NamedObjectSorter ());

        for (Location ll : p.getLocations ())
        {

            tn.add (new DefaultMutableTreeNode (ll));

        }

        root.add (tn);

        TreeParentNode o = new TreeParentNode (QObject.OBJECT_TYPE,
                                               Environment.getObjectTypeNamePlural (QObject.OBJECT_TYPE));

        tn = new DefaultMutableTreeNode (o);

        Collections.sort (p.getQObjects (),
                          new NamedObjectSorter ());

        for (QObject oo : p.getQObjects ())
        {

            tn.add (new DefaultMutableTreeNode (oo));

        }

        root.add (tn);

        TreeParentNode r = new TreeParentNode (ResearchItem.OBJECT_TYPE,
                                               Environment.getObjectTypeNamePlural (ResearchItem.OBJECT_TYPE));

        tn = new DefaultMutableTreeNode (r);

        Collections.sort (p.getResearchItems (),
                          NamedObjectSorter.getInstance ());

        for (ResearchItem rr : p.getResearchItems ())
        {

            tn.add (new DefaultMutableTreeNode (rr));

        }

        root.add (tn);

        return root;

    }
*/
    public static void getSelectedObjects (DefaultMutableTreeNode n,
                                           NamedObject            addTo,
                                           Set                    s)
                                    throws GeneralException
    {

        Enumeration<DefaultMutableTreeNode> en = n.children ();

        while (en.hasMoreElements ())
        {

            DefaultMutableTreeNode nn = en.nextElement ();

            SelectableDataObject sd = (SelectableDataObject) nn.getUserObject ();

            if (sd.selected)
            {

                s.add (new Link (addTo,
                                 sd.obj));

            }

            UIUtils.getSelectedObjects (nn,
                                        addTo,
                                        s);

        }

    }

    public static void initTable (JTable t)
    {

        t.setAlignmentX (Component.LEFT_ALIGNMENT);
        t.setOpaque (false);
        t.setFillsViewportHeight (true);
        t.setShowVerticalLines (false);
        t.setRowHeight (24);
        t.setBorder (null);

        t.setDefaultRenderer (Object.class,
                              new DefaultTableCellRenderer ()
                              {

                                  @Override
                                  public Component getTableCellRendererComponent (JTable  t,
                                                                                  Object  value,
                                                                                  boolean isSelected,
                                                                                  boolean hasFocus,
                                                                                  int     row,
                                                                                  int     column)
                                  {

                                      super.getTableCellRendererComponent (t,
                                                                           value,
                                                                           isSelected,
                                                                           hasFocus,
                                                                           row,
                                                                           column);

                                      this.setBorder (UIUtils.createPadding (0, 3, 0, 3));

                                      return this;

                                  }

                              });

    }

    public static JTable createTable ()
    {

        JTable t = new JTable ();

        UIUtils.initTable (t);

        return t;

    }

    public static JTree createSelectableTree ()
    {

        final JTree tree = UIUtils.createTree ();

        tree.setCellRenderer (new SelectableProjectTreeCellRenderer ());

        tree.setOpaque (false);
        tree.setBorder (null);
        tree.setBorder (UIUtils.createPadding (5, 5, 5, 5));

        tree.setRootVisible (false);
        tree.setShowsRootHandles (true);
        tree.setScrollsOnExpand (true);

        tree.addMouseListener (new MouseEventHandler ()
        {

            private void selectAllChildren (DefaultTreeModel       model,
                                            DefaultMutableTreeNode n,
                                            boolean                v)
            {

                Enumeration<DefaultMutableTreeNode> en = n.children ();

                while (en.hasMoreElements ())
                {

                    DefaultMutableTreeNode c = en.nextElement ();

                    SelectableDataObject s = (SelectableDataObject) c.getUserObject ();

                    s.selected = v;

                    // Tell the model that something has changed.
                    model.nodeChanged (c);

                    // Iterate.
                    this.selectAllChildren (model,
                                            c,
                                            v);

                }

            }

            @Override
            public void handlePress (MouseEvent ev)
            {

                TreePath tp = tree.getPathForLocation (ev.getX (),
                                                       ev.getY ());

                if (tp != null)
                {

                    DefaultMutableTreeNode n = (DefaultMutableTreeNode) tp.getLastPathComponent ();

                    // Tell the model that something has changed.
                    DefaultTreeModel model = (DefaultTreeModel) tree.getModel ();

                    SelectableDataObject s = (SelectableDataObject) n.getUserObject ();

                    s.selected = !s.selected;

                    model.nodeChanged (n);

                    this.selectAllChildren (model,
                                            n,
                                            s.selected);

                }

            }

        });

        tree.putClientProperty (com.jgoodies.looks.Options.TREE_LINE_STYLE_KEY,
                                com.jgoodies.looks.Options.TREE_LINE_STYLE_NONE_VALUE);

        tree.putClientProperty ("Tree.paintLines",
                                Boolean.FALSE);

        return tree;

    }

    public static JTree createLinkedToTree (final AbstractProjectViewer projectViewer,
                                            final NamedObject           dataObject,
                                            boolean                     editMode)
    {

        final JTree tree = UIUtils.createTree ();

        if (editMode)
        {

            tree.setCellRenderer (new SelectableProjectTreeCellRenderer ());

        } else
        {

            tree.setCellRenderer (new ProjectTreeCellRenderer (true));

        }

        tree.setOpaque (true);
        tree.setBorder (null);

        tree.setRootVisible (false);
        tree.setShowsRootHandles (true);
        tree.setScrollsOnExpand (true);

        if (editMode)
        {

            // Never toggle.
            // tree.setToggleClickCount (-1);

            tree.addMouseListener (new MouseEventHandler ()
                {

                    private void selectAllChildren (DefaultTreeModel       model,
                                                    DefaultMutableTreeNode n,
                                                    boolean                v)
                    {

                        Enumeration<DefaultMutableTreeNode> en = n.children ();

                        while (en.hasMoreElements ())
                        {

                            DefaultMutableTreeNode c = en.nextElement ();

                            SelectableDataObject s = (SelectableDataObject) c.getUserObject ();

                            s.selected = v;

                            // Tell the model that something has changed.
                            model.nodeChanged (c);

                            // Iterate.
                            this.selectAllChildren (model,
                                                    c,
                                                    v);

                        }

                    }

                    @Override
                    public void handlePress (MouseEvent ev)
                    {

                        TreePath tp = tree.getPathForLocation (ev.getX (),
                                                               ev.getY ());

                        if (tp != null)
                        {

                            DefaultMutableTreeNode n = (DefaultMutableTreeNode) tp.getLastPathComponent ();

                            // Tell the model that something has changed.
                            DefaultTreeModel model = (DefaultTreeModel) tree.getModel ();

                            SelectableDataObject s = (SelectableDataObject) n.getUserObject ();

                            /*
                                                if ((ev.getClickCount () == 2)
                                                    &&
                                                    (n.getChildCount () > 0)
                                                   )
                                                {

                                                    this.selectAllChildren (model,
                                                                            n,
                                                                            s.selected);

                                                } else {

                                                    s.selected = !s.selected;

                                                }
                             */
                            s.selected = !s.selected;

                            model.nodeChanged (n);

                        }

                    }

                });

        } else
        {

            tree.addMouseListener (new MouseEventHandler ()
            {

                @Override
                public void handleDoublePress (MouseEvent ev)
                {

                    TreePath tp = tree.getPathForLocation (ev.getX (),
                                                           ev.getY ());

                    if (tp != null)
                    {

                        DefaultMutableTreeNode n = (DefaultMutableTreeNode) tp.getLastPathComponent ();

                        if ((n.getChildCount () == 0) &&
                            (ev.getClickCount () == 2))
                        {

                            projectViewer.viewObject ((DataObject) n.getUserObject ());

                        }

                    }

                }

            });

        }

        tree.putClientProperty (com.jgoodies.looks.Options.TREE_LINE_STYLE_KEY,
                                com.jgoodies.looks.Options.TREE_LINE_STYLE_NONE_VALUE);

        tree.putClientProperty ("Tree.paintLines",
                                Boolean.FALSE);

        tree.setModel (UIUtils.getLinkedToTreeModel (projectViewer,
                                                     dataObject,
                                                     editMode));

        UIUtils.expandPathsForLinkedOtherObjects (tree,
                                                  dataObject);

        return tree;

    }

    public static void scrollIntoView (final JComponent c)
    {

        Environment.scrollIntoView (c);

    }

    public static void expandPathsForLinkedOtherObjects (JTree       tree,
                                                         NamedObject d)
    {

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) ((DefaultTreeModel) tree.getModel ()).getRoot ();

        for (Link l : d.getLinks ())
        {

            // Get the path for the object.
            TreePath tp = UIUtils.getTreePathForUserObject (root,
                                                            l.getOtherObject (d));

            if (tp != null)
            {

                tree.expandPath (tp.getParentPath ());

            }

        }

    }

    public static DefaultTreeModel getLinkedToTreeModel (AbstractProjectViewer projectViewer,
                                                         NamedObject           dataObject,
                                                         boolean               editMode)
    {

        List exclude = new ArrayList ();
        exclude.add (dataObject);

        // Painful but just about the only way.
        projectViewer.setLinks (dataObject);

        // Get all the "other objects" for the links the note has.
        Iterator<Link> it = dataObject.getLinks ().iterator ();

        Set links = new HashSet ();

        while (it.hasNext ())
        {

            links.add (it.next ().getOtherObject (dataObject));

        }

        return new DefaultTreeModel (UIUtils.createLinkToTree (projectViewer.getProject (),
                                                               exclude,
                                                               links,
                                                               editMode));

    }

    public static Box createLinkedToItemsBox (final NamedObject           obj,
                                              final AbstractProjectViewer pv,
                                              final QPopup                p,
                                              boolean                     addTitle)
    {

        Box pa = new Box (BoxLayout.Y_AXIS);
        pa.setOpaque (false);

        if (addTitle)
        {

            JLabel h = new JLabel ("Linked to");
            pa.add (h);
            pa.add (Box.createVerticalStrut (3));

        }

        Set<NamedObject> sl = obj.getOtherObjectsInLinks ();

        Iterator<NamedObject> liter = sl.iterator ();

        while (liter.hasNext ())
        {

            final NamedObject other = liter.next ();

            JLabel l = new JLabel (other.getName ());
            l.setOpaque (false);
            l.setIcon (Environment.getIcon (other.getObjectType (),
                                            Constants.ICON_MENU));
            l.setToolTipText (getUIString (viewitem,tooltip));
            //"Click to view the item");
            l.setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));
            l.setForeground (Color.BLUE);
            l.setBorder (new EmptyBorder (0,
                                          5,
                                          0,
                                          0));

            l.setAlignmentX (Component.LEFT_ALIGNMENT);

            pa.add (l);
            pa.add (Box.createVerticalStrut (3));

            l.addMouseListener (new MouseEventHandler ()
            {

                @Override
                public void handlePress (MouseEvent ev)
                {

                    // Prevents the popup from disappearing.
                    pv.viewObject (other);

                    if (p != null)
                    {

                        // Close the popup.
                        p.setVisible (false);

                    }

                }

            });

        }

        pa.setPreferredSize (new Dimension (380,
                                            pa.getPreferredSize ().height));

        return pa;

    }

    public static QPopup createClosablePopup (final String         title,
                                              final Icon           icon,
                                              final ActionListener onClose,
                                              final Component      content,
                                              final AbstractViewer viewer,
                                                    Point          showAt)
    {

        final QPopup ep = UIUtils.createClosablePopup (title,
                                                       icon,
                                                       onClose);

        Box b = new Box (BoxLayout.Y_AXIS);

        b.setBorder (UIUtils.createPadding (10, 10, 10, 10));

        b.add (content);

        ep.setContent (b);

        b.setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                           b.getPreferredSize ().height));

        if (showAt == null)
        {

            showAt = UIUtils.getCenterShowPosition (viewer,
                                                    ep);

        }

        viewer.showPopupAt (ep,
                            showAt,
                            false);
        ep.setDraggable (viewer);

        return ep;

    }

    public static QPopup createClosablePopup (final String         title,
                                              final Icon           icon,
                                              final ActionListener onClose)
    {

        final QPopup qp = new QPopup (Environment.replaceObjectNames (title),
                                      icon,
                                      null)
        {

            public void setVisible (boolean v)
            {

                if ((!v)
                    &&
                    (onClose != null)
                   )
                {

                    onClose.actionPerformed (new ActionEvent (this,
                                                              0,
                                                              "closing"));

                }

                super.setVisible (v);

            }

        };

        JButton close = UIUtils.createButton (Constants.CLOSE_ICON_NAME,
                                              Constants.ICON_MENU,
                                              getUIString (actions,clicktoclose),
                                              //"Click to close",
                                              new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                qp.removeFromParent ();

            }

        });

        List<JButton> buts = new ArrayList ();
        buts.add (close);

        qp.getHeader ().setControls (UIUtils.createButtonBar (buts));

        return qp;

    }

    public static DefaultMutableTreeNode createChapterNotesNode (Chapter c)
    {

        if (!UserProperties.getAsBoolean (Constants.SHOW_NOTES_IN_CHAPTER_LIST_PROPERTY_NAME))
        {

            return null;

        }

        Set<Note> notes = c.getNotes ();

        if ((notes == null)
            ||
            (notes.size () == 0)
           )
        {

            return null;

        }

        TreeParentNode nullN = new TreeParentNode (Note.OBJECT_TYPE,
                                                   getUIString (objectnames,plural, Note.OBJECT_TYPE),
                                                   //Environment.replaceObjectNames ("{Notes}"),
                                                   notes.size ());

        DefaultMutableTreeNode root = new DefaultMutableTreeNode (nullN);

        for (Note n : c.getNotes ())
        {

            DefaultMutableTreeNode node = UIUtils.createTreeNode (n,
                                                                  null,
                                                                  null,
                                                                  false);

            if (node == null)
            {

                continue;

            }

            root.add (node);

        }

        if ((c.getNotes () == null)
            ||
            (c.getNotes ().size () == 0)
           )
        {

            return null;

        }

        return root;

    }

    public static DefaultMutableTreeNode createNoteTree (ProjectViewer pv)
    {

        DefaultMutableTreeNode root = new DefaultMutableTreeNode (pv.getProject ());

        Map<String, Set<Note>> typeNotes = pv.getNotesAgainstTypes ();

        Map<String, DefaultMutableTreeNode> noteNodes = new HashMap ();

        TreeParentNode nullN = new TreeParentNode (Note.OBJECT_TYPE,
                                                   "No Type");

        DefaultMutableTreeNode nullNode = new DefaultMutableTreeNode (nullN);

        noteNodes.put (null,
                       nullNode);

        Set<String> types = typeNotes.keySet ();

        for (String type : types)
        {

            boolean added = false;

            Set<Note> notes = typeNotes.get (type);

            for (Note n : notes)
            {

                String t = n.getType ();

                DefaultMutableTreeNode noteNode = null;

                if ((t == null) ||
                    (t.equals ("")))
                {

                    noteNode = noteNodes.get (null);

                } else
                {

                    if (t.equals (type))
                    {

                        if (!added)
                        {

                            added = true;

                            TreeParentNode b = new TreeParentNode (Note.OBJECT_TYPE,
                                                                   type,
                                                                   notes.size ());
                                                                   //noteTypeHandler.getObjectsForType (t).size ());

                            DefaultMutableTreeNode tn = new DefaultMutableTreeNode (b);

                            root.add (tn);

                            noteNodes.put (type,
                                           tn);

                        }

                        noteNode = noteNodes.get (t);

                    }

                }

                if (noteNode != null)
                {

                    DefaultMutableTreeNode nn = new DefaultMutableTreeNode (n);

                    noteNode.add (nn);

                }

            }

        }

        if (nullNode.getChildCount () > 0)
        {

            // Insert at the top.
            root.insert (nullNode,
                         0);

        }

        return root;

    }

    public static JRadioButton createRadioButton (String text)
    {

        return UIUtils.createRadioButton (text,
                                          null);

    }

    public static JRadioButton createRadioButton (String         text,
                                                  ActionListener onClick)
    {

        JRadioButton b = new JRadioButton ()
        {

            @Override
            public void setText (String t)
            {

                super.setText (String.format ("<html>%s</html>",
                                              Environment.replaceObjectNames (t)));

            }

        };

        b.setText (text);
        b.setBackground (null);
        b.setOpaque (false);

        if (onClick != null)
        {

            b.addActionListener (onClick);

        }

        b.setVerticalTextPosition (SwingConstants.TOP);
        b.setVerticalAlignment (SwingConstants.TOP);

        return b;

    }

    public static JCheckBox createCheckBox (String text)
    {

        return UIUtils.createCheckBox (text,
                                       null);

    }

    public static JCheckBox createCheckBox (String         text,
                                            ActionListener onClick)
    {

        JCheckBox b = new JCheckBox ()
        {

            @Override
            public void setText (String t)
            {

                super.setText (String.format ("<html>%s</html>",
                                              t));

            }

        };

        b.setText (text);
        b.setBackground (null);
        b.setOpaque (false);

        if (onClick != null)
        {

            b.addActionListener (onClick);

        }

        b.setVerticalTextPosition (SwingConstants.TOP);
        b.setVerticalAlignment (SwingConstants.TOP);

        return b;

    }

    public static DefaultMutableTreeNode createChaptersTree (Project    p,
                                                             Collection exclude,
                                                             Collection init,
                                                             boolean    selectable)
    {

        DefaultMutableTreeNode root = UIUtils.createTreeNode (p,
                                                              exclude,
                                                              init,
                                                              selectable);

        if (p.getBooks ().size () == 1)
        {

            Book b = (Book) p.getBooks ().get (0);

            // Get the chapters.
            List<Chapter> chaps = b.getChapters ();

            for (Chapter c : chaps)
            {

                DefaultMutableTreeNode node = UIUtils.createTree (c,
                                                                  exclude,
                                                                  init,
                                                                  selectable);

                if (node == null)
                {

                    continue;

                }

                root.add (node);

            }

        } else
        {

            List<Book> books = p.getBooks ();

            for (int i = 0; i < books.size (); i++)
            {

                Book b = books.get (i);

                DefaultMutableTreeNode node = UIUtils.createTree (b,
                                                                  exclude,
                                                                  init,
                                                                  selectable);

                if (node == null)
                {

                    continue;

                }

                root.add (node);

            }

        }

        return root;

    }

    public static void addAssetsToLinkToTree (DefaultMutableTreeNode     root,
                                              UserConfigurableObjectType forType,
                                              Set<Asset>                 assets,
                                              Collection                 exclude,
                                              Collection                 init,
                                              boolean                    selectable)
    {

        if ((assets == null)
            ||
            (assets.size () == 0)
           )
        {

            return;

        }

        DefaultMutableTreeNode charn = null;

        if (selectable)
        {

            SelectableDataObject s = new SelectableDataObject (forType);
            s.parentNode = true;

            charn = new DefaultMutableTreeNode (s);

            root.add (charn);

            root = charn;

        }

        for (Asset a : assets)
        {

            if (!selectable)
            {

                if (!init.contains (a))
                {

                    continue;

                }

            }

            DefaultMutableTreeNode node = UIUtils.createTreeNode (a,
                                                                  exclude,
                                                                  init,
                                                                  selectable);

            if (node == null)
            {

                continue;

            }

            root.add (node);

        }

    }

    public static DefaultMutableTreeNode createLinkToTree (Project    p,
                                                           Collection exclude,
                                                           Collection init,
                                                           boolean    selectable)
    {

        DefaultMutableTreeNode root = UIUtils.createTreeNode (p,
                                                              exclude,
                                                              init,
                                                              selectable);

        if (p.getBooks ().size () == 1)
        {

            Book b = (Book) p.getBooks ().get (0);

            // Get the chapters.
            List<Chapter> chaps = b.getChapters ();

            for (Chapter c : chaps)
            {

                if (!selectable)
                {

                    if (!init.contains (c))
                    {

                        continue;

                    }

                }

                DefaultMutableTreeNode node = UIUtils.createTree (c,
                                                                  exclude,
                                                                  init,
                                                                  selectable);

                if (node == null)
                {

                    continue;

                }

                root.add (node);

            }

        } else
        {

            List<Book> books = p.getBooks ();

            for (int i = 0; i < books.size (); i++)
            {

                Book b = books.get (i);

                if (!selectable)
                {

                    if (!init.contains (b))
                    {

                        continue;

                    }

                }

                DefaultMutableTreeNode node = UIUtils.createTree (b,
                                                                  exclude,
                                                                  init,
                                                                  selectable);

                if (node == null)
                {

                    continue;

                }

                root.add (node);

            }

        }

        Set<UserConfigurableObjectType> assetTypes = Environment.getAssetUserConfigurableObjectTypes (true);

        for (UserConfigurableObjectType t : assetTypes)
        {

            Set<Asset> as = p.getAssets (t);

            UIUtils.addAssetsToLinkToTree (root,
                                           t,
                                           as,
                                           exclude,
                                           init,
                                           selectable);

        }

        Map<String, Set<Note>> allNotes = new TreeMap ();

        List<Book> books = p.getBooks ();

        // Collect all the notes.
        for (int i = 0; i < books.size (); i++)
        {

            List<Chapter> chapters = books.get (i).getChapters ();

            for (int j = 0; j < chapters.size (); j++)
            {

                Set<Note> notes = chapters.get (j).getNotes ();

                for (Note n : notes)
                {

                    String t = n.getType ();

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

        Iterator<Map.Entry<String, Set<Note>>> iter = allNotes.entrySet ().iterator ();

        while (iter.hasNext ())
        {

            Map.Entry<String, Set<Note>> item = iter.next ();

            String t = item.getKey ();

            Set<Note> l = item.getValue ();

            // Peek at the top of the list to see the actual type.
            t = l.iterator ().next ().getType ();
            //t = l.get (0).getType ();

            BlankNamedObject bdo = new BlankNamedObject (Note.OBJECT_TYPE,
                                                         t);

            Object o = bdo;

            if (selectable)
            {

                SelectableDataObject s = new SelectableDataObject (bdo);
                s.parentNode = true;
                o = s;

            }

            DefaultMutableTreeNode tn = new DefaultMutableTreeNode (o);

            for (Note n : l)
            {

                if (!selectable)
                {

                    if (!init.contains (n))
                    {

                        continue;

                    }

                }

                DefaultMutableTreeNode node = UIUtils.createTreeNode (n,
                                                                      exclude,
                                                                      init,
                                                                      selectable);

                if (node != null)
                {

                    tn.add (node);

                }

            }

            if (tn.getChildCount () > 0)
            {

                root.add (tn);

            }

        }

        return root;

    }

    public static void expandAllNodesWithChildren (JTree t)
    {

        DefaultTreeModel dtm = (DefaultTreeModel) t.getModel ();

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) dtm.getRoot ();

        Enumeration en = root.depthFirstEnumeration ();

        while (en.hasMoreElements ())
        {

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement ();

            if (node.getChildCount () > 0)
            {

                t.expandPath (new TreePath (node.getPath ()));

            }

        }

    }

    public static void getSelectedObjects (DefaultMutableTreeNode n,
                                           Set                    s)
    {

        Enumeration<DefaultMutableTreeNode> en = n.children ();

        while (en.hasMoreElements ())
        {

            DefaultMutableTreeNode nn = en.nextElement ();

            SelectableDataObject sd = (SelectableDataObject) nn.getUserObject ();

            if (sd.selected)
            {

                s.add (sd.obj);

            }

            UIUtils.getSelectedObjects (nn,
                                        s);

        }

    }

    public static void addSelectableListener (final JTree tree)
    {

        tree.addMouseListener (new MouseEventHandler ()
        {

            private void selectAllChildren (DefaultTreeModel       model,
                                            DefaultMutableTreeNode n,
                                            boolean                v)
            {

                Enumeration<DefaultMutableTreeNode> en = n.children ();

                while (en.hasMoreElements ())
                {

                    DefaultMutableTreeNode c = en.nextElement ();

                    SelectableDataObject s = (SelectableDataObject) c.getUserObject ();

                    s.selected = v;

                    // Tell the model that something has changed.
                    model.nodeChanged (c);

                    // Iterate.
                    this.selectAllChildren (model,
                                            c,
                                            v);

                }

            }

            @Override
            public void handlePress (MouseEvent ev)
            {

                TreePath tp = tree.getPathForLocation (ev.getX (),
                                                       ev.getY ());

                if (tp != null)
                {

                    DefaultMutableTreeNode n = (DefaultMutableTreeNode) tp.getLastPathComponent ();

                    // Tell the model that something has changed.
                    DefaultTreeModel model = (DefaultTreeModel) tree.getModel ();

                    SelectableDataObject s = (SelectableDataObject) n.getUserObject ();
/*
                if ((ev.getClickCount () == 2)
                    &&
                    (n.getChildCount () > 0)
                   )
                {

                    this.selectAllChildren (model,
                                            n,
                                            s.selected);

                } else {

                    s.selected = !s.selected;

                }
*/
                    s.selected = !s.selected;

                    model.nodeChanged (n);

                }

            }

        });

    }

    public static DefaultMutableTreeNode createTree (Book       b,
                                                     Collection exclude,
                                                     Collection init,
                                                     boolean    selectable)
    {

        DefaultMutableTreeNode root = UIUtils.createTreeNode (b,
                                                              exclude,
                                                              init,
                                                              selectable);

        if (root == null)
        {

            return null;

        }

        // Get the chapters.
        List<Chapter> chs = b.getChapters ();

        for (int i = 0; i < chs.size (); i++)
        {

            Chapter c = chs.get (i);

            if (!selectable)
            {

                if ((init != null)
                    &&
                    (!init.contains (c))
                   )
                {

                    continue;

                }

            }

            DefaultMutableTreeNode node = UIUtils.createTree (c,
                                                              exclude,
                                                              init,
                                                              selectable);

            if (node == null)
            {

                continue;

            }

            root.add (node);

        }

        return root;

    }

    public static DefaultMutableTreeNode createTreeNode (Object     o,
                                                         Collection exclude,
                                                         Collection init,
                                                         boolean    selectable)
    {

        if ((exclude != null) &&
            (exclude.contains (o)))
        {

            return null;

        }

        if (selectable)
        {

            SelectableDataObject so = new SelectableDataObject ((NamedObject) o);

            if ((init != null)
                &&
                (init.contains (o))
               )
            {

                so.selected = true;

            }

            o = so;

        }

        return new DefaultMutableTreeNode (o);

    }

    public static DefaultMutableTreeNode createTree (Scene      s,
                                                     Collection exclude,
                                                     Collection init,
                                                     boolean    selectable)
    {

        DefaultMutableTreeNode root = UIUtils.createTreeNode (s,
                                                              exclude,
                                                              init,
                                                              selectable);

        if (root == null)
        {

            return null;

        }

        // Get the outline items.
        Iterator iter = s.getOutlineItems ().iterator ();

        while (iter.hasNext ())
        {

            Object o = iter.next ();

            if (!selectable)
            {

                if ((init != null) &&
                    (!init.contains (o)))
                {

                    continue;

                }

            }

            DefaultMutableTreeNode node = UIUtils.createTreeNode (o,
                                                                  exclude,
                                                                  init,
                                                                  selectable);

            if (node == null)
            {

                continue;

            }

            root.add (node);

        }

        return root;

    }

    public static DefaultMutableTreeNode createTree (Chapter    c,
                                                     Collection exclude,
                                                     Collection init,
                                                     boolean    selectable)
    {

        DefaultMutableTreeNode root = UIUtils.createTreeNode (c,
                                                              exclude,
                                                              init,
                                                              selectable);

        if (root == null)
        {

            return null;

        }

        List items = new ArrayList (c.getScenes ());

        items.addAll (c.getOutlineItems ());

        Collections.sort (items,
                          new ChapterItemSorter ());

        for (int j = 0; j < items.size (); j++)
        {

            ChapterItem i = (ChapterItem) items.get (j);
/*
            if (i instanceof Note)
            {

                Note n = (Note) i;

                DefaultMutableTreeNode node = UIUtils.createTreeNode ((Note) n,
                                                                      exclude,
                                                                      init,
                                                                      false);

                if (node == null)
                {

                    continue;

                }

                root.add (node);

            }
*/
            if (i instanceof Scene)
            {

                Scene s = (Scene) i;

                if (!selectable)
                {

                    if ((init != null) &&
                        (!init.contains (s)))
                    {

                        continue;

                    }

                }

                DefaultMutableTreeNode node = UIUtils.createTree ((Scene) s,
                                                                  exclude,
                                                                  init,
                                                                  selectable);

                if (node == null)
                {

                    continue;

                }

                root.add (node);

            }

            if (i instanceof OutlineItem)
            {

                OutlineItem oi = (OutlineItem) i;

                if (!selectable)
                {

                    if ((init != null) &&
                        (!init.contains (oi)))
                    {

                        continue;

                    }

                }

                DefaultMutableTreeNode node = UIUtils.createTreeNode (oi,
                                                                      exclude,
                                                                      init,
                                                                      selectable);

                if (node == null)
                {

                    continue;

                }

                root.add (node);

            }

        }

        if (!selectable)
        {

            DefaultMutableTreeNode notesNode = UIUtils.createChapterNotesNode (c);

            if (notesNode != null)
            {

                root.add (notesNode);

            }

        }

        return root;

    }

    private static void showErrorMessage (final PopupsSupported parent,
                                          final String    message)
    {

        if (parent == null)
        {

            return;

        }

        // Force back onto event thread.
        UIUtils.doActionLater (new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                final Box content = new Box (BoxLayout.Y_AXIS);

                AbstractProjectViewer pv = null;

                if (parent instanceof AbstractProjectViewer)
                {

                    pv = (AbstractProjectViewer) parent;

                }

                JTextPane m = UIUtils.createHelpTextPane (String.format (Environment.getUIString (LanguageStrings.errormessage,
                                                                                                  LanguageStrings.text),
                                                                         //"%s<br /><br /><a href='%s:%s'>Click here to contact Quoll Writer support about this problem.</a>",
                                                                         message,
                                                                         Constants.ACTION_PROTOCOL,
                                                                         "reportbug"),
                                                          pv);
                m.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                          m.getPreferredSize ().height));
                m.setBorder (null);
                content.add (m);

                content.add (Box.createVerticalStrut (10));

                JButton close = UIUtils.createButton (getUIString (buttons, LanguageStrings.close));

                JButton[] buts = new JButton[] { close };

                JComponent buttons = UIUtils.createButtonBar2 (buts,
                                                               Component.LEFT_ALIGNMENT);
                buttons.setAlignmentX (Component.LEFT_ALIGNMENT);
                content.add (buttons);
                content.setBorder (new EmptyBorder (10, 10, 10, 10));

                final QPopup ep = UIUtils.createPopup (Environment.getUIString (LanguageStrings.errormessage,
                                                                                LanguageStrings.title),
                                                       //"Oops, an error has occurred...",
                                                       Constants.ERROR_ICON_NAME,
                                                       content,
                                                       true,
                                                       null);

                close.addActionListener (new ActionListener ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        ep.removeFromParent ();

                    }
                });

                content.setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                                         content.getPreferredSize ().height));

                parent.showPopupAt (ep,
                                    UIUtils.getCenterShowPosition ((Component) parent,
                                                                   ep),
                                    false);
                ep.setDraggable ((Component) parent);

            }

        });

    }

    /**
     * And this convoluted mess is what happens when you change something fundemantal half way
     * through!  This needs to be cleaned up in a future release.
     *
     * @param parent The parent to show the error against.
     * @param message The message to show.
     */
    public static void showErrorMessage (Object parent,
                                         String message)
    {

        if (parent == null)
        {

            UIUtils.showErrorMessage (message);

            return;

        }

        if (parent instanceof PopupsSupported)
        {

            UIUtils.showErrorMessage ((PopupsSupported) parent,
                                      message);

            return;

        }

        if (parent instanceof PopupWindow)
        {

            UIUtils.showErrorMessage ((PopupWindow) parent,
                                      message);

            return;

        }

        if (parent instanceof PopupWindow)
        {

            UIUtils.showErrorMessage (((PopupWindow) parent).getViewer (),
                                      message);

            return;

        }

        if (parent instanceof QuollPanel)
        {

            UIUtils.showErrorMessage ((PopupsSupported) ((QuollPanel) parent).getViewer (),
                                      message);

            return;

        }

        UIUtils.showErrorMessage (message);

    }
/*
    private static void showErrorMessage (final AbstractProjectViewer pv,
                                          final String                message)
    {

        if (pv == null)
        {

            return;

        }

        UIUtils.doActionLater (new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                ErrorWindow ew = new ErrorWindow (pv,
                                                  message);

                ew.init ();

            }

        });

    }
  */
    private static void showErrorMessage (final PopupWindow p,
                                          final String      message)
    {

        if (p == null)
        {

            return;

        }

        UIUtils.doActionLater (new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                ErrorWindow ew = new ErrorWindow (p.getViewer (),
                                                  message);

                ew.init ();

                Rectangle pbounds = p.getBounds ();

                Dimension size = ew.getPreferredSize ();

                int x = ((pbounds.width - size.width) / 2) + pbounds.x;
                int y = ((pbounds.height - size.height) / 2) + pbounds.y;

                // Move the window
                Point showAt = new Point (x,
                                          y);

                ew.setShowAt (showAt);

            }

        });

    }

    private static void showErrorMessage (final String      message)
    {

        UIUtils.doActionLater (new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                ErrorWindow ew = new ErrorWindow (null,
                                                  message);

                ew.init ();

            }

        });

    }

    public static void showObjectSelectPopup (final Set<? extends NamedObject> objs,
                                              final AbstractViewer             parent,
                                              final String                     popupTitle,
                                              final ActionListener             onSelect,
                                              final boolean                    closeOnSelect,
                                              final Point                      showAt)
    {

        if (popupTitle == null)
        {

            throw new IllegalArgumentException ("Expected a popup title.");

        }

        UIUtils.doActionLater (new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                final Box content = new Box (BoxLayout.Y_AXIS);

                content.setOpaque (true);
                content.setBackground (UIUtils.getComponentColor ());

                DefaultListModel<NamedObject> m = new DefaultListModel ();

                for (NamedObject o : objs)
                {

                    m.addElement (o);

                }

                final JList l = new JList ();
                l.setModel (m);
                l.setLayoutOrientation (JList.VERTICAL);
                l.setVisibleRowCount (0);
                l.setOpaque (true);
                l.setBackground (UIUtils.getComponentColor ());
                l.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                 Short.MAX_VALUE));
                UIUtils.setAsButton (l);

                l.setCellRenderer (new DefaultListCellRenderer ()
                {

                    public Component getListCellRendererComponent (JList   list,
                                                                   Object  value,
                                                                   int     index,
                                                                   boolean isSelected,
                                                                   boolean cellHasFocus)
                    {

                        NamedObject obj = (NamedObject) value;

                        JLabel l = (JLabel) super.getListCellRendererComponent (list,
                                                                                value,
                                                                                index,
                                                                                isSelected,
                                                                                cellHasFocus);

                        l.setText (obj.getName ());

                        l.setFont (l.getFont ().deriveFont (UIUtils.getScaledFontSize (14)).deriveFont (Font.PLAIN));
                        l.setIcon (Environment.getObjectIcon (obj,
                                                              Constants.ICON_NOTIFICATION));
                        l.setBorder (UIUtils.createBottomLineWithPadding (5, 5, 5, 5));

                        if (cellHasFocus)
                        {

                            l.setBackground (Environment.getHighlightColor ());

                        }

                        return l;

                    }

                });

                int rowHeight = 37;

                l.setAlignmentX (JComponent.LEFT_ALIGNMENT);
                /*
                final Dimension sSize = new Dimension (this.swatchSize.width + (2 * this.borderWidth) + (2 * this.horizGap),
                                                       this.swatchSize.height + (2 * this.borderWidth) + (2 * this.vertGap));
                */
                JScrollPane sp = new JScrollPane (l);

                sp.setHorizontalScrollBarPolicy (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                sp.getVerticalScrollBar ().setUnitIncrement (rowHeight);
                sp.setAlignmentX (JComponent.LEFT_ALIGNMENT);
                sp.setOpaque (false);

                sp.getViewport ().setPreferredSize (new Dimension (400,
                                                                   rowHeight * (objs.size () > 3 ? 3 : objs.size ())));

                sp.setBorder (null);

                content.add (sp);

                final QPopup ep = UIUtils.createClosablePopup (popupTitle,
                                                               Environment.getIcon (Constants.VIEW_ICON_NAME,
                                                                                    Constants.ICON_POPUP),
                                                               null);

                ep.setContent (content);

                l.addListSelectionListener (new ListSelectionListener ()
                {

                    @Override
                    public void valueChanged (ListSelectionEvent ev)
                    {

                        if (onSelect != null)
                        {

                            NamedObject obj = (NamedObject) l.getSelectedValue ();

                            onSelect.actionPerformed (new ActionEvent (l,
                                                                       0,
                                                                       obj.getObjectReference ().asString ()));

                            if (closeOnSelect)
                            {

                                ep.removeFromParent ();

                            }

                        }
                    }

                });

                content.setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                                    content.getPreferredSize ().height));

                parent.showPopupAt (ep,
                                    (showAt != null ? showAt : UIUtils.getCenterShowPosition (parent,
                                                                                              ep)),
                                    false);
                ep.setDraggable (parent);

            }

        });

    }

    public static void showMessage (final PopupsSupported parent,
                                    final String          title,
                                    final Component       message)
    {

        UIUtils.showMessage (parent,
                             title,
                             message,
                             null,
                             null);

    }

    public static void showMessage (final PopupsSupported parent,
                                    final String          title,
                                    final Component       message,
                                    final String          confirmButtonLabel,
                                    final ActionListener  onConfirm)
    {

        UIUtils.showMessage (parent,
                             title,
                             message,
                             confirmButtonLabel,
                             onConfirm,
                             null);

    }

    public static void showMessage (final PopupsSupported parent,
                                    final String          title,
                                    final Component       message,
                                    final String          confirmButtonLabel,
                                    final ActionListener  onConfirm,
                                    final Point           showAt)
    {

        if (parent == null)
        {

            return;

        }

        UIUtils.doActionLater (new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                final Box content = new Box (BoxLayout.Y_AXIS);

                content.setOpaque (true);
                content.setBackground (UIUtils.getComponentColor ());
                content.add (message);

                content.add (Box.createVerticalStrut (10));

                JButton close = UIUtils.createButton ((confirmButtonLabel != null ? confirmButtonLabel : getUIString (buttons, LanguageStrings.close)),
                                                      onConfirm);

                JButton[] buts = new JButton[] { close };

                JComponent buttons = UIUtils.createButtonBar2 (buts,
                                                               Component.CENTER_ALIGNMENT);
                buttons.setAlignmentX (Component.LEFT_ALIGNMENT);
                content.add (buttons);
                content.setBorder (new EmptyBorder (10, 10, 10, 10));

                final QPopup ep = UIUtils.createClosablePopup ((title != null ? title : getUIString (generalmessage,title)),
                                                                //"Just so you know..."),
                                                               Environment.getIcon (Constants.INFO_ICON_NAME,
                                                                                    Constants.ICON_POPUP),
                                                               onConfirm);

                ep.setContent (content);

                close.addActionListener (ep.getCloseAction ());

                content.setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                                    content.getPreferredSize ().height));

                parent.showPopupAt (ep,
                                    (showAt != null ? showAt : UIUtils.getCenterShowPosition ((Component) parent,
                                                                                              ep)),
                                    false);
                ep.setDraggable ((Component) parent);

            }

        });

    }

    public static void showMessage (final PopupsSupported parent,
                                    final String          title,
                                    final String          message,
                                    final String          confirmButtonLabel,
                                    final ActionListener  onConfirm)
    {

        UIUtils.showMessage (parent,
                             title,
                             message,
                             confirmButtonLabel,
                             onConfirm,
                             null);

    }

    public static void showMessage (final PopupsSupported parent,
                                    final String          title,
                                    final String          message,
                                    final String          confirmButtonLabel,
                                    final ActionListener  onConfirm,
                                    final Point           showAt)
    {

        AbstractProjectViewer pv = null;

        if (parent instanceof AbstractProjectViewer)
        {

            pv = (AbstractProjectViewer) parent;

        }

        JTextPane m = UIUtils.createHelpTextPane (message,
                                                  pv);

        m.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                  m.getPreferredSize ().height));
        m.setBorder (null);

        UIUtils.showMessage (parent,
                             title,
                             m,
                             confirmButtonLabel,
                             onConfirm,
                             showAt);

    }

    public static void showMessage (PopupsSupported parent,
                                    String          title,
                                    String          message)
    {

        UIUtils.showMessage (parent,
                             title,
                             message,
                             null);

    }

    public static void showMessage (PopupsSupported parent,
                                    String          title,
                                    String          message,
                                    Point           showAt)
    {

        UIUtils.showMessage (parent,
                             title,
                             message,
                             null,
                             null,
                             showAt);

    }

    public static void showMessage (AbstractViewer parent,
                                    String         title,
                                    String          message,
                                    String          confirmButtonLabel,
                                    ActionListener  onConfirm)
    {

        if (parent == null)
        {

            UIUtils.showMessage (title,
                                 message,
                                 confirmButtonLabel,
                                 onConfirm);

            return;

        }

        UIUtils.showMessage ((PopupsSupported) parent,
                             title,
                             message,
                             confirmButtonLabel,
                             onConfirm);

    }

    public static void showMessage (Component      parent,
                                    String         title,
                                    String         message,
                                    String         confirmButtonLabel,
                                    ActionListener onConfirm)
    {

        if (parent == null)
        {

            UIUtils.showMessage (title,
                                 message,
                                 confirmButtonLabel,
                                 onConfirm);

            return;

        }

        if (parent instanceof QuollPanel)
        {

            UIUtils.showMessage ((PopupsSupported) ((QuollPanel) parent).getViewer (),
                                 title,
                                 message,
                                 confirmButtonLabel,
                                 onConfirm);

            return;

        }

        if (parent instanceof PopupsSupported)
        {

            UIUtils.showMessage ((PopupsSupported) parent,
                                 title,
                                 message,
                                 confirmButtonLabel,
                                 onConfirm);

            return;

        }

        if (parent instanceof PopupWindow)
        {

            UIUtils.showMessage (((PopupWindow) parent).getViewer (),
                                 title,
                                 message,
                                 confirmButtonLabel,
                                 onConfirm);

            return;

        }

        UIUtils.showMessage (title,
                             message,
                             confirmButtonLabel,
                             onConfirm);

    }

    public static void showMessage (Component parent,
                                    String    title,
                                    String    message)
    {

        UIUtils.showMessage (parent,
                             title,
                             message,
                             null,
                             null);

    }

    public static void showMessage (Component parent,
                                    String    message)
    {

        UIUtils.showMessage (parent,
                             null,
                             message,
                             null,
                             null);

    }

    private static void showMessage (String                title,
                                     String                message,
                                     String                confirmButtonLabel,
                                     ActionListener        onConfirm)
    {

        MessageWindow ew = new MessageWindow (null,
                                              title,
                                              message,
                                              confirmButtonLabel,
                                              onConfirm);

        ew.init ();

    }

    public static QPopup createPopup (String         title,
                                      String         icon,
                                      JComponent     content,
                                      boolean        includeCancel,
                                      final ActionListener cancelListener)
    {

        final QPopup p = new QPopup (title,
                                     (icon != null ? Environment.getIcon (icon,
                                                                          Constants.ICON_POPUP) : null),
                                     null);

        if (content != null)
        {

            p.setContent (content);

        }

        if (includeCancel)
        {

            final JButton cancel = UIUtils.createButton ("cancel",
                                                         Constants.ICON_MENU,
                                                         getUIString (actions,clicktoclose),
                                                         //"Click to close.",
                                                         null);

            cancel.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    p.setVisible (false);

                    if (cancelListener != null)
                    {

                        cancelListener.actionPerformed (ev);

                    }

                }

            });

            List<JButton> buts = new ArrayList ();
            buts.add (cancel);

            p.getHeader ().setControls (UIUtils.createButtonBar (buts));

        }

        return p;

    }

    public static JLabel createInformationLabel (String message)
    {

        JLabel l = new JLabel ()
        {

            public void setText (String t)
            {

                if (t == null)
                {

                    super.setText ("");

                    return;

                }

                super.setText (String.format ("<html><i>%s</i></html>",
                                              t));

            }

        };

        l.setText (message);
        l.setAlignmentX (Component.LEFT_ALIGNMENT);

        return l;

    }

    public static JLabel createLoadingLabel (String message)
    {

        JLabel l = new JLabel (Environment.getLoadingIcon (),
                               SwingConstants.LEFT)
        {

            @Override
            public void setText (String t)
            {

                super.setText (String.format ("<html>%s</html>",
                                              (t != null ? t : "Loading...")));

            }

        };

        l.setText (message);
        l.setVisible (false);
        l.setAlignmentX (Component.LEFT_ALIGNMENT);

        return l;

    }

    public static JLabel createErrorLabel (String message)
    {

        JLabel err = new JLabel ()
        {

            @Override
            public void setText (String t)
            {

                super.setText (String.format ("<html>%s</html>",
                                              t));

            }

            @Override
            public void setToolTipText (String t)
            {

                super.setText (String.format ("%s",
                                              Environment.replaceObjectNames (t)));

            }

        };

        err.setText (message);
        err.setForeground (UIUtils.getColor (Constants.ERROR_TEXT_COLOR));
        err.setIcon (Environment.getIcon (Constants.ERROR_RED_ICON_NAME,
                                          Constants.ICON_MENU));
        err.setAlignmentX (Component.LEFT_ALIGNMENT);
        err.setVerticalTextPosition (SwingConstants.TOP);
        err.setVerticalAlignment (SwingConstants.TOP);

        return err;

    }

    public static JLabel createLabel (String message)
    {

        JLabel err = new JLabel ()
        {

            @Override
            public void setText (String t)
            {

                super.setText (String.format ("<html>%s</html>",
                                              t));

            }

        };

        err.setText (message);
        err.setAlignmentX (Component.LEFT_ALIGNMENT);

        return err;

    }

    public static void createPopupMenuLabels (Map labels)
    {

        int maxWidth = 0;
        int maxHeight = 0;

        Iterator iter = labels.keySet ().iterator ();

        while (iter.hasNext ())
        {

            String l = (String) iter.next ();

            JLabel lab = new JLabel (l + " ");
            lab.setOpaque (false);
            lab.setBorder (null);

            labels.put (l,
                        lab);

            Dimension dim = lab.getPreferredSize ();

            int w = dim.width;
            int h = dim.height;

            if (w > maxWidth)
            {

                maxWidth = w;

            }

            if (h > maxHeight)
            {

                maxHeight = h;

            }

        }

        FormLayout fl = new FormLayout ("right:" + maxWidth + "px",
                                        "center:" + 20 + "px");

        iter = labels.entrySet ().iterator ();

        while (iter.hasNext ())
        {

            PanelBuilder    b = new PanelBuilder (fl);
            CellConstraints cc = new CellConstraints ();

            Map.Entry item = (Map.Entry) iter.next ();

            String l = (String) item.getKey ();

            JLabel ll = (JLabel) item.getValue ();

            b.add (ll,
                   cc.xy (1,
                          1));

            JPanel p = b.getPanel ();
            p.setOpaque (false);

            labels.put (l,
                        p);

        }

    }

    public static void setAsButton (Component c)
    {

        c.setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));

    }

    public static void setDefaultCursor (Component c)
    {

        c.setCursor (Cursor.getPredefinedCursor (Cursor.DEFAULT_CURSOR));

    }

    public static void setAsButton2 (Component c)
    {

        c.setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));

        if (c instanceof AbstractButton)
        {

            final AbstractButton b = (AbstractButton) c;

            b.setContentAreaFilled (false);

            // b.setMargin (new Insets (2, 2, 2, 2));
            b.addMouseListener (new MouseEventHandler ()
            {

                @Override
                public void mouseEntered (MouseEvent ev)
                {

                    b.setContentAreaFilled (true);

                }

                @Override
                public void mouseExited (MouseEvent ev)
                {

                    b.setContentAreaFilled (false);

                }

            });

        }

    }

    public static Border createLineBorder ()
    {

        return new LineBorder (Environment.getBorderColor (),
                               1);

    }

    public static JComboBox createNumberComboBox (Vector<Integer> vals,
                                                  int             def)
    {

        final JComboBox cb = new JComboBox (vals);

        cb.setEditor (new javax.swing.plaf.basic.BasicComboBoxEditor ()
        {

            protected JTextField createEditorComponent ()
            {

                return new FormattedTextField ("[0-9]");

            }

        });

        cb.setEditable (true);

        cb.setMaximumSize (cb.getPreferredSize ());

        if (def > 0)
        {

            cb.setSelectedItem (def);

        }

        return cb;

    }

    public static JComboBox getFontSizesComboBox (final int sizeDef)
    {

        Vector<Integer> sizeV = new Vector ();

        boolean defAdded = false;

        for (int i = 8; i < 19; i += 2)
        {

            if ((sizeDef < i) &&
                (!defAdded))
            {

                sizeV.addElement (sizeDef);
                defAdded = true;

            }

            if (i != sizeDef)
            {

                sizeV.addElement (i);

            }

        }

        if (sizeDef > 18)
        {

            sizeV.addElement (sizeDef);

        }

        final JComboBox sizes = UIUtils.createNumberComboBox (sizeV,
                                                              sizeDef);

        return sizes;

    }

    public static JComboBox getLineSpacingComboBox (final float lsDef)
    {

        Vector<Float> lineS = new Vector ();

        boolean defAdded = false;

        for (float i = 0.5f; i < 2.5f; i += 0.5f)
        {

            if ((lsDef < i) &&
                (!defAdded))
            {

                lineS.addElement (lsDef);
                defAdded = true;

            }

            if (lsDef != i)
            {

                lineS.addElement (i);

            }

        }

        if (lsDef > 2.0f)
        {

            lineS.addElement (lsDef);

        }

        final JComboBox line = new JComboBox (lineS);

        line.setEditable (true);

        line.setEditor (new javax.swing.plaf.basic.BasicComboBoxEditor ()
        {

            protected JTextField createEditorComponent ()
            {

                return new FormattedTextField ("[0-9\\.]");

            }

        });

        line.setMaximumSize (line.getPreferredSize ());

        if (lsDef > 0)
        {

            line.setSelectedItem (lsDef);

        }

        return line;

    }

    public static JComboBox getFontsComboBox (final String       selected)
    {

        GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment ();

        String[] envfonts = gEnv.getAvailableFontFamilyNames ();

        Vector<String> vector = new Vector ();

        for (int i = 1; i < envfonts.length; i++)
        {

            if (new Font (envfonts[i],
                          Font.PLAIN,
                          12).canDisplayUpTo ("ABCDEFabcdef") == -1)
            {

                vector.addElement (envfonts[i]);

            }

        }

        final JComboBox fonts = new JComboBox (vector);

        fonts.setRenderer (new DefaultListCellRenderer ()
            {

                public Component getListCellRendererComponent (JList   list,
                                                               Object  value,
                                                               int     index,
                                                               boolean isSelected,
                                                               boolean cellHasFocus)
                {

                    super.getListCellRendererComponent (list,
                                                        value,
                                                        index,
                                                        isSelected,
                                                        cellHasFocus);

                    this.setFont (new Font ((String) value,
                                            Font.PLAIN,
                                            12));

                    return this;

                }

            });

        fonts.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                             fonts.getPreferredSize ().height));

        if (selected != null)
        {

            fonts.setSelectedItem (selected);

        }

        return fonts;

    }

    /**
     * Get a combobox for the user to select the text alignment.
     * The order is always, Left, Justified, Right.
     *
     * LanguageStrings - "textalignments".
     *
     * @param alignDef The default alignment, should be one of the ALIGN_* constants from QTextEditor, can be null.
     * @returns The combobox.
     */
    public static JComboBox getAlignmentComboBox (final String alignDef)
    {

        Vector<String> alignS = new Vector ();
        alignS.add (getUIString (textalignments,left));
                    //QTextEditor.ALIGN_LEFT);
        alignS.add (getUIString (textalignments,justified));
        //QTextEditor.ALIGN_JUSTIFIED);
        alignS.add (getUIString (textalignments,right));
        //QTextEditor.ALIGN_RIGHT);

        final JComboBox align = new JComboBox (alignS);

        align.setMaximumSize (align.getPreferredSize ());

        if (alignDef != null)
        {

            if (alignDef.equals (QTextEditor.ALIGN_LEFT))
            {

                align.setSelectedIndex (0);

            }

            if (alignDef.equals (QTextEditor.ALIGN_JUSTIFIED))
            {

                align.setSelectedIndex (1);

            }

            if (alignDef.equals (QTextEditor.ALIGN_RIGHT))
            {

                align.setSelectedIndex (2);

            }

            //align.setSelectedItem (alignDef);

        }

        return align;

    }

    public static Component getLimitWrapper (Component c)
    {

        Box sw = new Box (BoxLayout.X_AXIS);
        sw.add (c);
        sw.add (Box.createHorizontalGlue ());

        return sw;

    }

    public static Border createTestLineBorder ()
    {

        return new LineBorder (Color.GREEN,
                               1);

    }

    public static Border createTestLineBorder2 ()
    {

        return new LineBorder (Color.RED,
                               1);

    }

    public static Header createBoldSubHeader (String title,
                                              String iconType)
    {

        Header h = new Header (Environment.replaceObjectNames (title),
                               ((iconType == null) ? null : Environment.getIcon (iconType,
                                                                                 Constants.ICON_MENU)),
                               null);

        h.setFont (h.getFont ().deriveFont (UIUtils.getScaledFontSize (14)).deriveFont (Font.PLAIN));

        h.setTitleColor (UIUtils.getTitleColor ());
        h.setOpaque (false);

        h.setPaintProvider (null);

        h.setAlignmentX (Component.LEFT_ALIGNMENT);

        h.setBorder (UIUtils.createPadding (0, 0, 2, 0));

        return h;

    }

    public static JLabel createSubHeader (String title,
                                          String iconType)
    {

        JLabel l = new JLabel (title);
        l.setIcon (Environment.getIcon (iconType,
                                        Constants.ICON_MENU));
        l.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                         l.getPreferredSize ().height));
        l.setBorder (new CompoundBorder (new MatteBorder (0,
                                                          0,
                                                          1,
                                                          0,
                                                          Environment.getBorderColor ()),
                                         new EmptyBorder (0,
                                                          0,
                                                          2,
                                                          0)));
        l.setAlignmentX (Component.LEFT_ALIGNMENT);

        return l;

    }

/*

    public static List<Segment> getChapterSnippetsForNames (Collection<String> names,
                                                            Chapter            c)
    {

        return UIUtils.getTextSnippetsForNames (names,
                                                c.getText ());

    }
*/
/*
    public static List<Segment> getTextSnippetsForNames (Collection<String> names,
                                                         String             text)
    {

        List<Segment> snippets = new ArrayList<Segment> ();

        if (text != null)
        {

            text = StringUtils.replaceString (text,
                                              String.valueOf ('\r'),
                                              "");

        }

        TextIterator ti = new TextIterator (text);

        for (String n : names)
        {

            Map<Sentence, NavigableSet<Integer>> matches = ti.findInSentences (n,
                                                                               null);

            if (matches != null)
            {

                Iterator<Sentence> iter = matches.keySet ().iterator ();

                char[] tchar = text.toCharArray ();

                while (iter.hasNext ())
                {

                    Sentence sen = iter.next ();

                    Set<Integer> inds = matches.get (sen);

                    if ((inds != null)
                        &&
                        (inds.size () > 0)
                       )
                    {

                        Segment s = new Segment (tchar,
                                                 sen.getAllTextStartOffset (),
                                                 sen.getText ().length ());

                        snippets.add (s);

                    }

                }

            }

        }

        // Order the segments by the start offset.
        try
        {

            Query q = new Query ();
            q.parse ("SELECT * FROM javax.swing.text.Segment ORDER BY beginIndex");
            QueryResults qr = q.execute (snippets);

            snippets = new ArrayList (qr.getResults ());

        } catch (Exception e) {

            Environment.logError ("Unable to sort segments",
                                  e);

        }

        return snippets;

    }
*/
/*
    public static Set<NamedObject> getObjectsContaining (String  s,
                                                         Project p)
    {

        Set<NamedObject> ret = new TreeSet (NamedObjectSorter.getInstance ());

        for (NamedObject n : p.getAllNamedChildObjects ())
        {

            if (n.contains (s))
            {

                ret.add (n);

            }

        }

        return ret;

    }

    public static Set<Asset> getAssetsContaining (String  s,
                                                  Project p)
    {

        Set<Asset> ret = new TreeSet (NamedObjectSorter.getInstance ());

        for (NamedObject n : p.getAllNamedChildObjects (Asset.class))
        {

            if (n.contains (s))
            {

                ret.add ((Asset) n);

            }

        }

        return ret;

    }

    public static Set<Asset> getAssetsContaining (String                     s,
                                                  UserConfigurableObjectType limitTo,
                                                  Project                    p)
    {

        Set<Asset> ret = new TreeSet (NamedObjectSorter.getInstance ());

        if (limitTo != null)
        {

            if (!limitTo.isAssetObjectType ())
            {

                return ret;

            }

        }

        for (NamedObject n : p.getAllNamedChildObjects (limitTo))
        {

            if (n.contains (s))
            {

                ret.add ((Asset) n);

            }

        }

        return ret;

    }

    public static Set<Note> getNotesContaining (String  s,
                                                Project p)
    {

        Set<Note> ret = new TreeSet (NamedObjectSorter.getInstance ());

        for (NamedObject n : p.getAllNamedChildObjects (Note.class))
        {

            if (n.contains (s))
            {

                ret.add ((Note) n);

            }

        }

        return ret;

    }

    public static Set<OutlineItem> getOutlineItemsContaining (String  s,
                                                              Project p)
    {

        Set<OutlineItem> ret = new TreeSet (NamedObjectSorter.getInstance ());

        for (NamedObject n : p.getAllNamedChildObjects (OutlineItem.class))
        {

            if (n.contains (s))
            {

                ret.add ((OutlineItem) n);

            }

        }

        return ret;

    }

    public static Set<Scene> getScenesContaining (String  s,
                                                  Project p)
    {

        Set<Scene> ret = new TreeSet (NamedObjectSorter.getInstance ());

        for (NamedObject n : p.getAllNamedChildObjects (Scene.class))
        {

            if (n.contains (s))
            {

                ret.add ((Scene) n);

            }

        }

        return ret;

    }
*/
    public static Map<Chapter, List<Segment>> getObjectSnippets (NamedObject n,
                                                                 AbstractProjectViewer pv)
    {

        Project p = pv.getProject ();

        Map<Chapter, List<Segment>> data = new LinkedHashMap ();

        Set<String> names = n.getAllNames ();

        // String name = n.getName ().toLowerCase ();

        // Get all the books and chapters.
        List<Book> books = p.getBooks ();

        for (int i = 0; i < books.size (); i++)
        {

            Book b = books.get (i);

            List<Chapter> chapters = b.getChapters ();

            for (int j = 0; j < chapters.size (); j++)
            {

                Chapter c = chapters.get (j);

                String t = (c.getText () != null ? c.getText ().getText () : null);

                // See if there is an editor for it.
                AbstractEditorPanel aep = (AbstractEditorPanel) pv.getEditorForChapter (c);

                if (aep != null)
                {

                    t = aep.getEditor ().getText ();

                }

                if ((t == null)
                    ||
                    (t.trim ().equals (""))
                   )
                {

                    continue;

                }

                List<Segment> snippets = TextUtilities.getTextSnippetsForNames (names,
                                                                                t);

                if ((snippets != null) &&
                    (snippets.size () > 0))
                {

                    data.put (c,
                              snippets);

                }

            }

        }

        return data;

    }

/*
    public static Map<Chapter, List<Segment>> getTextSnippets (String  s,
                                                               AbstractProjectViewer pv)
    {

        Map<Chapter, List<Segment>> data = new LinkedHashMap ();

        // String name = n.getName ().toLowerCase ();

        List<String> names = new ArrayList ();
        names.add (s);

        // Get all the books and chapters.
        List<Book> books = pv.getProject ().getBooks ();

        for (int i = 0; i < books.size (); i++)
        {

            Book b = books.get (i);

            List<Chapter> chapters = b.getChapters ();

            for (int j = 0; j < chapters.size (); j++)
            {

                Chapter c = chapters.get (j);

                AbstractEditorPanel qep = pv.getEditorForChapter (c);

                String t = null;

                if (qep != null)
                {

                    t = qep.getEditor ().getText ();

                } else {

                    if (c.getText () != null)
                    {

                        // Get the text.
                        t = c.getText ().getText ();

                    }

                }

                if (t == null)
                {

                    continue;

                }

                List<Segment> snippets = TextUtilities.getTextSnippetsForNames (names,
                                                                                t);

                if ((snippets != null) &&
                    (snippets.size () > 0))
                {

                    data.put (c,
                              snippets);

                }

            }

        }

        return data;

    }
*/
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

        // Replace <a with "<a style=' etc...
        s = StringUtils.replaceString (s,
                                       "<a ",
                                       "<a style='color: " + Constants.HTML_LINK_COLOR + ";' ");

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

                    w = "<a href='" + urlPrefix + URLEncoder.encode (st, "utf-8") + "'>" + urlPrefix + st + "</a>";

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

    public static void removeNodeFromTreeIfNoChildren (JTree  tree,
                                                       Object n)
    {

        DefaultTreeModel model = (DefaultTreeModel) tree.getModel ();

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot ();

        // See how many children there are.
        TreePath tp = UIUtils.getTreePathForUserObject (root,
                                                        n);

        if (tp != null)
        {

            DefaultMutableTreeNode nNode = (DefaultMutableTreeNode) tp.getLastPathComponent ();

            if (nNode.getChildCount () == 0)
            {

                // Remove it.
                model.removeNodeFromParent (nNode);

            }

        }

    }

    public static void removeNodeFromTree (JTree  tree,
                                           Object n)
    {

        DefaultTreeModel model = (DefaultTreeModel) tree.getModel ();

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot ();

        TreePath tp = UIUtils.getTreePathForUserObject (root,
                                                        n);

        // It can happen but shouldn't.
        if (tp == null)
        {

            return;

        }

        DefaultMutableTreeNode nNode = (DefaultMutableTreeNode) tp.getLastPathComponent ();

        if (nNode != null)
        {

            model.removeNodeFromParent (nNode);

        }

    }
/*
    public static Set<NamedObject> getReferencedObjects (String      t,
                                                         Project     p,
                                                         NamedObject ignore)
    {

        if (t == null)
        {

            return t;

        }

        Set<NamedObject> ret = new HashSet ();

        Set<NamedObject> objs = p.getAllNamedChildObjects (Asset.class);

        Set<NamedObject> chaps = p.getAllNamedChildObjects (Chapter.class);

        if (chaps.size () > 0)
        {

            objs.addAll (chaps);

        }

        if (objs.size () == 0)
        {

            return ret;

        }

        if (ignore != null)
        {

            objs.remove (ignore);

        }

        Map<String, List<NamedObjectNameWrapper>> items = new HashMap ();

        for (NamedObject o : objs)
        {

            NamedObjectNameWrapper.addTo (o,
                                          items);

        }

        // Get all the names.
        Set<String> names = items.keySet ();

        List<String> namesL = new ArrayList (names);

        try
        {

            JoSQLComparator jc = new JoSQLComparator ("SELECT * FROM java.lang.String ORDER BY toString.length DESC");

            Collections.sort (namesL,
                              jc);

        } catch (Exception e)
        {

            Environment.logError ("Unable to construct josql comparator",
                                  e);

        }

        for (String name : namesL)
        {

            int           ind = 0;
            int           start = 0;
            StringBuilder b = new StringBuilder ();
            String        tl = t.toLowerCase ();

            while (ind != -1)
            {

                ind = tl.indexOf (name,
                                  start);

                if (ind != -1)
                {

                    String tag = "[[_$@" + name.hashCode () + "@$_]]";

                    b.append (t.substring (start,
                                           ind));
                    b.append (tag);

                    start = ind + name.length ();

                } else
                {

                    b.append (t.substring (start));

                }

            }

            t = b.toString ();

        }

        for (String name : namesL)
        {

            List<NamedObjectNameWrapper> nitems = items.get (name);

            NamedObjectNameWrapper nw = nitems.get (0);

            t = StringUtils.replaceString (t,
                                           "[[_$@" + name.hashCode () + "@$_]]",
                                           "<a href='" + Constants.OBJECTREF_PROTOCOL + "://" + nw.namedObject.getObjectReference ().asString () + "'>" + nw.name + "</a>");

        }


    }
    */
    public static String markupStringForAssets (String      t,
                                                Project     p,
                                                NamedObject ignore)
    {

        if (t == null)
        {

            return t;

        }

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

        Map<String, List<NamedObjectNameWrapper>> items = new HashMap ();

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
/*
            b = b.replace (ind,
                           ind + obj.name.length (),
                           "<a href='" + Constants.OBJECTREF_PROTOCOL + "://" + obj.namedObject.getObjectReference ().asString () + "'>" + obj.name + "</a>");
  */

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

        }

        t = b.toString ();

        t = StringUtils.replaceString (t,
                                       String.valueOf ('\n'),
                                       "<br />");

        return t;

    }

    public static String getObjectALink (NamedObject d)
    {

        return "<a href='" + Constants.OBJECTREF_PROTOCOL + "://" + d.getObjectReference ().asString () + "'>" + d.getName () + "</a>";

    }

    public static Map<String, String> parseState (String s)
    {

        // Preserve the order, it "may" be important.
        Map ret = new LinkedHashMap ();

        StringTokenizer t = new StringTokenizer (s,
                                                 String.valueOf ('\n'));

        while (t.hasMoreTokens ())
        {

            String tok = t.nextToken ().trim ();

            StringTokenizer tt = new StringTokenizer (tok,
                                                      "=");

            while (tt.hasMoreTokens ())
            {

                if (tt.countTokens () == 2)
                {

                    String name = tt.nextToken ().trim ();
                    String value = tt.nextToken ().trim ();

                    ret.put (name,
                             value);

                }

            }

        }

        return ret;

    }

    public static String createState (Map values)
    {

        StringBuilder b = new StringBuilder ();

        Iterator iter = values.keySet ().iterator ();

        while (iter.hasNext ())
        {

            String k = iter.next ().toString ();
            String v = values.get (k).toString ();

            b.append (k);
            b.append ("=");
            b.append (v);
            b.append ('\n');

        }

        return b.toString ();

    }

    public static JButton createToolBarButton (String         icon,
                                               String         tooltip,
                                               String         actionCommand,
                                               ActionListener action)
    {

        JButton bt = new JButton (Environment.getIcon (icon,
                                                       Constants.ICON_TOOLBAR))
        {

            @Override
            public void setText (String t)
            {

                super.setText (Environment.replaceObjectNames (t));

            }

        };

        UIUtils.setAsButton (bt);
        bt.setToolTipText (Environment.replaceObjectNames (tooltip));
        bt.setActionCommand (actionCommand);
        bt.setOpaque (false);

        if (action != null)
        {

            bt.addActionListener (action);

        }

        return bt;

    }

    public static JScrollPane createTreeScroll (JTree tree)
    {

        JScrollPane scroll = new JScrollPane (tree);
        scroll.setBorder (new MatteBorder (3,
                                           3,
                                           3,
                                           3,
                                           tree.getBackground ()));
        scroll.setOpaque (false);
        scroll.setOpaque (true);

        return scroll;

    }

    public static JTree createTree ()
    {

        JTree tree = new JTree ();
        tree.setCellRenderer (new ProjectTreeCellRenderer (false));
        tree.setOpaque (true);
        tree.setBorder (null);
        tree.setRootVisible (false);
        tree.setShowsRootHandles (true);
        tree.setScrollsOnExpand (true);
        tree.setRowHeight (0);

        tree.putClientProperty (com.jgoodies.looks.Options.TREE_LINE_STYLE_KEY,
                                com.jgoodies.looks.Options.TREE_LINE_STYLE_NONE_VALUE);

        tree.putClientProperty ("Tree.paintLines",
                                Boolean.FALSE);

        return tree;

    }

    public static void setTreeRootNode (JTree                  tree,
                                        DefaultMutableTreeNode tn)
    {

        java.util.List<TreePath> openPaths = new ArrayList ();

        Enumeration<TreePath> paths = tree.getExpandedDescendants (new TreePath (tree.getModel ().getRoot ()));

        if (paths != null)
        {

            while (paths.hasMoreElements ())
            {

                openPaths.add (paths.nextElement ());

            }

        }

        DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel ();

        dtm.setRoot (tn);

        for (TreePath p : openPaths)
        {

            tree.expandPath (UIUtils.getTreePathForUserObject (tn,
                                                               ((DefaultMutableTreeNode) p.getLastPathComponent ()).getUserObject ()));

        }

    }

    public static void setCenterOfScreenLocation (Window f)
    {

        f.pack ();

        Dimension dim = Toolkit.getDefaultToolkit ().getScreenSize ();

        int w = f.getSize ().width;
        int h = f.getSize ().height;
        int x = (dim.width - w) / 2;
        int y = (dim.height - h) / 2;

        // Move the window
        f.setLocation (x,
                       y);

    }

    public static void addHyperLinkListener (final JEditorPane    p,
                                             final AbstractViewer projectViewer)
    {

        p.addHyperlinkListener (new HyperlinkListener ()
        {

            @Override
            public void hyperlinkUpdate (HyperlinkEvent ev)
            {

                if (ev.getEventType () == HyperlinkEvent.EventType.ACTIVATED)
                {

                    URL url = ev.getURL ();

                    UIUtils.openURL (projectViewer,
                                     url);

                }

            }

        });

    }

    public static Header createHeader (String     title,
                                       int        titleType)
    {

        return UIUtils.createHeader (title,
                                     titleType,
                                     (String) null,
                                     null);

    }

    public static Header createHeader (String     title,
                                       int        titleType,
                                       String     icon,
                                       JComponent controls)
    {

        int iconType = Constants.ICON_MENU;

        if (titleType == Constants.POPUP_WINDOW_TITLE)
        {

            iconType = Constants.ICON_POPUP;

        }

        if (titleType == Constants.SUB_PANEL_TITLE)
        {

            iconType = Constants.ICON_SUB_PANEL_MAIN;

        }

        if (titleType == Constants.PANEL_TITLE)
        {

            iconType = Constants.ICON_PANEL_MAIN;


        }

        if (titleType == Constants.FULL_SCREEN_TITLE)
        {

            iconType = Constants.ICON_PANEL_MAIN;

        }

        ImageIcon ii = null;

        if (icon != null)
        {

            ii = Environment.getIcon (icon,
                                      iconType);

        }

        return UIUtils.createHeader (title,
                                     titleType,
                                     ii,
                                     controls);

    }

    public static Header createHeader (String     title,
                                       int        titleType,
                                       Icon       icon,
                                       JComponent controls)
    {

        int fontSize = 10;
        Insets ins = null;

        if (titleType == Constants.POPUP_WINDOW_TITLE)
        {

            fontSize = 16;
            ins = null; //new Insets (3, 3, 3, 3);

        }

        if (titleType == Constants.SUB_PANEL_TITLE)
        {

            fontSize = 14;
            ins = new Insets (5, 5, 0, 0);

        }

        if (titleType == Constants.PANEL_TITLE)
        {

            fontSize = 16;
            ins = new Insets (5, 7, 5, 7);


        }

        if (titleType == Constants.FULL_SCREEN_TITLE)
        {

            fontSize = 18;
            ins = new Insets (5, 10, 8, 5);


        }

        Header h = new Header (Environment.replaceObjectNames (title),
                               icon,
                               controls);

        h.setAlignmentX (Component.LEFT_ALIGNMENT);

        h.setFont (h.getFont ().deriveFont ((float) UIUtils.scaleToScreenSize (fontSize)).deriveFont (Font.PLAIN));
        h.setTitleColor (UIUtils.getTitleColor ());
        h.setPadding (ins);

        return h;

    }

    public static JPanel createHelpBox (String                helpText,
                                        int                   iconType,
                                        AbstractProjectViewer pv)
    {

        FormLayout fl = new FormLayout ("p, 3px, fill:90px:grow",
                                        "top:p");

        PanelBuilder pb = new PanelBuilder (fl);

        CellConstraints cc = new CellConstraints ();

        ImagePanel helpII = new ImagePanel (Environment.getIcon ("help",
                                                                 iconType),
                                            null);
        helpII.setAlignmentX (Component.LEFT_ALIGNMENT);

        pb.add (helpII,
                cc.xy (1,
                       1));

        JTextPane helpT = UIUtils.createHelpTextPane (helpText,
                                                      pv);

        JScrollPane hsp = new JScrollPane (helpT);
        hsp.setOpaque (false);
        hsp.setBorder (null);
        hsp.getViewport ().setOpaque (false);
        hsp.setAlignmentX (Component.LEFT_ALIGNMENT);

        pb.add (hsp,
                cc.xy (3,
                       1));

        JPanel helpBox = pb.getPanel ();
        helpBox.setOpaque (false);
        helpBox.setVisible (false);
        helpBox.setBorder (new EmptyBorder (0,
                                            0,
                                            5,
                                            0));
        helpBox.setAlignmentX (Component.LEFT_ALIGNMENT);

        return helpBox;

    }

    public static String formatTextForHelpPane (String text)
    {

        if (text != null)
        {

            StringBuilder buf = new StringBuilder ();

            //text = Environment.replaceObjectNames (text);

            text = StringUtils.replaceString (text,
                                              String.valueOf ('\n'),
                                              "<br />");

            text = UIUtils.markupLinks (text);

            int ind = text.indexOf ("[");

            while (ind > -1)
            {

                int end = text.indexOf ("]",
                                        ind + 1);

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

                    v = "<img src=\"" + Environment.getIconURL (icon, Constants.ICON_MENU) + "\" />";

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

            text = UIUtils.getWithHTMLStyleSheet (new JTextField ("abcdABCD"),
                                                  text);

        }

        return text;

    }

    // TODO: Make this work.
    public static JTextPane createHelpTextPane (StringWithMarkup text,
                                                AbstractViewer   viewer)
    {

        return UIUtils.createHelpTextPane ((text != null ? text.getMarkedUpText () : null),
                                           viewer);

    }

    public static JTextPane createHelpTextPane (AbstractViewer   viewer)
    {

        return UIUtils.createHelpTextPane ((String) null,
                                           viewer);

    }

    public static JTextPane createHelpTextPane (String         text,
                                                AbstractViewer viewer)
    {

        HTMLEditorKit kit = new HTMLEditorKit ();
        HTMLDocument  doc = (HTMLDocument) kit.createDefaultDocument ();

        final JTextPane desc = new JTextPane (doc)
        {

            @Override
            public void setText (String t)
            {

                super.setText (UIUtils.formatTextForHelpPane (t));

            }

        };

        desc.setEditorKit (kit);
        desc.setEditable (false);
        desc.setOpaque (false);
        desc.setAlignmentX (JComponent.LEFT_ALIGNMENT);

        desc.setSize (new Dimension (500,
                                     Short.MAX_VALUE));

        desc.setText (text);

        UIUtils.addHyperLinkListener (desc,
                                      viewer);

        return desc;

    }

    public static HTMLPanel createHelpTextPane2 (String text,
                                                 AbstractProjectViewer projectViewer)
    {

        HTMLPanel p = new HTMLPanel (text,
                                     projectViewer);

        p.setOpaque (false);
        p.setAlignmentX (JComponent.LEFT_ALIGNMENT);

        p.setBorder (new EmptyBorder (5,
                                         5,
                                         15,
                                         5));

        p.setSize (UIUtils.getPopupWidth () - 20, 10000);

        p.doDocumentLayout (p.getGraphics ());


        return p;

    }

    public static String getWithHTMLStyleSheet (JTextComponent desc,
                                                String         text)
    {

        return UIUtils.getWithHTMLStyleSheet (desc,
                                              text,
                                              null,
                                              null);

    }

    public static String getWithHTMLStyleSheet (JTextComponent desc,
                                                String         text,
                                                String         linkColor,
                                                String         textColor)
    {

        if (text == null)
        {

            text = "";

        }

        text = StringUtils.replaceString (text,
                                          String.valueOf ('\n'),
                                          "<br />");

        StringBuilder t = new StringBuilder ();
        t.append ("<html><head>");
        t.append (UIUtils.getHTMLStyleSheet (desc,
                                             linkColor,
                                             textColor));
        t.append ("</head><body><span>");
        t.append (UIUtils.markupLinks (text));
        t.append ("</span></body></html>");

        return t.toString ();

    }

    public static void openURL (Component parent,
                                String    url)
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

            UIUtils.showErrorMessage (parent,
                                      String.format (getUIString (general,unabletoopenwebpage),
                                                     url));
                                      //"Unable to open web page: " + url);

            return;

        }

        UIUtils.openURL (parent,
                         u);

    }

    public static AbstractViewer getViewer (Component parent)
    {

        if (parent == null)
        {

            return null;

        }

        if (parent instanceof AbstractViewer)
        {

            return (AbstractViewer) parent;

        }

        if (parent instanceof PopupWindow)
        {

            return ((PopupWindow) parent).getViewer ();

        }

        if (parent instanceof QuollPanel)
        {

            return ((QuollPanel) parent).getViewer ();

        }

        return UIUtils.getViewer (parent.getParent ());

    }

    public static AbstractProjectViewer getProjectViewer (Component parent)
    {

        AbstractViewer v = UIUtils.getViewer (parent);

        if ((v != null)
            &&
            (v instanceof AbstractProjectViewer)
           )
        {

            return (AbstractProjectViewer) v;

        }

        return null;

    }

/*
    public static AbstractViewer getViewer (Component parent)
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
*/
    public static void showFile (Component parent,
                                 File      f)
    {

        try
        {

            Desktop.getDesktop ().open (f);

        } catch (Exception e)
        {

            Environment.logError ("Unable to open: " +
                                  f,
                                  e);

            UIUtils.showErrorMessage (parent,
                                      String.format (getUIString (general,unabletoopenfile),
                                                     f.getPath ()));
                                      //"Unable to open: " + f);

            return;

        }

    }

    public static void openURL (Component parent,
                                URL       url)
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

            if (!p.endsWith (".html"))
            {

                p += ".html";

            }

            u = u + "/user-guide/" + url.getHost () + p;

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

            if (parent != null)
            {

                AbstractViewer pv = UIUtils.getViewer (parent);

                if (pv != null)
                {

                    Environment.eventOccurred (new ProjectEvent (pv,
                                                                 ProjectEvent.HELP,
                                                                 ProjectEvent.SHOW));

                }

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

            if (parent != null)
            {

                AbstractProjectViewer pv = UIUtils.getProjectViewer (parent);

                if (pv != null)
                {

                    pv.viewObject (pv.getProject ().getObjectForReference (ObjectReference.parseObjectReference (url.getHost ())));

                    return;

                }

            }

        }


        if (url.getProtocol ().equals (Constants.ACTION_PROTOCOL))
        {

            String action = url.getPath ();

            if (parent != null)
            {

                AbstractViewer pv = Environment.getFocusedViewer ();

                if (pv != null)
                {

                    pv.handleHTMLPanelAction (action);

                    return;

                }

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

            UIUtils.showErrorMessage (parent,
                                      String.format (getUIString (general,unabletoopenwebpage),
                                                     url));
                                      //"Unable to open web page: " + url);

        }

    }

    public static JTextPane createTextViewPane (final StringWithMarkup        description,
                                                final AbstractProjectViewer   pv)
    {

        // TODO: Markup to html?
        return UIUtils.createTextViewPane ((description != null ? description.getMarkedUpText () : null),
                                           pv);

    }

    public static JTextPane createTextViewPane (final String                  description,
                                                final AbstractProjectViewer   pv)
    {

        HTMLEditorKit kit = new HTMLEditorKit ();
        HTMLDocument  doc = (HTMLDocument) kit.createDefaultDocument ();

        final JTextPane desc = new JTextPane (doc)
        {

            @Override
            public void setText (String t)
            {

                if (t == null)
                {

                    t = "";

                }

                super.setText (UIUtils.getWithHTMLStyleSheet (this,
                                                              UIUtils.markupStringForAssets (t,
                                                                                             pv.getProject (),
                                                                                             null)));

            }

        };

        desc.setEditorKit (kit);
        desc.setEditable (false);
        desc.setOpaque (false);

        desc.setSize (new Dimension (500, Short.MAX_VALUE));
        desc.addHyperlinkListener (new HyperlinkAdapter ()
        {

            public void hyperlinkUpdate (HyperlinkEvent ev)
            {

                if (ev.getEventType () == HyperlinkEvent.EventType.ACTIVATED)
                {

                    URL url = ev.getURL ();

                    if (url.getProtocol ().equals (Constants.OBJECTREF_PROTOCOL))
                    {

                        pv.viewObject (pv.getProject ().getObjectForReference (ObjectReference.parseObjectReference (url.getHost ())));

                        return;

                    }

                    if (url.getProtocol ().equals (Constants.OBJECTNAME_PROTOCOL))
                    {

                        String n = url.getHost ();

                        try
                        {

                            n = URLDecoder.decode (url.getHost (), "utf-8");

                        } catch (Exception e) {

                            // Ignore.

                        }

                        Set<Asset> objs = pv.getProject ().getAllAssetsByName (n,
                                                                               null);

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

                            try
                            {

                                Point point = desc.modelToView (ev.getSourceElement ().getStartOffset ()).getLocation ();

                                point = SwingUtilities.convertPoint (desc,
                                                                     point,
                                                                     pv);

                                UIUtils.showObjectSelectPopup (objs,
                                                               pv,
                                                               getUIString (selectitem,popup,title),
                                                               //"Select an item to view",
                                                               new ActionListener ()
                                                               {

                                                                    @Override
                                                                    public void actionPerformed (ActionEvent ev)
                                                                    {

                                                                        pv.viewObject (pv.getProject ().getObjectForReference (ObjectReference.parseObjectReference (ev.getActionCommand ())));

                                                                    }

                                                               },
                                                               true,
                                                               point);

                            } catch (Exception e) {

                                Environment.logError ("Unable to show popup",
                                                      e);

                                pv.viewObject (objs.iterator ().next ());

                                return;

                            }

                        }

                    }

                    if (url.getProtocol ().equals ("mailto"))
                    {

                        return;

                    }

                    UIUtils.openURL (pv,
                                     url);

                }

            }

        });

        desc.setText (description);

        return desc;

    }

    public static JEditorPane createObjectDescriptionViewPane (final StringWithMarkup        description,
                                                             final NamedObject             n,
                                                             final AbstractProjectViewer   pv,
                                                             final ProjectObjectQuollPanel qp)
    {

        // TODO: Markup to html?
        return UIUtils.createObjectDescriptionViewPane ((description != null ? description.getMarkedUpText () : null),
                                                        n,
                                                        pv,
                                                        qp);

    }

    public static JFXPanel createJFXObjectDescriptionViewPane (final String                  description,
                                                             final NamedObject             n,
                                                             final AbstractProjectViewer   pv,
                                                             final ProjectObjectQuollPanel qp)
    {

        final JFXPanel jfxp = new JFXPanel ();

        Platform.runLater (new Runnable ()
        {

            @Override
            public void run ()
            {

                BorderPane bp = new BorderPane ();
                WebView web = new WebView ();

                web.getEngine ().loadContent (UIUtils.getWithHTMLStyleSheet (null,
                                                              UIUtils.markupStringForAssets (description,
                                                                                             pv.getProject (),
                                                                                             n)));

                bp.setCenter (web);

                javafx.scene.Scene s = new javafx.scene.Scene (bp);

                jfxp.setScene (s);

            }

        });

        return jfxp;

    }

    public static JEditorPane createObjectDescriptionViewPane (final String                  description,
                                                             final NamedObject             n,
                                                             final AbstractProjectViewer   pv,
                                                             final ProjectObjectQuollPanel qp)
    {

        HTMLEditorKit kit = new HTMLEditorKit ();
        HTMLDocument  doc = (HTMLDocument) kit.createDefaultDocument ();

        //final JTextPane desc = new JTextPane (doc)
        final JEditorPane desc = new JEditorPane ()
        {

            @Override
            public void setText (String t)
            {

                super.setText (UIUtils.getWithHTMLStyleSheet (this,
                                                              UIUtils.markupStringForAssets (t,
                                                                                             pv.getProject (),
                                                                                             n)));

            }

        };

        desc.setDocument (doc);
        desc.setEditorKit (kit);
        desc.setEditable (false);
        desc.setOpaque (false);

        /*
        desc.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                            Short.MAX_VALUE));
*/
        desc.setSize (new Dimension (250, Short.MAX_VALUE));
        desc.addHyperlinkListener (new HyperlinkListener ()
        {

            @Override
            public void hyperlinkUpdate (HyperlinkEvent ev)
            {

                if (ev.getEventType () == HyperlinkEvent.EventType.ACTIVATED)
                {

                    URL url = ev.getURL ();

                    if (url.getProtocol ().equals (Constants.OBJECTREF_PROTOCOL))
                    {

                        pv.viewObject (pv.getProject ().getObjectForReference (ObjectReference.parseObjectReference (url.getHost ())));

                        return;

                    }

                    if (url.getProtocol ().equals (Constants.OBJECTNAME_PROTOCOL))
                    {

                        String un = url.getHost ();

                        try
                        {

                            un = URLDecoder.decode (un, "utf-8");

                        } catch (Exception e) {

                            // Ignore.

                        }

                        Set<NamedObject> objs = pv.getProject ().getAllNamedObjectsByName (un);

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

                            try
                            {

                                Point point = desc.modelToView (ev.getSourceElement ().getStartOffset ()).getLocation ();

                                point = SwingUtilities.convertPoint (desc,
                                                                     point,
                                                                     pv);

                                UIUtils.showObjectSelectPopup (objs,
                                                               pv,
                                                               getUIString (selectitem,popup,title),
                                                               //"Select an item to view",
                                                               new ActionListener ()
                                                               {

                                                                    @Override
                                                                    public void actionPerformed (ActionEvent ev)
                                                                    {

                                                                        pv.viewObject (pv.getProject ().getObjectForReference (ObjectReference.parseObjectReference (ev.getActionCommand ())));

                                                                    }

                                                               },
                                                               true,
                                                               point);

                            } catch (Exception e) {

                                Environment.logError ("Unable to show popup",
                                                      e);

                                pv.viewObject (objs.iterator ().next ());

                                return;

                            }

                        }

                        return;

                    }

                    if (url.getProtocol ().equals ("mailto"))
                    {

                        return;

                    }

                    UIUtils.openURL (pv,
                                     url);

                }

            }

        });

        if (qp != null)
        {

            PropertyChangedAdapter pca = new PropertyChangedAdapter ()
            {

                public void propertyChanged (PropertyChangedEvent ev)
                {

                    if (ev.getChangeType ().equals (NamedObject.DESCRIPTION))
                    {

                        desc.setText (description);

                    }

                }

            };

            qp.addObjectPropertyChangedListener (pca);

            pca.propertyChanged (new PropertyChangedEvent (qp,
                                                           NamedObject.DESCRIPTION,
                                                           null,
                                                           null));

        } else {

            desc.setText (description);

        }

        return desc;

    }

    public static String colorToHex (Color c)
    {

        return "#" + Integer.toHexString (c.getRGB ()).substring (2);

    }

    public static String getHTMLStyleSheet (JTextComponent desc,
                                            String         textColor,
                                            String         linkColor)
    {

        return UIUtils.getHTMLStyleSheet (desc,
                                          textColor,
                                          linkColor,
                                          -1);

    }

    public static String getHTMLStyleSheet (JTextComponent desc,
                                            String         textColor,
                                            String         linkColor,
                                            int            textSize)
    {

        StringBuilder t = new StringBuilder ();

        Font f = null;

        if (desc != null)
        {

            f = desc.getFont ();

        } else {

            f = new JLabel ().getFont ();

        }

        if (linkColor == null)
        {

            linkColor = Constants.HTML_LINK_COLOR;

        }

        if (!linkColor.startsWith ("#"))
        {

            linkColor = "#" + linkColor;

        }

        if (textColor == null)
        {

            textColor = "#000000";

        }

        if (!textColor.startsWith ("#"))
        {

            textColor = "#" + textColor;

        }

        int size = textSize;

        if (size < 1)
        {

            size = (int) f.getSize ();

        }

        t.append ("<style>");
        t.append ("*{font-family: \"" + f.getFontName () + "\"; font-size: " + size + "px; background-color: transparent; color: " + textColor + ";}\n");
        t.append ("body{padding: 0px; margin: 0px;color: " + textColor + "; font-size: " + size + "pt; font-family: \"" + f.getFontName () + "\";}");
        t.append ("body{overflow-x: hidden; overflow-y: hidden;}");
        t.append ("h1, h2, h3{font-family: \"" + f.getFontName () + "\";}\n");
        t.append ("span{color: " + textColor + "; font-size: " + size + "pt; font-family: \"" + f.getFontName () + "\";}\n");
        t.append ("p{padding: 0px; margin: 0px; color: " + textColor + "; font-size: " + size + "pt; font-family: \"" + f.getFontName () + "\";}\n");
        t.append ("img{vertical-align:middle; padding: 3px; border: solid 1px transparent; line-height: 22px;}");
        t.append ("a{color: " + linkColor + "; text-decoration: none; font-size: " + size + "pt; font-family: \"" + f.getFontName () + "\";}\n");
        t.append ("a.link{color: " + linkColor + "; text-decoration: none; font-size: " + size + "pt; font-family: \"" + f.getFontName () + "\";}\n");

        t.append ("a:hover {text-decoration: underline; font-size: " + size + "pt; font-family: \"" + f.getFontName () + "\";}\n");
        t.append ("b {font-size: " + size + "pt; font-family: \"" + f.getFontName () + "\";}\n");

        t.append ("ul{margin-left: 20px;}\n");
        t.append ("li{font-size: " + ((int) f.getSize ()) + "pt; font-family: \"" + f.getFontName () + "\";}\n");
        t.append ("p.error{padding: 0px; margin: 0px; color: red;}");
        t.append ("h1.help{font-size:" + (((int) f.getSize ()) + 6) + "pt; padding: 0px; margin: 0px; border-bottom: solid 1px " + UIUtils.colorToHex (Environment.getBorderColor ()) + "; font-weight: normal;}");
        t.append ("p.help{margin: 0px; margin-left: 5px; margin-top: 5px; padding: 0px; margin-bottom: 10px;}");
        t.append ("p.help img{padding-left: 3px; padding-right: 3px;}");
        t.append (".error{color: red;}");
        t.append (".warning{color: red;}");

        // A pox on the java html renderer.
        t.append ("span.b{font-weight: bold;}");
        t.append ("span.bi{font-style: italic; font-weight: bold;}");
        t.append ("span.bu{font-weight: bold; text-decoration: underline;}");
        t.append ("span.biu{font-style: italic; font-weight: bold; text-decoration: underline;}");
        t.append ("span.i{font-style: italic;}");
        t.append ("span.iu{font-style: italic; text-decoration: underline;}");
        t.append ("span.u{text-decoration: underline;}");

        // Sigh, this is needed otherwise inner "a" tags won't inherit the styles.
        t.append ("span.b a{font-weight: bold;}");
        t.append ("span.bi a{font-style: italic; font-weight: bold;}");
        t.append ("span.bu a{font-weight: bold; text-decoration: underline;}");
        t.append ("span.biu a{font-style: italic; font-weight: bold; text-decoration: underline;}");
        t.append ("span.i a{font-style: italic;}");
        t.append ("span.iu a{font-style: italic; text-decoration: underline;}");
        t.append ("span.u a{text-decoration: underline;}");
        t.append ("span.iu a{font-style: italic; text-decoration: underline;}");
        t.append ("span a img{text-decoration: none; border: 0px;}");
        t.append ("</style>");

        return t.toString ();

    }

    public static String getHTMLStyleSheet (JTextComponent desc)
    {

        return UIUtils.getHTMLStyleSheet (desc,
                                          "#000000",
                                          Constants.HTML_LINK_COLOR);

    }

    public static AbstractAction createAddAssetActionListener (final UserConfigurableObjectType forAssetType,
                                                               final ProjectViewer              viewer,
                                                               final String                     name,
                                                               final String                     desc)
    {

        return new AbstractAction ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                Asset as = null;

                try
                {

                    as = Asset.createAsset (forAssetType);

                } catch (Exception e) {

                    Environment.logError ("Unable to create new asset for object type: " +
                                          forAssetType,
                                          e);

                    UIUtils.showErrorMessage (viewer,
                                              String.format (getUIString (assets,add,actionerror),
                                                             forAssetType.getObjectTypeName ()));
                                              //"Unable to create new asset type.");

                    return;

                }

                if (as == null)
                {

                    Environment.logError ("Unable to create new asset for object type: " +
                                          forAssetType);

                    UIUtils.showErrorMessage (viewer,
                                              String.format (getUIString (assets,add,actionerror),
                                                             forAssetType.getObjectTypeName ()));
                                              //"Unable to create new asset type.");

                    return;

                }

                if (name != null)
                {

                    as.setName (name);

                }

                if (desc != null)
                {

                    as.setDescription (new StringWithMarkup (desc));

                }

                String addAsset = UserProperties.get (Constants.ADD_ASSETS_PROPERTY_NAME);

                // Should we use a popup?
                if (((addAsset.equals (Constants.ADD_ASSETS_TRY_POPUP))
                     &&
                     (forAssetType.getNonCoreFieldCount () == 0)
                    )
                    ||
                    (addAsset.equals (Constants.ADD_ASSETS_POPUP))
                   )
                {

                    AssetActionHandler aah = new AssetActionHandler (as,
                                                                     viewer);

                    aah.setPopupOver (viewer);

                    aah.actionPerformed (ev);

                    return;

                }

                viewer.showAddAsset (as,
                                     null);

            }

        };

    }

    public static void addNewAssetItemsToPopupMenu (final Container     m,
                                                    final Component     showPopupAt,
                                                    final ProjectViewer pv,
                                                    final String        name,
                                                    final String        desc)
    {

        String pref = "Shortcut: ";

        Set<UserConfigurableObjectType> types = Environment.getAssetUserConfigurableObjectTypes (true);

        for (UserConfigurableObjectType type : types)
        {

            JMenuItem mi = new JMenuItem (type.getObjectTypeName (),
                                          type.getIcon16x16 ());

            m.add (mi);

            KeyStroke k = type.getCreateShortcutKeyStroke ();

            if (k != null)
            {

                mi.setMnemonic (k.getKeyChar ());
                mi.setToolTipText (pref + Utils.keyStrokeToString (k));

            }

            mi.addActionListener (UIUtils.createAddAssetActionListener (type,
                                                                        pv,
                                                                        name,
                                                                        desc));

        }

    }

    public static void addNewAssetItemsAsToolbarToPopupMenu (JPopupMenu    m,
                                                             Component     showPopupAt,
                                                             ProjectViewer pv,
                                                             String        name,
                                                             String        desc)
    {

        List<JComponent> buts = new ArrayList ();

        Set<UserConfigurableObjectType> types = Environment.getAssetUserConfigurableObjectTypes (true);

        for (UserConfigurableObjectType type : types)
        {

            JButton but = UIUtils.createButton (type.getIcon16x16 (),
                                                String.format (getUIString (assets,add,button,tooltip),
                                                                //"Click to add a new %s",
                                                               type.getObjectTypeName ()),
                                                null);

            buts.add (but);

            final UserConfigurableObjectType _type = type;

            but.addActionListener (new ActionListener ()
            {

                @Override
                public void actionPerformed (ActionEvent ev)
                {

                    Asset a = null;

                    try
                    {

                        a = Asset.createAsset (_type);

                    } catch (Exception e) {

                        Environment.logError ("Unable to create new asset for object type: " +
                                              _type,
                                              e);

                        UIUtils.showErrorMessage (pv,
                                                  String.format (getUIString (assets,add,actionerror),
                                                                 _type.getObjectTypeName ()));
                                                  //"Unable to create new asset type.");

                        return;

                    }

                    if (a == null)
                    {

                        Environment.logError ("Unable to create new asset for object type: " +
                                              _type);

                        UIUtils.showErrorMessage (pv,
                                                  String.format (getUIString (assets,add,actionerror),
                                                                 _type.getObjectTypeName ()));
                                                  //"Unable to create new asset type.");

                        return;

                    }

                    if (name != null)
                    {

                        a.setName (name);

                    }

                    if (desc != null)
                    {

                        a.setDescription (new StringWithMarkup (desc));

                    }

                    AssetActionHandler aah = new AssetActionHandler (a,
                                                                     pv);

                    if (showPopupAt instanceof PopupsSupported)
                    {

                        aah.setPopupOver (pv);
                        aah.setShowPopupAt (null,
                                            "below");

                    } else
                    {

                        aah.setShowPopupAt (showPopupAt,
                                            "above");

                    }

                    aah.actionPerformed (ev);

                }

            });

        }

        m.add (UIUtils.createPopupMenuButtonBar (null,
                                                 m,
                                                 buts));

    }

    public static Color getBorderHighlightColor ()
    {

        return UIUtils.getColor ("#275C92");

    }

    public static Color getHintTextColor ()
    {

        return UIUtils.getColor ("#aaaaaa");

    }

    public static Color hexToColor (String hexCode)
    {

        return UIUtils.getColor (hexCode);

    }

    public static Color getColor (String hexCode)
    {

        if (hexCode.startsWith ("#"))
        {

            hexCode = hexCode.substring (1);

        }

        hexCode = hexCode.toUpperCase ();

        return new Color (Integer.parseInt (hexCode.substring (0,
                                                               2),
                                            16),
                          Integer.parseInt (hexCode.substring (2,
                                                               4),
                                            16),
                          Integer.parseInt (hexCode.substring (4),
                                            16));
    }

    public static String getFrameTitle (String name)
    {

        return Environment.replaceObjectNames (name) + Environment.getWindowNameSuffix ();

    }

    public static void setFrameTitle (Frame  f,
                                      String name)
    {

        f.setTitle (UIUtils.getFrameTitle (name));

    }

    public static void setFrameTitle (Dialog f,
                                      String name)
    {

        f.setTitle (UIUtils.getFrameTitle (name));

    }

    public static JButton createHelpPageButton (final String  helpPage,
                                                final int     iconType,
                                                final String  helpText)
    {

        final JButton helpBut = new JButton (Environment.getIcon ("help",
                                                                  iconType));
        helpBut.setToolTipText ((helpText != null ? helpText : getUIString (help,button,tooltip)));
        //"Click to view the help"));
        helpBut.setOpaque (false);
        UIUtils.setAsButton (helpBut);

        helpBut.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                UIUtils.openURL (helpBut,
                                 "help://" + helpPage);

            }

        });

        return helpBut;

    }

    public static JLabel createWebsiteLabel (final String  website,
                                             final String  display,
                                             final boolean useLabelText)
    {

        final JLabel web = new JLabel (String.format ("<html>%s</html>",
                                                      Environment.replaceObjectNames (((display == null) ? website : display))))
        {

            @Override
            public void setText (String t)
            {

                if (!this.getText ().equals (""))
                {

                    throw new IllegalStateException ("Once set the website label text cannot be modified.");

                }

                super.setText (t);

            }

        };

        // web.setEditable (false);
        web.setOpaque (false);
        web.setBorder (null);
        web.setForeground (new Color (112,
                                      149,
                                      226));
        UIUtils.setAsButton (web);

        web.addMouseListener (new MouseEventHandler ()
        {

            @Override
            public void mouseEntered (MouseEvent ev)
            {

                Map attrs = new HashMap ();
                attrs.put (TextAttribute.UNDERLINE,
                           TextAttribute.UNDERLINE_LOW_ONE_PIXEL);

                web.setFont (web.getFont ().deriveFont (attrs));

            }

            @Override
            public void mouseExited (MouseEvent ev)
            {

                Map attrs = new HashMap ();
                attrs.put (TextAttribute.UNDERLINE,
                           null);

                web.setFont (web.getFont ().deriveFont (attrs));

            }

            @Override
            public void handlePress (MouseEvent ev)
            {

                String w = website;

                if (useLabelText)
                {

                    w = web.getText ();

                }

                if ((w == null) ||
                    (w.trim ().equals ("")))
                {

                    return;

                }

                if ((!w.toLowerCase ().startsWith ("http://")) &&
                    (!w.toLowerCase ().startsWith ("https://")))
                {

                    w = "http://" + w;

                }

                try
                {

                    UIUtils.openURL (web,
                                     w);

                } catch (Exception e)
                {

                    Environment.logError ("Unable to visit website: " +
                                          w,
                                          e);

                }

            }

        });

        return web;

    }

    public static JLabel createClickableLabel (String title,
                                               Icon   icon)
    {

        String ns = null;

        return UIUtils.createClickableLabel (title,
                                             icon,
                                             ns);

    }

    public static JLabel createClickableLabel (final String title,
                                               final Icon   icon,
                                               final String gotoURLOnClick)
    {

        return UIUtils.createClickableLabel (title,
                                             icon,
                                             new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                if (gotoURLOnClick != null)
                {

                    UIUtils.openURL (null,
                                     gotoURLOnClick);

                }

            }

        });

    }

    public static void makeClickable (final JLabel l,
                                      final ActionListener onClick)
    {

        l.addMouseListener (new MouseEventHandler ()
        {

            @Override
            public void handlePress (MouseEvent ev)
            {

                if (onClick != null)
                {

                    try
                    {

                        onClick.actionPerformed (new ActionEvent (l, 1, "clicked"));

                    } catch (Exception e) {

                        Environment.logError ("Unable to perform action",
                                              e);

                    }

                }

            }

        });

        l.setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));

    }

    public static JLabel createClickableLabel (final String         title,
                                               final Icon           icon,
                                               final ActionListener onClick)
    {

        final JLabel l = new JLabel (null,
                                     icon,
                                     SwingConstants.LEFT)
        {

            @Override
            public void setText (String t)
            {

                if (t == null)
                {

                    super.setText (null);

                    return;

                }

                super.setText (String.format ("<html>%s</html>",
                                              Environment.replaceObjectNames (t)));

            }

            @Override
            public void setToolTipText (String t)
            {

                if (t == null)
                {

                    super.setToolTipText (null);

                    return;

                }

                super.setToolTipText (String.format ("%s",
                                                     Environment.replaceObjectNames (t)));

            }

        };

        l.setText (title);
        l.setForeground (UIUtils.getColor (Constants.HTML_LINK_COLOR));
        l.setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));
        l.setVerticalAlignment (SwingConstants.TOP);
        l.setVerticalTextPosition (SwingConstants.TOP);
        UIUtils.makeClickable (l,
                               onClick);

        return l;

    }

    public static JLabel createLabel (final String         title,
                                      final Icon           icon,
                                      final ActionListener onClick)
    {

        final JLabel l = new JLabel (null,
                                     icon,
                                     SwingConstants.LEFT)
        {

            @Override
            public void setText (String t)
            {

                if (t == null)
                {

                    super.setText (null);

                    return;

                }

                super.setText (String.format ("<html>%s</html>",
                                              t));

            }

            @Override
            public void setToolTipText (String t)
            {

                if (t == null)
                {

                    super.setToolTipText (null);

                    return;

                }

                super.setToolTipText (t);

            }

        };

        l.setText (title);

        if (onClick != null)
        {

            l.setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));
            UIUtils.makeClickable (l,
                                   onClick);

        }

        l.setVerticalAlignment (SwingConstants.TOP);
        l.setVerticalTextPosition (SwingConstants.TOP);

        return l;

    }

    public static int getA4PageCountForChapter (Chapter c,
                                                String  text)
    {

        if ((text == null)
            ||
            (text.trim ().length () == 0)
           )
        {

            return 0;

        }

        // Create a new editor.
        QTextEditor ed = UIUtils.wordCountPerPageEditor;
/*
        QTextEditor ed = new QTextEditor (null,
                                          false);
                                          */
        ed.setSectionBreak (Constants.SECTION_BREAK);

        ed.setLineSpacing (c.getPropertyAsFloat (Constants.EDITOR_LINE_SPACING_PROPERTY_NAME));
        ed.setFontSize ((int) (UIUtils.getEditorFontSize (c.getPropertyAsInt (Constants.EDITOR_FONT_SIZE_PROPERTY_NAME))));
        ed.setFontFamily (c.getProperty (Constants.EDITOR_FONT_PROPERTY_NAME));
        ed.setAlignment (c.getProperty (Constants.EDITOR_ALIGNMENT_PROPERTY_NAME));

        ed.setTextWithMarkup (new StringWithMarkup (text));

        int ppi = java.awt.Toolkit.getDefaultToolkit ().getScreenResolution ();

        // A4 - 17cm wide, 25.7cm high.
        // 6.7" wide, 10.12" high

        float pageHeight = 10.12f;
        float pageWidth = 6.7f;

        ed.setSize (new Dimension ((int) (pageWidth * ppi),
                                   Integer.MAX_VALUE));

        ed.setSize (new Dimension ((int) (pageWidth * ppi),
                                   ed.getPreferredSize ().height));

        // Get the height, divide by the page size.
        int a4PageCount = (int) (ed.getHeight () / (pageHeight * ppi));

        if (ed.getHeight () > (pageHeight * ppi * a4PageCount))
        {

            a4PageCount++;

        }

        /*
         *standard pages count

        ed.setSize (new Dimension (4 * ppi,
                                   ed.getPreferredSize ().height));

        int standardPageCount = ed.getHeight () / (4 * ppi);

        if (ed.getHeight () > (6.5f * ppi * standardPageCount))
        {

            standardPageCount++;

        }
        */

        return a4PageCount;

    }

    public static JScrollPane createScrollPane (JComponent c,
                                                boolean    addTopBorderOnScroll)
    {

        final JScrollPane sp = new JScrollPane (c);
        sp.setOpaque (true);
        sp.getViewport ().setBackground (UIUtils.getComponentColor ());
        sp.setBackground (UIUtils.getComponentColor ());
        sp.setAlignmentX (Component.LEFT_ALIGNMENT);
        sp.getVerticalScrollBar ().setUnitIncrement (DEFAULT_SCROLL_BY_AMOUNT);

        // Ease of use thing since you want all tables to scroll on rows.
        if (c instanceof JTable)
        {

            sp.getVerticalScrollBar ().setUnitIncrement (((JTable) c).getRowHeight ());
            sp.getVerticalScrollBar ().setBlockIncrement (1);

        }

        if (addTopBorderOnScroll)
        {

            final Border defBorder = UIUtils.createPadding (1, 0, 0, 0);

            final Border scrollBorder = new MatteBorder (1, 0, 0, 0,
                                                         UIUtils.getInnerBorderColor ());

            sp.setBorder (defBorder);

            sp.getVerticalScrollBar ().addAdjustmentListener (new AdjustmentListener ()
            {

                public void adjustmentValueChanged (AdjustmentEvent ev)
                {

                    if (sp.getVerticalScrollBar ().getValue () > 0)
                    {

                        sp.setBorder (scrollBorder);

                    } else {

                        sp.setBorder (defBorder);

                    }

                }

            });

        } else {

            sp.setBorder (UIUtils.createLineBorder ());

        }

        return sp;

    }

    public static JScrollPane createScrollPane (JComponent c)
    {

        return UIUtils.createScrollPane (c,
                                         false);

    }

    public static JScrollPane createScrollPane (final JTree c)
    {

        return UIUtils.createScrollPane (c,
                                         -1);

    }

    public static JScrollPane createScrollPane (final JTree c,
                                                final int   maxHeight)
    {

        JScrollPane sp = new JScrollPane (c)
        {

            public Dimension getPreferredSize ()
            {

                Dimension d = c.getPreferredSize ();

                if (maxHeight > 0)
                {

                    if (d.height > maxHeight)
                    {

                        d.height = maxHeight;

                    }

                }

                return d;

            }

        };

        //JScrollPane sp = new JScrollPane (c);
        sp.setOpaque (true);
        sp.getViewport ().setBackground (UIUtils.getComponentColor ());
        sp.setBackground (UIUtils.getComponentColor ());
        sp.setBorder (UIUtils.createLineBorder ());
        sp.setAlignmentX (Component.LEFT_ALIGNMENT);
        sp.getVerticalScrollBar ().setUnitIncrement (DEFAULT_SCROLL_BY_AMOUNT);

        return sp;

    }

    public static JScrollPane createScrollPane (JTextComponent t)
    {

        JScrollPane sp = new JScrollPane (t);
        sp.setOpaque (false);

        sp.setBorder (new JScrollPane ().getBorder ());

        t.setMargin (new Insets (3,
                                 3,
                                 3,
                                 3));
        sp.setAlignmentX (Component.LEFT_ALIGNMENT);

        sp.getVerticalScrollBar ().setUnitIncrement (DEFAULT_SCROLL_BY_AMOUNT);

        return sp;

    }

    public static JSplitPane createSplitPane (int orientation)
    {

        JSplitPane sp = new JSplitPane (orientation,
                                        false);
        sp.setDividerSize (4);//UIUtils.getSplitPaneDividerSize () + 4);

        sp.setUI (new BasicSplitPaneUI ()
        {

            public BasicSplitPaneDivider createDefaultDivider ()
            {

                // Probably going to hell for this...
                final AtomicBoolean highlight = new AtomicBoolean (false);

                final BasicSplitPaneDivider div = new BasicSplitPaneDivider (this)
                {

                    @Override
                    public void paint (Graphics g)
                    {

                        if (highlight.get ())
                        {

                            g.setColor (UIUtils.getBorderColor ());

                        } else {

                            g.setColor(new Color (0, 0, 0, 0));

                        }

                        g.fillRect(0, 0, getSize().width, getSize().height);
                        super.paint (g);

                    }

                };

                div.addMouseListener (new MouseEventHandler ()
                {

                    @Override
                    public void mouseEntered (MouseEvent ev)
                    {

                        if (UserProperties.getAsBoolean (Constants.HIGHLIGHT_SPLITPANE_DIVIDERS_PROPERTY_NAME))
                        {

                            highlight.set (true);
                            div.validate ();
                            div.repaint ();

                        }

                    }

                    @Override
                    public void mouseExited (MouseEvent ev)
                    {

                        highlight.set (false);
                        div.validate ();
                        div.repaint ();

                    }

                    @Override
                    public void mouseReleased (MouseEvent ev)
                    {

                        highlight.set (false);
                        div.validate ();
                        div.repaint ();

                    }

                });

                div.addMouseMotionListener (new MouseEventHandler ()
                {

                    @Override
                    public void mouseDragged (MouseEvent ev)
                    {

                        if (UserProperties.getAsBoolean (Constants.HIGHLIGHT_SPLITPANE_DIVIDERS_PROPERTY_NAME))
                        {

                            highlight.set (true);
                            div.validate ();
                            div.repaint ();

                        }

                    }

                });

                return div;

            }

        });

        sp.setBorder (null);
        sp.setOpaque (false);
        sp.setContinuousLayout (true);

        return sp;

    }

    public static JTextField createTextField ()
    {

        JTextField f = new JTextField ();
        f.setAlignmentX (JComponent.LEFT_ALIGNMENT);
        f.setBorder (new JScrollPane ().getBorder ());
        f.setMargin (new Insets (3,
                                 3,
                                 3,
                                 3));

        return f;

    }

    public static JPasswordField createPasswordField ()
    {

        JPasswordField f = new JPasswordField ();
        f.setAlignmentX (JComponent.LEFT_ALIGNMENT);
        f.setBorder (new JScrollPane ().getBorder ());
        f.setMargin (new Insets (3,
                                 3,
                                 3,
                                 3));

        return f;

    }

    public static JComponent createOpaqueGlue (int dir)
    {

        JComponent c = null;

        if (dir == BoxLayout.Y_AXIS)
        {

            c = (JComponent) Box.createVerticalGlue ();

        } else {

            c = (JComponent) Box.createHorizontalGlue ();

        }

        c.setOpaque (true);
        c.setBackground (UIUtils.getComponentColor ());

        return c;

    }

    /**
     * Create a text area with the specified placeholder for the number of rows and max chars.  The
     * dictionary and synonym providers from the passed in viewer are used.
     *
     * @param pv The project viewer.
     * @param placeholder The placeholder text.
     * @param rows The number of rows of text.
     * @param maxChars The maximum number of characters allowed.
     * @returns The text area.
     */
    public static TextArea createTextArea (AbstractProjectViewer pv,
                                           String                placeholder,
                                           int                   rows,
                                           int                   maxChars)
    {

        TextArea t = new TextArea (placeholder,
                                   rows,
                                   maxChars);
        t.setDictionaryProvider (pv.getDictionaryProvider ());
        t.setSpellCheckEnabled (pv.isSpellCheckingEnabled ());

        try
        {

            t.setSynonymProvider (pv.getSynonymProvider ());

        } catch (Exception e) {

            Environment.logError ("Unable to set synonym provider.",
                                  e);

        }

        return t;

    }

    /**
     * Create a text area with the specified placeholder for the number of rows and max chars.  The
     * default dictionary provider from Environment.getDefaultDictionaryProvider is used and a
     * synonym provider with null language is also used.
     *
     * @param placeholder The placeholder text.
     * @param rows The number of rows of text.
     * @param maxChars The maximum number of characters allowed.
     * @returns The text area.
     */
    public static TextArea createTextArea (String placeholder,
                                           int    rows,
                                           int    maxChars)
    {

        TextArea t = new TextArea (placeholder,
                                   rows,
                                   maxChars);

        try
        {

            t.setDictionaryProvider (Environment.getDefaultDictionaryProvider ());
            t.setSpellCheckEnabled (true);

        } catch (Exception e) {

            Environment.logError ("Unable to set dictionary provider",
                                  e);

        }

        try
        {

            t.setSynonymProvider (Environment.getSynonymProvider (null));

        } catch (Exception e) {

            Environment.logError ("Unable to set synonym provider.",
                                  e);

        }

        return t;

    }

    public static JComponent createPopupMenuButtonBar (String           title,
                                                       final JPopupMenu parent,
                                                       List<JComponent> buttons)
    {

        ActionListener aa = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                parent.setVisible (false);

            }

        };

        for (JComponent but : buttons)
        {

            if (but instanceof JButton)
            {

                JButton b = (JButton) but;

                b.addActionListener (aa);

            }

        }

        Box b = new Box (BoxLayout.X_AXIS);

        // This is to get around the "icon space" in the menu.
        b.add (Box.createHorizontalStrut (32));

        if (title == null)
        {

            title = "";

        }

        JLabel l = new JLabel (Environment.replaceObjectNames (title));

        l.setForeground (UIUtils.getColor ("#444444"));
        l.setFont (l.getFont ().deriveFont (Font.ITALIC));

        l.setPreferredSize (new Dimension (Math.max (50, l.getPreferredSize ().width),
                                           l.getPreferredSize ().height));

        b.add (l);

        b.add (Box.createHorizontalStrut (10));

        b.add (UIUtils.createButtonBar (buttons));

        b.add (Box.createHorizontalGlue ());

        return b;

    }

    public static JPanel createButtonBar2 (JButton[] buts,
                                           float     alignment)
    {

        ButtonBarBuilder bb = ButtonBarBuilder.create ();

        if (buts == null)
        {

            return bb.build ();

        }

        if ((alignment == Component.RIGHT_ALIGNMENT)
            ||
            (alignment == Component.CENTER_ALIGNMENT)
           )
        {

            bb.addGlue ();

        }

        bb.addButton (buts);

        if (alignment == Component.CENTER_ALIGNMENT)
        {

            bb.addGlue ();

        }

        bb.opaque (false);

        JPanel p = bb.build ();

        p.setAlignmentX (Component.LEFT_ALIGNMENT);

        return p;

    }

    public static JToolBar createButtonBar (List<? extends JComponent> buttons)
    {

        JToolBar tb = new JToolBar ();
        tb.setOpaque (false);
        tb.setFloatable (false);
        tb.setRollover (true);
        tb.setBackground (new Color (0,
                                     0,
                                     0,
                                     0));

        for (int i = 0; i < buttons.size (); i++)
        {

            JComponent b = buttons.get (i);

            UIUtils.setAsButton2 (b);
            tb.add (b);

        }

        tb.setBackground (null);
        tb.setAlignmentX (Component.LEFT_ALIGNMENT);
        tb.setBorder (null);

        return tb;

    }

    public static JButton createButton (ImageIcon icon)
    {

        return UIUtils.createButton (icon,
                                     null,
                                     null);

    }

    public static JButton createButton (ImageIcon      icon,
                                        String         toolTipText,
                                        ActionListener action)
    {

        JButton b = new JButton (icon);

        b.setFocusPainted (false);
        b.setToolTipText (toolTipText);
        b.setOpaque (false);
        UIUtils.setAsButton (b);

        if (action != null)
        {

            b.addActionListener (action);

        }

        return b;

    }

    public static JButton createButton (String         icon,
                                        int            iconType,
                                        String         toolTipText,
                                        ActionListener action)
    {

        return UIUtils.createButton (Environment.getIcon (icon,
                                                          iconType),
                                     toolTipText,
                                     action);

    }

    public static JButton createButton (String label,
                                        String icon)
    {

        JButton b = new JButton (Environment.getButtonLabel (label),
                                 (icon == null ? null : Environment.getIcon (icon,
                                                                             Constants.ICON_MENU)));

        return b;

    }

    public static JButton createButton (String label)
    {

        return UIUtils.createButton (label,
                                     (String) null);

    }

    public static JButton createButton (String         label,
                                        ActionListener onClick)
    {

        JButton b = UIUtils.createButton (label,
                                          (String) null);

        if (onClick != null)
        {

            b.addActionListener (onClick);

        }

        return b;

    }

    public static void treeChanged (DataObject d,
                                    JTree      tree)
    {

        DefaultTreeModel model = (DefaultTreeModel) tree.getModel ();

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) UIUtils.getTreePathForUserObject ((DefaultMutableTreeNode) model.getRoot (),
                                                                                                 d).getLastPathComponent ();

        model.nodeStructureChanged (node);

    }

    public static void informTreeOfNodeChange (NamedObject n,
                                               JTree       tree)
    {

        DefaultTreeModel model = (DefaultTreeModel) tree.getModel ();

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) UIUtils.getTreePathForUserObject ((DefaultMutableTreeNode) model.getRoot (),
                                                                                                 n).getLastPathComponent ();

        model.nodeChanged (node);

    }

    public static String formatPrompt (Prompt p)
    {

        if (p == null)
        {

            return getUIString (warmups,prompt,view,unavailable);
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

        return p.getText () + "<br /> - " + link;


    }

    public static Font getHeaderFont ()
    {

        if (UIUtils.headerFont == null)
        {

            UIUtils.headerFont = new JLabel ("").getFont ().deriveFont (Font.BOLD,
                                                                        16);

        }

        return UIUtils.headerFont;

    }

    public static float getScaledFontSize (int v)
    {

        // Ugh, assume a 96 dpi and let the underlying windows manager handle scaling.
        // We return a float here so that calls to Font.deriveFont can use the value directly rather than
        // having to cast.
        return (float) (Math.round ((float) v * ((float) DEFAULT_ASSUMED_SCREEN_RESOLUTION / 72f)));

    }

    @Deprecated
    /**
     * Use UIUtils.getScaledFontSize instead.
     */
    public static float scaleToScreenSize (double h)
    {

        // Ugh, assume a 96 dpi and let the underlying windows manager handle scaling.
        return UIUtils.getScaledFontSize ((int) h);

    }

    public static int getPrintFontSize ()
    {

        return UIUtils.getPrintFontSize (new JLabel ().getFont ().getSize ());

    }

    public static int getPrintFontSize (int size)
    {

        return Math.round ((float) size / ((float) java.awt.Toolkit.getDefaultToolkit ().getScreenResolution () / 72f));

    }

    public static int getEditorFontSize (int size)
    {

        // Need to take the screen resolution into account.
        float s = (float) size * ((float) java.awt.Toolkit.getDefaultToolkit ().getScreenResolution () / 72f);

        return (int) s;

    }

    public static boolean isChildOf (MouseEvent ev,
                                     Component  parent)
    {

        return UIUtils.isChildOf ((Component) ev.getSource (),
                                  parent);

    }

    public static boolean isChildOf (Component child,
                                     Component parent)
    {

        Container p = child.getParent ();

        while (p != null)
        {

            if (p == parent)
            {

                return true;

            }

            p = p.getParent ();

        }

        return false;

    }

    public static ImageIcon getColoredIcon (String name,
                                            int    type,
                                            Color  c)
    {

        ImageIcon ic = Environment.getIcon (name,
                                            type);

        if (ic == null)
        {

            return null;

        }

        return UIUtils.getColoredIcon (ic.getImage (),
                                       c);

    }

    public static ImageIcon getColoredIcon (Image  im,
                                            Color  c)
    {

        if (im == null)
        {

            return null;

        }

        final int crgb = c.getRGB ();

        ImageFilter filter = new RGBImageFilter()
        {
          public final int filterRGB(int x, int y, int rgb)
          {
            if (rgb != 0)
            {

            }
             return (rgb != 0 ? crgb : rgb);

          }
        };

        ImageProducer ip = new FilteredImageSource (im.getSource (), filter);
        return new ImageIcon (Toolkit.getDefaultToolkit().createImage(ip));

    }

    public static Image replaceColorInImage (Image  im,
                                             Color  orig,
                                             Color  changeTo)
    {

        if (im == null)
        {

            return null;

        }

        final int orgb = orig.getRGB ();
        final int crgb = changeTo.getRGB ();

        ImageFilter filter = new RGBImageFilter()
        {
          public final int filterRGB(int x, int y, int rgb)
          {
            if (rgb != 0)
            {

            }

            return (rgb == orgb ? crgb : rgb);

          }
        };

        ImageProducer ip = new FilteredImageSource (im.getSource (), filter);
        return Toolkit.getDefaultToolkit().createImage (ip);

    }

    public static Color getIconColumnColor ()
    {

        return UIUtils.getColor ("#f0f0f0");//"f5fcfe");

    }

    public static int getSplitPaneDividerSize ()
    {

        return 2;

    }

    public static Color getTitleColor ()
    {

        return UIUtils.getColor ("#333333");

    }

    public static Color getInnerBorderColor ()
    {

        return UIUtils.getColor ("#dddddd");

    }

    public static Color getDragIconColor ()
    {

        return UIUtils.getColor ("#aaaaaa");

    }

    public static Color getBorderColor ()
    {

        return UIUtils.getColor ("#aaaaaa");

    }

    public static Color getComponentColor ()
    {

        return UIUtils.getColor ("#fdfdfd");

    }

    public static boolean clipboardHasContent ()
    {

        try
        {

            return (Toolkit.getDefaultToolkit ().getSystemClipboard ().getData (DataFlavor.stringFlavor) != null);

        } catch (Exception e)
        {

        }

        return false;

    }

    /**
     * Draw a string on the image at a specified "location", supported values for <b>where</b> are:
     *      - tl - draw the string starting at the top left corner.
     *      - bl - bottom left corner.
     *      - tr - top right corner. (so start the text in from the right)
     *      - br - bottom right corner (start, in from the right and bottom)
     *
     * @param im The image to draw on, this image is untouched.
     * @param text The text to draw on the image.
     * @param font The font to use.
     * @param color The color to use.
     * @param where Where the text should be drawn.
     * @returns A new image with the text drawn on top of <b>im</b>.
     */
    public static BufferedImage drawStringOnImage (BufferedImage im,
                                                   String        text,
                                                   Font          font,
                                                   Color         color,
                                                   String        where)
    {

        FontMetrics m = im.getGraphics ().getFontMetrics (font);

        int sh = m.getHeight ();
        int sw = m.stringWidth (text);

        int imw = im.getWidth ();
        int imh = im.getHeight ();

        int nw = 0;
        int nh = 0;

        Point w = null;

        if (where.equals ("tr"))
        {

            nw = imw - sw;
            nh = sh;

        }

        if (where.equals ("bl"))
        {

            nh = imh - sh;

        }

        if (where.equals ("br"))
        {

            nh = imh;// - sh;
            nw = imw - sw;

        }

        return UIUtils.drawStringOnImage (im,
                                          text,
                                          font,
                                          color,
                                          new Point (nw, nh));

    }

    public static BufferedImage getImageOfComponent (Component c,
                                                     int       width,
                                                     int       height)
    {

        width = (width > 0 ? width : c.getWidth ());
        height = (height > 0 ? height : c.getHeight ());

        BufferedImage image = new BufferedImage (width,
                                                 height,
                                                 BufferedImage.TYPE_INT_ARGB_PRE);

        Graphics2D g = (Graphics2D) image.getGraphics ();

        RenderingHints rh = new RenderingHints(
             RenderingHints.KEY_TEXT_ANTIALIASING,
             RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHints(rh);

        if (c.isOpaque ())
        {

            g.setColor (c.getBackground ());

            g.fillRect (0,
                        0,
                        width,
                        height);

        }

        c.paint (g);

        return image;

    }

    public static BufferedImage copyImage (BufferedImage im)
    {

        BufferedImage b = new BufferedImage (im.getWidth (),
                                             im.getHeight (),
                                             im.getType ());
        Graphics g = b.getGraphics ();
        g.drawImage (im,
                     0,
                     0,
                     null);
        g.dispose ();

        return b;

    }

    /**
     * Draw a string onto an image at the specified point (<b>where</b>).
     *
     * @param im The image to draw onto.
     * @param text The text to draw.
     * @param font The font to use.
     * @param color The color to use.
     * @param where Where to draw the text.
     * @returns A new image with the text overlaid on the image.
     */
    public static BufferedImage drawStringOnImage (BufferedImage im,
                                                   String        text,
                                                   Font          font,
                                                   Color         color,
                                                   Point         where)
    {

        BufferedImage newIm = UIUtils.copyImage (im);

        Graphics2D g = (Graphics2D) newIm.getGraphics ();

        g.setFont (font);

        g.setRenderingHint (RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        g.setColor (color);

        FontMetrics fm = g.getFontMetrics ();

        java.awt.geom.Rectangle2D b = g.getFontMetrics ().getStringBounds (text,
                                                            g);

        g.drawString (text,
                      where.x,
                      (int) (where.y + b.getHeight ()));

        return newIm;

    }

    public static BufferedImage getBufferedImage (Image     im,
                                                  Component io)
    {

        if (im == null)
        {

            return null;

        }

        int           height = im.getHeight (io);
        int           width = im.getWidth (io);

        BufferedImage bi = new BufferedImage (width,
                                              height,
                                              BufferedImage.TYPE_INT_ARGB);
        Graphics    g = bi.getGraphics ();
        g.drawImage (im,
                     0,
                     0,
                     Color.black,
                     io);

        return bi;

    }

    public static BufferedImage getScaledImage (DataSource ds,
                                                int        width,
                                                int        height)
    {

        try
        {

            return UIUtils.getScaledImage (ImageIO.read (ds.getInputStream ()),
                                           width,
                                           height);

        } catch (Exception e) {

            Environment.logError ("Unable to read image from data source: " + ds,
                                  e);

            return null;

        }

    }

    public static BufferedImage getImage (byte[] bytes)
                                   throws GeneralException
    {

        if (bytes == null)
        {

            return null;

        }

        try
        {

            return ImageIO.read (new ByteArrayInputStream (bytes));

        } catch (Exception e) {

            throw new GeneralException ("Unable to convert bytes to an image",
                                        e);

        }

    }

    public static byte[] getImageBytes (BufferedImage im)
                                 throws GeneralException
    {

        if (im == null)
        {

            return null;

        }

        try
        {

            ByteArrayOutputStream bout = new ByteArrayOutputStream ();

            // Shouldn't use png here, it is too slow.
            if (!ImageIO.write (im,
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

    public static BufferedImage getImage (File f)
    {

        if (f == null)
        {

            return null;

        }

        try
        {

            return ImageIO.read (f);

        } catch (Exception e) {

            Environment.logError ("Unable to find image for file: " + f,
                                  e);

            return null;

        }

    }

    public static BufferedImage getScaledImage (File          f,
                                                int           width,
                                                int           height)
    {

        return UIUtils.getScaledImage (UIUtils.getImage (f),
                                       width,
                                       height);

    }

    public static BufferedImage getScaledImage (File          f,
                                                int           width)
    {

        return UIUtils.getScaledImage (UIUtils.getImage (f),
                                       width);

    }

    public static BufferedImage getScaledImage (BufferedImage img,
                                                int           targetWidth)
    {

        if (img == null)
        {

            return null;

        }

        return Scalr.resize (img,
                             Scalr.Method.QUALITY,
                             Scalr.Mode.FIT_TO_WIDTH,
                             targetWidth,
                             Scalr.OP_ANTIALIAS);

    }

    public static BufferedImage createBufferedImage (int width,
                                                     int height)
    {

        BufferedImage im = new BufferedImage (width,
                                              height,
                                              BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = im.createGraphics ();

        g.setBackground (new Color (0, true));
        g.clearRect (0, 0, width, height);

        g.dispose ();

        return im;

    }

    public static Graphics2D createThrowawayGraphicsInstance ()
    {

        return new BufferedImage (1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics ();

    }

    public static BufferedImage iconToImage(Icon icon)
    {

        if (icon == null)
        {

            return null;

        }

        if (icon instanceof ImageIcon)
        {

            return (BufferedImage) ((ImageIcon)icon).getImage();

        } else {

            int w = icon.getIconWidth();
            int h = icon.getIconHeight();
            GraphicsEnvironment ge =
              GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            BufferedImage image = gc.createCompatibleImage(w, h);
            Graphics2D g = image.createGraphics();
            icon.paintIcon(null, g, 0, 0);
            g.dispose();
            return image;

       }

    }

    public static BufferedImage getScaledImage (BufferedImage img,
                                                int           targetWidth,
                                                int           targetHeight)
    {

        return Scalr.resize (img,
                             Scalr.Method.QUALITY,
                             Scalr.Mode.FIT_EXACT,
                             (targetWidth > 0 ? targetWidth : img.getWidth ()),
                             (targetHeight > 0 ? targetHeight : img.getHeight ()),
                             Scalr.OP_ANTIALIAS);
/*
        BufferedImage tmp = new BufferedImage (targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = tmp.createGraphics();
        g2.setRenderingHint (RenderingHints.KEY_RENDERING,
                             RenderingHints.VALUE_RENDER_QUALITY);
        g2.drawImage(img, 0, 0, targetWidth, targetHeight, null);
        g2.dispose();

        return tmp;
  */
    }

    public static ImageIcon overlayImage (ImageIcon bg,
                                          ImageIcon fg,
                                          String    where)
    {

        JButton but = new JButton ();

        Image bgi = bg.getImage ();
        Image fgi = fg.getImage ();

        BufferedImage n = new BufferedImage (bgi.getWidth (but),
                                             bgi.getHeight (but),
                                             BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = n.createGraphics ();
        g.setRenderingHint (RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        g.drawImage (bgi,
                     0,
                     0,
                     null);

        if (where.equals ("tl"))
        {

            g.drawImage (fgi,
                         0,
                         0,
                         null);

        }

        if (where.equals ("tr"))
        {

            g.drawImage (fgi,
                         bgi.getWidth (but) - fgi.getWidth (but),
                         0,
                         null);

        }

        if (where.equals ("bl"))
        {

            g.drawImage (fgi,
                         0,
                         bgi.getHeight (but) - fgi.getHeight (but),
                         null);

        }

        if (where.equals ("br"))
        {

            g.drawImage (fgi,
                         bgi.getWidth (but) - fgi.getWidth (but),
                         bgi.getHeight (but) - fgi.getHeight (but),
                         null);

        }

        g.dispose ();

        //bg.setImage (bgi);

        return new ImageIcon (n);

    }

    public static FileFinder createFileFind (final String         initPath,
                                             final String         finderTitle,
                                             final int            fileSelectionMode,
                                             final String         approveButtonText,
                                             final ActionListener handler)
    {

        FileFinder ff = new FileFinder ();

        ff.setOnSelectHandler (handler);
        ff.setApproveButtonText (approveButtonText);
        ff.setFinderSelectionMode (fileSelectionMode);
        ff.setFinderTitle (finderTitle);

        ff.init ();

        if (initPath != null)
        {

            ff.setFile (new File (initPath));

        }

        return ff;

    }

    public static Component getComponentByName (Container parent,
                                                String    name)
    {

        if (name == null)
        {

            return null;

        }

        if (parent == null)
        {

            return null;

        }

        Component[] children = parent.getComponents ();

        for (int i = 0; i < children.length; i++)
        {

            if (name.equals (children[i].getName ()))
            {

                return children[i];

            }

        }

        return null;

    }

    public static JMenuItem createMenuItem (String label,
                                            String icon,
                                            ActionListener action)
    {

        JMenuItem mi = new JMenuItem (Environment.getButtonLabel (label),
                                      Environment.getIcon (icon,
                                                 Constants.ICON_MENU));
        mi.addActionListener (action);

        return mi;

    }

    public static JMenuItem createMenuItem (String         label,
                                            ImageIcon      icon,
                                            ActionListener action)
    {

        JMenuItem mi = new JMenuItem (Environment.getButtonLabel (label),
                                      icon);
        mi.addActionListener (action);

        return mi;

    }

    public static JMenuItem createMenuItem (String label,
                                            String icon,
                                            ActionListener action,
                                            String         actionCommand,
                                            KeyStroke      accel)
    {

        JMenuItem mi = UIUtils.createMenuItem (label,
                                               icon,
                                               action);

        mi.setActionCommand (actionCommand);
        mi.setAccelerator (accel);

        return mi;

    }

    public static void listenToTableForCellChanges (JTable table,
                                                    Action action)
    {

        // Shouldn't be cleaned up since it adds itself as a property listener to the table.
        new TableCellListener (table,
                               action);

    }

    public static boolean isAnimatedGif (File f)
    {

        String type = Utils.getFileType (f);

        if (f == null)
        {

            throw new IllegalArgumentException ("Unable to determine file type for file: " +
                                                f);

        }

        int c = 0;

        ImageReader r = null;

            r = ImageIO.getImageReadersBySuffix (type).next ();

            if (r == null)
            {

                throw new IllegalArgumentException ("Unsupported file type for file: " +
                                                    f);

            }

        try
        {

            r.setInput (ImageIO.createImageInputStream (f));

        } catch (Exception e) {

            throw new IllegalArgumentException ("Unable to read image file: " +
                                                f,
                                                e);

        }

        try
        {

            int ind = 0;

            // Upper bound valid?
            while (ind < 100)
            {

                if (c > 1)
                {

                    return true;

                }

                r.read (ind);

                ind++;
                c++;

            }

        } catch (Exception e) {

        }

        return false;

    }

    public static void doActionWhenPanelIsReady (final QuollPanel     p,
                                                 final ActionListener action,
                                                 final Object         contextObject,
                                                 final String         actionTypeName)
    {

        final Exception pe = new Exception ();

        final javax.swing.Timer t = new javax.swing.Timer (100,
                                                           null);

        // TODO: Change this to be based on an event that the panel fires when ready.
        ActionListener l = new ActionListener ()
        {

            private int count = 0;

            public void actionPerformed (ActionEvent ev)
            {

                if (p.isReadyForUse ())
                {

                    t.setRepeats (false);
                    t.stop ();

                    try
                    {

                        action.actionPerformed (new ActionEvent ((contextObject != null ? contextObject : action),
                                                                 1,
                                                                 (actionTypeName != null ? actionTypeName : "any")));

                    } catch (Exception e) {

                        Environment.logError ("Unable to perform action: " + action + "\nCause: " +
                                              Utils.getStackTrace (pe),
                                              e);

                    }

                    return;

                }

                // 2s delay max.
                if (count > 50)
                {

                    Environment.logError ("Unable to perform action: " + action,
                                          new Exception ("Unable to perform action for panel: " + p.getPanelId (),
                                                         pe));

                    t.setRepeats (false);
                    t.stop ();

                }

                count++;

            }

        };

        t.setRepeats (true);
        t.addActionListener (l);

        t.start ();

    }

    public static void doActionLater (final ActionListener action)
    {

        UIUtils.doActionLater (action,
                               null,
                               null);

    }

    public static void doActionLater (final ActionListener action,
                                      final Object         contextObject,
                                      final String         actionTypeName)
    {

        SwingUtilities.invokeLater (new Runner ()
        {

            public void run ()
            {

                try
                {

                    action.actionPerformed (new ActionEvent ((contextObject != null ? contextObject : action),
                                                             1,
                                                             (actionTypeName != null ? actionTypeName : "any")));

                } catch (Exception e) {

                    Environment.logError ("Unable to perform action",
                                          e);

                }

            }

        });

    }

    public static void addDoActionOnReturnPressed (TextArea       text,
                                                   ActionListener action)
    {

        text.addDoActionOnCtrlReturnPressed (action);

    }

    public static void addDoActionOnReturnPressed (final JTextComponent text,
                                                   final ActionListener action)
    {

        if (action == null)
        {

            return;

        }

        if (text instanceof JTextField)
        {

            text.addKeyListener (new KeyAdapter ()
            {

                public void keyPressed (KeyEvent ev)
                {

                    if (ev.getKeyCode () == KeyEvent.VK_ENTER)
                    {

                        // This is the same as save for the form.
                        action.actionPerformed (new ActionEvent (text,
                                                                 1,
                                                                 "return-pressed"));

                    }

                }

            });

        } else {

            text.addKeyListener (new KeyAdapter ()
            {

                public void keyPressed (KeyEvent ev)
                {

                    if (((ev.getModifiersEx () & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK) &&
                        (ev.getKeyCode () == KeyEvent.VK_ENTER))
                    {

                        action.actionPerformed (new ActionEvent (text,
                                                                 1,
                                                                 "ctrl-return-pressed"));

                    }

                }

            });

        }

    }

    public static QPopup createQuestionPopup (AbstractViewer        viewer,
                                              String                title,
                                              String                icon,
                                              String                message,
                                              String                confirmButtonLabel,
                                              String                cancelButtonLabel,
                                              final ActionListener  onConfirm,
                                              final ActionListener  onCancel,
                                              final ActionListener  onClose,
                                              Point                 showAt)
    {

        Map<String, ActionListener> buttons = new LinkedHashMap ();

        if (onConfirm != null)
        {

            buttons.put (Environment.getButtonLabel (confirmButtonLabel,
                                                     getUIString (LanguageStrings.buttons,confirm)),
                         onConfirm);

        }

        buttons.put (Environment.getButtonLabel (cancelButtonLabel,
                                                 getUIString (LanguageStrings.buttons,cancel)),
                     onCancel);

        return UIUtils.createQuestionPopup (viewer,
                                            title,
                                            icon,
                                            message,
                                            buttons,
                                            onClose,
                                            showAt);

    }

    public static QPopup createQuestionPopup (AbstractViewer              viewer,
                                              String                      title,
                                              String                      icon,
                                              String                      message,
                                              Map<String, ActionListener> buttons,
                                              ActionListener              onClose,
                                              Point                       showAt)
    {

        JComponent mess = UIUtils.createHelpTextPane (Environment.replaceObjectNames (message),
                                                      viewer);
        mess.setBorder (null);
        mess.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                     500));

        return UIUtils.createQuestionPopup (viewer,
                                            title,
                                            icon,
                                            mess,
                                            buttons,
                                            onClose,
                                            showAt);

    }

    public static QPopup createQuestionPopup (PopupsSupported             viewer,
                                              String                      title,
                                              String                      icon,
                                              JComponent                  mess,
                                              Map<String, ActionListener> buttons,
                                              ActionListener              onClose,
                                              Point                       showAt)
    {

        final QPopup qp = UIUtils.createClosablePopup (title,
                                                       Environment.getIcon (icon,
                                                                            Constants.ICON_POPUP),
                                                       onClose);

        Box content = new Box (BoxLayout.Y_AXIS);

        content.add (mess);
        content.add (Box.createVerticalStrut (10));

        JButton[] buts = new JButton[buttons.size ()];

        int i = 0;

        // Too much type information required to do entrySet...
        // We will never do much iterating here so simpler and clearer to just do it the "hard way".
        Iterator<String> iter = buttons.keySet ().iterator ();

        while (iter.hasNext ())
        {

            String label = iter.next ();

            ActionListener a = buttons.get (label);

            JButton b = UIUtils.createButton (label,
                                              qp.getCloseAction ());

            if (a != null)
            {

                b.addActionListener (a);

            }

            buts[i++] = b;

        }

        JComponent bs = UIUtils.createButtonBar2 (buts,
                                                  Component.LEFT_ALIGNMENT); //ButtonBarFactory.buildLeftAlignedBar (buts);
        bs.setAlignmentX (Component.LEFT_ALIGNMENT);
        content.add (bs);
        content.setBorder (new EmptyBorder (10, 10, 10, 10));
        qp.setContent (content);

        content.setPreferredSize (new Dimension (Math.max (UIUtils.getPopupWidth (), bs.getPreferredSize ().width),
                                                 content.getPreferredSize ().height));

        if (showAt == null)
        {

            if (viewer instanceof Component)
            {

                showAt = UIUtils.getCenterShowPosition ((Component) viewer,
                                                        qp);

            }

        }

        viewer.showPopupAt (qp,
                            showAt,
                            false);

        if (viewer instanceof Component)
        {

            qp.setDraggable ((Component) viewer);

        }

        return qp;

    }

    public static QPopup createHelpPopup (AbstractViewer  viewer,
                                          String          title,
                                          String          text,
                                          ActionListener  onClose,
                                          Point           showAt)
    {

        final QPopup qp = UIUtils.createClosablePopup ((title != null ? title : getUIString (help,popup,title)),
                                                        //"Help"),
                                                       Environment.getIcon (Constants.HELP_ICON_NAME,
                                                                            Constants.ICON_POPUP),
                                                       onClose);

        Box content = new Box (BoxLayout.Y_AXIS);

        content.add (UIUtils.createHelpTextPane (text,
                                                 viewer));
        content.add (Box.createVerticalStrut (10));

        JButton b = UIUtils.createButton (getUIString (buttons,close));

        JButton[] buts = { b };

        JComponent bs = UIUtils.createButtonBar2 (buts,
                                                  Component.CENTER_ALIGNMENT);
        bs.setAlignmentX (Component.LEFT_ALIGNMENT);
        content.add (bs);
        content.setBorder (UIUtils.createPadding (10, 10, 10, 10));
        qp.setContent (content);

        content.setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                                 content.getPreferredSize ().height));

        if (showAt == null)
        {

            if (viewer instanceof Component)
            {

                showAt = UIUtils.getCenterShowPosition ((Component) viewer,
                                                        qp);

            }

        }

        viewer.showPopupAt (qp,
                            showAt,
                            false);

        if (viewer instanceof Component)
        {

            qp.setDraggable ((Component) viewer);

        }

        return qp;

    }

    public static QPopup createPasswordInputPopup (final AbstractViewer  viewer,
                                                   String                title,
                                                   String                icon,
                                                   String                message,
                                                   String                confirmButtonLabel,
                                                   String                cancelButtonLabel,
                                                   String                initValue,
                                                   final ValueValidator  validator,
                                                   final ActionListener  onConfirm,
                                                   final ActionListener  onCancel,
                                                   Point                 showAt)
    {

        return UIUtils.createTextInputPopup (viewer,
                                             title,
                                             icon,
                                             message,
                                             UIUtils.createPasswordField (),
                                             confirmButtonLabel,
                                             cancelButtonLabel,
                                             initValue,
                                             validator,
                                             onConfirm,
                                             onCancel,
                                             showAt);

    }

    public static QPopup createTextInputPopup (final AbstractViewer  viewer,
                                               String                title,
                                               String                icon,
                                               String                message,
                                               String                confirmButtonLabel,
                                               String                cancelButtonLabel,
                                               String                initValue,
                                               final ValueValidator  validator,
                                               final ActionListener  onConfirm,
                                               final ActionListener  onCancel,
                                               Point                 showAt)
    {

        return UIUtils.createTextInputPopup (viewer,
                                             title,
                                             icon,
                                             message,
                                             UIUtils.createTextField (),
                                             confirmButtonLabel,
                                             cancelButtonLabel,
                                             initValue,
                                             validator,
                                             onConfirm,
                                             onCancel,
                                             showAt);

    }

    private static QPopup createTextInputPopup (final AbstractViewer  viewer,
                                                String                title,
                                                String                icon,
                                                String                message,
                                                JTextField            textField,
                                                String                confirmButtonLabel,
                                                String                cancelButtonLabel,
                                                String                initValue,
                                                final ValueValidator  validator,
                                                final ActionListener  onConfirm,
                                                final ActionListener  onCancel,
                                                Point                 showAt)
    {

        final QPopup qp = UIUtils.createPopup (Environment.replaceObjectNames (title),
                                               icon,
                                               null,
                                               true,
                                               onCancel);

        final Box content = new Box (BoxLayout.Y_AXIS);

        JComponent mess = UIUtils.createHelpTextPane (message,
                                                      viewer);
        mess.setBorder (null);
        mess.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                     mess.getPreferredSize ().height));

        content.add (mess);

        content.add (Box.createVerticalStrut (10));

        final JLabel error = UIUtils.createErrorLabel (getUIString (form,errors,novalue));
        //"Please enter a value.");

        error.setVisible (false);
        error.setBorder (UIUtils.createPadding (0, 0, 5, 0));

        final JTextField text = textField;

        text.setMinimumSize (new Dimension (300,
                                            text.getPreferredSize ().height));
        text.setPreferredSize (new Dimension (300,
                                              text.getPreferredSize ().height));
        text.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                            text.getPreferredSize ().height));
        text.setAlignmentX (Component.LEFT_ALIGNMENT);

        if (initValue != null)
        {

            text.setText (initValue);

        }

        error.setAlignmentX (Component.LEFT_ALIGNMENT);

        content.add (error);
        content.add (text);

        content.add (Box.createVerticalStrut (10));

        JButton confirm = null;
        JButton cancel = UIUtils.createButton ((cancelButtonLabel != null ? cancelButtonLabel : getUIString (buttons, LanguageStrings.cancel)),
                                               new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                if (onCancel != null)
                {

                    onCancel.actionPerformed (ev);

                }

                qp.removeFromParent ();

            }

        });

        if (onConfirm != null)
        {

            ActionListener confirmAction = new ActionListener ()
            {

                @Override
                public void actionPerformed (ActionEvent ev)
                {

                    if (validator != null)
                    {

                        String mess = validator.isValid (text.getText ().trim ());

                        if (mess != null)
                        {

                            // Should probably wrap this in a
                            error.setText (mess);

                            error.setVisible (true);

                            // Got to be an easier way of doing this.
                            content.setPreferredSize (null);

                            content.setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                                                     content.getPreferredSize ().height));

                            viewer.showPopupAt (qp,
                                                qp.getLocation (),
                                                false);

                            return;

                        }

                    }

                    onConfirm.actionPerformed (new ActionEvent (text,
                                                                0,
                                                                text.getText ().trim ()));

                    qp.removeFromParent ();

                }

            };

            confirm = UIUtils.createButton ((confirmButtonLabel != null ? confirmButtonLabel : getUIString (buttons, LanguageStrings.confirm)),
                                            confirmAction);

            UIUtils.addDoActionOnReturnPressed (text,
                                                confirmAction);

        }

        JButton[] buts = null;

        if (confirm != null)
        {

            buts = new JButton[] { confirm, cancel };

        } else {

            buts = new JButton[] { cancel };

        }

        JComponent buttons = UIUtils.createButtonBar2 (buts,
                                                       Component.LEFT_ALIGNMENT); //ButtonBarFactory.buildLeftAlignedBar (buts);
        buttons.setAlignmentX (Component.LEFT_ALIGNMENT);
        content.add (buttons);
        content.setBorder (UIUtils.createPadding (10, 10, 10, 10));
        qp.setContent (content);

        content.setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                                 content.getPreferredSize ().height));

        if (showAt == null)
        {

            showAt = UIUtils.getCenterShowPosition (viewer,
                                                    qp);

        }

        qp.setDraggable (viewer);

        viewer.showPopupAt (qp,
                            showAt,
                            false);

        if (initValue != null)
        {

            text.selectAll ();

        }

        text.grabFocus ();

        return qp;

    }

    public static Point getCenterShowPosition (Component parent,
                                               Component show)
    {

        Dimension pd = (parent.isShowing () ? parent.getSize () : parent.getPreferredSize ());
        Dimension sd = (show.isShowing () ? show.getSize () : show.getPreferredSize ());

        return new Point ((pd.width - sd.width) / 2,
                          (pd.height - sd.height) / 2);

    }

    public static void doLater (ActionListener l)
    {

        UIUtils.doLater (l,
                         null);

    }

    public static void doLater (final ActionListener l,
                                final Object         c)
    {

        if (l == null)
        {

            return;

        }

        SwingUtilities.invokeLater (new Runnable ()
        {

            public void run ()
            {

               try
               {

                   l.actionPerformed (new ActionEvent ((c != null ? c : this),
                                                       0,
                                                       "do"));

               } catch (Exception e) {

                    Environment.logError ("Unable to perform action",
                                          e);
/*
                    if (c instanceof Component)
                    {

                        UIUtils.showErrorMessage ((Component) c,
                                                  "Unable to perform action");

                    }
*/
               }

            }

        });


    }

    /**
     * Set the specified empty border (padding) around the component, this is shorthand for:
     *      c.setBorder (UIUtils.createPadding (top, left, bottom, right));
     *
     * @param c The component to set the border on.
     * @param top The top padding.
     * @param left The left padding.
     * @param bottom The bottom padding.
     * @param right The right padding.
     */
    public static void setPadding (JComponent c,
                                   int        top,
                                   int        left,
                                   int        bottom,
                                   int        right)
    {

        c.setBorder (UIUtils.createPadding (top, left, bottom, right));

    }

    /**
     * Is a wrapper for:
     * <code>
     *  new CompoundBorder (new MatteBorder (0, 0, 1, 0, UIUtils.getInnerBorderColor ()),
     *                      UIUtils.createPadding (top, left, bottom, right));
     * </code>
     *
     * @returns A compound border with an outer line and an inner padding.
     */
    public static CompoundBorder createBottomLineWithPadding (int top,
                                                              int left,
                                                              int bottom,
                                                              int right)
    {

        return new CompoundBorder (new MatteBorder (0, 0, 1, 0, UIUtils.getInnerBorderColor ()),
                                   UIUtils.createPadding (top, left, bottom, right));

    }

    /**
     * Is a wrapper for:
     * <code>
     *  new CompoundBorder (new MatteBorder (1, 0, 0, 0, UIUtils.getInnerBorderColor ()),
     *                      UIUtils.createPadding (top, left, bottom, right));
     * </code>
     *
     * @returns A compound border with an outer line and an inner padding.
     */
    public static CompoundBorder createTopLineWithPadding (int top,
                                                           int left,
                                                           int bottom,
                                                           int right)
    {

        return new CompoundBorder (new MatteBorder (1, 0, 0, 0, UIUtils.getInnerBorderColor ()),
                                   UIUtils.createPadding (top, left, bottom, right));

    }

    /**
     * Is a wrapper for:
     * <code>
     *  new CompoundBorder (new MatteBorder (1, 1, 1, 1, UIUtils.getBorderColor ()),
     *                      UIUtils.createPadding (top, left, bottom, right));
     * </code>
     *
     * @returns A compound border with an outer line and an inner padding.
     */
    public static CompoundBorder createLineBorderWithPadding (int top,
                                                              int left,
                                                              int bottom,
                                                              int right)
    {

        return new CompoundBorder (new MatteBorder (1, 1, 1, 1, UIUtils.getBorderColor ()),
                                   UIUtils.createPadding (top, left, bottom, right));

    }

    public static EmptyBorder createPadding (int top,
                                             int left,
                                             int bottom,
                                             int right)
    {

        return new EmptyBorder (top, left, bottom, right);

    }

    public static void closePopupParent (Container parent)
    {

        if (parent == null)
        {

            return;

        }

        if (parent instanceof QPopup)
        {

            ((QPopup) parent).removeFromParent ();

            return;

        }

        UIUtils.closePopupParent (parent.getParent ());

    }

    public static void resizeParent (final Container parent)
    {

        if (parent == null)
        {

            return;

        }

        if (parent instanceof QPopup)
        {

            ((QPopup) parent).resize ();

            return;

        }

        if (parent instanceof PopupWindow)
        {

            ((PopupWindow) parent).resize ();

            return;

        }

        UIUtils.resizeParent (parent.getParent ());

    }

    public static QPopup createWizardPopup (String         title,
                                            String         iconType,
                                            ActionListener onClose,
                                            Wizard         wizard)
    {

        QPopup p = UIUtils.createClosablePopup (title,
                                                (iconType != null ? Environment.getIcon (iconType, Constants.ICON_POPUP) : null),
                                                onClose);

        wizard.init ();

        wizard.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                        wizard.getPreferredSize ().height));
        wizard.setBorder (UIUtils.createPadding (10, 10, 10, 10));

        p.setContent (wizard);

        return p;

    }

    /**
     * Return a new value validator that ensures that the word (case-insensitive) "yes" is provided.
     *
     * @returns The validator.
     */
    public static ValueValidator getYesValueValidator ()
    {

        return new ValueValidator<String> ()
        {

            public String isValid (String v)
            {

                if ((v == null)
                    ||
                    (!v.trim ().equalsIgnoreCase (getUIString (form,affirmativevalue)))
                   )
                {

                    return getUIString (form,errors,affirmativevalue);
                    //"Please enter the word Yes below.";

                }

                return null;

            }

        };

    }

    public static JTextField createSearchBox (final int            delay,
                                              final ActionListener onSearch)
    {

        final JTextField text = UIUtils.createTextField ();
        text.setBorder (new CompoundBorder (UIUtils.createPadding (5, 10, 5, 10),
                                            text.getBorder ()));

        KeyAdapter vis = new KeyAdapter ()
        {

            private javax.swing.Timer searchT = new javax.swing.Timer (delay,
                                               onSearch);

            public void keyPressed (KeyEvent ev)
            {

                this.searchT.setRepeats (false);
                this.searchT.stop ();

                // If enter was pressed then search, don't start the timer.
                if (ev.getKeyCode () == KeyEvent.VK_ENTER)
                {

                    if (onSearch != null)
                    {

                        onSearch.actionPerformed (new ActionEvent (text, 1, text.getText ().trim ()));

                    }


                    return;

                }

                this.searchT.start ();

            }

        };

        text.addKeyListener (vis);
        text.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                            text.getPreferredSize ().height));

        text.addMouseListener (new MouseEventHandler ()
        {

            @Override
            public void mouseEntered (MouseEvent ev)
            {

                text.grabFocus ();
                text.selectAll ();

            }

        });

        return text;

    }

    public static JComponent showTemporaryNotification (String                t,
                                                        AbstractProjectViewer parent,
                                                        Component             showAt,
                                                        int                   showFor)
    {

        Box outer = new Box (BoxLayout.Y_AXIS);

        outer.setBorder (QPopup.defaultBorder);
        outer.setOpaque (false);
        outer.setDoubleBuffered (true);
        outer.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                            Short.MAX_VALUE));
        Box inner = new Box (BoxLayout.Y_AXIS);
        inner.setBackground (UIUtils.getComponentColor ());
        inner.setOpaque (true);

        inner.setBorder (UIUtils.createPadding (5, 5, 5, 5));

        JTextPane m = UIUtils.createHelpTextPane (t,
                                                  parent);
        m.setSize (new Dimension (150 - 20,
                                  m.getPreferredSize ().height));

        inner.add (m);

        outer.add (inner);

        parent.showPopupAt (outer,
                            showAt,
                            false);

        return outer;

    }

    public static javax.swing.Timer createCyclicAnimator (final PropertyChangeListener cycleUp,
                                                          final PropertyChangeListener cycleDown,
                                                          final int                    fps,
                                                          final int                    duration,
                                                          final int                    startValue,
                                                          final int                    endValue,
                                                          final int                    repeatCount,
                                                          final ActionListener         onComplete)
    {

        final boolean incr = (startValue <= endValue);

        final AtomicInteger count = new AtomicInteger (0);

        final Map<String, javax.swing.Timer> animators = new HashMap ();

        final javax.swing.Timer up = UIUtils.createAnimator (cycleUp,
                                           fps,
                                           duration / 2,
                                           (incr ? startValue : endValue),
                                           (incr ? endValue : startValue),
                                           new ActionListener ()
                                           {

                                                public void actionPerformed (ActionEvent ev)
                                                {

                                                    if (incr)
                                                    {

                                                        count.incrementAndGet ();

                                                    }

                                                    if ((!incr)
                                                        &&
                                                        (count.get () == repeatCount)
                                                       )
                                                    {

                                                        if (onComplete != null)
                                                        {

                                                            onComplete.actionPerformed (new ActionEvent ("complete", endValue, "complete"));

                                                        }

                                                        return;

                                                    }

                                                    animators.get ("down").restart ();

                                                }

                                           });

        animators.put ("up",
                       up);

        final javax.swing.Timer down = UIUtils.createAnimator (cycleDown,
                                           fps,
                                           duration / 2,
                                           (incr ? endValue : startValue),
                                           (incr ? startValue : endValue),
                                           new ActionListener ()
                                           {

                                                public void actionPerformed (ActionEvent ev)
                                                {


                                                    if (!incr)
                                                    {

                                                        count.incrementAndGet ();

                                                    }

                                                    if ((incr)
                                                        &&
                                                        (count.get () == repeatCount)
                                                       )
                                                    {

                                                        if (onComplete != null)
                                                        {

                                                            onComplete.actionPerformed (new ActionEvent ("complete", endValue, "complete"));

                                                        }

                                                        return;

                                                    }

                                                    animators.get ("up").restart ();

                                                }

                                           });

        animators.put ("down",
                       down);

        javax.swing.Timer t = new javax.swing.Timer (0, null);

        t.addActionListener (new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                if (incr)
                {

                    up.start ();

                } else {

                    down.start ();

                }

            }

        });

        t.setRepeats (false);

        return t;

    }

    public static javax.swing.Timer createAnimator (final PropertyChangeListener l,
                                                    final int                    fps,
                                                    final int                    duration,
                                                    final double                 startValue,
                                                    final double                 endValue,
                                                    final ActionListener         onComplete)
    {

        final javax.swing.Timer t = new javax.swing.Timer (1000 / fps, null);

        final double increment = ((endValue - startValue) / (double) fps) * (1000d / (double) duration);

        final boolean incr = (startValue <= endValue);

        t.addActionListener (new ActionListener ()
        {

            private double v = startValue;

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                if ((incr && (this.v >= endValue))
                    ||
                    (!incr && (this.v <= endValue))
                   )
                {

                    t.stop ();

                    if (onComplete != null)
                    {

                        this.v = startValue;

                        onComplete.actionPerformed (new ActionEvent ("complete", 1, "complete"));

                    }

                    return;

                }

                double nv = this.v + increment;

                if ((incr)
                    &&
                    (nv > endValue)
                   )
                {

                    nv = endValue;

                }

                if ((!incr)
                    &&
                    (nv < endValue)
                   )
                {

                    nv = endValue;

                }

                l.propertyChange (new PropertyChangeEvent (this,
                                                           "value",
                                                           this.v,
                                                           nv));

                this.v = nv;

            }

        });

        t.setCoalesce (true);

        return t;

    }

    public static void askForPasswordForProject (final ProjectInfo            proj,
                                                       ValueValidator<String> validator,
                                                 final ActionListener         onProvided,
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

                    public String isValid (String v)
                    {

                        java.util.List<String> prefix = Arrays.asList (project,actions,openproject,enterpasswordpopup,errors);

                        if ((v == null)
                            ||
                            (v.trim ().equals (""))
                           )
                        {

                            return getUIString (prefix,novalue);
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

                                return getUIString (prefix,projectalreadyopen);
                                //"Sorry, the {project} appears to already be open in Quoll Writer.  Please close all other instances of Quoll Writer first before trying to open the {project}.";

                            }

                            if (ObjectManager.isEncryptionException (e))
                            {

                                return getUIString (prefix,invalidpassword);
                                //"Password is not valid.";

                            }

                            Environment.logError ("Cant open project: " +
                                                  proj,
                                                  e);

                            UIUtils.showErrorMessage (parentViewer,
                                                      getUIString (prefix,general));
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

            UIUtils.createPasswordInputPopup (parentViewer,
                                              getUIString (prefix,title),
                                              //"Password required",
                                              Constants.PROJECT_ICON_NAME,
                                              String.format (getUIString (prefix,text),
                                              //"{Project} <b>%s</b> is encrypted, please enter the password to unlock it below.",
                                                         proj.getName ()),
                                              getUIString (prefix,buttons,open),
                                              //"Open",
                                              getUIString (prefix,buttons,cancel),
                                              //Constants.CANCEL_BUTTON_LABEL_ID,
                                              null,
                                              validator,
                                              new ActionListener ()
                                              {

                                                  @Override
                                                  public void actionPerformed (ActionEvent ev)
                                                  {

                                                      proj.setFilePassword (ev.getActionCommand ());

                                                      if (onProvided != null)
                                                      {

                                                        onProvided.actionPerformed (new ActionEvent (proj,
                                                                                                     1,
                                                                                                     "provided"));

                                                      }

                                                  }

                                              },
                                              null,
                                              null);

        } else {

            onProvided.actionPerformed (new ActionEvent (proj, 1, "provided"));

        }

    }

    public static JComboBox getSpellCheckLanguagesSelector (final ActionListener onSelect,
                                                            final String         defLang)
    {

        final JComboBox spellcheckLang = new JComboBox ();

        if (onSelect != null)
        {

            spellcheckLang.addItemListener (new ItemAdapter ()
            {

               public void itemStateChanged (ItemEvent ev)
               {

                    if (ev.getStateChange () != ItemEvent.SELECTED)
                    {

                        return;

                    }

                    final String lang = spellcheckLang.getSelectedItem ().toString ();

                    onSelect.actionPerformed (new ActionEvent (spellcheckLang, 1, lang));

               }

            });

        }

        // Get the languages supported by the spellchecker.
        new Thread (new Runnable ()
        {

            public void run ()
            {

                String l = null;

                try
                {

                    l = Environment.getUrlFileAsString (new URL (Environment.getQuollWriterWebsite () + "/" + Environment.getProperty (Constants.QUOLL_WRITER_SUPPORTED_LANGUAGES_URL_PROPERTY_NAME)));

                } catch (Exception e) {

                    // Something gone wrong, so just add english.
                    l = Constants.ENGLISH;

                    Environment.logError ("Unable to get language files url",
                                          e);

                }

                StringTokenizer t = new StringTokenizer (l,
                                                         String.valueOf ('\n'));

                final Vector langs = new Vector ();

                while (t.hasMoreTokens ())
                {

                    String lang = t.nextToken ().trim ();

                    if (lang.equals (""))
                    {

                        continue;

                    }

                    langs.add (lang);

                }

                SwingUtilities.invokeLater (new Runnable ()
                {

                    public void run ()
                    {

                        spellcheckLang.setModel (new DefaultComboBoxModel (langs));
                        spellcheckLang.setSelectedItem (defLang);
                        spellcheckLang.setEnabled (true);

                    }

                });

            }

        }).start ();

        return spellcheckLang;

    }

	public static void showManageBackups (final ProjectInfo    proj,
                                          final AbstractViewer viewer)
	{

        String popupName = "managebackups";
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

        Environment.fireUserProjectEvent (viewer,
                                          ProjectEvent.BACKUPS,
                                          ProjectEvent.SHOW);

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

        UIUtils.createQuestionPopup (viewer,
                                     getUIString (backups,_new,popup,title),
                                     //"Create a Backup",
                                     Constants.SNAPSHOT_ICON_NAME,
                                     String.format (getUIString (backups,_new,popup,text),
                                                    //"Please confirm you wish to create a backup of {project} <b>%s</b>.",
                                                    proj.getName ()),
                                     getUIString (backups,_new,popup,buttons,confirm),
                                     //"Yes, create it",
                                     getUIString (backups,_new,popup,buttons,cancel),
                                     //null,
                                     new ActionAdapter ()
                                     {

                                        public void actionPerformed (ActionEvent ev)
                                        {

                                            try
                                            {

                                                File f = Environment.createBackupForProject (proj,
                                                                                             false);

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

                                                UIUtils.showMessage ((PopupsSupported) viewer,
                                                                     getUIString (backups,_new,confirmpopup,title),
                                                                    //"Backup created",
                                                                     b);

                                            } catch (Exception e)
                                            {

                                                Environment.logError ("Unable to create backup of project: " +
                                                                      proj,
                                                                      e);

                                                UIUtils.showErrorMessage (viewer,
                                                                          getUIString (backups,_new,actionerror));
                                                                          //"Unable to create backup.");

                                            }

                                        }

                                    },
                                    null,
                                    null,
                                    null);

    }

    public static void downloadDictionaryFiles (String               lang,
                                                final AbstractViewer parent,
                                                final ActionListener onComplete)
    {

        if (Environment.isEnglish (lang))
        {

            lang = Constants.ENGLISH;

        }

        final String langOrig = lang;
        final String language = lang;

        String fileLang = lang;

        // Legacy, if the user doesn't have the language file but DOES have a thesaurus then just
        // download the English-dictionary-only.zip.
        if ((Environment.isEnglish (lang))
            &&
            (!Environment.getDictionaryFile (lang).exists ())
            &&
            (Environment.hasSynonymsDirectory (lang))
           )
        {

            fileLang = "English-dictionary-only";

        }

        URL url = null;

        try
        {

            url = new URL (Environment.getQuollWriterWebsite () + "/" + StringUtils.replaceString (Environment.getProperty (Constants.QUOLL_WRITER_LANGUAGE_FILES_URL_PROPERTY_NAME),
                                                                                                   "[[LANG]]",
                                                                                                   StringUtils.replaceString (fileLang,
                                                                                                                              " ",
                                                                                                                              "%20")));

        } catch (Exception e) {

            Environment.logError ("Unable to download language files, cant create url",
                                  e);

            UIUtils.showErrorMessage (parent,
                                      getUIString (dictionary,download,actionerror));
                                      //"Unable to download language files");

            return;

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

            UIUtils.showErrorMessage (parent,
                                      getUIString (dictionary,download,actionerror));
                                    //"Unable to download language files");

            return;

        }

        _file.deleteOnExit ();

        final File file = _file;

        Box b = new Box (BoxLayout.Y_AXIS);

        final JTextPane htmlP = UIUtils.createHelpTextPane (String.format (getUIString (dictionary,download,notification),
                                                                            //"The language files for <b>%s</b> are now being downloaded.",
                                                                           language),
                                                            parent);
        htmlP.setBorder (null);
        htmlP.setBackground (null);
        htmlP.setOpaque (false);
        htmlP.setAlignmentX (Component.LEFT_ALIGNMENT);

        b.add (htmlP);
        b.add (Box.createVerticalStrut (10));

        final JProgressBar prog = new JProgressBar (0, 100);

        prog.setPreferredSize (new Dimension (500, 25));
        prog.setMaximumSize (new Dimension (500, 25));
        prog.setAlignmentX (Component.LEFT_ALIGNMENT);

        b.add (prog);

        final Notification n = parent.addNotification (b,
                                                       Constants.DOWNLOAD_ICON_NAME,
                                                       -1,
                                                       null);

        final ActionListener removeNotification = new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

               n.removeNotification ();

            }
        };

        final UrlDownloader downloader = new UrlDownloader (url,
                                                            file,
                                                            new DownloadListener ()
                                                            {

                                                                public void handleError (Exception e)
                                                                {

                                                                    UIUtils.doLater (removeNotification);

                                                                    Environment.logError ("Unable to download language files",
                                                                                          e);

                                                                    UIUtils.showErrorMessage (parent,
                                                                                              getUIString (dictionary,download,actionerror));
                                                                                              //"A problem has occurred while downloading the language files for <b>" + langOrig + "</b>.<br /><br />Please contact Quoll Writer support for assistance.");

                                                                }

                                                                public void progress (final int downloaded,
                                                                                      final int total)
                                                                {

                                                                    UIUtils.doLater (new ActionListener ()
                                                                    {

                                                                        @Override
                                                                        public void actionPerformed (ActionEvent ev)
                                                                        {

                                                                            int val = (int) (((double) downloaded / (double) total) * 100);

                                                                            prog.setValue (val);

                                                                        }

                                                                    });

                                                                }

                                                                public void finished (int total)
                                                                {

                                                                    prog.setValue (100);
                                                                    prog.setIndeterminate (true);

                                                                    new Thread (new Runner ()
                                                                    {

                                                                        public void run ()
                                                                        {

                                                                            // Now extract the file into the relevant directory.
                                                                            try
                                                                            {

                                                                                Utils.extractZipFile (file,
                                                                                                      Environment.getUserQuollWriterDir ());

                                                                            } catch (Exception e) {

                                                                                Environment.logError ("Unable to extract language zip file: " +
                                                                                                      file +
                                                                                                      " to: " +
                                                                                                      Environment.getUserQuollWriterDir (),
                                                                                                      e);

                                                                                 UIUtils.showErrorMessage (parent,
                                                                                                           getUIString (dictionary,download,actionerror));

                                                                                return;

                                                                            } finally {

                                                                                file.delete ();

                                                                            }

                                                                            if (onComplete != null)
                                                                            {

                                                                                UIUtils.doLater (new ActionListener ()
                                                                                {

                                                                                    @Override
                                                                                    public void actionPerformed (ActionEvent ev)
                                                                                    {

                                                                                        prog.setIndeterminate (false);

                                                                                        onComplete.actionPerformed (new ActionEvent (parent, 0, langOrig));

                                                                                    }

                                                                                });

                                                                            }

                                                                            UIUtils.doLater (removeNotification);

                                                                        }

                                                                    }).start ();

                                                                }

                                                            });

        downloader.start ();

        n.addCancelListener (new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                downloader.stop ();

                file.delete ();

            }

        });

    }

    public static Form createForm (Set<FormItem> items)
    {

        return new Form (Form.Layout.stacked,
                         items,
                         (Set) null);

    }

    public static Form createForm (Set<FormItem>                    items,
                                   Map<Form.Button, ActionListener> buttons)
    {

        return new Form (Form.Layout.stacked,
                         items,
                         buttons);

    }

    public static Form createForm (Set<FormItem>       items,
                                   Set<JButton> buttons)
    {

        return new Form (Form.Layout.stacked,
                         items,
                         buttons);

    }

	public static void showAddNewProject (AbstractViewer viewer,
                                          Point          showAt,
                                          ActionListener onCreate)
	{

        final QPopup popup = UIUtils.createClosablePopup (getUIString (newproject, LanguageStrings.popup,title),
                                                        //"Create a new {project}",
                                                          Environment.getIcon (Constants.ADD_ICON_NAME,
                                                                               Constants.ICON_POPUP),
                                                          null);

		Box content = new Box (BoxLayout.Y_AXIS);

		content.add (UIUtils.createHelpTextPane (getUIString (newproject, LanguageStrings.popup,text),
                                                //"To create a new {Project} enter the name below, select the directory it should be saved to and press the Create button.",
										         null));

		final NewProjectPanel newProjPanel = new NewProjectPanel ();

		JComponent cp = newProjPanel.createPanel (popup,
												  popup.getCloseAction (),
												  true,
												  popup.getCloseAction (),
												  true);

		content.add (cp);

        content.setBorder (UIUtils.createPadding (10, 10, 10, 10));

        content.setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                     content.getPreferredSize ().height));

        popup.setContent (content);

        viewer.showPopupAt (popup,
                            (showAt != null ? showAt : UIUtils.getCenterShowPosition (viewer,
                                                                                      popup)),
                            false);

        popup.resize ();

        popup.setDraggable (viewer);

	}

    public static JComponent getChapterInfoPreview (Chapter               c,
                                                    String                format,
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

        String text = format;

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

        text = StringUtils.replaceString (text,
                                          " ",
                                          "&nbsp;");
        text = StringUtils.replaceString (text,
                                          nl,
                                          "<br />");

        text = StringUtils.replaceString (text,
                                          Constants.DESCRIPTION_TAG,
                                          desc);

        text = StringUtils.replaceString (text,
                                          Constants.DESCRIPTION_FIRST_LINE_TAG,
                                          descFirstLine);

        text = StringUtils.replaceString (text,
                                          Constants.CHAPTER_FIRST_LINE_TAG,
                                          chapText);

        ChapterCounts cc = viewer.getChapterCounts (c);

        if (cc == null)
        {

            // Get the current text instead.
            cc = new ChapterCounts (c.getChapterText ());

        }

        text = StringUtils.replaceString (text,
                                          Constants.WORDS_TAG,
                                          String.format (getUIString (project,sidebar,chapters,preview,words),
                                                        //"%s words",
                                                         Environment.formatNumber (cc.wordCount)));

        text = StringUtils.replaceString (text,
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

                ep = Environment.getPercent (ecc.wordCount, cc.wordCount);

            }

        }

        if (ep < 0)
        {

            ep = 0;

        }

        text = StringUtils.replaceString (text,
                                          Constants.EDIT_COMPLETE_TAG,
                                          String.format (getUIString (project,sidebar,chapters,preview,editcomplete),
                                          //"%s%% complete",
                                                         Environment.formatNumber (ep)));

        if (text.contains (Constants.PROBLEM_FINDER_PROBLEM_COUNT_TAG))
        {

            text = StringUtils.replaceString (text,
                                              Constants.PROBLEM_FINDER_PROBLEM_COUNT_TAG,
                                              String.format (getUIString (project,sidebar,chapters,preview,problemcount),
                                                            //"%s problems",
                                                             Environment.formatNumber (viewer.getProblems (c).size ())));

        }

        if (text.contains (Constants.SPELLING_ERROR_COUNT_TAG))
        {

            text = StringUtils.replaceString (text,
                                              Constants.SPELLING_ERROR_COUNT_TAG,
                                              String.format (getUIString (project,sidebar,chapters,preview,spellingcount),
                                                            //"%s spelling errors",
                                                             Environment.formatNumber (viewer.getSpellingErrors (c).size ())));

        }

        ReadabilityIndices ri = viewer.getReadabilityIndices (c);

        if (ri == null)
        {

            ri = new ReadabilityIndices ();
            ri.add (c.getChapterText ());

        }

        String na = getUIString (project,sidebar,chapters,preview,notapplicable);

        String GL = na; //"N/A";
        String RE = na; //"N/A";
        String GF = na; //"N/A";

        if (cc.wordCount > Constants.MIN_READABILITY_WORD_COUNT)
        {

            GL = Environment.formatNumber (Math.round (ri.getFleschKincaidGradeLevel ()));
            RE = Environment.formatNumber (Math.round (ri.getFleschReadingEase ()));
            GF = Environment.formatNumber (Math.round (ri.getGunningFogIndex ()));

        }

        text = StringUtils.replaceString (text,
                                          Constants.READABILITY_TAG,
                                          String.format (getUIString (project,sidebar,chapters,preview,readability),
                                          //"GL: %s, RE: %s, GF: %s",
                                                         GL,
                                                         RE,
                                                         GF));

         JEditorPane p = UIUtils.createHelpTextPane (text,
                                                     null);
/*
         p.setSize (new Dimension (380,
                                   0));
                                   */
         p.setAlignmentX (Component.LEFT_ALIGNMENT);

        return p;

    }

    public static void showAddNewObjectType (AbstractViewer viewer)
    {

        UserConfigurableObjectType utype = new UserConfigurableObjectType ();

        utype.setObjectTypeName (getUIString (userobjects,type,_new,defaults,names,singular));
        //"Widget");
        utype.setObjectTypeNamePlural (getUIString (userobjects,type,_new,defaults,names,plural));
        //"Widgets");
        utype.setLayout (null);
        utype.setAssetObjectType (true);
        utype.setIcon24x24 (Environment.getIcon ("whats-new",
                                                 Constants.ICON_TITLE));
        utype.setIcon16x16 (Environment.getIcon ("whats-new",
                                                 Constants.ICON_SIDEBAR));

        // Name
        ObjectNameUserConfigurableObjectTypeField nameF = new ObjectNameUserConfigurableObjectTypeField ();

        nameF.setFormName (getUIString (userobjects,type,_new,defaults,fields,name));
        //"Name");

        utype.addConfigurableField (nameF);

        // Description
        ObjectDescriptionUserConfigurableObjectTypeField cdescF = new ObjectDescriptionUserConfigurableObjectTypeField ();

        cdescF.setSearchable (true);
        cdescF.setFormName (getUIString (userobjects,type,_new,defaults,fields,description));
        //"Description");

        utype.addConfigurableField (cdescF);

        Wizard w = UserConfigurableObjectTypeEdit.getAsWizard (viewer,
                                                               utype);

        final QPopup p = UIUtils.createWizardPopup (getUIString (userobjects,type,_new,popup,title),
                                                    //"Add a new type of Object",
                                                    Constants.NEW_ICON_NAME,
                                                    null,
                                                    w);
        w.setPreferredSize (new Dimension (UIUtils.getPopupWidth () - 20,
                              w.getPreferredSize ().height));

        viewer.showPopupAt (p,
                            UIUtils.getCenterShowPosition (viewer,
                                                           p),
                            false);
        p.setDraggable (viewer);

        UIUtils.doLater (new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                UIUtils.resizeParent (p);

            }

        });


    }

    public static void showObjectTypeEdit (UserConfigurableObjectType utype,
                                           AbstractViewer             viewer)
    {

        final QPopup p = UIUtils.createClosablePopup (String.format (getUIString (userobjects,type,edit,popup,title),
                                                                    //"Edit the %s information",
                                                                     utype.getObjectTypeName ()),
                                                      Environment.getIcon (Constants.EDIT_ICON_NAME,
                                                                           Constants.ICON_POPUP),
                                                      null);

        Box b = new Box (BoxLayout.Y_AXIS);

        b.setBorder (UIUtils.createPadding (10, 10, 10, 10));

        JTextPane m = UIUtils.createHelpTextPane (String.format (getUIString (userobjects,type,edit,popup,text),
                                                                //"Use this popup to add or edit the fields, layout and information for your %s.",
                                                                 utype.getObjectTypeNamePlural ()),
                                                  viewer);

        m.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                  m.getPreferredSize ().height));
        m.setBorder (null);

        b.add (m);

        b.add (Box.createVerticalStrut (5));

        b.add (UserConfigurableObjectTypeEdit.getAsTabs (viewer,
                                                         utype));

        JButton finishb = new JButton (getUIString (userobjects,type,edit,popup,buttons,finish));
        //"Finish");

        finishb.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                UIUtils.closePopupParent (p);

            }

        });

        JButton[] fbuts = new JButton[] { finishb };

        JPanel bp = UIUtils.createButtonBar2 (fbuts,
                                              Component.CENTER_ALIGNMENT);
        bp.setOpaque (false);

        bp.setAlignmentX (Component.LEFT_ALIGNMENT);
        b.add (Box.createVerticalStrut (10));

        b.add (bp);

        p.setContent (b);

        b.setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                           b.getPreferredSize ().height));

        viewer.showPopupAt (p,
                            UIUtils.getCenterShowPosition (viewer,
                                                           p),
                                 false);
        p.setDraggable (viewer);

    }

    public static JButton createTagsMenuToolBarButton (final NamedObject           obj,
                                                       final AbstractProjectViewer viewer)
    {

        JButton b = UIUtils.createToolBarButton (Constants.TAG_ICON_NAME,
                                                 null,
                                                 null,
                                                 new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                JPopupMenu tagMenu = new JPopupMenu ();

                Set<Tag> allTags = null;

                try
                {

                    allTags = Environment.getAllTags ();

                } catch (Exception e) {

                    Environment.logError ("Unable to get all tags",
                                          e);

                    return;

                }

                for (Tag t : allTags)
                {

                    final JCheckBox it = new JCheckBox (t.getName (),
                                                        obj.hasTag (t));
                    it.setBorder (UIUtils.createPadding (3, 3, 3, 3));

                    it.addActionListener (new ActionListener ()
                    {

                        @Override
                        public void actionPerformed (ActionEvent ev)
                        {

                            if (it.isSelected ())
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

                                UIUtils.showErrorMessage (viewer,
                                                          getUIString (tags,actions,apply,actionerror));
                                                          //"Unable to add/remove tag.");

                                return;

                            }

                            viewer.reloadTreeForObjectType (TaggedObjectAccordionItem.ID_PREFIX + t.getKey ());

                        }

                    });

                    tagMenu.add (it);

                }

                if (allTags.size () > 0)
                {

                    tagMenu.addSeparator ();

                }

                tagMenu.add (UIUtils.createMenuItem (getUIString (tags,popupmenu,_new),
                                                    //"Add New Tag(s)",
                                                    Constants.EDIT_ICON_NAME,
                                                    new ActionListener ()
                                                    {

                                                         @Override
                                                         public void actionPerformed (ActionEvent ev)
                                                         {

                                                            new AddNewTagActionHandler (obj,
                                                                                        viewer).actionPerformed (ev);

                                                         }

                                                    }));

                Component c = (Component) ev.getSource ();

                tagMenu.show (c,
                              c.getX (),
                              c.getY ());

            }

        });

        return b;

    }

    public static JMenu createTagsMenu (final NamedObject           obj,
                                        final AbstractProjectViewer viewer)
    {

        JMenu tagMenu = new JMenu (getUIString (tags,popupmenu,title));
        //"Tags");

        tagMenu.setIcon (Environment.getIcon (Constants.TAG_ICON_NAME,
                                              Constants.ICON_MENU));

        Set<Tag> allTags = null;

        try
        {

            allTags = Environment.getAllTags ();

        } catch (Exception e) {

            Environment.logError ("Unable to get all tags",
                                  e);

            return tagMenu;

        }

        for (Tag t : allTags)
        {

            final JCheckBox it = new JCheckBox (t.getName (),
                                                obj.hasTag (t));
            it.setBorder (UIUtils.createPadding (3, 3, 3, 3));

            it.addActionListener (new ActionListener ()
            {

                @Override
                public void actionPerformed (ActionEvent ev)
                {

                    if (it.isSelected ())
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

                        UIUtils.showErrorMessage (viewer,
                                                  getUIString (tags,actions,apply,actionerror));
                                                  //"Unable to add/remove tag.");

                        return;

                    }

                    viewer.reloadTreeForObjectType (TaggedObjectAccordionItem.ID_PREFIX + t.getKey ());

                }

            });

            tagMenu.add (it);

        }

        if (allTags.size () > 0)
        {

            tagMenu.addSeparator ();

        }

        tagMenu.add (UIUtils.createMenuItem (getUIString (tags,popupmenu,_new),
                                            //"Add New Tag(s)",
                                            Constants.ADD_ICON_NAME,
                                            new ActionListener ()
                                            {

                                                 @Override
                                                 public void actionPerformed (ActionEvent ev)
                                                 {

                                                     new AddNewTagActionHandler (obj,
                                                                                 viewer).actionPerformed (ev);

                                                 }

                                            }));

        return tagMenu;

    }

}
