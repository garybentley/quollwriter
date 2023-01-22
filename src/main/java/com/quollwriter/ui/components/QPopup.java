package com.quollwriter.ui.components;

import java.awt.*;

import java.util.List;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.LayerUI;
import java.awt.event.*;
import com.quollwriter.ui.PopupsSupported;
import com.quollwriter.ui.AbstractViewer;

public class QPopup extends Box
{

    public static Border defaultBorder = new CompoundBorder (UIUtils.popupPanelDropShadow,
                                                             new MatteBorder (1,
                                                                              1,
                                                                              1,
                                                                              1,
                                                                              com.quollwriter.ui.UIUtils.getBorderColor ()));

    private Box      box = null;
    protected Header header = null;
    private Dragger  dragger = null;
    private List<PopupListener> listeners = new ArrayList ();
    private String name = null;
    private boolean allowRemoveOnEscape = true;
    private boolean removeOnClose = true;

    public QPopup(String    title,
                  Icon      icon,
                  Component controls)
    {

        this (new Header (title,
                          icon,
                          controls));

        this.header.setFont (this.header.getFont ().deriveFont ((float) com.quollwriter.ui.UIUtils.getScaledFontSize (14)).deriveFont (Font.PLAIN));
        this.header.setTitleColor (com.quollwriter.ui.UIUtils.getTitleColor ());

    }

    public QPopup(Header h)
    {

        super (BoxLayout.Y_AXIS);

        final QPopup _this = this;
        
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

        this.header.addMouseListener (new MouseAdapter ()
        {
           
            public void mouseReleased (MouseEvent ev)
            {
                
                _this.toFront ();
                
            }
            
        });
        
        ActionMap am = this.getActionMap ();
        
        am.put ("hide",
                new ActionAdapter ()
                {
                    
                    public void actionPerformed (ActionEvent ev)
                    {
                        
                        if (!_this.allowRemoveOnEscape)
                        {
                            
                            return;
                            
                        }
                                                
                        _this.removeFromParent ();
                        
                    }
                    
                });
        
        InputMap im = this.getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW);
        
        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_ESCAPE,
                                        0),
                "hide");
                
        this.add (this.box);

    }
    
    public void setRemoveOnClose (boolean v)
    {
        
        this.removeOnClose = v;
        
    }
    
    public void setAllowRemoveOnEscape (boolean v)
    {
        
        this.allowRemoveOnEscape = v;
        
    }
    
    public void setPopupName (String n)
    {
        
        this.name = n;
        
    }
    
    public String getPopupName ()
    {
        
        return this.name;
        
    }
    
    public void addPopupListener (PopupListener l)
    {
        
        this.listeners.add (l);
        
    }
    
    public void removePopupListener (PopupListener l)
    {
        
        this.listeners.remove (l);
        
    }
    
    public Header getHeader ()
    {

        return this.header;

    }

    public JComponent getContent ()
    {

        return (JComponent) this.box.getComponent (1);

    }
    
    public void resize ()
    {
        
        JComponent c = this.getContent ();
        c.setPreferredSize (null);

        c.validate ();
        c.setPreferredSize (new Dimension (com.quollwriter.ui.UIUtils.getPopupWidth (),
                                           c.getPreferredSize ().height));
        c.setMinimumSize (c.getPreferredSize ());
  
        Container p = this.getParent ();

        while (p != null)
        {
        
            if (p instanceof PopupsSupported)
            {
                                      
                ((PopupsSupported) p).showPopupAt (this,
                                                   this.getLocation (),
                                                   false);

                break;
        
            }
            
            p = p.getParent ();
            
        }
  
        PopupEvent ev = new PopupEvent (this);
  
        for (PopupListener l : this.listeners)
        {
            
            l.popupResized (ev);
            
        }
  
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
    
    public ActionListener getCloseAction ()
    {
    
        final QPopup _this = this;
    
        return new ActionListener ()
        {
        
            public void actionPerformed (ActionEvent ev)
            {
                
                _this.removeFromParent ();
                
            }
        
        };    
        
    }
    
    public void removeFromParent ()
    {

        this.setVisible (false);
        
        Container p = this.getParent ();
           
        if (!this.removeOnClose)
        {
            
            return;
                        
        }
        
        if (p != null)
        {
            
            p.remove (this);
            
            p.validate ();
            
            p.repaint ();
            
            while (p != null)
            {
                
                if ((p instanceof AbstractViewer)
                    &&
                    (this.name != null)
                   )
                {
                    
                    ((AbstractViewer) p).removeNamedPopup (this.name);
                    
                }
                
                p = p.getParent ();
                            
            }
                        
        }
        
    }
    
    public void setVisible (boolean v)
    {
        
        super.setVisible (v);
        
        if (v)
        {
            
            PopupEvent ev = new PopupEvent (this);
      
            for (PopupListener l : this.listeners)
            {
                
                l.popupShown (ev);
                
            }            
            
        } else {
            
            PopupEvent ev = new PopupEvent (this);
      
            for (PopupListener l : this.listeners)
            {
                
                l.popupHidden (ev);
                
            }            
            
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

    public void toFront ()
    {
                
        if (this.getParent () instanceof JLayeredPane)
        {
            
            JLayeredPane p = (JLayeredPane) this.getParent ();
            
            p.setPosition (this,
                           0);
            
        }
        
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
