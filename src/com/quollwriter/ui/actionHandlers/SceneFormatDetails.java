package com.quollwriter.ui.actionHandlers;

import java.awt.event.ActionListener;

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

        return item.getDescription ();

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
                                                   ep.getProjectViewer (),
                                                   showAtItem);

    }

}
