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
    private JComboBox<String> dlist = null;
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

        ListCellRenderer rend = new DefaultListCellRenderer ()
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
            
        };

        if (visibleRowCount < 1)
        {
            
            visibleRowCount = 1;
            
        }

        this.visibleRows = visibleRowCount;
        
        if (visibleRowCount == 1)
        {
            
            this.dlist = new JComboBox<String> (items);
            
            this.dlist.setRenderer (rend);

            if ((selected != null)
                &&
                (selected.size () > 0)
               )
            {
            
                this.dlist.setSelectedItem (selected.iterator ().next ());

            }
            
        } else {
        
            this.list = new JList<String> (items);
        
            this.list.setCellRenderer (rend);            

            this.list.setVisibleRowCount (visibleRowCount > items.size () ? items.size () : visibleRowCount);

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
                
        this.required = itemsRequired;
    
        this.maxCount = maxSelectedCount;
            
    }

    public void addListSelectionListener (ListSelectionListener l)
    {
        
        this.list.addListSelectionListener (l);
        
    }
    
    public JComponent getComponent ()
    {

        if (this.list != null)
        {

            this.list.setPreferredSize (new Dimension (Math.max (150, this.list.getPreferredSize ().width),
                                                       this.list.getPreferredSize ().height));
    
            if (this.visibleRows < 5)
            {
    
                this.list.setBorder (UIUtils.createLineBorder ());
    
                return this.list;
                
            }
    
            JComponent c = UIUtils.createScrollPane (this.list);
                    
            return c;

        }
        
        this.dlist.setPreferredSize (new Dimension (Math.max (150, this.dlist.getPreferredSize ().width),
                                                    this.dlist.getPreferredSize ().height));

        return this.dlist;

    }

    public Set<String> getValue ()
    {
            
        Set<String> ret = new LinkedHashSet ();

        if (this.list != null)
        {
            
            if (this.list.getSelectedValuesList () == null)
            {
                
                return ret;
                
            }
            
            ret.addAll (this.list.getSelectedValuesList ());
    
            return ret;

        } else {
                
            if (this.dlist.getSelectedItem () == null)
            {
                
                return ret;
                        
            }
            
            ret.add (this.dlist.getSelectedItem ().toString ());
                        
        }
        
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
