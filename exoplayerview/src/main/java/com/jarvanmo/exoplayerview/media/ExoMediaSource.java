package com.jarvanmo.exoplayerview.media;

import java.util.List;

/**
 * Created by mo on 18-1-11.
 * 剑气纵横三万里 一剑光寒十九洲
 */

public interface ExoMediaSource {

    interface Quality{
        CharSequence name();
        String url();
    }

    String url();
    String name();
    List<Quality> qualities();
}
