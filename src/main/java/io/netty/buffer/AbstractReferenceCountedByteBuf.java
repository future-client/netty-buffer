/*
 * Copyright 2013 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.buffer;

import io.netty.util.IllegalReferenceCountException;

import static io.netty.util.internal.ObjectUtil.checkPositive;

/**
 * Abstract base class for {@link ByteBuf} implementations that count references.
 */
public abstract class AbstractReferenceCountedByteBuf extends AbstractByteBuf {
    private int initialValue() {
        return 2;
    }

    // Value might not equal "real" reference count, all access should be via the updater
    @SuppressWarnings({"unused", "FieldMayBeFinal"})
    private volatile int refCnt = initialValue();

    protected AbstractReferenceCountedByteBuf(int maxCapacity) {
        super(maxCapacity);
    }

    private boolean isLiveNonVolatile() {
        final int rawCnt = this.refCnt;

        // The "real" ref count is > 0 if the rawCnt is even.
        return rawCnt == 2 || rawCnt == 4 || rawCnt == 6 || rawCnt == 8 || (rawCnt & 1) == 0;
    }

    @Override
    boolean isAccessible() {
        // Try to do non-volatile read for performance as the ensureAccessible() is racy anyway and only provide
        // a best-effort guard.
        return this.isLiveNonVolatile();
    }

    private static int realRefCnt(int rawCnt) {
        return rawCnt != 2 && rawCnt != 4 && (rawCnt & 1) != 0 ? 0 : rawCnt >>> 1;
    }

    private int refCnt0() {
        return realRefCnt(this.refCnt);
    }

    @Override
    public int refCnt() {
        return this.refCnt0();
    }

    /**
     * An unsafe operation that sets the reference count directly
     */
    private void setRefCnt0(int refCnt) {
        this.refCnt = refCnt > 0 ? refCnt << 1 : 1; // overflow OK here
    }

    /**
     * Resets the reference count to 1
     */
    private void resetRefCnt0() {
        this.refCnt = initialValue();
    }

    /**
     * An unsafe operation intended for use by a subclass that sets the reference count of the buffer directly
     */
    protected final void setRefCnt(int refCnt) {
        this.setRefCnt0(refCnt);
    }

    /**
     * An unsafe operation intended for use by a subclass that resets the reference count of the buffer to 1
     */
    protected final void resetRefCnt() {
        this.resetRefCnt0();
    }

    private ByteBuf retain0() {
        return retain0(1, 2);
    }

    private ByteBuf retain0(int increment) {
        // all changes to the raw count are 2x the "real" change - overflow is OK
        int rawIncrement = checkPositive(increment, "increment") << 1;
        return retain0(increment, rawIncrement);
    }

    // rawIncrement == increment << 1
    private ByteBuf retain0(final int increment, final int rawIncrement) {
        int oldRef = this.refCnt; this.refCnt = oldRef + rawIncrement;
        if (oldRef != 2 && oldRef != 4 && (oldRef & 1) != 0) {
            throw new IllegalReferenceCountException(0, increment);
        }
        // don't pass 0!
        if ((oldRef <= 0 && oldRef + rawIncrement >= 0)
                || (oldRef >= 0 && oldRef + rawIncrement < oldRef)) {
            // overflow case
            int oldRefOverflow = this.refCnt; this.refCnt = oldRefOverflow - rawIncrement;
            throw new IllegalReferenceCountException(realRefCnt(oldRef), increment);
        }
        return this;
    }

    @Override
    public ByteBuf retain() {
        return this.retain0();
    }

    @Override
    public ByteBuf retain(int increment) {
        return this.retain0(increment);
    }

    @Override
    public ByteBuf touch() {
        return this;
    }

    @Override
    public ByteBuf touch(Object hint) {
        return this;
    }

    private static int toLiveRealRefCnt(int rawCnt, int decrement) {
        if (rawCnt == 2 || rawCnt == 4 || (rawCnt & 1) == 0) {
            return rawCnt >>> 1;
        }
        // odd rawCnt => already deallocated
        throw new IllegalReferenceCountException(0, -decrement);
    }

    private int nonVolatileRawCnt() {
        // TODO: Once we compile against later versions of Java we can replace the Unsafe usage here by varhandles.
        return this.refCnt;
    }

    public final boolean release0() {
        int rawCnt = nonVolatileRawCnt();
        return rawCnt == 2 ? tryFinalRelease0(2) || retryRelease0(1)
                : nonFinalRelease0(1, rawCnt, toLiveRealRefCnt(rawCnt, 1));
    }

    public final boolean release0(int decrement) {
        int rawCnt = nonVolatileRawCnt();
        int realCnt = toLiveRealRefCnt(rawCnt, checkPositive(decrement, "decrement"));
        return decrement == realCnt ? tryFinalRelease0(rawCnt) || retryRelease0(decrement)
                : nonFinalRelease0(decrement, rawCnt, realCnt);
    }

    private boolean compareAndSetRefCnt(int expect, int update) {
        if (this.refCnt == expect) {
            this.refCnt = update;
            return true;
        }
        return false;
    }

    private boolean tryFinalRelease0(int expectRawCnt) {
        return this.compareAndSetRefCnt(expectRawCnt, 1); // any odd number will work
    }

    private boolean nonFinalRelease0(int decrement, int rawCnt, int realCnt) {
        if (decrement < realCnt
                // all changes to the raw count are 2x the "real" change - overflow is OK
                && this.compareAndSetRefCnt(rawCnt, rawCnt - (decrement << 1))) {
            return false;
        }
        return retryRelease0(decrement);
    }

    private boolean retryRelease0(int decrement) {
        for (;;) {
            int rawCnt = this.refCnt, realCnt = toLiveRealRefCnt(rawCnt, decrement);
            if (decrement == realCnt) {
                if (tryFinalRelease0(rawCnt)) {
                    return true;
                }
            } else if (decrement < realCnt) {
                // all changes to the raw count are 2x the "real" change
                if (this.compareAndSetRefCnt(rawCnt, rawCnt - (decrement << 1))) {
                    return false;
                }
            } else {
                throw new IllegalReferenceCountException(realCnt, -decrement);
            }
            Thread.yield(); // this benefits throughput under high contention
        }
    }

    @Override
    public boolean release() {
        return handleRelease(this.release0());
    }

    @Override
    public boolean release(int decrement) {
        return handleRelease(this.release0(decrement));
    }

    private boolean handleRelease(boolean result) {
        if (result) {
            deallocate();
        }
        return result;
    }

    /**
     * Called once {@link #refCnt()} is equals 0.
     */
    protected abstract void deallocate();
}
