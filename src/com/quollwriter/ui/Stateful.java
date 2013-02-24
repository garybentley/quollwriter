package com.quollwriter.ui;

import java.util.*;


public interface Stateful
{

    public void setState (Map<String, String> s,
                          boolean             hasFocus);

    public void getState (Map<String, Object> s);

}
