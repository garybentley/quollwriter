package com.quollwriter.ui.sidebars;

import java.awt.Dimension;
import java.awt.Component;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.tree.*;
import javax.swing.border.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.components.ScrollableBox;

public class TargetsSideBar extends AbstractSideBar<AbstractViewer>
{
    
    public static final String ID = "targets";
    
    private Targets targets = null;
    
    public TargetsSideBar (AbstractViewer v)
    {
        
        super (v);
                
    }
    
    @Override
    public String getId ()
    {
        
        return ID;
        
    }
    
    public String getIconType ()
    {
        
        return Constants.TARGET_ICON_NAME;
        
    }
        
    public String getActiveTitle ()
    {
        
        return Environment.getUIString (LanguageStrings.project,
                                        LanguageStrings.sidebar,
                                        LanguageStrings.targets,
                                        LanguageStrings.title);
        //"Targets";
    }
    
    public String getTitle ()
    {
        
        return this.getActiveTitle ();
        
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
                                                                    
    }
        
    @Override
    public List<JComponent> getHeaderControls ()
    {
        
        java.util.List<JComponent> buts = new ArrayList ();
    
        buts.add (UIUtils.createHelpPageButton ("project/targets",
                                                Constants.ICON_PANEL_ACTION,
                                                null));
        
        return buts;
        
    }
    
    /**
     * Always 250, 250.
     */
    @Override
    public Dimension getMinimumSize ()
    {
        
        return new Dimension (275,
                              250);        
    }
    
    public JComponent getContent ()
    {
        
        this.targets = new Targets (this.viewer);
        
        this.targets.setBorder (UIUtils.createPadding (0, 0, 5, 5));        
        
        this.targets.init ();
                        
        return this.wrapInScrollPane (this.targets);
        
    }
    
}