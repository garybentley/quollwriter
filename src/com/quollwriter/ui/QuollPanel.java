package com.quollwriter.ui;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.text.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

public abstract class QuollPanel extends JRootPane /*Box*/ implements Stateful,
                                                                      MouseListener,
                                                                      PopupsSupported
{

    public static final int UNSAVED_CHANGES_ACTION_EVENT = 0;

    public static final int SAVED = 1;

    public static final String HAS_CHANGES_COMMAND = "hasChanges";
    public static final String NO_CHANGES_COMMAND = "noChanges";

    protected AbstractProjectViewer projectViewer = null;

    protected Box                                   content = null;
    protected NamedObject                           obj = null;
    private java.util.List                          actionListeners = new ArrayList ();
    private java.util.List<PropertyChangedListener> propertyChangedListeners = new ArrayList ();
    private boolean                                 hasUnsavedChanges = false;
    private JToolBar                                toolBar = null;

    public QuollPanel(AbstractProjectViewer pv,
                      NamedObject           obj)
    {

        this (pv,
              obj,
              true);

    }

    public QuollPanel(AbstractProjectViewer pv,
                      NamedObject           obj,
                      boolean               addScrollPane)
    {

        // super (BoxLayout.PAGE_AXIS);

        this.projectViewer = pv;
        this.obj = obj;

        this.setOpaque (false);
        this.setBackground (null);
        
        this.content = new Box (BoxLayout.Y_AXIS);
        this.content.setBackground (UIUtils.getComponentColor ());
        
        this.getContentPane ().setBackground (UIUtils.getComponentColor ());
        
        this.getContentPane ().add (this.content);
                
    }

    public abstract void close ();

    public abstract void init ()
                        throws GeneralException;

    public abstract void getState (Map<String, Object> s);

    public abstract void setState (Map<String, String> s,
                                   boolean             hasFocus);

    public abstract boolean saveUnsavedChanges ()
                                         throws Exception;

    public abstract String getIconType ();

    public abstract String getTitle ();
    
    public abstract void fillToolBar (JToolBar toolBar,
                                      boolean  fullScreen);

    public abstract void fillPopupMenu (MouseEvent ev,
                                        JPopupMenu popup);

    public abstract List<Component> getTopLevelComponents ();

    public abstract <T extends NamedObject> void refresh (T n);

    public void remove (Component c)
    {

        this.content.remove (c);

    }

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

    public java.util.List<PropertyChangedListener> getObjectPropertyChangedListeners ()
    {

        return this.propertyChangedListeners;

    }

    public void removeObjectPropertyChangedListener (PropertyChangedListener l)
    {

        this.propertyChangedListeners.remove (l);

    }

    public void addObjectPropertyChangedListener (PropertyChangedListener l,
                                                  Map                     events)
    {

        this.propertyChangedListeners.add (l);

        this.obj.addPropertyChangedListener (l,
                                             events);

    }

    public NamedObject getForObject ()
    {

        return this.obj;

    }

    public void saveObject ()
                     throws Exception
    {

        this.projectViewer.saveObject (this.obj,
                                       true);

        this.setHasUnsavedChanges (false);

        // Fire an event to interested parties.
        this.fireActionEvent (new ActionEvent (this,
                                               QuollPanel.SAVED,

                                               // QuollEditorPanel.CHAPTER_SAVED,
                                               "chapterSaved"));

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

    public void mouseClicked (MouseEvent ev)
    {
    }

    public void mouseEntered (MouseEvent ev)
    {
    }

    public void mouseExited (MouseEvent ev)
    {
    }

    public void mousePressed (MouseEvent ev)
    {

        this.mouseReleased (ev);

    }

    public void mouseReleased (MouseEvent ev)
    {

        if (!ev.isPopupTrigger ())
        {

            return;

        }

        final JPopupMenu popup = new JPopupMenu ();

        this.fillPopupMenu (ev,
                            popup);

        popup.pack ();

        popup.show ((Component) ev.getSource (),
                    ev.getPoint ().x,
                    ev.getPoint ().y);

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

    public String getPanelId ()
    {

        return this.obj.getObjectReference ().asString ();

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

    public boolean hasUnsavedChanges ()
    {

        return this.hasUnsavedChanges;

    }

    protected void setHasUnsavedChanges (boolean hasChanges)
    {

        this.hasUnsavedChanges = hasChanges;

        this.fireActionEvent (new ActionEvent (this,
                                               QuollPanel.UNSAVED_CHANGES_ACTION_EVENT,
                                               (hasChanges ? QuollPanel.HAS_CHANGES_COMMAND : QuollPanel.NO_CHANGES_COMMAND)));

    }

    public void removePopup (Component c)
    {

        this.getLayeredPane ().remove (c);

        this.getLayeredPane ().validate ();

        this.getLayeredPane ().repaint ();

        // this.content.remove (c);

        // this.content.repaint ();

    }

    public void addPopup (Component c,
                          boolean   hideOnClick)
    {

        this.addPopup (c,
                       hideOnClick,
                       false);

    }

    public void showPopupAt (Component popup,
                             Component showAt)
    {

        Point po = SwingUtilities.convertPoint (showAt,
                                                0,
                                                0,
                                                this.getContentPane ());

        this.showPopupAt (popup,
                          po);


    }

    public void showPopupAt (Component c,
                             Point     p)
    {

        Insets ins = this.getInsets ();

        if ((c.getParent () == null)
            &&
            (c.getParent () != this.getLayeredPane ())
           )
        {

            this.addPopup (c,
                           true,
                           false);

        }

        Dimension cp = c.getPreferredSize ();

        if ((p.y + cp.height) > (this.getBounds ().height - ins.top - ins.bottom))
        {

            p = new Point (p.x,
                           p.y);

            p.y = p.y - cp.height;

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
                             String    where)
    {

        this.showPopupAt (c,
                          new Rectangle (p,
                                         c.getPreferredSize ()),
                          where);

    }

    public void showPopupAt (Component c,
                             Rectangle r,
                             String    where)
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
                                     y));

/*
        c.setBounds (x,
                     y,
                     c.getPreferredSize ().width,
                     c.getPreferredSize ().height);

        c.setVisible (true);

        this.validate ();
        this.repaint ();
*/
    }

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

/*
        this.content.getLayeredPane ().add (c,
                             JLayeredPane.POPUP_LAYER);

        this.content.getLayeredPane ().moveToFront (c);
*/
        if (hideOnClick)
        {

            final QuollPanel _this = this;
            final boolean    hideVia = hideViaVisibility;

            // Need to do it this way because mouse events aren't being forwarded/delivered.
            MouseAdapter m = new MouseAdapter ()
            {

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

                        for (int i = 0; i < comps.size (); i++)
                        {

                            comps.get (i).removeMouseListener (this);

                        }

                    }

                }

            };

            List<Component> comps = this.getTopLevelComponents ();

            for (int i = 0; i < comps.size (); i++)
            {

                comps.get (i).addMouseListener (m);

            }

        }

    }

    public AbstractProjectViewer getProjectViewer ()
    {

        return this.projectViewer;

    }

}
