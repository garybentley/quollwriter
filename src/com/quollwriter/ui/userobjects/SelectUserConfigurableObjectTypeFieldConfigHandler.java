package com.quollwriter.ui.userobjects;

import java.awt.Dimension;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import java.math.*;

import javax.swing.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.data.*;

public class SelectUserConfigurableObjectTypeFieldConfigHandler implements UserConfigurableObjectTypeFieldConfigHandler
{

    private MultiLineTextFormItem editItems = null;
    private JCheckBox editAllowMulti = null;
    private SelectUserConfigurableObjectTypeField field = null;

    public SelectUserConfigurableObjectTypeFieldConfigHandler (SelectUserConfigurableObjectTypeField field)
    {
        
        this.field = field;
        
        this.editItems = new MultiLineTextFormItem ("Items",
                                                    "Enter the items that can be selected, separate each item by a comma (,) or a semi-colon (;).",
                                                    3,
                                                    -1,
                                                    false,
                                                    null);
                                       
        this.editAllowMulti = UIUtils.createCheckBox ("Allow multiple items to be selected");
        
    }
                
    @Override    
    public boolean updateFromExtraFormItems ()
    {
        
        Set<String> items = new LinkedHashSet ();
        
        StringTokenizer t = new StringTokenizer (this.editItems.getText (),
                                                 ";,");

        while (t.hasMoreTokens ())
        {
            
            items.add (t.nextToken ().trim ());
            
        }

        this.field.setItems (items);
        
        this.field.setAllowMulti (this.editAllowMulti.isSelected ());
        
        return true;           
        
    }
        
    @Override
    public Set<String> getExtraFormItemErrors (UserConfigurableObjectType objType)
    {
        
        Set<String> errors = new LinkedHashSet ();
                    
        StringTokenizer t = new StringTokenizer (this.editItems.getText (),
                                                 ";,");
        
        if (t.countTokens () == 0)
        {
            
            errors.add ("At least one item must be specified.");
            
        }
                    
        return errors;
        
    }
    
    @Override
    public Set<FormItem> getExtraFormItems ()
    {
     
        Set<FormItem> nitems = new LinkedHashSet ();
                     
        Collection<String> items = this.field.getItems ();
        
        StringBuilder b = new StringBuilder ();
        
        if (items != null)
        {
        
            for (String i : items)
            {
                
                if (b.length () > 0)
                {
                    
                    b.append (", ");
                         
                }
                
                b.append (i);
                
            }

        }
        
        if (b.length () > 0)
        {
        
            this.editItems.setText (b.toString ());
            
        }
        
        nitems.add (this.editItems);
                                 
        this.editAllowMulti.setSelected (this.field.isAllowMulti ());
                                 
        nitems.add (new AnyFormItem (null,
                                     this.editAllowMulti));

        return nitems;
                                  
    }

    @Override
    public String getConfigurationDescription ()
    {
        
        Set<String> strs = new LinkedHashSet ();

        strs.add ("select list");
        
        if (this.field.isAllowMulti ())
        {
            
            strs.add ("can select multiple items");
            
        }

        Collection<String> items = this.field.getItems ();

        if (items.size () > 0)
        {
            
            StringBuilder b = new StringBuilder ("items: ");
        
            int i = 0;
            
            for (String v : items)
            {
                
                if (i > 0)
                {
                    
                    b.append (", ");
                    
                }
                
                b.append (v);
                
                i++;
                
                if (i == 5)
                {
                    
                    break;
                    
                }
                
            }
        
            strs.add (b.toString ());
        
            if (items.size () > 5)
            {
                
                strs.add (String.format (", +%s others",
                                         Environment.formatNumber (items.size () - 5)));
                
            }
        
        }
        
        return Utils.joinStrings (strs,
                                  null);
                        
    }
    
}
