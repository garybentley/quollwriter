package com.quollwriter.achievements.ui;

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.components.Header;
import com.quollwriter.achievements.*;
import com.quollwriter.achievements.rules.*;

public class Achievements extends Box implements AchievementReachedListener
{

    private Map<String, Header> headers = new HashMap ();
    private Map<String, Box> boxes = new HashMap ();
    private AbstractViewer viewer = null;
    private Header header = null;
    
    public Achievements (AbstractViewer viewer)
    {
        
        super (BoxLayout.Y_AXIS);
        
        this.viewer = viewer;

        this.header = UIUtils.createHeader ("Achievements",
                                            Constants.PANEL_TITLE,
                                            "achievement",
                                            null);
        
    }
    
    public Header getHeader ()
    {
        
        return this.header;
        
    }
    
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

        Map<String, Set<String>> achieved = man.getAchievedAchievementIds (this.viewer);
        
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

    public void init ()
    {

        final Achievements _this = this;

        final AchievementsManager man = Environment.getAchievementsManager ();

        man.addAchievementReachedListener (this);
        
        this.add (header);

        JComponent help = UIUtils.createHelpTextPane ("Listed below are all the achievements you have found/reached..  Some of the achievements apply to each {project} you create, others are just for general usage of {QW}.  There are also a number of hidden achievements, can you find them?",
                                                      this.viewer);
        
        help.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                            Short.MAX_VALUE));
        
        help.setBorder (new EmptyBorder (0, 10, 0, 0));
        
        this.add (help);
                                
        Set<AchievementRule> projRules = man.getPerProjectRules ();

        Set<AchievementRule> userRules = man.getUserRules ();

        Map<String, Set<String>> achieved = man.getAchievedAchievementIds (this.viewer);

        Box main = new Box (BoxLayout.Y_AXIS);
        
        final JScrollPane lscroll = new JScrollPane (main);
        lscroll.setBorder (null);
        lscroll.setOpaque (false);
        lscroll.getViewport ().setBorder (null);
        lscroll.getViewport ().setOpaque (false);
        lscroll.getVerticalScrollBar ().setUnitIncrement (20);
        lscroll.setAlignmentX (Component.LEFT_ALIGNMENT);
        lscroll.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                               Short.MAX_VALUE));        
                
        this.add (lscroll);
                
        AccordionItem gen = new AccordionItem ("General - " + achieved.get ("user").size () + " / " + userRules.size ())
        {
            
            @Override
            public JComponent getContent ()
            {
                
                return _this.getAchievementsBox (userRules,
                                                 achieved.get ("user"),
                                                 "user");
                
            }
            
        };
        
        this.headers.put ("user",
                          gen.getHeader ());
        
        main.add (gen);

        if (this.viewer instanceof AbstractProjectViewer)
        {
        
            gen.setBorder (new EmptyBorder (0, 10, 0, 0));
            AccordionItem proj = new AccordionItem ("Project - " + achieved.get ("project").size () + " / " + projRules.size ())
            {
                
                @Override
                public JComponent getContent ()
                {
            
                    return _this.getAchievementsBox (projRules,
                                                     achieved.get ("project"),
                                                     "project");
                                              
                }
                
            };
            
            this.headers.put ("project",
                              proj.getHeader ());
            
            proj.setBorder (new EmptyBorder (0, 10, 0, 0));
        
            main.add (proj);
            
        }
        
        UIUtils.doLater (new ActionListener ()
        {
            
            @Override
            public void actionPerformed (ActionEvent ev)
            {
                
                lscroll.getVerticalScrollBar ().setValue (0);
                                
            }
            
        });
    }

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

    public static JCheckBox createAchievementsSoundEnabledInFullScreenCheckbox ()
    {

        final AchievementsManager man = Environment.getAchievementsManager ();
        
        final JCheckBox fullScreenSoundsOn = UIUtils.createCheckBox ("Play the sound in full screen mode");

        fullScreenSoundsOn.setSelected (man.isSoundsInFullScreenEnabled ());
        
        fullScreenSoundsOn.addActionListener (new ActionListener ()
        {
           
            @Override
            public void actionPerformed (ActionEvent ev)
            {
                
                man.setSoundsInFullScreenEnabled (fullScreenSoundsOn.isSelected ());
                
            }
            
        });
        
        return fullScreenSoundsOn;
        
    }

    public static JCheckBox createAchievementsEnabledCheckbox (final boolean showOffMessage)
    {
        
        final AchievementsManager man = Environment.getAchievementsManager ();
        
        final JCheckBox achievementsOn = UIUtils.createCheckBox ("Turn achievements on");
        achievementsOn.setSelected (man.isAchievementsEnabled ());
        
        achievementsOn.addActionListener (new ActionListener ()
        {
           
            @Override
            public void actionPerformed (ActionEvent ev)
            {
                
                if (showOffMessage)
                {
                
                    SwingUtilities.invokeLater (new Runnable ()
                    {
    
                        public void run ()
                        {
                    
                            if (!achievementsOn.isSelected ())
                            {
                                
                                UIUtils.showMessage (achievementsOn.getParent (),
                                                     "You can turn achievements on at any time by using the Achievements option in the Project menu.");
                                
                            }
    
                        }
                        
                    });

                }

                man.setAchievementsEnabled (achievementsOn.isSelected ());
                
            }
            
        });
 
        return achievementsOn;
        
    }

    public static JCheckBox createAchievementsSoundEnabledCheckbox ()
    {

        final AchievementsManager man = Environment.getAchievementsManager ();

        final JCheckBox achievementSounds = UIUtils.createCheckBox ("Play a sound when an achievement is reached");
        achievementSounds.setSelected (man.isSoundEnabled ());        
        
        achievementSounds.addActionListener (new ActionListener ()
        {
           
            @Override
            public void actionPerformed (ActionEvent ev)
            {
                
                man.setSoundEnabled (achievementSounds.isSelected ());
                
            }
            
        });
        
        return achievementSounds;
        
    }
    
}