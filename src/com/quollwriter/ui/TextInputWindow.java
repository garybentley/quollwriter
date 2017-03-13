package com.quollwriter.ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import com.gentlyweb.utils.*;

import com.jgoodies.forms.factories.*;

import com.quollwriter.*;

import com.quollwriter.ui.components.*;

public class TextInputWindow extends PopupWindow 
{
    
    protected JTextField text = null;
    private String iconType = null;
    private JButton[] buttons = null;
    private String headerTitle = null;
    private String windowTitle = null;
    private String message = null;
    private JLabel error = null;
    private JComponent content = null;
    
    public TextInputWindow()
    {

        this (null);
        
    }

    public TextInputWindow (AbstractViewer pv)
    {

        super (pv,
               Component.LEFT_ALIGNMENT);
    
        this.text = UIUtils.createTextField ();
        this.error = UIUtils.createErrorLabel ("Please enter a value.");
        
        this.error.setVisible (false);
        this.error.setBorder (UIUtils.createPadding (5, 0, 0, 0));

    }
    
    public JTextField getTextField ()
    {
        
        return this.text;
        
    }
    
    public static TextInputWindow create (AbstractViewer        viewer,
                                          String                title,
                                          String                icon,
                                          String                message,
                                          String                confirmButtonLabel,
                                          String                initValue,
                                          final ValueValidator<String> validator,
                                          final ActionListener onConfirm,
                                          final ActionListener onCancel)
    {

        return TextInputWindow.create (viewer,
                                       title,
                                       icon,
                                       message,
                                       confirmButtonLabel,
                                       null,
                                       initValue,
                                       validator,
                                       onConfirm,
                                       onCancel);
    
    }
    
    public static TextInputWindow create (AbstractViewer        viewer,
                                          String                title,
                                          String                icon,
                                          String                message,
                                          String                confirmButtonLabel,
                                          String                cancelButtonLabel,
                                          String                initValue,
                                          final ValueValidator<String> validator,
                                          final ActionListener onConfirm,
                                          final ActionListener onCancel)
    {
        
        final TextInputWindow ti = new TextInputWindow (viewer);
        
        ti.addWindowListener (new WindowAdapter ()
        {

            @Override
            public void windowClosing (WindowEvent ev)
            {

                if (onCancel != null)
                {
                    
                    onCancel.actionPerformed (new ActionEvent (ti,
                                                               0,
                                                               "cancel"));
                                    
                }
            
            }

        });
                
        if (initValue != null)
        {
            
            ti.setText (initValue);
            
        }
        
        ti.setHeaderTitle (title);
        ti.setMessage (message);
        
        ti.setHeaderIconType (icon);
        
        ti.setWindowTitle (title);
                
        JButton confirm = null;
        JButton cancel = UIUtils.createButton ((cancelButtonLabel != null ? cancelButtonLabel : Constants.CANCEL_BUTTON_LABEL_ID),
                                               new ActionListener ()
        {
           
            @Override
            public void actionPerformed (ActionEvent ev)
            {
                                
                ti.close ();
                
            }
            
        });
        
        if (onConfirm != null)
        {
            
            ActionListener confirmAction = new ActionListener ()
            {
                
                @Override
                public void actionPerformed (ActionEvent ev)
                {
                    
                    if (validator != null)
                    {
                        
                        String mess = validator.isValid (ti.getText ());
                        
                        if (mess != null)
                        {
                            
                            ti.setError (mess);
                            
                            ti.showError (true);

                            return;
                            
                        }
                                                
                    }
                    
                    onConfirm.actionPerformed (new ActionEvent (ti,
                                                                0,
                                                                ti.getText ()));
                    
                    ti.close ();
                    
                }
                
            };
            
            confirm = UIUtils.createButton (confirmButtonLabel,
                                            confirmAction);
            
            UIUtils.addDoActionOnReturnPressed (ti.getTextField (),
                                                confirmAction);
            
        }
                            
        JButton[] buts = null;
        
        if (confirm != null)
        {
            
            buts = new JButton[] { confirm, cancel };
            
        } else {
            
            buts = new JButton[] { cancel };
            
        }
        
        ti.setButtons (buts);
        
        ti.init ();
        
        return ti;
        
    }

    public void setVisible (boolean v)
    {
        
        super.setVisible (v);
        
        this.text.grabFocus ();
        
    }
    
    public void setMessage (String m)
    {
        
        this.message = m;
        
    }
    
    public void setError (String mess)
    {
        
        this.error.setText (Environment.replaceObjectNames (mess));
        
        this.error.setMaximumSize (this.error.getPreferredSize ());
        
    }
    
    public void showError (boolean v)
    {
        
        this.error.setVisible (v);
        
        this.resize ();
        
    }
    
    public String getMessage ()
    {
        
        return this.message;
        
    }
    
    public String getHelpText ()
    {
        
        return this.getMessage ();
        
    }
    
    public JButton[] getButtons ()
    {
        
        return this.buttons;
        
    }
    
    public void setButtons (JButton[] buts)
    {
        
        this.buttons = buts;
        
    }
    
    public void setText (String v)
    {
        
        if (v == null)
        {
            
            v = "";
            
        }
        
        this.text.setText (v);
        
    }
    
    public String getText ()
    {
        
        return this.text.getText ();
        
    }
    
    public JComponent getContentPanel ()
    {
        
        this.content = new Box (BoxLayout.Y_AXIS);
        
        this.text.setMinimumSize (new Dimension (300,
                                                 this.text.getPreferredSize ().height));
        this.text.setPreferredSize (new Dimension (300,
                                                   this.text.getPreferredSize ().height));
        this.text.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                 this.text.getPreferredSize ().height));
        
        this.text.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.error.setAlignmentX (Component.LEFT_ALIGNMENT);        

        this.content.add (this.error);

        this.content.add (Box.createVerticalStrut (5));
        this.content.add (this.text);
                                        
        this.content.setBorder (UIUtils.createPadding (0, 0, 0, 0));
                                
        return this.content;
        
    }
    
    public String getWindowTitle ()
    {

        return this.windowTitle;

    }
    
    public void setWindowTitle (String t)
    {
        
        this.windowTitle = t;
        
    }

    public void setHeaderTitle (String t)
    {
        
        this.headerTitle = t;
        
    }
    
    public String getHeaderTitle ()
    {

        return this.headerTitle;

    }

    public void setHeaderIconType (String t)
    {
        
        this.iconType = t;
        
    }
    
    public String getHeaderIconType ()
    {

        return this.iconType;

    }
    
    public void init ()
    {

        super.init ();

    }

}
