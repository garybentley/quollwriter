package com.quollwriter.ui.actionHandlers;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;

public abstract class ProjectViewerActionHandler<E extends AbstractProjectViewer> extends AbstractActionHandler<E>
{

    protected AbstractEditorPanel editorPanel = null;

    public ProjectViewerActionHandler(NamedObject d,
                                      E           pv,
                                      int         mode)
    {

        this (d,
               pv,
               mode,
               true);

    }

    public ProjectViewerActionHandler(NamedObject         d,
                                      AbstractEditorPanel qep,
                                      int                 mode)
    {

        super (d,
               // This feels wrong to me, we shouldn't have to cast here...
               (E) qep.getViewer (),
               mode);

        this.editorPanel = qep;

    }

    public ProjectViewerActionHandler(NamedObject         d,
                                      AbstractEditorPanel qep,
                                      int                 mode,
                                      boolean             addHideControl)
    {

        super (d,
               (E) qep.getViewer (),
               mode,
               addHideControl);

        this.editorPanel = qep;

    }

    public ProjectViewerActionHandler(NamedObject d,
                                      E           pv,
                                      int         mode,
                                      boolean     addHideControl)
    {

        super (d,
               pv,
               mode,
               addHideControl);

    }

    @Override
    public E getViewer ()
    {
        
        return super.getViewer ();
        
    }
    
    public void handleCancel (int mode)
    {

        if ((this.mode == AbstractActionHandler.ADD) &&
            (this.dataObject instanceof ChapterItem) &&
            (this.editorPanel instanceof ChapterItemViewer))
        {

            ChapterItem c = (ChapterItem) this.dataObject;

            ((ChapterItemViewer) this.editorPanel).removeItem (c);

            this.editorPanel.repaint ();

        }

    }

}
