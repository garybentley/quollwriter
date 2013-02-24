package com.quollwriter.ui.components;

import java.awt.event.ActionEvent;


public class FormEvent extends ActionEvent
{

    public static final int SAVE = 1;
    public static final int CANCEL = 2;

    public static final String SAVE_ACTION_NAME = "save";
    public static final String CANCEL_ACTION_NAME = "cancel";

    private Form form = null;

    public FormEvent(Form   f,
                     int    type,
                     String action)
    {

        super (f,
               type,
               action);

        this.form = f;

    }

    public Form getForm ()
    {

        return this.form;

    }

}
