package com.quollwriter.ui.sidebars;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.image.*;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.border.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.data.*;
import com.quollwriter.events.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.editors.ui.*;
import com.quollwriter.ui.components.Dragger;
import com.quollwriter.ui.components.DragListener;
import com.quollwriter.ui.components.DragEvent;

public class ProjectSideBar extends AbstractSideBar<ProjectViewer>
{

    private ProjectEditorsAccordionItem editors = null;
    private Map<String, ProjectObjectsAccordionItem> projItemBoxes = new HashMap ();
    private JComponent content = null;
    private JComponent contentBox = null;
    private Set<String> objTypes = null;
    private Dragger projEdsDragger = null;
    private JLayeredPane contentWrapper = null;
    
    public ProjectSideBar (ProjectViewer v,
                           Set<String>   objTypes)
    {
        
        super (v);

        this.contentBox = new Box (BoxLayout.Y_AXIS);
        
        this.contentBox.setOpaque (false);
        this.contentBox.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.contentBox.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                       Short.MAX_VALUE));
        this.objTypes = objTypes;
                                          
        this.content = this.wrapInScrollPane (this.contentBox);
                
    }
    
    @Override
    public void onHide ()
    {
        
    }
    
    @Override
    public void onShow ()
    {
        
    }    
    
    public void onClose ()
    {
        
        
    }
    
    public boolean removeOnClose ()
    {
        
        return false;
        
    }
    
    public String getActiveTitle ()
    {
        
        return "{Project}";
        
    }
    
    public String getTitle ()
    {
        
        return null;
        
    }
    
    public String getActiveIconType ()
    {
        
        return Project.OBJECT_TYPE;
        
    }
    
    public String getIconType ()
    {
        
        return null;
        
    }
    
    public Dimension getMinimumSize ()
    {
        
        return new Dimension (UIUtils.getScreenScaledWidth (250),
                              200);
        
    }
    
    @Override
    public List<JComponent> getHeaderControls ()
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

        this.setObjectSelectedInSidebar (ev.getForObject ());
    
    }    
    
    public void setObjectSelectedInSidebar (DataObject d)
    {
        /*
        if (d == null)
        {
            
            return;
            
        }
        */
        for (String objType : this.projItemBoxes.keySet ())
        {
            
            ProjectObjectsAccordionItem it = this.projItemBoxes.get (objType);
            
            it.clearSelectedItemInTree ();
         
            if (d != null)
            {
                
                if (d.getObjectType ().equals (objType))
                {
                    
                    it.setObjectSelectedInTree (d);
                    
                }
                
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

        // TODO: When do drag/drop, can't assume editors is at the bottom.
        if (this.editors != null)
        {
            
            if (this.editors.isContentVisible ())
            {
                
                if (ats.length () > 0)
                {
                    
                    ats.append ("|");
                    
                }
                
                ats.append (ProjectEditor.OBJECT_TYPE);
                
            }
            
        }
        
        return ats.toString ();        
        
    }
 
    public JComponent getContentBox ()
    {
        
        return this.contentBox;
        
    }
  
    public void addAccordionItem (ProjectObjectsAccordionItem item)
    {
        
        this.contentBox.add (item);

        this.projItemBoxes.put (item.getForObjectType (),
                                item);
        
        item.init ();
        
    }
 
    @Override
    public void init ()
               throws GeneralException
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
                    
                    ChaptersAccordionItem it = new ChaptersAccordionItem (this.viewer);
                                        
                    this.addAccordionItem (it);
                
                }
                
                if (objType.equals (ProjectEditor.OBJECT_TYPE))
                {
                    
                    final ProjectSideBar _this = this;
                    
                    this.editors = new ProjectEditorsAccordionItem (this.viewer);

                    this.contentBox.add (this.editors);
                                
                    this.editors.init ();        
                    
                }
                
                if (assetObjTypes.contains (objType))
                {
                    
                    this.addAccordionItem (new AssetAccordionItem (objType,
                                                                   this.viewer));
                            
                }
            
                if (objType.equals (Note.OBJECT_TYPE))
                {
            
                    this.addAccordionItem (new NotesAccordionItem (this.viewer));
                
                }
                
            }

        }

    }
 
    public void setObjectsOpen (String objType)
    {
        
        ProjectObjectsAccordionItem t = this.projItemBoxes.get (objType);        

        if (t == null)
        {
            
            return;
            
        }
        
        t.setContentVisible (true);        
        
    }
 
    public void initOpenObjectTypes (final Set<String> types)
    {
        
        for (String objType : this.projItemBoxes.keySet ())
        {

            ProjectObjectsAccordionItem t = this.projItemBoxes.get (objType);        

            t.setContentVisible (types.contains (objType));        
            
        }
        
        // TODO: When adding drag-drop for sections sort this out.
        if (this.editors != null)
        {
            
            this.editors.setContentVisible (types.contains (ProjectEditor.OBJECT_TYPE));
            
        }
        
    }
    
}