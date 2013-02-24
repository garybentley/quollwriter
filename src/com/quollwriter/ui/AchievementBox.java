package com.quollwriter.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

import com.quollwriter.*;

import com.quollwriter.achievements.rules.*;

import com.quollwriter.ui.components.ImagePanel;

public class AchievementBox extends Box
{
    
    private AchievementRule rule = null;
    
    private ImagePanel ip = null;
    
    public AchievementBox (AchievementRule ar,
                           boolean         achieved,
                           boolean         limitDescSize)
    {
        
        super (BoxLayout.X_AXIS);
        
        this.rule = ar;        
                        
        this.ip = new ImagePanel (this.getIcon (achieved),
                                  null);
        
        this.ip.setAlignmentY (JComponent.TOP_ALIGNMENT);
        
        this.add (this.ip);

        this.add (Box.createHorizontalStrut (5));

        Box t = new Box (BoxLayout.Y_AXIS);
        t.setAlignmentY (JComponent.TOP_ALIGNMENT);
        
        this.add (t);
        
        JLabel name = new JLabel (Environment.replaceObjectNames (ar.getName ()));
        
        name.setFont (name.getFont ().deriveFont ((float) UIUtils.scaleToScreenSize (12)).deriveFont (Font.PLAIN));        
        //name.setFont (name.getFont ().deriveFont (((float) name.getFont ().getSize () + 2f)).deriveFont (Font.PLAIN));
        name.setAlignmentX (JComponent.LEFT_ALIGNMENT);
        
        t.add (name);
        
        JTextArea desc = new JTextArea (Environment.replaceObjectNames (ar.getDescription ()));
        desc.setAlignmentX (JComponent.LEFT_ALIGNMENT);
        
        if (limitDescSize)
        {

            desc.setSize (new Dimension (250,
                                         500));

        }

        desc.setWrapStyleWord (true);
        desc.setLineWrap (true);
        desc.setEditable (false);
        desc.setOpaque (false);
        desc.setBorder (new EmptyBorder (3, 5, 0, 5));

        t.add (desc);
        
        this.setAlignmentX (JComponent.LEFT_ALIGNMENT);
        this.setBorder (new EmptyBorder (5, 5, 5, 5));
        this.setMinimumSize (new Dimension (200,
                                            this.getPreferredSize ().height));        
        
    }
 
    public void setAchieved (boolean achieved)
    {
        
        this.ip.setIcon (this.getIcon (achieved));
        
    }
 
    private ImageIcon getIcon (boolean achieved)
    {
        
        ImageIcon image = null;
        
        try
        {
            
            image = (this.rule.getIcon () != null ? Environment.getIcon (this.rule.getIcon (),
                                                                         Constants.ICON_ACHIEVEMENT_HEADER) : Environment.getAchievementIcon ());
            
        } catch (Exception e) {
            
            // Ignore.
            
        }
        
        if (image == null)
        {
            
            image = Environment.getAchievementIcon ();
            
        }
        
        image.setImage (image.getImage ().getScaledInstance (24, 24, Image.SCALE_SMOOTH));

        if (achieved)
        {

            ImageIcon tick =Environment.getIcon ("tick",
                                                 Constants.ICON_ACHIEVEMENT_HEADER);
            
            tick.setImage (tick.getImage ().getScaledInstance (12, 12, Image.SCALE_SMOOTH));

            image = UIUtils.overlayImage (image,
                                          tick,
                                          "br");

        }        
        
        return image;
        
    }
 
    public AchievementRule getRule ()
    {
        
        return this.rule;
        
    }
    
}