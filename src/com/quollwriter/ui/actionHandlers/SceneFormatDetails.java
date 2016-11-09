package com.quollwriter.ui.actionHandlers;

import java.util.Set;

import java.awt.event.ActionListener;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;

public class SceneFormatDetails implements ChapterItemFormatDetails<Scene>
{

    public String getIcon (Scene item)
    {
        
        return item.getObjectType ();
        
    }
    
    public String getTitle (Scene item)
    {
        
        return Environment.getObjectTypeName (item.getObjectType ());
        
    }

    public String getItemDescription (Scene item)
    {

        return item.getDescription ().getMarkedUpText ();

    }
    
    @Override
    public Set<JComponent> getTools (Scene               item,
                                     AbstractEditorPanel qep)
    {
        
        return null;
        
    }    
    
    public AbstractActionHandler getEditItemActionHandler (Scene               item,
                                                           AbstractEditorPanel ep)
    {

        return new ChapterItemActionHandler (item,
                                             ep,
                                             AbstractActionHandler.EDIT,
                                             -1);

    }

    public ActionListener getDeleteItemActionHandler (Scene               item,
                                                      AbstractEditorPanel ep,
                                                      boolean             showAtItem)
    {

        return new DeleteChapterItemActionHandler (item,
                                                   ep.getViewer (),
                                                   showAtItem);

    }

}
