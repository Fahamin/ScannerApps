package com.scan.imagetotext.Utils;

import java.util.ArrayList;
import java.util.List;

/* compiled from: FileDialog */
class ListenerList<L> {
    private List<L> listenerList = new ArrayList();

    /* compiled from: FileDialog */
    public interface FireHandler<L> {
        void fireEvent(L l);
    }

    ListenerList() {
    }

    public void add(L l) {
        this.listenerList.add(l);
    }

    public void fireEvent(FireHandler<L> fireHandler) {
        for (Object fireEvent : new ArrayList(this.listenerList)) {
            fireHandler.fireEvent((L) fireEvent);
        }
    }

    public void remove(L l) {
        this.listenerList.remove(l);
    }

    public List<L> getListenerList() {
        return this.listenerList;
    }

}
