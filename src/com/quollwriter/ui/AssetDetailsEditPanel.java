package com.quollwriter.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.toedter.calendar.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.ui.userobjects.*;
import com.quollwriter.ui.components.IconProvider;
import com.quollwriter.ui.components.QTextEditor;

public class AssetDetailsEditPanel<E extends Asset> extends EditPanel implements RefreshablePanel
{

    private ProjectViewer viewer = null;
    private Asset object = null;
    private boolean changeConfirm = false;
    private Set<UserConfigurableObjectFieldViewEditHandler> editHandlers = null;

    public AssetDetailsEditPanel (Asset                   a,
                                  ProjectViewer           viewer)
    {

        super ();

        this.object = a;
        this.viewer = viewer;
               
        InputMap im = this.getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW);

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_S,
                                        InputEvent.CTRL_MASK),
                "save");

        final AssetDetailsEditPanel _this = this;

        ActionMap am = this.getActionMap ();

        am.put ("save",
                new AbstractAction ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                _this.getDoSaveAction ().actionPerformed (ev);

            }

        });
               
    }
    
    @Override
    public void refresh ()
    {
        
        this.showViewPanel ();
        
    }
    
    @Override
    public void refreshViewPanel ()
    {

        this.refresh ();
    
    }
    
    @Override
    public boolean handleSave ()
    {

        Set<String> errors = new LinkedHashSet ();
    
        // Do our error checks.
        for (UserConfigurableObjectFieldViewEditHandler h : this.editHandlers)
        {            
                
            Set<String> herrs = h.getInputFormItemErrors ();
            
            if (herrs != null)
            {
                
                errors.addAll (herrs);
                
            }

        }
        
        if (errors.size () > 0)
        {
            
            this.showEditError (errors);
            
            return false;
            
        }
        
        Set<String> oldNames = this.object.getAllNames ();
        
        // Now setup the values.
        for (UserConfigurableObjectFieldViewEditHandler h : this.editHandlers)
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

            this.viewer.saveObject (this.object,
                                    true);

            this.viewer.fireProjectEvent (this.object.getObjectType (),
                                          ProjectEvent.EDIT,
                                          this.object);

        } catch (Exception e)
        {

            Environment.logError ("Unable to save: " +
                                  this.object,
                                  e);

            UIUtils.showErrorMessage (this.viewer,
                                      "Unable to save.");

            return false;

        }

        this.viewer.updateProjectDictionaryForNames (oldNames,
                                                     this.object);

        this.showViewPanel ();

        return true;

    }

    @Override
    public boolean handleCancel ()
    {

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
                                             "Discard changes?",
                                             Constants.HELP_ICON_NAME,
                                             "You have made some changes, do you want to discard them?",
                                             "Yes, discard them",
                                             "No, keep them",
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

        return true;

    }

    @Override
    public void handleEditStart ()
    {

        // Give the focus to the primary name field.

    }

    @Override
    public IconProvider getIconProvider ()
    {

        DefaultIconProvider iconProv = new DefaultIconProvider ()
        {

            @Override
            public ImageIcon getIcon (String name,
                                      int    type)
            {

                if (name.equals ("header"))
                {

                    name = Constants.INFO_ICON_NAME;

                }

                return super.getIcon (name,
                                      type);

            }

        };

        return iconProv;

    }

    public String getHelpText ()
    {

        return "Press the Edit icon again to save the information.";

    }

    public String getTitle ()
    {

        return "About";

    }
            
    public JComponent getAddPanel ()
    {
        
        final AssetDetailsEditPanel _this = this;
        
        this.editHandlers = this.object.getViewEditHandlers (this.viewer);
        
        // Build the edit panel.
        // Get the layout.
        AssetViewAddEditLayout l = this.getLayout (this.object);
        
        return l.createEdit (this.editHandlers,
                            new ActionListener ()
                            {
                                
                                @Override
                                public void actionPerformed (ActionEvent ev)
                                {
                                    
                                    _this.doSave ();
                                    
                                }
                                
                            });
            
    }
    
    @Override
    public JComponent getEditPanel ()
    {
        
        final AssetDetailsEditPanel _this = this;
        
        this.editHandlers = this.object.getViewEditHandlers (this.viewer);
        
        // Build the edit panel.
        // Get the layout.
        AssetViewAddEditLayout l = this.getLayout (this.object);
        
        return l.createEdit (this.editHandlers,
                             new ActionListener ()
                             {
                                
                                @Override
                                public void actionPerformed (ActionEvent ev)
                                {
                                    
                                    _this.doSave ();
                                    
                                }
                                
                             });
        
    }

    @Override
    public JComponent getViewPanel ()
    {
        
        // Build the view panel.
        
        // Get the layout.
        AssetViewAddEditLayout l = this.getLayout (this.object);
        
        return l.createView (this.object.getViewEditHandlers (this.viewer));
        
    }
    
    @Override
    public Set<FormItem> getEditItems ()
    {
        
        return null;
        
    }

    @Override
    public Set<FormItem> getViewItems ()
    {
        
        return null;
        
    }
    
    @Override
    public void doEdit ()
    {
        
        ActionListener a = AssetViewPanel.getEditAssetAction (this.viewer,
                                                              this.object);
        
        if (a == null)
        {
            
            Environment.logError ("Unable to get edit asset action for: " +
                                  this.object,
                                  null);
            
            UIUtils.showErrorMessage (this.viewer,
                                      String.format ("Unable to edit the {%s}",
                                                     Environment.getObjectTypeName (this.object)));
            
            return;
            
        }

        UIUtils.doLater (a);        
        
    }

    public AssetViewAddEditLayout getLayout (UserConfigurableObject obj)
    {
        
        return new AssetViewAddEditLayout (obj.getUserConfigurableObjectType ().getLayout (),
                                           this.viewer);
        
    }
    /*
    public class Layout
    {
        
        private String type = null;
        private AbstractProjectViewer viewer = null;
        
        public Layout (String                type,
                       AbstractProjectViewer viewer)
        {
            
            this.type = type;
            this.viewer = viewer;
            
        }
                
        public JComponent createView (Set<UserConfigurableObjectFieldViewEditHandler> handlers)
        {

            ObjectImageUserConfigurableObjectFieldViewEditHandler image = null;
            ObjectDescriptionUserConfigurableObjectFieldViewEditHandler desc = null;
            Set<UserConfigurableObjectFieldViewEditHandler> otherHandlers = new LinkedHashSet ();        
        
            for (UserConfigurableObjectFieldViewEditHandler h : handlers)
            {
            
                if (h.getTypeField () instanceof ObjectNameUserConfigurableObjectTypeField)
                {
                    
                    continue;
                    
                }

                if (h.getTypeField () instanceof ObjectImageUserConfigurableObjectTypeField)
                {
                    
                    image = (ObjectImageUserConfigurableObjectFieldViewEditHandler) h;
                    
                    continue;
                    
                }

                if (h.getTypeField () instanceof ObjectDescriptionUserConfigurableObjectTypeField)
                {
                    
                    desc = (ObjectDescriptionUserConfigurableObjectFieldViewEditHandler) h;
                    
                    continue;
                    
                }
                
                otherHandlers.add (h);
                                
            }
                
            if (this.type == null)
            {
                
                this.type = Constants.ASSET_LAYOUT_3;
                
            }
            
            if (this.type.equals (Constants.ASSET_LAYOUT_1))
            {
                
                if ((image == null)
                    &&
                    (desc == null)
                   )
                {
                    
                    this.type = null;
                    
                    // If we have no description and no image then just default to the 2 column layout.
                    return this.createView (handlers);
                    
                }
                
                Box ret = new Box (BoxLayout.Y_AXIS);
                
                Box top = new Box (BoxLayout.X_AXIS);
                top.setAlignmentX (Component.LEFT_ALIGNMENT);
                                
                ret.add (top);
                
                if ((image != null)
                    &&
                    (image.hasImage ())
                   )
                {
                    
                    JComponent im = image.getViewImageComponent ();
                    
                    im.setBorder (UIUtils.createPadding (5, 0, 0, 10));
                    
                    im.setAlignmentY (Component.TOP_ALIGNMENT);
                    top.add (im);
                    top.add (Box.createVerticalGlue ());
                    
                }

                if (desc != null)
                {
                    
                    JComponent t = desc.getViewTextComponent ();
                    
                    t.setAlignmentY (Component.TOP_ALIGNMENT);
                    top.add (t);
                    
                }
                            
                if (otherHandlers.size () > 0)
                {                            
                
                    JComponent o = this.createTwoColumnsLayout (otherHandlers);
                    o.setBorder (UIUtils.createPadding (10, 0, 0, 0));
                
                    ret.add (o);
                    
                }
                                                
                return ret;
                
            }
        
            if (this.type.equals (Constants.ASSET_LAYOUT_2))
            {
                
                if ((image == null)
                    &&
                    (desc == null)
                   )
                {
                    
                    this.type = null;
                    
                    // If we have no description and no image then just default to the 2 column layout.
                    return this.createView (handlers);
                    
                }
                
                Set<FormItem> left = new LinkedHashSet ();
                Set<FormItem> right = new LinkedHashSet ();
                
                if ((image != null)
                    &&
                    (image.hasImage ())
                   )
                {
                    
                    JComponent im = image.getViewImageComponent ();
                                        
                    im.setAlignmentY (Component.TOP_ALIGNMENT);
                    
                    left.add (new AnyFormItem (null,
                                               im));
                    
                }

                if (desc != null)
                {
                    
                    JComponent t = desc.getViewTextComponent ();
                    
                    left.add (new AnyFormItem (null,
                                               t));
                    
                }
                            
                if (otherHandlers.size () > 0)
                {                            
                
                    Set<FormItem> items = new LinkedHashSet ();
                    
                    for (UserConfigurableObjectFieldViewEditHandler h : otherHandlers)
                    {
                            
                        Set<FormItem> fits = h.getViewFormItems ();
                        
                        if (fits != null)
                        {
                        
                            right.addAll (fits);
                            
                        }
        
                    }
                                                            
                }
                                               
                return this.createTwoColumnsLayout (left,
                                                    250,
                                                    true,
                                                    right,
                                                    260,
                                                    false);
                                                                
            }

            if (this.type.equals (Constants.ASSET_LAYOUT_4))
            {
                
                if ((image == null)
                    &&
                    (desc == null)
                   )
                {
                    
                    this.type = null;
                    
                    // If we have no description and no image then just default to the 2 column layout.
                    return this.createView (handlers);
                    
                }
                
                Set<FormItem> left = new LinkedHashSet ();
                Set<FormItem> right = new LinkedHashSet ();
                
                if ((image != null)
                    &&
                    (image.hasImage ())
                   )
                {
                    
                    JComponent im = image.getViewImageComponent ();
                                        
                    im.setAlignmentY (Component.TOP_ALIGNMENT);
                    
                    right.add (new AnyFormItem (null,
                                                im));
                    
                }

                if (desc != null)
                {
                    
                    JComponent t = desc.getViewTextComponent ();
                    
                    right.add (new AnyFormItem (null,
                                                t));
                    
                }
                            
                if (otherHandlers.size () > 0)
                {                            
                
                    Set<FormItem> items = new LinkedHashSet ();
                    
                    for (UserConfigurableObjectFieldViewEditHandler h : otherHandlers)
                    {
                            
                        Set<FormItem> fits = h.getViewFormItems ();
                        
                        if (fits != null)
                        {
                        
                            left.addAll (fits);
                            
                        }
        
                    }
                                                            
                }
                                       
                return this.createTwoColumnsLayout (left,
                                                    260,
                                                    false,
                                                    right,
                                                    250,
                                                    true);
                                                                
            }

            if (this.type.equals (Constants.ASSET_LAYOUT_5))
            {
                
                if (desc == null)
                {
                    
                    this.type = null;
                    
                    // If we have no description and no image then just default to the 2 column layout.
                    return this.createView (handlers);
                    
                }
                
                Set<UserConfigurableObjectFieldViewEditHandler> _handlers = new LinkedHashSet (handlers);
                
                Set<FormItem> left = new LinkedHashSet ();
                Set<FormItem> right = new LinkedHashSet ();
                
                JComponent t = desc.getViewTextComponent ();
                
                right.add (new AnyFormItem (null,
                                            t));
                                                
                _handlers.remove (desc);

                if (_handlers.size () == 0)
                {
                    
                    this.type = "_bogus";
                    
                    return this.createView (handlers);
                            
                }
                
                Set<FormItem> items = new LinkedHashSet ();
                
                for (UserConfigurableObjectFieldViewEditHandler h : _handlers)
                {
                        
                    Set<FormItem> fits = h.getViewFormItems ();
                    
                    if (fits != null)
                    {
                    
                        left.addAll (fits);
                        
                    }
    
                }
                                                   
                return this.createTwoColumnsLayout (left,
                                                    260,
                                                    false,
                                                    right,
                                                    250,
                                                    true);
                
            }

            if (this.type.equals (Constants.ASSET_LAYOUT_6))
            {
                
                if (desc == null)
                {
                    
                    this.type = null;
                    
                    // If we have no description and no image then just default to the 2 column layout.
                    return this.createView (handlers);
                    
                }
                
                Set<UserConfigurableObjectFieldViewEditHandler> _handlers = new LinkedHashSet (handlers);
                
                Set<FormItem> left = new LinkedHashSet ();
                Set<FormItem> right = new LinkedHashSet ();
                
                JComponent t = desc.getViewTextComponent ();
                
                left.add (new AnyFormItem (null,
                                          t));
                            
                _handlers.remove (desc);

                if (_handlers.size () == 0)
                {
                    
                    this.type = null;
                    
                    return this.createView (handlers);
                            
                }
                
                Set<FormItem> items = new LinkedHashSet ();
                
                for (UserConfigurableObjectFieldViewEditHandler h : _handlers)
                {
                        
                    Set<FormItem> fits = h.getViewFormItems ();
                    
                    if (fits != null)
                    {
                    
                        right.addAll (fits);
                        
                    }
    
                }
                                        
                return this.createTwoColumnsLayout (left,
                                                    250,
                                                    true,
                                                    right,
                                                    260,
                                                    false);
                
            }

            if (this.type.equals (Constants.ASSET_LAYOUT_7))
            {
                
                if ((image == null)
                    &&
                    (desc == null)
                   )
                {
                    
                    this.type = null;
                    
                    // If we have no description and no image then just default to the 2 column layout.
                    return this.createView (handlers);
                    
                }
                
                Set<FormItem> left = new LinkedHashSet ();
                Set<FormItem> right = new LinkedHashSet ();
                
                if ((image != null)
                    &&
                    (image.hasImage ())
                   )
                {
                    
                    JComponent im = image.getViewImageComponent ();
                                        
                    im.setAlignmentY (Component.TOP_ALIGNMENT);
                    
                    right.add (new AnyFormItem (null,
                                                im));
                    
                }

                if (desc != null)
                {
                    
                    JComponent t = desc.getViewTextComponent ();
                    
                    left.add (new AnyFormItem (null,
                                               t));
                    
                }
                            
                if (otherHandlers.size () > 0)
                {                            
                
                    Set<FormItem> items = new LinkedHashSet ();
                    
                    for (UserConfigurableObjectFieldViewEditHandler h : otherHandlers)
                    {
                            
                        Set<FormItem> fits = h.getViewFormItems ();
                        
                        if (fits != null)
                        {
                        
                            right.addAll (fits);
                            
                        }
        
                    }
                                                            
                }
                                                
                return this.createTwoColumnsLayout (left,
                                                    250,
                                                    true,
                                                    right,
                                                    260,
                                                    false);
                                                                
            }

            if (this.type.equals (Constants.ASSET_LAYOUT_8))
            {
                
                if ((image == null)
                    &&
                    (desc == null)
                   )
                {
                    
                    this.type = null;
                    
                    // If we have no description and no image then just default to the 2 column layout.
                    return this.createView (handlers);
                    
                }
                
                Set<FormItem> left = new LinkedHashSet ();
                Set<FormItem> right = new LinkedHashSet ();
                
                if ((image != null)
                    &&
                    (image.hasImage ())
                   )
                {
                    
                    JComponent im = image.getViewImageComponent ();
                                        
                    im.setAlignmentY (Component.TOP_ALIGNMENT);
                    
                    left.add (new AnyFormItem (null,
                                               im));
                    
                }

                if (desc != null)
                {
                    
                    JComponent t = desc.getViewTextComponent ();
                    
                    right.add (new AnyFormItem (null,
                                                t));
                    
                }
                            
                if (otherHandlers.size () > 0)
                {                            
                
                    Set<FormItem> items = new LinkedHashSet ();
                    
                    for (UserConfigurableObjectFieldViewEditHandler h : otherHandlers)
                    {
                            
                        Set<FormItem> fits = h.getViewFormItems ();
                        
                        if (fits != null)
                        {
                        
                            left.addAll (fits);
                            
                        }
        
                    }
                                                            
                }
                                          
                return this.createTwoColumnsLayout (left,
                                                    260,
                                                    false,
                                                    right,
                                                    250,
                                                    true);

            }

            if (this.type.equals (Constants.ASSET_LAYOUT_3))
            {

                return this.createTwoColumnsLayout (handlers);
            
            }            
            
            Set<FormItem> items = new LinkedHashSet ();
            
            for (UserConfigurableObjectFieldViewEditHandler h : handlers)
            {
            
                if (h.getTypeField () instanceof ObjectNameUserConfigurableObjectTypeField)
                {
                    
                    continue;
                    
                }

                Set<FormItem> fits = h.getViewFormItems ();
                
                if (fits != null)
                {
                
                    items.addAll (fits);
                    
                }

            }
                
            return this.createStackedLayoutColumn (items);
            
        }
        
        public JComponent createEdit (Set<UserConfigurableObjectFieldViewEditHandler> handlers,
                                      ActionListener                                  formSave)
        {
            
            ObjectNameUserConfigurableObjectFieldViewEditHandler name = null;
            ObjectImageUserConfigurableObjectFieldViewEditHandler image = null;
            ObjectDescriptionUserConfigurableObjectFieldViewEditHandler desc = null;
            Set<UserConfigurableObjectFieldViewEditHandler> otherHandlers = new LinkedHashSet ();        
        
            for (UserConfigurableObjectFieldViewEditHandler h : handlers)
            {
            
                if (h.getTypeField () instanceof ObjectNameUserConfigurableObjectTypeField)
                {
                    
                    name = (ObjectNameUserConfigurableObjectFieldViewEditHandler) h;
                    
                    continue;
                    
                }

                if (h.getTypeField () instanceof ObjectImageUserConfigurableObjectTypeField)
                {
                    
                    image = (ObjectImageUserConfigurableObjectFieldViewEditHandler) h;
                    
                    continue;
                    
                }

                if (h.getTypeField () instanceof ObjectDescriptionUserConfigurableObjectTypeField)
                {
                    
                    desc = (ObjectDescriptionUserConfigurableObjectFieldViewEditHandler) h;
                    
                    continue;
                    
                }
                
                otherHandlers.add (h);
                                
            }
            
            if (this.type == null)
            {
                
                this.type = Constants.ASSET_LAYOUT_3;
                
            }
            
            if (this.type.equals (Constants.ASSET_LAYOUT_1))
            {
                
                if ((image == null)
                    &&
                    (desc == null)
                   )
                {
                    
                    this.type = null;
                    
                    // If we have no description and no image then just default to the 2 column layout.
                    return this.createEdit (handlers,
                                            formSave);
                    
                }
                
                Box ret = new Box (BoxLayout.Y_AXIS);
                
                ret.setAlignmentX (Component.LEFT_ALIGNMENT);
                
                Box top = new Box (BoxLayout.Y_AXIS);
                
                if ((image != null)
                    &&
                    (desc != null)
                   )
                {
                    
                    top = new Box (BoxLayout.X_AXIS);
                
                }
                
                top.setAlignmentX (Component.LEFT_ALIGNMENT);
                
                Set<FormItem> topL = new LinkedHashSet ();
                Set<FormItem> topR = new LinkedHashSet ();
                
                topL.addAll (name.getInputFormItems (null,
                                                     formSave));
                
                if (image != null)
                {
                    
                    topL.addAll (image.getInputFormItems (null,
                                                          formSave));
                    
                    if (desc != null)
                    {
                                                                                                              
                        topR.addAll (desc.getInputFormItems (null,
                                                             formSave));
                                                           
                    } 

                } else {
                
                    if (desc != null)
                    {
                                                                                                              
                        topL.addAll (desc.getInputFormItems (null,
                                                             formSave));
                            
                    }
                    
                }

                JComponent ltopL = this.createStackedLayoutColumn (topL);
                
                top.add (ltopL);

                if ((image != null)
                    &&
                    (desc != null)
                   )
                {
                    
                    ltopL.setPreferredSize (new Dimension (260, ltopL.getPreferredSize ().height));                
                    ltopL.setMinimumSize (new Dimension (260, ltopL.getPreferredSize ().height));
                    ltopL.setMaximumSize (new Dimension (260, ltopL.getPreferredSize ().height));

                    top.add (Box.createHorizontalStrut (15));
                    
                }
                
                JComponent ltopR = this.createStackedLayoutColumn (topR);
                
                top.add (ltopR);
                
                top.setMaximumSize (new Dimension (Short.MAX_VALUE, top.getPreferredSize ().height));

                ret.add (top);
                
                ret.add (Box.createVerticalStrut (10));
                            
                if (otherHandlers.size () > 0)
                {                            
                
                    JScrollPane o = this.createTwoColumnsEditLayout (otherHandlers,
                                                                     formSave);
                
                    JComponent c = (JComponent) o.getViewport ().getView ();
                                
                    c.setAlignmentY (Component.TOP_ALIGNMENT);
                    c.setMaximumSize (new Dimension (Short.MAX_VALUE, Short.MAX_VALUE));//c.getPreferredSize ().height));
                    ret.add (c);
                    
                }
                                                             
                return UIUtils.createScrollPane (ret);
            
            }
            
            if (this.type.equals (Constants.ASSET_LAYOUT_2))
            {
                
                if ((image == null)
                    &&
                    (desc == null)
                   )
                {
                    
                    this.type = null;
                    
                    // If we have no description and no image then just default to the 2 column layout.
                    return this.createEdit (handlers,
                                            formSave);
                    
                }
                
                // XXX Handle single column.
                if (otherHandlers.size () == 0)
                {
                    
                }
                
                Set<FormItem> left = new LinkedHashSet ();
                Set<FormItem> right = new LinkedHashSet ();
                
                left.addAll (name.getInputFormItems (null,
                                                     formSave));
                
                if (image != null)
                {
                                        
                    left.addAll (image.getInputFormItems (null,
                                                          formSave));
                    
                }
                
                if (desc != null)
                {
                    
                                                            
                    left.add (new AnyFormItem (desc.getTypeField ().getFormName (),
                                               t));
                    
                }                
                            
                if (otherHandlers.size () > 0)
                {                            
                
                    Set<FormItem> items = new LinkedHashSet ();
                    
                    for (UserConfigurableObjectFieldViewEditHandler h : otherHandlers)
                    {
                            
                        Set<FormItem> fits = h.getInputFormItems (null,
                                                                  formSave);
                        
                        if (fits != null)
                        {
                        
                            right.addAll (fits);
                            
                        }
        
                    }
                                                            
                }
                                               
                return this.createTwoColumnsLayout (left,
                                                    250,
                                                    true,
                                                    right,
                                                    260,
                                                    false);
                                                                
            }
            
            if (this.type.equals (Constants.ASSET_LAYOUT_3))
            {

                return this.createTwoColumnsEditLayout (handlers,
                                                        formSave);
            
            }                        
            
            if (this.type.equals (Constants.ASSET_LAYOUT_4))
            {
                
                if ((image == null)
                    &&
                    (desc == null)
                   )
                {
                    
                    this.type = null;
                    
                    // If we have no description and no image then just default to the 2 column layout.
                    return this.createEdit (handlers,
                                            formSave);
                    
                }
                
                // XXX Handle single column.
                if (otherHandlers.size () == 0)
                {
                    
                }
                
                Set<FormItem> left = new LinkedHashSet ();
                Set<FormItem> right = new LinkedHashSet ();
                
                left.addAll (name.getInputFormItems (null,
                                                     formSave));
                
                if (image != null)
                {
                    
                    JComponent im = image.getViewImageComponent ();
                                        
                    im.setAlignmentY (Component.TOP_ALIGNMENT);
                    
                    right.addAll (image.getInputFormItems (null,
                                                           formSave));
                    
                }

                if (desc != null)
                {
                    
                    QTextEditor t = desc.getInputTextComponent (formSave);
                                                            
                    right.add (new AnyFormItem (desc.getTypeField ().getFormName (),
                                                t));
                    
                }
                            
                if (otherHandlers.size () > 0)
                {                            
                
                    Set<FormItem> items = new LinkedHashSet ();
                    
                    for (UserConfigurableObjectFieldViewEditHandler h : otherHandlers)
                    {
                            
                        Set<FormItem> fits = h.getInputFormItems (null,
                                                                  formSave);
                        
                        if (fits != null)
                        {
                        
                            left.addAll (fits);
                            
                        }
        
                    }
                                                            
                }
                                       
                return this.createTwoColumnsLayout (left,
                                                    260,
                                                    false,
                                                    right,
                                                    250,
                                                    true);
                                                                
            }            
            
            if (this.type.equals (Constants.ASSET_LAYOUT_5))
            {
                
                if (desc == null)
                {
                    
                    this.type = null;
                    
                    // If we have no description and no image then just default to the 2 column layout.
                    return this.createEdit (handlers,
                                            formSave);
                    
                }
                      
                Set<UserConfigurableObjectFieldViewEditHandler> _handlers = new LinkedHashSet (handlers);
                      
                _handlers.remove (name);
                _handlers.remove (desc);
                
                if (_handlers.size () == 0)
                {
                    
                    this.type = null;
                    
                    return this.createEdit (handlers,
                                            formSave);
                                
                }
                
                Set<FormItem> left = new LinkedHashSet ();
                Set<FormItem> right = new LinkedHashSet ();
                
                left.addAll (name.getInputFormItems (null,
                                                     formSave));
                
                QTextEditor t = desc.getInputTextComponent (formSave);
                                                        
                right.add (new AnyFormItem (desc.getTypeField ().getFormName (),
                                            t));
                
                Set<FormItem> items = new LinkedHashSet ();
                
                for (UserConfigurableObjectFieldViewEditHandler h : _handlers)
                {
                        
                    Set<FormItem> fits = h.getInputFormItems (null,
                                                              formSave);
                    
                    if (fits != null)
                    {
                    
                        left.addAll (fits);
                        
                    }
    
                }
                                                            
                return this.createTwoColumnsLayout (left,
                                                    260,
                                                    false,
                                                    right,
                                                    250,
                                                    true);
                
            }
            
            if (this.type.equals (Constants.ASSET_LAYOUT_6))
            {
                
                if (desc == null)
                {
                    
                    this.type = null;
                    
                    // If we have no description and no image then just default to the 2 column layout.
                    return this.createEdit (handlers,
                                            formSave);
                    
                }
                      
                Set<UserConfigurableObjectFieldViewEditHandler> _handlers = new LinkedHashSet (handlers);
                      
                _handlers.remove (name);
                _handlers.remove (desc);                      
                      
                if (_handlers.size () == 0)
                {
                    
                    this.type = null;
                    
                    return this.createEdit (handlers,
                                            formSave);
                                
                }
                                
                Set<FormItem> left = new LinkedHashSet ();
                Set<FormItem> right = new LinkedHashSet ();
                
                right.addAll (name.getInputFormItems (null,
                                                      formSave));
                
                QTextEditor t = desc.getInputTextComponent (formSave);
                                                        
                left.add (new AnyFormItem (desc.getTypeField ().getFormName (),
                                           t));
                
                Set<FormItem> items = new LinkedHashSet ();
                
                for (UserConfigurableObjectFieldViewEditHandler h : _handlers)
                {
                        
                    Set<FormItem> fits = h.getInputFormItems (null,
                                                              formSave);
                    
                    if (fits != null)
                    {
                    
                        right.addAll (fits);
                        
                    }
    
                }
                                                            
                return this.createTwoColumnsLayout (left,
                                                    250,
                                                    true,
                                                    right,
                                                    260,
                                                    false);
                
            }
            
            if (this.type.equals (Constants.ASSET_LAYOUT_7))
            {
                
                if ((image == null)
                    &&
                    (desc == null)
                   )
                {
                    
                    this.type = null;
                    
                    // If we have no description and no image then just default to the 2 column layout.
                    return this.createEdit (handlers,
                                            formSave);
                    
                }
                
                Set<FormItem> left = new LinkedHashSet ();
                Set<FormItem> right = new LinkedHashSet ();
                
                right.addAll (name.getInputFormItems (null,
                                                      formSave));
                
                if ((image != null)
                    &&
                    (image.hasImage ())
                   )
                {
                                        
                    right.addAll (image.getInputFormItems (null,
                                                           formSave));
                    
                }

                if (desc != null)
                {
                    
                    QTextEditor t = desc.getInputTextComponent (formSave);
                                                            
                    left.add (new AnyFormItem (desc.getTypeField ().getFormName (),
                                               t));
                    
                }
                            
                if (otherHandlers.size () > 0)
                {                            
                
                    Set<FormItem> items = new LinkedHashSet ();
                    
                    for (UserConfigurableObjectFieldViewEditHandler h : otherHandlers)
                    {
                            
                        Set<FormItem> fits = h.getInputFormItems (null,
                                                                  formSave);
                        
                        if (fits != null)
                        {
                        
                            right.addAll (fits);
                            
                        }
        
                    }
                                                            
                }
                                                
                return this.createTwoColumnsLayout (left,
                                                    250,
                                                    true,
                                                    right,
                                                    260,
                                                    false);
                                                                
            }
            
            if (this.type.equals (Constants.ASSET_LAYOUT_8))
            {
                
                if ((image == null)
                    &&
                    (desc == null)
                   )
                {
                    
                    this.type = null;
                    
                    // If we have no description and no image then just default to the 2 column layout.
                    return this.createView (handlers);
                    
                }
                
                Set<FormItem> left = new LinkedHashSet ();
                Set<FormItem> right = new LinkedHashSet ();
                
                if (name != null)
                {
                    
                    left.addAll (name.getInputFormItems (null,
                                                         formSave));
                    
                }
                
                if (image != null)
                {
                    
                    left.addAll (image.getInputFormItems (null,
                                                          formSave));
                    
                }
                
                if (desc != null)
                {
                    
                    QTextEditor t = desc.getInputTextComponent (formSave);
                    
                    t.setBorder (new JScrollPane ().getBorder ());
                    t.setMargin (new java.awt.Insets (3, 3, 3, 3));
                    t.setMaximumSize (new java.awt.Dimension (Short.MAX_VALUE, Short.MAX_VALUE));
                                        
                    right.add (new AnyFormItem (desc.getTypeField ().getFormName (),
                                                t));
                      
                }
                            
                if (otherHandlers.size () > 0)
                {                            
                
                    Set<FormItem> items = new LinkedHashSet ();
                    
                    for (UserConfigurableObjectFieldViewEditHandler h : otherHandlers)
                    {
                            
                        Set<FormItem> fits = h.getInputFormItems (null,
                                                                  formSave);
                        
                        if (fits != null)
                        {
                        
                            left.addAll (fits);
                            
                        }
        
                    }
                                                            
                }
                                                
                return this.createTwoColumnsLayout (left,
                                                    260,
                                                    false,
                                                    right,
                                                    250,
                                                    true);
                
            }
            
            Set<FormItem> items = new LinkedHashSet ();

            for (UserConfigurableObjectFieldViewEditHandler h : handlers)
            {
                 
                Set<FormItem> fits = h.getInputFormItems (null,
                                                          formSave);
                
                if (fits != null)
                {
                
                    items.addAll (fits);
                    
                }
                
            }
                
            final Form f = new Form (Form.Layout.stacked,
                                     items,
                                     null);

            f.setBorder (null);
                               
            return f;            
            
        }
        
        private JComponent createStackedLayoutColumn (Set<FormItem> items)
        {
            
            Box b = new Box (BoxLayout.Y_AXIS);
            
            Iterator<FormItem> iter = items.iterator ();
            
            while (iter.hasNext ())
            {
                        
                FormItem it = iter.next ();
                
                Object l = it.getLabel ();
                
                if (l != null)
                {
                
                    if (l instanceof String)
                    {
        
                        String label = l.toString ();
        
                        if (label != null)
                        {
        
                            l = UIUtils.createInformationLabel (label);
        
                        }
    
                    }
                
                    if (l instanceof JComponent)
                    {
                
                        l = this.wrap ((JComponent) l);
                         
                        b.add ((JComponent) l);

                    }
                                
                }
                            
                JComponent c = it.getComponent ();
            
                if (c != null)
                {
                    
                    c.setOpaque (false);
                                                 
                    c = this.wrap (c);
                         
                    if (l == null)
                    {
                        
                        b.add (c);
                        
                    } else {

                        b.add (Box.createVerticalStrut (6));    

                        Box _b = new Box (BoxLayout.X_AXIS);
                        _b.add (Box.createHorizontalStrut (10));
                        _b.add (c);
                        c.setAlignmentY (Component.TOP_ALIGNMENT);
                        c.setAlignmentX (Component.LEFT_ALIGNMENT);
                        //c.setMaximumSize (new Dimension (Short.MAX_VALUE, c.getPreferredSize ().height));
                        _b.setAlignmentY (Component.TOP_ALIGNMENT);
                        _b.setAlignmentX (Component.LEFT_ALIGNMENT);
                        _b.setMaximumSize (new Dimension (Short.MAX_VALUE, c.getPreferredSize ().height));
                
                        if ((c instanceof QTextEditor)
                            &&
                            (!iter.hasNext ())
                           )
                        {
                            
                            _b.setMaximumSize (new Dimension (Short.MAX_VALUE, Short.MAX_VALUE));
                            //c.setMaximumSize (new Dimension (Short.MAX_VALUE, Short.MAX_VALUE));
                            
                        }
                        
                        b.add (_b);
                            
                    }
                    
                }
                
                if (iter.hasNext ())
                {
                    
                    b.add (Box.createVerticalStrut (10));
                    
                }
                
            }
            
            //b.add (Box.createVerticalGlue ());
            b.setOpaque (false);
            b.setAlignmentX (Component.LEFT_ALIGNMENT);
            b.setAlignmentY (Component.TOP_ALIGNMENT);
            
            if (true)
            {
                
                return b;
                
            }
            
            String cols = "10px, d:grow";
    
            StringBuilder rows = new StringBuilder ();
            
            //Iterator<FormItem> iter = items.iterator ();
            
            while (iter.hasNext ())
            {
            
                FormItem it = iter.next ();
    
                JComponent c = it.getComponent ();
    
                if (it.getLabel () != null)
                {
                    
                    rows.append ("p");
                    
                }
                
                if (c != null)
                {
                    
                    if (it.getLabel () != null)
                    {
                        
                        rows.append (", 6px, ");
                        
                    }
                
                    if (iter.hasNext ())
                    {
                
                        rows.append ("top:p");
                        
                    } else {
                        
                        rows.append ("top:p");
                        
                    }
    
                }
                
                if (iter.hasNext ())
                {
                    
                    rows.append (", 10px, ");
                    
                }
    
            }
                
            FormLayout   fl = new FormLayout (cols,
                                              rows.toString ());
            PanelBuilder bb = new PanelBuilder (fl);
            
            CellConstraints cc = new CellConstraints ();
    
            iter = items.iterator ();
    
            int r = 1;
    
            while (iter.hasNext ())
            {
    
                FormItem it = iter.next ();
                Object l = it.getLabel ();
                
                if (l != null)
                {
                
                    if (l instanceof String)
                    {
        
                        String label = l.toString ();
        
                        if (label != null)
                        {
        
                            bb.addLabel (label,
                                        cc.xyw (1,
                                                 r,
                                                 2));
        
                            r += 2;
        
                        }
    
                    } else {
                
                        if (l instanceof JComponent)
                        {
                    
                            l = this.wrap ((JComponent) l);
                                        
                        }
                                
                        bb.add ((JComponent) l,
                               cc.xyw (1,
                                        r,
                                        2));
                        
                        r += 2;
                        
                    }
        
                }
                            
                JComponent c = it.getComponent ();
            
                if (c != null)
                {
                    
                    c.setOpaque (false);
                                    
                    c = this.wrap (c);
                         
                    if (l == null)
                    {
                        
                        bb.add (c,
                               cc.xyw (1,
                                       r,
                                       2));

                    } else {

                        bb.add (c,
                               cc.xy (2,
                                      r));

                    }
                    
                    r += 2;
                    
                }
    
            }

            JPanel p = bb.getPanel ();
            p.setOpaque (false);
            p.setAlignmentX (Component.LEFT_ALIGNMENT);
            p.setMaximumSize (new java.awt.Dimension (Short.MAX_VALUE,
                                             Short.MAX_VALUE));
                        
            return p;
                        
        }
                
        private JScrollPane createTwoColumnsLayout (Set<UserConfigurableObjectFieldViewEditHandler> handlers)
        {

            Set<UserConfigurableObjectFieldViewEditHandler> col1Handlers = new LinkedHashSet ();
            Set<UserConfigurableObjectFieldViewEditHandler> col2Handlers = new LinkedHashSet ();
        
            Set<FormItem> col1 = new LinkedHashSet ();
            Set<FormItem> col2 = new LinkedHashSet ();
                    
            int c = 1;
        
            for (UserConfigurableObjectFieldViewEditHandler h : handlers)
            {
            
                Set<FormItem> fits = h.getViewFormItems ();
                
                if (fits != null)
                {
                                            
                    if (c % 2 == 0)
                    {
                        
                        col2.addAll (fits);
                        
                    } else {
                        
                        col1.addAll (fits);
                        
                    }

                }
                
                c++;
                
            }
                
            return this.createTwoColumnsLayout (col1,
                                                250,
                                                true,
                                                col2,
                                                250,
                                                true);
            
        }
        
        private JScrollPane createTwoColumnsEditLayout (Set<UserConfigurableObjectFieldViewEditHandler> handlers,
                                                        ActionListener                                  formSave)
        {

            Set<UserConfigurableObjectFieldViewEditHandler> col1Handlers = new LinkedHashSet ();
            Set<UserConfigurableObjectFieldViewEditHandler> col2Handlers = new LinkedHashSet ();
        
            Set<FormItem> col1 = new LinkedHashSet ();
            Set<FormItem> col2 = new LinkedHashSet ();
                    
            int c = 1;
        
            for (UserConfigurableObjectFieldViewEditHandler h : handlers)
            {
            
                Set<FormItem> fits = h.getInputFormItems (null,
                                                          formSave);
                
                if (fits != null)
                {
                                            
                    if (c % 2 == 0)
                    {
                        
                        col2.addAll (fits);
                        
                    } else {
                        
                        col1.addAll (fits);
                        
                    }

                }
                
                c++;
                
            }
                
            return this.createTwoColumnsLayout (col1,
                                                250,
                                                true,
                                                col2,
                                                250,
                                                true);
            
        }

        private JScrollPane createTwoColumnsLayout (Set<FormItem> col1,
                                                    int           col1Width,
                                                    boolean       col1Grow,
                                                    Set<FormItem> col2,
                                                    int           col2Width,
                                                    boolean       col2Grow)
        {
                
            JComponent col1C = this.createStackedLayoutColumn (col1);
                                            
            JComponent col2C = this.createStackedLayoutColumn (col2);

            col1C.setOpaque (false);
            col2C.setOpaque (false);                        
                                    
            Box b = new Box (BoxLayout.X_AXIS);
            b.setAlignmentX (Component.LEFT_ALIGNMENT);
            b.add (col1C);
            b.add (Box.createHorizontalStrut (15));
            b.add (col2C);

            b.setBorder (UIUtils.createPadding (5, 0, 5, 5));
  
            col1C.setMinimumSize (new Dimension (col1Width, col1C.getPreferredSize ().height));          
            
            if (!col1Grow)
            {
            
                col1C.setMaximumSize (new Dimension (col1Width, Short.MAX_VALUE));
                
            } else {
                
                col2C.setPreferredSize (new Dimension (col2Width, col2C.getPreferredSize ().height));            
                
            }
            
            col2C.setMinimumSize (new Dimension (col2Width, col2C.getPreferredSize ().height));
            
            if (!col2Grow)
            {
                
                col2C.setMaximumSize (new Dimension (col2Width, Short.MAX_VALUE));
                
            } else {
                
               col1C.setPreferredSize (new Dimension (col1Width, col1C.getPreferredSize ().height));
                
            }

            JScrollPane sp = UIUtils.createScrollPane (b);
            sp.setBorder (null);
                     
            return sp;
            
        }        

        private JComponent wrap (JComponent component)
        {
       
            component.setAlignmentY (Component.TOP_ALIGNMENT);
            component.setAlignmentX (Component.LEFT_ALIGNMENT);
            //component.setMinimumSize (component.getPreferredSize ());
       
            if ((component instanceof JDateChooser)
                ||
                (component instanceof JComboBox)
                ||
                (component instanceof JSpinner)
                ||
                (component instanceof JLabel)
                ||
                (component instanceof JList)
               )
            {
                
                component.setMaximumSize (component.getPreferredSize ());
            
                Box tb = new Box (BoxLayout.X_AXIS);
                tb.add (component);
                tb.add (Box.createHorizontalGlue ());
                tb.setAlignmentY (Component.TOP_ALIGNMENT);
                tb.setAlignmentX (Component.LEFT_ALIGNMENT);

                tb.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                  component.getPreferredSize ().height));
                
                component = tb;
                
            }

            return component;
        
        }        
        
    }
    */
}
