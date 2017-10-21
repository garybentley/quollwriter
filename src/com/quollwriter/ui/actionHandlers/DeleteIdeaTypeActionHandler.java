package com.quollwriter.ui.actionHandlers;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.components.*;
import com.quollwriter.ui.panels.*;

public class DeleteIdeaTypeActionHandler extends YesDeleteConfirmTextInputActionHandler
{

    private IdeaType  ideaType = null;
    private IdeaBoard ideaBoard = null;

    public DeleteIdeaTypeActionHandler (IdeaType  it,
                                        IdeaBoard ib)
    {

        super (ib.getViewer (),
               it);

        this.ideaType = it;
        this.ideaBoard = ib;

    }

    public String getWarning ()
    {

        return Environment.getUIString (LanguageStrings.ideaboard,
                                        LanguageStrings.ideatypes,
                                        LanguageStrings.delete,
                                        LanguageStrings.warning);
        //"Warning!  All ideas associated with the type will be deleted.  Once deleted the type and ideas cannot be restored.";

    }

    public String getDeleteType ()
    {

        return Environment.getUIString (LanguageStrings.ideaboard,
                                        LanguageStrings.ideatypes,
                                        LanguageStrings.delete,
                                        LanguageStrings.deletetype);
        //"Idea Type";

    }

    public boolean onConfirm (String v)
                              throws Exception
    {

        this.ideaBoard.deleteIdeaType (this.ideaType);

        return true;

    }

}
