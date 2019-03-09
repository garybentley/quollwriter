package com.quollwriter.ui.fx.swing;

import java.awt.Point;

import javax.swing.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.fx.viewers.*;

public interface ChapterItemViewer<E extends AbstractProjectViewer> extends PopupsSupported
{

    public E getViewer ();

    public int getIconColumnXOffset (ChapterItem c);

    public JScrollPane getScrollPane ();

    public Point getLastMousePosition ();

    public QTextEditor getEditor ();

    public IconColumn<E> getIconColumn ();

    public ChapterItemTransferHandler getChapterItemTransferHandler ();

    public <T extends ChapterItem> void removeItem (T c);

    public <T extends ChapterItem> void addItem (T c)
                                          throws GeneralException;

    public void highlightItemTextInEditor (ChapterItem c);

    public void removeItemHighlightTextFromEditor (ChapterItem c);

}
