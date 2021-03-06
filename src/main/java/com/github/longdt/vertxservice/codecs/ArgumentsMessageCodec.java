package com.github.longdt.vertxservice.codecs;

import com.github.longdt.vertxservice.util.Arguments;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.buffer.impl.BufferImpl;
import io.vertx.core.eventbus.MessageCodec;

public class ArgumentsMessageCodec implements MessageCodec<Arguments, Arguments> {
    public static final String CODEC_NAME = "arguments";

    @Override
    public void encodeToWire(Buffer buffer, Arguments arguments) {
        var buf = ((BufferImpl) buffer).byteBuf();
        var output = Kryos.getOutput();
        output.setOutputStream(new ByteBufOutputStream(buf));
        Kryos.getKryo().writeObject(output, arguments);
        output.close();
    }

    @Override
    public Arguments decodeFromWire(int pos, Buffer buffer) {
        var buf = ((BufferImpl) buffer).byteBuf().slice(pos, buffer.length() - pos);
        var input = Kryos.getInput();
        input.setInputStream(new ByteBufInputStream(buf));
        return Kryos.getKryo().readObject(input, Arguments.class);
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
