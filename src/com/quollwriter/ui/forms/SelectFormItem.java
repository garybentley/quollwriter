package com.quollwriter.ui.forms;

import java.util.Vector;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.List;

import java.awt.Dimension;
import java.awt.Component;

import javax.swing.*;
import javax.swing.event.*;

import com.quollwriter.ui.*;

public class SelectFormItem extends FormItem<Set<String>>
{
    
    private JList<String> list = null;
    private int maxCount = 0;
    private boolean required = false;
    private int visibleRows = 0;
    
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

        if (visibleRowCount < 1)
        {
            
            visibleRowCount = 1;
            
        }

        this.visibleRows = visibleRowCount;
        this.list = new JList<String> (items);
        
        this.list.setCellRenderer (new DefaultListCellRenderer ()
        {
           
            @Override
            public Component getListCellRendererComponent (JList list,
                                                  Object        value,
                                                  int           index,
                                                  boolean       isSelected,
                                                  boolean       cellHasFocus)
            {
                
                super.getListCellRendererComponent (list,
                                                    value,
                                                    index,
                                                    isSelected,
                                                    cellHasFocus);
                
                this.setBorder (UIUtils.createBottomLineWithPadding (2, 2, 2, 2));
                
                return this;
                
            }
            
        });            
        
        if (visibleRowCount == 1)
        {
            
            this.list.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
            
        }
        
        this.list.setVisibleRowCount (visibleRowCount > items.size () ? items.size () : visibleRowCount);
        this.required = itemsRequired;
    
        this.maxCount = maxSelectedCount;
    
        final SelectFormItem _this = this;
    
        this.list.addListSelectionListener (new ListSelectionListener ()
        {
               
            public void valueChanged (ListSelectionEvent ev)
            {
                                            
                //_this.updateRequireLabel ();
                
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

        this.list.setPreferredSize (new Dimension (Math.max (150, this.list.getPreferredSize ().width),
                                                   this.list.getPreferredSize ().height));

        if (this.visibleRows < 5)
        {

            return this.list;
            
        }

        JComponent c = UIUtils.createScrollPane (this.list);
                
        return c;

    }

    public Set<String> getValue ()
    {
                    
        Set<String> ret = new LinkedHashSet (this.list.getSelectedValuesList ());

        return ret;
        
    }
    
    public boolean hasError ()
    {
        
        if (!this.required)
        {
            
            return false;
            
        }
        
        List<String> sel = this.list.getSelectedValuesList ();
        
        int c = 0;
        
        if ((sel != null)
            &&
            (sel.size () > this.maxCount)
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
    
        List<String> sel = this.list.getSelectedValuesList ();
        
        int c = 0;
        
        if (sel != null)
        {
            
            c = sel.size ();
            
        }
    
        //this.setError (false);
    
        if (!this.required)
        {
            
            return;
            
        }
    
        if (c > 0)
        {
            
            if (c > this.maxCount)
            {
                
                c = this.maxCount;

                //this.setError (true);
                
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
