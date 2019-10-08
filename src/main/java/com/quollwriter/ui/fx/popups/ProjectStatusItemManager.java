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

public class ProjectStatusItemManager extends PopupContent
{

    public static final String POPUP_ID = "manageprojectstatuses";

    public ProjectStatusItemManager (AbstractViewer viewer)
    {

        super (viewer);

        VBox b = new VBox ();

        final ProjectStatusItemManager _this = this;

        ObservableList<String> types = FXCollections.observableArrayList (UserProperties.getProjectStatuses ().stream ()
                                                                            .map (p -> p.get ())
                                                                            .collect (Collectors.toList ()));

        // Get the current project status items.
        List<String> prefix = Arrays.asList (project,status,actions,manage);

        TextItemManager man = TextItemManager.builder ()
            .addTitle (getUILanguageStringProperty (Utils.newList (prefix,_new,title)))
            .addDescription (getUILanguageStringProperty (Utils.newList (prefix,_new,text)))
            .currentItemsTitle (getUILanguageStringProperty (Utils.newList (prefix,table,title)))
            //.currentItemsDescription ()
            //.errorText ()
            .items (types)
            .build ();

        this.addSetChangeListener (UserProperties.projectStatusesProperty (),
                                   c ->
        {

            man.setItems (FXCollections.observableArrayList (UserProperties.getProjectStatuses ().stream ()
                                                                .map (p -> p.get ())
                                                                .collect (Collectors.toList ())));

        });

        b.getChildren ().add (man);

        man.setOnItemRemoved (ev ->
        {

            String v = ev.getOldValue ();

            UserProperties.removeProjectStatus (v.trim ());

        });

        man.setOnItemAdded (ev ->
        {

            String v = ev.getNewValue ();

            UserProperties.addProjectStatus (v.trim ());

        });

        man.setOnItemChanged (ev ->
        {

            String v = ev.getNewValue ();
            String n = ev.getOldValue ();

            StringProperty p = UserProperties.getProjectStatus (n);

            if (p != null)
            {

                p.setValue (v);

            }

        });

        b.getChildren ().add (QuollButtonBar.builder ()
            .button (QuollButton.builder ()
                        .buttonType (ButtonBar.ButtonData.FINISH)
                        .label (manageitems,finish)
                        .onAction (ev ->
                        {

                            _this.close ();

                        })
                        .build ())
            .build ());

        this.getChildren ().add (b);

    }

    @Override
    public QuollPopup createPopup ()
    {

        QuollPopup p = QuollPopup.builder ()
            .title (project,status,actions,manage,title)
            .styleClassName (StyleClassNames.PROJECTSTATUSES)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (POPUP_ID)
            .withViewer (this.viewer)
            .removeOnClose (true)
            .build ();

        p.setPrefWidth (300);
        p.requestFocus ();

        return p;

    }

}
