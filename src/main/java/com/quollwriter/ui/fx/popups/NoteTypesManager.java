package com.quollwriter.ui.fx.popups;

import java.util.*;
import java.util.stream.*;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.collections.transformation.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class NoteTypesManager extends PopupContent
{

    public static final String POPUP_ID = "notetypesmanager";

    public NoteTypesManager (AbstractViewer viewer)
    {

        super (viewer);

        VBox b = new VBox ();

        // TODO Add a listener on the user properties to make changes as things change.

        TextItemManager man = TextItemManager.builder ()
            .addTitle (getUILanguageStringProperty (notetypes,actions,manage,newtypes,title))
            .addDescription (getUILanguageStringProperty (notetypes,actions,manage,newtypes,text))
            .currentItemsTitle (getUILanguageStringProperty (notetypes,actions,manage,table,title))
            .currentItemsDescription (getUILanguageStringProperty (notetypes,actions,manage,table,text))
            .items (FXCollections.observableList (UserProperties.getNoteTypes ().stream ()
                        .map (p -> p.getValue ())
                        .collect (Collectors.toList ())))
            .build ();

        b.getChildren ().add (man);

        man.setOnItemRemoved (ev ->
        {

            String v = ev.getOldValue ();
            UserProperties.removeNoteType (v);

        });

        man.setOnItemAdded (ev ->
        {

            String v = ev.getNewValue ();
            UserProperties.addNoteType (v);

        });

        man.setOnItemChanged (ev ->
        {

            String v = ev.getNewValue ();
            String n = ev.getOldValue ();
            StringProperty oldv = UserProperties.getNoteTypeProperty (n);
            oldv.setValue (v);

        });

        b.getChildren ().add (QuollButtonBar.builder ()
            .button (QuollButton.builder ()
                        .buttonType (ButtonBar.ButtonData.FINISH)
                        .label (manageitems,finish)
                        .onAction (ev ->
                        {

                            this.close ();

                        })
                        .build ())
            .build ());

        this.getChildren ().add (b);

    }

    @Override
    public QuollPopup createPopup ()
    {

        QuollPopup p = QuollPopup.builder ()
            .title (notetypes,actions,manage,title)
            .styleClassName (StyleClassNames.NOTETYPESMANAGER)
            .headerIconClassName (StyleClassNames.EDIT)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (POPUP_ID)
            .withViewer (this.viewer)
            .removeOnClose (true)
            .build ();

        p.requestFocus ();

        return p;

    }

}
