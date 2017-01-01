package com.quollwriter.data;

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

import com.quollwriter.ui.*;
import com.quollwriter.ui.components.FormItem;

public class SelectUserConfigurableObjectTypeField extends UserConfigurableObjectTypeField
{

    public static final String ITEMS = "items";
    public static final String ALLOWMULTI = "allowmulti";

    private TextArea editItems = null;
    private JCheckBox editAllowMulti = null;

    public SelectUserConfigurableObjectTypeField ()
    {
        
        super (Type.select);

        this.editItems = new TextArea ("Enter the items that can be selected, separate each item by a comma (,) or a semi-colon (;).",
                                       3,
                                       -1);

        this.editAllowMulti = UIUtils.createCheckBox ("Allow multiple items to be selected");
        
    }
        
    public void setItems (Collection<String> its)
    {
        
        this.setDefinitionValue (ITEMS, new ArrayList (its));
        
    }
    
    public Collection<String> getItems ()
    {
        
        return (Collection<String>) this.getDefinitionValue (ITEMS);
        
    }
    
    @Override
    public boolean isSearchable ()
    {
        
        return true;
        
    }        
    
    public void initConfiguration (Map initVal)
    {
                
    }
    
    public void fillConfiguration (Map m)
    {
                            
    }
    
    public String getConfigurationDescription ()
    {
        
        return "|todo|";
        
    }
    
    public boolean updateFromExtraFormItems ()
    {
        
        Set<String> items = new LinkedHashSet ();
        
        StringTokenizer t = new StringTokenizer (this.editItems.getText (),
                                                 ";,");

        while (t.hasMoreTokens ())
        {
            
            items.add (t.nextToken ().trim ());
            
        }

        this.setItems (items);
        
        this.setAllowMulti (this.editAllowMulti.isSelected ());
        
        return true;           
        
    }
    
    public boolean isAllowMulti ()
    {
        
        return (Boolean) this.getDefinitionValue (ALLOWMULTI);
        
    }
    
    public void setAllowMulti (boolean v)
    {
        
        this.setDefinitionValue (ALLOWMULTI, v);
        
    }
    
    public Set<String> getExtraFormItemErrors ()
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
    
    public Set<FormItem> getExtraFormItems ()
    {
     
        Set<FormItem> nitems = new LinkedHashSet ();
                     
        Collection<String> items = this.getItems ();
        
        StringBuilder b = new StringBuilder ();
        
        for (String i : items)
        {
            
            if (b.length () > 0)
            {
                
                b.append (", ");
                     
            }
            
            b.append (i);
            
        }
        
        this.editItems.setText (b.toString ());
        
        nitems.add (new FormItem ("Items",
                                  this.editItems));
                                 
        this.editAllowMulti.setSelected (this.isAllowMulti ());
                                 
        nitems.add (new FormItem (null,
                                  this.editAllowMulti));

        return nitems;
                                  
    }

}
