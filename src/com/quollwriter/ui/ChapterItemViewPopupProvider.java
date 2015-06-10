package com.quollwriter.ui;

import javax.swing.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.components.QPopup;

public interface ChapterItemViewPopupProvider
{
    
    public QPopup getViewPopup (ChapterItem         it,
                                AbstractEditorPanel panel)
                         throws GeneralException;
    
}