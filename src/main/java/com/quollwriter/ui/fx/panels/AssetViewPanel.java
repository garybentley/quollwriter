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

public class AssetViewPanel extends NamedObjectPanelContent<ProjectViewer, Asset> implements ToolBarSupported
{

    private VBox content = null;
    private SortableColumnsPanel layout = null;
    private Button headerEditBut = null;
    private Button headerSaveBut = null;
    private Button headerCancelBut = null;
    private VBox error = null;
    private BooleanProperty layoutModeEnabled = null;

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
            .styleClassName (StyleClassNames.EDIT)
            .onAction (ev ->
            {

                this.showEdit ();

            })
            .build ();
        this.headerEditBut.managedProperty ().bind (this.headerEditBut.visibleProperty ());

        this.headerSaveBut = QuollButton.builder ()
            .tooltip (getUILanguageStringProperty (Arrays.asList (assets,view,toolbar,save,tooltip),
                                                   this.object.getUserConfigurableObjectType ().nameProperty ()))
            .styleClassName (StyleClassNames.SAVE)
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

                this.save ();

            })
            .build ();
        this.headerSaveBut.managedProperty ().bind (this.headerSaveBut.visibleProperty ());
        this.headerSaveBut.setVisible (false);

        this.headerCancelBut = QuollButton.builder ()
            .tooltip (getUILanguageStringProperty (assets,view,toolbar,cancel,tooltip))
            .styleClassName (StyleClassNames.CANCEL)
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

        UIUtils.setBackgroundImage (header.getIcon (),
                                    a.getUserConfigurableObjectType ().icon24x24Property (),
                                    this.getBinder ());

        //header.getIcon ().imageProperty ().bind (a.getUserConfigurableObjectType ().icon24x24Property ());

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
        /*
        sp.viewportBoundsProperty ().addListener ((pr, oldv, newv) ->
        {

            this.layout.setMinWidth (newv.getWidth ());

        });
        */
        //this.layout.minWidthProperty ().bind (sp.widthProperty ());
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

        view.getChildren ().addAll (header, this.error, sp);

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

        this.addEventHandler (FieldBox.SAVE_EVENT,
                              ev ->
        {

            // See if any of the fields are current being edited.
            this.setHasUnsavedChanges (this.layout.areFieldsBeingEdited ());

            // Get all fields that are visible and save them.
            this.save ();

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
            .styleClassName (StyleClassNames.APPEARSINCHAPTERS)
            .onAction (ev ->
            {

                this.viewer.showAppearsInChaptersSideBarForAsset (this.object);

            })
            .build ());

        items.add (QuollMenuButton.builder ()
            .tooltip (getUILanguageStringProperty (Utils.newList (prefix,_new,tooltip)))
            .styleClassName (StyleClassNames.NEW)
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
           .styleClassName (StyleClassNames.DELETE)
           .onAction (ev ->
           {

               this.viewer.showDeleteAsset (this.object);

           })
           .build ());

       items.add (QuollButton.builder ()
          .tooltip (getUILanguageStringProperty (Utils.newList (prefix,config,tooltip),
                                                                Environment.getObjectTypeName (this.object)))
          .styleClassName (StyleClassNames.CONFIG)
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

                      this.save ();

                  });

        Panel panel = Panel.builder ()
            .title (this.object.nameProperty ())
            .content (this)
            .styleClassName (StyleClassNames.ASSET)
            .styleSheet ("viewasset")
            .panelId (this.object.getObjectReference ().asString ())
            .actionMappings (am)
            .build ();

        this.showView ();

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

        this.setHasUnsavedChanges (false);

        this.layout.showView ();
/*
TODO Remove
        Set<UserConfigurableObjectFieldViewEditHandler> handlers = this.object.getViewEditHandlers2 (this.viewer);

        this.content.getChildren ().clear ();
        Node n = this.layout.createView (handlers);
        n.getStyleClass ().add (StyleClassNames.VIEW);

        ScrollPane sp = new ScrollPane (n);
        VBox.setVgrow (sp,
                       Priority.ALWAYS);

        this.content.getChildren ().add (sp);
*/
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

        this.setHasUnsavedChanges (true);

        this.layout.showEdit ();

    }

    private void save ()
    {

        Set<String> oldNames = this.object.getAllNames ();

        if (!this.layout.updateFields ())
        {

            ComponentUtils.showErrorMessage (this.viewer,
                                             getUILanguageStringProperty (assets,save,actionerror));
                                      //"Unable to save.");

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

/*
    @Override
    public void eventOccurred (ProjectEvent ev)
    {

        if (ev.getType ().equals (ProjectEvent.USER_OBJECT_TYPE))
        {

            if (ev.getSource ().equals (this.getForObject ().getUserConfigurableObjectType ()))
            {

                this.refresh ();

            }

        }

    }
*/
    /**
     * Get the edit details panel for the specified asset, callers <b>MUST</b> call "init" to finish the creation.
     *
     * @param a The asset.
     * @param pv The project viewer.
     * @returns The edit details panel.
     * @throws GeneralException If the panel cannot be created.
     */
/*
    @Override
    public AssetDetailsEditPanel getDetailEditPanel (ProjectViewer pv,
                                                     NamedObject   n)
                                              throws GeneralException
    {

        final AssetViewPanel _this = this;

        final AssetDetailsEditPanel p = new AssetDetailsEditPanel ((Asset) n,
                                                                   pv);

        p.addActionListener (new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                if (ev.getID () == EditPanel.EDIT_VISIBLE)
                {

                    _this.setHasUnsavedChanges ();

                }

                if ((ev.getID () == EditPanel.CANCELLED) ||
                    (ev.getID () == EditPanel.VIEW_VISIBLE) ||
                    (ev.getID () == EditPanel.SAVED))
                {

                    _this.setHasUnsavedChanges ();

                }

            }

        });

        return p;

    }
*/
/*
    public static ActionListener getEditAssetAction (final ProjectViewer pv,
                                                     final Asset         a)
    {

        return new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                // Display the object then edit it.
                pv.viewObject (a,
                               new ActionListener ()
                {

                    @Override
                    public void actionPerformed (ActionEvent ev)
                    {

                        // TODO Clean up dodgy cast.
                        AssetViewPanel p = (AssetViewPanel) pv.getPanelForObject (a);

                        if (p == null)
                        {

                            Environment.logError ("Unable to edit asset: " +
                                                  a);

                            UIUtils.showErrorMessage (pv,
                                                      String.format (getUIString (assets,edit,actionerror),
                                                                     a.getObjectTypeName (),
                                                                     a.getName ()));

                            return;

                        }

                        p.editObject ();

                    }

                });

            }

        };

    }
*/
/*
    public static ActionListener getDeleteAssetAction (final ProjectViewer pv,
                                                       final Asset         a)
    {

        return new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                List<String> prefix = Arrays.asList (assets,delete,confirmpopup);

                UIUtils.createQuestionPopup (pv,
                                             String.format (getUIString (prefix,title),
                                                            a.getObjectTypeName ()),
                                             Constants.DELETE_ICON_NAME,
                                             String.format (getUIString (prefix,text),
                                                            a.getObjectTypeName (),
                                                            a.getName ()),
                                             getUIString (prefix,buttons,confirm),
                                             getUIString (prefix,buttons,cancel),
                                             //"Yes, delete it",
                                             //null,
                                             new ActionListener ()
                                             {

                                                public void actionPerformed (ActionEvent ev)
                                                {

                                                    try
                                                    {

                                                        pv.deleteObject (a);

                                                    } catch (Exception e) {

                                                        Environment.logError ("Unable to delete asset: " +
                                                                              a,
                                                                              e);

                                                        UIUtils.showErrorMessage (pv,
                                                                                  String.format (getUIString (assets,delete,actionerror),
                                                                                                 a.getObjectTypeName (),
                                                                                                 a.getName ()));

                                                    }

                                                }

                                             },
                                             null,
                                             null,
                                             null);

            }

        };

    }
*/
/*
    public ActionListener getEditObjectAction (ProjectViewer pv,
                                               NamedObject   n)
    {

        return AssetViewPanel.getEditAssetAction (pv,
                                                  (Asset) n);

    }

    public ActionListener getDeleteObjectAction (ProjectViewer pv,
                                                 NamedObject   n)
    {

        return AssetViewPanel.getDeleteAssetAction (pv,
                                                    (Asset) n);

    }

    public void doFillToolBar (ToolBar tb,
                               boolean  fullScreen)
    {

        List<String> prefix = Arrays.asList (assets,view,toolbar);

        final AssetViewPanel _this = this;

        final JButton b = UIUtils.createToolBarButton (Constants.NEW_ICON_NAME,
                                                       getUIString (prefix,_new,tooltip),
                                                       //"Click to add a new " + Environment.getObjectTypeName (QCharacter.OBJECT_TYPE) + ", " + Environment.getObjectTypeName (Location.OBJECT_TYPE) + ", " + Environment.getObjectTypeName (QObject.OBJECT_TYPE) + " etc.",
                                                       "new",
                                                       null);

        ActionAdapter ab = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                JPopupMenu m = new JPopupMenu ();

                UIUtils.addNewAssetItemsToPopupMenu (m,
                                                     b,
                                                     _this.viewer,
                                                     null,
                                                     null);

                Component c = (Component) ev.getSource ();

                m.show (c,
                        c.getX (),
                        c.getY ());

            }

        };

        b.addActionListener (ab);

        tb.add (b);

        JButton tagsb = UIUtils.createTagsMenuToolBarButton (this.obj,
                                                             this.viewer);

        tagsb.setToolTipText (String.format (getUIString (prefix,tags,tooltip),
                                        //"Click to add/remove tags for this %1$s",
                                             Environment.getObjectTypeName (this.obj)));

        tb.add (tagsb);

        tb.add (UIUtils.createToolBarButton ("delete",
                                             String.format (getUIString (prefix,delete,tooltip),
                                                            this.obj.getObjectTypeName ()),
                                             //"Click to delete this " + Environment.getObjectTypeName (this.obj.getObjectType ()),
                                             "delete",
                                             this.getDeleteObjectAction (this.viewer,
                                                                         this.obj)));

    }
*/
/*
    public void doInit ()
    {

        this.obj.addPropertyChangedListener (this);

    }

    public JComponent getBottomPanel ()
    {

        final AppearsInChaptersEditPanel ep = this.createAppearsInChaptersPanel ();
        final AssetViewPanel _this = this;

        ep.init ();

        return ep;

    }

    public AppearsInChaptersEditPanel getAppearsInChaptersEditPanel ()
    {

        return this.appearsInPanel;

    }

    private AppearsInChaptersEditPanel createAppearsInChaptersPanel ()
    {

        this.appearsInPanel = new AppearsInChaptersEditPanel (this.viewer,
                                                              this.obj);

        return this.appearsInPanel;

    }

    @Override
    public void save ()
               throws GeneralException
    {

        // TODO

    }

    @Override
    public void doRefresh ()
    {

        this.getHeader ().setIcon (this.obj.getUserConfigurableObjectType ().getIcon24x24 ());

        if (this.appearsInPanel != null)
        {

            this.appearsInPanel.refresh ();

        }

    }

    @Override
    public void fillPopupMenu (MouseEvent ev,
                               JPopupMenu popup)
    {

        List<String> prefix = Arrays.asList (assets,view,popupmenu,items);

        // Don't show the popup when we are editing the details.
        if (this.getDetailsPanel ().isEditing ())
        {

            popup.add (UIUtils.createMenuItem (getUIString (prefix,save),
                                               Constants.SAVE_ICON_NAME,
                                               this.getDetailsPanel ().getDoSaveAction ()));

            return;

        }

        final AssetViewPanel _this = this;

        popup.add (UIUtils.createMenuItem (getUIString (prefix,edit),
                                           Constants.EDIT_ICON_NAME,
                                           this.getEditObjectAction (this.viewer,
                                                                     (Asset) this.obj)));

        popup.add (UIUtils.createTagsMenu (this.obj,
                                           this.viewer));

        popup.add (UIUtils.createMenuItem (getUIString (prefix,addfileordocument),
                                           //"Add File/Document",
                                           Constants.ADD_ICON_NAME,
                                           new ActionListener ()
                                           {

                                                @Override
                                                public void actionPerformed (ActionEvent ev)
                                                {

                                                    _this.getObjectDocumentsEditPanel ().showAddDocument ();

                                                }

                                           }));

        popup.add (UIUtils.createMenuItem (getUIString (prefix,delete),
                                           //"Delete",
                                           Constants.DELETE_ICON_NAME,
                                           this.getDeleteObjectAction (this.viewer,
                                                                       (Asset) this.obj)));

        popup.addSeparator ();

        popup.add (UIUtils.createMenuItem (String.format (getUIString (prefix,editobjecttypeinfo),
                                                          //"Edit the %s information/fields",
                                                          this.obj.getObjectTypeName ()),
                                           Constants.EDIT_ICON_NAME,
                                           new ActionListener ()
                                           {

                                                 @Override
                                                 public void actionPerformed (ActionEvent ev)
                                                 {

                                                     UIUtils.showObjectTypeEdit (_this.obj.getUserConfigurableObjectType (),
                                                                                 _this.viewer);

                                                 }

                                           }));

    }
*/
}
