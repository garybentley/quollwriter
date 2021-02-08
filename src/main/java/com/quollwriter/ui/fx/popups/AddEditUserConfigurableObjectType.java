package com.quollwriter.ui.fx.popups;

import java.util.*;

import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class AddEditUserConfigurableObjectType extends PopupContent<AbstractViewer>
{

    public static final String POPUP_ID = "userconfigobjtype";
    private UserConfigurableObjectType type = null;
    private QuollTextField name = null;
    private QuollTextField plural = null;
    private ImageSelector smallIcon = null;
    private ImageSelector bigIcon = null;

    public AddEditUserConfigurableObjectType (AbstractViewer viewer)
    {

        this (new UserConfigurableObjectType (),
              viewer);

    }

    public AddEditUserConfigurableObjectType (UserConfigurableObjectType type,
                                              AbstractViewer             viewer)
    {

        super (viewer);

        this.type = type;

        final java.util.List<String> prefix = Arrays.asList (userobjects,basic,edit);

        String n = this.type.getObjectTypeName ();

        if (this.type.getKey () == null)
        {

            n = getUILanguageStringProperty (userobjects,LanguageStrings.type,_new,defaults,LanguageStrings.names,singular).getValue ();

        }

        this.name = QuollTextField.builder ()
            .text (n)
            .build ();

        String pn = this.type.getObjectTypeNamePlural ();

        if (this.type.getKey () == null)
        {

            pn = getUILanguageStringProperty (userobjects,LanguageStrings.type,_new,defaults,LanguageStrings.names,LanguageStrings.plural).getValue ();

        }

        this.plural = QuollTextField.builder ()
            .text (pn)
            .build ();

        int bigIconWidth = 24;

        Image bigIm = this.type.getIcon24x24 ();

        this.bigIcon = ImageSelector.builder ()
            .styleClassName (StyleClassNames.BIGICON)
            .image (bigIm)
            .withViewer (this.viewer)
            .build ();

        if (bigIm == null)
        {

            this.bigIcon.pseudoClassStateChanged (StyleClassNames.NOIMAGE_PSEUDO_CLASS, true);

        }

        this.bigIcon.imageProperty ().addListener ((pr, oldv, newv) ->
        {

            if ((newv != null)
                &&
                (oldv != null)
                &&
                (!newv.equals (oldv))
               )
            {

                if (this.smallIcon.getImagePath () == null)
                {

                    try
                    {

                        this.smallIcon.setImage (this.bigIcon.getImagePath ());

                    } catch (Exception e) {

                        Environment.logError ("Unable to set image: " + this.bigIcon.getImagePath (),
                                              e);

                    }

                }

                if (newv.getWidth () != bigIconWidth)
                {

                    String pid = "bigiconwarn";

                    if (viewer.getPopupById (pid) != null)
                    {

                        return;

                    }

                    QuollPopup.messageBuilder ()
                        .withViewer (viewer)
                        .popupId (pid)
                        .styleClassName (StyleClassNames.WARNING)
                        .title (userobjects,basic,edit,warnings,LanguageStrings.bigicon,LanguageStrings.popup,title)
                        .message (getUILanguageStringProperty (Arrays.asList (userobjects,basic,edit,warnings,LanguageStrings.bigicon,LanguageStrings.popup,text),
                                                               bigIconWidth))
                        .closeButton ()
                        .build ();

                }

            }

        });

        int smallIconWidth = 16;

        Image smallIm = this.type.getIcon16x16 ();

        this.smallIcon = ImageSelector.builder ()
            .styleClassName (StyleClassNames.SMALLICON)
            .image (smallIm)
            .withViewer (this.viewer)
            .build ();

        if (smallIm == null)
        {

            this.smallIcon.pseudoClassStateChanged (StyleClassNames.NOIMAGE_PSEUDO_CLASS, true);

        }

        this.smallIcon.imageProperty ().addListener ((pr, oldv, newv) ->
        {

            if ((newv != null)
                &&
                (oldv != null)
                &&
                (!newv.equals (oldv))
               )
            {

                if (this.bigIcon.getImagePath () == null)
                {

                    try
                    {

                        this.bigIcon.setImage (this.smallIcon.getImagePath ());

                    } catch (Exception e) {

                        Environment.logError ("Unable to set image: " + this.smallIcon.getImagePath (),
                                              e);

                    }

                }

                if (newv.getWidth () != smallIconWidth)
                {

                    String pid = "smalliconwarn";

                    if (viewer.getPopupById (pid) != null)
                    {

                        return;

                    }

                    QuollPopup.messageBuilder ()
                        .withViewer (viewer)
                        .popupId (pid)
                        .styleClassName (StyleClassNames.WARNING)
                        .title (userobjects,basic,edit,warnings,LanguageStrings.smallicon,LanguageStrings.popup,title)
                        .message (getUILanguageStringProperty (Arrays.asList (userobjects,basic,edit,warnings,LanguageStrings.smallicon,LanguageStrings.popup,text),
                                                               smallIconWidth))
                        .closeButton ()
                        .build ();

                }

            }

        });

        Form f = Form.builder ()
            .description (userobjects,LanguageStrings.type,_new,wizard,basic,text)
            .item (getUILanguageStringProperty (userobjects,basic,edit,labels,LanguageStrings.name),
                   this.name)
            .item (getUILanguageStringProperty (userobjects,basic,edit,labels,LanguageStrings.plural),
                   this.plural)
            .item (getUILanguageStringProperty (userobjects,basic,edit,labels,LanguageStrings.bigicon),
                   this.bigIcon)
            .item (getUILanguageStringProperty (userobjects,basic,edit,labels,LanguageStrings.smallicon),
                   this.smallIcon)
            .confirmButton (buttons,save)
            .cancelButton (buttons,cancel)
            .build ();

        f.setOnCancel (ev ->
        {

            this.close ();

        });

        f.setOnConfirm (ev ->
        {

            f.hideError ();
            ev.consume ();

            if (name.getText () == null)
            {

                f.showError (getUILanguageStringProperty (userobjects,basic,edit,errors, LanguageStrings.name,novalue));
                return;

            }

            if (plural.getText () == null)
            {

                f.showError (getUILanguageStringProperty (userobjects,basic,edit,errors, LanguageStrings.plural,novalue));
                return;

            }

            if (smallIcon.getImage () == null)
            {

                f.showError (getUILanguageStringProperty (userobjects,basic,edit,errors,LanguageStrings.smallicon,novalue));
                return;

            }

            if (bigIcon.getImage () == null)
            {

                f.showError (getUILanguageStringProperty (userobjects,basic,edit,errors,LanguageStrings.bigicon,novalue));
                return;

            }

            // Check that the name(s) aren't already in use.
            Set<UserConfigurableObjectType> types = Environment.getAssetUserConfigurableObjectTypes (false);

            for (UserConfigurableObjectType t : types)
            {

                if (t.equals (this.type))
                {

                    continue;

                }

                if (t.getObjectTypeName ().equalsIgnoreCase (name.getText ()))
                {

                    f.showError (getUILanguageStringProperty (Arrays.asList (userobjects,basic,edit,errors,LanguageStrings.name,valueexists),
                                                              name.getText ()));
                    return;

                }

                if (t.getObjectTypeNamePlural ().equalsIgnoreCase (plural.getText ()))
                {

                    f.showError (getUILanguageStringProperty (Arrays.asList (userobjects,basic,edit,errors,LanguageStrings.plural,valueexists),
                                                              plural.getText ()));
                    return;

                }

            }

            try
            {

                boolean showAdd = false;

                this.type.setObjectTypeName (name.getText ());
                this.type.setObjectTypeNamePlural (plural.getText ());
                this.type.setIcon16x16 (UIUtils.getScaledImage (smallIcon.getImage (), smallIconWidth));
                this.type.setIcon24x24 (UIUtils.getScaledImage (bigIcon.getImage (), bigIconWidth));

                if (this.type.getKey () == null)
                {

                    showAdd = true;
                    // Name
                    ObjectNameUserConfigurableObjectTypeField nameF = new ObjectNameUserConfigurableObjectTypeField ();

                    nameF.setFormName (getUILanguageStringProperty (userobjects,LanguageStrings.type,_new,defaults,fields,LanguageStrings.name).getValue ());
                    nameF.setUserConfigurableObjectType (this.type);

                    // Description
                    ObjectDescriptionUserConfigurableObjectTypeField cdescF = new ObjectDescriptionUserConfigurableObjectTypeField ();

                    cdescF.setSearchable (true);
                    cdescF.setFormName (getUILanguageStringProperty (userobjects,LanguageStrings.type,_new,defaults,fields,description).getValue ());
                    //"Description");
                    cdescF.setUserConfigurableObjectType (this.type);

                    this.type.setAssetObjectType (true);

                    this.type.setConfigurableFields (Arrays.asList (nameF, cdescF));

                }

                Environment.updateUserConfigurableObjectType (this.type);

                if ((showAdd)
                    &&
                    (this.viewer instanceof ProjectViewer)
                   )
                {

                    ((ProjectViewer) this.viewer).showAddNewAsset (new Asset (this.type));

                }

            } catch (Exception e) {

                Environment.logError ("Unable to add/edit type: " +
                                      this.type,
                                      e);

                ComponentUtils.showErrorMessage (viewer,
                                                 getUILanguageStringProperty (userobjects,LanguageStrings.type, (this.type.getKey () != null ? edit : _new),actionerror));

            }

            this.close ();

        });

        this.getChildren ().add (f);

    }

    @Override
    public QuollPopup createPopup ()
    {

        StringProperty title = null;

        if (this.type.getKey () == null)
        {

            title = getUILanguageStringProperty (userobjects,LanguageStrings.type,_new,LanguageStrings.popup,LanguageStrings.title);

        } else {

            title = getUILanguageStringProperty (Arrays.asList (userobjects,LanguageStrings.type,edit,LanguageStrings.popup,LanguageStrings.title),
                                                 this.type.getObjectTypeName ());

        }

        QuollPopup p = QuollPopup.builder ()
            .title (title)
            .styleClassName ("newuserconfigobjtype")
            .styleSheet ("newuserconfigobjtype")
            .headerIconClassName (this.type.getKey () == null ? StyleClassNames.ADD : StyleClassNames.EDIT)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (AddEditUserConfigurableObjectType.getPopupIdForType (this.type))
            .removeOnClose (true)
            .withViewer (this.viewer)
            .build ();

        p.toFront ();

        p.addEventHandler (QuollPopup.PopupEvent.SHOWN_EVENT,
                           ev ->
        {

            this.name.requestFocus ();

        });

        return p;

    }

    public static String getPopupIdForType (UserConfigurableObjectType t)
    {

        if (t.getKey () == null)
        {

            return POPUP_ID;

        } else {

            return POPUP_ID + t.getObjectReference ().asString ();

        }

    }

}
