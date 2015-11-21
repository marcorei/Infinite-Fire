/* (c) 2015 Markus Riegel
 * license: MIT
 */
package com.marcorei.infinitefire;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Simplified DataSnapshot.
 * InfiniteFireSnapshot allows that an {@link InfiniteFireArray} can be replaced for testing.
 * You'll get the key and the value but without the overhead.
 */
public class InfiniteFireSnapshot<T> {
    private String key;
    private T value;

    public InfiniteFireSnapshot(@NonNull String key, T value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    @Nullable
    public T getValue() {
        return value;
    }
}
