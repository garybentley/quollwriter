package com.quollwriter.ui;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.Enumeration;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.text.*;
import javax.swing.event.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.components.QTextEditor;
import com.quollwriter.ui.components.ActionAdapter;

import com.quollwriter.text.*;

public class ChapterProblemResultsBox extends FindResultsBox<ProjectViewer>
{

    private Map<Chapter, Set<Issue>> problems = null;
    private Object highlightId = null;
    private Object lineHighlightId = null;
    private QTextEditor highlightedEditor = null;

    public ChapterProblemResultsBox (String                   title,
                                     String                   iconType,
                                     String                   forObjType,
                                     ProjectViewer            viewer,
                                     Map<Chapter, Set<Issue>> problems)
    {

        super (title,
               iconType,
               forObjType,
               viewer);
               
        this.problems = problems;

    }

    @Override
    public void fillTreePopupMenu (JPopupMenu m,
                                   MouseEvent ev)
    {

        final ChapterProblemResultsBox _this = this;

        final TreePath tp = this.tree.getPathForLocation (ev.getX (),
                                                          ev.getY ());

        JMenuItem mi = null;

        if (tp != null)
        {

            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent ();

            Object d = node.getUserObject ();

            if (d instanceof Issue)
            {

                Issue issue = (Issue) d;

                m.add (UIUtils.createMenuItem ("Ignore this problem",
                                               Constants.ERROR_ICON_NAME,
                                               new ActionListener ()
                                               {

                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent ();

                                                        Chapter c = (Chapter) parent.getUserObject ();

                                                        Set<Issue> issues = new HashSet ();

                                                        issues.add (issue);

                                                        _this.saveIgnores (issues,
                                                                           c);

                                                    }

                                               }));

            }

            if (d instanceof Chapter)
            {

                Chapter c = (Chapter) d;

                m.add (UIUtils.createMenuItem ("Ignore all problems for this {chapter}",
                                               Constants.ERROR_ICON_NAME,
                                               new ActionListener ()
                                               {

                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        Set<Issue> issues = new HashSet ();

                                                        Enumeration en = node.children ();

                                                        while (en.hasMoreElements ())
                                                        {

                                                            DefaultMutableTreeNode child = (DefaultMutableTreeNode) en.nextElement ();

                                                            Issue issue = (Issue) child.getUserObject ();

                                                            issues.add (issue);

                                                        }

                                                        _this.saveIgnores (issues,
                                                                           c);

                                                    }

                                               }));
/*
                Set<Issue> ignored = c.getProblemFinderIgnores (this.rule);

                int s = ignored.size ();

                if (s > 0)
                {

                    m.add (UIUtils.createMenuItem (String.format ("Un-ignore the %s problem%s currently ignored in this {chapter}",
                                                                  s,
                                                                  (s == 1 ? "" : "s")),
                                                   Constants.PROBLEM_FINDER_ICON_NAME,
                                                   new ActionListener ()
                                                   {

                                                        public void actionPerformed (ActionEvent ev)
                                                        {

                                                            Set<Issue> ignored = c.getProblemFinderIgnores ();



                                                            Set<Issue> issues = new HashSet ();

                                                            Enumeration en = node.children ();

                                                            Set<DefaultMutableTreeNode> toRemove = new HashSet ();

                                                            while (en.hasMoreElements ())
                                                            {

                                                                DefaultMutableTreeNode child = (DefaultMutableTreeNode) en.nextElement ();

                                                                Issue issue = (Issue) child.getUserObject ();

                                                                issues.add (issue);

                                                                toRemove.add (child);

                                                            }

                                                            for (DefaultMutableTreeNode n : toRemove)
                                                            {

                                                                ((DefaultTreeModel) _this.tree.getModel ()).removeNodeFromParent (n);

                                                            }

                                                            _this.saveIgnores (issues,
                                                                               c);

                                                        }

                                                   }));
*/

            }

        }

    }

    private void saveIgnores (Set<Issue> ignore,
                              Chapter    c)
    {

        c.getProblemFinderIgnores ().addAll (ignore);

        try
        {

            this.viewer.saveProblemFinderIgnores (c);

            this.viewer.getProblemFinderSideBar ().find ();

            this.viewer.fireProjectEvent (ProjectEvent.PROBLEM_FINDER,
                                          ProjectEvent.IGNORE);

        } catch (Exception e) {

            Environment.logError ("Unable to save problem finder ignores for chapter: " +
                                  c,
                                  e);

            UIUtils.showErrorMessage (this.viewer,
                                      "Unable to save ignore.");

        }

    }
    
    @Override
    public void initTree ()
    {

        final ChapterProblemResultsBox _this = this;
        
        this.tree.setCellRenderer (new DefaultTreeCellRenderer ()
        {

            public Component getTreeCellRendererComponent (JTree   tree,
                                                           Object  value,
                                                           boolean sel,
                                                           boolean expanded,
                                                           boolean leaf,
                                                           int     row,
                                                           boolean hasFocus)
            {

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

                value = node.getUserObject ();

                if (value instanceof Issue)
                {

                    Issue i = (Issue) value;

                    Chapter c = (Chapter) ((DefaultMutableTreeNode) node.getParent ()).getUserObject ();

                    TextBlock b = i.getTextBlock ();

                    if (b instanceof Paragraph)
                    {

                        value = ((Paragraph) b).getFirstSentence ().getText ();

                    } else {

                        value = b.getText ();

                    }

                }

                super.getTreeCellRendererComponent (tree,
                                                    value,
                                                    sel,
                                                    expanded,
                                                    leaf,
                                                    row,
                                                    hasFocus);

                this.setBorder (UIUtils.createPadding (2, 2, 2, 2));
                this.setIcon (null);

                if (value instanceof Chapter)
                {

                    Chapter c = (Chapter) value;

                    this.setText (c.getName () + " (" + node.getChildCount () + ")");

                } else {

                    this.setIcon (Environment.getIcon (Chapter.OBJECT_TYPE,
                                                       Constants.ICON_TREE));

                }

                return this;

            }

        });

        PopupPreviewListener mm = new PopupPreviewListener (this);
        
        this.tree.addMouseMotionListener (mm);
        this.tree.addMouseListener (mm);        
        
        DefaultMutableTreeNode tn = new DefaultMutableTreeNode (this.viewer.getProject ());

        UIUtils.createTree (this.problems,
                            tn);

        // Create the tree.
        ((DefaultTreeModel) this.tree.getModel ()).setRoot (tn);

        int c = 0;

        for (NamedObject n : this.problems.keySet ())
        {

            c += this.problems.get (n).size ();

        }

        this.count = c;

        this.updateItemCount (this.count);

        this.setContentVisible (true);
        
        
    }

    @Override
    public boolean isAllowObjectPreview ()
    {
        
        return false;
        
    }
    
    @Override
    public void handleViewObject (TreePath tp,
                                  Object   o)
    {

        if (o instanceof Issue)
        {

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent ();

            // Get the offset.
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent ();

            Chapter c = (Chapter) parent.getUserObject ();

            Issue i = (Issue) o;

            this.showChapterIssue (c,
                                   i);
                               
        }
        
        if (o instanceof Chapter)
        {

            this.toggleTreePath (tp);        
        
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent ();
            
            // Damn son this is fugly.
            if ((node.getChildCount () == 1)
                &&
                (this.tree.isExpanded (UIUtils.getTreePathForUserObject ((DefaultMutableTreeNode) this.tree.getModel ().getRoot (),
                                                                         node.getUserObject ())))
               )
            {
                
                DefaultMutableTreeNode n = node.getFirstLeaf ();
                
                TreePath tpp = UIUtils.getTreePathForUserObject ((DefaultMutableTreeNode) this.tree.getModel ().getRoot (),
                                                                 n.getUserObject ());
                
                this.handleViewObject (tpp,
                                       n.getUserObject ());                
                                
                this.tree.setSelectionPath (tpp);
                    
            }
            
        }

    }

    public void clearHighlight ()
    {

        if (this.highlightedEditor != null)
        {

            this.highlightedEditor.removeHighlight (this.highlightId);
            this.highlightedEditor.removeHighlight (this.lineHighlightId);

        }

    }

    public void showChapterIssue (final Chapter c,
                                  final Issue   i)
    {

        this.clearHighlight ();

        final ChapterProblemResultsBox _this = this;

        this.viewer.viewObject (c,
                                new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                AbstractEditorPanel p = _this.viewer.getEditorForChapter (c);

                try
                {

                    p.scrollToPosition (i.getStartIssuePosition ());

                } catch (Exception e) {

                    Environment.logError ("Unable to scroll to: " + i.getStartIssuePosition (),
                                          e);

                    return;

                }

                TextBlock textBlock = i.getTextBlock ();

                final QTextEditor ed = p.getEditor ();

                int end = textBlock.getAllTextEndOffset () + 1;

                if (textBlock instanceof Sentence)
                {

                    end = ((Sentence) textBlock).getLastWord ().getAllTextEndOffset ();

                }

                _this.lineHighlightId = ed.addHighlight (textBlock.getAllTextStartOffset (),
                                                         end,
                                                         ProblemFinder.getTextBlockHighlighter (),
                                                         true);

                _this.highlightId = ed.addHighlight (i.getStartIssuePosition (),
                                                     i.getEndIssuePosition (),
                                                     ProblemFinder.getIssueHighlighter (),
                                                     true);

                _this.highlightedEditor = ed;

            }

        });

    }

    private class PopupPreviewListener extends MouseEventHandler
    {
        
        private HideablePopup popup = null;
        private Issue lastObject = null;
        private ChapterProblemResultsBox results = null;
                          
        public PopupPreviewListener (ChapterProblemResultsBox results)
        {
            
            this.results = results;
                              
        }
        
        @Override
        public void handlePress (MouseEvent ev)
        {
            
            if (this.popup != null)
            {
            
                this.popup.hidePopup ();
                
            }
            
        }
        
        @Override
        public void mouseMoved (MouseEvent ev)
        {
        
            final PopupPreviewListener _this = this;
        
            // Edit the chapter.
            TreePath tp = this.results.tree.getPathForLocation (ev.getX (),
                                                                ev.getY ());

            if (tp == null)
            {

                return;

            }

            Object d = ((DefaultMutableTreeNode) tp.getLastPathComponent ()).getUserObject ();

            if (!(d instanceof Issue))
            {
                
                return;
                
            }
                                    
            if (this.popup != null)
            {
                
                this.popup.hidePopup ();
                                    
            }
            
            if ((this.lastObject != null)
                &&
                (d != this.lastObject)
               )
            {
                
                // Hide the popup.
                this.popup.hidePopup ();
                
            }
                    
            final Issue issue = (Issue) d;
                            
            this.lastObject = issue;
            
            Point po = this.results.viewer.convertPoint (this.results.getTree (),
                                                         new Point (ev.getX () + 10,
                                                                    this.results.getTree ().getPathBounds (tp).y + this.results.getTree ().getPathBounds (tp).height - 5));

            this.popup = new HideablePopup (this.results.getViewer ())
            {
                
                @Override
                public JComponent getContent ()
                {
                    
                    JEditorPane desc = UIUtils.createHelpTextPane (issue.getDescription (),
                                                                   null);
        
                    FormLayout fl = new FormLayout ("380px",
                                                    "p");
        
                    PanelBuilder pb = new PanelBuilder (fl);
        
                    CellConstraints cc = new CellConstraints ();
        
                    pb.add (desc, cc.xy (1, 1));
        
                    desc.setAlignmentX (Component.LEFT_ALIGNMENT);
        
                    JPanel p = pb.getPanel ();
                    p.setOpaque (true);
                    p.setBackground (UIUtils.getComponentColor ());
                    
                    return p;
                  
                }
            };
            
            // Show the first line of the description.
            this.popup.show (1000,
                             250,
                             po,
                             new ActionAdapter ()
                             {
                                
                                public void actionPerformed (ActionEvent ev)
                                {
                                    
                                    _this.lastObject = null;
                                    
                                }
                                
                             });
                            
        }
        
        @Override
        public void mouseExited (MouseEvent ev)
        {

            this.lastObject = null;
        
            if (this.popup != null)
            {
        
                this.popup.hidePopup ();
                
            }

        }        
        
    }
    
}
