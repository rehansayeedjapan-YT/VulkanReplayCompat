/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.Validate
 */
package net.vulkanmod.render.chunk.util;

import java.util.Iterator;
import org.apache.commons.lang3.Validate;

public class CircularIntList {
    private final int size;
    private final int[] list;
    private int startIndex;
    private final OwnIterator iterator;
    private final RangeIterator rangeIterator;

    public CircularIntList(int size) {
        this.size = size;
        this.list = new int[size + 2];
        this.iterator = new OwnIterator();
        this.rangeIterator = new RangeIterator();
    }

    public void updateStartIdx(int startIndex) {
        int[] list = this.list;
        this.startIndex = startIndex;
        list[0] = -1;
        list[this.size + 1] = -1;
        int k = 1;
        int i = startIndex;
        while (i < this.size) {
            list[k] = i++;
            ++k;
        }
        i = 0;
        while (i < startIndex) {
            list[k] = i++;
            ++k;
        }
    }

    public int getNext(int i) {
        return this.list[i + 1];
    }

    public int getPrevious(int i) {
        return this.list[i - 1];
    }

    public OwnIterator iterator() {
        return this.iterator;
    }

    public RangeIterator getRangeIterator(int startIndex, int endIndex) {
        this.rangeIterator.update(startIndex, endIndex);
        return this.rangeIterator;
    }

    public RangeIterator createRangeIterator() {
        return new RangeIterator();
    }

    public class OwnIterator
    implements Iterator<Integer> {
        private int currentIndex = 0;
        private final int maxIndex;

        public OwnIterator() {
            this.maxIndex = CircularIntList.this.size;
        }

        @Override
        public boolean hasNext() {
            return this.currentIndex < this.maxIndex;
        }

        @Override
        public Integer next() {
            ++this.currentIndex;
            return CircularIntList.this.list[this.currentIndex];
        }

        public int getCurrentIndex() {
            return this.currentIndex;
        }

        public void restart() {
            this.currentIndex = 0;
        }
    }

    public class RangeIterator
    implements Iterator<Integer> {
        private int currentIndex;
        private int startIndex;
        private int endIndex;

        public void update(int startIndex, int endIndex) {
            Validate.isTrue((endIndex < CircularIntList.this.list.length ? 1 : 0) != 0, (String)"Beyond max size", (Object[])new Object[0]);
            this.startIndex = startIndex + 1;
            this.endIndex = endIndex + 1;
            this.restart();
        }

        @Override
        public boolean hasNext() {
            return this.currentIndex < this.endIndex;
        }

        @Override
        public Integer next() {
            ++this.currentIndex;
            return CircularIntList.this.list[this.currentIndex];
        }

        public int getCurrentIndex() {
            return this.currentIndex;
        }

        public void restart() {
            this.currentIndex = this.startIndex - 1;
        }
    }
}

