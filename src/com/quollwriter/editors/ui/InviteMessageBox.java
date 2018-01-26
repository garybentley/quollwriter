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

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public class InviteMessageBox extends MessageBox<InviteMessage>
{

    private Box responseBox = null;

    public InviteMessageBox (InviteMessage  mess,
                             AbstractViewer viewer)
    {

        super (mess,
               viewer);

    }

    @Override
    public boolean isShowAttentionBorder ()
    {

        return false;

    }

    @Override
    public boolean isAutoDealtWith ()
    {

        return false;

    }

    public void doUpdate ()
    {

        this.responseBox.setVisible (!this.message.isDealtWith ());

    }

    public void doInit ()
    {

        final InviteMessageBox _this = this;

        String title = getUIString (editors,messages,invite,sent,LanguageStrings.title);
        //"Sent invitation";

        if (!this.message.isSentByMe ())
        {

            title = getUIString (editors,messages,invite,received,LanguageStrings.title);
            //"Received an invitation to become {an editor}";

        }

        JComponent h = UIUtils.createBoldSubHeader (title,
                                                    Constants.EDITORS_ICON_NAME);

        this.add (h);


        if ((!this.message.isDealtWith ())
            &&
            (!this.message.isSentByMe ())
           )
        {

            this.responseBox = new Box (BoxLayout.Y_AXIS);

            this.responseBox.setBorder (UIUtils.createPadding (0, 5, 0, 5));

            this.add (this.responseBox);

            JComponent l = UIUtils.createBoldSubHeader (getUIString (editors,messages,invite,received,response),
                                                        //"Select your response below",
                                                        null);

            this.responseBox.add (l);

            JButton accept = UIUtils.createButton (getUIString (editors,messages,invite,received,buttons,LanguageStrings.confirm,text),
                                                  //"Accept",
                                                   new ActionListener ()
            {

                @Override
                public void actionPerformed (ActionEvent ev)
                {

                    _this.handleResponse (true);

                }

            });

            accept.setToolTipText (getUIString (editors,messages,invite,received,buttons,LanguageStrings.confirm,tooltip));
            //"Click to accept the invitation");

            JButton reject = UIUtils.createButton (getUIString (editors,messages,invite,received,buttons,LanguageStrings.reject,text),
                                                   //"Reject",
                                                   new ActionListener ()
            {

                @Override
                public void actionPerformed (ActionEvent ev)
                {

                    _this.handleResponse (false);

                }

            });

            reject.setToolTipText (getUIString (editors,messages,invite,received,buttons,LanguageStrings.reject,text));
            //"Click to reject the invitation");

            JButton[] buts = new JButton[] { accept, reject };

            JComponent bb = UIUtils.createButtonBar2 (buts,
                                                      Component.LEFT_ALIGNMENT);

            bb.setAlignmentX (Component.LEFT_ALIGNMENT);

            this.responseBox.add (bb);

        }

    }

    private void handleResponse (boolean accepted)
    {

        final InviteMessageBox _this = this;

        EditorEditor ed = this.message.getEditor ();

        InviteResponseMessage rm = new InviteResponseMessage (accepted,
                                                              EditorsEnvironment.getUserAccount ());
        rm.setEditor (ed);

        ActionListener onComplete = new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.message.setDealtWith (true);

                try
                {

                    EditorsEnvironment.updateMessage (_this.message);

                } catch (Exception e) {

                    Environment.logError ("Unable to update message: " +
                                          _this.message,
                                          e);

                    UIUtils.showErrorMessage (_this.viewer,
                                              getUIString (editors,messages,update,actionerror));
                                              //"Unable to update invite, please contact Quoll Writer support for assistance.");

                }

            }

        };

        if (accepted)
        {

            EditorsEnvironment.acceptInvite (ed,
                                             rm,
                                             onComplete);

        } else {

            EditorsEnvironment.rejectInvite (ed,
                                             rm,
                                             onComplete);

        }

    }
}
