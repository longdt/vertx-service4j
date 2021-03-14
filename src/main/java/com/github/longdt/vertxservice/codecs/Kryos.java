package com.github.longdt.vertxservice.codecs;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.longdt.vertxservice.util.Arguments;
import com.github.longdt.vertxservice.util.ShareableList;
import com.github.longdt.vertxservice.util.ShareableMap;
import com.github.longdt.vertxservice.util.ShareableSet;
import io.netty.util.concurrent.FastThreadLocal;

import java.util.function.Consumer;

public class Kryos {
    private static FastThreadLocal<Kryo> localKryo;
    private static final FastThreadLocal<Input> localInput = new FastThreadLocal<>() {
        @Override
        protected Input initialValue() {
            return new Input(4096);
        }
    };
    private static final FastThreadLocal<Output> localOutput = new FastThreadLocal<>() {
        @Override
        protected Output initialValue() {
            return new Output(4096);
        }
    };

    public static void initialize(Consumer<Kryo> kryoConfigurer) {
        localKryo = new FastThreadLocal<>() {
            @Override
            protected Kryo initialValue() throws Exception {
                var kryo = new Kryo();
                kryo.register(Arguments.class);
                kryo.register(Object[].class);
                kryo.register(ShareableList.class);
                kryo.register(ShareableSet.class);
                kryo.register(ShareableMap.class);
                kryoConfigurer.accept(kryo);
                return kryo;
            }
        };
    }

    public static Kryo getKryo() {
        return localKryo.get();
    }

    public static Input getInput() {
        return localInput.get();
    }

    public static Output getOutput() {
        return localOutput.get();
    }
}
