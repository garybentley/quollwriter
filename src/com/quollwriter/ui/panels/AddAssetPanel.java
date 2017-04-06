package com.quollwriter.ui.panels;

import java.awt.event.*;
import java.awt.Component;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

import javax.swing.*;
import javax.swing.border.*;

import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.ui.userobjects.*;

import com.quollwriter.ui.components.Header;

public class AddAssetPanel extends ProjectObjectQuollPanel<ProjectViewer, Asset> implements MainPanelListener
{
    
    private Set<UserConfigurableObjectFieldViewEditHandler> addHandlers = null;
    private JLabel error = null;
    
    public AddAssetPanel (ProjectViewer viewer,
                          Asset         asset)
    {

        super (viewer,
               asset);
        
        this.viewer.addMainPanelListener (this);

        this.error = UIUtils.createErrorLabel ("");
        
        this.error.setVisible (false);
        
        this.error.setBorder (UIUtils.createPadding (5, 10, 5, 5));
        
    }    
    
    @Override
    public void panelShown (MainPanelEvent ev)
    {
        
        if (ev.getPanel () == this)
        {
            
            this.setHasUnsavedChanges (true);                
            
        }
        
    }
    
    @Override
    public String getPanelId ()
    {
        
        return "add" + this.obj.getObjectType ();
        
    }
    
    @Override
    public void close ()
    {
        
    }

    @Override
    public void init ()
               throws GeneralException
    {

        final AddAssetPanel _this = this;
    
        Header title = UIUtils.createHeader (String.format ("Add new %s",
                                                            this.obj.getObjectTypeName ()),
                                             Constants.PANEL_TITLE,
                                             this.obj.getUserConfigurableObjectType ().getIcon24x24 (),
                                             null);

        title.setBorder (new CompoundBorder (UIUtils.createBottomLineWithPadding (0, 0, 2, 0),
                                             title.getBorder ()));

        this.add (title);

        ActionListener formSave = new ActionListener ()
        {
            
            @Override
            public void actionPerformed (ActionEvent ev)
            {
                
                _this.handleSave ();
                
            }
            
        };
        
        this.addHandlers = this.obj.getViewEditHandlers (this.viewer);
        
        Set<FormItem> items = new LinkedHashSet ();
        
        for (UserConfigurableObjectFieldViewEditHandler h : this.addHandlers)
        {
             
            Set<FormItem> fits = h.getInputFormItems (null,
                                                      formSave);
            
            if (fits != null)
            {
            
                items.addAll (fits);
                
            }
            
        }
            
        AssetViewAddEditLayout layout = new AssetViewAddEditLayout (this.obj.getUserConfigurableObjectType ().getLayout (),
                                                                    this.viewer);
            
        JComponent c = layout.createEdit (this.addHandlers,
                                          formSave);
                              
        c.setBorder (UIUtils.createPadding (2, 7, 7, 0));

        c.setMaximumSize (new java.awt.Dimension (Short.MAX_VALUE, Short.MAX_VALUE));
                        
        JButton save = UIUtils.createButton (Constants.SAVE_ICON_NAME,
                                             Constants.ICON_MENU,
                                             "Click to save",
                                             formSave);

        JButton cancel = UIUtils.createButton (Constants.CANCEL_ICON_NAME,
                                               Constants.ICON_MENU,
                                               "Click to cancel",
                                              new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.setHasUnsavedChanges (false);
            
                _this.viewer.closePanel (_this);

            }

        });

        List<JButton> buts = new ArrayList ();
        buts.add (save);
        buts.add (cancel);
        
        title.setControls (UIUtils.createButtonBar (buts));                     
                     
        this.add (this.error);

        this.add (c);
                                
/*                                
        final JScrollPane sp = UIUtils.createScrollPane (c);        
                
        sp.setBorder (UIUtils.createPadding (1, 0, 0, 0));
        sp.getVerticalScrollBar ().setUnitIncrement (50);

        sp.getVerticalScrollBar ().addAdjustmentListener (new AdjustmentListener ()
        {

            @Override
            public void adjustmentValueChanged (AdjustmentEvent ev)
            {

                if (sp.getVerticalScrollBar ().getValue () > 0)
                {

                    sp.setBorder (new MatteBorder (1, 0, 0, 0,
                                                   UIUtils.getInnerBorderColor ()));

                } else {

                    sp.setBorder (UIUtils.createPadding (1, 0, 0, 0));

                }

            }

        });
                
        this.add (sp);
*/
        this.setReadyForUse (true);        
                        
    }
    
    public void hideError ()
    {
        
        this.error.setVisible (false);
        
    }
    
    public void showErrors (Set<String> messages)
    {
        
        if ((messages == null)
            ||
            (messages.size () == 0)
           )
        {
            
            this.error.setVisible (false);
            
            return;
            
        }
        
        if (messages.size () == 1)
        {
            
            this.showError (messages.iterator ().next ());
            
            return;
            
        }
        
        StringBuilder b = new StringBuilder ();

        for (String m : messages)
        {

            b.append (String.format ("<li>%s</li>",
                                     m));

        }
        
        this.showError ("Please correct the following errors:<ul>" + b.toString () + "</ul>");
        
    }
    
    public void showError (String message)
    {
        
        this.error.setText (message);
        
        this.error.setVisible (true);
                
    }
    
    private boolean handleSave ()
    {
        
        Set<String> errors = new LinkedHashSet ();
    
        // Do our error checks.
        for (UserConfigurableObjectFieldViewEditHandler h : this.addHandlers)
        {            
                
            Set<String> herrs = h.getInputFormItemErrors ();
            
            if (herrs != null)
            {
                
                errors.addAll (herrs);
                
            }

        }
        
        if (errors.size () > 0)
        {
            
            this.showErrors (errors);
            
            return false;
            
        }
                
        // Now setup the values.
        for (UserConfigurableObjectFieldViewEditHandler h : this.addHandlers)
        {            
                        
            try
            {
                
                h.updateFieldFromInput ();
                                
            } catch (Exception e) {
                
                Environment.logError ("Unable to get input save value for: " +
                                      h.getTypeField (),
                                      e);
                
                UIUtils.showErrorMessage (this.viewer,
                                          "Unable to save.");
                
                return false;
                
            }
            
        }
        
        try
        {

            this.viewer.getProject ().addAsset (this.obj);

            this.saveObject ();

        } catch (Exception e)
        {

            Environment.logError ("Unable to save: " +
                                  this.obj,
                                  e);

            UIUtils.showErrorMessage (this.viewer,
                                      "Unable to save.");

            return false;

        }
        
        this.viewer.openObjectSection (this.obj);                                               

        this.viewer.reloadTreeForObjectType (this.obj.getUserConfigurableObjectType ().getObjectTypeId ());
                                       
        this.viewer.fireProjectEvent (this.obj.getObjectType (),
                                      ProjectEvent.NEW,
                                      this.obj);
        
        this.viewer.closePanel (this);
        
        this.viewer.viewAsset (this.obj);        
        
        return true;
        
    }
    
    @Override
    public void getState (Map<String, Object> s)
    {
        
    }

    @Override
    public void setState (Map<String, String> s,
                          boolean             hasFocus)
    {
        
    }
    
    @Override
    public ImageIcon getIcon (int iconTypeSize)
    {
        
        return this.obj.getUserConfigurableObjectType ().getIcon16x16 ();
        
    }
    
    @Override
    public String getTitle ()
    {
        
        return "Add a new " + this.obj.getObjectTypeName ();
        
    }
    
    @Override
    public void fillToolBar (JToolBar toolBar,
                             boolean  fullScreen)
    {

    }

    @Override
    public void fillPopupMenu (MouseEvent ev,
                               JPopupMenu popup)
    {

    }
    
    @Override
    public List<Component> getTopLevelComponents ()
    {

        return null;

    }
    
    @Override
    public boolean saveUnsavedChanges ()
                                throws Exception
    {
        
        return this.handleSave ();
        
    }

    @Override
    public void refresh ()
    {
        
    }
    
}