package com.quollwriter.ui;

import java.awt.*;
import javax.swing.*;

import com.quollwriter.events.*;
import com.quollwriter.ui.sidebars.*;

public interface SideBarsSupported
{

    public JPopupMenu getShowOtherSideBarsPopupSelector ();

    public int getActiveSideBarCount ();
    
    public AbstractSideBar getActiveOtherSideBar ();
    
    public void addSideBarListener (SideBarListener l);
    
    public void removeSideBarListener (SideBarListener l);
    
    public void closeSideBar ();

}
