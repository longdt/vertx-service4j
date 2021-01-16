package com.github.longdt.vertxservice.codecs;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageCodec;

public class MessageCodecs {
    public static <T> boolean registerDefaultCodec(Vertx vertx, Class<T> clazz, MessageCodec<T, ?> codec) {
        try {
            vertx.eventBus().registerDefaultCodec(clazz, codec);
            return true;
        } catch (IllegalStateException ex) {
            return false;
        }
    }

    public static boolean registerCodec(Vertx vertx, MessageCodec<?, ?> codec) {
        try {
            vertx.eventBus().registerCodec(codec);
            return true;
        } catch (IllegalStateException ex) {
            return false;
        }
    }
}
