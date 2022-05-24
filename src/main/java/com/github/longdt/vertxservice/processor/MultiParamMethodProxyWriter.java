package com.github.longdt.vertxservice.processor;

import com.github.longdt.vertxservice.codecs.ArgumentsMessageCodec;
import com.github.longdt.vertxservice.util.Arguments;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.vertx.core.eventbus.DeliveryOptions;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Types;
import java.util.List;

public class MultiParamMethodProxyWriter extends MethodProxyWriter {

    protected MultiParamMethodProxyWriter(Types types) {
        super(types);
    }

    @Override
    public void writeMethod(TypeSpec.Builder builder, ExecutableElement element) {
        MethodSpec.Builder methodBuilder = MethodSpec.overriding(element);
        methodBuilder.addStatement("var $L = new $T()", Constant.OPTIONS_VARIABLE, ClassName.get(DeliveryOptions.class));
        methodBuilder.addStatement("$L.addHeader($S, $S)", Constant.OPTIONS_VARIABLE, Constant.ACTION, element.getSimpleName());
        methodBuilder.addStatement("$L.setCodecName($T.CODEC_NAME)", Constant.OPTIONS_VARIABLE, ClassName.get(ArgumentsMessageCodec.class));

        var messageVar = addArguments(methodBuilder, element.getParameters());

        var resultType = ((DeclaredType) element.getReturnType()).getTypeArguments().get(0);
        TypeName requestParamType = MethodProxyWriter.getRequestParamType(types, resultType);
        methodBuilder.addCode("return this.$L.eventBus().<$T>request(this.address, $L, $L)\n",
                Constant.VERTX_VARIABLE,
                requestParamType,
                messageVar,
                Constant.OPTIONS_VARIABLE);
        addResultHandler(methodBuilder, resultType);
        builder.addMethod(methodBuilder.build());
    }

    public String addArguments(MethodSpec.Builder methodBuilder, List<? extends VariableElement> params) {
        Object[] arguments = new Object[params.size() + 2];
        arguments[0] = Constant.ARGUMENTS_VARIABLE;
        arguments[1] = ClassName.get(Arguments.class);
        var format = new StringBuilder("var $L = $T.of($L");
        arguments[2] = addArgument(methodBuilder, params.get(0), 0);
        for (int i = 1; i < params.size(); ++i) {
            arguments[i + 2] = addArgument(methodBuilder, params.get(i), i);
            format.append(", $L");
        }
        format.append(")");
        methodBuilder.addStatement(format.toString(), arguments);
        return Constant.ARGUMENTS_VARIABLE;
    }

    private String addArgument(MethodSpec.Builder methodBuilder, VariableElement param, int paramIdx) {
        var paramType = param.asType();
        String paramName = param.getSimpleName().toString();
        if (SupportedTypes.isCollectionType(types, paramType)) {
            var newParamName = Constant.ARG_VARIABLE + paramIdx;
            methodBuilder.addStatement("var $L = $T.of($L)", newParamName, MethodProxyWriter.getRawParamType(types, paramType), paramName);
            paramName = newParamName;
        }
        return paramName;
    }
}
