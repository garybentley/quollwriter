package com.quollwriter.ui.panels;

import java.util.*;

import java.awt.Dimension;
import java.awt.Component;
import java.awt.Color;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.data.*;
import com.quollwriter.achievements.*;
import com.quollwriter.achievements.ui.*;
import com.quollwriter.achievements.rules.*;

import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.components.Accordion;
import com.quollwriter.ui.components.ActionAdapter;

public class AchievementsPanel extends QuollPanel<AbstractViewer> //implements AchievementReachedListener
{
    
    public static final String PANEL_ID = "achievements";    
    private Achievements achievements = null;
    
    public AchievementsPanel (AbstractViewer pv)
                       throws GeneralException
    {

        super (pv);
        
        this.achievements = new Achievements (pv);

    }

    public void init ()
    {

        this.achievements.init ();
        
        this.add (this.achievements);

    }

    public void fillPopupMenu (MouseEvent ev,
                               JPopupMenu popup)
    {

    }

    public List<Component> getTopLevelComponents ()
    {

        return new ArrayList ();

    }

    public void fillToolBar (JToolBar toolBar,
                             boolean  fullScreen)
    {

    }

    public String getPanelId ()
    {

        // Gonna regret this...
        return AchievementsPanel.PANEL_ID;

    }

    public void setState (Map<String, String> s,
                          boolean             hasFocus)
    {

        this.setReadyForUse (true);
    
    }
    
    public void getState (Map<String, Object> m)
    {

    }

    @Override
    public String getTitle ()
    {
        
        return "Achievements";
        
    }
    
    @Override
    public ImageIcon getIcon (int type)
    {

        return Environment.getIcon ("achievement",
                                    type);

    }

    public void close ()
    {

    }
    
}