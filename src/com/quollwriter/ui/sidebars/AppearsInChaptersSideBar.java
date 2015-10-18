package com.quollwriter.ui.sidebars;

import java.awt.Dimension;
import java.awt.Component;

import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.tree.*;
import javax.swing.border.*;

import com.quollwriter.data.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.components.ScrollableBox;

public class AppearsInChaptersSideBar extends AbstractSideBar<AbstractProjectViewer>
{
    
    private NamedObject obj = null;
    private AppearsInChaptersEditPanel panel = null;
    private JTree tree = null;
    
    public AppearsInChaptersSideBar (AbstractProjectViewer      v,
                                     AppearsInChaptersEditPanel p)
    {
        
        super (v);
        
        this.panel = p;
        
    }
    
    public String getIconType ()
    {
        
        return this.panel.getForObject ().getObjectType ();
        
    }
        
    public String getActiveTitle ()
    {
        
        return this.panel.getForObject ().getName () + " appears in";
    }
    
    public String getTitle ()
    {
        
        return this.panel.getForObject ().getName ();
        
    }
    
    public boolean removeOnClose ()
    {
        
        return false;
        
    }
    
    public boolean canClose ()
    {
        
        return true;
        
    }
    
    @Override
    public void onShow ()
    {
        
    }
    
    @Override
    public void onHide ()
    {
        
    }
    
    @Override
    public void onClose ()
    {
        
        this.panel.removeHighlight ();

        //this.projectViewer.removeSideBarListener (this.panel);
                                                            
    }
    
    public void setSnippets (Map<Chapter, List<Segment>> snippets)
    {
        
        DefaultMutableTreeNode tn = new DefaultMutableTreeNode (this.viewer.getProject ());

        UIUtils.createTree (snippets,
                            tn);

        UIUtils.setTreeRootNode (tree,
                                 tn);        
        
        // Get the first item and show that.
        if (snippets.size () > 0)
        {
            
            Chapter c = snippets.keySet ().iterator ().next ();
            
            List<Segment> segs = snippets.get (c);

            Segment s = null;
            
            if ((segs != null)
                &&
                (segs.size () > 0)
               )
            {
            
                s = segs.get (0);
                
            }
 
            this.panel.showSnippet (c,
                                    s,
                                    this.tree);
                
        }
        
    }
    
    @Override
    public List<JComponent> getHeaderControls ()
    {
        
        return null;
        
    }
    
    public JComponent getContent ()
    {
        
        Box b = new Box (BoxLayout.Y_AXIS);
        
        JComponent help = UIUtils.createHelpTextPane ("appears in the following {chapters}.",
                                                      this.viewer);
        
        help.setSize (new Dimension (250,
                                     100));
        help.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                            100));
        
        help.setBorder (UIUtils.createPadding (0, 10, 5, 5));
                
        b.add (help);

        this.tree = this.panel.createTree (1);

        this.tree.setExpandsSelectedPaths (true);
                
        JScrollPane treeScroll = new JScrollPane (this.tree);
        treeScroll.setOpaque (false);
        treeScroll.setAlignmentX (Component.LEFT_ALIGNMENT);
        treeScroll.setAlignmentY (Component.TOP_ALIGNMENT);
        treeScroll.setBorder (null);
        tree.setBorder (UIUtils.createPadding (0, 10, 5, 5));
        
        b.add (treeScroll);        

        b.setMinimumSize (new Dimension (250,
                                         250));
        b.setPreferredSize (new Dimension (250,
                                           250));
                
        return b;
        
    }
    
}