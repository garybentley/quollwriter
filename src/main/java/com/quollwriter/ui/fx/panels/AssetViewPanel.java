package com.quollwriter.ui.fx.panels;

import java.util.*;

import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.data.*;

import com.quollwriter.ui.userobjects.*;

import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.Environment.getUIString;

public class AssetViewPanel extends NamedObjectPanelContent<ProjectViewer, Asset>
{

    private SplitPane mainSplitPane = null;
    private SplitPane leftSplitPane = null;
    private SplitPane rightSplitPane = null;

    public AssetViewPanel (ProjectViewer pv,
                           Asset         a)
                   throws GeneralException
    {

        super (pv,
               a);

        this.getStyleClass ().add (a.getUserConfigurableObjectType ().getObjectReference ().asString ());

        Header header = Header.builder ()
            .title (a.nameProperty ())
            .styleClassName (a.getUserConfigurableObjectType ().getObjectType ()) // TODO ?
            .build ();

        this.mainSplitPane = new SplitPane ();
        this.mainSplitPane.setOrientation (Orientation.HORIZONTAL);
        this.leftSplitPane = new SplitPane ();
        this.leftSplitPane.setOrientation (Orientation.VERTICAL);
        this.rightSplitPane = new SplitPane ();
        this.rightSplitPane.setOrientation (Orientation.VERTICAL);

        this.mainSplitPane.getItems ().addAll (this.leftSplitPane, this.rightSplitPane);

        this.leftSplitPane.getItems ().addAll (new AssetDetailsPanel (this.object), new AppearsInChaptersPanel (this.object));

        this.rightSplitPane.getItems ().addAll (new DocumentsPanel (this.object), new LinkedToPanel (this.object, this.getBinder (), pv));

        this.getChildren ().addAll (header, this.mainSplitPane);

    }

    @Override
    public Panel createPanel ()
    {

        Panel panel = Panel.builder ()
            // TODO .title (this.object.nameProperty ())
            .title (new javafx.beans.property.SimpleStringProperty (this.object.getName ()))
            .content (this)
            .styleClassName (StyleClassNames.ASSET)
            .panelId (this.object.getObjectReference ().asString ())
            // TODO .headerControls ()
            .toolbar (() ->
            {

                return new LinkedHashSet<Node> ();

            })
            .build ();

        return panel;

    }

    @Override
    public void init (State s)
    {

        // TODO Change to convert the width px value to a relative % value.

        try
        {

            int v = s.getAsInt (Constants.ASSET_MAIN_SPLIT_PANE_DIVIDER_LOCATION_PROPERTY_NAME,
                                -1);

            if (v <= 0)
            {

                return;

            }

            this.mainSplitPane.setDividerPositions (v);

        } catch (Exception e)
        {

            return;

        }

        try
        {

            int v = s.getAsInt (Constants.ASSET_LEFT_SPLIT_PANE_DIVIDER_LOCATION_PROPERTY_NAME,
                                -1);

            if (v > 0)
            {

                this.leftSplitPane.setDividerPositions (v);

            }

        } catch (Exception e)
        {

        }

        try
        {

            int v = s.getAsInt (Constants.ASSET_RIGHT_SPLIT_PANE_DIVIDER_LOCATION_PROPERTY_NAME,
                                -1);

            if (v > 0)
            {

                this.rightSplitPane.setDividerPositions (v);

            }

        } catch (Exception e)
        {

        }

    }

    public State getState ()
    {

        State s = super.getState ();

        // TODO Convert a width px value?
        s.set (Constants.ASSET_MAIN_SPLIT_PANE_DIVIDER_LOCATION_PROPERTY_NAME,
               this.mainSplitPane.getDividerPositions ()[0]);
        s.set (Constants.ASSET_LEFT_SPLIT_PANE_DIVIDER_LOCATION_PROPERTY_NAME,
               this.leftSplitPane.getDividerPositions ()[0]);
        s.set (Constants.ASSET_RIGHT_SPLIT_PANE_DIVIDER_LOCATION_PROPERTY_NAME,
               this.rightSplitPane.getDividerPositions ()[0]);

        return s;

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
