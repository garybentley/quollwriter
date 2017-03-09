package com.quollwriter.ui.actionHandlers;

import java.util.Set;

import java.awt.event.*;
import javax.swing.*;

import com.quollwriter.data.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.components.*;

public interface ChapterItemFormatDetails<E extends ChapterItem, V extends AbstractViewer>
{
    
    public abstract ActionListener getEditItemActionHandler (E                    item,
                                                             ChapterItemViewer<V> qep);

    public abstract ActionListener getDeleteItemActionHandler (E                    item,
                                                               ChapterItemViewer<V> qep,
                                                               boolean              showAtItem);

    public abstract Set<JComponent> getTools  (E                    item,
                                               ChapterItemViewer<V> qep);

    public abstract String getItemDescription (E item);

    public abstract String getIcon (E item);
    
    public abstract String getTitle (E item);    
    
}