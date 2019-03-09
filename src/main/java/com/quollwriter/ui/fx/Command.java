package com.quollwriter.ui.fx;

import java.util.*;

public class Command
{

    private Runnable action = null;
    private List<String> ids = null;

    public Command (Runnable  action,
                    String... commandIds)
    {

        if (action == null)
        {

            throw new IllegalArgumentException ("Action must be provided.");

        }

        if ((commandIds == null)
            ||
            (commandIds.length == 0)
           )
        {

            throw new IllegalArgumentException ("At least 1 command id must be provided.");

        }

        this.action = action;

        this.ids = Arrays.asList (commandIds);

    }

    public void run ()
    {

        UIUtils.runLater (this.action);

    }

    public void run (Runnable doAfter)
    {

        UIUtils.runLater (() ->
        {

            this.action.run ();

            if (doAfter != null)
            {

                UIUtils.runLater (doAfter);

            }

        });

    }

    public List<String> commandIds ()
    {

        return this.ids;

    }

    public static Command create (Runnable  action,
                                  String... commandIds)
    {

        return new Command (action,
                            commandIds);

    }

}
