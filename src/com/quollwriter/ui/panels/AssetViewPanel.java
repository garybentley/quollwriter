package com.quollwriter.ui.panels;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.lang.reflect.*;

import java.text.*;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Arrays;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.tree.*;

import com.gentlyweb.properties.*;

import com.gentlyweb.utils.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.events.*;

import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.userobjects.*;
//import com.quollwriter.ui.components.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.QPopup;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public class AssetViewPanel extends AbstractObjectViewPanel<ProjectViewer, Asset> implements PropertyChangedListener, ProjectEventListener
{

    private AppearsInChaptersEditPanel appearsInPanel = null;

    public AssetViewPanel (ProjectViewer pv,
                           Asset         a)
                   throws GeneralException
    {

        super (pv,
               a);

        Environment.addUserProjectEventListener (this);

    }

    @Override
    public ImageIcon getIcon (int type)
    {

        int w = Environment.getIconPixelWidthForType (type);

        ImageIcon im = null;

        if (w == 24)
        {

            im = this.getForObject ().getUserConfigurableObjectType ().getIcon24x24 ();

        }

        if (w == 16)
        {

            im = this.getForObject ().getUserConfigurableObjectType ().getIcon16x16 ();

        }

        return im;

    }

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

    /**
     * Get the edit details panel for the specified asset, callers <b>MUST</b> call "init" to finish the creation.
     *
     * @param a The asset.
     * @param pv The project viewer.
     * @returns The edit details panel.
     * @throws GeneralException If the panel cannot be created.
     */
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

                    _this.setHasUnsavedChanges (p,
                                                true);

                }

                if ((ev.getID () == EditPanel.CANCELLED) ||
                    (ev.getID () == EditPanel.VIEW_VISIBLE) ||
                    (ev.getID () == EditPanel.SAVED))
                {

                    _this.setHasUnsavedChanges (p,
                                                false);

                }

            }

        });

        return p;

    }

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

                        AssetViewPanel p = (AssetViewPanel) pv.getQuollPanelForObject (a);

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

    public void doFillToolBar (JToolBar tb,
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

    public void doInit ()
    {

        this.obj.addPropertyChangedListener (this);

    }

    @Override
    public void propertyChanged (PropertyChangedEvent ev)
    {

    /*
        EditPanel edPan = this.getDetailsPanel ();

        final Set<String> objChangeEventTypes = edPan.getObjectChangeEventTypes ();

        if ((objChangeEventTypes == null)
            ||
            (objChangeEventTypes.size () == 0)
           )
        {

            return;

        }

        boolean update = false;

        for (String t : objChangeEventTypes)
        {

            if (ev.getChangeType ().equals (t))
            {

                update = true;

                break;

            }

        }

        if (!update)
        {

            return;

        }

        this.refresh ();
*/
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
    public void doRefresh ()
    {

        this.getHeader ().setIcon (this.obj.getUserConfigurableObjectType ().getIcon24x24 ());

        if (this.appearsInPanel != null)
        {

            this.appearsInPanel.refresh ();

        }

    }

    @Override
    public void close ()
    {

        this.obj.removePropertyChangedListener (this);

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

}
