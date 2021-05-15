package com.github.longdt.vertxservice.processor;

import com.github.longdt.vertxservice.util.*;
import com.squareup.javapoet.*;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

public class NoParamMethodProxyWriter extends MethodProxyWriter {
    protected NoParamMethodProxyWriter(Types types) {
        super(types);
    }

    @Override
    public void writeMethod(TypeSpec.Builder builder, ExecutableElement element) {
        MethodSpec.Builder methodBuilder = MethodSpec.overriding(element);
        methodBuilder.addStatement("var $L = new $T()", Constant.OPTIONS_VARIABLE, ClassName.get(DeliveryOptions.class));
        methodBuilder.addStatement("$L.addHeader($S, $S)", Constant.OPTIONS_VARIABLE, Constant.ACTION, element.getSimpleName());
        var resultType = ((DeclaredType) element.getReturnType()).getTypeArguments().get(0);
        TypeName requestParamType = getRequestParamType(resultType);
        methodBuilder.addCode("return $L.eventBus().<$T>request(address, null, $L)\n",
                Constant.VERTX_VARIABLE,
                requestParamType,
                Constant.OPTIONS_VARIABLE);
        addResultHandler(methodBuilder, resultType);
        builder.addMethod(methodBuilder.build());
    }

}
