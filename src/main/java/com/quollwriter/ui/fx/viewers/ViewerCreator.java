package com.quollwriter.ui.fx.viewers;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;

/**
 * A base class for content that is suitable for display within a Viewer.
 */
public interface ViewerCreator
{

    Viewer createViewer ()
                  throws GeneralException;

}
