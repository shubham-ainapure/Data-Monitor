package com.example.datamonitor;

import java.util.Comparator;

public class DataUsageComparator implements Comparator<AppModel> {
    @Override
    public int compare(AppModel o1, AppModel o2) {
        return Long.compare(o2.getDataUsage(),o1.getDataUsage());
    }
}
