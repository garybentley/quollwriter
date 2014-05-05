package com.quollwriter.ui.forms;

import java.util.Vector;
import java.util.Set;
import java.util.LinkedHashSet;

import java.awt.Dimension;

import javax.swing.*;
import javax.swing.event.*;

public class SelectFormItem extends FormItem<Set<String>>
{
    
    private JList list = null;
    private int maxCount = 0;
    private boolean required = false;
    
    public SelectFormItem (String label,
                           Vector<String> items,
                           int            visibleRowCount,
                           Set<String>    selected,
                           int            maxSelectedCount,
                           boolean        itemsRequired,
                           String         helpText)
    {
        
        super (label,
               (maxSelectedCount > 0 ? itemsRequired : false),
               helpText);

        this.list = new JList (items);
        this.list.setVisibleRowCount (visibleRowCount);
        this.required = itemsRequired;
    
        this.maxCount = maxSelectedCount;
    
        final SelectFormItem _this = this;
    
        this.list.addListSelectionListener (new ListSelectionListener ()
        {
               
            public void valueChanged (ListSelectionEvent ev)
            {
                                            
                _this.updateRequireLabel ();
                
            }
    
        });        
        
        if (selected != null)
        {
        
            for (int i = 0; i < this.list.getModel ().getSize (); i++)
            {
        
                for (String s : selected)
                {
                 
                    String sv = (String) this.list.getModel ().getElementAt (i);
                 
                    if (sv.equals (s))
                    {
                    
                        this.list.getSelectionModel ().addSelectionInterval (i, i);
                    
                    }
                    
                }

            }
                
        }
        
    }

    public void addListSelectionListener (ListSelectionListener l)
    {
        
        this.list.addListSelectionListener (l);
        
    }
    
    public JComponent getComponent ()
    {

        JScrollPane slist = new JScrollPane (this.list);
        slist.setPreferredSize (new Dimension (150,
                                                   100));
        slist.setMaximumSize (new Dimension (150,
                                                   100));
        
        return slist;

    }

    public Set<String> getValue ()
    {
                    
        Set<String> ret = new LinkedHashSet ();
        
        Object[] sel = this.list.getSelectedValues ();
        
        for (int i = 0; i < sel.length; i++)
        {
            
            ret.add (sel[i].toString ());
            
        }
        
        return ret;
        
    }
    
    public boolean hasError ()
    {
        
        if (!this.required)
        {
            
            return false;
            
        }
        
        Object[] sel = this.list.getSelectedValues ();
        
        int c = 0;
        
        if ((sel != null)
            &&
            (sel.length > this.maxCount)
           )
        {

            return true;
        
        }            
        
        return false;
        
    }
    
    public void updateRequireLabel (JLabel requireLabel)
    {

        if (this.maxCount < 0)
        {
            
            requireLabel.setVisible (false);
            return;
            
        }
    
        Object[] sel = this.list.getSelectedValues ();
        
        int c = 0;
        
        if (sel != null)
        {
            
            c = sel.length;
            
        }
    
        this.setError (false);
    
        if (!this.required)
        {
            
            return;
            
        }
    
        if (c > 0)
        {
            
            if (c > this.maxCount)
            {
                
                c = this.maxCount;

                this.setError (true);
                
            }
            
            requireLabel.setText (String.format ("(select up to %s , %s remaining)",
                                                       this.maxCount,
                                                       (this.maxCount - c)));
            
        } else {
            
            requireLabel.setText (String.format ("(select up to %s)",
                                                       this.maxCount));
            
        }            
    
    
    }
    
}
