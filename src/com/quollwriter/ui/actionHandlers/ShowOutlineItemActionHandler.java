package com.quollwriter.ui.actionHandlers;

import java.awt.event.ActionListener;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;

public class ShowOutlineItemActionHandler extends ShowChapterItemActionHandler
{

    public ShowOutlineItemActionHandler(ChapterItem      item,
                                        QuollEditorPanel qep)
    {

        super (item,
               qep);

    }

    public String getItemDescription (ChapterItem item)
    {

        OutlineItem it = (OutlineItem) item;
        
        String desc = item.getDescription ();
        
        if (it.getScene () != null)
        {
            
            desc = "Scene: " + it.getScene ().getName () + "<br />" + desc;
            
        }

        return desc;

    }

    public AbstractActionHandler getEditItemActionHandler (ChapterItem item)
    {

        return new ChapterItemActionHandler (item,
                                             this.quollEditorPanel,
                                             AbstractActionHandler.EDIT,
                                             -1);

    }

    public ActionListener getDeleteItemActionHandler (ChapterItem item)
    {

        return new DeleteChapterItemActionHandler (item,
                                                   this.quollEditorPanel);

    }

}
