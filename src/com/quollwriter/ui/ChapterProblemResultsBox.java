package com.quollwriter.ui;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.Enumeration;

import java.awt.Component;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.text.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.components.QTextEditor;

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

            this.projectViewer.saveProblemFinderIgnores (c);

            this.projectViewer.getProblemFinderSideBar ().find ();

            this.projectViewer.fireProjectEvent (ProjectEvent.PROBLEM_FINDER,
                                                 ProjectEvent.IGNORE);

        } catch (Exception e) {

            Environment.logError ("Unable to save problem finder ignores for chapter: " +
                                  c,
                                  e);

            UIUtils.showErrorMessage (this.projectViewer,
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

        DefaultMutableTreeNode tn = new DefaultMutableTreeNode (this.projectViewer.getProject ());

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

        } else {

            this.toggleTreePath (tp);

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

        this.projectViewer.viewObject (c,
                                       new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                AbstractEditorPanel p = _this.projectViewer.getEditorForChapter (c);

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

}
