package com.quollwriter.ui.fx.swing;

import java.util.*;

public interface PopupListener extends EventListener
{

    public void popupShown (PopupEvent ev);

    public void popupHidden (PopupEvent ev);

    public void popupResized (PopupEvent ev);

}
