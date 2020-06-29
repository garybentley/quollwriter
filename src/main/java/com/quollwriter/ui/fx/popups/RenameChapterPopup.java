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

public class RenameChapterPopup extends PopupContent<AbstractProjectViewer>
{

    private static final String POPUP_ID = "renamechapter";

    private Chapter chapter = null;

    public RenameChapterPopup (AbstractProjectViewer viewer,
                               Chapter               chapter)
    {

        super (viewer);

        this.chapter = chapter;

        QuollTextField text = QuollTextField.builder ()
            .text (this.chapter.getName ())
            .build ();

        Form f = Form.builder ()
            .inViewer (this.viewer)
            .confirmButton (LanguageStrings.project,actions,renamechapter,confirm)
            .cancelButton (actions,cancel)
            .description (LanguageStrings.project,actions,renamechapter,LanguageStrings.text)
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

                f.showError (getUILanguageStringProperty (project,actions,renamechapter,errors,novalue));
                //"Please enter a new name.";

                return;

            }

            boolean exists = false;

            Set<Chapter> cs = this.chapter.getBook ().getAllChaptersWithName (v);

            if (cs.size () > 0)
            {

                for (Chapter c : cs)
                {

                    if (c.getKey () != this.chapter.getKey ())
                    {

                        exists = true;
                        break;

                    }

                }

            }

            if (exists)
            {

                f.showError (getUILanguageStringProperty (project,actions,renamechapter,errors,valueexists));
                //"Another {chapter} with that name already exists.";

                return;

            }

            try
            {

                this.chapter.setName (v);

                this.viewer.saveObject (this.chapter,
                                        true);

                this.viewer.fireProjectEvent (ProjectEvent.Type.chapter,
                                              ProjectEvent.Action.rename,
                                              this.chapter);

                this.close ();

            } catch (Exception e)
            {

                Environment.logError ("Unable to change name of chapter: " +
                                      this.chapter +
                                      " to: " +
                                      v,
                                      e);

                f.showError (getUILanguageStringProperty (project,actions,renamechapter,actionerror));
                //"An internal error has occurred.\n\nUnable to change name of {chapter}.");

            }

        });

        VBox b = new VBox ();
        VBox.setVgrow (f, Priority.ALWAYS);
        b.getChildren ().addAll (f);

        this.getChildren ().addAll (b);

    }

    @Override
    public QuollPopup createPopup ()
    {

        QuollPopup p = QuollPopup.builder ()
            .title (getUILanguageStringProperty (LanguageStrings.project,actions,renamechapter,title))
            .styleClassName (StyleClassNames.RENAME)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (RenameChapterPopup.getPopupIdForChapter (this.chapter))
            .withViewer (this.viewer)
            .removeOnClose (true)
            .build ();
        p.requestFocus ();

        Environment.fireUserProjectEvent (this.viewer,
                                          ProjectEvent.Type.chapter,
                                          ProjectEvent.Action.rename);

        return p;

    }

    public static String getPopupIdForChapter (Chapter c)
    {

        return POPUP_ID + c.getKey ();

    }

}
