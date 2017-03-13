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

public class QuestionWindow extends PopupWindow 
{
    
    private String iconType = null;
    private JButton[] buttons = null;
    private String headerTitle = null;
    private String windowTitle = null;
    private String message = null;
    private JTextPane error = null;
    private JComponent content = null;
    
    public QuestionWindow()
    {
        
        this.error = UIUtils.createHelpTextPane ("<p class='error'></p>",
                                                 null);
        this.error.setVisible (false);
        this.error.setBorder (new EmptyBorder (0,
                                               0,
                                               5,
                                               0));
        
    }

    public QuestionWindow (AbstractProjectViewer pv)
    {

        this ();

    }
        
    public static QuestionWindow create (AbstractProjectViewer viewer,
                                         String                title,
                                         String                icon,
                                         String                message,
                                         String                confirmButtonLabel,
                                         final ActionListener  onConfirm,
                                         final ActionListener  onCancel)
    {
        
        final QuestionWindow ti = new QuestionWindow (viewer);
        
        ti.setHeaderTitle (title);
        ti.setMessage (message);
        
        ti.setHeaderIconType (icon);
        
        ti.setWindowTitle (title);
                
        JButton confirm = null;
        JButton cancel = UIUtils.createButton ("Cancel",
                                               new ActionListener ()
        {
           
            @Override
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
        
        if (onConfirm != null)
        {
            
            ActionListener confirmAction = new ActionListener ()    
            {
                
                public void actionPerformed (ActionEvent ev)
                {
                                        
                    onConfirm.actionPerformed (ev);
                    
                    ti.close ();
                    
                }
                
            };
            
            confirm = UIUtils.createButton (confirmButtonLabel,
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
        
    }
    
    public void setMessage (String m)
    {
        
        this.message = m;
        
    }
    
    public void setError (String mess)
    {
        
        this.error.setText ("<p class='error'>" + mess + "</p>");
        
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
        
    public JComponent getContentPanel ()
    {
        
        this.content = new Box (BoxLayout.Y_AXIS);
        
        this.error.setAlignmentX (Component.LEFT_ALIGNMENT);        

        this.content.add (this.error);
                                        
        this.content.setBorder (new EmptyBorder (0,
                                                 5,
                                                 0,
                                                 0));
                                
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
