package com.quollwriter.editors.ui;

import java.util.*;
import javafx.scene.*;
import javafx.beans.property.*;

import com.quollwriter.*;
import com.quollwriter.text.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.editors.messages.*;
import com.quollwriter.editors.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

//@MessageBoxFor(Message=ProjectCommentsMessage.class)
public class ProjectCommentsMessageBox extends MessageBox<ProjectCommentsMessage>
{

    private AbstractProjectViewer commentsViewer = null;

    public ProjectCommentsMessageBox (ProjectCommentsMessage mess,
                                      AbstractViewer         viewer)
    {

        super (mess,
               viewer);

        this.getStyleClass ().add (StyleClassNames.PROJECTCOMMENTS);

        final ProjectCommentsMessageBox _this = this;

        ProjectInfo proj = null;

        try
        {

            proj = Environment.getProjectById (this.message.getForProjectId (),
                                               (this.message.isSentByMe () ? Project.EDITOR_PROJECT_TYPE : Project.NORMAL_PROJECT_TYPE));

        } catch (Exception e) {

            Environment.logError ("Unable to get project info for project with id: " +
                                  this.message.getForProjectId (),
                                  e);

        }

        this.getChildren ().add (Header.builder ()
            .title (getUILanguageStringProperty (Arrays.asList (editors,messages,projectcomments,(this.message.isSentByMe () ? sent : received),title),
                                                 this.message.getComments ().size ()))
            .styleClassName (StyleClassNames.SUBTITLE)
            .iconClassName (StyleClassNames.COMMENT)
            .build ());

        // Show
        //   * Sent/Received
        //   * Version (optional)
        //   * Notes (optional)
        //   * View comments

        ProjectVersion pv = this.message.getProjectVersion ();
        String genComm = this.message.getGeneralComment ();

        String verName = pv.getName ();

        EditorEditor ed = this.message.getEditor ();

        // We are wimping out here
        String projVerName = this.message.getProjectVersion ().getName ();

        String projVerId = this.message.getProjectVersion ().getId ();

        Form.Builder fb = Form.builder ()
            .layoutType (Form.LayoutType.column);

        if (proj != null)
        {

            fb.item (getUILanguageStringProperty (editors,messages,projectcomments,labels,project),
                     QuollHyperlink.builder ()
                        .label (message.forProjectNameProperty ())
                        .styleClassName (StyleClassNames.VIEWPROJECT)
                        .tooltip (editors,messages,projectcomments,labels,clicktoviewproject)
                        .onAction (ev ->
                        {

                            ProjectInfo _proj = null;

                            try
                            {

                                _proj = Environment.getProjectById (_this.message.getForProjectId (),
                                                                    (_this.message.isSentByMe () ? Project.EDITOR_PROJECT_TYPE : Project.NORMAL_PROJECT_TYPE));

                            } catch (Exception e) {

                                Environment.logError ("Unable to get project info for project with id: " +
                                                      _this.message.getForProjectId (),
                                                      e);

                            }

                            if (_proj != null)
                            {

                                try
                                {

                                    Environment.openProject (_proj);

                                } catch (Exception e) {

                                    Environment.logError ("Unable to open project: " +
                                                          _proj,
                                                          e);

                                }

                            }

                        })
                        .build ());

        } else {

            fb.item (getUILanguageStringProperty (editors,messages,projectcomments,labels,project),
                     QuollLabel.builder ()
                        .label (message.forProjectNameProperty ())
                        .build ());

        }

        fb.item (getUILanguageStringProperty (editors,messages,projectcomments,labels,(this.message.isSentByMe () ? sent : received)),
                 QuollLabel.builder ()
                    .label (new SimpleStringProperty (Environment.formatDateTime (this.message.getWhen ())))
                    .build ());

        if (verName != null)
        {

            fb.item (getUILanguageStringProperty (editors,messages,projectcomments,labels,version),
                     QuollLabel.builder ()
                        .label (this.message.getProjectVersion ().nameProperty ())
                        .build ());

        }

        if (genComm != null)
        {

            TextIterator ti = new TextIterator (genComm);

            if (ti.getSentenceCount () > 1)
            {

                genComm = ti.getFirstSentence ().getText ();

            }

            fb.item (getUILanguageStringProperty (editors,messages,projectcomments,labels,notes),
                     QuollTextView.builder ()
                        .inViewer (this.viewer)
                        .text (new SimpleStringProperty (genComm))
                        .build ());

        }

        if (proj != null)
        {

            fb.item (QuollHyperlink.builder ()
                        .label (editors,messages,projectcomments,labels,clicktoviewcomments)
                        .styleClassName (StyleClassNames.VIEW)
                        .onAction (ev ->
                        {

                            this.message.setDealtWith (true);

                            try
                            {

                                EditorsEnvironment.updateMessage (this.message);

                            } catch (Exception e) {

                                Environment.logError ("Unable to update message: " +
                                                      this.message,
                                                      e);

                            }

                            if (this.commentsViewer != null)
                            {

                                this.commentsViewer.show ();
                                this.commentsViewer.toFront ();

                                return;

                            }

                            EditorsUIUtils.showProjectComments (this.message,
                                                                this.viewer,
                                                                (commViewer) ->
                                                                {

                                                                    _this.commentsViewer = commViewer;
                                                                    _this.commentsViewer.getViewer ().addEventHandler (Viewer.ViewerEvent.CLOSE_EVENT,
                                                                                                                       eev ->
                                                                    {

                                                                        _this.commentsViewer = null;

                                                                    });

                                                                });

                        })
                        .build ());

        }

        this.getChildren ().add (fb.build ());

    }

    public boolean isAutoDealtWith ()
    {

        return false;

    }

}
