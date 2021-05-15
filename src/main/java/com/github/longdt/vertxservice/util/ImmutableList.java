package com.github.longdt.vertxservice.util;

import io.vertx.core.shareddata.Shareable;

import java.util.ArrayList;
import java.util.List;

public class ImmutableList<E> implements Shareable {
    private final List<E> data;

    private ImmutableList(List<E> data) {
        this.data = data;
    }


    public static <E> ImmutableList<E> of(List<E> data) {
        return new ImmutableList<>(data);
    }

    public List<E> unwrap() {
        return data;
    }

    @Override
    public ImmutableList<E> copy() {
        return new ImmutableList<>(new ArrayList<>(data));
    }
}
