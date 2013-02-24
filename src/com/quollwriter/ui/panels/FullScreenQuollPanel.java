package com.quollwriter.ui.panels;

import java.awt.Component;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.data.*;


public class FullScreenQuollPanel extends QuollPanel
{

    private QuollPanel child = null;
    private Border     childBorder = null;

    public FullScreenQuollPanel(QuollPanel child)
    {

        super (child.getProjectViewer (),
               child.getForObject (),
               false);

        this.child = child;

        this.childBorder = this.child.getBorder ();

    }

    public String getTitle ()
    {
        
        return this.child.getTitle ();
        
    }
    
    public String getIconType ()
    {

        return this.child.getIconType ();

    }

    public void init ()
    {

    }

    public void close ()
    {

        this.child.setBorder (this.childBorder);

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

    }

    public boolean saveUnsavedChanges ()
                                throws Exception
    {

        return this.child.saveUnsavedChanges ();

    }

    public void fillToolBar (JToolBar toolBar,
                             boolean  fullScreen)
    {

        this.child.fillToolBar (toolBar,
                                fullScreen);

    }

    public void fillPopupMenu (MouseEvent ev,
                               JPopupMenu popup)
    {

        this.child.fillPopupMenu (ev,
                                  popup);

    }

    public List<Component> getTopLevelComponents ()
    {

        return this.child.getTopLevelComponents ();

    }

    public void refresh (NamedObject n)
    {

        this.child.refresh (n);

    }

}
