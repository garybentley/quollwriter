package com.quollwriter.editors.ui;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

import java.awt.event.*;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.*;
import javax.swing.border.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.editors.messages.*;
import com.quollwriter.editors.*;

public class EditorRemovedMessageBox extends MessageBox<EditorRemovedMessage>
{

    private Box responseBox = null;

    public EditorRemovedMessageBox (EditorRemovedMessage mess,
                                    AbstractViewer       viewer)
    {

        super (mess,
               viewer);

    }

    public boolean isAutoDealtWith ()
    {

        return false;

    }

    public void doUpdate ()
    {

        if (this.message.isDealtWith ())
        {

            if (this.responseBox != null)
            {

                this.responseBox.setVisible (false);

            }

        }

    }

    public void doInit ()
    {

        final EditorRemovedMessageBox _this = this;

        JComponent h = UIUtils.createBoldSubHeader (String.format ("Removed %sas {a contact}",
                                                                   (this.message.isSentByMe () ? "" : "you ")),
                                                    Constants.DELETE_ICON_NAME);

        this.add (h);

        String text = String.format ("<b>%s</b> has removed you as a {contact}.  You will no longer receive any messages from them or be able to send them messages.",
                                     this.message.getEditor ().getShortName ());

        if (this.message.isSentByMe ())
        {

            text = String.format ("You removed <b>%s</b> as a {contact}.  You will no longer receive any messages from them or be able to send them messages.",
                                  this.message.getEditor ().getShortName ());

        }

        JTextPane desc = UIUtils.createHelpTextPane ("<html><i>" + text + "</i></html>",
                                                     this.viewer);

        this.add (Box.createVerticalStrut (5));

        Box descb = new Box (BoxLayout.Y_AXIS);
        descb.setBorder (UIUtils.createPadding (0, 5, 0, 0));
        descb.add (desc);
        descb.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.add (descb);

        desc.setBorder (null);
        desc.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                     desc.getPreferredSize ().height));

        if (!this.message.isDealtWith ())
        {

            // Show the response.
            this.responseBox = new Box (BoxLayout.Y_AXIS);

            this.add (this.responseBox);

            JTextPane rdesc = UIUtils.createHelpTextPane (String.format ("Clicking on the button below will remove <b>%s</b> from your list of current {contacts}.  You can still get access to them in the options menu of the {Contacts} sidebar via the <b>View the previous {contacts}</b> item.",
                                                                         this.message.getEditor ().getShortName ()),
                                                         this.viewer);

            this.responseBox.add (Box.createVerticalStrut (5));

            Box rdescb = new Box (BoxLayout.Y_AXIS);
            rdescb.setBorder (UIUtils.createPadding (0, 5, 0, 0));
            rdescb.add (rdesc);
            rdescb.setAlignmentX (Component.LEFT_ALIGNMENT);
            this.responseBox.add (rdescb);

            rdesc.setBorder (null);
            rdesc.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                          rdesc.getPreferredSize ().height));

            final EditorEditor ed = this.message.getEditor ();

            this.responseBox.setBorder (UIUtils.createPadding (5, 0, 0, 0));

            JButton ok = new JButton ("Ok, got it");

            ok.addActionListener (new ActionListener ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    try
                    {

                        // Unsubscribe.
                        EditorsEnvironment.getMessageHandler ().unsubscribeFromEditor (ed);

                        // For all projects, if they are a project editor then set them as previous.
                        EditorsEnvironment.removeEditorAsProjectEditorForAllProjects (ed);

                        // Uupdate the editor to be previous.
                        ed.setEditorStatus (EditorEditor.EditorStatus.previous);

                        EditorsEnvironment.updateEditor (ed);

                        _this.responseBox.setVisible (false);

                        _this.message.setDealtWith (true);

                        EditorsEnvironment.updateMessage (_this.message);

                        // Offer to remove any projects we are editing for them.
                        EditorsUIUtils.showDeleteProjectsForEditor (_this.viewer,
                                                                    _this.message.getEditor (),
                                                                    null);

                    } catch (Exception e) {

                        Environment.logError ("Unable to update editor: " +
                                              ed,
                                              e);

                        UIUtils.showErrorMessage (_this.viewer,
                                                  "Unable to update {contact}, please contact Quoll Writer support for assistance.");

                        return;

                    }

                }

            });

            JButton[] buts = new JButton[] { ok };

            JPanel bb = UIUtils.createButtonBar2 (buts,
                                                  Component.LEFT_ALIGNMENT);
            bb.setOpaque (false);
            bb.setAlignmentX (Component.LEFT_ALIGNMENT);
            bb.setBorder (UIUtils.createPadding (5, 0, 0, 0));

            this.responseBox.add (bb);

        }

    }

}
