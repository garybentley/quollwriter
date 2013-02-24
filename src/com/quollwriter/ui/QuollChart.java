package com.quollwriter.ui;

import javax.swing.*;

import org.jfree.chart.*;

import com.quollwriter.*;
import com.quollwriter.ui.panels.*;

public interface QuollChart
{
    
    public String getTitle ();
    
    public String getType ();
    
    public JComponent getControls ();
    
    public JFreeChart getChart ();
    
    public void init (AbstractProjectViewer pv,
                      WordCountPanel        wcp)
                      throws                GeneralException;
    
}