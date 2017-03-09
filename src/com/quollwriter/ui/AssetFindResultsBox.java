package com.quollwriter.ui;

import java.util.Set;

import javax.swing.*;
import javax.swing.tree.*;

import com.quollwriter.data.*;

public class AssetFindResultsBox extends FindResultsBox
{

    private Set<Asset> objs = null;
    private UserConfigurableObjectType objType = null;
    
    public AssetFindResultsBox (UserConfigurableObjectType type,
                                AbstractProjectViewer      viewer,
                                Set<Asset>                 objs)
    {
                                            
        super (type.getObjectTypeNamePlural (),
               type.getIcon16x16 (),
               viewer);
        
        this.objs = objs;
        
        this.count = objs.size ();
        
                                            
    }
        
    @Override
    public String getId ()
    {
        
        return "findasset" + this.objType.getKey ();
        
    }
    
    @Override
    public void initTree ()
    {
        
        // Let subclasses override for their own behaviour.
        DefaultMutableTreeNode tn = new DefaultMutableTreeNode (this.viewer.getProject ());

        UIUtils.createTree (this.objs,
                            tn);
        
        ((DefaultTreeModel) this.tree.getModel ()).setRoot (tn);
        
        this.count = objs.size ();
        
        this.updateItemCount (this.count);
        
    }
            
}
