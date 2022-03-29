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
package io.netty.util;

import io.netty.util.internal.ObjectUtil;

/**
 * Collection of method to handle objects that may implement {@link ReferenceCounted}.
 */
public final class ReferenceCountUtil {
    /**
     * Try to call {@link ReferenceCounted#retain()} if the specified message implements {@link ReferenceCounted}.
     * If the specified message doesn't implement {@link ReferenceCounted}, this method does nothing.
     */
    @SuppressWarnings("unchecked")
    public static <T> T retain(T msg) {
        if (msg instanceof ReferenceCounted) {
            return (T) ((ReferenceCounted) msg).retain();
        }
        return msg;
    }

    /**
     * Try to call {@link ReferenceCounted#retain(int)} if the specified message implements {@link ReferenceCounted}.
     * If the specified message doesn't implement {@link ReferenceCounted}, this method does nothing.
     */
    @SuppressWarnings("unchecked")
    public static <T> T retain(T msg, int increment) {
        ObjectUtil.checkPositive(increment, "increment");
        if (msg instanceof ReferenceCounted) {
            return (T) ((ReferenceCounted) msg).retain(increment);
        }
        return msg;
    }

    /**
     * Tries to call {@link ReferenceCounted#touch()} if the specified message implements {@link ReferenceCounted}.
     * If the specified message doesn't implement {@link ReferenceCounted}, this method does nothing.
     */
    @SuppressWarnings("unchecked")
    public static <T> T touch(T msg) {
        if (msg instanceof ReferenceCounted) {
            return (T) ((ReferenceCounted) msg).touch();
        }
        return msg;
    }

    /**
     * Tries to call {@link ReferenceCounted#touch(Object)} if the specified message implements
     * {@link ReferenceCounted}.  If the specified message doesn't implement {@link ReferenceCounted},
     * this method does nothing.
     */
    @SuppressWarnings("unchecked")
    public static <T> T touch(T msg, Object hint) {
        if (msg instanceof ReferenceCounted) {
            return (T) ((ReferenceCounted) msg).touch(hint);
        }
        return msg;
    }

    /**
     * Try to call {@link ReferenceCounted#release()} if the specified message implements {@link ReferenceCounted}.
     * If the specified message doesn't implement {@link ReferenceCounted}, this method does nothing.
     */
    public static boolean release(Object msg) {
        if (msg instanceof ReferenceCounted) {
            return ((ReferenceCounted) msg).release();
        }
        return false;
    }

    /**
     * Try to call {@link ReferenceCounted#release(int)} if the specified message implements {@link ReferenceCounted}.
     * If the specified message doesn't implement {@link ReferenceCounted}, this method does nothing.
     */
    public static boolean release(Object msg, int decrement) {
        ObjectUtil.checkPositive(decrement, "decrement");
        if (msg instanceof ReferenceCounted) {
            return ((ReferenceCounted) msg).release(decrement);
        }
        return false;
    }

    /**
     * Try to call {@link ReferenceCounted#release()} if the specified message implements {@link ReferenceCounted}.
     * If the specified message doesn't implement {@link ReferenceCounted}, this method does nothing.
     * Unlike {@link #release(Object)} this method catches an exception raised by {@link ReferenceCounted#release()}
     * and logs it, rather than rethrowing it to the caller.  It is usually recommended to use {@link #release(Object)}
     * instead, unless you absolutely need to swallow an exception.
     */
    public static void safeRelease(Object msg) {
        try {
            release(msg);
        } catch (Throwable t) {
            // Ignore.
        }
    }

    /**
     * Try to call {@link ReferenceCounted#release(int)} if the specified message implements {@link ReferenceCounted}.
     * If the specified message doesn't implement {@link ReferenceCounted}, this method does nothing.
     * Unlike {@link #release(Object)} this method catches an exception raised by {@link ReferenceCounted#release(int)}
     * and logs it, rather than rethrowing it to the caller.  It is usually recommended to use
     * {@link #release(Object, int)} instead, unless you absolutely need to swallow an exception.
     */
    public static void safeRelease(Object msg, int decrement) {
        try {
            ObjectUtil.checkPositive(decrement, "decrement");
            release(msg, decrement);
        } catch (Throwable t) {
            // Ignore.
        }
    }

    /**
     * Returns reference count of a {@link ReferenceCounted} object. If object is not type of
     * {@link ReferenceCounted}, {@code -1} is returned.
     */
    public static int refCnt(Object msg) {
        return msg instanceof ReferenceCounted ? ((ReferenceCounted) msg).refCnt() : -1;
    }

    private ReferenceCountUtil() { }
}
