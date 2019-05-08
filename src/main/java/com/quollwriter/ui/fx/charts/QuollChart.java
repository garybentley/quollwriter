package com.quollwriter.ui.fx.charts;

import javafx.scene.layout.*;
import javafx.beans.property.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;

public interface QuollChart extends Stateful
{

    public StringProperty getTitle ();

    public String getType ();

    public Region getControls ()
                      throws GeneralException;

    public Region getChart ()
                   throws GeneralException;

    public Region getDetails ()
                    throws GeneralException;

}
