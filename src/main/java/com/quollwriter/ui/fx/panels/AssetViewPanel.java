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

public class AssetViewPanel extends NamedObjectPanelContent<ProjectViewer, Asset> implements ToolBarSupported
{

    private VBox content = null;
    private SortableColumnsPanel layout = null;
    private Button headerEditBut = null;
    private Button headerSaveBut = null;
    private Button headerCancelBut = null;
    private VBox error = null;
    private BooleanProperty layoutModeEnabled = null;
    private FieldBox nameFieldBox = null;

    public AssetViewPanel (ProjectViewer pv,
                           Asset         a)
                   throws GeneralException
    {

        super (pv,
               a);

        this.getBackgroundPane ().setDragImportAllowed (true);
        this.layoutModeEnabled = new SimpleBooleanProperty (false);

        this.getStyleClass ().add (StyleClassNames.ASSET);
        this.getStyleClass ().add (a.getUserConfigurableObjectType ().getObjectReference ().asString ());

        this.headerEditBut = QuollButton.builder ()
            .tooltip (getUILanguageStringProperty (Arrays.asList (assets,view,toolbar,edit,tooltip),
                                                   this.object.getUserConfigurableObjectType ().nameProperty ()))
            .iconName (StyleClassNames.EDIT)
            .onAction (ev ->
            {

                this.showEdit ();

            })
            .build ();
        this.headerEditBut.managedProperty ().bind (this.headerEditBut.visibleProperty ());

        this.headerSaveBut = QuollButton.builder ()
            .tooltip (getUILanguageStringProperty (Arrays.asList (assets,view,toolbar,save,tooltip),
                                                   this.object.getUserConfigurableObjectType ().nameProperty ()))
            .iconName (StyleClassNames.SAVE)
            .onAction (ev ->
            {

                if (this.layoutModeEnabled.getValue ())
                {

                    // We are in layout.
                    this.layoutModeEnabled.setValue (false);
                    this.getStyleClass ().remove (StyleClassNames.LAYOUT);
                    this.headerSaveBut.setVisible (false);
                    this.headerEditBut.setVisible (true);
                    this.headerCancelBut.setVisible (false);

                }

                this.saveFull ();

            })
            .build ();
        this.headerSaveBut.managedProperty ().bind (this.headerSaveBut.visibleProperty ());
        this.headerSaveBut.setVisible (false);

        this.headerCancelBut = QuollButton.builder ()
            .tooltip (getUILanguageStringProperty (assets,view,toolbar,cancel,tooltip))
            .iconName (StyleClassNames.CANCEL)
            .onAction (ev ->
            {

                this.cancelEdit ();

            })
            .build ();
        this.headerCancelBut.managedProperty ().bind (this.headerCancelBut.visibleProperty ());
        this.headerCancelBut.setVisible (false);

        Set<Node> items = new LinkedHashSet<> ();
        items.add (this.headerEditBut);
        items.add (this.headerSaveBut);
        items.add (this.headerCancelBut);

        VBox view = new VBox ();
        VBox.setVgrow (view,
                       Priority.ALWAYS);

        Header header = Header.builder ()
            .title (a.nameProperty ())
            .styleClassName (a.getUserConfigurableObjectType ().getObjectType ())
            .controls (items)
            .build ();

        header.setOnMouseClicked (ev ->
        {

            if (ev.getButton () != MouseButton.PRIMARY)
            {

                return;

            }

            if (ev.getClickCount () == 2)
            {

                String pid = RenameAssetPopup.getPopupIdForAsset (this.object);

                if (this.viewer.getPopupById (pid) != null)
                {

                    return;

                }

                QuollPopup qp = new RenameAssetPopup (this.viewer,
                                                      this.object).getPopup ();

                this.viewer.showPopup (qp,
                                       header,
                                       Side.BOTTOM);

            }

        });

        header.getIcon ().setImage (a.getUserConfigurableObjectType ().icon24x24Property (),
                                    this.getBinder ());

        ObjectNameUserConfigurableObjectTypeField nameTypeField = this.object.getUserConfigurableObjectType ().getPrimaryNameField ();

        this.nameFieldBox = new FieldBox (nameTypeField,
                                          this.object,
                                          null,
                                          this.getBinder (),
                                          this.viewer);

        this.nameFieldBox.showEditFull ();
        this.nameFieldBox.setVisible (false);

        this.error = new VBox ();
        this.error.getStyleClass ().add (StyleClassNames.ERROR);
        this.error.managedProperty ().bind (this.error.visibleProperty ());
        this.error.setVisible (false);

        this.layout = new SortableColumnsPanel (a,
                                                this.getBinder (),
                                                this.viewer);
        VBox.setVgrow (this.layout,
                       Priority.ALWAYS);

        ScrollPane sp = new ScrollPane (this.layout);
        sp.setPannable (true);
        sp.setOnDragOver (ev ->
        {

            // TODO This needs more work.
            double diffy = 1d - ((sp.getViewportBounds ().getHeight () - 10) / sp.getViewportBounds ().getHeight ());
            double diffx = 1d - ((sp.getViewportBounds ().getWidth () - 10) / sp.getViewportBounds ().getWidth ());

            if (ev.getY () <= 100)
            {

                sp.setVvalue (sp.getVvalue () - diffy);

            }

            if (ev.getY () >= sp.getViewportBounds ().getHeight () - 100)
            {

                sp.setVvalue (sp.getVvalue () + diffy);

            }

            if (ev.getX () <= 100)
            {

                sp.setHvalue (sp.getHvalue () - diffx);

            }

            if (ev.getX () >= sp.getViewportBounds ().getWidth () - 100)
            {

                sp.setHvalue (sp.getHvalue () + diffx);

            }

        });
        VBox.setVgrow (sp,
                       Priority.ALWAYS);
        sp.vvalueProperty ().addListener ((pr, oldv, newv) ->
        {

           header.pseudoClassStateChanged (StyleClassNames.SCROLLING_PSEUDO_CLASS, newv.doubleValue () > 0);

        });

        view.getChildren ().addAll (header, this.error, this.nameFieldBox, sp);

        this.getChildren ().add (view);

        this.addEventHandler (FieldBox.EDIT_EVENT,
                              ev ->
        {

            this.setHasUnsavedChanges (true);

        });

        this.addEventHandler (LayoutColumn.EDIT_COLUMN_EVENT,
                              ev ->
        {

            this.headerEditBut.setVisible (false);
            this.headerSaveBut.setVisible (true);
            this.headerCancelBut.setVisible (true);

            this.setHasUnsavedChanges (true);

        });

        this.addEventHandler (FieldBox.SAVE_SINGLE_EVENT,
                              ev ->
        {

            if (ev.getTarget () instanceof FieldBox)
            {

                this.saveSingle ((FieldBox) ev.getTarget ());
                this.setHasUnsavedChanges (this.layout.areFieldsBeingEdited ());

            }

        });

        this.addEventHandler (FieldBox.SAVE_FULL_EVENT,
                              ev ->
        {

            // See if any of the fields are current being edited.
            this.setHasUnsavedChanges (this.layout.areFieldsBeingEdited ());

            // Get all fields that are visible and save them.
            this.saveFull ();

        });

        this.addEventHandler (FieldBox.CANCEL_EVENT,
                              ev ->
        {

            // See if any of the fields are current being edited.
            this.setHasUnsavedChanges (this.layout.areFieldsBeingEdited ());

        });

        this.setReadyForUse ();

    }

    @Override
    public ObjectProperty<Image> iconProperty ()
    {

        return this.object.getUserConfigurableObjectType ().icon16x16Property ();

    }

    @Override
    public Set<Node> getToolBarItems ()
    {

        List<String> prefix = Arrays.asList (assets,view,toolbar);

        Set<Node> items = new LinkedHashSet<> ();

        items.add (QuollButton.builder ()
            .tooltip (getUILanguageStringProperty (Utils.newList (prefix,appearsinchapters,tooltip),
                                                   Environment.getObjectTypeName (this.object)))
            .iconName (StyleClassNames.APPEARSINCHAPTERS)
            .onAction (ev ->
            {

                this.viewer.showAppearsInChaptersSideBarForAsset (this.object);

            })
            .build ());

        items.add (QuollMenuButton.builder ()
            .tooltip (getUILanguageStringProperty (Utils.newList (prefix,_new,tooltip)))
            .iconName (StyleClassNames.NEW)
            .items (() ->
            {

                return UIUtils.getNewAssetMenuItems (this.viewer);

            })
            .build ());

        items.add (UIUtils.createTagsMenuButton (this.object,
                                                 getUILanguageStringProperty (Utils.newList (prefix,tags,tooltip),
                                                                              Environment.getObjectTypeName (this.object)),
                                                 this.viewer));

        items.add (QuollButton.builder ()
           .tooltip (getUILanguageStringProperty (Utils.newList (prefix,delete,tooltip),
                                                                 Environment.getObjectTypeName (this.object)))
           .iconName (StyleClassNames.DELETE)
           .onAction (ev ->
           {

               this.viewer.showDeleteAsset (this.object);

           })
           .build ());

       items.add (QuollButton.builder ()
          .tooltip (getUILanguageStringProperty (Utils.newList (prefix,config,tooltip),
                                                                Environment.getObjectTypeName (this.object)))
          .iconName (StyleClassNames.CONFIG)
          .onAction (ev ->
          {

              this.viewer.showEditUserConfigurableType (this.object.getUserConfigurableObjectType ());

          })
          .build ());

        return items;

    }

    @Override
    public Panel createPanel ()
    {

        Map<KeyCombination, Runnable> am = new HashMap<> ();

        am.put (new KeyCodeCombination (KeyCode.E,
                                        KeyCombination.SHORTCUT_DOWN),
                () ->
                {

                    // Do edit.
                    this.showEdit ();

                });

          am.put (new KeyCodeCombination (KeyCode.S,
                                          KeyCombination.SHORTCUT_DOWN),
                  () ->
                  {

                      this.saveFull ();

                  });

        Panel panel = Panel.builder ()
            .title (this.object.nameProperty ())
            .content (this)
            .styleClassName (StyleClassNames.ASSET)
            .styleSheet ("viewasset")
            .panelId (this.object.getObjectReference ().asString ())
            .actionMappings (am)
            .build ();

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

    public void showView ()
    {

        this.headerEditBut.setVisible (true);
        this.headerSaveBut.setVisible (false);
        this.headerCancelBut.setVisible (false);
        this.nameFieldBox.setVisible (false);

        try
        {

            this.nameFieldBox.showView ();

        } catch (Exception e) {

            // Will this EVER happen?
            Environment.logError ("Unable to show view",
                                  e);

        }

        this.setHasUnsavedChanges (false);

        this.layout.showView ();

    }

    public void cancelEdit ()
    {

        // TODO?
/*
        List<String> prefix = new ArrayList ();
        prefix.add (LanguageStrings.assets);
        prefix.add (LanguageStrings.save);
        prefix.add (LanguageStrings.cancel);
        prefix.add (LanguageStrings.popup);

        final AssetDetailsEditPanel _this = this;

        boolean changed = false;

        // Check for any field having changes.

        if (!changed)
        {

            if ((changed)
                &&
                (!this.changeConfirm)
               )
            {

                UIUtils.createQuestionPopup (this.viewer,
                                             Environment.getUIString (prefix,
                                                                      LanguageStrings.title),
                                             //"Discard changes?",
                                             Constants.HELP_ICON_NAME,
                                             Environment.getUIString (prefix,
                                                                      LanguageStrings.text),
                                             //"You have made some changes, do you want to discard them?",
                                             Environment.getUIString (prefix,
                                                                      LanguageStrings.buttons,
                                                                      LanguageStrings.confirm),
                                             //"Yes, discard them",
                                             Environment.getUIString (prefix,
                                                                      LanguageStrings.buttons,
                                                                      LanguageStrings.cancel),
                                             //"No, keep them",
                                             // On confirm
                                             new ActionListener ()
                                             {

                                                 @Override
                                                 public void actionPerformed (ActionEvent ev)
                                                 {

                                                     _this.changeConfirm = true;

                                                     _this.doCancel ();

                                                 }

                                             },
                                             // On cancel
                                             null,
                                             null,
                                             null);

                return false;

            }

        }

        this.changeConfirm = false;
*/
        this.showView ();

    }

    public void showEdit ()
    {

        this.headerEditBut.setVisible (false);
        this.headerSaveBut.setVisible (true);
        this.headerCancelBut.setVisible (true);
        this.nameFieldBox.setVisible (true);
        this.nameFieldBox.showEditFull ();

        this.setHasUnsavedChanges (true);

        this.layout.showEdit ();

    }

    private void saveSingle (FieldBox fb)
    {

        Set<String> oldNames = this.object.getAllNames ();

        if (fb.save ())
        {

            try
            {

                this.viewer.saveObject (this.object,
                                        true);

                this.viewer.fireProjectEvent (ProjectEvent.Type.asset,
                                              ProjectEvent.Action.edit,
                                              this.object);

                fb.showView ();

            } catch (Exception e)
            {

                Environment.logError ("Unable to save: " +
                                      this.object,
                                      e);

                ComponentUtils.showErrorMessage (this.viewer,
                                                 getUILanguageStringProperty (assets,save,actionerror));
                                          //"Unable to save.");

                return;

            }

            this.viewer.updateProjectDictionaryForNames (oldNames,
                                                         this.object);

        }

    }

    private void saveFull ()
    {

        Set<String> oldNames = this.object.getAllNames ();

        boolean hasErrors = false;

        if (!this.nameFieldBox.save ())
        {

            hasErrors = true;

        }

        if (!this.layout.updateFields ())
        {

            hasErrors = true;

        }

        if (hasErrors)
        {

            return;

        }

        try
        {

            this.viewer.saveObject (this.object,
                                    true);

            this.viewer.fireProjectEvent (ProjectEvent.Type.asset,
                                          ProjectEvent.Action.edit,
                                          this.object);

        } catch (Exception e)
        {

            Environment.logError ("Unable to save: " +
                                  this.object,
                                  e);

            ComponentUtils.showErrorMessage (this.viewer,
                                             getUILanguageStringProperty (assets,save,actionerror));
                                      //"Unable to save.");

            return;

        }

        this.viewer.updateProjectDictionaryForNames (oldNames,
                                                     this.object);

        this.showView ();

    }

}
