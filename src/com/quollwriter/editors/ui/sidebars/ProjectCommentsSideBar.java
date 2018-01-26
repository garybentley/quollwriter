package com.quollwriter.editors.ui.sidebars;

import java.util.Vector;
import java.util.Set;
import java.util.Arrays;

import java.awt.event.*;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.*;
import javax.swing.event.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.*;
import com.quollwriter.text.*;
import com.quollwriter.ui.sidebars.*;
import com.quollwriter.editors.*;
import com.quollwriter.editors.ui.*;
import com.quollwriter.editors.messages.*;
import com.quollwriter.ui.components.QPopup;
import com.quollwriter.ui.components.Header;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public class ProjectCommentsSideBar extends ProjectSentReceivedSideBar<ProjectCommentsMessage, ProjectCommentsViewer>
{

    private JLabel otherCommentsLabel = null;

    public ProjectCommentsSideBar (ProjectCommentsViewer  viewer,
                                   ProjectCommentsMessage message)
    {

        super (viewer,
               message);

    }

    @Override
    public void onHide ()
    {

    }

    @Override
    public void onShow ()
    {

    }

    @Override
    public String getTitle ()
    {

        return getUIString (editors,project,sidebar,comments,(this.message.isSentByMe () ? sent : received),title);

        //return String.format ("{Comments} %s",
        //                      (this.message.isSentByMe () ? "to" : "from"));

    }

    @Override
    public String getItemsIconType ()
    {

        return Constants.COMMENT_ICON_NAME;

    }

    @Override
    public int getItemCount ()
    {

        return this.message.getComments ().size ();

    }

    @Override
    public String getItemsTitle ()
    {

        return "{Comments}";

    }

    @Override
    public String getIconType ()
    {

        return null;

    }

    @Override
    public JComponent getMessageDetails (ProjectCommentsMessage message)
    {

        final ProjectCommentsSideBar _this = this;

        String rows = "p";

        String verName = this.viewer.getProject ().getProjectVersion ().getName ();

        if (verName != null)
        {

            rows += ", 6px, p";

        }

        final String genComments = message.getGeneralComment ();

        if (genComments != null)
        {

            rows += ", 6px, top:p";

        }

        FormLayout fl = new FormLayout ("right:p, 6px, fill:100px:grow",
                                        rows);

        fl.setHonorsVisibility (true);
        PanelBuilder builder = new PanelBuilder (fl);

        CellConstraints cc = new CellConstraints ();

        java.util.List<String> prefix = Arrays.asList (editors,project,sidebar,comments,labels);

        int row = 1;

        builder.addLabel ("<html>" + getUIString (prefix,(this.message.isSentByMe () ? sent : received)) + "</html>",
                        //(this.message.isSentByMe () ? "Sent" : "Received"))),
                          cc.xy (1,
                                 row));

        builder.addLabel (Environment.formatDateTime (message.getWhen ()),
                          cc.xy (3,
                                 row));

        row += 2;

        if (verName != null)
        {

            builder.addLabel ("<html>" + getUIString (prefix,version) + "</html>",
                            //"<i>Version</i></html>"),
                              cc.xy (1,
                                     row));

            builder.addLabel (verName,
                              cc.xy (3,
                                     row));

            row += 2;

        }

        if (genComments != null)
        {

            builder.addLabel ("<html>" + getUIString (prefix,notes) + "</html>",
                            //>{Notes}</i></html>"),
                              cc.xy (1,
                                     row));

            String commText = genComments;

            TextIterator ti = new TextIterator (commText);

            final int sc = ti.getSentenceCount ();

            if (sc > 1)
            {

                commText = ti.getFirstSentence ().getText ();

                commText += getUIString (prefix,more);
                //"<br /><a href='#'>More, click to view all.</a>";

            }

            JComponent mess = UIUtils.createHelpTextPane (commText,
                                                          this.viewer);

            if (sc > 1)
            {

                mess.addMouseListener (new MouseEventHandler ()
                {

                    public void handlePress (MouseEvent ev)
                    {

                        UIUtils.showMessage ((PopupsSupported) _this.viewer,
                                             getUIString (editors,project,sidebar,comments,notes,popup,title),
                                             //"Notes",
                                             genComments);

                    }

                });

            }

            mess.setBorder (null);

            builder.add (mess,
                         cc.xy (3,
                                row));

            row += 2;

        }

        JPanel bp = builder.getPanel ();
        bp.setAlignmentX (Component.LEFT_ALIGNMENT);

        Box b = new Box (BoxLayout.Y_AXIS);

        b.add (bp);

        this.otherCommentsLabel = UIUtils.createClickableLabel ("",
                                                                Environment.getIcon (Constants.VIEW_ICON_NAME,
                                                                                     Constants.ICON_MENU),
                                                                new ActionListener ()
                                                                {

                                                                    public void actionPerformed (ActionEvent ev)
                                                                    {

                                                                        _this.showOtherCommentsSelector ();

                                                                    }

                                                                });

        this.otherCommentsLabel.setBorder (UIUtils.createPadding (5, 5, 5, 5));

        this.showOtherCommentsLabel ();

        b.add (this.otherCommentsLabel);

        return b;

    }

    private void showOtherCommentsSelector ()
    {

        final ProjectCommentsSideBar _this = this;

        final Vector<ProjectCommentsMessage> pcms = new Vector ();

        Set<EditorMessage> messages = this.getMessage ().getEditor ().getMessages (this.getMessage ().getForProjectId (),
                                                                                   ProjectCommentsMessage.MESSAGE_TYPE);

        for (EditorMessage m : messages)
        {

            ProjectCommentsMessage pcm = (ProjectCommentsMessage) m;

            if ((pcm.equals (this.getMessage ()))
                ||
                (!pcm.getProjectVersion ().getId ().equals (this.getMessage ().getProjectVersion ().getId ()))
               )
            {

                continue;

            }

            pcms.add (pcm);

        }

        if (pcms.size () == 0)
        {

            return;

        }

        if (pcms.size () == 1)
        {

            EditorsUIUtils.showProjectComments (pcms.get (0),
                                                _this.viewer,
                                                null);

        } else {

            final JList<ProjectCommentsMessage> projCmsL = new JList (pcms);

            projCmsL.addListSelectionListener (new ListSelectionListener ()
            {

                public void valueChanged (ListSelectionEvent ev)
                {

                    ProjectCommentsMessage pcm = projCmsL.getSelectedValue ();

                    EditorsUIUtils.showProjectComments (pcm,
                                                        _this.viewer,
                                                        null);

                    UIUtils.closePopupParent (projCmsL);

                }

            });

            projCmsL.setCellRenderer (new DefaultListCellRenderer ()
            {

                public Component getListCellRendererComponent (JList   list,
                                                               Object  value,
                                                               int     index,
                                                               boolean isSelected,
                                                               boolean cellHasFocus)
                {

                    Box b = new Box (BoxLayout.Y_AXIS);

                    if (index < pcms.size () - 1)
                    {

                        b.setBorder (UIUtils.createBottomLineWithPadding (3, 3, 3, 3));

                    } else {

                        b.setBorder (UIUtils.createPadding (3, 3, 3, 3));

                    }

                    ProjectCommentsMessage pcm = (ProjectCommentsMessage) value;

                    int c = pcm.getComments ().size ();

                    Header h = UIUtils.createBoldSubHeader (String.format (getUIString (editors,project,sidebar,comments,othercomments,item,title),
                                                                            //"%s {comment%s}",
                                                                           Environment.formatNumber (c)),
                                                                           //(c == 1 ? "" : "s")),
                                                            null);

                    b.add (h);

                    h.setPreferredSize (new Dimension (Short.MAX_VALUE,
                                                       h.getPreferredSize ().height));

                    // Get the first line of the notes, if provided.
                    String genComm = pcm.getGeneralComment ();

                    if (genComm != null)
                    {

                        TextIterator ti = new TextIterator (genComm);

                        if (ti.getSentenceCount () > 1)
                        {

                            genComm = ti.getFirstSentence ().getText ();

                        }

                        JComponent mess = UIUtils.createHelpTextPane (genComm,
                                                                      _this.viewer);

                        Box mb = new Box (BoxLayout.X_AXIS);

                        mb.setAlignmentX (Component.LEFT_ALIGNMENT);

                        mess.setBorder (null);

                        mb.add (mess);

                        mb.setBorder (UIUtils.createPadding (0, 5, 0, 5));

                        b.add (mb);

                    }

                    JLabel info = UIUtils.createInformationLabel (String.format (getUIString (editors,project,sidebar,comments,othercomments,item,(_this.getMessage ().isSentByMe () ? sent : received)),
                                                                                //"Received: %s",
                                                                                 Environment.formatDate (pcm.getWhen ())));

                    info.setBorder (UIUtils.createPadding (0, 5, 0, 5));

                    b.add (info);

                    b.setToolTipText (getUIString (editors,project,sidebar,comments,othercomments,item,tooltip));
                    //"<html>Click to view the {comments}.</html>");

                    return b;

                }

            });

            final QPopup qp = UIUtils.createClosablePopup (getUIString (editors,project,sidebar,comments,othercomments,popup,title),
                                                            //"Select a set of comments to view",
                                                           Environment.getIcon (Constants.COMMENT_ICON_NAME,
                                                                                Constants.ICON_POPUP),
                                                           null);

            Box content = new Box (BoxLayout.Y_AXIS);

            UIUtils.setAsButton (projCmsL);

            content.add (projCmsL);
            content.setBorder (UIUtils.createPadding (10, 10, 10, 10));
            qp.setContent (content);

            content.setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                                     content.getPreferredSize ().height));

            _this.viewer.showPopupAt (qp,
                                      this.otherCommentsLabel,
                                      false);

            qp.setDraggable (_this);

        }

    }

    private void showOtherCommentsLabel ()
    {

        Set<EditorMessage> messages = this.getMessage ().getEditor ().getMessages (this.getMessage ().getForProjectId (),
                                                                                   ProjectCommentsMessage.MESSAGE_TYPE);

        int c = 0;

        for (EditorMessage m : messages)
        {

            ProjectCommentsMessage pcm = (ProjectCommentsMessage) m;

            if ((pcm.equals (this.getMessage ()))
                ||
                (!pcm.getProjectVersion ().getId ().equals (this.getMessage ().getProjectVersion ().getId ()))
               )
            {

                continue;

            }

            c += pcm.getComments ().size ();

        }

        String l = String.format (getUIString (editors,project,sidebar,comments,labels,othercomments),
                                  Environment.formatNumber (c));
/*
        if (c == 1)
        {

            l = "1 other {comment} is available for this version, click to view it.";

        } else {

            l = String.format ("%s other {comments} are available for this version, click to select other {comments} to view.",
                               Environment.formatNumber (c));

        }
*/

        this.otherCommentsLabel.setText (l);

        this.otherCommentsLabel.setVisible (c > 0);

    }

};
