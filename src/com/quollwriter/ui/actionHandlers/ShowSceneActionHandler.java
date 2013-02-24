package com.quollwriter.ui.actionHandlers;

import java.awt.event.ActionListener;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;

public class ShowSceneActionHandler extends ShowChapterItemActionHandler
{

    public ShowSceneActionHandler(ChapterItem      item,
                                  QuollEditorPanel qep)
    {

        super (item,
               qep);

    }

    public String getItemDescription (ChapterItem item)
    {

        return item.getDescription ();

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
