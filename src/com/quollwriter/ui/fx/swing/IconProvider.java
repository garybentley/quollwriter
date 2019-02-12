package com.quollwriter.ui.fx.swing;

import javax.swing.*;

import com.quollwriter.data.*;

public interface IconProvider
{

    public ImageIcon getIcon (Object obj,
                              int    type);

    public ImageIcon getObjectIcon (String ot,
                                    int    iconType);

    public ImageIcon getObjectIcon (DataObject d,
                                    int        iconType);

}
