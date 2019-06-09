package com.quollwriter.ui.fx;

import com.quollwriter.data.*;
import com.quollwriter.ui.fx.viewers.*;

public class ProjectViewerCommand<E extends DataObject> extends Command
{

    private Action<E> f = null;
    private ProjectViewer viewer = null;

    public ProjectViewerCommand (ProjectViewer viewer,
                                 Action<E>     f,
                                 String...     ids)
    {

        super (ids);

        this.viewer = viewer;
        this.f = f;

    }

    @Override
    public void run (Runnable doAfter)
    {

        this.run (doAfter,
                  (E) null);

    }

    @Override
    public void run ()
    {

        this.run ((Runnable) null);

    }

    public void run (E... objs)
    {

        this.run (null,
                  objs);

    }

    public void run (Runnable doAfter,
                     E...     objs)
    {

        UIUtils.runLater (() ->
        {

            this.f.apply (this.viewer,
                          objs);

            if (doAfter != null)
            {

                UIUtils.runLater (doAfter);

            }

        });


    }

    @FunctionalInterface
    public interface Action<E extends DataObject>
    {

        void apply (ProjectViewer viewer,
                    E...          objs);

    }

}
