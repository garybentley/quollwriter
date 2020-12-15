package com.quollwriter.ui.fx.popups;

import java.util.*;
import java.util.function.*;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.event.*;

import javafx.beans.property.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.text.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class EditUserConfigurableTypeFieldPopup extends PopupContent<ProjectViewer>
{

    private static final String POPUP_ID = "edittypefield";

    private EditUserConfigurableTypeFieldPanel panel = null;
    private UserConfigurableObjectTypeField field = null;

    public EditUserConfigurableTypeFieldPopup (ProjectViewer                   viewer,
                                               UserConfigurableObjectTypeField field,
                                               Runnable                        onFieldUpdated)
    {

        super (viewer);

        this.field = field;
        this.panel = new EditUserConfigurableTypeFieldPanel (field);

         this.panel.addEventHandler (Form.FormEvent.CONFIRM_EVENT,
                                     ev ->
         {

             onFieldUpdated.run ();

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
            .title (userobjects,fields,edit,title)
            .styleClassName (StyleClassNames.EDIT)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (getPopupIdForField (this.field))
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

    public static String getPopupIdForField (UserConfigurableObjectTypeField f)
    {

        return POPUP_ID + f.getObjectReference ().asString ();

    }

}
