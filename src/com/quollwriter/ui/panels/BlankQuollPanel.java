package com.quollwriter.ui.panels;

import java.awt.Component;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.data.*;

public class BlankQuollPanel extends QuollPanel
{
    
    public static final String PANEL_ID = "blank";
    
    public BlankQuollPanel (AbstractProjectViewer pv)
    {
        
        super (pv,
               null);
        
    }

    public String getPanelId ()
    {

        return PANEL_ID;
    
    }
    
    public void close ()
    {
        
    }

    public void init ()
    {
        
        this.add (UIUtils.createHelpTextPane ("<p>You are seeing this because no {chapter}/{asset}/panel has been selected.</p><p>This usually happens when you have just deleted the item you were viewing.</p><p>To continue, just select an item from the sidebar.</p>"));
        
    }

    public void getState (Map<String, Object> s)
    {
        
    }

    public void setState (Map<String, String> s,
                          boolean             hasFocus)
    {
        
    }

    public boolean saveUnsavedChanges ()
                                       throws Exception
    {
        
        return false;
        
    }

    public String getIconType ()
    {
        
        return "blank";
        
    }

    public String getTitle ()
    {
        
        return "No object selected";
        
    }
    
    public void fillToolBar (JToolBar toolBar,
                             boolean  fullScreen)
    {
                
    }
    
    public void fillPopupMenu (MouseEvent ev,
                               JPopupMenu popup)
    {
        
    }

    public List<Component> getTopLevelComponents ()
    {
        
        return new ArrayList ();
        
    }

    public <T extends NamedObject> void refresh (T n)
    {
        
    }
    
}