package com.quollwriter.ui;

import java.awt.event.*;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import com.gentlyweb.utils.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.ui.components.*;


public class ContactSupport extends PopupWindow
{

    private JTextArea desc = null;

    public ContactSupport(AbstractProjectViewer v)
    {

        super (v);

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

        return "Please use the form below to contact Quoll Writer support.  If you wish to receive a response then please provide your name and an email address.";

    }

    public void init ()
    {

        super.init ();

        this.desc.grabFocus ();

    }

    public JComponent getContentPanel ()
    {

        Box b = new Box (BoxLayout.Y_AXIS);

        this.desc = UIUtils.createTextArea (10);
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

        b.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    if (_this.desc.getText ().trim ().equals (""))
                    {

                        UIUtils.showWarning (_this,
                                             "Please enter a description.");

                        return;

                    }

                    // Send the message.
                    Map details = new HashMap ();
                    details.put ("details",
                                 _this.desc.getText ());

                    try
                    {

                        Environment.sendMessageToSupport ("contact",
                                                          details);

                        UIUtils.showMessage (_this,
                                             "Thank you, your request has been logged with Quoll Writer support.");

                    } catch (Exception e)
                    {

                        Environment.logError ("Unable to send message to support",
                                              e);

                        UIUtils.showErrorMessage (_this,
                                                  "Unable to send message.");

                    }

                    _this.close ();

                }

            });

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
