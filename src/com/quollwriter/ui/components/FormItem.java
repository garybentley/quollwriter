package com.quollwriter.ui.components;

import javax.swing.*;


public class FormItem
{

    public Object    label = null;
    public JComponent component = null;
    public String    formatSpec = null;

    public FormItem(Object     l,
                    JComponent c)
    {

        this.label = l;
        this.component = c;

    }

    public FormItem(Object     l,
                    JComponent c,
                    String     s)
    {

        this (l,
              c);

        this.formatSpec = s;

    }

}
