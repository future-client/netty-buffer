/*
 * Copyright 2012 The Netty Project
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
package io.netty.util.internal;

public final class PlatformDependent {
    /**
     * Raises an exception bypassing compiler checks for checked exceptions.
     */
    public static void throwException(Throwable t) {
        PlatformDependent.<RuntimeException>throwException0(t);
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void throwException0(Throwable t) throws E {
        throw (E) t;
    }

    public static byte[] allocateUninitializedArray(int size) {
        return new byte[size];
    }

    private static final boolean UNALIGNED;
    static {
        final String arch = System.getProperty("os.arch", "");
        UNALIGNED = arch.matches("^(i[3-6]86|x86(_64)?|x64|amd64)$");
    }

    /**
     * {@code true} if and only if the platform supports unaligned access.
     *
     * @see <a href="https://en.wikipedia.org/wiki/Segmentation_fault#Bus_error">Wikipedia on segfault</a>
     */
    public static boolean isUnaligned() {
        return UNALIGNED;
    }

    private PlatformDependent() {
        // only static method supported
    }
}
