package com.quollwriter.ui.fx;

import java.util.*;

import com.quollwriter.*;

public interface Stateful
{

    public void init (State state)
               throws GeneralException;

    public State getState ();

}
