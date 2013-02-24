package com.quollwriter.ui.sidebars;

import java.awt.Component;
import java.awt.Dimension;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import javax.swing.*;
import javax.swing.border.*;

import com.quollwriter.ui.*;
import com.quollwriter.data.*;
import com.quollwriter.events.*;

public class ProjectSideBar extends AbstractSideBar<ProjectViewer>
{

    private Map<String, ProjectObjectsAccordionItem> projItemBoxes = new HashMap ();
    
    public ProjectSideBar (ProjectViewer v)
    {
        
        super (v);
        
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
                
        Box b = new Box (BoxLayout.Y_AXIS);
        
        b.setOpaque (false);
        b.setAlignmentX (Component.LEFT_ALIGNMENT);
        b.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                         Short.MAX_VALUE));
/*
        b.setBorder (new EmptyBorder (0,
                                      5,
                                      0,
                                      0));
  */                                               
        ChaptersAccordionItem it = new ChaptersAccordionItem (this.projectViewer);

        b.add (it);
        
        this.projItemBoxes.put (Chapter.OBJECT_TYPE,
                                it);

        java.util.List<String> objTypes = new ArrayList ();
        
        objTypes.add (QCharacter.OBJECT_TYPE);
        objTypes.add (Location.OBJECT_TYPE);
        objTypes.add (QObject.OBJECT_TYPE);
        objTypes.add (ResearchItem.OBJECT_TYPE);
    
        for (String objType : objTypes)
        {
    
            // Sheer laziness here, fix up later.        
            AssetAccordionItem t = new AssetAccordionItem (objType,
                                                           this.projectViewer);
        
            this.projItemBoxes.put (objType,
                                    t);
        
            b.add (t);
        
        }
        
        NotesAccordionItem nit = new NotesAccordionItem (this.projectViewer);

        b.add (nit);
        
        this.projItemBoxes.put (Note.OBJECT_TYPE,
                                nit);

        return this.wrapInScrollPane (b);
                                /*
        JScrollPane lscroll = new JScrollPane (b);
        lscroll.setBorder (new EmptyBorder (0, 5, 0, 0));
        lscroll.setOpaque (false);
        lscroll.getViewport ().setBorder (null);
        lscroll.getViewport ().setOpaque (false);
        lscroll.getVerticalScrollBar ().setUnitIncrement (20);
        lscroll.setAlignmentX (Component.LEFT_ALIGNMENT);
        lscroll.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                               Short.MAX_VALUE));
        
        return lscroll;
        */
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
 
    public void init ()
    {
        
        super.init ();
        
        for (String objType : this.projItemBoxes.keySet ())
        {
            
            ProjectObjectsAccordionItem it = this.projItemBoxes.get (objType);

            it.init ();
            
        }        
        
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