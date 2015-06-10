package com.quollwriter.ui;

import java.awt.*;


// Really don't like this name, maybe PopupsViewer???
public interface PopupsSupported
{

    public void addPopup (Component c,
                          boolean   hideOnClick,
                          boolean   hideViaVisibility);

    public void showPopupAt (Component c,
                             Point     p,
                             boolean   hideOnClick);

    public void showPopupAt (Component c,
                             Component at,
                             boolean   hideOnClick);

    public void removePopup (Component c);

}
