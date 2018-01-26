package com.quollwriter.editors.ui;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;

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
import com.quollwriter.editors.ui.sidebars.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.editors.messages.*;
import com.quollwriter.editors.*;
import com.quollwriter.text.*;
import com.quollwriter.events.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public class UpdateProjectMessageBox extends MessageBox<UpdateProjectMessage> implements EditorChangedListener
{

    private Box responseBox = null;
    private ProjectSentReceivedViewer sentViewer = null;
    private JLabel previousLabel = null;

    public UpdateProjectMessageBox (UpdateProjectMessage mess,
                                    AbstractViewer       viewer)
    {

        super (mess,
               viewer);

    }

    @Override
    public void editorChanged (EditorChangedEvent ev)
    {

        if (ev.getEditor () == this.message.getEditor ())
        {

            this.updateForEditor ();

        }

    }

    private void updateForEditor ()
    {

        this.previousLabel.setVisible (false);

        if ((!this.message.isDealtWith ())
            &&
            (this.message.getEditor ().isPrevious ())
           )
        {

            this.previousLabel.setText (String.format (getUIString (editors,messages,updateproject,received,undealtwith,previouseditor),
                                                        //"<b>%s</b> is a previous {contact}.  This message can no longer be acted upon.",
                                                       this.message.getEditor ().getShortName ()));

            this.previousLabel.setVisible (true);

            this.responseBox.setVisible (false);

        }

    }

    @Override
    public boolean isShowAttentionBorder ()
    {

        if (this.message.getEditor ().isPrevious ())
        {

            return false;

        }

        return super.isShowAttentionBorder ();

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

        EditorsEnvironment.addEditorChangedListener (this);

        final UpdateProjectMessageBox _this = this;

        String text = getUIString (editors,messages,updateproject,sent,title);
        //"{Project} update sent";//Sent {project} update";

        if (!this.message.isSentByMe ())
        {

            text = getUIString (editors,messages,updateproject,received,title);
            //"{Project} update received"; //Received {project} update";

        }

        JComponent h = UIUtils.createBoldSubHeader (text,
                                                    Constants.EDIT_ICON_NAME);

        this.add (h);

        JComponent bp = EditorsUIUtils.getProjectMessageDetails (this.message,
                                                                 this.viewer,
                                                                 this);
        bp.setBorder (UIUtils.createPadding (0, 5, 0, 5));

        this.add (bp);

        this.responseBox = new Box (BoxLayout.Y_AXIS);

        this.responseBox.setVisible (false);

        this.add (this.responseBox);

        this.previousLabel = UIUtils.createInformationLabel ("");

        this.add (this.previousLabel);

        this.updateForEditor ();

        if (!this.message.isSentByMe ())
        {

            ActionListener updateOrView = new ActionListener ()
            {

                @Override
                public void actionPerformed (ActionEvent ev)
                {

                    EditorsUIUtils.showProjectUpdate (_this.message,
                                                      _this.viewer,
                                                      null);

                }

            };

            // Not sent by me.
            if ((!this.message.isDealtWith ())
                &&
                (!this.message.getEditor ().isPrevious ())
               )
            {

                JButton update = UIUtils.createButton (getUIString (editors,messages,updateproject,received,undealtwith,buttons,LanguageStrings.update),
                                                        //"Update the {project}",
                                                       updateOrView);

                //update.setToolTipText ("Click to update the {project}");

                JButton[] buts = new JButton[] { update };

                JComponent bb = UIUtils.createButtonBar2 (buts,
                                                          Component.LEFT_ALIGNMENT);

                bb.setAlignmentX (Component.LEFT_ALIGNMENT);

                this.responseBox.add (bb);
                this.responseBox.setVisible (true);

            }

        }

    }

}
