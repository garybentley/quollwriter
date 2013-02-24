package com.quollwriter.ui.components;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;

public class QPopup extends Box
{

    public static Border defaultBorder = new CompoundBorder (UIUtils.popupPanelDropShadow,
                                                             new MatteBorder (1,
                                                                              1,
                                                                              1,
                                                                              1,
                                                                              com.quollwriter.ui.UIUtils.getBorderColor ()));
    /*
                                                                              new Color (171,
                                                                                         171,
                                                                                         171,
                                                                                         171)));
*/
    public static Font defaultTitleFont = new Font ("Tahoma",
                                                    Font.BOLD,
                                                    14);

    private Box      box = null;
    protected Header header = null;
    private Dragger  dragger = null;

    public QPopup(String    title,
                  Icon      icon,
                  Component controls)
    {

        this (new Header (title,
                          icon,
                          controls));

        this.header.setFont (this.header.getFont ().deriveFont ((float) com.quollwriter.ui.UIUtils.scaleToScreenSize (14)).deriveFont (Font.PLAIN));
        this.header.setTitleColor (com.quollwriter.ui.UIUtils.getTitleColor ());

    }

    public QPopup(Header h)
    {

        super (BoxLayout.Y_AXIS);

        this.header = h;

        this.setBorder (QPopup.defaultBorder);
        this.setOpaque (false);
        this.setDoubleBuffered (true);
        this.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                            Short.MAX_VALUE));
        this.box = new Box (BoxLayout.Y_AXIS);
        //this.box.setBackground (UIManager.getColor ("Panel.background"));
        this.box.setBackground (com.quollwriter.ui.UIUtils.getComponentColor ());
        this.box.setOpaque (true);
        this.box.add (this.header);
        this.box.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.header.setAlignmentX (Component.LEFT_ALIGNMENT);

        this.header.setPadding (new Insets (3, 5, 0, 3));
        
        this.add (this.box);

    }

    public Header getHeader ()
    {

        return this.header;

    }

    public JComponent getContent ()
    {

        return (JComponent) this.box.getComponent (1);

    }

    public void hideIn (int     secs,
                        final boolean remove)
    {
        
        final QPopup _this = this;
        
        Timer t = new Timer (secs * 1000,
                             new ActionAdapter ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {
                
                _this.setVisible (false);

                if (remove)
                {
                    
                    _this.removeFromParent ();
                    
                }
                
            }
            
        });

        t.setRepeats (false);
                
        t.start ();
        
    }
    
    public void removeFromParent ()
    {
        
        this.setVisible (false);
        
        Container p = this.getParent ();
        
        if (p != null)
        {
            
            p.remove (this);
            
            p.validate ();
            
            p.repaint ();
            
        }
        
    }
    
    public void setContent (JComponent content)
    {

        content.setAlignmentX (Component.LEFT_ALIGNMENT);
        /*
        content.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                               content.getPreferredSize ().height));
        */
        if (this.box.getComponentCount () >= 2)
        {

            this.box.remove (1);

        }

        this.box.add (content,
                      1);

        this.box.add (Box.createVerticalGlue ());

    }

    public Dragger setDraggable (Component within)
    {

        if (within == null)
        {
            
            if (this.getParent () == null)
            {
                
                throw new IllegalArgumentException ("No parent");
                
            } else {
                
                within = this.getParent ();
                
            }
            
        }

        this.dragger = new Dragger (this.header,
                                    this,
                                    within);

        return this.dragger;

    }

}
