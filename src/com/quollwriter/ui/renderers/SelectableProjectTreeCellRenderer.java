package com.quollwriter.ui.renderers;

import java.awt.*;
import java.awt.event.*;

import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;


public class SelectableProjectTreeCellRenderer extends DefaultTreeCellRenderer
{

    private Map<String, Icon> icons = new HashMap ();

    private Map<String, String> iconTypes = new HashMap ();
    
    private boolean showIcons = true;
    
    public SelectableProjectTreeCellRenderer()
    {

    }

    /**
     * Override this method to indicate that the component associated with the
     * value.  This method always returns true.  The value will be a string or the user object associated
     * with the node.
     *
     * @returns A value indicating whether the component associated with the value should be enabled.
     */
    public boolean shouldEnable (Object value)
    {
       
       return true;
        
    }
    
    /**
     * Override to specify the tool tip text for the component associated with the value.
     * This method always returns null.  The value will be a string or the user object associated
     * with the node.
     *
     * @returns A value for the tool tip for the component associated with the value.
     */
    public String getToolTipText (Object value)
    {
        
        return null;
        
    }
    
    public void setIconType (String objType,
                             String iconType)
    {
        
        this.iconTypes.put (objType,
                            iconType);
        
    }
    
    public void setShowIcons (boolean v)
    {
        
        this.showIcons = v;
        
    }
    
    public Component getTreeCellRendererComponent (JTree   tree,
                                                   Object  value,
                                                   boolean sel,
                                                   boolean expanded,
                                                   boolean leaf,
                                                   int     row,
                                                   boolean hasFocus)
    {

        Box p = new Box (BoxLayout.X_AXIS);

        p.setOpaque (false);

        p.setToolTipText (this.getToolTipText (value));
        
        JLabel l = new JLabel ();
        l.setOpaque (false);

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

        value = node.getUserObject ();

        if (value instanceof String)
        {

            l.setText ((String) value);

            l.setToolTipText (this.getToolTipText (value));
            
            p.add (l);

            return p;

        }

        SelectableDataObject s = (SelectableDataObject) value;

        if (!s.parentNode)
        {

            JCheckBox b = new JCheckBox ();
            b.setOpaque (false);
            p.add (b);
            p.add (Box.createHorizontalStrut (5));
            
            if (this.shouldEnable (s.obj))
            {
            
                b.setSelected (s.selected);
                
            }
            
            b.setToolTipText (this.getToolTipText (s.obj));
            b.setEnabled (this.shouldEnable (s.obj));

        }

        p.add (l);

        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent ();

        if (parent != null)
        {

            SelectableDataObject sparent = (SelectableDataObject) parent.getUserObject ();

            if (!sparent.parentNode)
            {

                String ot = s.obj.getObjectType ();

                if (s.obj instanceof TreeParentNode)
                {

                    ot = ((TreeParentNode) s.obj).getForObjectType ();

                }

                String iot = this.iconTypes.get (ot);
                
                if (iot != null)
                {
                    
                    ot = iot;
                    
                }
                
                Icon ic = this.icons.get (ot);

                if (ic == null)
                {

                    ic = Environment.getIcon (ot,
                                              Constants.ICON_TREE);

                    this.icons.put (ot,
                                    ic);

                }

                if (this.showIcons)
                {
                
                    l.setIcon (ic);
                    
                } else {
                    
                    l.setIcon (null);
                    
                }

            }

        }

        l.setText (((NamedObject) s.obj).getName ());

        l.setEnabled (this.shouldEnable (s.obj));
        l.setToolTipText (this.getToolTipText (s.obj));
        
        UIUtils.setAsButton (p);

        p.setBorder (new EmptyBorder (2, 2, 2, 2));

        return p;

    }

}
