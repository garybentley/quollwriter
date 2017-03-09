package com.quollwriter.ui.forms;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.*;
import java.awt.Cursor;
import java.awt.Color;

import java.util.*;

import javax.swing.*;

import com.toedter.calendar.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;

public class Form extends Box
{

    public enum Layout
    {
        
        stacked ("stacked"),
        columns ("columns");
        
        String layout = null;
        
        Layout (String l)
        {
            
            this.layout = l;
            
        }
        
    };

    public enum Button
    {
        
        save ("save"),
        cancel ("cancel");
        
        String button = null;
        
        Button (String b)
        {
            
            this.button = b;
            
        }
        
    };

    private JLabel  error = null;
    private ActionListener onError = null;

    public Form (final Layout                      layout,
                 final Set<FormItem>               items,
                 final Map<Button, ActionListener> buttons)
    {
        
        super (BoxLayout.Y_AXIS);
        
        final Form _this = this;
        
        this.error = UIUtils.createErrorLabel ("");
        
        this.error.setVisible (false);
        
        this.error.setBorder (UIUtils.createPadding (0, 0, 5, 5));
        
        this.add (this.error);
        
        if (layout == Layout.columns)
        {
            
            this.createColumnLayout (items,
                                     buttons);
            
        }

        if (layout == Layout.stacked)
        {
            
            this.createStackedLayout (items,
                                      buttons);
            
        }
        
    }
    
    public boolean checkForm ()
    {
        
        this.error.setVisible (false);
        
        return true;
        
    }
        
    public void setOnError (ActionListener l)
    {
        
        this.onError = l;
        
    }
    
    private void createStackedLayout (Set<FormItem>               items,
                                      Map<Button, ActionListener> buttons)
    {

        String cols = "10px, fill:100px:grow";

        StringBuilder rows = new StringBuilder ();
        
        Iterator<FormItem> iter = items.iterator ();
        
        while (iter.hasNext ())
        {
        
            FormItem it = iter.next ();

            JComponent c = it.getComponent ();

            if (it.getLabel () != null)
            {
                
                rows.append ("p");
                
            }
            
            if (c != null)
            {
                
                if (it.getLabel () != null)
                {
                    
                    rows.append (", 6px, ");
                    
                }
            
                rows.append ("p");

            }
            
            if (iter.hasNext ())
            {
                
                rows.append (", 10px, ");
                
            }

        }

        final Form _this = this;
        
        FormLayout   fl = new FormLayout (cols,
                                          rows.toString () + ", 10px, fill:p:grow");
        PanelBuilder b = new PanelBuilder (fl);
        //b.border (Borders.DIALOG);
        
        CellConstraints cc = new CellConstraints ();

        iter = items.iterator ();

        int r = 1;

        while (iter.hasNext ())
        {

            FormItem it = iter.next ();
            Object l = it.getLabel ();
            
            if (l != null)
            {
            
                if (l instanceof String)
                {
    
                    String label = l.toString ();
    
                    if (label != null)
                    {
    
                        b.addLabel (Environment.replaceObjectNames (label),
                                    cc.xyw (1,
                                             r,
                                             2));
    
                        r += 2;
    
                    }

                } else {
            
                    if (l instanceof JComponent)
                    {
                
                        l = this.wrap ((JComponent) l);
                
                    }
                            
                    if ((l instanceof JTextArea) ||
                        (l instanceof JTextPane) ||
                        (l instanceof JList))
                    {
    
                        l = new JScrollPane ((Component) l);
                        ((JScrollPane) l).getViewport ().setOpaque (false);
                        
                    }

                    b.add ((Component) l,
                           cc.xyw (1,
                                    r,
                                    2));
                    
                    r += 2;
                    
                }
    
            }
                        
            JComponent c = it.getComponent ();
        
            if (c != null)
            {
                
                c.setOpaque (false);
                                
                c = this.wrap (c);
                                
                if ((c instanceof JTextArea) ||
                    (c instanceof JTextPane) ||
                    (c instanceof JList))
                {

                    c = new JScrollPane (c);
                    ((JScrollPane) c).getViewport ().setOpaque (false);
                    ((JScrollPane) c).setOpaque (false);
                    
                }
                
                b.add (c,
                       cc.xy (2,
                              r));
                
                r += 2;
                
            }

        }

        List<JButton> bs = new ArrayList ();

        if (buttons != null)
        { 
        
            if (buttons.keySet ().contains (Button.save))
            {
    
                JButton but = UIUtils.createButton (Constants.SAVE_BUTTON_LABEL_ID,
                                                    null);
    
                but.addActionListener (new ActionListener ()
                {
    
                    @Override
                    public void actionPerformed (ActionEvent ev)
                    {
    
                        _this.error.setVisible (false);
                                        
                        ActionListener l = buttons.get (Button.save);
                    
                        if (l != null)
                        {
                            
                            l.actionPerformed (new ActionEvent (_this, 0, "save"));
                            
                        }
    
                    }
    
                });
    
                bs.add (but);
    
            }

            if (buttons.keySet ().contains (Button.cancel))
            {
    
                JButton but = UIUtils.createButton (Constants.CANCEL_BUTTON_LABEL_ID,
                                                    null);
    
                but.addActionListener (new ActionListener ()
                {
    
                    @Override
                    public void actionPerformed (ActionEvent ev)
                    {
    
                        _this.error.setVisible (false);
                                        
                        ActionListener l = buttons.get (Button.cancel);
                    
                        if (l != null)
                        {
                            
                            l.actionPerformed (new ActionEvent (_this, 0, "cancel"));
                            
                        }
    
                    }
    
                });
    
                bs.add (but);
    
            }

        }
            
        if (bs.size () > 0)
        {
        
            b.add (ButtonBarBuilder.create ().addButton ((JButton[]) bs.toArray (new JButton[bs.size ()])).build (),
                   cc.xy (2,
                          r));

        }
        
        JPanel p = b.getPanel ();
        p.setOpaque (false);
        p.setAlignmentX (Component.LEFT_ALIGNMENT);

        this.add (p);
    
    }
    
    private void createColumnLayout (Set<FormItem>               items,
                                     Map<Button, ActionListener> buttons)
    {
        
        String cols = "right:pref, 6px, fill:100px:grow";

        StringBuilder rows = new StringBuilder ();
        
        Iterator<FormItem> iter = items.iterator ();
        
        while (iter.hasNext ())
        {
        
/*
            FormItem fi = (FormItem) items.get (i);

            if ((fi.component instanceof JComboBox)
                ||
                (fi.component instanceof JSpinner)
                ||
                (fi.component instanceof com.toedter.calendar.JDateChooser)
               )
            {

                fi.component.setMaximumSize (fi.component.getPreferredSize ());

                Box tb = new Box (BoxLayout.X_AXIS);
                tb.add (fi.component);
                tb.add (Box.createHorizontalGlue ());

                fi.component = tb;

            }
*/

            FormItem it = iter.next ();

            JComponent c = it.getComponent ();
            
            if ((c instanceof JTextArea)
                ||
                (c instanceof TextArea)
               )
            {

                rows.append ("top:p");

            } else {
                
                rows.append ("p");                
                
            }

            if (iter.hasNext ())
            {

                rows.append (", 6px, ");
  
            }

        }

        final Form _this = this;
        
        FormLayout   fl = new FormLayout (cols,
                                          rows.toString () + ", 10px, fill:p:grow");
        PanelBuilder b = new PanelBuilder (fl);
        //b.border (Borders.DIALOG);
        
        CellConstraints cc = new CellConstraints ();

        iter = items.iterator ();

        int r = 1;

        while (iter.hasNext ())
        {

            FormItem it = iter.next ();
            Object l = it.getLabel ();
            
            JComponent c = it.getComponent ();
        
            if (c != null)
            {
                
                c.setOpaque (false);
                
                if (l instanceof String)
                {

                    String label = l.toString ();

                    if (label != null)
                    {

                        b.addLabel (Environment.replaceObjectNames (label),
                                    cc.xy (1,
                                           r));

                    }

                } else
                {
                
                    if (l instanceof JComponent)
                    {
                
                        l = this.wrap ((JComponent) l);
                
                    }
                                
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
                
                c = this.wrap (c);
                                
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
                
            } else {
            
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

            }

            r += 2;

        }

        List<JButton> bs = new ArrayList ();

        if (buttons != null)
        { 
        
            if (buttons.keySet ().contains (Button.save))
            {
    
                JButton but = UIUtils.createButton (Constants.SAVE_BUTTON_LABEL_ID,
                                                    null);
    
                but.addActionListener (new ActionListener ()
                {
    
                    @Override
                    public void actionPerformed (ActionEvent ev)
                    {
    
                        _this.error.setVisible (false);
                                        
                        ActionListener l = buttons.get (Button.save);
                    
                        if (l != null)
                        {
                            
                            l.actionPerformed (new ActionEvent (_this, 0, "save"));
                            
                        }
    
                    }
    
                });
    
                bs.add (but);
    
            }

            if (buttons.keySet ().contains (Button.cancel))
            {
    
                JButton but = UIUtils.createButton (Constants.CANCEL_BUTTON_LABEL_ID,
                                                    null);
    
                but.addActionListener (new ActionListener ()
                {
    
                    @Override
                    public void actionPerformed (ActionEvent ev)
                    {
    
                        _this.error.setVisible (false);
                                        
                        ActionListener l = buttons.get (Button.cancel);
                    
                        if (l != null)
                        {
                            
                            l.actionPerformed (new ActionEvent (_this, 0, "cancel"));
                            
                        }
    
                    }
    
                });
    
                bs.add (but);
    
            }

        }
            
        if (bs.size () > 0)
        {
        
            b.add (ButtonBarBuilder.create ().addButton ((JButton[]) bs.toArray (new JButton[bs.size ()])).build (),//ButtonBarFactory.buildLeftAlignedBar ((JButton[]) bs.toArray (new JButton[bs.size ()])),
                   cc.xy (3,
                          r));

        }
        
        JPanel p = b.getPanel ();
        p.setOpaque (false);
        p.setAlignmentX (Component.LEFT_ALIGNMENT);

        this.add (p);
        
    }

    private JComponent wrap (JComponent component)
    {
   
        if ((component instanceof JDateChooser)
            ||
            (component instanceof JComboBox)
            ||
            (component instanceof JSpinner)
            ||
            (component instanceof JLabel)
            ||
            (component instanceof JList)
            ||
            (component instanceof JScrollPane)
           )
        {
            
            component.setMaximumSize (component.getPreferredSize ());
        
            Box tb = new Box (BoxLayout.X_AXIS);
            tb.add (component);
            tb.add (Box.createHorizontalGlue ());

            return tb;
            
        }
                
        return component;
    
    }
    
    public void hideError ()
    {
        
        this.error.setVisible (false);
        
    }
    
    public void showErrors (Set<String> messages)
    {
        
        if ((messages == null)
            ||
            (messages.size () == 0)
           )
        {
            
            this.error.setVisible (false);
            
            return;
            
        }
        
        if (messages.size () == 1)
        {
            
            this.showError (messages.iterator ().next ());
            
            return;
            
        }
        
        StringBuilder b = new StringBuilder ();

        for (String m : messages)
        {

            b.append (String.format ("<li>%s</li>",
                                     m));

        }
        
        this.showError ("Please correct the following errors:<ul>" + b.toString () + "</ul>");
        
    }
    
    public void showError (String message)
    {
        
        this.error.setText (message);
        
        this.error.setVisible (true);
                
        if (this.onError != null)
        {
            
            this.onError.actionPerformed (new ActionEvent (this, 1, message));
                
        }

    }
    
}
