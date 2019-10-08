package com.quollwriter.data;

import java.util.*;

import javafx.scene.*;
import javafx.beans.value.*;
import javafx.beans.property.*;
import javafx.collections.*;

public interface IPropertyBinder
{

    IPropertyBinder getBinder ();

    default <T> ListenerHandle addChangeListener (ObservableValue<T> value,
                                                  ChangeListener<T> listener)
    {

        return this.getBinder ().addChangeListener (value,
                                                    listener);

    }

    default <T> ListenerHandle addSetChangeListener (ObservableSet<T>     set,
                                                     SetChangeListener<T> listener)
    {

        return this.getBinder ().addSetChangeListener (set,
                                                       listener);

    }

    default <T> ListenerHandle addListChangeListener (ObservableList<T>     list,
                                                      ListChangeListener<T> listener)
    {

        return this.getBinder ().addListChangeListener (list,
                                                        listener);

    }

    default void dispose ()
    {

        this.getBinder ().dispose ();

    }

    public interface ListenerHandle
    {

        void dispose ();

    }

}
