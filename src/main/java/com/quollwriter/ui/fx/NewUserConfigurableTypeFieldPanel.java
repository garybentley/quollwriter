package com.quollwriter.ui.fx;

import java.util.*;
import java.util.function.*;

import javafx.beans.property.*;
import javafx.util.*;
import javafx.collections.*;
import javafx.event.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.userobjects.*;
import com.quollwriter.ui.fx.components.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class NewUserConfigurableTypeFieldPanel extends VBox
{

    public static final EventType<Event> FIELD_CREATED_EVENT = new EventType<> ("newtypefield.created");

    private QuollTextField nameField = null;
    private UserConfigurableObjectTypeField field = null;

    public NewUserConfigurableTypeFieldPanel (UserConfigurableObjectType type)
    {

        List<String> prefix = Arrays.asList (userobjects,fields,add);
        this.getStyleClass ().addAll (StyleClassNames.NEW, StyleClassNames.FIELD);
/*
        Header h = Header.builder ()
            // TODO
            .title (getUILanguageStringProperty (userobjects,fields,add,title))
            .build ();

        v.getChildren ().add (h);
*/
        ObservableList<UserConfigurableObjectTypeField.Type> vals = FXCollections.observableList (new ArrayList<> ());

        for (UserConfigurableObjectTypeField.Type t : UserConfigurableObjectTypeField.Type.values ())
        {

            // TODO: File removed for now due to complexity.
            if (t == UserConfigurableObjectTypeField.Type.file)
            {

                continue;

            }

            if (t == UserConfigurableObjectTypeField.Type.objectname)
            {

                continue;

            }

            // Only allow a single object desc.
            if ((t == UserConfigurableObjectTypeField.Type.objectdesc)
                &&
                (type.getObjectDescriptionField () != null)
               )
            {

                continue;

            }

            // Only allow a single object desc.
            if ((t == UserConfigurableObjectTypeField.Type.objectimage)
                &&
                (type.getObjectImageField () != null)
               )
            {

                continue;

            }

            vals.add (t);

        }

        ChoiceBox<UserConfigurableObjectTypeField.Type> selType = new ChoiceBox<> (vals);

        selType.setConverter (new StringConverter<UserConfigurableObjectTypeField.Type> ()
        {

            @Override
            public UserConfigurableObjectTypeField.Type fromString (String s)
            {

                return null;

            }

            @Override
            public String toString (UserConfigurableObjectTypeField.Type p)
            {

                return p.getName ();

            }

        });

        VBox fieldBox = new VBox ();
        fieldBox.managedProperty ().bind (fieldBox.visibleProperty ());
        fieldBox.setVisible (false);

        this.nameField = QuollTextField.builder ()
            .styleClassName (StyleClassNames.NAME)
            .build ();

        Form f = Form.builder ()
            .layoutType (Form.LayoutType.stacked)
            .item (getUILanguageStringProperty (Utils.newList (prefix,labels,LanguageStrings.name)),
                   this.nameField)
            .item (getUILanguageStringProperty (Utils.newList (prefix,labels,LanguageStrings.type)),
                   selType)
            .build ();
        this.getChildren ().addAll (f, fieldBox);

        selType.getSelectionModel ().selectedItemProperty ().addListener ((pr, oldv, newv) ->
        {

            this.field = null;

            fieldBox.getChildren ().clear ();

            UserConfigurableObjectTypeField _field = UserConfigurableObjectTypeField.Type.getNewFieldForType (newv);

            UserConfigurableObjectType fakeType = new UserConfigurableObjectType ();
            fakeType.setObjectTypeName (type.getObjectTypeName ());

            _field.setUserConfigurableObjectType (fakeType);

            final UserConfigurableObjectTypeFieldConfigHandler handler = _field.getConfigHandler2 ();

            Set<Form.Item> nitems = handler.getExtraFormItems ();

            if ((nitems != null)
                &&
                (nitems.size () > 0)
               )
            {

                fieldBox.getChildren ().add (Form.builder ()
                    .layoutType (Form.LayoutType.stacked)
                    .items (nitems)
                    .build ());

                fieldBox.setVisible (true);

            }

            Form bf = Form.builder ()
                .layoutType (Form.LayoutType.stacked)
                .confirmButton (getUILanguageStringProperty (buttons,save))
                .cancelButton (getUILanguageStringProperty (buttons,cancel))
                .build ();
            f.addEventHandler (Form.FormEvent.CONFIRM_EVENT,
                               ev -> bf.fireEvent (ev));
            bf.setOnConfirm (ev ->
            {

                ev.consume ();
                Set<StringProperty> errs = new LinkedHashSet<> ();

                if (nameField.getText ().trim ().equals (""))
                {

                    errs.add (getUILanguageStringProperty (Utils.newList (prefix,errors,LanguageStrings.name,novalue)));

                }

                _field.setUserConfigurableObjectType (null);

                // Check for the name being the same as another field.
                String formName = nameField.getText ().trim ();
                String lformName = formName.toLowerCase ();

                for (UserConfigurableObjectTypeField uf : type.getConfigurableFields ())
                {

                    if (uf.getFormName ().equalsIgnoreCase (lformName))
                    {

                        errs.add (getUILanguageStringProperty (Utils.newList (prefix,errors,LanguageStrings.name,valueexists),
                                                               uf.getFormName ()));

                    }

                }

                Set<StringProperty> errs2 = handler.getExtraFormItemErrors (type);

                if (errs2 != null)
                {

                    errs.addAll (errs2);

                }

                if (errs.size () > 0)
                {

                    f.showErrors (errs);
                    return;

                }

                _field.setFormName (formName);

                handler.updateFromExtraFormItems ();

                _field.setUserConfigurableObjectType (type);
                _field.setOrder (type.getConfigurableFields ().size ());

                this.field = _field;

                this.fireEvent (new Event (FIELD_CREATED_EVENT));

            });

            fieldBox.getChildren ().add (bf);

        });

        selType.getSelectionModel ().select (UserConfigurableObjectTypeField.Type.text);

    }

    public UserConfigurableObjectTypeField getField ()
    {

        return this.field;

    }

    @Override
    public void requestFocus ()
    {

        this.nameField.requestFocus ();

    }

}
