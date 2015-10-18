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

public class PasswordInputWindow extends TextInputWindow 
{
    
    private String iconType = null;
    private JButton[] buttons = null;
    private String headerTitle = null;
    private String windowTitle = null;
    private String message = null;
    
    public PasswordInputWindow()
    {

        this.text = UIUtils.createPasswordField ();

    }

    public static PasswordInputWindow create (String title,
                                              String icon,
                                              String message,
                                              String confirmButtonLabel,
                                              final ValueValidator<String> validator,
                                              final ActionListener onConfirm,
                                              final ActionListener onCancel)
    {
        
        final PasswordInputWindow ti = new PasswordInputWindow ();

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
        
        ti.setHeaderTitle (title);
        ti.setMessage (message);
        
        ti.setHeaderIconType (icon);
        
        ti.setWindowTitle (title);
                
        JButton confirm = null;
        JButton cancel = UIUtils.createButton ("Cancel",
                                               null);
        
        if (onConfirm != null)
        {
            
            confirm = UIUtils.createButton (confirmButtonLabel,
                                            null);

            ActionListener confirmAction = new ActionAdapter ()
            {
                
                public void actionPerformed (ActionEvent ev)
                {
                    
                    if (validator != null)
                    {

                        String mess = validator.isValid (ti.getText ());
                        
                        if (mess != null)
                        {
                            
                            ti.setError (mess);
                            
                            ti.showError (true);

                            ti.resize ();
                            
                            return;
                            
                        }
                        
                    }
                    
                    onConfirm.actionPerformed (new ActionEvent (ti,
                                                                0,
                                                                ti.getText ()));
                    
                    ti.close ();
                    
                }
                
            };

            UIUtils.addDoActionOnReturnPressed (ti.getTextField (),
                                                confirmAction);
            confirm.addActionListener (confirmAction);
            
        }
        
        cancel.addActionListener (new ActionAdapter ()
        {
           
            public void actionPerformed (ActionEvent ev)
            {
                
                if (onCancel != null)
                {
                    
                    onCancel.actionPerformed (new ActionEvent (ti,
                                                               0,
                                                               "cancel"));
                                    
                }
                
                ti.close ();
                
            }
            
        });
                    
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

}
