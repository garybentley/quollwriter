package com.quollwriter.ui.panels;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;

import java.io.*;

import java.lang.reflect.*;

import java.text.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.tree.*;

import com.gentlyweb.properties.*;

import com.gentlyweb.utils.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.data.*;

import com.quollwriter.events.*;

import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.IconProvider;
import com.quollwriter.ui.components.FormItem;

public class ChapterViewPanel extends AbstractObjectViewPanel
{

    private JTree outlineTree = null;

    public ChapterViewPanel(ProjectViewer pv,
                            Chapter       c)
                     throws GeneralException
    {

        super (pv,
               c);

    }

    private void createOutlineTreePopupMenu (MouseEvent ev)
    {

        final ChapterViewPanel _this = this;

        final TreePath tp = _this.outlineTree.getPathForLocation (ev.getX (),
                                                                  ev.getY ());

        final JPopupMenu m = new JPopupMenu ();

        JMenuItem mi = null;

        if (tp != null)
        {

            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent ();

            final DataObject d = (DataObject) node.getUserObject ();

            if (d instanceof OutlineItem)
            {

                final OutlineItem oi = (OutlineItem) d;

                mi = new JMenuItem ("View",
                                    Environment.getIcon ("view",
                                                         Constants.ICON_MENU));
                mi.addActionListener (new ActionAdapter ()
                    {

                        public void actionPerformed (ActionEvent ev)
                        {

                            _this.projectViewer.viewObject (oi);

                        }

                    });

                m.add (mi);

                mi = new JMenuItem ("Edit",
                                    Environment.getIcon ("edit",
                                                         Constants.ICON_MENU));
                mi.addActionListener (this.projectViewer.getAction (ProjectViewer.EDIT_PLOT_OUTLINE_ITEM_ACTION,
                                                                    oi));
                m.add (mi);

                mi = new JMenuItem ("Delete",
                                    Environment.getIcon ("delete",
                                                         Constants.ICON_MENU));
                mi.addActionListener (this.projectViewer.getAction (ProjectViewer.DELETE_PLOT_OUTLINE_ITEM_ACTION,
                                                                    oi));
                m.add (mi);

                mi = new JMenuItem ("Add New " + Environment.getObjectTypeName (OutlineItem.OBJECT_TYPE) + " Below",
                                    Environment.getIcon (OutlineItem.OBJECT_TYPE,
                                                         Constants.ICON_MENU));
                mi.addActionListener (this.projectViewer.getAction (ProjectViewer.NEW_PLOT_OUTLINE_ITEM_BELOW_ACTION,
                                                                    oi.getChapter ()));
                m.add (mi);

            }

        }

        m.show ((Component) ev.getSource (),
                ev.getX (),
                ev.getY ());

    }

    public EditPanel getBottomEditPanel ()
    {

        if (true)
        {

            return null;

        }

        final ChapterViewPanel _this = this;

        return new EditPanel (true)
        {

            public void refreshViewPanel ()
            {


            }

            public boolean handleSave ()
            {

                return true;

            }

            public java.util.List<FormItem> getEditItems ()
            {

                return null;

            }

            public java.util.List<FormItem> getViewItems ()
            {

                return null;

            }

            public boolean handleCancel ()
            {

                return true;

            }

            public void handleEditStart ()
            {


            }

            public IconProvider getIconProvider ()
            {

                DefaultIconProvider iconProv = new DefaultIconProvider ();
                iconProv.putIcon ("header",
                                  OutlineItem.OBJECT_TYPE);

                return iconProv;

            }

            public String getHelpText ()
            {

                return null;

            }

            public String getTitle ()
            {

                return "Outline";

            }

            public JComponent getEditPanel ()
            {

                return null;

            }

            public JComponent getViewPanel ()
            {

                _this.outlineTree = UIUtils.createTree ();

                _this.outlineTree.setDragEnabled (true);

                ((DefaultTreeModel) _this.outlineTree.getModel ()).setRoot (UIUtils.createTree ((Chapter) _this.obj,
                                                                                                null,
                                                                                                null,
                                                                                                false));

                JScrollPane sp = UIUtils.createTreeScroll (_this.outlineTree);

                _this.outlineTree.setDropTarget (new DropTarget (_this.outlineTree,
                                                                 DataObjectTransferHandler.getDropHandler ()));

                // _this.outlineTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                _this.outlineTree.setDropMode (DropMode.ON);
                // _this.outlineTree.setTransferHandler (new DataObjectTransferHandler (_this.projectViewer));

                sp.setOpaque (false);
                // sp.getViewport ().setOpaque (false);

                _this.outlineTree.addMouseListener (new MouseAdapter ()
                    {

                        public void mouseReleased (MouseEvent ev)
                        {

                            if (ev.isPopupTrigger ())
                            {

                                _this.createOutlineTreePopupMenu (ev);

                            }

                        }

                        public void mousePressed (MouseEvent ev)
                        {

                            if (ev.isPopupTrigger ())
                            {

                                _this.createOutlineTreePopupMenu (ev);

                            }

                        }

                        public void mouseClicked (MouseEvent ev)
                        {

                            // View the item.
                            TreePath tp = _this.outlineTree.getPathForLocation (ev.getX (),
                                                                                ev.getY ());

                            if (tp == null)
                            {

                                return;

                            }

                            DataObject d = (DataObject) ((DefaultMutableTreeNode) tp.getLastPathComponent ()).getUserObject ();

                            if ((ev.getClickCount () == 2) &&
                                (!ev.isPopupTrigger ()))
                            {

                                _this.projectViewer.viewObject (d);

                            }

                        }

                    });

                return sp;

            }

        };

    }

    public void doFillToolBar (JToolBar tb,
                               boolean  fullScreen)
    {

    }

    public Color getHeaderColor ()
    {

        return null;

    }

    public String getTitle ()
    {
        
        return this.obj.getName () + " - Information";
        
    }
    
    public String getIconType ()
    {

        return "chapter-information";

    }

    public ActionListener getDeleteObjectAction (final ProjectViewer pv,
                                                 final NamedObject   n)
    {

        return new DeleteChapterActionHandler ((Chapter) n,
                                               pv);

    }

    public DetailsEditPanel getDetailEditPanel (ProjectViewer pv,
                                                NamedObject   n)
    {

        return new ChapterDetailsEditPanel ((Chapter) n,
                                            pv);

    }

    public String getPanelId ()
    {

        return ChapterViewPanel.getPanelIdForObject (this.obj);

    }

    public static String getPanelIdForObject (NamedObject n)
    {

        return new ObjectReference (Chapter.INFORMATION_OBJECT_TYPE,
                                    n.getKey (),
                                    null).asString ();

    }

    public void doInit ()
    {

    }

    public void doRefresh ()
    {

    }

    public void close ()
    {


    }

}
