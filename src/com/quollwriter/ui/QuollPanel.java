package com.quollwriter.ui;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.text.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.plaf.LayerUI;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import com.gentlyweb.properties.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.events.*;

import com.quollwriter.ui.actionHandlers.*;

public abstract class QuollPanel<E extends AbstractViewer> extends JRootPane implements Stateful,
                                                                                        PopupsSupported
{

    public static final int UNSAVED_CHANGES_ACTION_EVENT = 0;

    //public static final int SAVED = 1;

    //public static final String HAS_CHANGES_COMMAND = "hasChanges";
    //public static final String NO_CHANGES_COMMAND = "noChanges";

    protected E viewer = null;

    protected Box                                   content = null;
    private java.util.List                          actionListeners = new ArrayList ();
    private JToolBar                                toolBar = null;
    private boolean                                 readyForUse = false;
    private MouseEventHandler mouseEventHandler = null;
    
    public QuollPanel (E viewer)
    {

        this.viewer = viewer;

        this.setOpaque (false);
        this.setBackground (null);
        
        this.content = new Box (BoxLayout.Y_AXIS);
        this.content.setBackground (UIUtils.getComponentColor ());
        
        this.getContentPane ().setBackground (UIUtils.getComponentColor ());
        
        this.getContentPane ().add (this.content);

        final QuollPanel _this = this;
        
        this.mouseEventHandler = this.getDefaultMouseEventHandler ();
        
        this.getContentPane ().add (new JLayer<JComponent> (this.content, new LayerUI<JComponent> ()
        {
            
            @Override
            public void installUI(JComponent c) {
                super.installUI(c);
                // enable mouse motion events for the layer's subcomponents
                ((JLayer) c).setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK|AWTEvent.MOUSE_MOTION_EVENT_MASK);
            }

            @Override
            public void uninstallUI(JComponent c) {
                super.uninstallUI(c);
                // reset the layer event mask
                ((JLayer) c).setLayerEventMask(0);
            }
             
            @Override
            public void processMouseEvent (MouseEvent                   ev,
                                           JLayer<? extends JComponent> l)
            {
                
                if (_this.mouseEventHandler == null)
                {
                    
                    return;
                    
                }
                
                if (ev.getID () == MouseEvent.MOUSE_MOVED)
                {
                    
                    _this.mouseEventHandler.mouseMoved (ev);
                    
                    return;
                    
                }
                
                if (ev.getID () == MouseEvent.MOUSE_RELEASED)
                {
                    
                    _this.mouseEventHandler.mouseReleased (ev);
                    
                    return;
                    
                }

                if (ev.getID () == MouseEvent.MOUSE_PRESSED)
                {
                    
                    _this.mouseEventHandler.mousePressed (ev);
                    
                    return;
                    
                }

                if (ev.getID () == MouseEvent.MOUSE_DRAGGED)
                {
                    
                    _this.mouseEventHandler.mousePressed (ev);
                    
                    return;
                    
                }

                if (ev.getID () == MouseEvent.MOUSE_ENTERED)
                {
                    
                    _this.mouseEventHandler.mouseEntered (ev);
                    
                    return;
                    
                }
                
                if (ev.getID () == MouseEvent.MOUSE_EXITED)
                {
                    
                    _this.mouseEventHandler.mouseExited (ev);
                    
                    return;
                    
                }

                if (ev.getID () == MouseEvent.MOUSE_CLICKED)
                {
                    
                    _this.mouseEventHandler.mouseClicked (ev);
                    
                    return;
                    
                }
                                
            }   
            
        }));
                
    }

    /**
     * Set a mouse event handler for the panel, mouse events are forwarded to the handler
     * which should makes it's own checks to determine what event should be handled.
     *
     * Set to null to prevent the default behavior which is to fill the popup menu.
     *
     * @param m The mouse event handler.
     */
    public void setMouseEventHandler (MouseEventHandler m)
    {
        
        this.mouseEventHandler = m;
        
    }
    
    /**
     * Get the current mouse event handler.
     *
     * @return The current mouse event handler.
     */
    public MouseEventHandler getMouseEventHandler ()
    {
        
        return this.mouseEventHandler;
        
    }
    
    /**
     * Creates a default mouse event handler that just fills the popup menu for the panel by calling
     * this.fillPopupMenu.
     *
     * @return A default mouse event handler.
     */
    public MouseEventHandler getDefaultMouseEventHandler ()
    {
        
        final QuollPanel _this = this;
        
        return new MouseEventHandler ()
        {
            
            @Override
            public void fillPopup (JPopupMenu m,
                                   MouseEvent ev)
            {

                _this.fillPopupMenu (ev,
                                     m);
                
            }

        };
        
    }
    
    public E getViewer ()
    {
        
        return this.viewer;
        
    }
    
    public boolean isReadyForUse ()
    {
        
        return this.readyForUse;
        
    }
    
    public void setReadyForUse (boolean v)
    {
        
        this.readyForUse = true;
        
    }
    
    public abstract String getPanelId ();
    
    public abstract void close ();

    public abstract void init ()
                        throws GeneralException;

    public abstract void getState (Map<String, Object> s);

    public abstract void setState (Map<String, String> s,
                                   boolean             hasFocus);

    public abstract ImageIcon getIcon (int type);
                                   
    //public abstract String getIconType ();

    public abstract String getTitle ();
    
    public abstract void fillToolBar (JToolBar toolBar,
                                      boolean  fullScreen);

    public abstract void fillPopupMenu (MouseEvent ev,
                                        JPopupMenu popup);

    public abstract List<Component> getTopLevelComponents ();

    //public abstract <T extends NamedObject> void refresh (T n);

    @Override
    public void remove (Component c)
    {

        this.content.remove (c);

    }

    @Override
    public Component add (Component c)
    {

        if (this.content == null)
        {

            super.add (c);

            return c;

        }

        if (c instanceof JComponent)
        {

            ((JComponent) c).setAlignmentX (Component.LEFT_ALIGNMENT);

        }

        this.content.add (c);

        return c;

    }

    @Override
    public Component add (Component c,
                          int       index)
    {

        if (this.content == null)
        {

            super.add (c,
                       index);

            return c;

        }

        if (c instanceof JComponent)
        {

            ((JComponent) c).setAlignmentX (Component.LEFT_ALIGNMENT);

        }

        // Add to the content box.
        this.content.add (c,
                          index);

        return c;

    }

    public void performAction (ActionEvent ev)
    {

        Action aa = (Action) this.getActionMap ().get (ev.getActionCommand ());

        if (aa != null)
        {

            aa.actionPerformed (ev);

        }

    }

    public void removeActionListener (ActionListener a)
    {

        this.actionListeners.remove (a);

    }

    public void addActionListener (ActionListener a)
    {

        this.actionListeners.add (a);

    }

    protected void fireActionEvent (ActionEvent ev)
    {

        for (int i = 0; i < this.actionListeners.size (); i++)
        {

            ActionListener a = (ActionListener) this.actionListeners.get (i);

            a.actionPerformed (ev);

        }

    }
    
    private JToolBar createToolBar ()
    {
        
        JToolBar t = new JToolBar ();
        t.setFloatable (false);
        t.setOpaque (false);
        t.setRollover (true);
        t.setAlignmentX (Component.LEFT_ALIGNMENT);
        
        return t;
        
    }
    
    public JToolBar getToolBar (boolean fullScreen)
    {

        JToolBar tb = this.createToolBar ();
                                              
        this.fillToolBar (tb,
                          fullScreen);
        
        this.toolBar = tb;
        
        return tb;

    }

    public void setToolBarButtonIcon (String actionCommand,
                                      String toolTipText,
                                      String icon)
    {

        AbstractButton ab = this.getToolBarButton (actionCommand);

        if (ab == null)
        {

            return;

        }

        ab.setToolTipText (toolTipText);

        ab.setIcon (Environment.getIcon (icon,
                                         Constants.ICON_TOOLBAR)); /* was false */

        this.toolBar.repaint ();

    }

    public AbstractButton getToolBarButton (String actionCommand)
    {

        Component[] comps = this.toolBar.getComponents ();

        for (int i = 0; i < comps.length; i++)
        {

            Component c = comps[i];

            if (c instanceof AbstractButton)
            {

                AbstractButton ab = (AbstractButton) c;

                if ((ab.getActionCommand () != null) &&
                    (ab.getActionCommand ().equals (actionCommand)))
                {

                    return ab;

                }

            }

        }

        return null;

    }

    @Override
    public void removePopup (Component c)
    {

        c.setVisible (false);
    
        this.getLayeredPane ().remove (c);

        this.getLayeredPane ().validate ();

        this.getLayeredPane ().repaint ();

        // this.content.remove (c);

        // this.content.repaint ();

    }

    public void addPopup (Component c,
                          boolean   hideOnParentClick)
    {

        this.addPopup (c,
                       hideOnParentClick,
                       false);

    }

    @Override
    public void showPopupAt (Component popup,
                             Component showAt,
                             boolean   hideOnParentClick)
    {

        Point po = SwingUtilities.convertPoint (showAt,
                                                0,
                                                0,
                                                this.getContentPane ());

        this.showPopupAt (popup,
                          po,
                          hideOnParentClick);


    }

    @Override
    public void showPopupAt (Component c,
                             Point     p,
                             boolean   hideOnParentClick)
    {

        Insets ins = this.getInsets ();

        if ((c.getParent () == null)
            &&
            (c.getParent () != this.getLayeredPane ())
           )
        {

            this.addPopup (c,
                           hideOnParentClick,
                           false);

        }

        Dimension cp = c.getPreferredSize ();

        if ((p.y + cp.height) > (this.getBounds ().height - ins.top - ins.bottom))
        {

            p = new Point (p.x,
                           p.y);

            p.y = p.y - (cp.height - c.getBounds ().height);

        }

        if (p.y < 0)
        {

            p = new Point (p.x,
                           p.y);

            p.y = 10;

        }

        if ((p.x + cp.width) > (this.getBounds ().width - ins.left - ins.right))
        {

            p = new Point (p.x,
                           p.y);

            p.x = p.x - cp.width;

        }

        if (p.x < 0)
        {

            p = new Point (p.x,
                           p.y);

            p.x = 10;

        }

        c.setBounds (p.x,
                     p.y,
                     c.getPreferredSize ().width,
                     c.getPreferredSize ().height);

        c.setVisible (true);
        this.validate ();
        this.repaint ();

    }

    public void showPopupAt (Component c,
                             Point     p,
                             String    where,
                             boolean   hideOnParentClick)
    {

        this.showPopupAt (c,
                          new Rectangle (p,
                                         c.getPreferredSize ()),
                          where,
                          hideOnParentClick);

    }

    public void showPopupAt (Component c,
                             Rectangle r,
                             String    where,
                             boolean   hideOnParentClick)
    {

        Dimension s = c.getPreferredSize ();

        int x = r.x;
        int y = r.y;

        if (where == null)
        {

            where = "below";

        }

        if (where.equals ("below"))
        {

            y = y + r.height;

        }

        if (where.equals ("above"))
        {

            y = y - s.height;

        }

        if (y < 0)
        {

            y = r.y + r.height;

        }

        if ((x + s.width) > (this.getWidth ()))
        {

            x = this.getWidth () - 20 - s.width;

        }

        if (x < 0)
        {

            x = 5;

        }

        this.showPopupAt (c,
                          new Point (x,
                                     y),
                          hideOnParentClick);

    }

    @Override
    public void addPopup (final Component c,
                          boolean         hideOnClick,
                          boolean         hideViaVisibility)
    {

        if (c.getParent () != null)
        {
            
            c.getParent ().remove (c);
            
        }

        this.getLayeredPane ().add (c,
                                    JLayeredPane.POPUP_LAYER);

        this.getLayeredPane ().moveToFront (c);

        if (hideOnClick)
        {

            final QuollPanel _this = this;
            final boolean    hideVia = hideViaVisibility;

            // Need to do it this way because mouse events aren't being forwarded/delivered.
            MouseEventHandler m = new MouseEventHandler ()
            {
                
                @Override
                public void mouseReleased (MouseEvent ev)
                {

                    c.setVisible (false);

                    if (hideVia)
                    {

                        c.setVisible (false);

                    } else
                    {

                        _this.removePopup (c);

                        List<Component> comps = _this.getTopLevelComponents ();

                        if (comps != null)
                        {
                        
                            for (int i = 0; i < comps.size (); i++)
                            {
    
                                comps.get (i).removeMouseListener (this);
    
                            }

                        }
                            
                    }

                }

            };

            List<Component> comps = this.getTopLevelComponents ();

            if (comps != null)
            {
            
                for (int i = 0; i < comps.size (); i++)
                {
    
                    comps.get (i).addMouseListener (m);
    
                }

            }
                
        }

    }

}
