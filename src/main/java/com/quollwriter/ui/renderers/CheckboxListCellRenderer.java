package com.quollwriter.ui.renderers;

import java.awt.Component;

import javax.swing.*;
import javax.swing.border.*;

public class CheckboxListCellRenderer extends JCheckBox implements ListCellRenderer
{

    public CheckboxListCellRenderer()
    {


    }

    public Component getListCellRendererComponent (JList   list,
                                                   Object  v,
                                                   int     index,
                                                   boolean isSelected,
                                                   boolean cellHasFocus)
    {

        this.setBorder (new EmptyBorder (2, 2, 2, 2));

        this.setText (v.toString ());

        if (isSelected)
        {
            setBackground (list.getSelectionBackground ());
            setForeground (list.getSelectionForeground ());
        } else
        {
            setBackground (list.getBackground ());
            setForeground (list.getForeground ());
        }

        setEnabled (list.isEnabled ());
        setFont (list.getFont ());
        setOpaque (true);

        return this;

    }

}
