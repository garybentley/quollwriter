package com.quollwriter.ui;

import javax.swing.*;

import org.jfree.chart.*;

import com.quollwriter.*;
import com.quollwriter.ui.panels.*;

public interface QuollChart
{
    
    public String getTitle ();
    
    public String getType ();
    
    public JComponent getControls (boolean update)
                            throws GeneralException;
    
    public JFreeChart getChart (boolean update)
                         throws GeneralException;
    
    public JComponent getDetail (boolean update)
                          throws GeneralException;
    
    public void init (StatisticsPanel wcp)
               throws GeneralException;
    
}