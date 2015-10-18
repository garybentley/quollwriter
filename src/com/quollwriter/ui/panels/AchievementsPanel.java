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

public class AchievementsPanel extends QuollPanel //implements AchievementReachedListener
{
    
    public static final String PANEL_ID = "achievements";    
    private Achievements achievements = null;
    
    public AchievementsPanel (AbstractProjectViewer pv,
                              Project               p)
                              throws                GeneralException
    {

        super (pv,
               p);
        
        this.achievements = new Achievements (pv);

    }
/*
    public void achievementReached (AchievementReachedEvent ev)
    {
                
        AchievementRule ar = ev.getRule ();

        String t = ar.getCategory ();
        
        Header h = this.headers.get (t);
        
        if (h == null)
        {
            
            return;
            
        }

        AchievementsManager man = Environment.getAchievementsManager ();

        Map<String, Set<String>> achieved = man.getAchievedAchievementIds (this.projectViewer);
        
        Box b = this.boxes.get (t);
        
        if (t.equals ("user"))
        {

            Set<AchievementRule> userRules = man.getUserRules ();

            h.setTitle ("General - " + achieved.get (t).size () + " / " + userRules.size ());
            
            for (int i = 0; i < b.getComponentCount (); i++)
            {
                
                Component c = b.getComponent (i);
                
                if (c instanceof AchievementBox)
                {
                    
                    AchievementBox ab = (AchievementBox) c;
                    
                    if (ab.getRule ().getId ().equals (ar.getId ()))
                    {
                        
                        ab.setAchieved (true);
                        
                        ab.setVisible (true);
                        
                        b.remove (ab);
                        
                        b.add (ab,
                               0);
                        
                    }
                    
                }
                
            }
        
        }

        if (t.equals ("project"))
        {

            Set<AchievementRule> projRules = man.getPerProjectRules ();
            
            h.setTitle ("Project - " + achieved.get (t).size () + " / " + projRules.size ());

            for (int i = 0; i < b.getComponentCount (); i++)
            {
                
                Component c = b.getComponent (i);
                
                if (c instanceof AchievementBox)
                {
                    
                    AchievementBox ab = (AchievementBox) c;
                    
                    if (ab.getRule ().getId ().equals (ar.getId ()))
                    {
                        
                        ab.setAchieved (true);

                        ab.setVisible (true);

                        b.remove (ab);
                        
                        b.add (ab,
                               0);
                        
                    }
                    
                }
                
            }
        
        }
                
    }
*/
    public void init ()
    {

        this.achievements.init ();
        
        this.add (this.achievements);
    /*
        final AchievementsPanel _this = this;

        final AchievementsManager man = Environment.getAchievementsManager ();

        man.addAchievementReachedListener (this);

        Header header = UIUtils.createHeader ("Achievements",
                                       Constants.PANEL_TITLE,
                                       "achievement",
                                       null);
        
        this.add (header);

        JComponent help = UIUtils.createHelpTextPane ("Listed below are all the achievements you have found/reached within Quoll Writer.  Some of the achievements apply to each {project} you create, others are just for general usage of Quoll Writer.  There are also a number of hidden achievements, can you find them?",
                                                      this.projectViewer);
        
        help.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                            Short.MAX_VALUE));
        
        help.setBorder (new EmptyBorder (0, 10, 0, 0));
        
        this.add (help);
                                
        Set<AchievementRule> projRules = man.getPerProjectRules ();

        Set<AchievementRule> userRules = man.getUserRules ();

        Map<String, Set<String>> achieved = man.getAchievedAchievementIds (this.projectViewer);

        Box main = new Box (BoxLayout.Y_AXIS);
        
        JScrollPane lscroll = new JScrollPane (main);
        lscroll.setBorder (null);
        lscroll.setOpaque (false);
        lscroll.getViewport ().setBorder (null);
        lscroll.getViewport ().setOpaque (false);
        lscroll.getVerticalScrollBar ().setUnitIncrement (20);
        lscroll.setAlignmentX (Component.LEFT_ALIGNMENT);
        lscroll.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                               Short.MAX_VALUE));        
                
        this.add (lscroll);
                
        AccordionItem gen = new AccordionItem ("General - " + achieved.get ("user").size () + " / " + userRules.size (),
                                               null);
        
        gen.setContent (this.getAchievementsBox (userRules,
                                                 achieved.get ("user"),
                                                 "user"));
        
        this.headers.put ("user",
                          gen.getHeader ());
        
        gen.setBorder (new EmptyBorder (0, 10, 0, 0));
        AccordionItem proj = new AccordionItem ("Project - " + achieved.get ("project").size () + " / " + projRules.size (),
                                                null);
        
        proj.setContent (this.getAchievementsBox (projRules,
                                                  achieved.get ("project"),
                                                  "project"));
        
        this.headers.put ("project",
                          proj.getHeader ());
        
        proj.setBorder (new EmptyBorder (0, 10, 0, 0));
        
        main.add (gen);
        main.add (proj);
        */                        
    }
/*
    private JComponent getAchievementsBox (Set<AchievementRule> rules,
                                           Set<String>          achievedIds,
                                           String               type)
    {

        Box b = new Box (BoxLayout.Y_AXIS);

        b.setOpaque (true);
        b.setBackground (UIUtils.getComponentColor ());
        b.setAlignmentX (Component.LEFT_ALIGNMENT);
        b.setBorder (new EmptyBorder (0, 5, 0, 0));
        
        int c = 0;
                
        for (AchievementRule ar : rules)
        {
            
            AchievementBox ab = new AchievementBox (ar,
                                                    achievedIds.contains (ar.getId ()),
                                                    false);

            if (ar.isHidden ())
            {
                
                ab.setVisible (false);
                
            }

            if (achievedIds.contains (ar.getId ()))
            {
                
                b.add (ab,
                       0);

                ab.setVisible (true);

                ab.setBorder (new CompoundBorder (new MatteBorder (0, 0, 1, 0, Environment.getInnerBorderColor ()),
                                                  ab.getBorder ()));
                        
            } else {
                
                b.add (ab);                

                if (c < (rules.size () - 1))
                {
    
                    ab.setBorder (new CompoundBorder (new MatteBorder (0, 0, 1, 0, Environment.getInnerBorderColor ()),
                                                      ab.getBorder ()));
    
                }
                
            }
                                    
            c++;
                        
        }

        this.boxes.put (type,
                        b);

        return b;
                                
    }
*/
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

    public void fillToolBar (JToolBar toolBar,
                             boolean  fullScreen)
    {

    }

    public boolean saveUnsavedChanges ()
                                throws Exception
    {

        return true;

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

    public String getTitle ()
    {
        
        return "Achievements";
        
    }
    
    public String getIconType ()
    {

        return "achievement";

    }

    public void close ()
    {

    }
    
}