package com.quollwriter.ui.fx.panels;

import java.util.*;
import java.util.stream.*;

import javafx.beans.property.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.image.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.userobjects.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.popups.*;
import com.quollwriter.ui.fx.components.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class NewAssetPanel extends NamedObjectPanelContent<ProjectViewer, Asset>
{

    private VBox content = null;
    private SortableColumnsPanel layout = null;
    private Button headerSaveBut = null;
    private Button headerCancelBut = null;
    private VBox error = null;
    private UserConfigurableObjectFieldViewEditHandler nameFieldHandler = null;
    private UserConfigurableObjectField nameField = null;
    private ErrorBox nameErrorBox = null;

    public NewAssetPanel (ProjectViewer pv,
                          Asset         a)
                   throws GeneralException
    {

        super (pv,
               a);

        this.getStyleClass ().addAll (StyleClassNames.ASSET, StyleClassNames.NEW);
        this.getStyleClass ().add (a.getUserConfigurableObjectType ().getObjectReference ().asString ());

        this.headerSaveBut = QuollButton.builder ()
            .tooltip (assets,add,LanguageStrings.panel,buttons,save,tooltip)
            .styleClassName (StyleClassNames.SAVE)
            .onAction (ev ->
            {

                this.save ();

            })
            .build ();

        this.headerCancelBut = QuollButton.builder ()
            .tooltip (assets,add,LanguageStrings.panel,buttons,cancel,tooltip)
            .styleClassName (StyleClassNames.CANCEL)
            .onAction (ev ->
            {

                // Remove the panel...
                this.setHasUnsavedChanges (false);

                this.viewer.closePanel (this,
                                        null);

            })
            .build ();

        Set<Node> items = new LinkedHashSet<> ();
        items.add (this.headerSaveBut);
        items.add (this.headerCancelBut);

        VBox view = new VBox ();
        VBox.setVgrow (view,
                       Priority.ALWAYS);

        Header header = Header.builder ()
            .title (getUILanguageStringProperty (Arrays.asList (assets,add,LanguageStrings.panel,title),
                                                 a.getUserConfigurableObjectType ().objectTypeNameProperty ()))
            .styleClassName (a.getUserConfigurableObjectType ().getObjectType ())
            .controls (items)
            .build ();

        header.getIcon ().imageProperty ().bind (a.getUserConfigurableObjectType ().icon24x24Property ());

        VBox box = new VBox ();

        ObjectNameUserConfigurableObjectTypeField nameTypeField = this.object.getUserConfigurableObjectType ().getPrimaryNameField ();

        this.nameField = this.object.getField (nameTypeField);

        this.nameFieldHandler = nameTypeField.getViewEditHandler2 (this.object,
                                                                   nameField,
                                                                   this.getBinder (),
                                                                   this.viewer);

        VBox nbox = new VBox ();
        nbox.getStyleClass ().addAll (StyleClassNames.OBJECTNAME, StyleClassNames.FIELD);

        nbox.getChildren ().add (QuollLabel.builder ()
            .label (nameTypeField.formNameProperty ())
            .build ());

        Runnable doSave = () ->
        {

            this.save ();

        };

        this.nameErrorBox = ErrorBox.builder ()
            .build ();

        nbox.getChildren ().add (this.nameErrorBox);

        Set<Form.Item> its = nameFieldHandler.getInputFormItems (null, doSave);

        for (Form.Item it : its)
        {

            VBox _b = new VBox ();
            _b.getStyleClass ().add (StyleClassNames.CONTROL);
            _b.getChildren ().add (it.control);

            nbox.getChildren ().add (_b);

        }

        this.layout = new SortableColumnsPanel (a,
                                                this.getBinder (),
                                                this.viewer);
        VBox.setVgrow (box,
                       Priority.ALWAYS);
        VBox.setVgrow (this.layout,
                       Priority.ALWAYS);
        box.getChildren ().addAll (nbox, this.layout);

        ScrollPane sp = new ScrollPane (box);
        VBox.setVgrow (sp,
                       Priority.ALWAYS);
        sp.vvalueProperty ().addListener ((pr, oldv, newv) ->
        {

           header.pseudoClassStateChanged (StyleClassNames.SCROLLING_PSEUDO_CLASS, newv.doubleValue () > 0);

        });

        view.getChildren ().addAll (header, sp);

        this.getChildren ().add (view);

        this.setHasUnsavedChanges (true);
        this.layout.showEdit ();

    }

    @Override
    public ObjectProperty<Image> iconProperty ()
    {

        return this.object.getUserConfigurableObjectType ().icon16x16Property ();

    }

    @Override
    public Panel createPanel ()
    {

        Map<KeyCombination, Runnable> am = new HashMap<> ();

        am.put (new KeyCodeCombination (KeyCode.S,
                                        KeyCombination.SHORTCUT_DOWN),
                () ->
                {

                  this.save ();

                });

        Panel panel = Panel.builder ()
            .title (getUILanguageStringProperty (Arrays.asList (assets,add,LanguageStrings.panel,title),
                                                 this.object.getUserConfigurableObjectType ().objectTypeNameProperty ()))
            .content (this)
            .styleClassName (StyleClassNames.ASSET)
            .panelId (this.object.getUserConfigurableObjectType ().getObjectReference ().asString ())
            .actionMappings (am)
            .build ();
        panel.getStyleClass ().add (StyleClassNames.NEW);

        return panel;

    }

    @Override
    public void init (State s)
    {

        if (s != null)
        {

            this.layout.init (s.getAsState ("layout"));

        }

    }

    public State getState ()
    {

        State s = super.getState ();

        s.set ("layout",
               this.layout.getState ());

        return s;

    }

    @Override
    public void saveObject ()
    {

        this.save ();

    }

    private boolean save ()
    {

        this.nameErrorBox.setVisible (false);
        this.lookup (".objectname .control").pseudoClassStateChanged (StyleClassNames.ERROR_PSEUDO_CLASS, false);
        Set<StringProperty> nameErrs = this.nameFieldHandler.getInputFormItemErrors ();

        if ((nameErrs != null)
            &&
            (nameErrs.size () > 0)
           )
        {

            this.nameErrorBox.setErrors (nameErrs);
            this.nameErrorBox.setVisible (true);

            this.lookup (".objectname .control").pseudoClassStateChanged (StyleClassNames.ERROR_PSEUDO_CLASS, true);
            return false;

        }

        if (!this.layout.updateFields ())
        {

            return false;

        }

        try
        {

            this.nameFieldHandler.updateFieldFromInput ();

        } catch (Exception e) {

            Environment.logError ("Unable to add asset for type: " + this.object.getUserConfigurableObjectType (),
                                  e);

            ComponentUtils.showErrorMessage (this.viewer,
                                             getUILanguageStringProperty (assets,add,actionerror));

            return false;

        }

        String name = this.object.getName ();

        try
        {

            this.object.setProject (this.viewer.getProject ());

            // Save first so that it has a key.
            super.saveObject ();

            this.viewer.getProject ().addAsset (this.object);

        } catch (Exception e)
        {

            Environment.logError ("Unable to save: " +
                                  this.object,
                                  e);

            ComponentUtils.showErrorMessage (this.viewer,
                                             getUILanguageStringProperty (assets,add,actionerror));

        }

        this.setHasUnsavedChanges (false);

        this.viewer.openObjectSection (this.object);

        this.viewer.fireProjectEvent (ProjectEvent.Type.asset,
                                      ProjectEvent.Action._new,
                                      this.object);

        this.viewer.closePanel (this,
                                null);

        this.viewer.viewAsset (this.object,
                               null);

        this.viewer.updateProjectDictionaryForNames (this.object.getAllNames (),
                                                     this.object);

        return true;

    }

}
