package com.quollwriter.ui.fx;

import javafx.event.*;
import javafx.scene.*;
import javafx.geometry.*;

import com.quollwriter.ui.fx.components.*;

public interface PopupsViewer
{

    void showPopup (QuollPopup p,
                    double     x,
                    double     y);

    void showPopup (QuollPopup p,
                    Node       n,
                    Side       s);

   QuollPopup getPopupById (String id);

   void removePopup (QuollPopup p);

   void addPopup (QuollPopup p);

   <T extends Event> void addEventHandler (EventType<T>            type,
                                           EventHandler<? super T> handler);

}
