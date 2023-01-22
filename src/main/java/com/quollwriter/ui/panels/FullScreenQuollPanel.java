package com.quollwriter.ui.panels;

import java.awt.Component;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.data.*;

public class FullScreenQuollPanel extends QuollPanel<AbstractViewer>
{

    private QuollPanel child = null;
    private Border     childBorder = null;

    public FullScreenQuollPanel (QuollPanel child)
    {

        super (child.getViewer ());

        this.child = child;

        this.childBorder = this.child.getBorder ();

    }

    @Override
    public String getTitle ()
    {
        
        return this.child.getTitle ();
        
    }
    
    @Override
    public ImageIcon getIcon (int type)
    {

        return this.child.getIcon (type);

    }
    
    @Override
    public void init ()
    {

    }

    @Override
    public void close ()
    {

        this.child.setBorder (this.childBorder);

    }

    @Override
    public String getPanelId ()
    {
        
        return this.child.getPanelId ();
        
    }
    
    public QuollPanel getChild ()
    {

        return this.child;

    }

    public void restore ()
    {

        this.close ();

    }

    public void getState (Map<String, Object> s)
    {

        this.child.getState (s);

    }

    public void setState (Map<String, String> s,
                          boolean             hasFocus)
    {

        this.child.setState (s,
                             hasFocus);

        this.setReadyForUse (true);
                             
    }

    public boolean saveUnsavedChanges ()
                                throws Exception
    {

        if (this.child instanceof ProjectObjectQuollPanel)
        {
            
            ProjectObjectQuollPanel pc = (ProjectObjectQuollPanel) this.child;
    
            return pc.saveUnsavedChanges ();
        
        }
        
        return false;

    }

    @Override
    public void fillToolBar (JToolBar toolBar,
                             boolean  fullScreen)
    {

        this.child.fillToolBar (toolBar,
                                fullScreen);

    }

    @Override
    public void fillPopupMenu (MouseEvent ev,
                               JPopupMenu popup)
    {

        this.child.fillPopupMenu (ev,
                                  popup);

    }

    @Override
    public List<Component> getTopLevelComponents ()
    {

        return this.child.getTopLevelComponents ();

    }

    public void refresh ()
    {

        if (this.child instanceof ProjectObjectQuollPanel)
        {
            
            ProjectObjectQuollPanel pc = (ProjectObjectQuollPanel) this.child;
            
            pc.refresh ();

        }
            
    }

}
