package com.quollwriter.ui.fx;

import java.util.*;

public class CommandWithArgs<E> extends Command
{

    private Action<E> f = null;

    public CommandWithArgs (Action<E>     f,
                            String...     ids)
    {

        super (ids);
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

            this.f.apply (objs);

            if (doAfter != null)
            {

                UIUtils.runLater (doAfter);

            }

        });


    }

    @FunctionalInterface
    public interface Action<E>
    {

        void apply (E... objs);

    }

}
