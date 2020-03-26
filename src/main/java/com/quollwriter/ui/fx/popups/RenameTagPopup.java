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

public class RenameTagPopup extends PopupContent<AbstractProjectViewer>
{

    public static final String POPUP_ID = "renametag";

    private Tag tag = null;

    public RenameTagPopup (AbstractProjectViewer viewer,
                           Tag                   tag)
    {

        super (viewer);

        this.tag = tag;

        QuollTextField text = QuollTextField.builder ()
            .text (this.tag.getName ())
            .build ();

        Form f = Form.builder ()
            .inViewer (this.viewer)
            .description (tags,actions,rename,LanguageStrings.text)
            .confirmButton (tags,actions,rename,buttons,confirm)
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

                f.showError (getUILanguageStringProperty (tags,actions,rename,errors,novalue));
                //"Please enter a new name.";

                return;

            }

            Tag t = Environment.getTagByName (v);

            if ((t != null)
                &&
                (this.tag.equals (t))
               )
            {

                ev.consume ();

                f.showError (getUILanguageStringProperty (Arrays.asList (tags,actions,rename,errors,valueexists),
                                                          t.getName ()));
                //"Please enter a new name.";

                return;

            }

            try
            {

                this.tag.setName (v.trim ());

                Environment.saveTag (this.tag);

                this.close ();

            } catch (Exception e) {

                Environment.logError ("Unable to rename tag to: " + v + ", tag: " + this.tag,
                                      e);

               f.showError (getUILanguageStringProperty (Arrays.asList (tags,actions,rename,errors,general),
                                                         t.getName ()));

                ComponentUtils.showErrorMessage (this.viewer,
                                                 getUILanguageStringProperty (LanguageStrings.project,actions,renameproject,actionerror));

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
            .title (getUILanguageStringProperty (tags,actions,rename,title))
            .styleClassName (StyleClassNames.RENAME)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (RenameTagPopup.getPopupIdForTag (this.tag))
            .withViewer (this.viewer)
            .removeOnClose (true)
            .build ();
        p.requestFocus ();

        Environment.fireUserProjectEvent (this.viewer,
                                          ProjectEvent.Type.tag,
                                          ProjectEvent.Action.rename);

        return p;

    }

    public static String getPopupIdForTag (Tag t)
    {

        return POPUP_ID + t.getKey ();

    }

}
