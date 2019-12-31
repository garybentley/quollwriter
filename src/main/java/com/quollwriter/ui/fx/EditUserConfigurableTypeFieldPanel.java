package com.quollwriter.ui.fx;

import java.util.*;
import java.util.function.*;

import javafx.beans.property.*;
import javafx.util.*;
import javafx.collections.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.userobjects.*;
import com.quollwriter.ui.fx.components.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class EditUserConfigurableTypeFieldPanel extends VBox
{

    private QuollTextField nameField = null;

    public EditUserConfigurableTypeFieldPanel (UserConfigurableObjectTypeField field)
    {

        List<String> prefix = Arrays.asList (userobjects,fields,LanguageStrings.edit);

/*
        Header h = Header.builder ()
            .title (getUILanguageStringProperty (userobjects,fields,LanguageStrings.edit,title))
            .build ();

        this.config.getChildren ().add (h);
*/
        UserConfigurableObjectTypeFieldConfigHandler handler = field.getConfigHandler2 ();

        this.nameField = QuollTextField.builder ()
            .styleClassName (StyleClassNames.NAME)
            .text (field.getName ())
            .build ();

        Form.Builder fb = Form.builder ()
            .layoutType (Form.LayoutType.stacked)
            .item (getUILanguageStringProperty (Utils.newList (prefix,labels,LanguageStrings.name)),
                   nameField);

        Set<Form.Item> nitems = handler.getExtraFormItems ();

        if ((nitems != null)
            &&
            (nitems.size () > 0)
           )
        {

            fb.items (nitems);

        }

        Form f = fb.confirmButton (getUILanguageStringProperty (buttons,save))
                   .cancelButton (getUILanguageStringProperty (buttons,cancel))
                   .build ();
        f.setOnConfirm (ev ->
        {

            ev.consume ();
            Set<StringProperty> errs = new LinkedHashSet<> ();

           if (nameField.getText ().trim ().equals (""))
           {

               errs.add (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.errors,LanguageStrings.name,novalue)));

           }

           // Check for the name being the same as another field.
           String formName = nameField.getText ().trim ();
           String lformName = formName.toLowerCase ();

           for (UserConfigurableObjectTypeField uf : field.getUserConfigurableObjectType ().getConfigurableFields ())
           {

               if ((!uf.equals (field))
                   &&
                   (uf.getFormName ().equalsIgnoreCase (lformName))
                  )
               {

                   errs.add (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.errors,LanguageStrings.name,valueexists),
                                                          uf.getFormName ()));

               }

           }

           Set<StringProperty> errs2 = handler.getExtraFormItemErrors (field.getUserConfigurableObjectType ());

           errs.addAll (errs2);

           if (errs.size () > 0)
           {

               f.showErrors (errs);
               return;

           }

           field.setFormName (formName);

           handler.updateFromExtraFormItems ();

           this.fireEvent (new Form.FormEvent (f, Form.FormEvent.CONFIRM_EVENT));

       });


        this.getChildren ().add (f);

    }

    @Override
    public void requestFocus ()
    {

        this.nameField.requestFocus ();

    }

}
