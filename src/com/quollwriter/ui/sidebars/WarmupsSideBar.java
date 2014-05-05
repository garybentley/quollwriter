package com.quollwriter.ui.sidebars;

import java.awt.Component;

import java.util.List;

import javax.swing.*;

import com.quollwriter.ui.*;
import com.quollwriter.data.*;
import com.quollwriter.events.*;

public class WarmupsSideBar extends AbstractSideBar<WarmupsViewer>
{

    private JTree                  warmupsTree = null;
    private WarmupsAccordionItem   warmupsItem = null;
    
    public WarmupsSideBar (WarmupsViewer v)
    {
        
        super (v);
        
    }

    public List<JButton> getHeaderControls ()
    {
        
        return null;
        
    }
    
    public boolean removeOnClose ()
    {
        
        return false;
        
    }
    
    public boolean canClose ()
    {
        
        return false;
        
    }
    
    public String getIconType ()
    {
        
        return null;
        
    }
    
    public void onClose ()
    {
        
    }
    
    public String getTitle ()
    {
        
        return null;
        
    }
    
    public void init ()
    {
        
        super.init ();
        
        this.warmupsItem.init ();
        
    }

    public void reloadTree ()
    {
        
        this.warmupsItem.update ();
                
    }    
    
    public void panelShown (MainPanelEvent ev)
    {

        this.setObjectSelectedInSidebar (ev.getPanel ().getForObject ());
    
    }    
    
    public void setObjectSelectedInSidebar (DataObject d)
    {
                    
        this.warmupsItem.clearSelectedItemInTree ();
            
        this.warmupsItem.setObjectSelectedInTree (d);
        
    }
    
    public JComponent getContent ()
    {
        
        this.warmupsItem = new WarmupsAccordionItem (this.projectViewer);

        this.warmupsItem.setTitle ("Previous {Warmups}");

        JScrollPane sp = new JScrollPane (this.warmupsItem);
        
        sp.setOpaque (false);
        sp.setAlignmentX (Component.LEFT_ALIGNMENT);
        sp.setBorder (null);
        sp.getViewport ().setOpaque (false);
        sp.getVerticalScrollBar ().setUnitIncrement (20);
        
        return sp;
        
    }
    
    public JTree getWarmupsTree ()
    {
        
        return this.warmupsItem.getTree ();
        
    }
    
}