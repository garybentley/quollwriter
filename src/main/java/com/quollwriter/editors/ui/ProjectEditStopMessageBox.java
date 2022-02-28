package com.quollwriter.editors.ui;

import java.util.*;
import javafx.beans.property.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.*;
import javafx.scene.image.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.editors.messages.*;
import com.quollwriter.editors.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

//@MessageBoxFor(Message=ProjectEditStopMessage.class)
public class ProjectEditStopMessageBox extends MessageBox<ProjectEditStopMessage>
{

    private AbstractProjectViewer commentsViewer = null;
    private VBox responseBox = null;

    public ProjectEditStopMessageBox (ProjectEditStopMessage mess,
                                      AbstractViewer         viewer)
    {

        super (mess,
               viewer);

        final ProjectEditStopMessageBox _this = this;

        this.getStyleClass ().add ("projecteditstopmessage");

        StringProperty title = null;
        //"Stopped editing {project}";

        if (this.message.isSentByMe ())
        {

            title = getUILanguageStringProperty (editors,messages,projecteditstop,sent,LanguageStrings.title);

        } else {

            title = getUILanguageStringProperty (editors,messages,projecteditstop,received,LanguageStrings.title);

        }

       this.getChildren ().add (Header.builder ()
        .title (title)
        .styleClassName (StyleClassNames.HEADER)
        .iconClassName (StyleClassNames.STOPPED)
        .build ());

        Form.Builder fb = Form.builder ();
        fb.layoutType (Form.LayoutType.column);

        String reason = this.message.getReason ();

         ProjectInfo proj = null;

         try
         {

             proj = Environment.getProjectById (_this.message.getForProjectId (),
                                                null);

         } catch (Exception e) {

             Environment.logError ("Unable to get project info for project with id: " +
                                   _this.message.getForProjectId (),
                                   e);

         }

         Node projVal = null;

         if (proj != null)
         {

             projVal = QuollHyperlink.builder ()
                .label (new SimpleStringProperty (this.message.getForProjectName ()))
                .tooltip (project,actions,openproject,tooltips,general)
                .onAction (ev ->
                {

                    ProjectInfo proj2 = null;

                    try
                    {

                        proj2 = Environment.getProjectById (this.message.getForProjectId (),
                                                           null);

                    } catch (Exception e) {

                        Environment.logError ("Unable to get project info for project with id: " +
                                              this.message.getForProjectId (),
                                              e);

                    }

                    if (proj2 != null)
                    {

                        try
                        {

                            Environment.openProject (proj2);

                        } catch (Exception e) {

                            Environment.logError ("Unable to open project: " +
                                                  proj2,
                                                  e);

                        }

                    }

                })
                .build ();

        } else {

            projVal = QuollLabel.builder ()
                .label (new SimpleStringProperty (this.message.getForProjectName ()))
                .build ();

        }

        fb.item (getUILanguageStringProperty (editors,messages,projecteditstop,labels,project),
                 projVal);

        if (reason != null)
        {

            fb.item (getUILanguageStringProperty (editors,messages,projecteditstop,labels,project),
                     QuollTextView.builder ()
                        .text (new SimpleStringProperty (reason))
                        .build ());

        }

        this.getChildren ().add (fb.build ());

        if (!this.message.isDealtWith ())
        {

            // Show the response.
            this.responseBox = new VBox ();
            this.responseBox.managedProperty ().bind (this.responseBox.visibleProperty ());
            this.responseBox.getStyleClass ().add (StyleClassNames.RESPONSE);

            this.getChildren ().add (this.responseBox);

            this.responseBox.getChildren ().add (QuollTextView.builder ()
                .inViewer (viewer)
                .text (getUILanguageStringProperty (Arrays.asList (editors,messages,projecteditstop,received,undealtwith,text),
                                                    this.message.getEditor ().getMainName (),
                                                    this.message.getForProjectName ()))
                .build ());

            final EditorEditor ed = this.message.getEditor ();

            QuollButton ok = QuollButton.builder ()
                .label (editors,messages,projecteditstop,received,undealtwith,buttons,confirm)
                .onAction (ev ->
                {

                    try
                    {

                        ProjectInfo p = Environment.getProjectById (this.message.getForProjectId (),
                                                                    Project.NORMAL_PROJECT_TYPE);

                        ProjectEditor pe = EditorsEnvironment.getProjectEditor (p,
                                                                                ed);

                        pe.setCurrent (false);
                        pe.setEditorTo (new Date ());
                        pe.statusMessageProperty ().unbind ();
                        pe.statusMessageProperty ().bind (getUILanguageStringProperty (Arrays.asList (editors,messages,projecteditstop,editorstatus),
                                                            //"Stopped editing: %s",
                                                                                       Environment.formatDate (pe.getEditorTo ())));

                        EditorsEnvironment.updateProjectEditor (pe);

                        this.message.setDealtWith (true);

                        EditorsEnvironment.updateMessage (this.message);

                    } catch (Exception e) {

                        Environment.logError ("Unable to update editor: " +
                                              ed,
                                              e);

                        ComponentUtils.showErrorMessage (this.viewer,
                                                         getUILanguageStringProperty (editors,editor,edit,actionerror));
                                                  //"Unable to update {contact}, please contact Quoll Writer support for assistance.");

                        return;

                    }

                })
                .build ();

            QuollButtonBar bb = QuollButtonBar.builder ()
                .button (ok)
                .build ();

            this.responseBox.getChildren ().add (bb);

        }

        this.binder.addChangeListener (this.message.dealtWithProperty (),
                                       (pr, oldv, newv) ->
        {

            this.responseBox.setVisible (!newv);

        });

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

}
