package com.quollwriter.ui;

import java.awt.Component;
import java.awt.Dimension;

import java.awt.event.*;

import java.util.*;

import javax.swing.*;

import com.toedter.calendar.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.userobjects.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.ui.components.QTextEditor;
import com.quollwriter.ui.components.ScrollableBox;

/**
 * A class responsible for creating the layouts required for viewing and add/editing an
 * asset.
 */
public class AssetViewAddEditLayout
{
    
    private String type = null;
    private AbstractProjectViewer viewer = null;
    
    public AssetViewAddEditLayout (String                type,
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
            ret.setBorder (UIUtils.createPadding (5, 0, 5, 5));
            
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
                                                260,
                                                true,
                                                right,
                                                250,
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
            
            if (image != null)
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
                return this.createEdit (handlers,
                                        formSave);
                
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
        
        Box b = new com.quollwriter.ui.components.ScrollableBox (BoxLayout.Y_AXIS);
        
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
                    _b.setAlignmentY (Component.TOP_ALIGNMENT);
                    _b.setAlignmentX (Component.LEFT_ALIGNMENT);
                    _b.setMinimumSize (c.getPreferredSize ());
                    _b.setMaximumSize (new Dimension (Short.MAX_VALUE, c.getPreferredSize ().height));
            
                    if ((c instanceof QTextEditor)
                        &&
                        (!iter.hasNext ())
                       )
                    {
                        
                        //c.setPreferredSize (new Dimension (250, 300));
                        //_b.setMinimumSize (new Dimension (250, 300));
                        c.setMaximumSize (new Dimension (Short.MAX_VALUE, Short.MAX_VALUE));

                        _b.setMaximumSize (new Dimension (Short.MAX_VALUE, Short.MAX_VALUE));
                        
                    }
            
                    b.add (_b);
                        
                }
                
            }
            
            if (iter.hasNext ())
            {
                
                b.add (Box.createVerticalStrut (10));
                
            }
            
        }
        
        b.setOpaque (false);
        b.setAlignmentX (Component.LEFT_ALIGNMENT);
        b.setAlignmentY (Component.TOP_ALIGNMENT);
        b.setMinimumSize (b.getPreferredSize ());
        b.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                         Short.MAX_VALUE));
                           
        return b;
                    
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
                                
        Box b = new ScrollableBox (BoxLayout.X_AXIS);
        b.setAlignmentX (Component.LEFT_ALIGNMENT);
        b.add (col1C);
        b.add (Box.createHorizontalStrut (15));
        b.add (col2C);

        b.setBorder (UIUtils.createPadding (5, 0, 5, 5));
        
        JPanel p = new com.quollwriter.ui.components.ScrollablePanel (new java.awt.BorderLayout ());
        p.setOpaque (false);
        p.add (b);
        
        col1C.setMinimumSize (new Dimension (col1Width, col1C.getPreferredSize ().height));          
        
        if (!col1Grow)
        {
        
            col1C.setMaximumSize (new Dimension (col1Width, Short.MAX_VALUE));
            
        } else {
            
            //col1C.setMaximumSize (new Dimension (Short.MAX_VALUE, Short.MAX_VALUE));            
            col2C.setPreferredSize (new Dimension (col2Width, col2C.getPreferredSize ().height));            
            
        }
        
        col2C.setMinimumSize (new Dimension (col2Width, col2C.getPreferredSize ().height));
        
        if (!col2Grow)
        {
            
            col2C.setMaximumSize (new Dimension (col2Width, Short.MAX_VALUE));
            
        } else {
            
            //col2C.setMaximumSize (new Dimension (Short.MAX_VALUE, Short.MAX_VALUE));            
            col1C.setPreferredSize (new Dimension (col1Width, col1C.getPreferredSize ().height));
            
        }
        
        JScrollPane sp = UIUtils.createScrollPane (p);
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
