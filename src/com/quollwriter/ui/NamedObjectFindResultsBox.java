package com.quollwriter.ui;

import java.util.Set;

import javax.swing.*;
import javax.swing.tree.*;

import com.quollwriter.data.*;

public class NamedObjectFindResultsBox<E extends NamedObject> extends FindResultsBox
{

    private Set<E> objs = null;
    
    public NamedObjectFindResultsBox (String                title,
                                      String                iconType,
                                      String                forObjType,
                                      AbstractProjectViewer viewer,
                                      Set<E>                objs)
    {
                                            
        super (title,
               iconType,
               forObjType,
               viewer);
        
        this.objs = objs;
        
        this.count = objs.size ();
        
                                            
    }
        
    @Override
    public void initTree ()
    {
        
        // Let subclasses override for their own behaviour.
        DefaultMutableTreeNode tn = new DefaultMutableTreeNode (this.projectViewer.getProject ());

        UIUtils.createTree (this.objs,
                            tn);
        
        ((DefaultTreeModel) this.tree.getModel ()).setRoot (tn);
        
        this.count = objs.size ();
        
        this.updateItemCount (this.count);
        
    }
            
}
