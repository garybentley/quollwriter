package com.quollwriter.ui;

import java.util.List;
import java.util.Map;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.text.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.components.QTextEditor;

public class ChapterFindResultsBox extends FindResultsBox
{

    private Map<Chapter, List<Segment>> snippets = null;
    private Object highlightId = null;
    private QTextEditor highlightedEditor = null;
    private String forObjType = null;
    
    public ChapterFindResultsBox (String                      title,
                                  String                      iconType,
                                  String                      forObjType,
                                  AbstractProjectViewer       viewer,
                                  Map<Chapter, List<Segment>> snippets)
    {
                                            
        super (title,
               iconType,
               forObjType,
               viewer);
                     
        this.forObjType = forObjType;
                     
        this.snippets = snippets;
                                            
    }
    
    @Override
    public String getId ()
    {
        
        return this.forObjType;
        
    }
    
    @Override
    public void initTree ()
    {
        
        this.tree.setCellRenderer (new ChapterSnippetsTreeCellRenderer ());//new MultiLineTreeCellRenderer (this));

        DefaultMutableTreeNode tn = new DefaultMutableTreeNode (this.viewer.getProject ());

        UIUtils.createTree (this.snippets,
                            tn);

        // Create the tree.
        ((DefaultTreeModel) this.tree.getModel ()).setRoot (tn);
            
        int c = 0;
        
        for (NamedObject n : this.snippets.keySet ())
        {
            
            c += this.snippets.get (n).size ();
            
        }
        
        this.count = c;
        
        this.updateItemCount (this.count);
        
        this.setContentVisible (true);
        
    }
        
    @Override
    protected void handleViewObject (TreePath tp,
                                     Object   o)
    {
               
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
            
            return;
            
        }
                  
        if (o instanceof Segment)
        {

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent ();

            // Get the offset.
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent ();

            Chapter c = (Chapter) parent.getUserObject ();

            Segment s = (Segment) o;

            this.showChapterSegment (c,
                                     s);

        } else {
            
            this.toggleTreePath (tp);
                            
        }
       
    }
    
    public void clearHighlight ()
    {
        
        if (this.highlightedEditor != null)
        {
            
            this.highlightedEditor.removeHighlight (this.highlightId);

        }
        
    }
    
    public void showChapterSegment (final Chapter c,
                                    final Segment s)
    {
        
        this.clearHighlight ();
    
        final ChapterFindResultsBox _this = this;
    
        this.viewer.viewObject (c,
                                new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {
        
                AbstractEditorPanel p = _this.viewer.getEditorForChapter (c);
                                
                try
                {
                    
                    p.scrollToPosition (s.getBeginIndex ());
                    
                } catch (Exception e) {
                    
                    Environment.logError ("Unable to scroll to: " + s.getBeginIndex (),
                                          e);
                    
                    return;
                    
                }

                final QTextEditor ed = p.getEditor ();
                
                UIUtils.doLater (new ActionListener ()
                {                           
                
                    @Override
                    public void actionPerformed (ActionEvent ev)
                    {
                
                        _this.highlightId = ed.addHighlight (s.getBeginIndex (),
                                                             s.getEndIndex (),
                                                             null,
                                                             true);
                
                        _this.highlightedEditor = ed;
                        
                    }
                    
                });
                
            }
            
        });
        
    }    
                    
}
