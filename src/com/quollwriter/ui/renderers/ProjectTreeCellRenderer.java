package com.quollwriter.ui.renderers;

import java.awt.*;

import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;


public class ProjectTreeCellRenderer extends DefaultTreeCellRenderer
{

    private Map icons = new HashMap ();
    private boolean showObjectIcons = false;

    public ProjectTreeCellRenderer (boolean showObjectIcons)
    {

        this.showObjectIcons = showObjectIcons;
    
    }

    public Component getTreeCellRendererComponent (JTree   tree,
                                                   Object  value,
                                                   boolean sel,
                                                   boolean expanded,
                                                   boolean leaf,
                                                   int     row,
                                                   boolean hasFocus)
    {

        super.getTreeCellRendererComponent (tree,
                                            value,
                                            sel,
                                            expanded,
                                            leaf,
                                            row,
                                            hasFocus);

        this.setBorder (new EmptyBorder (2, 2, 2, 2));
        
        this.setOpaque (false);
                                            
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

        value = node.getUserObject ();
        
        if (value instanceof DataObject)
        {

            DataObject d = (DataObject) value;

            String ot = null;

            if (d instanceof Note)
            {

                ot = "bullet-black";

            }
            
            if ((d instanceof OutlineItem)
                ||
                (d instanceof Scene)
                ||
                ((this.showObjectIcons)
                 &&
                 (!(value instanceof BlankNamedObject))
                )
               )
            {
                
                ot = d.getObjectType ();
                
            }
            /*
            if (value instanceof TreeParentNode)
            {

                TreeParentNode tpn = (TreeParentNode) value;

                ot = tpn.getForObjectType ();

                if ((ot.equals (Note.OBJECT_TYPE)) &&
                    (Note.EDIT_NEEDED_NOTE_TYPE.equals (tpn.getName ())))
                {

                    ot = "edit-needed-note";

                } else
                {

                    if (ot.equals (QCharacter.OBJECT_TYPE))
                    {

                        ot = ot + "-multi";

                    }

                    
                    
                }

            }
*/
            
            Icon ic = null;
            
            if (ot != null)
            {
                
                ic = (Icon) this.icons.get (ot);

                if (ic == null)
                {
    
                    ic = Environment.getIcon (ot,
                                              Constants.ICON_TREE);
    
                    this.icons.put (ot,
                                    ic);
    
                }
    
            }

            this.setIcon (ic);

        }

        if (value instanceof NamedObject)
        {

            String n = ((NamedObject) value).getName ();
            
            if (value instanceof TreeParentNode)
            {
                
                TreeParentNode t = (TreeParentNode) value;
                
                if (t.getCount () > -1)
                {
                    
                    n += " (" + t.getCount () + ")";
                    
                }
                
            }
        
            this.setText (n);

        }

        this.setToolTipText ("Click to view");
        this.setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));

        return this;

    }
    
}
