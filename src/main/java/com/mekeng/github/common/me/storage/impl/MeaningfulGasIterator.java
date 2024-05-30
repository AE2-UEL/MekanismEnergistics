package com.mekeng.github.common.me.storage.impl;

import appeng.api.storage.data.IAEStack;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class MeaningfulGasIterator<T extends IAEStack<T>> implements Iterator<T> {
    private final Iterator<T> parent;
    private T next;

    public MeaningfulGasIterator(Iterator<T> iterator) {
        this.parent = iterator;
    }

    public boolean hasNext() {
        while(this.parent.hasNext()) {
            this.next = this.parent.next();
            if (this.next.isMeaningful()) {
                return true;
            }
            this.parent.remove();
        }
        this.next = null;
        return false;
    }

    public T next() {
        if (this.next == null) {
            throw new NoSuchElementException();
        } else {
            return this.next;
        }
    }

    public void remove() {
        this.parent.remove();
    }
}