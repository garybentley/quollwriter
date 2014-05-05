package com.quollwriter.ui.sidebars;

import java.awt.Component;
import java.awt.Dimension;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.border.*;

import com.quollwriter.ui.*;
import com.quollwriter.data.*;
import com.quollwriter.events.*;

public class ProjectSideBar extends AbstractSideBar<AbstractProjectViewer>
{

    private Map<String, ProjectObjectsAccordionItem> projItemBoxes = new HashMap ();
    private JComponent content = null;
    private JComponent contentBox = null;
    private Set<String> objTypes = null;
    
    public ProjectSideBar (AbstractProjectViewer v,
                           Set<String>           objTypes)
    {
        
        super (v);

        this.contentBox = new Box (BoxLayout.Y_AXIS);
        
        this.contentBox.setOpaque (false);
        this.contentBox.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.contentBox.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                       Short.MAX_VALUE));
/*
        b.setBorder (new EmptyBorder (0,
                                      5,
                                      0,
                                      0));
  */

        this.objTypes = objTypes;
                                          
        this.content = this.wrapInScrollPane (this.contentBox);
                
    }
    
    public void onClose ()
    {
        
        
    }
    
    public boolean removeOnClose ()
    {
        
        return false;
        
    }
    
    public String getTitle ()
    {
        
        return null;
        
    }
    
    public String getIconType ()
    {
        
        return null;
        
    }
    
    public List<JButton> getHeaderControls ()
    {
        
        return null;
        
    }
    
    public boolean canClose ()
    {
        
        return false;
        
    }
    
    public JComponent getContent ()
    {
                
        return this.content;
                
    }

    public void panelShown (MainPanelEvent ev)
    {

        this.setObjectSelectedInSidebar (ev.getPanel ().getForObject ());
    
    }    
    
    public void setObjectSelectedInSidebar (DataObject d)
    {
        
        if (d == null)
        {
            
            return;
            
        }
        
        for (String objType : this.projItemBoxes.keySet ())
        {
            
            ProjectObjectsAccordionItem it = this.projItemBoxes.get (objType);
            
            it.clearSelectedItemInTree ();
            
            if (d.getObjectType ().equals (objType))
            {
                
                it.setObjectSelectedInTree (d);
                
            } 

        }        
        
    }

    public void reloadTreeForObjectType (String objType)
    {
        
        ProjectObjectsAccordionItem it = this.projItemBoxes.get (objType);

        if (it != null)
        {
        
            it.update ();
            
        }
        
    }

    public void showObjectInTree (String      treeObjType,
                                  NamedObject obj)
    {
        
        JTree tree = this.getTreeForObjectType (treeObjType);
        
        if (tree == null)
        {
            
            return;
            
        }
        
        DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel ();

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) dtm.getRoot ();

        tree.expandPath (UIUtils.getTreePathForUserObject (root,
                                                           obj));
        
    }
    
    public JTree getTreeForObjectType (String objType)
    {
                
        ProjectObjectsAccordionItem it = this.projItemBoxes.get (objType);
        
        if (it == null)
        {
            
            return null;
            
        }
        
        return it.getTree ();
        
    }
    
    public String getSaveState ()
    {
        
        StringBuilder ats = new StringBuilder ();

        for (String objType : this.projItemBoxes.keySet ())
        {
            
            ProjectObjectsAccordionItem ai = this.projItemBoxes.get (objType);
        
            if (ai.isContentVisible ())
            {

                if (ats.length () > 0)
                {

                    ats.append ("|");

                }

                ats.append (objType);

            }

        }

        return ats.toString ();        
        
    }
 
    public void addAccordionItem (ProjectObjectsAccordionItem item)
    {
        
        this.contentBox.add (item);

        this.projItemBoxes.put (item.getObjectType (),
                                item);
        
        item.init ();
        
    }
 
    public void init ()
    {
        
        super.init ();

        if (this.objTypes != null)
        {
        
            java.util.Set<String> assetObjTypes = new HashSet ();
            
            assetObjTypes.add (QCharacter.OBJECT_TYPE);
            assetObjTypes.add (Location.OBJECT_TYPE);
            assetObjTypes.add (QObject.OBJECT_TYPE);
            assetObjTypes.add (ResearchItem.OBJECT_TYPE);
      
            for (String objType : this.objTypes)
            {
      
                if (objType.equals (Chapter.OBJECT_TYPE))
                {
                    
                    this.addAccordionItem (new ChaptersAccordionItem (this.projectViewer));
                
                }
                
                if (assetObjTypes.contains (objType))
                {
                    
                    if (!(this.projectViewer instanceof ProjectViewer))
                    {
                    
                        throw new IllegalArgumentException ("Asset object type: " +
                                                            objType +
                                                            " is only supported when the project viewer is an instance of: " +
                                                            ProjectViewer.class.getName ());
                                            
                    }
        
                    // Sheer laziness here, fix up later.        
                    this.addAccordionItem (new AssetAccordionItem (objType,
                                                                   (ProjectViewer) this.projectViewer));
                            
                }
            
                if (objType.equals (Note.OBJECT_TYPE))
                {
            
                    this.addAccordionItem (new NotesAccordionItem (this.projectViewer));
                
                }
                
            }

        }
        /*
        for (String objType : this.projItemBoxes.keySet ())
        {
            
            ProjectObjectsAccordionItem it = this.projItemBoxes.get (objType);

            it.init ();
            
        }        
        */
    }
 
    public void initOpenObjectTypes (final Set<String> types)
    {
        
        for (String objType : this.projItemBoxes.keySet ())
        {

            ProjectObjectsAccordionItem t = this.projItemBoxes.get (objType);        

            t.setContentVisible (types.contains (objType));
            
        }
        
    }
    
}