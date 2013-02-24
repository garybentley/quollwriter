package com.quollwriter.ui.components;

import java.awt.Component;


public class FormItem
{

    public Object    label = null;
    public Component component = null;
    public String    formatSpec = null;

    public FormItem(Object    l,
                    Component c)
    {

        this.label = l;
        this.component = c;

    }

    public FormItem(Object    l,
                    Component c,
                    String    s)
    {

        this (l,
              c);

        this.formatSpec = s;

    }

}
