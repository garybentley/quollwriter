package com.quollwriter.ui.fx;

public interface Buildable<T extends IBuilder<T, E>, E>
{

    static <T extends IBuilder<T, E>, E> T builder ()
    {

        throw new IllegalStateException ();

    }

}
