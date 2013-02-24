package com.quollwriter.ui.actionHandlers;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.components.*;


public class AddBookActionHandler extends ActionAdapter
{

    private ProjectViewer projectViewer = null;
    private Book          book = null;

    public AddBookActionHandler(Book          b,
                                ProjectViewer pv)
    {

        this.book = b;
        this.projectViewer = pv;

    }

    public void actionPerformed (ActionEvent ev)
    {

    }

}
