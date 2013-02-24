package com.quollwriter.ui.actionHandlers;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;

public class ShowNoteActionHandler extends ShowChapterItemActionHandler
{

    public ShowNoteActionHandler(Note             n,
                                 QuollEditorPanel qep)
    {

        super (n,
               qep);

    }

    public String getItemDescription (ChapterItem item)
    {

        Note n = (Note) item;

        String summ = n.getSummary ();

        if (n.isEditNeeded ())
        {
            
            return summ;
            
        }

        if (!n.getType ().equals (""))
        {

            summ = n.getType () + ": " + summ;

        }
        
        if (n.getDescription () != null)
        {

            summ += "<br />" + n.getDescription ();

        }

        return summ;

    }

    public AbstractActionHandler getEditItemActionHandler (ChapterItem item)
    {

        return new NoteActionHandler ((Note) item,
                                      this.quollEditorPanel);

    }

    public ActionListener getDeleteItemActionHandler (ChapterItem item)
    {

        return new DeleteNoteActionHandler ((Note) item,
                                            this.quollEditorPanel);

    }

}
