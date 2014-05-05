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

public class AssetViewPanel extends AbstractObjectViewPanel implements PropertyChangedListener
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

    private ActionListener assetDeleteAction = null;
    private AppearsInChaptersEditPanel appearsInPanel = null;

    public AssetViewPanel (AbstractProjectViewer pv,
                           Asset                 a)
                   throws GeneralException
    {

        super (pv,
               a);
               
    }

    public static DetailsEditPanel getEditDetailsPanel (Asset                 a,
                                                        AbstractProjectViewer pv)
                                                 throws GeneralException
    {

        DetailsEditPanel adep = null;

        try
        {

            Class c = AssetViewPanel.detailsEditPanels.get (a.getClass ());

            Constructor con = c.getConstructor (Asset.class,
                                                AbstractProjectViewer.class);

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
    public DetailsEditPanel getDetailEditPanel (AbstractProjectViewer pv,
                                                NamedObject           n)
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
    public static ActionListener getDeleteAssetAction (final AbstractProjectViewer pv,
                                                       final Asset                 a)
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
                                             null);

            }

        };

    }

    public ActionListener getDeleteObjectAction (AbstractProjectViewer pv,
                                                 NamedObject           n)
    {

        if (this.assetDeleteAction == null)
        {

            this.assetDeleteAction = AssetViewPanel.getDeleteAssetAction (pv,
                                                                          (Asset) n);

        }

        return this.assetDeleteAction;

    }

    public void doFillToolBar (JToolBar tb,
                               boolean  fullScreen)
    {

    }

    public void doInit ()
    {

        DetailsEditPanel edPan = this.getDetailsPanel ();
               
        Set<String> objChangeEventTypes = edPan.getObjectChangeEventTypes ();
        
        if ((objChangeEventTypes != null)
            &&
            (objChangeEventTypes.size () > 0)
           )
        {
            
            Map events = new HashMap ();
            
            for (String s : objChangeEventTypes)
            {
                
                events.put (s,
                            s);
                
            }
            
            this.obj.addPropertyChangedListener (this,
                                                 events);            
            
        }               
    
    }

    public void propertyChanged (PropertyChangedEvent ev)
    {

        this.getDetailsPanel ().refreshViewPanel ();
    
    }    
    
    public EditPanel getBottomEditPanel ()
    {

        final EditPanel ep = this.createAppearsInChaptersPanel ();
        final AssetViewPanel _this = this;

        ep.init ();

        return ep;

    }

    private EditPanel createAppearsInChaptersPanel ()
    {

        this.appearsInPanel = new AppearsInChaptersEditPanel (this.projectViewer,
                                                              this.obj);
        
        return this.appearsInPanel;

    }

    public void doRefresh ()
    {

    }

    public void close ()
    {

        this.obj.removePropertyChangedListener (this);
    
        this.appearsInPanel.close ();

    }

}
