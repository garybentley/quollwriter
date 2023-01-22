package com.quollwriter.ui.fx.swing;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Image;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.text.*;

import com.quollwriter.*;
import com.quollwriter.data.*;

public class ChapterItemMoveMouseHandler extends ChapterItemMoveHandler
{

    public ChapterItemMoveMouseHandler (final ChapterItem item,
                                        final IconColumn  ic)
    {

        super (ic);

        final ChapterItemMoveMouseHandler _this = this;

        MouseAdapter m = new MouseAdapter ()
        {

            private boolean dragInProgress = false;

            public void mousePressed (MouseEvent ev)
            {

                _this.setItem (item);

            }

            public void mouseDragged (MouseEvent ev)
            {

                if (!this.dragInProgress)
                {

                    _this.startDrag ();

                    this.dragInProgress = true;

                }

                _this.doDrag ();

            }

            public void mouseReleased (MouseEvent ev)
            {

                if (!this.dragInProgress)
                {

                    return;

                }

                this.dragInProgress = false;

                _this.dragFinished ();

            }

        };

        ImagePanel im = ic.getImagePanel (item);

        im.addMouseListener (m);
        im.addMouseMotionListener (m);

    }

}
