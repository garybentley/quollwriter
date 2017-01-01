package com.quollwriter.data;

import java.awt.Dimension;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

import java.math.*;

import javax.swing.*;

import com.quollwriter.ui.components.FormItem;

public class NumberUserObjectTypeField extends UserConfigurableObjectTypeField
{

    public static final String MAX = "max";
    public static final String MIN = "min";

    private JSpinner minsp = null;
    private JSpinner maxsp = null;
    
    private BigDecimal minVal = null;
    private BigDecimal maxVal = null;
            
    public NumberUserObjectTypeField ()
    {
        
        super (Type.number);

        SpinnerNumberModel minsm = new SpinnerNumberModel ();

        this.minsp = new JSpinner (minsm);
    
        this.minsp.setPreferredSize (new Dimension (100,
                                                    this.minsp.getPreferredSize ().height));

        SpinnerNumberModel maxsm = new SpinnerNumberModel ();

        this.maxsp = new JSpinner (maxsm);
    
        this.maxsp.setPreferredSize (new Dimension (100,
                                                    this.maxsp.getPreferredSize ().height));
        
    }

    public void initConfiguration (Map initVal)
    {
        
        String min = (String) initVal.get (MIN);
        String max = (String) initVal.get (MAX);
        
        if (min != null)
        {
        
            this.minVal = new BigDecimal (min);
            
        }
        
        if (max != null)
        {
            
            this.maxVal = new BigDecimal (max);
            
        }
        
    }
    
    public void fillConfiguration (Map m)
    {
                    
        if (this.minVal != null)
        {
            
            // We use a string here.
            m.put (MIN,
                   this.minVal.toString ());
            
        }
        
        if (this.maxVal != null)
        {
            
            m.put (MAX,
                   this.maxVal.toString ());
            
        }

    }
    
    public String getConfigurationDescription ()
    {
        
        StringBuilder b = new StringBuilder ();
        
        if (this.minVal != null)
        {
            
            b.append ("min: " + this.minVal.toString ());
            
        }
        
        if (this.maxVal != null)
        {
            
            if (b.length () > 0)
            {
                
                b.append (", ");
                
            }
            
            b.append ("max: " + this.maxVal.toString ());
            
        }
        
        return b.toString ();

    }

    public boolean updateFromExtraFormItems ()
    {
        
        this.minVal = this.convertMin ();
        this.maxVal = this.convertMax ();

        return true;
        
    }
    
    private BigDecimal convertMax ()
    {
        
        return this.convert ((Number) this.maxsp.getValue ());
        
    }
    
    private BigDecimal convertMin ()
    {
        
        return this.convert ((Number) this.minsp.getValue ());
        
    }

    private BigDecimal convert (Number n)
    {
        
        return new BigDecimal (((Number) n).toString ());
        
    }
    
    public Set<String> getExtraFormItemErrors ()
    {
        
        Set<String> errors = new LinkedHashSet ();

        Number min = (Number) this.minsp.getValue ();
        Number max = (Number) this.maxsp.getValue ();

        if (this.convertMin ().compareTo (this.convertMax ()) > 0)
        {
                        
            errors.add ("The maximum value must be greater than the minimum.");
            
        }
        
        return errors;
        
    }

    public Set<FormItem> getExtraFormItems ()
    {
     
        Set<FormItem> nitems = new LinkedHashSet ();

        nitems.add (new FormItem ("Minimum",
                                  this.minsp));
            
        nitems.add (new FormItem ("Maximum",
                                  this.maxsp));

        return nitems;
    
    }

}
