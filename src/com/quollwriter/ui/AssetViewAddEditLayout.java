package com.quollwriter.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

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
        ObjectNameUserConfigurableObjectFieldViewEditHandler name = null;
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
            
            this.type = Constants.ASSET_LAYOUT_0;
            
        }
        
        if (this.type.equals (Constants.ASSET_LAYOUT_0))
        {

            Set<UserConfigurableObjectFieldViewEditHandler> _handlers = new LinkedHashSet (handlers);
            
            _handlers.remove (name);
            
            return this.createSingleColumnViewLayout (_handlers);
        
        }
        
        if (this.type.equals (Constants.ASSET_LAYOUT_1))
        {
            
            if ((image == null)
                &&
                (desc == null)
               )
            {
                
                this.type = null;
                
                handlers.remove (name);
                
                // If we have no description and no image then just default to the 2 column layout.
                return this.createView (handlers);
                
            }
            
            Box ret = new ScrollableBox (BoxLayout.Y_AXIS);
            
            Box top = new Box (BoxLayout.X_AXIS);
            top.setAlignmentX (Component.LEFT_ALIGNMENT);
                            
            ret.add (top);
            
            boolean addSpacer = false;
            
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
                
                addSpacer = true;
                
            }

            if ((desc != null)
                &&
                (desc.getFieldValue () != null)
                &&
                (desc.getFieldValue ().hasText ())
               )
            {
                
                JComponent t = desc.getViewTextComponent ();
                
                t.setAlignmentY (Component.TOP_ALIGNMENT);
                top.add (t);
                
                addSpacer = true;

            }
                        
            if (otherHandlers.size () > 0)
            {                            
            
                if (addSpacer)
                {

                    ret.add (Box.createVerticalStrut (10));
                    
                }
            
                JComponent o = (JComponent) this.createTwoColumnsLayout (otherHandlers).getViewport ().getView ();
                o.setBorder (UIUtils.createPadding (0, 0, 0, 0));
                o.setMinimumSize (new Dimension (525, o.getPreferredSize ().height));
                o.setAlignmentX (Component.LEFT_ALIGNMENT);
                o.setAlignmentY (Component.TOP_ALIGNMENT);
            
                ret.add (o);
                
            }

            ret.setBorder (UIUtils.createPadding (5, 0, 5, 5));
                         
            JScrollPane sp = UIUtils.createScrollPane (ret);
            sp.setBorder (null);

            return sp;
            
        }
    
        if (this.type.equals (Constants.ASSET_LAYOUT_2))
        {
            
            if ((image == null)
                &&
                ((desc == null)
                 ||
                 (desc.getFieldValue () == null)
                 ||
                 (!desc.getFieldValue ().hasText ())
                )
               )
            {
                
                Set<UserConfigurableObjectFieldViewEditHandler> _handlers = new LinkedHashSet (handlers);
                
                _handlers.remove (desc);
                
                return this.createSingleColumnViewLayout (_handlers);
                                
            }
            
            if ((!image.hasImage ())
                &&
                ((desc == null)
                 ||
                 (desc.getFieldValue () == null)
                 ||
                 (!desc.getFieldValue ().hasText ())
                )
               )
            {
                
                Set<UserConfigurableObjectFieldViewEditHandler> _handlers = new LinkedHashSet (handlers);
                
                _handlers.remove (desc);
                
                return this.createSingleColumnViewLayout (_handlers);
                                
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
                ((desc == null)
                 ||
                 (desc.getFieldValue () == null)
                )
               )
            {
                
                Set<UserConfigurableObjectFieldViewEditHandler> _handlers = new LinkedHashSet (handlers);
                
                _handlers.remove (desc);
                
                return this.createSingleColumnViewLayout (_handlers);
                                
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
            
            if ((desc == null)
                ||
                (desc.getFieldValue () == null)
               )
            {
                
                Set<UserConfigurableObjectFieldViewEditHandler> _handlers = new LinkedHashSet (handlers);
                
                _handlers.remove (name);
                _handlers.remove (desc);
                
                return this.createSingleColumnViewLayout (_handlers);
                                
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
            
            if ((desc == null)
                ||
                (desc.getFieldValue () == null)
               )
            {
                
                Set<UserConfigurableObjectFieldViewEditHandler> _handlers = new LinkedHashSet (handlers);
                
                _handlers.remove (name);
                _handlers.remove (desc);
                
                return this.createSingleColumnViewLayout (_handlers);
                                
            }
            
            Set<UserConfigurableObjectFieldViewEditHandler> _handlers = new LinkedHashSet (handlers);
            
            Set<FormItem> left = new LinkedHashSet ();
            Set<FormItem> right = new LinkedHashSet ();
            
            JComponent t = desc.getViewTextComponent ();
            
            left.add (new AnyFormItem (null,
                                      t));
                        
            _handlers.remove (name);
            _handlers.remove (desc);
            
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
                 
            if ((desc == null)
                 ||
                 (desc.getFieldValue () == null)
                 ||
                 (desc.getFieldValue ().getText () == null)
               )
            {
                
                return this.createSingleColumnViewLayoutForItems (right);
                                
            } else {
                
                JComponent t = desc.getViewTextComponent ();
                
                left.add (new AnyFormItem (null,
                                           t));
                
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
              
            if ((desc == null)
                 ||
                 (desc.getFieldValue () == null)
                 ||
                 (desc.getFieldValue ().getText () == null)
               )
            {
                
                return this.createSingleColumnViewLayoutForItems (left);
                                
            } else {
                
                JComponent t = desc.getViewTextComponent ();
                
                right.add (new AnyFormItem (null,
                                            t));
                
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

            handlers.remove (name);

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
            
        return this.createStackedLayoutColumn (items,
                                               true);
        
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
            
            this.type = Constants.ASSET_LAYOUT_0;
            
        }
        
        if (this.type.equals (Constants.ASSET_LAYOUT_0))
        {
        
            Set<UserConfigurableObjectFieldViewEditHandler> _handlers = new LinkedHashSet ();                    
        
            _handlers.add (name);
            _handlers.addAll (handlers);
        
            return this.createSingleColumnEditLayout (_handlers,
                                                      formSave);        

        }
        
        if (this.type.equals (Constants.ASSET_LAYOUT_1))
        {

            Box ret = new Box (BoxLayout.Y_AXIS);
            
            ret.setAlignmentX (Component.LEFT_ALIGNMENT);
            
            Set<FormItem> tleft = new LinkedHashSet ();
            Set<FormItem> tright = new LinkedHashSet ();
            
            tleft.addAll (name.getInputFormItems (null,
                                                  formSave));
            
            if (image != null)
            {
                
                tleft.addAll (image.getInputFormItems (null,
                                                       formSave));
                
                if (desc != null)
                {
                                                                                                          
                    tright.addAll (desc.getInputFormItems (null,
                                                           formSave));
                                                       
                } 

            } else {
            
                if (desc != null)
                {
                                                                                                          
                    tleft.addAll (desc.getInputFormItems (null,
                                                          formSave));
                        
                }
                
            }
            
            if ((image == null)
                ||
                (desc == null)
               )
            {
                
                JComponent c = this.createStackedLayoutColumn (tleft,
                                                               false);
                
                ret.add (c);
                
                c.setPreferredSize (new Dimension (50, c.getPreferredSize ().height));
                
                if (desc == null)
                {
                    
                    c.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                     c.getPreferredSize ().height));
                    
                }
                
                c.setAlignmentX (Component.LEFT_ALIGNMENT);
                c.setAlignmentY (Component.TOP_ALIGNMENT);
                
                //c.setMaximumSize (new Dimension (Short.MAX_VALUE, Short.MAX_VALUE));
                
            } else {
                
                JComponent l = this.createTwoColumnsEditLayout (tleft,
                                                    260,
                                                    (image == null),
                                                    tright,
                                                    250,
                                                    true);

                ret.add (l);

            }
                                    
            if (otherHandlers.size () > 0)
            {                            
            
                if (ret.getComponentCount () > 0)
                {
                    
                    ret.add (Box.createVerticalStrut (10));
                    
                }
            
                JScrollPane o = this.createTwoColumnsEditLayout (otherHandlers,
                                                                 formSave);
            
                JComponent c = (JComponent) o.getViewport ().getView ();
                         
                c.setAlignmentY (Component.TOP_ALIGNMENT);
                c.setAlignmentX (Component.LEFT_ALIGNMENT);
                
                c.setMaximumSize (new Dimension (Short.MAX_VALUE, Short.MAX_VALUE));//c.getPreferredSize ().height));
                ret.add (c);
                //ret.add (Box.createVerticalGlue ());
                
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
                
                left.addAll (desc.getInputFormItems (null,
                                                     formSave));

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
                                           
            return this.createTwoColumnsEditLayout (left,
                                                260,
                                                true,
                                                right,
                                                250,
                                                false);
                                                            
        }
        
        if (this.type.equals (Constants.ASSET_LAYOUT_3))
        {

            Box ret = new Box (BoxLayout.Y_AXIS);
            
            ret.setAlignmentX (Component.LEFT_ALIGNMENT);
            
            Set<FormItem> top = new LinkedHashSet ();

            Set<UserConfigurableObjectFieldViewEditHandler> _handlers = new LinkedHashSet (handlers);
            
            _handlers.remove (name);
            
            top.addAll (name.getInputFormItems (null,
                                                formSave));
                            
            JComponent c = this.createStackedLayoutColumn (top,
                                                           false);
                
            ret.add (c);
                
            c.setPreferredSize (new Dimension (50, c.getPreferredSize ().height));
            c.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                             c.getPreferredSize ().height));

            c.setAlignmentX (Component.LEFT_ALIGNMENT);
            c.setAlignmentY (Component.TOP_ALIGNMENT);
                                                    
            if (_handlers.size () > 0)
            {                            
            
                if (ret.getComponentCount () > 0)
                {
                    
                    ret.add (Box.createVerticalStrut (10));
                    
                }
            
                JScrollPane o = this.createTwoColumnsEditLayout (_handlers,
                                                                 formSave);
            
                c = (JComponent) o.getViewport ().getView ();
                         
                c.setAlignmentY (Component.TOP_ALIGNMENT);
                c.setAlignmentX (Component.LEFT_ALIGNMENT);
                
                c.setMaximumSize (new Dimension (Short.MAX_VALUE, Short.MAX_VALUE));//c.getPreferredSize ().height));
                ret.add (c);
                
            }

            return UIUtils.createScrollPane (ret);
        
        }                        
        
        if (this.type.equals (Constants.ASSET_LAYOUT_4))
        {
            
            if ((image == null)
                &&
                ((desc == null)
                 ||
                 (desc.getFieldValue () == null)
                )
               )
            {
                
                Set<UserConfigurableObjectFieldViewEditHandler> _handlers = new LinkedHashSet (handlers);
                
                _handlers.remove (desc);
                
                return this.createSingleColumnEditLayout (_handlers,
                                                          formSave);
                                
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
                
                right.addAll (desc.getInputFormItems (null,
                                                      formSave));
                                
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
                                   
            return this.createTwoColumnsEditLayout (left,
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
                                           
            right.addAll (desc.getInputFormItems (null,
                                                  formSave));
            
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
                                                        
            return this.createTwoColumnsEditLayout (left,
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
                                
                // If we have no description and no image then just default to the 2 column layout.
                return this.createSingleColumnEditLayout (handlers,
                                                          formSave);
                
            }
                  
            Set<UserConfigurableObjectFieldViewEditHandler> _handlers = new LinkedHashSet (handlers);
                  
            _handlers.remove (name);
            _handlers.remove (desc);                      
                  
            if (_handlers.size () == 0)
            {
                
                return this.createSingleColumnEditLayout (_handlers,
                                                          formSave);
                            
            }
                            
            Set<FormItem> left = new LinkedHashSet ();
            Set<FormItem> right = new LinkedHashSet ();
            
            right.addAll (name.getInputFormItems (null,
                                                  formSave));
            
            left.addAll (desc.getInputFormItems (null,
                                                 formSave));
                        
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
                                                        
            return this.createTwoColumnsEditLayout (left,
                                                250,
                                                true,
                                                right,
                                                260,
                                                false);
            
        }
        
        if (this.type.equals (Constants.ASSET_LAYOUT_7))
        {
            
            if (desc == null)
            {
                
                return this.createSingleColumnEditLayout (handlers,
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
                
                left.addAll (desc.getInputFormItems (null,
                                                     formSave));
                
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
                                          
            return this.createTwoColumnsEditLayout (left,
                                                250,
                                                true,
                                                right,
                                                260,
                                                false);
                                                            
        }
        
        if (this.type.equals (Constants.ASSET_LAYOUT_8))
        {
            
            if (desc == null)
            {
                
                return this.createSingleColumnEditLayout (handlers,
                                                          formSave);
                
            }
            
            if ((image == null)
                &&
                (desc != null)
                &&
                (otherHandlers.size () == 0)
               )
            {
                
                // Just use a single column layout.
                Set<UserConfigurableObjectFieldViewEditHandler> _handlers = new LinkedHashSet ();
                _handlers.add (name);
                _handlers.add (desc);
                
                return this.createSingleColumnEditLayout (_handlers,
                                                          formSave);
                
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
                
                right.addAll (desc.getInputFormItems (null,
                                                      formSave));

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
                                                                              
            return this.createTwoColumnsEditLayout (left,
                                                260,
                                                false,
                                                right,
                                                250,
                                                true);
            
        }
        
        return this.createSingleColumnEditLayout (handlers,
                                                  formSave);
        /*
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
                                 items);

        f.setBorder (null);
                           
        return f;            
        */
    }
    
    private JComponent XcreateStackedLayoutColumn (Set<FormItem> items)
    {
        
        Box b = new /*com.quollwriter.ui.components.Scrollable*/Box (BoxLayout.Y_AXIS);
        
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
            
    private JComponent createStackedLayoutColumn (Set<FormItem> items,
                                                  boolean       addFiller)
    {
        
        GridBagLayout layout = new GridBagLayout ();

        JPanel b = new JPanel (layout);
        b.setOpaque (false);

        int r = 0;

        GridBagConstraints cons = new GridBagConstraints ();
                
        Iterator<FormItem> iter = items.iterator ();
        
        while (iter.hasNext ())
        {
                    
            cons.gridx = 0;
            cons.gridy = GridBagConstraints.RELATIVE;
            cons.gridwidth = 1;
            cons.gridheight = 1;
            cons.fill = GridBagConstraints.HORIZONTAL;        
            cons.weightx = 1;
            cons.weighty = 0;
            cons.insets = new Insets (0, 0, 0, 0);
            cons.anchor = GridBagConstraints.NORTHWEST;
                    
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
                         
                    int top = 0;
                         
                    if (r > 0)
                    {
                        
                        top = 10;
                        
                    }
                        
                    cons.insets = new Insets (top, 0, 0, 0);

                    b.add ((JComponent) l,
                           cons);
                     
                    r++;
                     
                }
                            
            }
                        
            JComponent c = it.getComponent ();
        
            if (c != null)
            {
                
                c.setOpaque (false);
                                             
                c = this.wrap (c);
                     
                int top = 0;
                     
                if (r > 0)
                {
                    
                    top = 5;
                    
                }
                     
                if (l == null)
                {
                    
                    cons.insets = new Insets (top, 0, 0, 0);
                    
                    if (!addFiller)
                    {
                    
                        if ((c instanceof QTextEditor)
                            &&
                            (!iter.hasNext ())
                           )
                        {
                            
                            cons.fill = GridBagConstraints.BOTH;
                            cons.weightx = 1;
                            cons.weighty = 1;
                                                    
                        }
                
                        if ((c instanceof TextArea)
                            &&
                            (!iter.hasNext ())
                           )
                        {
                            
                            cons.fill = GridBagConstraints.BOTH;
                            cons.weightx = 1;
                            cons.weighty = 1;
                                                    
                        }
    
                        if ((c instanceof JEditorPane)
                            &&
                            (!iter.hasNext ())
                           )
                        {
                            
                            cons.fill = GridBagConstraints.BOTH;
                            cons.weightx = 1;
                            cons.weighty = 1;
    
                        }

                    }
                    
                    b.add (c,
                           cons);
                    
                    r++;
                    
                } else {

                    cons.insets = new Insets (top, 10, 0, 0);

                    if (!addFiller)
                    {

                        if ((c instanceof QTextEditor)
                            &&
                            (!iter.hasNext ())
                           )
                        {
                            
                            cons.fill = GridBagConstraints.BOTH;
                            cons.weightx = 1;
                            cons.weighty = 1;
                                                    
                        }
                
                        if ((c instanceof TextArea)
                            &&
                            (!iter.hasNext ())
                           )
                        {
                            
                            cons.fill = GridBagConstraints.BOTH;
                            cons.weightx = 1;
                            cons.weighty = 1;
                                                    
                        }
    
                        if ((c instanceof JEditorPane)
                            &&
                            (!iter.hasNext ())
                           )
                        {
                            
                            cons.fill = GridBagConstraints.BOTH;
                            cons.weightx = 1;
                            cons.weighty = 1;
                                                    
                        }

                    }

                    b.add (c,
                           cons);
                        
                    r++;
                        
                }
                
            }
                        
        }

        if (addFiller)
        {
                  
            this.addColumnFiller (b);

        }
   
        b.setAlignmentY (Component.TOP_ALIGNMENT);
        b.setAlignmentX (Component.LEFT_ALIGNMENT);
        b.setOpaque (false);

        return b;
                    
    }

    private void addColumnFiller (JComponent addTo)
    {

        GridBagConstraints cons = new GridBagConstraints ();

        cons.gridx = 0;
        cons.gridy = GridBagConstraints.RELATIVE;
        cons.gridwidth = 1;
        cons.gridheight = 1;
        cons.insets = new Insets (0, 0, 0, 0);
        cons.anchor = GridBagConstraints.NORTHWEST;

        cons.weightx = 1;
        cons.weighty = 1;
        cons.fill = GridBagConstraints.BOTH;
        
        JPanel p = new JPanel ();
        p.setOpaque (false);
        p.setPreferredSize (new Dimension (0, 0));
        
        addTo.add (p,
                   cons);
        
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
            
        JComponent p = this.createTwoColumnsEditLayout (col1,
                                            250,
                                            true,
                                            col2,
                                            250,
                                            true);
        
        p.setBorder (UIUtils.createPadding (0, 0, 0, 7));
        JScrollPane sp = UIUtils.createScrollPane (p);
        sp.setBorder (null);
        
        return sp;
        
    }

    private JScrollPane createSingleColumnEditLayout (Set<UserConfigurableObjectFieldViewEditHandler> handlers,
                                                      ActionListener                                formSave)
    {
        
        Set<FormItem> col = new LinkedHashSet ();
    
        for (UserConfigurableObjectFieldViewEditHandler h : handlers)
        {
        
            Set<FormItem> fits = h.getInputFormItems (null,
                                                      formSave);

            col.addAll (fits);

        }
        
        final JComponent colC = this.createStackedLayoutColumn (col,
                                                                true);
                              
        colC.setBorder (UIUtils.createPadding (5, 0, 5, 7));
                                           
        colC.setPreferredSize (new Dimension (50, colC.getPreferredSize ().height));          

        JScrollPane sp = UIUtils.createScrollPane (colC);
        sp.setBorder (null);

        return sp;
        
    }

    private JScrollPane createSingleColumnViewLayout (Set<UserConfigurableObjectFieldViewEditHandler> handlers)
    {
        
        Set<FormItem> col = new LinkedHashSet ();
    
        for (UserConfigurableObjectFieldViewEditHandler h : handlers)
        {
        
            Set<FormItem> fits = h.getViewFormItems ();

            col.addAll (fits);

        }
        
        return this.createSingleColumnViewLayoutForItems (col);
        
    }

    private JScrollPane createSingleColumnViewLayoutForItems (Set<FormItem> items)
    {
                
        final JComponent colC = this.createStackedLayoutColumn (items,
                                                                true);
                              
        colC.setBorder (UIUtils.createPadding (5, 0, 5, 5));
                                           
        colC.setPreferredSize (new Dimension (50, colC.getPreferredSize ().height));          

        JScrollPane sp = UIUtils.createScrollPane (colC);
        sp.setBorder (null);

        return sp;
        
    }

    private JScrollPane createTwoColumnsLayout (Set<FormItem> col1,
                                                int           col1Width,
                                                boolean       col1Grow,
                                                Set<FormItem> col2,
                                                int           col2Width,
                                                boolean       col2Grow)
    {

        final JComponent col1C = this.createStackedLayoutColumn (col1,
                                                                 (!col1Grow || (col1Grow && col2Grow)));
                                           
        col1C.setPreferredSize (new Dimension (col1Width, col1C.getPreferredSize ().height));          
                           
        final JComponent col2C = this.createStackedLayoutColumn (col2,
                                                                 (!col2Grow || (col1Grow && col2Grow)));

        col2C.setPreferredSize (new Dimension (col2Width, col2C.getPreferredSize ().height));        

        col1C.setOpaque (false);
        col2C.setOpaque (false);                        

        GridBagLayout layout = new GridBagLayout ();

        final JPanel p = new JPanel (layout);
                
        p.setOpaque (false);
        p.setBorder (UIUtils.createPadding (3, 0, 0, 0));

        GridBagConstraints cons = new GridBagConstraints ();
        cons.gridx = 0;
        cons.gridy = 0;
        cons.gridwidth = 1;
        cons.gridheight = 1;
        cons.fill = GridBagConstraints.NONE;        
        cons.weightx = 0;
        cons.weighty = 0;
        cons.insets = new Insets (0, 0, 0, 15);
        cons.anchor = GridBagConstraints.NORTH;
        
        if ((col1Grow)
            &&
            (!col2Grow)
           )
        {
            
            cons.weightx = 1;
            cons.weighty = 1;
            cons.fill = GridBagConstraints.BOTH;        

        } 
        
        if ((col1Grow)
            &&
            (col2Grow)
           )
        {
            
            cons.weightx = 0.5;
            cons.weighty = 1;
            cons.fill = GridBagConstraints.BOTH;        

        } 

        p.add (col1C,
               cons);

        cons = new GridBagConstraints ();
        cons.gridx = 1;
        cons.gridy = 0;
        cons.gridwidth = 1;
        cons.gridheight = 1;
        cons.fill = GridBagConstraints.NONE;        
        cons.weightx = 0;
        cons.weighty = 0;
        cons.insets = new Insets (0, 0, 0, 0);
        cons.anchor = GridBagConstraints.NORTH;

        if ((!col1Grow)
            &&
            (col2Grow)
           )
        {

            cons.weightx = 1;
            cons.weighty = 1;
            cons.fill = GridBagConstraints.BOTH;        
            
        }

        if ((col1Grow)
            &&
            (col2Grow)
           )
        {
            
            cons.weightx = 0.5;
            cons.weighty = 1;
            cons.fill = GridBagConstraints.BOTH;        

        } 
        
        p.add (col2C,
               cons);
        
        JScrollPane sp = UIUtils.createScrollPane (p);
        sp.setBorder (null);
                 
        return sp;

    }

    private JComponent createTwoColumnsEditLayout (Set<FormItem> col1,
                                                int           col1Width,
                                                boolean       col1Grow,
                                                Set<FormItem> col2,
                                                int           col2Width,
                                                boolean       col2Grow)
    {

        final JComponent col1C = this.createStackedLayoutColumn (col1,
                                                                 (!col1Grow || (col1Grow && col2Grow)));
                                           
        col1C.setPreferredSize (new Dimension (col1Width, col1C.getPreferredSize ().height));          
                           
        final JComponent col2C = this.createStackedLayoutColumn (col2,
                                                                 (!col2Grow || (col1Grow && col2Grow)));

        col2C.setPreferredSize (new Dimension (col2Width, col2C.getPreferredSize ().height));        

        col1C.setOpaque (false);
        col2C.setOpaque (false);                        

        GridBagLayout layout = new GridBagLayout ();

        final JPanel p = new JPanel (layout);
                
        p.setOpaque (false);
        p.setBorder (UIUtils.createPadding (0, 0, 0, 0));

        GridBagConstraints cons = new GridBagConstraints ();
        cons.gridx = 0;
        cons.gridy = 0;
        cons.gridwidth = 1;
        cons.gridheight = 1;
        cons.fill = GridBagConstraints.NONE;        
        cons.weightx = 0;
        cons.weighty = 0;
        cons.insets = new Insets (0, 0, 0, 15);
        cons.anchor = GridBagConstraints.NORTH;
        
        if ((col1Grow)
            &&
            (!col2Grow)
           )
        {
            
            cons.weightx = 1;
            cons.weighty = 1;
            cons.fill = GridBagConstraints.BOTH;        

            Box b = new Box (BoxLayout.X_AXIS);

            b.add (col1C);

            col1C.setBorder (UIUtils.createPadding (3, 2, 0, 5));  

            Box _b = new ScrollableBox (BoxLayout.Y_AXIS);
            _b.add (col2C);
            _b.setBorder (UIUtils.createPadding (3, 15, 0, 7));

            JScrollPane sp = UIUtils.createScrollPane (_b);
            sp.setBorder (null);
            sp.setAlignmentY (Component.TOP_ALIGNMENT);
            sp.setAlignmentX (Component.LEFT_ALIGNMENT);
            
            b.add (sp);
            
            b.setAlignmentY (Component.TOP_ALIGNMENT);
            b.setAlignmentX (Component.LEFT_ALIGNMENT);
            
            return b;

        } 
        
        if ((col1Grow)
            &&
            (col2Grow)
           )
        {
            
            cons.weightx = 0.5;
            cons.weighty = 1;
            cons.fill = GridBagConstraints.BOTH;        

        } 

        p.add (col1C,
               cons);

        cons = new GridBagConstraints ();
        cons.gridx = 1;
        cons.gridy = 0;
        cons.gridwidth = 1;
        cons.gridheight = 1;
        cons.fill = GridBagConstraints.NONE;        
        cons.weightx = 0;
        cons.weighty = 0;
        cons.insets = new Insets (0, 0, 0, 0);
        cons.anchor = GridBagConstraints.NORTH;

        if ((!col1Grow)
            &&
            (col2Grow)
           )
        {

            cons.weightx = 1;
            cons.weighty = 1;
            cons.fill = GridBagConstraints.BOTH;        

            Box b = new Box (BoxLayout.X_AXIS);

            Box _b = new ScrollableBox (BoxLayout.Y_AXIS);
            _b.add (col1C);
            _b.setBorder (UIUtils.createPadding (3, 2, 0, 7));
            col1C.setMinimumSize (new Dimension (col1Width, 200));

            JScrollPane sp = UIUtils.createScrollPane (_b);
            sp.setBorder (null);
            sp.setAlignmentY (Component.TOP_ALIGNMENT);
            sp.setAlignmentX (Component.LEFT_ALIGNMENT);
            
            b.add (sp);

            col2C.setBorder (UIUtils.createPadding (3, 7, 0, 7));

            b.add (col2C);
            
            b.setAlignmentY (Component.TOP_ALIGNMENT);
            b.setAlignmentX (Component.LEFT_ALIGNMENT);
            
            return b;
            
        }

        if ((col1Grow)
            &&
            (col2Grow)
           )
        {
            
            cons.weightx = 0.5;
            cons.weighty = 1;
            cons.fill = GridBagConstraints.BOTH;        

        } 
        
        p.add (col2C,
               cons);
        
        return p;
        
        //JScrollPane sp = UIUtils.createScrollPane (p);
        //sp.setBorder (null);
                 
        //return sp;

    }

    private JScrollPane XcreateTwoColumnsLayout (Set<FormItem> col1,
                                                int           col1Width,
                                                boolean       col1Grow,
                                                Set<FormItem> col2,
                                                int           col2Width,
                                                boolean       col2Grow)
    {
            
        JComponent col1C = this.createStackedLayoutColumn (col1,
                                                           !col1Grow);
                                        
        JComponent col2C = this.createStackedLayoutColumn (col2,
                                                           !col2Grow);

        col1C.setOpaque (false);
        col2C.setOpaque (false);                        
                                
        Box b = new /*Scrollable*/Box (BoxLayout.X_AXIS);
        b.setAlignmentX (Component.LEFT_ALIGNMENT);
        b.add (col1C);
        b.add (Box.createHorizontalStrut (15));
        b.add (col2C);

        b.setBorder (UIUtils.createPadding (5, 0, 5, 5));
        b.setMinimumSize (new Dimension (col1Width + 15 + col2Width,
                                         300));
        
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
            ||
            ((component instanceof JScrollPane)
             &&
             (((JScrollPane) component).getViewport ().getView () instanceof JList)
            )
           )
        {

            component.setMinimumSize (component.getPreferredSize ());
            component.setMaximumSize (component.getPreferredSize ());
        
            Box tb = new Box (BoxLayout.X_AXIS);
            tb.add (component);
            tb.add (Box.createHorizontalGlue ());
            tb.setAlignmentY (Component.TOP_ALIGNMENT);
            tb.setAlignmentX (Component.LEFT_ALIGNMENT);
            /*
            tb.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                              component.getPreferredSize ().height));
              */                                
            component = tb;
            
        }

        return component;
    
    }        
    
}
