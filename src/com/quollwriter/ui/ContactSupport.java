package com.quollwriter.ui;

import java.awt.event.*;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import com.quollwriter.*;

import com.quollwriter.ui.components.*;


public class ContactSupport extends PopupWindow
{

    private JTextArea desc = null;
    private JLabel error = null;
    private ActionListener sendAction = null;
    
    public ContactSupport(AbstractProjectViewer v)
    {

        super (v);

        this.desc = UIUtils.createTextArea (10);
        
        this.error = UIUtils.createErrorLabel ("");
        this.error.setVisible (false);
        this.error.setBorder (new EmptyBorder (0,
                                               0,
                                               5,
                                               0));

        final ContactSupport _this = this;
                                               
        this.sendAction = new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                if (_this.desc.getText ().trim ().equals (""))
                {

                    _this.error.setText ("Please enter some text below.");
                    _this.error.setVisible (true);
                    
                    _this.resize ();
                
                    return;

                }

                // Send the message.
                Map details = new HashMap ();
                details.put ("details",
                             _this.desc.getText ());

                try
                {

                    Environment.sendMessageToSupport ("contact",
                                                      details,
                                                      new ActionAdapter ()
                    {

                        public void actionPerformed (ActionEvent ev)
                        {
                    
                            UIUtils.showMessage (_this,
                                                 "Message sent",
                                                 "Your request has been logged with Quoll Writer support.  If you provided an email address then you should get a response within 1-2 days.  If not feel then free to send the message again.");

                        }
                        
                    });                        
                                                 
                } catch (Exception e)
                {

                    Environment.logError ("Unable to send message to support",
                                          e);

                    UIUtils.showErrorMessage (_this,
                                              "Unable to send message.");

                }

                _this.close ();

            }

        };

        UIUtils.addDoActionOnReturnPressed (this.desc,
                                            this.sendAction);

        this.desc.addKeyListener (new KeyAdapter ()
        {
           
            public void keyPressed (KeyEvent ev)
            {
                
                if (!_this.desc.getText ().trim ().equals (""))
                {
                    
                    _this.error.setVisible (false);
                    _this.resize ();
                    
                }
                
            }
            
        });
        
    }

    public String getWindowTitle ()
    {

        return "Contact Support";

    }

    public String getHeaderTitle ()
    {

        return "Contact Support";

    }

    public String getHeaderIconType ()
    {

        return "email";

    }

    public String getHelpText ()
    {

        return "Please use the form below to contact Quoll Writer support.  If you wish to receive a response then please add your name and an email address.";

    }

    public void init ()
    {

        super.init ();

        this.desc.grabFocus ();

    }

    public JComponent getContentPanel ()
    {

        final ContactSupport _this = this;
    
        Box b = new Box (BoxLayout.Y_AXIS);

        b.add (this.error);
        
        this.desc.setCaretPosition (0);
        
        JScrollPane sp = new JScrollPane (this.desc);

        // sp.setBorder (null);
        sp.setAlignmentX (java.awt.Component.LEFT_ALIGNMENT);
        b.add (sp);

        return b;

    }

    public JButton[] getButtons ()
    {

        final ContactSupport _this = this;

        JButton b = new JButton ("Send");
        JButton c = new JButton ("Cancel");

        b.addActionListener (this.sendAction);

        c.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    _this.close ();

                }

            });

        JButton[] buts = new JButton[2];
        buts[0] = b;
        buts[1] = c;

        return buts;

    }

}
