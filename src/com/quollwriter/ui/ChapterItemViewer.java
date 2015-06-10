package com.quollwriter.ui;

import java.awt.Point;

import javax.swing.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.components.QTextEditor;

public interface ChapterItemViewer
{
    
    public int getIconColumnXOffset (ChapterItem c);
    
    public JScrollPane getScrollPane ();
    
    public Point getLastMousePosition ();
    
    public QTextEditor getEditor ();
    
    public IconColumn getIconColumn ();
    
    public ChapterItemTransferHandler getChapterItemTransferHandler ();
 
    public void removeItem (ChapterItem c);

    public void addItem (ChapterItem c)
                  throws GeneralException;
                  
    public void highlightItemTextInEditor (ChapterItem c);
    
    public void removeItemHighlightTextFromEditor (ChapterItem c);
    
}