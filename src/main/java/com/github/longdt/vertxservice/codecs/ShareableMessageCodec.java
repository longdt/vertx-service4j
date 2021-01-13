package com.github.longdt.vertxservice.codecs;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.buffer.impl.BufferImpl;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.shareddata.Shareable;

public class ShareableMessageCodec implements MessageCodec<Shareable, Shareable> {
    public static final String CODEC_NAME = "shareable";

    @Override
    public void encodeToWire(Buffer buffer, Shareable shareable) {
        var buf = ((BufferImpl) buffer).byteBuf();
        var output = Kryos.getOutput();
        output.setOutputStream(new ByteBufOutputStream(buf));
        Kryos.getKryo().writeClassAndObject(output, shareable);
        output.close();
    }

    @Override
    public Shareable decodeFromWire(int pos, Buffer buffer) {
        var buf = ((BufferImpl) buffer).byteBuf().slice(pos, buffer.length() - pos);
        var input = Kryos.getInput();
        input.setInputStream(new ByteBufInputStream(buf));
        return (Shareable) Kryos.getKryo().readClassAndObject(input);
    }

    @Override
    public Shareable transform(Shareable shareable) {
        return shareable.copy();
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
