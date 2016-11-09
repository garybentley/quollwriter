package com.quollwriter.ui.actionHandlers;

import java.util.Set;

import java.awt.event.*;
import javax.swing.*;

import com.quollwriter.data.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.components.*;

public interface ChapterItemFormatDetails<E extends ChapterItem>
{
    
    public abstract AbstractActionHandler getEditItemActionHandler (E                   item,
                                                                    AbstractEditorPanel qep);

    public abstract ActionListener getDeleteItemActionHandler (E                   item,
                                                               AbstractEditorPanel qep,
                                                               boolean             showAtItem);

    public abstract Set<JComponent> getTools  (E                   item,
                                               AbstractEditorPanel qep);

    public abstract String getItemDescription (E item);

    public abstract String getIcon (E item);
    
    public abstract String getTitle (E item);    
    
}