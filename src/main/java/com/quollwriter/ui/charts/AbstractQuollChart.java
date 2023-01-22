package com.quollwriter.ui.charts;

import javax.swing.*;

import org.jfree.chart.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;

public abstract class AbstractQuollChart<E extends AbstractViewer> implements QuollChart
{

    protected E viewer = null;
    protected StatisticsPanel parent = null;

    public AbstractQuollChart (E viewer)
    {

        this.viewer = viewer;

    }

    public void updateChart ()
    {

        JFreeChart ch = null;
        JComponent det = null;

        try
        {

            ch = this.getChart (true);

            det = this.getDetail (true);

        } catch (Exception e) {

            Environment.logError ("Unable to create chart",
                                  e);

            UIUtils.showErrorMessage (this.viewer,
                                      getUIString (charts,view,actionerror));
                                      //"Unable to show chart.");

            return;

        }

        this.parent.updateChart (ch,
                                 det);

    }

    public void init (StatisticsPanel wcp)
               throws GeneralException
    {

        this.parent = wcp;

    }

    public JLabel createDetailLabel (JLabel l)
    {

        l.setIcon (Environment.getIcon (Constants.BULLET_ICON_NAME,
                                        Constants.ICON_MENU));

        l.setVerticalAlignment (SwingConstants.TOP);
        l.setVerticalTextPosition (SwingConstants.TOP);

        return l;

    }

    public JLabel createWarningLabel (JLabel l)
    {

        l.setIcon (Environment.getIcon (Constants.ERROR_ICON_NAME,
                                        Constants.ICON_MENU));

        l.setVerticalAlignment (SwingConstants.TOP);
        l.setVerticalTextPosition (SwingConstants.TOP);

        return l;

    }

    public JLabel createDetailLabel (String text)
    {

        JLabel l = UIUtils.createLabel (text);

        return this.createDetailLabel (l);

    }

    public JLabel createWarningLabel (String text)
    {

        JLabel l = UIUtils.createLabel (text);

        return this.createWarningLabel (l);

    }

}
