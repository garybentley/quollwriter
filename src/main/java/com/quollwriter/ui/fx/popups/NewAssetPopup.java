package com.quollwriter.ui.fx.popups;

import java.nio.file.*;
import java.nio.charset.*;
import java.util.*;

import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.layout.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.editors.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.userobjects.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class NewAssetPopup extends PopupContent<ProjectViewer>
{

    public static final String POPUP_ID = "createasset";
    private ObjectNameUserConfigurableObjectFieldViewEditHandler nameHandler = null;
    private ObjectDescriptionUserConfigurableObjectFieldViewEditHandler descHandler = null;
    private HyperlinkLinkedToPanel linkedToPanel = null;
    private Form form = null;

    private Asset asset = null;
    private boolean displayAfterSave = false;

    public NewAssetPopup (Asset         asset,
                          ProjectViewer viewer)
    {

        super (viewer);

        this.asset = asset;

        VBox b = new VBox ();
        this.getChildren ().add (b);

        Set<Form.Item> items = new LinkedHashSet<> ();

        Runnable doSave = () ->
        {

            this.handleSave ();

        };

        UserConfigurableObjectType type = asset.getUserConfigurableObjectType ();

        this.nameHandler = type.getPrimaryNameField ().getViewEditHandler2 (asset,
                                                                            asset.getField (type.getPrimaryNameField ()),
                                                                            this.getBinder (),
                                                                            viewer);

        if (type.getObjectDescriptionField () != null)
        {

            this.descHandler = type.getObjectDescriptionField ().getViewEditHandler2 (asset,
                                                                                      asset.getField (type.getObjectDescriptionField ()),
                                                                                      this.getBinder (),
                                                                                      viewer);

        }

        items.addAll (this.nameHandler.getInputFormItems (null,
                                                          doSave));

        if (this.descHandler != null)
        {

            items.addAll (this.descHandler.getInputFormItems (null,
                                                              doSave));

        }

        if (type.getNonCoreFieldCount () > 0)
        {

            QuollHyperlink l = QuollHyperlink.builder ()
                .label (assets,add,LanguageStrings.popup,showdetail,text)
                .tooltip (getUILanguageStringProperty (Arrays.asList (assets,add,LanguageStrings.popup,showdetail,tooltip),
                                                       asset.getObjectTypeName ()))
                .styleClassName (StyleClassNames.EDIT)
                .onAction (ev ->
                {

                    try
                    {

                        asset.setName (this.nameHandler.getInputSaveValue ());

                        if (this.descHandler != null)
                        {

                            asset.setDescription (this.descHandler.getInputSaveValue ());

                        }

                        viewer.showAddNewAssetInTab (asset);

                        this.close ();

                    } catch (Exception e) {

                        Environment.logError ("Unable to init asset fields",
                                              e);

                        ComponentUtils.showErrorMessage (viewer,
                                                         getUILanguageStringProperty (assets,add,LanguageStrings.popup,errors,showdetail));

                    }

                })
                .build ();

            items.add (new Form.Item (l));

        }

        this.linkedToPanel = new HyperlinkLinkedToPanel (this.asset,
                                                         this.getBinder (),
                                                         this.viewer);

        items.add (new Form.Item (this.linkedToPanel));

        this.form = Form.builder ()
            .items (items)
            .styleClassName (StyleClassNames.NEW)
            .confirmButton (getUILanguageStringProperty (buttons,save))
            .cancelButton (getUILanguageStringProperty (buttons,cancel))
            .build ();

        this.form.addEventHandler (Form.FormEvent.CANCEL_EVENT,
                                   ev ->
        {

            this.close ();

        });

        this.form.addEventHandler (Form.FormEvent.CONFIRM_EVENT,
        ev ->
        {

            if (this.handleSave ())
            {

                this.close ();

            }

        });

        b.getChildren ().add (this.form);

    }

    public NewAssetPopup (UserConfigurableObjectType objType,
                          ProjectViewer              viewer)
    {

        this (new Asset (objType),
              viewer);

    }

    public Set<StringProperty> getFormErrors ()
    {

        Set<StringProperty> errs = new LinkedHashSet<> ();

        Set<StringProperty> nerrs = this.nameHandler.getInputFormItemErrors ();

        if (nerrs != null)
        {

            errs.addAll (nerrs);

        }

        if (this.descHandler != null)
        {

            Set<StringProperty> derrs = this.descHandler.getInputFormItemErrors ();

            if (derrs != null)
            {

                errs.addAll (derrs);

            }

        }

        return errs;

    }

    public boolean handleSave ()
    {

        this.form.hideError ();

        Set<StringProperty> err = this.getFormErrors ();

        if (err.size () > 0)
        {

            this.form.showErrors (err);
            return false;

        }

        try
        {

            this.nameHandler.updateFieldFromInput ();

        } catch (Exception e) {

            Environment.logError ("Unable to get name value from: " +
                                  this.nameHandler,
                                  e);

            ComponentUtils.showErrorMessage (this.viewer,
                                             getUILanguageStringProperty (Arrays.asList (assets,add,actionerror),
                                                                          this.asset.getObjectTypeName ()));
                                      //"Unable to add new " + this.object.getObjectTypeName () + ".");

            return false;

        }

        if (this.descHandler != null)
        {

            try
            {

                this.descHandler.updateFieldFromInput ();

            } catch (Exception e) {

                Environment.logError ("Unable to get description value from: " +
                                      this.descHandler,
                                      e);

                ComponentUtils.showErrorMessage (this.viewer,
                                                 getUILanguageStringProperty (Arrays.asList (assets,add,actionerror),
                                                                              this.asset.getObjectTypeName ()));
                                          //"Unable to add new " + this.object.getObjectTypeName () + ".");

                return false;

            }

        }

        Set<String> oldNames = this.asset.getAllNames ();

        this.asset.setProject (this.viewer.getProject ());

        try
        {

            this.asset.setLinks (this.linkedToPanel.getLinkedToPanel ().getSelected ());
            this.viewer.saveObject (this.asset,
                                    true);

            this.viewer.getProject ().addAsset (this.asset);

            this.viewer.openObjectSection (this.asset.getObjectType ());

            this.viewer.fireProjectEvent (ProjectEvent.Type.asset,
                                          ProjectEvent.Action._new,
                                          this.asset);

        } catch (Exception e)
        {

            Environment.logError ("Unable to add new: " +
                                  this.asset,
                                  e);

            ComponentUtils.showErrorMessage (this.viewer,
                                             getUILanguageStringProperty (Arrays.asList (assets,add,actionerror),
                                                                          this.asset.getObjectTypeName ()));
                                      //"Unable to add new " + this.object.getObjectTypeName () + ".");

            return false;

        }

        this.viewer.updateProjectDictionaryForNames (oldNames,
                                                     this.asset);

        if (this.displayAfterSave)
        {

            this.viewer.viewObject (this.asset);

        }

        return true;

    }

    @Override
    public QuollPopup createPopup ()
    {

        QuollPopup p = QuollPopup.builder ()
            .title (getUILanguageStringProperty (Arrays.asList (assets,add,LanguageStrings.popup,title),
                                                 Environment.getObjectTypeName (this.asset)))
            .styleClassName (StyleClassNames.CREATEASSET)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (POPUP_ID)
            .withViewer (this.viewer)
            .removeOnClose (true)
            .build ();

        p.getHeader ().getIcon ().imageProperty ().bind (this.asset.getUserConfigurableObjectType ().icon16x16Property ());

        p.requestFocus ();

        UIUtils.runLater (() ->
        {

            this.nameHandler.grabInputFocus ();

        });

        return p;

    }

}
