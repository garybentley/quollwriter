package com.quollwriter.ui.fx.popups;

import java.util.*;
import java.nio.file.*;

import org.jdom.*;

import javafx.scene.*;
import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import com.gentlyweb.xml.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class RenameProjectPopup extends PopupContent<AbstractProjectViewer>
{

    public static final String POPUP_ID = "renameproject";

    private Project project = null;

    public RenameProjectPopup (AbstractProjectViewer viewer,
                               Project               project)
    {

        super (viewer);

        this.project = project;
        BasicHtmlTextFlow mess = BasicHtmlTextFlow.builder ()
            .styleClassName (StyleClassNames.DESCRIPTION)
            .withViewer (viewer)
            .text (getUILanguageStringProperty (LanguageStrings.project,actions,renameproject,text))
            .build ();

        QuollTextField text = QuollTextField.builder ()
            .text (this.project.getName ())
            .build ();

        Form f = Form.builder ()
            .withViewer (this.viewer)
            .confirmButton (LanguageStrings.project,actions,renameproject,confirm)
            .cancelButton (actions,cancel)
            .item (text)
            .build ();

        f.setOnCancel (ev ->
        {

            this.close ();

        });

        f.setOnConfirm (ev ->
        {

            String v = text.getText ().trim ();

            if ((v == null)
                ||
                (v.length () == 0)
               )
            {

                ev.consume ();

                f.showError (getUILanguageStringProperty (LanguageStrings.project,actions,renameproject,errors,novalue));
                //"Please enter a new name.";

                return;

            }

            if (!v.equalsIgnoreCase (this.project.getName ()))
            {

                Path newDir = this.project.getProjectDirectory ().getParentFile ().toPath ().resolve (Utils.sanitizeForFilename (v));

                if (Files.exists (newDir))
                {

                    ev.consume ();

                    f.showError (getUILanguageStringProperty (LanguageStrings.project,actions,renameproject,errors,valueexists));
                    //"A {project} with that name already exists.";

                    return;

                }

            }

            try
            {

                this.doRename (v);

                this.close ();

            } catch (Exception e) {

                Environment.logError ("Unable to rename project to: " + v + ", project: " + this.project,
                                      e);

                ComponentUtils.showErrorMessage (this.viewer,
                                                 getUILanguageStringProperty (LanguageStrings.project,actions,renameproject,actionerror));

            }

        });

        VBox b = new VBox ();
        VBox.setVgrow (f, Priority.ALWAYS);
        b.getChildren ().addAll (mess, f);

        this.getChildren ().addAll (b);

    }

    private void doRename (String v)
                    throws Exception
    {

        final String newName = v;
        final String oldName = this.project.getName ();

        if (newName.equals (oldName))
        {

            return;

        }

        if (!newName.equals (oldName))
        {

            this.project.setName (newName);

            // Save the project.
            this.viewer.saveProject ();

            // See how many books are in the project, if there is just one then change the name of it to be the same
            // as the project.
            if (this.project.getBooks ().size () == 1)
            {

                Book b = this.project.getBooks ().get (0);

                b.setName (newName);

                this.viewer.saveObject (b,
                                        true);

            }

            final Path newDir = this.project.getProjectDirectory ().getParentFile ().toPath ().resolve (Utils.sanitizeForFilename (newName));

            // Close the viewer.
            this.viewer.close (true,
                               () ->
            {

                // Rename the dir.
                try
                {

                    Files.move (this.project.getProjectDirectory ().toPath (),
                                newDir);

                } catch (Exception e) {

                    Environment.logError ("Unable to rename project directory: " +
                                          this.project.getProjectDirectory () +
                                          " to: " +
                                          newDir);

                    ComponentUtils.showErrorMessage (getUILanguageStringProperty (LanguageStrings.project,actions,renameproject,actionerror));
                                              //"Unable to rename {project} directory, please contact Quoll Writer support for assistance.");

                    return;

                }

                this.project.setProjectDirectory (newDir.toFile ());

                // Open the project again.
                try
                {

                    Environment.openProject (this.project);

                } catch (Exception e)
                {

                    Environment.logError ("Unable to reopen project: " +
                                          this.project,
                                          e);

                    ComponentUtils.showErrorMessage (getUILanguageStringProperty (LanguageStrings.project,actions,renameproject,errors,cantreopenproject));
                                              //"Unable to reopen project, please contact Quoll Writer support for assistance.");

                    return;

                }

                AbstractProjectViewer apv = Environment.getProjectViewer (this.project);

                if (apv != null)
                {

                    apv.fireProjectEventLater (ProjectEvent.Type.project,
                                               ProjectEvent.Action.rename);

                }

            });

        }

    }

    @Override
    public QuollPopup createPopup ()
    {

        QuollPopup p = QuollPopup.builder ()
            .title (getUILanguageStringProperty (LanguageStrings.project,actions,renameproject,title))
            .styleClassName (StyleClassNames.RENAME)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (POPUP_ID)
            .withViewer (this.viewer)
            .removeOnClose (true)
            .build ();
        p.requestFocus ();

        Environment.fireUserProjectEvent (this.viewer,
                                          ProjectEvent.Type.project,
                                          ProjectEvent.Action.rename);

        return p;

    }

}
