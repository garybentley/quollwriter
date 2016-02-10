package com.quollwriter.ui;

import java.awt.Component;
import java.awt.event.*;

import java.util.Map;
import java.util.List;
import java.util.TimerTask;
import java.util.ArrayList;

import javax.swing.text.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.border.*;

import com.quollwriter.*;
import com.quollwriter.ui.sidebars.*;
import com.quollwriter.data.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.ui.components.FormItem;
import com.quollwriter.ui.components.IconProvider;
import com.quollwriter.ui.components.QTextEditor;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.Header;

public class AppearsInChaptersEditPanel extends EditPanel implements SideBarListener
{

    public static final String SIDEBAR_PANEL_ID = "appears-in-chapters";

    private JTree          chapterTree = null;
    private AbstractProjectViewer viewer = null;
    private NamedObject obj = null;
    private AppearsInChaptersSideBar sideBar = null;
    private QTextEditor editor = null;
    private Object highlightId = null;
    private String sideBarId = null;
        
    public AppearsInChaptersEditPanel (AbstractProjectViewer viewer,
                                       NamedObject           obj)
    {
        
        super (true);
        
        this.obj = obj;
        this.viewer = viewer;
        
        this.sideBarId = SIDEBAR_PANEL_ID + this.obj.getObjectReference ().asString ();

    }

    public void updateChapterTree (Map<Chapter, List<Segment>> snippets)
    {
        
        DefaultMutableTreeNode tn = new DefaultMutableTreeNode (this.viewer.getProject ());

        UIUtils.createTree (snippets,
                            tn);

        UIUtils.setTreeRootNode (this.chapterTree,
                                 tn);        
        
    }

    public void refreshViewPanel ()
    {

        this.updateChapterTree (UIUtils.getObjectSnippets (this.obj,
                                                           this.viewer));

    }

    public NamedObject getForObject ()
    {
        
        return this.obj;
        
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

        DefaultIconProvider iconProv = new DefaultIconProvider ()
        {
          
            @Override
            public ImageIcon getIcon (String name,
                                      int    type)
            {
                
                if (name.equals ("header"))
                {
                    
                    name = Book.OBJECT_TYPE;
                    
                }
                
                return super.getIcon (name,
                                      type);
                
            }
            
        };

        return iconProv;

    }

    public String getHelpText ()
    {

        return null;

    }

    public String getTitle ()
    {

        return "Appears in {Chapters}";

    }

    public JComponent getEditPanel ()
    {

        return null;

    }

    public void sideBarHidden (SideBarEvent ev)
    {
        
        this.removeHighlight ();
        
    }
    
    public void sideBarShown (SideBarEvent ev)
    {

        this.removeHighlight ();
        
    }
    
    private void showInProjectViewerSideBar ()
                                      throws GeneralException
    {

        if (this.sideBar != null)
        {
        
            this.removeHighlight ();
    
            this.viewer.showSideBar (this.sideBarId);
    
            this.sideBar.setSnippets (UIUtils.getObjectSnippets (this.obj,
                                                                 this.viewer));

            return;
            
        }
    
        ((ProjectViewer) this.viewer).addSideBarListener (this);
            
        this.sideBar = new AppearsInChaptersSideBar (this.viewer,
                                                     this);
        
        this.viewer.addSideBar (this.sideBarId,
                                this.sideBar);

        this.showInProjectViewerSideBar ();
            
    }
    
    public void removeHighlight ()
    {
        
        try
        {
            
            this.editor.removeHighlight (this.highlightId);
            
            this.highlightId = null;
            
        } catch (Exception e) {
            
            // Ignore.
            
        }
        
    }
    
    public void showSnippet (final Chapter c,
                             final Segment s,
                             final JTree   tree)
    {

        final AppearsInChaptersEditPanel _this = this;
    
        if (c != null)
        {
    
            this.viewer.viewObject (c,
                                    new ActionListener ()
            {
                
                public void actionPerformed (ActionEvent ev)
                {

                    Object v = c;
                
                    if (s != null)
                    {
            
                        v = s;
            
                        _this.viewer.scrollTo (c,
                                               s.getBeginIndex ());
            
                        final QTextEditor ed = _this.viewer.getEditorForChapter (c).getEditor ();
            
                        _this.removeHighlight ();
            
                        _this.editor = ed;
            
                        _this.highlightId = ed.addHighlight (s.getBeginIndex (),
                                                             s.getEndIndex (),
                                                             null,
                                                             true);
            
                    }

                    // See how many children there are.
                    TreePath tp = UIUtils.getTreePathForUserObject ((DefaultMutableTreeNode) tree.getModel ().getRoot (),
                                                                    v);
            
                    if (tp != null)
                    {
            
                        tree.setSelectionPath (tp);
                        
                    }

                }
                
            });
            
        }            
        
    }
    
    public JTree createTree (final int viewClickCount)
    {
        
        final AppearsInChaptersEditPanel _this = this;
        
        final JTree tree = UIUtils.createTree ();
        tree.setCellRenderer (new ChapterSnippetsTreeCellRenderer ());
        tree.setToggleClickCount (1);
        tree.setAlignmentX (Component.LEFT_ALIGNMENT);

        tree.addMouseListener (new MouseAdapter ()
            {

                public void mouseReleased (MouseEvent ev)
                {

                    // Goto the chapter/snippet.
                    if (ev.getClickCount () == viewClickCount)
                    {

                        TreePath tp = tree.getPathForLocation (ev.getX (),
                                                               ev.getY ());

                        if (tp == null)
                        {

                            return;

                        }

                        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent ();

                        Object o = node.getUserObject ();

                        if (o instanceof Chapter)
                        {

                            _this.showSnippet ((Chapter) o,
                                               null,
                                               tree);

                            return;
                                               
                        }

                        if (o instanceof Segment)
                        {

                            // Get the offset.
                            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent ();

                            Chapter c = (Chapter) parent.getUserObject ();

                            Segment s = (Segment) o;

                            _this.showSnippet (c,
                                               s,
                                               tree);
                            
                        }

                    }

                }

            });

        return tree;
                    
    }
    
    public JComponent getViewPanel ()
    {

        final AppearsInChaptersEditPanel _this = this;

        java.util.List<JComponent> buttons = new ArrayList ();
            
        JButton but = UIUtils.createButton (Environment.getIcon ("sendout",
                                                          Constants.ICON_PANEL_SECTION_ACTION),
                                               "Click to view these items in the sidebar",
                                               new ActionAdapter ()
                                               {
                                                
                                                    public void actionPerformed (ActionEvent ev)
                                                    {
                                                        
                                                        try
                                                        {
                                                        
                                                            _this.showInProjectViewerSideBar ();
                                                            
                                                        } catch (Exception e) {
                                                            
                                                            Environment.logError ("Unable to show appears in chapters in sidebar",
                                                                                  e);
                                                            
                                                            UIUtils.showErrorMessage (_this,
                                                                                      "Unable to show.");
                                                            
                                                        }
                                                        
                                                    }
                                                
                                               });

        buttons.add (but);
        
        this.header.setControls (UIUtils.createButtonBar (buttons));
        
        this.chapterTree = this.createTree (2);
            
        JScrollPane treeScroll = new JScrollPane (this.chapterTree);
        treeScroll.setOpaque (false);
        treeScroll.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.chapterTree.setBorder (new EmptyBorder (0, 5, 0, 0));
        
        return treeScroll;

    }

    public void close ()
    {
                
    }
    
}