package com.quollwriter.ui.whatsnewcomps;

import java.awt.Dimension;

import java.util.Set;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;

import com.gentlyweb.properties.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.achievements.*;
import com.quollwriter.achievements.rules.*;
import com.quollwriter.achievements.ui.*;
import com.quollwriter.ui.components.ActionAdapter;

public class version_1_4_4 implements WhatsNewComponentProvider
{
    
    private Map<String, JComponent> comps = new HashMap ();
    
    public version_1_4_4 ()
    {
        
    }
    
    public JComponent getComponent (final AbstractViewer pv,
                                    final String         id)
    {
        
        JComponent c = this.comps.get (id);
        
        if (c != null)
        {
            
            return c;
            
        }
                
        if (id.equals ("achievements"))
        {

            final AchievementsManager man = Environment.getAchievementsManager ();

            man.setAchievementsEnabled (true);
            
            man.setSoundEnabled (true);    
            
            final Box b = new Box (BoxLayout.Y_AXIS);
        
            c = b;
            
            b.add (Achievements.createAchievementsEnabledCheckbox (true));
                        
            b.add (Box.createVerticalStrut (5));

            JLabel viewExample = UIUtils.createClickableLabel ("View an example achievement",
                                                               null);
            
            viewExample.setBorder (new EmptyBorder (0, 15, 0, 0));
            
            b.add (viewExample);
            
            Box arb = null;
                        
            Set<AchievementRule> rules = man.getUserRules ();

            int r = new Random ().nextInt (rules.size ());
            
            for (AchievementRule ar : rules)
            {
                                
                arb = new AchievementBox (ar,
                                          false,
                                          true);

                arb.setBorder (new CompoundBorder (new EmptyBorder (10, 15, 0, 0),
                                                   new CompoundBorder (UIUtils.createLineBorder (),
                                                                       new EmptyBorder (5, 5, 5, 5))));
                arb.setVisible (false);
                                
                r--;
                
                if (r <= 0)
                {
                    
                    break;
                    
                }
                                
            }
            
            b.add (arb);
            
            final Box _arb = arb;
            
            viewExample.addMouseListener (new MouseAdapter ()
            {
                
                public void mousePressed (MouseEvent ev)
                {
               
                    _arb.setVisible (!_arb.isVisible ());

                    _arb.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                        _arb.getPreferredSize ().height));

                    
                    b.validate ();
                    b.repaint ();  
                    
                }
                
            });
            
            b.add (Box.createVerticalStrut (15));
            
            final JCheckBox soundB = Achievements.createAchievementsSoundEnabledCheckbox ();

            final JCheckBox soundFSB = Achievements.createAchievementsSoundEnabledInFullScreenCheckbox ();

            soundB.addActionListener (new ActionAdapter ()
            {
               
                public void actionPerformed (ActionEvent ev)
                {
                    
                    soundFSB.setEnabled (soundB.isSelected ());
                    
                }
                
            });
            
            b.add (soundB);

            b.add (Box.createVerticalStrut (10));

            soundFSB.setBorder (new EmptyBorder (0, 15, 0, 0));            

            b.add (soundFSB);

            b.add (Box.createVerticalStrut (10));

            JLabel playSound = UIUtils.createClickableLabel ("Play the sound",
                                                             null);
            
            playSound.setBorder (new EmptyBorder (0, 15, 0, 0));

            playSound.addMouseListener (new MouseAdapter ()
            {

                public void mousePressed (MouseEvent ev)
                {
               
                    man.playAchievementSound ();
                    
                }
                
            });
            
            b.add (playSound);
                        
        }
        
        if (id.equals ("tips"))
        {
            
            final JCheckBox tips = UIUtils.createCheckBox ("Show tips when Quoll Writer starts");
            tips.setSelected (true);
        
            tips.addActionListener (new ActionAdapter ()
            {
           
                public void actionPerformed (ActionEvent ev)
                {

                    try
                    {
                
                        Environment.setUserProperty (Constants.SHOW_TIPS_PROPERTY_NAME,
                                                     new BooleanProperty (Constants.SHOW_TIPS_PROPERTY_NAME,
                                                                          tips.isSelected ()));

                    } catch (Exception e) {
                        
                        Environment.logError ("Unable to set the tips property",
                                              e);
                        
                    }
                    
                }
                
            });

            c = tips;
            
        }

        this.comps.put (id,
                        c);
        
        return c;
        
    }
    
}