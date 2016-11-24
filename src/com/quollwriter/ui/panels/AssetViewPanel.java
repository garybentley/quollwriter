package com.quollwriter.ui.panels;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.lang.reflect.*;

import java.text.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
//import com.quollwriter.ui.components.*;
import com.quollwriter.ui.components.ActionAdapter;

public class AssetViewPanel extends AbstractObjectViewPanel<ProjectViewer> implements PropertyChangedListener
{

    private static Map<Class, Class> detailsEditPanels = new HashMap ();

    static
    {

        Map m = AssetViewPanel.detailsEditPanels;

        m.put (ResearchItem.class,
               ResearchItemDetailsEditPanel.class);
        m.put (Location.class,
               LocationDetailsEditPanel.class);
        m.put (QObject.class,
               ObjectDetailsEditPanel.class);
        m.put (QCharacter.class,
               CharacterDetailsEditPanel.class);

    }

    private AppearsInChaptersEditPanel appearsInPanel = null;

    public AssetViewPanel (ProjectViewer pv,
                           Asset         a)
                   throws GeneralException
    {

        super (pv,
               a);

    }

    public static DetailsEditPanel getEditDetailsPanel (Asset         a,
                                                        ProjectViewer pv)
                                                 throws GeneralException
    {

        DetailsEditPanel adep = null;

        try
        {

            Class c = AssetViewPanel.detailsEditPanels.get (a.getClass ());

            Constructor con = c.getConstructor (a.getClass (),
                                                ProjectViewer.class);

            adep = (DetailsEditPanel) con.newInstance (a,
                                                       pv);

            // adep.init ();

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to create asset details edit panel for object: " +
                                        a,
                                        e);

        }

        return adep;

    }

    /**
     * Get the edit details panel for the specified asset, callers <b>MUST</b> call "init" to finish the creation.
     *
     * @param a The asset.
     * @param pv The project viewer.
     * @returns The edit details panel.
     * @throws GeneralException If the panel cannot be created.
     */
    public DetailsEditPanel getDetailEditPanel (ProjectViewer pv,
                                                NamedObject   n)
                                         throws GeneralException
    {

        return AssetViewPanel.getEditDetailsPanel ((Asset) n,
                                                   pv);

    }

    public String getTitle ()
    {

        return this.obj.getName ();

    }

    public String getIconType ()
    {

        return this.obj.getObjectType ();

    }
/*
    public Color getHeaderColor ()
    {

        return AssetViewPanel.headerColors.get (this.obj.getClass ());

    }
*/
    public static ActionListener getEditAssetAction (final ProjectViewer pv,
                                                     final Asset         a)
    {

        if (!UserProperties.getAsBoolean (Constants.EDIT_ASSETS_IN_POPUP_PROPERTY_NAME))
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
                            
                            p.editObject ();
                            
                        }
                        
                    });
                    
                }
                
            };
            
        } else {
    
            // Put the edit panel into a popup.
            AbstractActionHandler aah = new AssetActionHandler (a,
                                                                pv,
                                                                AbstractActionHandler.EDIT);
    
            return aah;

        }
            
    }

    public static ActionListener getRenameAssetAction (final ProjectViewer pv,
                                                       final Asset         a)
    {

        return new RenameAssetActionHandler (a,
                                             pv);

    }

    public static ActionListener getDeleteAssetAction (final ProjectViewer pv,
                                                       final Asset         a)
    {

        return new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                UIUtils.createQuestionPopup (pv,
                                             "Delete " +
                                             Environment.getObjectTypeName (a),
                                             Constants.DELETE_ICON_NAME,
                                             "Please confirm you wish to delete " +
                                             Environment.getObjectTypeName (a) +
                                             " <b>" +
                                             a.getName () +
                                             "</b>?",
                                             "Yes, delete it",
                                             null,
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
                                                                                  "Unable to delete");

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

    public ActionListener getRenameObjectAction (ProjectViewer pv,
                                                 NamedObject   n)
    {

        return AssetViewPanel.getRenameAssetAction (pv,
                                                    (Asset) n);

    }

    public void doFillToolBar (JToolBar tb,
                               boolean  fullScreen)
    {

        final AssetViewPanel _this = this;

        final JButton b = UIUtils.createToolBarButton ("new",
                                                       "Click to add a new " + Environment.getObjectTypeName (QCharacter.OBJECT_TYPE) + ", " + Environment.getObjectTypeName (Location.OBJECT_TYPE) + ", " + Environment.getObjectTypeName (QObject.OBJECT_TYPE) + " etc.",
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

    }

    public void doInit ()
    {

        this.obj.addPropertyChangedListener (this);

    }

    @Override
    public void propertyChanged (PropertyChangedEvent ev)
    {

        DetailsEditPanel edPan = this.getDetailsPanel ();

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

        this.getDetailsPanel ().refreshViewPanel ();

        if (this.appearsInPanel != null)
        {

            this.appearsInPanel.refreshViewPanel ();

        }

    }

    public EditPanel getBottomEditPanel ()
    {

        final EditPanel ep = this.createAppearsInChaptersPanel ();
        final AssetViewPanel _this = this;

        ep.init ();

        return ep;

    }

    public AppearsInChaptersEditPanel getAppearsInChaptersEditPanel ()
    {

        return this.appearsInPanel;

    }

    private EditPanel createAppearsInChaptersPanel ()
    {

        this.appearsInPanel = new AppearsInChaptersEditPanel (this.viewer,
                                                              this.obj);

        return this.appearsInPanel;

    }

    @Override
    public void doRefresh ()
    {

    }

    @Override
    public void close ()
    {

        this.obj.removePropertyChangedListener (this);

        this.appearsInPanel.close ();

    }

    @Override
    public void fillPopupMenu (MouseEvent ev,
                               JPopupMenu popup)
    {

        final AssetViewPanel _this = this;
    
        popup.add (UIUtils.createMenuItem ("Edit",
                                           Constants.EDIT_ICON_NAME,
                                           this.getEditObjectAction (this.viewer,
                                                                     (Asset) this.obj)));

        popup.add (UIUtils.createMenuItem ("Add File/Document",
                                           Constants.ADD_ICON_NAME,
                                           new ActionListener ()
                                           {
                                            
                                                @Override
                                                public void actionPerformed (ActionEvent ev)
                                                {
                                                    
                                                    _this.getObjectDocumentsEditPanel ().showAddDocument ();
                                                    
                                                }
                                            
                                           }));

        popup.add (UIUtils.createMenuItem ("Delete",
                                           Constants.DELETE_ICON_NAME,
                                           this.getDeleteObjectAction (this.viewer,
                                                                       (Asset) this.obj)));

    
    }
    
}
