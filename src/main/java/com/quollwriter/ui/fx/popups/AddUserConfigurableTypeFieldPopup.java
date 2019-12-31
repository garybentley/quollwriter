package com.quollwriter.ui.fx.popups;

import java.util.*;
import java.util.function.*;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.event.*;

import javafx.beans.property.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.text.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class AddUserConfigurableTypeFieldPopup extends PopupContent<ProjectViewer>
{

    private static final String POPUP_ID = "newtypefield";

    private NewUserConfigurableTypeFieldPanel panel = null;

    public AddUserConfigurableTypeFieldPopup (ProjectViewer                             viewer,
                                              UserConfigurableObjectType                type,
                                              Consumer<UserConfigurableObjectTypeField> onFieldAdded)
    {

        super (viewer);

        this.panel = new NewUserConfigurableTypeFieldPanel (type);

        this.panel.addEventHandler (NewUserConfigurableTypeFieldPanel.FIELD_CREATED_EVENT,
                                    ev ->
        {

            onFieldAdded.accept (this.panel.getField ());

            this.close ();
            ev.consume ();

        });

        this.panel.addEventHandler (Form.FormEvent.CANCEL_EVENT,
                                    ev ->
        {

            this.close ();
            ev.consume ();

        });

        this.getChildren ().add (this.panel);

    }

    @Override
    public QuollPopup createPopup ()
    {

        QuollPopup p = QuollPopup.builder ()
            .title (userobjects,fields,add,title)
            .styleClassName (StyleClassNames.ADD)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (POPUP_ID)
            .removeOnClose (true)
            .withViewer (this.viewer)
            .build ();

        p.toFront ();

        p.addEventHandler (QuollPopup.PopupEvent.SHOWN_EVENT,
                           ev ->
        {

            this.panel.requestFocus ();

        });

        return p;

    }

    public static String getPopupIdForNote (Note ci)
    {

        return POPUP_ID + ci.getObjectReference ().asString ();

    }

}
