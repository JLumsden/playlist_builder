package com.example.playlist_builder.comparator;

import com.example.playlist_builder.data.Track;

import java.util.Comparator;

public class TrackPopularityComparator implements Comparator<Track> {

    @Override
    public int compare(Track t1, Track t2) {
        int pop1 = t1.getPopularity();
        int pop2 = t2.getPopularity();

        if (pop1 == pop2) {
            return 0;
        } else if (pop1 < pop2) {
            return 1;
        } else {
            return -1;
        }
    }
}
