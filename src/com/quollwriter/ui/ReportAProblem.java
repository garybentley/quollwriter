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


public class ReportAProblem extends PopupWindow
{

    private JTextArea desc = null;
    private JCheckBox sendLogFiles = null;

    public ReportAProblem(AbstractProjectViewer v)
    {

        super (v);

    }

    public String getWindowTitle ()
    {

        return "Report a Bug/Problem";

    }

    public String getHeaderTitle ()
    {

        return "Report a Bug/Problem";

    }

    public String getHeaderIconType ()
    {

        return "bug";

    }

    public String getHelpText ()
    {

        return "Please provide detail about the bug/problem in the box below, where possible be specific, the more information the better.  Use the <b>Send</b> button to send the information back to the Quoll Writer support.  The operating system you are using and the Java version will also be sent.  No personal information will be sent.<br /><br />If you wish to receive a response about the problem please provide your name and an email address.  Thank you for taking the time to report the problem!";

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

        b.add (Box.createVerticalStrut (10));

        this.sendLogFiles = new JCheckBox ("Also send the relevant log files");

        this.sendLogFiles.setSelected (true);
        this.sendLogFiles.setOpaque (false);
        this.sendLogFiles.setAlignmentX (java.awt.Component.LEFT_ALIGNMENT);

        b.add (this.sendLogFiles);

        return b;

    }

    public JButton[] getButtons ()
    {

        final ReportAProblem _this = this;

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

                        // Get the log files?
                        if (_this.sendLogFiles.isSelected ())
                        {

                            details.put ("errorLog",
                                         IOUtils.getFile (Environment.getErrorLogFile ()));
                            details.put ("generalLog",
                                         IOUtils.getFile (Environment.getGeneralLogFile ()));

                        }

                        Environment.sendMessageToSupport ("bug",
                                                          details);

                        UIUtils.showMessage (_this,
                                             "Thank you, the problem has been logged with Quoll Writer support.");

                        _this.projectViewer.fireProjectEvent (ProjectEvent.BUG_REPORT,
                                                              ProjectEvent.SUBMIT);

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
