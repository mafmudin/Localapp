package com.localapp.utils;

import java.util.ArrayList;

/**
 * Created by 4 way on 17-07-2017.
 */

public class CustomStringList extends ArrayList<String> {

    @Override
    public boolean contains(Object o) {
        String paramStr = (String)o;
        for (String s : this) {
            if (paramStr.equalsIgnoreCase(s)) return true;
        }
        return false;
    }

}
