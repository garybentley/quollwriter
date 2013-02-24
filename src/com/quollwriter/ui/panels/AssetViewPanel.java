package com.quollwriter.ui.panels;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.lang.reflect.*;

import java.text.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
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
import com.quollwriter.ui.renderers.*;
import com.quollwriter.events.*;

import com.quollwriter.ui.actionHandlers.*;
//import com.quollwriter.ui.components.*;
import com.quollwriter.ui.components.ActionAdapter;


public class AssetViewPanel extends AbstractObjectViewPanel
{

    private static Map<Class, Class> detailsEditPanels = new HashMap ();

    private static Map<Class, Color> headerColors = new HashMap ();

    static
    {

        Map m = AssetViewPanel.detailsEditPanels;

        m.put (ResearchItem.class,
               ResearchItemDetailsEditPanel.class);
        m.put (Location.class,
               LocationDetailsEditPanel.class);
        m.put (QObject.class,
               ObjectDetailsEditPanel.class);
        m.put (QCharacter.class,
               CharacterDetailsEditPanel.class);

        m = AssetViewPanel.headerColors;

        m.put (ResearchItem.class,
               UIUtils.getColor ("B7AFA3"));
        m.put (Location.class,
               UIUtils.getColor ("DAB576"));
        m.put (QObject.class,
               UIUtils.getColor ("7CB6DC"));
        m.put (QCharacter.class,
               UIUtils.getColor ("6D929B"));

    }

    //private JTree          chapterTree = null;
    private ActionListener assetDeleteAction = null;
    //private java.util.Timer appearsInChaptersRefreshTimer = null;
    private AppearsInChaptersEditPanel appearsInPanel = null;

    public AssetViewPanel(ProjectViewer pv,
                          Asset         a)
                   throws GeneralException
    {

        super (pv,
               a);

    }

    public static DetailsEditPanel getEditDetailsPanel (Asset         a,
                                                        ProjectViewer pv)
                                                 throws GeneralException
    {

        DetailsEditPanel adep = null;

        try
        {

            Class c = AssetViewPanel.detailsEditPanels.get (a.getClass ());

            Constructor con = c.getConstructor (Asset.class,
                                                ProjectViewer.class);

            adep = (DetailsEditPanel) con.newInstance (a,
                                                       pv);

            // adep.init ();

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to create asset details edit panel for object: " +
                                        a,
                                        e);

        }

        return adep;

    }

    /**
     * Get the edit details panel for the specified asset, callers <b>MUST</b> call "init" to finish the creation.
     *
     * @param a The asset.
     * @param pv The project viewer.
     * @returns The edit details panel.
     * @throws GeneralException If the panel cannot be created.
     */
    public DetailsEditPanel getDetailEditPanel (ProjectViewer pv,
                                                NamedObject   n)
                                         throws GeneralException
    {

        return AssetViewPanel.getEditDetailsPanel ((Asset) n,
                                                   pv);

    }

    public String getTitle ()
    {
        
        return this.obj.getName ();
        
    }
    
    public String getIconType ()
    {

        return this.obj.getObjectType ();

    }

    public Color getHeaderColor ()
    {

        return AssetViewPanel.headerColors.get (this.obj.getClass ());

    }

    public static ActionListener getDeleteAssetAction (final ProjectViewer pv,
                                                       final Asset         a)
    {

        return new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                if (JOptionPane.showConfirmDialog (pv,
                                                   "Please confirm you wish to delete " +
                                                   Environment.getObjectTypeName (a) +
                                                   ": " +
                                                   a.getName (),
                                                   "Confirm deletion of " +
                                                   Environment.getObjectTypeName (a) +
                                                   ": " + a.getName (),
                                                   JOptionPane.OK_CANCEL_OPTION,
                                                   JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION)
                {

                    pv.deleteAsset (a);

                }

            }

        };

    }

    public ActionListener getDeleteObjectAction (ProjectViewer pv,
                                                 NamedObject   n)
    {

        if (this.assetDeleteAction == null)
        {

            this.assetDeleteAction = AssetViewPanel.getDeleteAssetAction (pv,
                                                                          (Asset) n);

        }

        return this.assetDeleteAction;

    }

    public void doFillToolBar (JToolBar tb,
                               boolean  fullScreen)
    {

    }

    public void doInit ()
    {

    }

    public EditPanel getBottomEditPanel ()
    {

        final EditPanel ep = this.createAppearsInChaptersPanel ();
        final AssetViewPanel _this = this;

        ep.init ();

        return ep;

    }

    private EditPanel createAppearsInChaptersPanel ()
    {

        this.appearsInPanel = new AppearsInChaptersEditPanel (this.projectViewer,
                                                              this.obj);
        
        return this.appearsInPanel;
/*
        final AssetViewPanel _this = this;

        return new EditPanel (true)
        {

            public void refreshViewPanel ()
            {

                // Get the snippets.
                Map<Chapter, List<Segment>> snippets = UIUtils.getObjectSnippets (_this.obj,
                                                                                  _this.projectViewer);

                DefaultMutableTreeNode tn = new DefaultMutableTreeNode (_this.projectViewer.getProject ());

                UIUtils.createTree (snippets,
                                    tn);

                UIUtils.setTreeRootNode (_this.chapterTree,
                                         tn);

                // Create the tree.
                //((DefaultTreeModel) _this.chapterTree.getModel ()).setRoot (tn);

            }

            public boolean handleSave ()
            {

                return true;

            }

            public List<FormItem> getEditItems ()
            {

                return null;

            }

            public List<FormItem> getViewItems ()
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
                                  "book");

                return iconProv;

            }

            public String getHelpText ()
            {

                return null;

            }

            public String getTitle ()
            {

                return "Appears in Chapters";

            }

            public JComponent getEditPanel ()
            {

                return null;

            }

            public JComponent getViewPanel ()
            {

                _this.chapterTree = UIUtils.createTree ();
                _this.chapterTree.setCellRenderer (new ChapterSnippetsTreeCellRenderer ());
                _this.chapterTree.setToggleClickCount (1);
                _this.chapterTree.setAlignmentX (Component.LEFT_ALIGNMENT);

                _this.chapterTree.addMouseListener (new MouseAdapter ()
                    {

                        private QTextEditor editor = null;
                        private Object highlightId = null;

                        public void mouseReleased (MouseEvent ev)
                        {

                            // Goto the chapter/snippet.
                            if (ev.getClickCount () == 2)
                            {

                                TreePath tp = _this.chapterTree.getPathForLocation (ev.getX (),
                                                                                    ev.getY ());

                                if (tp == null)
                                {

                                    return;

                                }

                                final DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent ();

                                Object o = node.getUserObject ();

                                if (o instanceof Chapter)
                                {

                                    _this.projectViewer.editChapter ((Chapter) o);

                                }

                                if (o instanceof Segment)
                                {

                                    // Get the offset.
                                    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent ();

                                    final Chapter c = (Chapter) parent.getUserObject ();

                                    final Segment s = (Segment) o;

                                    _this.projectViewer.scrollTo (c,
                                                                  s.getBeginIndex ());

                                    final QTextEditor ed = _this.projectViewer.getEditorForChapter (c).getEditor ();

                                    if (this.editor != null)
                                    {

                                        this.editor.removeHighlight (this.highlightId);

                                    }

                                    this.editor = ed;

                                    this.highlightId = ed.addHighlight (s.getBeginIndex (),
                                                                        s.getEndIndex (),
                                                                        null,
                                                                        true);

                                }

                            }

                        }

                    });

                JScrollPane treeScroll = new JScrollPane (_this.chapterTree);
                treeScroll.setOpaque (false);
                treeScroll.setAlignmentX (Component.LEFT_ALIGNMENT);

                return treeScroll;

            }

        };
*/
    }

    public void doRefresh ()
    {

    }

    public void close ()
    {

        this.appearsInPanel.close ();

    }

}
