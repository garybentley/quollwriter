package com.quollwriter.ui.fx.userobjects;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import javafx.geometry.*;
import javafx.event.*;
import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.popups.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class FieldBox extends VBox
{

    public static final EventType<Event> SAVE_SINGLE_EVENT = new EventType<> ("fieldbox.save-single");
    public static final EventType<Event> SAVE_FULL_EVENT = new EventType<> ("fieldbox.save-full");
    public static final EventType<Event> EDIT_EVENT = new EventType<> ("fieldbox.edit");
    public static final EventType<Event> CANCEL_EVENT = new EventType<> ("fieldbox.cancel");

    private UserConfigurableObjectFieldViewEditHandler handler = null;
    private UserConfigurableObjectTypeField field = null;
    private LayoutColumn column = null;
    private ProjectViewer viewer = null;
    private UserConfigurableObject object = null;
    private Set<Form.Item> inputItems = null;
    private ErrorBox errors = null;
    private VBox edit = null;
    private VBox view = null;
    private VBox config = null;
    private boolean saveObject = false;
    private Header label = null;
    private Node viewBox = null;

    public FieldBox (UserConfigurableObjectTypeField         field,
                     UserConfigurableObject                  object,
                     LayoutColumn                            column,
                     IPropertyBinder                         binder,
                     ProjectViewer                           viewer)
              throws GeneralException
    {

        this.object = object;
        this.column = column;
        this.viewer = viewer;
        this.field = field;

        this.label = Header.builder ()
            .title (field.formNameProperty ())
            .styleClassName (StyleClassNames.LABEL)
            .contextMenu (() -> this.getViewContextMenuItems ())
            .build ();
        this.label.managedProperty ().bind (this.label.visibleProperty ());

        this.label.setOnMouseClicked (ev ->
        {

            if (ev.getButton () != MouseButton.PRIMARY)
            {

                return;

            }

            if (ev.getClickCount () == 2)
            {

                this.showEditSingle ();

            }

        });

        this.getChildren ().add (this.label);

        this.handler = field.getViewEditHandler2 (object,
                                                  object.getField (field),
                                                  binder,
                                                  viewer);

        this.managedProperty ().bind (this.visibleProperty ());
        this.getStyleClass ().add (StyleClassNames.FIELD);
        this.getStyleClass ().add (this.handler.getStyleClassName ());

        if (handler instanceof ObjectNameUserConfigurableObjectFieldViewEditHandler)
        {

            this.getStyleClass ().add (StyleClassNames.OBJECTNAME);

        }

        if (handler instanceof ObjectDescriptionUserConfigurableObjectFieldViewEditHandler)
        {

            this.getStyleClass ().add (StyleClassNames.OBJECTDESCRIPTION);
/*
            VBox.setVgrow (this,
                           Priority.ALWAYS);
*/
        }
/*
        if (handler instanceof LinkedToUserConfigurableObjectFieldViewEditHandler)
        {

            VBox.setVgrow (this,
                           Priority.ALWAYS);

        }

        if (handler instanceof DocumentsUserConfigurableObjectFieldViewEditHandler)
        {

            VBox.setVgrow (this,
                           Priority.ALWAYS);

        }
*/
        if (handler instanceof ObjectImageUserConfigurableObjectFieldViewEditHandler)
        {

            this.getStyleClass ().add (StyleClassNames.OBJECTIMAGE);

        }

        this.errors = ErrorBox.builder ()
            .build ();

        this.getChildren ().add (this.errors);

        this.config = new VBox ();
        VBox.setVgrow (this.config,
                       Priority.ALWAYS);
        this.config.getStyleClass ().addAll (StyleClassNames.CONFIG);
        this.config.managedProperty ().bind (this.config.visibleProperty ());
        this.config.setVisible (false);
        this.getChildren ().add (this.config);

        this.edit = new VBox ();
        VBox.setVgrow (this.edit,
                       Priority.ALWAYS);
        this.edit.getStyleClass ().addAll (StyleClassNames.EDIT);
        this.edit.managedProperty ().bind (this.edit.visibleProperty ());
        this.edit.setVisible (false);
        this.getChildren ().add (this.edit);

        this.view = new VBox ();
        this.view.getStyleClass ().addAll (StyleClassNames.VIEW);
        this.view.managedProperty ().bind (this.view.visibleProperty ());
        this.getChildren ().add (this.view);
        VBox.setVgrow (this.view,
                       Priority.ALWAYS);
        this.view.setOnMousePressed (ev ->
        {

            if (this.view.getProperties ().get ("context-menu") != null)
            {

                ((ContextMenu) this.view.getProperties ().get ("context-menu")).hide ();

            }

        });

        this.view.setOnContextMenuRequested (ev ->
        {

            ContextMenu cm = new ContextMenu ();

            cm.getItems ().addAll (this.getViewContextMenuItems ());

            ev.consume ();

           this.view.getProperties ().put ("context-menu", cm);

           cm.setAutoFix (true);
           cm.setAutoHide (true);
           cm.setHideOnEscape (true);
           cm.show (this.view,
                   ev.getScreenX (),
                   ev.getScreenY ());

        });

    }

    private Set<MenuItem> getViewContextMenuItems ()
    {

        if (this.column == null)
        {

            return new HashSet<> ();

        }

        Set<MenuItem> its = new LinkedHashSet<> ();

        Supplier<Set<MenuItem>> itMenu = this.handler.getViewContextMenuItems ();

        if (itMenu != null)
        {

            Set<MenuItem> vits = itMenu.get ();

            if (vits != null)
            {

                its.addAll (vits);

            }

            if (its.size () > 0)
            {

                its.add (new SeparatorMenuItem ());

            }

        }

        its.add (QuollMenuItem.builder ()
            .iconName (StyleClassNames.EDIT)
            .label (getUILanguageStringProperty (assets,fields,popupmenu,items,LanguageStrings.edit))
            .onAction (eev ->
            {

                this.showEditSingle ();

            })
            .build ());

        its.add (QuollMenuItem.builder ()
            .iconName (StyleClassNames.CONFIG)
            .label (getUILanguageStringProperty (assets,fields,popupmenu,items,LanguageStrings.config))
            .onAction (eev ->
            {

                this.showConfig ();

            })
            .build ());

        its.add (QuollMenuItem.builder ()
            .iconName (StyleClassNames.DELETE)
            .label (getUILanguageStringProperty (assets,fields,popupmenu,items,delete))
            .onAction (eev ->
            {

                this.showDelete ();

            })
            .build ());

        its.add (new SeparatorMenuItem ());

        its.add (QuollMenuItem.builder ()
            .iconName (StyleClassNames.EDIT)
            .label (getUILanguageStringProperty (assets,fields,popupmenu,items,editall))
            .onAction (eev ->
            {

                this.column.editAllFields ();

            })
            .build ());

        its.add (QuollMenuItem.builder ()
            .iconName (StyleClassNames.ADD)
            .label (getUILanguageStringProperty (assets,fields,popupmenu,items,addfieldbelow))
            .onAction (eev ->
            {

                this.column.showAddNewField (this);

            })
            .build ());

        its.add (new SeparatorMenuItem ());

        its.add (QuollMenuItem.builder ()
            .iconName (StyleClassNames.EDIT)
            .label (getUILanguageStringProperty (assets,fields,popupmenu,items,editcolumntitle))
            .onAction (eev ->
            {

                this.column.showEditColumnTitle ();

            })
            .build ());

        boolean show = !this.column.getFieldsColumn ().isShowFieldLabels ();

        its.add (QuollMenuItem.builder ()
            .iconName (show ? StyleClassNames.SHOW : StyleClassNames.HIDE)
            .label (assets,fields,popupmenu,items,(show ? LanguageStrings.show : hide))
            .onAction (eev ->
            {

                this.column.getFieldsColumn ().setShowFieldLabels (show);

            })
            .build ());


        its.add (QuollMenuItem.builder ()
            .iconName (StyleClassNames.ADD)
            .label (assets,fields,popupmenu,LanguageStrings.items,addcolumn)
            .onAction (eev ->
            {

                this.column.showAddNewLayoutColumn (this.column,
                                                    null);

            })
            .build ());

        if (this.column.getPanel ().getLayoutColumns ().size () > 1)
        {

            its.add (QuollMenuItem.builder ()
                .iconName (StyleClassNames.DELETE)
                .label (getUILanguageStringProperty (assets,fields,popupmenu,items,removecolumn))
                .onAction (eev ->
                {

                    this.column.showDeleteColumn ();

                })
                .build ());

        }

        return its;

    }

    public Header getLabel ()
    {

        return this.label;

    }

    public boolean isEditing ()
    {

        return this.edit.isVisible ();

    }

    public void dispose ()
    {

        // TODO ?

    }

    public UserConfigurableObjectTypeField getTypeField ()
    {

        return this.field;

    }

    public boolean isEditVisible ()
    {

        return this.edit.isVisible ();

    }

    public void setLayoutColumn (LayoutColumn lc)
    {

        this.column = lc;

    }

    public LayoutColumn getLayoutColumn ()
    {

        return this.column;

    }

    public UserConfigurableObjectFieldViewEditHandler getHandler ()
    {

        return this.handler;

    }

    public boolean save ()
    {

        if (this.inputItems == null)
        {

            return true;

        }

        this.pseudoClassStateChanged (StyleClassNames.ERROR_PSEUDO_CLASS, false);
        this.errors.setVisible (false);
        Set<StringProperty> errs = this.handler.getInputFormItemErrors ();

        if ((errs != null)
            &&
            (errs.size () > 0)
           )
        {

            this.pseudoClassStateChanged (StyleClassNames.ERROR_PSEUDO_CLASS, true);
            this.errors.setErrors (errs);
            this.errors.setVisible (true);
            return false;

        }

        try
        {

            this.handler.updateFieldFromInput ();

            this.inputItems = null;

        } catch (Exception e) {

            Environment.logError ("Unable to get input save value for: " +
                                  this.handler.getTypeField (),
                                  e);

            return false;

        }

        return true;

    }

    public void showConfig ()
    {

        String pid = EditUserConfigurableTypeFieldPopup.getPopupIdForField (this.field);

        if (this.viewer.getPopupById (pid) != null)
        {

            return;

        }

        new EditUserConfigurableTypeFieldPopup (this.viewer,
                                                this.field,
                                                () ->
        {

            // Need to save first so the hashcode gets set correctly.
            try
            {

                Environment.updateUserConfigurableObjectTypeField (field);

            } catch (Exception e) {

                Environment.logError ("Unable to save user config object type field: " +
                                      field,
                                      e);

                ComponentUtils.showErrorMessage (this.viewer,
                                                 getUILanguageStringProperty (userobjects,LanguageStrings.fields,LanguageStrings.edit,actionerror));
                                          //"Unable to create the field.");

                return;

            }

            try
            {

                this.showView ();

            } catch (Exception e) {

                Environment.logError ("Unable to view field: " + field,
                                      e);

                ComponentUtils.showErrorMessage (this.viewer,
                                                 getUILanguageStringProperty (assets,fields,LanguageStrings.view,actionerror));

            }

        }).show (this.label,
                 Side.BOTTOM);
/*
        this.config.setVisible (true);
        this.view.setVisible (false);
        List<String> prefix = Arrays.asList (userobjects,fields,LanguageStrings.edit);
        this.config.getChildren ().clear ();

        Header h = Header.builder ()
            .title (getUILanguageStringProperty (userobjects,fields,LanguageStrings.edit,title))
            .build ();

        this.config.getChildren ().add (h);

        UserConfigurableObjectTypeFieldConfigHandler handler = this.field.getConfigHandler2 ();

        QuollTextField nameField = QuollTextField.builder ()
            .styleClassName (StyleClassNames.NAME)
            .text (this.field.getName ())
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

        Form f = fb.button (getUILanguageStringProperty (buttons,save),
                            StyleClassNames.SAVE,
                            ButtonBar.ButtonData.APPLY,
                            null)
                   .button (getUILanguageStringProperty (buttons,cancel),
                            StyleClassNames.CANCEL,
                            ButtonBar.ButtonData.CANCEL_CLOSE,
                            ev ->
                            {

                                this.showView ();

                            })
                   .build ();

        this.config.getChildren ().add (f);

        f.addEventHandler (Form.FormEvent.CONFIRM_EVENT,
                           ev ->
        {

            Set<StringProperty> errs = new LinkedHashSet<> ();

            if (nameField.getText ().trim ().equals (""))
            {

                errs.add (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.errors,LanguageStrings.name,novalue)));

            }

            // Check for the name being the same as another field.
            String formName = nameField.getText ().trim ();
            String lformName = formName.toLowerCase ();

            for (UserConfigurableObjectTypeField uf : this.field.getUserConfigurableObjectType ().getConfigurableFields ())
            {

                if ((!uf.equals (this.field))
                    &&
                    (uf.getFormName ().equalsIgnoreCase (lformName))
                   )
                {

                    errs.add (getUILanguageStringProperty (Utils.newList (prefix,LanguageStrings.errors,LanguageStrings.name,valueexists),
                                                           uf.getFormName ()));

                }

            }

            Set<StringProperty> errs2 = handler.getExtraFormItemErrors (this.field.getUserConfigurableObjectType ());

            errs.addAll (errs2);

            if (errs.size () > 0)
            {

                f.showErrors (errs);
                return;

            }

            field.setFormName (formName);

            handler.updateFromExtraFormItems ();

        });

*/
/*
xxx
        java.util.List<String> prefix = new ArrayList ();
        prefix.add (LanguageStrings.userobjects);
        prefix.add (LanguageStrings.fields);
        prefix.add (LanguageStrings.edit);

        this.editPanel.removeAll ();

        final FieldsAddEdit _this = this;

        Set<FormItem> items = new LinkedHashSet ();

        final TextFormItem name = new TextFormItem (getUIString (prefix,
                                                                             LanguageStrings.labels,
                                                                             LanguageStrings.name),
                                                                             //"Name",
                                                    field.getFormName ());

        items.add (name);

        final UserConfigurableObjectTypeFieldConfigHandler handler = field.getConfigHandler ();

        items.add (new AnyFormItem (getUIString (prefix,
                                                             LanguageStrings.labels,
                                                             LanguageStrings.name),
                                                                             //"Type",
                                    UIUtils.createInformationLabel (field.getType ().getName ())));

        Set<FormItem> nitems = handler.getExtraFormItems ();

        if (nitems == null)
        {

            nitems = new HashSet ();

        }

        items.addAll (nitems);

        Map<Form.Button, ActionListener> buttons = new LinkedHashMap ();

        buttons.put (Form.Button.save,
                     new ActionListener ()
                     {

                        @Override
                        public void actionPerformed (ActionEvent ev)
                        {

                            Form f = (Form) ev.getSource ();

                            // Check for the name being the same as another field.
                            String formName = name.getText ().trim ();
                            String lformName = formName.toLowerCase ();

                            if (formName.equals (""))
                            {

                                f.showError (getUIString (prefix,
                                                                      LanguageStrings.errors,
                                                                      LanguageStrings.name,
                                                                      LanguageStrings.novalue));
                                //f.showError ("The name of the field must be specified.");

                                UIUtils.resizeParent (f);

                                return;

                            }

                            for (UserConfigurableObjectTypeField uf : _this.type.getConfigurableFields ())
                            {

                                if (!uf.equals (field))
                                {

                                    if (uf.getFormName ().toLowerCase ().equals (lformName))
                                    {

                                        f.showError (String.format (getUIString (prefix,
                                                                                             LanguageStrings.errors,
                                                                                             LanguageStrings.name,
                                                                                             LanguageStrings.valueexists),
                                                                    //"Already have a field called <b>%s</b>.",
                                                                    uf.getFormName ()));

                                        UIUtils.resizeParent (f);

                                        return;

                                    }

                                }

                            }

                            Set<String> errs = handler.getExtraFormItemErrors (_this.type);

                            if ((errs != null)
                                &&
                                (errs.size () > 0)
                               )
                            {

                                StringBuilder b = new StringBuilder ();

                                for (String err : errs)
                                {

                                    if (b.length () > 0)
                                    {

                                        b.append ("<br />");

                                    }

                                    b.append (err);

                                }

                                f.showError (b.toString ());

                                UIUtils.resizeParent (f);

                                return;

                            }

                            field.setFormName (name.getText ().trim ());

                            handler.updateFromExtraFormItems ();

                            _this.updateField (field);

                            _this.showPanel ("view");

                        }

                     });

        buttons.put (Form.Button.cancel,
                     new ActionListener ()
                     {

                        @Override
                        public void actionPerformed (ActionEvent ev)
                        {

                            _this.showPanel ("view");

                            return;

                        }

                     });


        final Form f = new Form (Form.Layout.stacked,
                                 items,
                                 buttons);
        f.setAlignmentX (Component.LEFT_ALIGNMENT);
        f.setBorder (UIUtils.createPadding (5, 10, 5, 5));

        if (this.showFormTitles)
        {

            JComponent h = UIUtils.createBoldSubHeader (getUIString (prefix,
                                                                                 LanguageStrings.title),
                                                                                 //"Edit field",
                                                        null);

            h.setBorder (UIUtils.createPadding (3, 5, 0, 0));

            this.editPanel.add (h);

        }

        this.editPanel.add (f);

        UIUtils.doLater (new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                name.requestFocus ();

                UIUtils.resizeParent (_this);

            }

        });

        VBox conf = new VBox ();
        conf.getStyleClass ().addAll (StyleClassNames.CONFIG);
        this.getChildren ().clear ();
        this.getChildren ().add (conf);

        HBox b = new HBox ();
        VBox v = new VBox ();
        QuollLabel l = QuollLabel.builder ()
            .label (this.handler.getField ().nameProperty ())
            .build ();
        BasicHtmlTextFlow d = BasicHtmlTextFlow.builder ()
            .text (this.handler.getField ().getUserConfigurableObjectTypeField ().getConfigHandler ().getConfigurationDescription ())
            .build ();
        v.getChildren ().addAll (l, d);

        b.getChildren ().add (v);

        ToolBar tb = new ToolBar ();
        tb.getStyleClass ().add (StyleClassNames.BUTTONS);
        tb.getItems ().add (QuollButton.builder ()
            .styleClassName (StyleClassNames.EDIT)
            .onAction (ev ->
            {

                // TODO

            })
            .build ());

        tb.getItems ().add (QuollButton.builder ()
            .styleClassName (StyleClassNames.DELETE)
            .onAction (ev ->
            {

                // TODO

            })
            .build ());

        b.getChildren ().add (tb);

        conf.getChildren ().add (b);
*/
    }

    public void showView ()
                   throws GeneralException
    {

        this.inputItems = null;
        this.errors.setVisible (false);
        this.pseudoClassStateChanged (StyleClassNames.ERROR_PSEUDO_CLASS, false);
        this.edit.setVisible (false);
        this.config.setVisible (false);

        if (this.handler instanceof ObjectNameUserConfigurableObjectFieldViewEditHandler)
        {

            this.view.setVisible (false);
            return;

        }

        Set<Form.Item> its = this.handler.getViewFormItems (() ->
                                                            {

                                                                this.fireEvent (new Event (this, this, SAVE_SINGLE_EVENT));

                                                            });

        for (Form.Item it : its)
        {

            VBox b = new VBox ();
            b.getStyleClass ().add (StyleClassNames.CONTROL);
            b.getChildren ().add (it.control);
            b.managedProperty ().bind (b.visibleProperty ());
            VBox.setVgrow (b,
                           Priority.ALWAYS);

            if ((this.handler instanceof ObjectImageUserConfigurableObjectFieldViewEditHandler)
                &&
                (it.control instanceof Region)
               )
            {

                ((Region) it.control).prefWidthProperty ().bind (b.widthProperty ());
                b.prefHeightProperty ().bind (((Region) it.control).heightProperty ());

            }

            this.view.getChildren ().remove (this.viewBox);

            this.viewBox = b;

            this.view.getChildren ().add (b);

        }

        this.view.setVisible (true);
        //this.view.requestLayout ();

    }

    private void removeField ()
    {

        this.column.getFieldsColumn ().fields ().remove (this.field);

        try
        {

            Environment.removeUserConfigurableObjectTypeField (this.field);

        } catch (Exception e) {

            Environment.logError ("Unable to delete user config object field: " +
                                  this.field,
                                  e);

            ComponentUtils.showErrorMessage (this.viewer,
                                             getUILanguageStringProperty (userobjects,fields,delete,actionerror));
                                      //"Unable to remove the field.");

            return;

        }

        Environment.fireUserProjectEvent (this.field.getUserConfigurableObjectType (),
                                          ProjectEvent.Type.userobjecttype,
                                          ProjectEvent.Action.changed,
                                          this.field.getUserConfigurableObjectType ());


    }

    public void showDelete ()
    {

        String id = "deletefield" + this.field.getObjectReference ().asString ();

        if (this.viewer.getPopupById (id) != null)
        {

            return;

        }

        QuollPopup qp = QuollPopup.messageBuilder ()
            .withViewer (this.viewer)
            .styleClassName (StyleClassNames.DELETE)
            .title (assets,fields,delete,popup,title)
            .removeOnClose (true)
            .popupId (id)
            .message (getUILanguageStringProperty (Arrays.asList (assets,fields,delete,popup,text),
                                                   this.field.getUserConfigurableObjectType ().getObjectTypeNamePlural (),
                                                   this.object.nameProperty ()))
/*
            .button (QuollButton.builder ()
                .label (getUILanguageStringProperty (Arrays.asList (assets,fields,delete,popup,buttons,_this),
                                                     this.field.getUserConfigurableObjectType ().getObjectTypeName ()))
                .buttonType (ButtonBar.ButtonData.APPLY)
                .onAction (eev ->
                {

                    this.viewer.getPopupById (id).close ();

                })
                .build ())
*/
            .button (QuollButton.builder ()
                .label (getUILanguageStringProperty (Arrays.asList (assets,fields,delete,popup,buttons,confirm),
                                                     this.field.getUserConfigurableObjectType ().getObjectTypeNamePlural ()))
                .buttonType (ButtonBar.ButtonData.APPLY)
                .onAction (eev ->
                {

                    this.removeField ();

                    this.viewer.getPopupById (id).close ();

                })
                .build ())
            .button (QuollButton.builder ()
                .label (buttons,cancel)
                .onAction (eev ->
                {

                    this.viewer.getPopupById (id).close ();

                })
                .build ())
            .build ();

        viewer.showPopup (qp,
                          this.label,
                          Side.BOTTOM);

    }

    public void showEditFull ()
    {

        this.pseudoClassStateChanged (StyleClassNames.ERROR_PSEUDO_CLASS, false);
        this.errors.setVisible (false);

        try
        {

            if (this.inputItems == null)
            {

                this.inputItems = this.handler.getInputFormItems (null,
                                                                  () ->
                                                                  {

                                                                      this.fireEvent (new Event (this, this, SAVE_FULL_EVENT));

                                                                  });

            }

        } catch (Exception e) {

            Environment.logError ("Unable to edit field: " + this.field,
                                  e);

            this.pseudoClassStateChanged (StyleClassNames.ERROR_PSEUDO_CLASS, true);
            this.errors.setVisible (true);
            this.errors.setErrors (getUILanguageStringProperty (assets,fields,LanguageStrings.edit,actionerror));
            return;

        }

        this.fireEvent (new Event (this, this, EDIT_EVENT));

        this.saveObject = false;
        this.view.setVisible (false);
        this.edit.setVisible (true);
        this.edit.getChildren ().clear ();

        VBox b = new VBox ();
        b.getStyleClass ().add (StyleClassNames.FULL);
        VBox.setVgrow (b,
                       Priority.ALWAYS);
        this.edit.getChildren ().add (b);

        VBox tb = new VBox ();
        tb.getStyleClass ().add (StyleClassNames.CONTROLS);
        VBox.setVgrow (tb,
                       Priority.ALWAYS);

        for (Form.Item it : this.inputItems)
        {

           VBox cb = new VBox ();
           cb.getStyleClass ().add (StyleClassNames.CONTROL);
           cb.getChildren ().add (it.control);

           if (it.control instanceof QuollImageView)
           {

               cb.prefHeightProperty ().bind (((QuollImageView) it.control).heightProperty ());

           }

           if (it.control instanceof QuollTextArea)
           {

               VBox.setVgrow (cb,
                              Priority.ALWAYS);

           }

           tb.getChildren ().add (cb);

        }

        b.getChildren ().add (tb);

    }

    public void showEditSingle ()
    {

        this.pseudoClassStateChanged (StyleClassNames.ERROR_PSEUDO_CLASS, false);
        this.errors.setVisible (false);

        try
        {

            if (this.inputItems == null)
            {

                this.inputItems = this.handler.getInputFormItems (null,
                                                                  () ->
                                                                  {

                                                                      this.fireEvent (new Event (this, this, SAVE_SINGLE_EVENT));

                                                                  });

            }

        } catch (Exception e) {

            Environment.logError ("Unable to edit field: " + this.field,
                                  e);

            this.pseudoClassStateChanged (StyleClassNames.ERROR_PSEUDO_CLASS, true);
            this.errors.setVisible (true);
            this.errors.setErrors (getUILanguageStringProperty (assets,fields,LanguageStrings.edit,actionerror));
            return;

        }

        this.fireEvent (new Event (this, this, EDIT_EVENT));

        //this.saveObject = true;
        this.edit.setVisible (true);
        this.view.setVisible (false);

        this.edit.getChildren ().clear ();

        VBox b = new VBox ();
        VBox.setVgrow (b,
                       Priority.ALWAYS);
        b.getStyleClass ().add (StyleClassNames.SINGLE);
        this.edit.getChildren ().add (b);

        QuollButton saveB = QuollButton.builder ()
            .tooltip (getUILanguageStringProperty (form,addedit,buttons,save,tooltip))
            .iconName (StyleClassNames.SAVE)
            .buttonType (ButtonBar.ButtonData.APPLY)
            .onAction (ev ->
            {

                this.fireEvent (new Event (this, this, SAVE_SINGLE_EVENT));

            })
            .build ();

        QuollButton cancelB = QuollButton.builder ()
            .tooltip (getUILanguageStringProperty (form,addedit,buttons,cancel,tooltip))
            .iconName (StyleClassNames.CANCEL)
            .buttonType (ButtonBar.ButtonData.CANCEL_CLOSE)
            .onAction (ev ->
            {

                this.inputItems = null;

                try
                {

                    this.showView ();

                } catch (Exception e) {

                    Environment.logError ("Unable to view field: " + this.field,
                                          e);

                    ComponentUtils.showErrorMessage (this.viewer,
                                                     getUILanguageStringProperty (assets,fields,LanguageStrings.view,actionerror));

                }

                this.fireEvent (new Event (this, this, CANCEL_EVENT));

            })
            .build ();

        ToolBar bb = new ToolBar ();
        bb.getStyleClass ().add (StyleClassNames.BUTTONS);
        bb.getItems ().addAll (saveB, cancelB);

        VBox tb = new VBox ();
        tb.getStyleClass ().add (StyleClassNames.CONTROLS);
        tb.getStyleClass ().add (this.field.getType ().getType ());
        VBox.setVgrow (tb,
                       Priority.ALWAYS);

        for (Form.Item it : this.inputItems)
        {

           VBox cb = new VBox ();
           cb.getStyleClass ().add (StyleClassNames.CONTROL);
           cb.getChildren ().add (it.control);

           if (it.control instanceof QuollTextArea)
           {

               VBox.setVgrow (cb,
                              Priority.ALWAYS);

           }

           tb.getChildren ().add (cb);

           bb.maxWidthProperty ().bind (((Region) it.control).widthProperty ());
           bb.prefWidthProperty ().bind (((Region) it.control).widthProperty ());

        }

        tb.getChildren ().add (bb);

        b.getChildren ().add (tb);

    }

}
