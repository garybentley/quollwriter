package com.quollwriter.editors.ui;

import java.util.*;
import javafx.beans.property.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.*;
import javafx.scene.image.*;
import javafx.embed.swing.*;

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

// Use an annotation?
//@MessageBox(class=NewProjectResponseMessage)
public class NewProjectResponseMessageBox extends MessageBox<NewProjectResponseMessage>
{

    private VBox responseBox = null;

    public NewProjectResponseMessageBox (NewProjectResponseMessage mess,
                                         AbstractViewer            viewer)
    {

        super (mess,
               viewer);

        this.getStyleClass ().add (StyleClassNames.NEWPROJECTRESPONSE);

        this.binder.addChangeListener (mess.getEditor ().editorStatusProperty (),
                                       (pr, oldv, newv) ->
        {

            if (this.responseBox != null)
            {

                this.responseBox.setVisible (false);

            }

        });

        this.binder.addChangeListener (mess.dealtWithProperty (),
                                       (pr, oldv, newv) ->
        {

            if ((newv)
                &&
                (this.responseBox != null)
               )
            {

                this.responseBox.setVisible (false);

            }

        });

        ProjectInfo proj = null;

        try
        {

            proj = Environment.getProjectById (this.message.getForProjectId (),
                                               (this.message.isSentByMe () ? Project.EDITOR_PROJECT_TYPE : Project.NORMAL_PROJECT_TYPE));

        } catch (Exception e) {

            Environment.logError ("Unable to get project: " +
                                  this.message.getForProjectId (),
                                  e);

        }

        final EditorEditor ed = this.message.getEditor ();

        ProjectEditor pe = null;

        if (proj != null)
        {

            try
            {

                pe = EditorsEnvironment.getProjectEditor (proj,
                                                          ed);

            } catch (Exception e) {

                Environment.logError ("Unable to get project editor for project: " +
                                      proj +
                                      ", editor: " +
                                      ed,
                                      e);

            }

        }

        final ProjectEditor fpe = pe;
/*
TODO Remove, not needed
        if (pe == null)
        {

            Environment.logError ("Unable to get project editor for project: " +
                                  proj +
                                  ", editor: " +
                                  ed);

        }
*/
        // Only do this if the editor is still pending.
        if ((!this.message.isDealtWith ())
            &&
            (this.message.getEditor ().isPending ())
           )
        {

            // Show the response.
            this.responseBox = new VBox ();
            this.responseBox.managedProperty ().bind (this.responseBox.visibleProperty ());

            this.getChildren ().add (this.responseBox);

            Header l = Header.builder ()
                .title (editors,messages,newprojectresponse,received,(this.message.isAccepted () ? accepted : rejected),title)
                .styleClassName (StyleClassNames.HEADER)
                .iconClassName (StyleClassNames.ACCEPTED)
                .build ();
            l.getStyleClass ().add (this.message.isAccepted () ? StyleClassNames.ACCEPTED : StyleClassNames.REJECTED);

            this.responseBox.getChildren ().add (l);

            this.responseBox.getChildren ().add (this.getResponseDetails ());

            if (this.message.isAccepted ())
            {

                if ((this.message.getEditorName () != null)
                    ||
                    (this.message.getEditorAvatar () != null)
                   )
                {

                    this.responseBox.getChildren ().add (QuollLabel.builder ()
                        .label (editors,messages,newprojectresponse,labels,extra)
                        .styleClassName (StyleClassNames.SUBTITLE)
                        .build ());

                    HBox editorInfo = new HBox ();

                    editorInfo.getStyleClass ().add (StyleClassNames.EDITORINFO);
                    this.responseBox.getChildren ().add (editorInfo);

                    if (this.message.getEditorAvatar () != null)
                    {

                        ImageView iv = UIUtils.getImageView (this.message.getEditorAvatar ());
                        editorInfo.getChildren ().add (iv);
                        iv.getStyleClass ().add (StyleClassNames.IMAGE);

                    }

                    if (this.message.getEditorName () != null)
                    {

                        editorInfo.getChildren ().add (QuollLabel.builder ()
                            .styleClassName (StyleClassNames.NAME)
                            .label (new SimpleStringProperty (this.message.getEditorName ()))
                            .build ());

                    }

                }

            }

            QuollButton ok = QuollButton.builder ()
                .label (editors,messages,newprojectresponse,received,undealtwith,buttons,confirm)
                .onAction (ev ->
                {

                    try
                    {

                        if (this.message.isAccepted ())
                        {

                            ed.setEditorStatus (EditorEditor.EditorStatus.current);

                            if (this.message.getEditorName () != null)
                            {

                                ed.setName (this.message.getEditorName ());

                            }

                            if (this.message.getEditorAvatar () != null)
                            {

                                ed.setAvatar (this.message.getEditorAvatar ());

                            }

                            EditorsEnvironment.updateEditor (ed);

                            fpe.setStatusMessage (Arrays.asList (editors,messages,newprojectresponse,received,editorstatus,accepted),
                                                                //"Accepted {project}: %s",
                                                  Arrays.asList (Environment.formatDate (this.message.getWhen ())));
                            fpe.setEditorFrom (this.message.getWhen ());
                            fpe.setCurrent (true);
                            fpe.setStatus (ProjectEditor.Status.accepted);

                            EditorsEnvironment.updateProjectEditor (fpe);

                        } else {

                            ed.setEditorStatus (EditorEditor.EditorStatus.rejected);

                            EditorsEnvironment.updateEditor (ed);

                            if (fpe != null)
                            {

                                try
                                {

                                    EditorsEnvironment.removeProjectEditor (fpe);

                                } catch (Exception e) {

                                    Environment.logError ("Unable to remove project editor: " +
                                                          fpe,
                                                          e);

                                }

                            }

                        }

                        this.message.setDealtWith (true);

                        EditorsEnvironment.updateMessage (this.message);

                        this.responseBox.setVisible (false);

                    } catch (Exception e) {

                        Environment.logError ("Unable to update editor: " +
                                              ed,
                                              e);

                        ComponentUtils.showErrorMessage (this.viewer,
                                                         getUILanguageStringProperty (editors,editor,edit,actionerror));
                                                  //"Unable to update {editor}, please contact Quoll Writer support for assistance.");

                        return;

                    }

                })
                .build ();

            QuollButtonBar bb = QuollButtonBar.builder ()
                .button (ok)
                .build ();

            this.responseBox.getChildren ().add (bb);

            return;

        }

        boolean accepted = this.message.isAccepted ();
        //String resMessage = this.message.getResponseMessage ();

        StringProperty t = null;

        if (this.message.isSentByMe ())
        {

            //String text = "Accepted";

            if (!accepted)
            {

                t = getUILanguageStringProperty (editors,messages,newprojectresponse,sent,rejected,title);

                //text = "Rejected";

            } else {

                t = getUILanguageStringProperty (editors,messages,newprojectresponse,sent,LanguageStrings.accepted,title);

            }

            //message = text + " {project}";

        } else {

            //message = "{Project} accepted";

            if (!accepted)
            {

                t = getUILanguageStringProperty (editors,messages,newprojectresponse,received,rejected,title);

                //message = "{Project} rejected";

            } else {

                t = getUILanguageStringProperty (editors,messages,newprojectresponse,received,LanguageStrings.accepted,title);

            }

        }

        Header h = Header.builder ()
            .title (t)
            .iconClassName ((accepted ? StyleClassNames.ACCEPTED : StyleClassNames.REJECTED))
            .styleClassName (StyleClassNames.HEADER)
            .build ();

        this.getChildren ().add (h);

        this.getChildren ().add (this.getResponseDetails ());

        if ((this.message.isSentByMe ())
            &&
            (proj != null)
           )
        {

            QuollHyperlink viewProj = QuollHyperlink.builder ()
                .label (editors,messages,newprojectresponse,sent,labels,clicktoview)
                .styleClassName (StyleClassNames.VIEW)
                .onAction (ev ->
                {

                    ProjectInfo proj2 = null;

                    try
                    {

                        proj2 = Environment.getProjectById (this.message.getForProjectId (),
                                                            Project.EDITOR_PROJECT_TYPE);

                    } catch (Exception e) {

                        Environment.logError ("Unable to get project: " +
                                              this.message.getForProjectId (),
                                              e);

                        ComponentUtils.showErrorMessage (Environment.getFocusedViewer (),
                                                         getUILanguageStringProperty (Arrays.asList (project,actions,openproject,openerrors,general),
                                                                                      this.message.getForProjectId (),
                                                                                      getUILanguageStringProperty (project,actions,openproject,openerrors,unspecified)));
                                                  //"Unable to open {project}, please contact Quoll Writer support for assistance.");

                        return;

                    }

                    try
                    {

                        Environment.openProject (proj2);

                    } catch (Exception e) {

                        Environment.logError ("Unable to get project: " +
                                              this.message.getForProjectId (),
                                              e);

                        ComponentUtils.showErrorMessage (Environment.getFocusedViewer (),
                                                         getUILanguageStringProperty (Arrays.asList (project,actions,openproject,openerrors,general),
                                                                                      this.message.getForProjectId (),
                                                                                      getUILanguageStringProperty (project,actions,openproject,openerrors,unspecified)));
                                                  //"Unable to open {project}, please contact Quoll Writer support for assistance.");

                        return;

                    }

                })
                .build ();

            // TODO Is this needed?
            //this.getChildren ().add (viewProj);

        }

        if ((!this.message.isSentByMe ())
            &&
            (!this.message.isDealtWith ())
           )
        {

            VBox b = new VBox ();
            b.managedProperty ().bind (b.visibleProperty ());

            QuollButton ok = QuollButton.builder ()
                .label (editors,messages,newprojectresponse,received,undealtwith,buttons,confirm)
                .onAction (ev ->
                {

                    try
                    {

                        if (this.message.isAccepted ())
                        {

                            fpe.setStatus (ProjectEditor.Status.accepted);

                            fpe.setEditorFrom (this.message.getWhen ());
                            fpe.setCurrent (true);
                            fpe.setStatusMessage (Arrays.asList (editors,messages,newprojectresponse,received,editorstatus,LanguageStrings.accepted),
                                                                //"Accepted {project}: %s",
                                                  Arrays.asList (Environment.formatDate (this.message.getWhen ())));

                            EditorsEnvironment.updateProjectEditor (fpe);

                        } else {

                            fpe.setCurrent (false);
                            fpe.setStatusMessage (Arrays.asList (editors,messages,newprojectresponse,received,editorstatus,rejected),
                                                                //"Rejected {project}: %s",
                                                  Arrays.asList (Environment.formatDate (this.message.getWhen ())));

                            EditorsEnvironment.removeProjectEditor (fpe);

                        }

                        this.message.setDealtWith (true);

                        EditorsEnvironment.updateMessage (this.message);

                        b.setVisible (false);

                    } catch (Exception e) {

                        Environment.logError ("Unable to update message: " +
                                              this.message,
                                              e);

                        ComponentUtils.showErrorMessage (this.viewer,
                                                         getUILanguageStringProperty (editors,messages,update,actionerror));
                                                  //"Unable to update message, please contact Quoll Writer support for assistance.");

                        return;

                    }

                })
                .build ();

            QuollButtonBar bb = QuollButtonBar.builder ()
                .button (ok)
                .build ();

            b.getChildren ().add (bb);

            this.getChildren ().add (b);

        }

    }

    public boolean isAutoDealtWith ()
    {

        return false;

    }

    private Node getResponseDetails ()
    {

        final NewProjectResponseMessageBox _this = this;

        ProjectInfo proj = null;

        try
        {

            proj = Environment.getProjectById (this.message.getForProjectId (),
                                               (this.message.isSentByMe () ? Project.EDITOR_PROJECT_TYPE : Project.NORMAL_PROJECT_TYPE));

        } catch (Exception e) {

            Environment.logError ("Unable to get project: " +
                                  this.message.getForProjectId (),
                                  e);

        }

        Node itemVal = null;

        if (proj != null)
        {

            itemVal = QuollHyperlink.builder ()
                .label (proj.nameProperty ())
                .tooltip (project,actions,openproject,tooltips,general)
                .styleClassName (StyleClassNames.VIEWPROJECT)
                .onAction (ev ->
                {

                    ProjectInfo proj2 = null;

                    try
                    {

                        proj2 = Environment.getProjectById (_this.message.getForProjectId (),
                                                            (_this.message.isSentByMe () ? Project.EDITOR_PROJECT_TYPE : Project.NORMAL_PROJECT_TYPE));

                    } catch (Exception e) {

                        Environment.logError ("Unable to get project: " +
                                              _this.message.getForProjectId (),
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

            NewProjectMessage m = (NewProjectMessage) this.message.getEditor ().getMessage (NewProjectMessage.MESSAGE_TYPE,
                                                                                            this.message.getForProjectId ());

            if (m != null)
            {

                itemVal = QuollLabel.builder ()
                    .label (new SimpleStringProperty (m.getForProjectName ()))
                    .build ();

            }

        }

        Form.Builder fb = Form.builder ()
            .layoutType (Form.LayoutType.column)
            .styleClassName (StyleClassNames.RESPONSE);

        fb.item (getUILanguageStringProperty (editors,messages,newprojectresponse,labels,project),
                 itemVal);

        String resMessage = this.message.getResponseMessage ();

        if (resMessage != null)
        {

            fb.item (getUILanguageStringProperty (editors,messages,newprojectresponse,labels,LanguageStrings.message),
                     QuollTextView.builder ()
                        .text (new SimpleStringProperty (resMessage))
                        .build ());

        }

        return fb.build ();

    }

}
