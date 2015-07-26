package com.quollwriter.ui.components;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.*;
import java.awt.Cursor;
import java.awt.Color;

import java.util.*;

import javax.swing.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;


public class Form extends QPopup
{

    public static int SAVE_BUTTON = 1;
    public static int CANCEL_BUTTON = 2;

    public static int SAVE_CANCEL_BUTTONS = Form.SAVE_BUTTON | Form.CANCEL_BUTTON;

    public static String SAVE_BUTTON_LABEL = "Save";
    public static String CANCEL_BUTTON_LABEL = "Cancel";

    private JLabel  error = null;
    private List    formListeners = new ArrayList ();
    private boolean hideOnCancel = true;

    public Form(String         title,
                Icon           icon,
                List<FormItem> items,
                Component      parent,
                int            buttons)
    {

        this (title,
              icon,
              items,
              parent,
              buttons,
              false);

    }

    public Form(String         title,
                Icon           icon,
                List<FormItem> items,
                Component      parent,
                int            buttons,
                boolean        addHideControl)
    {

        super (title,
               icon,
               null);

        if (addHideControl)
        {

            JButton bt = new JButton (Environment.getIcon ("cancel",
                                                           Constants.ICON_MENU));
            bt.setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));
            bt.setOpaque (false);
    
            bt.addActionListener (new ActionAdapter ()
            {
               
                public void actionPerformed (ActionEvent ev)
                {
                    
                    Form.this.setVisible (false);

                    Form.this.fireFormEvent (FormEvent.CANCEL,
                                             FormEvent.CANCEL_ACTION_NAME);

                    if (Form.this.hideOnCancel)
                    {

                        Form.this.hideForm ();

                    }                    
                    
                }
                
            });
            
            JToolBar tb = new JToolBar ();
            tb.setOpaque (false);
            tb.setFloatable (false);
            tb.setBackground (new Color (0,
                                         0,
                                         0,
                                         0));

            tb.add (bt);
            
            this.getHeader ().setControls (tb);
/*
            final ImagePanel ip = new ImagePanel (Environment.getIcon ("cancel",
                                                                       false).getImage (),
                                                  Environment.getTransparentImage ());
            com.quollwriter.ui.UIUtils.setAsButton (ip);

            this.getHeader ().setControls (ip);

            ip.addMouseListener (new MouseAdapter ()
                {

                    public void mouseReleased (MouseEvent ev)
                    {

                        Form.this.setVisible (false);

                        Form.this.fireFormEvent (FormEvent.CANCEL,
                                                 FormEvent.CANCEL_ACTION_NAME);

                        if (Form.this.hideOnCancel)
                        {

                            Form.this.hideForm ();

                        }

                    }

                });
*/
        }

        this.error = com.quollwriter.ui.UIUtils.createErrorLabel ("");
        
        this.error.setVisible (false);
        
        this.error.setBorder (com.quollwriter.ui.UIUtils.createPadding (5, 10, 0, 5));
        
        String cols = "right:pref, 6px, fill:100px:grow";

        StringBuilder rows = new StringBuilder ();
        
        for (int i = 0; i < items.size (); i++)
        {

            FormItem fi = (FormItem) items.get (i);

            if (fi.component instanceof JComboBox)
            {

                fi.component.setMaximumSize (fi.component.getPreferredSize ());

                Box tb = new Box (BoxLayout.X_AXIS);
                tb.add (fi.component);
                tb.add (Box.createHorizontalGlue ());

                fi.component = tb;

            }
            
            if (fi.component instanceof JTextArea)
            {

                rows.append ("top:p:grow");

            } else {
                
                rows.append ("p");                
                
            }

            if (i < (items.size () - 1))
            {

                rows.append (", 6px, ");

            }

        }

        final Form _this = this;
        
        FormLayout   fl = new FormLayout (cols,
                                          rows.toString () + ", 6px, fill:p:grow");
        PanelBuilder b = new PanelBuilder (fl);
        b.border (Borders.DIALOG);
        
        CellConstraints cc = new CellConstraints ();

        Iterator<FormItem> iter = items.iterator ();

        int r = 1;

        while (iter.hasNext ())
        {

            FormItem fi = iter.next ();

            Object l = fi.label;

            Component c = fi.component;

            if (c instanceof JComponent)
            {
            
                ((JComponent) c).setOpaque (false);
            
            }
            
            if (c == null)
            {

                if (l instanceof String)
                {

                    String label = l.toString ();

                    if (label != null)
                    {

                        b.addLabel (label,
                                    cc.xyw (1,
                                            r,
                                            3));

                    }

                } else
                {

                    b.add ((Component) l,
                           cc.xyw (1,
                                   r,
                                   3));

                }

            } else
            {

                if (l instanceof String)
                {

                    String label = l.toString ();

                    if (label != null)
                    {

                        b.addLabel (label,
                                    cc.xy (1,
                                           r));

                    }

                } else
                {

                    if ((l instanceof JTextArea) ||
                        (l instanceof JTextPane) ||
                        (l instanceof JList))
                    {

                        l = new JScrollPane ((Component) l);
                        ((JScrollPane) l).getViewport ().setOpaque (false);
                        
                    }

                    if (l == null)
                    {

                        b.addLabel ("",
                                    cc.xy (1,
                                           r));

                    } else
                    {

                        b.add ((Component) l,
                               cc.xy (1,
                                      r));

                    }

                }
                
                if ((c instanceof JTextArea) ||
                    (c instanceof JTextPane) ||
                    (c instanceof JList))
                {

                    c = new JScrollPane (c);
                    ((JScrollPane) c).getViewport ().setOpaque (false);
                    ((JScrollPane) c).setOpaque (false);
                    
                }
                

                b.add (c,
                       cc.xy (3,
                              r));

            }

            r += 2;

        }

        List bs = new ArrayList ();

        if ((buttons | Form.SAVE_BUTTON) > 0)
        {

            JButton but = new JButton (Form.SAVE_BUTTON_LABEL);

            but.addActionListener (new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        _this.error.setVisible (false);
                        
                        _this.resize ();
                    
                        Form.this.fireFormEvent (FormEvent.SAVE,
                                                 FormEvent.SAVE_ACTION_NAME);

                    }

                });

            bs.add (but);

        }

        if ((buttons | Form.CANCEL_BUTTON) > 0)
        {

            JButton but = new JButton (Form.CANCEL_BUTTON_LABEL);

            but.addActionListener (new ActionAdapter ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        Form.this.setVisible (false);

                        Form.this.fireFormEvent (FormEvent.CANCEL,
                                                 FormEvent.CANCEL_ACTION_NAME);

                        if (Form.this.hideOnCancel)
                        {

                            Form.this.hideForm ();

                        }

                    }

                });

            bs.add (but);

        }

        b.add (ButtonBarBuilder.create ().addButton ((JButton[]) bs.toArray (new JButton[bs.size ()])).build (),//ButtonBarFactory.buildLeftAlignedBar ((JButton[]) bs.toArray (new JButton[bs.size ()])),
               cc.xy (3,
                      r));

        JPanel p = b.getPanel ();
        //p.setBackground (UIManager.getColor ("Panel.background"));
        p.setBackground (com.quollwriter.ui.UIUtils.getComponentColor ());
        p.setAlignmentX (Component.LEFT_ALIGNMENT);
        
        Box wb = new Box (BoxLayout.Y_AXIS);
        wb.add (this.error);
        wb.add (p);
        
        this.setContent (wb);

        if (parent != null)
        {

            this.setDraggable (parent);

        }

        p.setVisible (true);

    }

    public void fireFormEvent (int    type,
                               String action)
    {

        FormEvent ev = new FormEvent (this,
                                      type,
                                      action);

        for (int i = 0; i < this.formListeners.size (); i++)
        {

            FormListener f = (FormListener) this.formListeners.get (i);

            f.actionPerformed (ev);

        }

    }

    public void showError (String message)
    {
        
        this.error.setText (message);
        
        this.error.setVisible (true);
        
        this.resize ();
        
    }
    
    public void addFormListener (FormListener f)
    {

        this.formListeners.add (f);

    }

    public void hideForm ()
    {

        // this.setCollapsed (true);

        Container parent = (Container) this.getParent ();

        if (parent != null)
        {

            parent.remove (this);
            parent.repaint ();

        }

    }

    public boolean isHideOnCancel ()
    {

        return this.hideOnCancel;

    }

    public void setHideOnCancel (boolean c)
    {

        this.hideOnCancel = c;

    }

}
