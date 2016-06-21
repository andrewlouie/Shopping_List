package com.andrewaarondev.shoppinglist;

/**
 * Created by Andrew on 5/27/2016.
 */
public class TitlesLoadedEvent {
    String[] prose;
    Integer[] ids;
    int focusPosition;

    TitlesLoadedEvent(String[] prose, Integer[] ids, int focusPosition) {
        this.prose = prose;
        this.focusPosition = focusPosition;
        this.ids = ids;
    }

    String[] getProse() {
        return (prose);
    }

    int getFocusPosition() {
        return (focusPosition);
    }

    Integer[] getIds() {
        return (ids);
    }
}
