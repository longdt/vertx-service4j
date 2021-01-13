package com.github.longdt.vertxservice.codecs;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.longdt.vertxservice.util.Arguments;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.util.concurrent.FastThreadLocal;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.buffer.impl.BufferImpl;
import io.vertx.core.eventbus.MessageCodec;

import java.util.function.Consumer;

public class ArgumentsMessageCodec implements MessageCodec<Arguments, Arguments> {
    public static final String CODEC_NAME = "arguments";
    private final FastThreadLocal<Kryo> localKryo;
    private final FastThreadLocal<Input> localInput = new FastThreadLocal<>() {
        @Override
        protected Input initialValue() {
            return new Input(4096);
        }
    };
    private final FastThreadLocal<Output> localOutput = new FastThreadLocal<>() {
        @Override
        protected Output initialValue() {
            return new Output(4096);
        }
    };

    public ArgumentsMessageCodec() {
        this(kryo -> kryo.setRegistrationRequired(false));
    }

    public ArgumentsMessageCodec(Consumer<Kryo> kryoConfigurer) {
        localKryo = new FastThreadLocal<>() {
            @Override
            protected Kryo initialValue() throws Exception {
                var kryo = new Kryo();
                kryoConfigurer.accept(kryo);
                return kryo;
            }
        };
    }

    @Override
    public void encodeToWire(Buffer buffer, Arguments arguments) {
        var buf = ((BufferImpl) buffer).byteBuf();
        var output = localOutput.get();
        output.setOutputStream(new ByteBufOutputStream(buf));
        localKryo.get().writeObject(output, arguments);
        output.close();
    }

    @Override
    public Arguments decodeFromWire(int pos, Buffer buffer) {
        var buf = ((BufferImpl) buffer).byteBuf().slice(pos, buffer.length() - pos);
        var input = localInput.get();
        input.setInputStream(new ByteBufInputStream(buf));
        return localKryo.get().readObject(input, Arguments.class);
    }

    @Override
    public Arguments transform(Arguments arguments) {
        return arguments.copy();
    }

    @Override
    public String name() {
        return CODEC_NAME;
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}
