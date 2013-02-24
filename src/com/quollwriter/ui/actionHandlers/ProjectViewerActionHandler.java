package com.quollwriter.ui.actionHandlers;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;

public abstract class ProjectViewerActionHandler extends AbstractActionHandler
{

    protected ProjectViewer       projectViewer = null;
    protected AbstractEditorPanel editorPanel = null;

    public ProjectViewerActionHandler(NamedObject   d,
                                      ProjectViewer pv,
                                      int           mode)
    {

        super (d,
               pv,
               mode);

        this.projectViewer = pv;

    }

    public ProjectViewerActionHandler(NamedObject         d,
                                      AbstractEditorPanel qep,
                                      int                 mode)
    {

        super (d,
               qep.getProjectViewer (),
               mode);

        this.editorPanel = qep;
        this.projectViewer = (ProjectViewer) qep.getProjectViewer ();

    }

    public ProjectViewerActionHandler(NamedObject         d,
                                      AbstractEditorPanel qep,
                                      int                 mode,
                                      boolean             addHideControl)
    {

        super (d,
               qep.getProjectViewer (),
               mode,
               addHideControl);

        this.editorPanel = qep;
        this.projectViewer = (ProjectViewer) qep.getProjectViewer ();

    }

    public ProjectViewerActionHandler(NamedObject   d,
                                      ProjectViewer pv,
                                      int           mode,
                                      boolean       addHideControl)
    {

        super (d,
               pv,
               mode,
               addHideControl);

        this.projectViewer = pv;

    }

    public void handleCancel (int mode)
    {

        if ((this.mode == AbstractActionHandler.ADD) &&
            (this.dataObject instanceof ChapterItem) &&
            (this.editorPanel instanceof QuollEditorPanel))
        {

            ChapterItem c = (ChapterItem) this.dataObject;

            // QuollEditorPanel qep = (QuollEditorPanel) this.projectViewer.getEditorForChapter (c.getChapter ());

            ((QuollEditorPanel) this.editorPanel).removeItem (c);

            this.editorPanel.repaint ();

        }

    }
/*
    public void actionPerformed (FormEvent ev)
    {

        if (ev.getID () == FormEvent.CANCEL)
        {

            if ((this.mode == AbstractActionHandler.ADD)
                &&
                (this.dataObject instanceof ChapterItem)
                &&
                (this.editorPanel instanceof QuollEditorPanel)
               )
            {

                ChapterItem c = (ChapterItem) this.dataObject;

                //QuollEditorPanel qep = (QuollEditorPanel) this.projectViewer.getEditorForChapter (c.getChapter ());

                ((QuollEditorPanel) this.editorPanel).removeItem (c);

                this.editorPanel.repaint ();

            }

            this.f.hideForm ();

            this.f = null;

            return;

        }

        super.actionPerformed (ev);

    }
*/
}
