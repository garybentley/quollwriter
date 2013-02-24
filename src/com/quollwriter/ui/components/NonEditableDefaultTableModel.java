package com.quollwriter.ui.components;

import javax.swing.table.*;


public class NonEditableDefaultTableModel extends DefaultTableModel
{

    public boolean isCellEditable (int row,
                                   int col)
    {

        return false;

    }

}
