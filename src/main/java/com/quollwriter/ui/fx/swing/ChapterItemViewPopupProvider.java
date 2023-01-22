package com.quollwriter.ui.fx.swing;

import javax.swing.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.viewers.*;

public interface ChapterItemViewPopupProvider<V extends AbstractProjectViewer>
{

    public QPopup getViewPopup (ChapterItem          it,
                                ChapterItemViewer<V> panel)
                         throws GeneralException;

}
