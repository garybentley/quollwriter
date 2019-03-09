package com.quollwriter.ui.fx;

public /*abstract class*/ interface IBuilder<T extends IBuilder<T, E>, E>
{

    public T _this ();

    public E build ()
             throws Exception;
/*
    protected abstract T _this ();

    public abstract E build ()
                      throws Exception;
*/
}
