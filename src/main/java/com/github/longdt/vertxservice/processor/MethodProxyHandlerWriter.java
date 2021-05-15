package com.github.longdt.vertxservice.processor;

import com.github.longdt.vertxservice.codecs.ShareableMessageCodec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import io.vertx.core.eventbus.DeliveryOptions;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

public abstract class MethodProxyHandlerWriter {
    protected final Types types;

    protected MethodProxyHandlerWriter(Types types) {
        this.types = types;
    }

    public abstract void writeInvocation(CodeBlock.Builder codeBlockBuilder, ExecutableElement element);

    public void addReplyHandler(CodeBlock.Builder codeBlockBuilder, TypeMirror resultType) {
        boolean needShareableCodec = SupportedTypes.needShareableCodec(resultType);
        if (needShareableCodec) {
            if (SupportedTypes.isCollectionType(types, resultType)) {
                codeBlockBuilder.addStatement("$L.reply($T.of(res.result()), new $T().setCodecName($T.CODEC_NAME))",
                        Constant.MESSAGE_VARIABLE,
                        MethodProxyWriter.getRawParamType(types, resultType),
                        ClassName.get(DeliveryOptions.class),
                        ClassName.get(ShareableMessageCodec.class));
            } else {
                codeBlockBuilder.addStatement("$L.reply(res.result(), new $T().setCodecName($T.CODEC_NAME))",
                        Constant.MESSAGE_VARIABLE,
                        ClassName.get(DeliveryOptions.class),
                        ClassName.get(ShareableMessageCodec.class));
            }
        } else {
            codeBlockBuilder.addStatement("$L.reply(res.result(), new $T())", Constant.MESSAGE_VARIABLE, ClassName.get(DeliveryOptions.class));
        }
    }
}
