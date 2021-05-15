package com.github.longdt.vertxservice.processor;

import com.github.longdt.vertxservice.codecs.ShareableMessageCodec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.vertx.core.eventbus.DeliveryOptions;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Types;

public class SingleParamMethodProxyWriter extends MethodProxyWriter {
    protected SingleParamMethodProxyWriter(Types types) {
        super(types);
    }

    @Override
    public void writeMethod(TypeSpec.Builder builder, ExecutableElement element) {
        MethodSpec.Builder methodBuilder = MethodSpec.overriding(element);
        methodBuilder.addStatement("var $L = new $T()", Constant.OPTIONS_VARIABLE, ClassName.get(DeliveryOptions.class));
        methodBuilder.addStatement("$L.addHeader($S, $S)", Constant.OPTIONS_VARIABLE, Constant.ACTION, element.getSimpleName());
        var param = element.getParameters().get(0);
        var paramType = param.asType();
        boolean needShareableCodec = SupportedTypes.needShareableCodec(paramType);
        String messageVar = param.getSimpleName().toString();
        if (needShareableCodec) {
            methodBuilder.addStatement("$L.setCodecName($T.CODEC_NAME)", Constant.OPTIONS_VARIABLE, ClassName.get(ShareableMessageCodec.class));
            if (SupportedTypes.isCollectionType(types, paramType)) {
                methodBuilder.addStatement("var $L = $T.of($L)", Constant.ARG_VARIABLE, MethodProxyWriter.getRawParamType(types, paramType), messageVar);
                messageVar = Constant.ARG_VARIABLE;
            }
        }
        var resultType = ((DeclaredType) element.getReturnType()).getTypeArguments().get(0);
        TypeName requestParamType = MethodProxyWriter.getRequestParamType(types, resultType);
        methodBuilder.addCode("return $L.eventBus().<$T>request(address, $L, $L)\n",
                Constant.VERTX_VARIABLE,
                requestParamType,
                messageVar,
                Constant.OPTIONS_VARIABLE);
        addResultHandler(methodBuilder, resultType);
        builder.addMethod(methodBuilder.build());
    }
}
