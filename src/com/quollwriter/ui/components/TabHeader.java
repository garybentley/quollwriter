/*
package com.quollwriter.ui.components;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import com.quollwriter.ui.events.*;


public class TabHeader extends Box
{

    public static final int TAB_CLOSING = 1;
    public static final int TAB_CLOSED = 2;

    private ImagePanel     closeB = null;
    private JTabbedPane    tp = null;
    private JLabel         label = null;
    private java.util.List listeners = new java.util.ArrayList ();
    private String         title = null;

    private MouseAdapter closeMouseListener = new MouseAdapter ()
    {

        private void redispatch (MouseEvent e)
        {

            e = SwingUtilities.convertMouseEvent (e.getComponent (),
                                                  e,
                                                  TabHeader.this);

            e.getComponent ().dispatchEvent (e);

        }

        public void mouseMoved (MouseEvent e)
        {

            this.redispatch (e);

        }

        public void mouseReleased (MouseEvent e)
        {

            this.redispatch (e);

        }

        public void mouseEntered (MouseEvent e)
        {

            this.redispatch (e);

        }

        public void mouseExited (MouseEvent e)
        {

            this.redispatch (e);

        }

        public void mouseClicked (MouseEvent e)
        {

            this.redispatch (e);

        }

        public void mousePressed (MouseEvent e)
        {

            int index = tp.indexOfTabComponent (TabHeader.this);

            Component c = tp.getTabComponentAt (index);

            VetoableActionEvent ev = new VetoableActionEvent (c,
                                                              TabHeader.TAB_CLOSING,
                                                              "tabClosing");

            TabHeader.this.fireActionEvent (ev);

            if (ev.isCancelled ())
            {

                this.redispatch (e);

                return;

            }

            tp.removeMouseListener (this);

            int ind = tp.indexOfTabComponent (c);

            if (ind != -1)
            {

                tp.remove (ind);

            }

            this.redispatch (e);

            ActionEvent aev = new ActionEvent (c,
                                               TabHeader.TAB_CLOSED,
                                               "tabClosed");

            TabHeader.this.fireActionEvent (aev);

        }

    };

    private MouseAdapter headerMouseListener = new MouseAdapter ()
    {

        private void redispatch (MouseEvent e)
        {

            e = SwingUtilities.convertMouseEvent (TabHeader.this,
                                                  e,
                                                  tp);

            e.getComponent ().dispatchEvent (e);

        }

        public void mouseMoved (MouseEvent e)
        {

            this.redispatch (e);

        }

        public void mouseDragged (MouseEvent e)
        {

            this.redispatch (e);

        }

        public void mouseReleased (MouseEvent e)
        {

            this.redispatch (e);

        }

        public void mouseEntered (MouseEvent e)
        {

            closeB.showImage (true);

            this.redispatch (e);

        }

        public void mouseExited (MouseEvent e)
        {

            closeB.showImage (false);

            this.redispatch (e);

        }

        public void mouseClicked (MouseEvent e)
        {

            this.redispatch (e);

        }

        public void mousePressed (MouseEvent e)
        {

            this.redispatch (e);

        }

    };

    public TabHeader(JTabbedPane tp,
                     ImageIcon   cancelIcon,
                     Image       transparentImage,
                     String      title)
    {

        super (BoxLayout.LINE_AXIS);

        this.tp = tp;
        this.title = title;

        this.setBackground (new Color (0,
                                       0,
                                       0,
                                       0));
        this.setOpaque (false);
        
        this.label = new JLabel (this.title);
        this.label.setBorder (null);
        this.label.setBackground (new Color (0,
                                             0,
                                             0,
                                             0));
        
        this.label.setOpaque (false);
        
        if (this.label.getPreferredSize ().width > 250)
        {
        
            this.label.setPreferredSize (new Dimension (250,
                                                  this.label.getPreferredSize ().height));

        }

        if (this.title == null)
        {
            
            this.label.setVisible (false);            
                                                  
        }
        
        this.add (this.label);
        this.add (Box.createHorizontalStrut (10));

        this.closeB = new ImagePanel (cancelIcon.getImage (),
                                      transparentImage);
        this.closeB.setBorder (null);
        this.closeB.setOpaque (false);
        this.closeB.addMouseListener (this.closeMouseListener);
        this.closeB.showImage (false);

        if (!this.canClose ())
        {
            
            this.closeB.setVisible (false);
            
        }
        
        this.add (this.closeB);

        this.setBorder (new EmptyBorder (1,
                                         1,
                                         1,
                                         1));

        this.addMouseListener (this.headerMouseListener);
        this.addMouseMotionListener (this.headerMouseListener);

    }
    
    public boolean canClose ()
    {
        
        return true;
        
    }
    
    public void setFont (Font f)
    {

        this.label.setFont (f);

    }

    public void setCloseIcon (ImageIcon i)
    {

        this.setCloseIcon (i.getImage ());

    }

    public void setCloseIcon (Image i)
    {

        this.closeB.setImage (i);
        this.closeB.showImage (false);
        this.closeB.setVisible (true);

        // Get a "faded" version.

    }

    public void setIcon (Icon i)
    {

        this.label.setIcon (i);

    }

    public void setComponentChanged (boolean v)
    {

        if (v)
        {

            this.label.setText (this.title + " *");
            this.label.setFont (this.label.getFont ().deriveFont (Font.BOLD));

        } else
        {

            this.label.setText (this.title);
            this.label.setFont (this.label.getFont ().deriveFont (Font.PLAIN));

        }

    }

    public String getTitle ()
    {

        return this.title;

    }

    public void setTitle (String t)
    {

        this.label.setText (t);

        this.title = t;
        
        this.label.setVisible (true);

    }

    protected void fireActionEvent (ActionEvent ev)
    {

        for (int i = 0; i < this.listeners.size (); i++)
        {

            ActionListener al = (ActionListener) this.listeners.get (i);

            al.actionPerformed (ev);

        }

    }

    public void removeActionListener (ActionListener a)
    {

        this.listeners.remove (a);

    }

    public void addActionListener (ActionListener a)
    {

        this.listeners.add (a);

    }

}
*/

package com.quollwriter.ui.components;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import com.quollwriter.ui.events.*;

import com.quollwriter.events.*;

public class TabHeader extends Box
{

    public static final int TAB_CLOSING = 1;
    public static final int TAB_CLOSED = 2;

    private ImagePanel     closeB = null;
    private JTabbedPane    tp = null;
    private JLabel         label = null;
    private java.util.List listeners = new java.util.ArrayList ();
    private String         title = null;

    private void closeTab ()
    {
        
        int index = this.tp.indexOfTabComponent (this);

        Component c = this.tp.getTabComponentAt (index);

        VetoableActionEvent ev = new VetoableActionEvent (c,
                                                          TabHeader.TAB_CLOSING,
                                                          "tabClosing");

        this.fireActionEvent (ev);

        if (ev.isCancelled ())
        {

            return;

        }
  
        ActionEvent aev = new ActionEvent (c,
                                           TabHeader.TAB_CLOSED,
                                           "tabClosed");

        this.fireActionEvent (aev);
        
    }
    
    public TabHeader(JTabbedPane tp,
                     ImageIcon   cancelIcon,
                     Image       transparentImage,
                     String      title)
    {

        super (BoxLayout.LINE_AXIS);
        
        final TabHeader _this = this;
        
        this.tp = tp;
        this.title = title;

        this.setBackground (new Color (0,
                                       0,
                                       0,
                                       0));
        this.setOpaque (false);
        
        this.label = new JLabel (this.title);
        this.label.setBorder (null);
        this.label.setBackground (new Color (0,
                                             0,
                                             0,
                                             0));
        
        this.label.setOpaque (false);
        
        if (this.label.getPreferredSize ().width > 250)
        {
        
            this.label.setPreferredSize (new Dimension (250,
                                                  this.label.getPreferredSize ().height));

        }

        if (this.title == null)
        {
            
            this.label.setVisible (false);            
                                                  
        }
        
        this.add (this.label);
        this.add (Box.createHorizontalStrut (10));

        this.closeB = new ImagePanel (cancelIcon.getImage (),
                                      transparentImage);
        this.closeB.setBorder (null);
        this.closeB.setOpaque (false);

        this.closeB.addMouseListener (new MouseAdapter ()
        {
    
            private void redispatch (MouseEvent e)
            {
    
                e = SwingUtilities.convertMouseEvent (e.getComponent (),
                                                      e,
                                                      TabHeader.this);
    
                e.getComponent ().dispatchEvent (e);
    
            }
    
            public void mouseMoved (MouseEvent e)
            {
    
                this.redispatch (e);
    
            }
    
            public void mouseReleased (MouseEvent e)
            {
    
                this.redispatch (e);
    
            }
    
            public void mouseEntered (MouseEvent e)
            {
    
                this.redispatch (e);
    
            }
    
            public void mouseExited (MouseEvent e)
            {
    
                this.redispatch (e);
    
            }
    
            public void mouseClicked (MouseEvent e)
            {
    
                this.redispatch (e);
    
            }
    
            public void mousePressed (MouseEvent e)
            {
    
                _this.closeTab ();
                
                this.redispatch (e);

            }
    
        });
/*
 *        CANT DO THIS THE HANDLER WONT FIRE EVENTS PROPERLY, NO IDEA WHY, HAD
 *        TO REVERT BACK TO THE OLD HANDLER
        this.closeB.addMouseListener (new MouseEventHandler ()
        {
            
            @Override
            public void redispatch (MouseEvent e)
            {
    
                e = SwingUtilities.convertMouseEvent (e.getComponent (),
                                                      e,
                                                      _this);
    
                e.getComponent ().dispatchEvent (e);
  
            }
            
            @Override
            public void handlePress (MouseEvent ev)
            {
                
                _this.closeB.removeMouseListener (this);
                                
                _this.closeTab ();
                
            }
            
        });
  */      
        this.closeB.showImage (false);

        if (!this.canClose ())
        {
            
            this.closeB.setVisible (false);
            
        }
        
        this.add (this.closeB);

        this.setBorder (new EmptyBorder (1,
                                         1,
                                         1,
                                         1));
        
        MouseEventHandler l = new MouseEventHandler ()
        {
           
            public void redispatch (MouseEvent e)
            {
    
                e = SwingUtilities.convertMouseEvent (_this,
                                                      e,
                                                      _this.tp);
    
                e.getComponent ().dispatchEvent (e);
    
            }
           
            public void mouseEntered (MouseEvent e)
            {
    
                closeB.showImage (true);
    
                super.mouseEntered (e);
    
            }
    
            public void mouseExited (MouseEvent e)
            {
    
                closeB.showImage (false);
    
                super.mouseExited (e);
    
            }
           
            @Override
            public void handlePress (MouseEvent ev)
            {
                
                int index = _this.tp.indexOfTabComponent (_this);

                _this.tp.setSelectedIndex (index);
                                
            }
           
            @Override
            public void handleMiddlePress (MouseEvent mev)
            {
                
                _this.closeTab ();

            }
            
            @Override
            public void fillPopup (JPopupMenu menu,
                                   MouseEvent ev)
            {
                
                menu.add (com.quollwriter.ui.UIUtils.createMenuItem ("Close",
                                                  com.quollwriter.Constants.CLOSE_ICON_NAME,
                                                  new ActionListener ()
                                                  {
                                                       
                                                      public void actionPerformed (ActionEvent ev)
                                                      {
                                                           
                                                           _this.closeTab ();
                                                           
                                                      }
                                                       
                                                  }));
                
            }
            
        };                
                
        this.addMouseListener (l);
        this.addMouseMotionListener (l);
        
    }
    
    public boolean canClose ()
    {
        
        return true;
        
    }
    
    public void setFont (Font f)
    {

        this.label.setFont (f);

    }

    public void setCloseIcon (ImageIcon i)
    {

        this.setCloseIcon (i.getImage ());

    }

    public void setCloseIcon (Image i)
    {

        this.closeB.setImage (i);
        this.closeB.showImage (false);
        this.closeB.setVisible (true);

        // Get a "faded" version.

    }

    public void setIcon (Icon i)
    {

        this.label.setIcon (i);

    }

    public void setComponentChanged (boolean v)
    {

        if (v)
        {

            this.label.setText (this.title + " *");
            this.label.setFont (this.label.getFont ().deriveFont (Font.BOLD));

        } else
        {

            this.label.setText (this.title);
            this.label.setFont (this.label.getFont ().deriveFont (Font.PLAIN));

        }

    }

    public String getTitle ()
    {

        return this.title;

    }

    public void setTitle (String t)
    {

        this.label.setText (t);

        this.title = t;
        
        this.label.setVisible (true);

    }

    protected void fireActionEvent (ActionEvent ev)
    {

        for (int i = 0; i < this.listeners.size (); i++)
        {

            ActionListener al = (ActionListener) this.listeners.get (i);

            al.actionPerformed (ev);

        }

    }

    public void removeActionListener (ActionListener a)
    {

        this.listeners.remove (a);

    }

    public void addActionListener (ActionListener a)
    {

        this.listeners.add (a);

    }

}
