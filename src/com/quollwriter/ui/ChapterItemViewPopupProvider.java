package com.quollwriter.ui;

import javax.swing.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.components.QPopup;

public interface ChapterItemViewPopupProvider<V extends AbstractViewer>
{
    
    public QPopup getViewPopup (ChapterItem          it,
                                ChapterItemViewer<V> panel)
                         throws GeneralException;
    
}