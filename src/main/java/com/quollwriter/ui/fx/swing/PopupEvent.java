package com.quollwriter.ui.fx.swing;

import java.util.*;

public class PopupEvent extends EventObject
{

    private QPopup popup = null;

    public PopupEvent (QPopup p)
    {

        super (p);

        this.popup = p;

    }

    public QPopup getPopup ()
    {

        return this.popup;

    }

}
