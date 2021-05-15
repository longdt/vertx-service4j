package com.github.longdt.vertxservice.util;

import io.vertx.core.shareddata.Shareable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImmutableSet<E> implements Shareable {
    private final Set<E> data;

    private ImmutableSet(Set<E> data) {
        this.data = data;
    }

    public static <E> ImmutableSet<E> of(Set<E> data) {
        return new ImmutableSet<>(data);
    }

    public Set<E> unwrap() {
        return data;
    }

    @Override
    public ImmutableSet<E> copy() {
        return new ImmutableSet<>(new HashSet<>(data));
    }
}
