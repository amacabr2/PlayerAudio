package com.iut_bm_info.amacabr2.playeraudio.utils;

import static java.lang.String.*;

/**
 * Created by amacabr2 on 23/11/17.
 */

public class ConvertDuration {

    public static String milliToMinuteAnsSeconde(long duration) {
        long minutes = (duration / 1000) / 60;
        long seconds = (duration / 1000) % 60;
        return format("%d:%02d", minutes, seconds);
    }
}
