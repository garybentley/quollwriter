package com.quollwriter.ui.fx.components;

import javafx.scene.layout.*;

import com.quollwriter.ui.fx.*;
import com.quollwriter.data.IPropertyBinder;
import com.quollwriter.data.PropertyBinder;

public abstract class BaseVBox extends VBox implements IPropertyBinder
{

    private PropertyBinder binder = new PropertyBinder ();

    @Override
    public IPropertyBinder getBinder ()
    {

        return this.binder;

    }

    public void dispose ()
    {

        this.binder.dispose ();

    }

}
