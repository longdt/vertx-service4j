package com.github.longdt.vertxservice.processor;

import com.github.longdt.vertxservice.util.Arguments;
import com.squareup.javapoet.CodeBlock;
import io.vertx.serviceproxy.HelperUtils;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Types;
import java.util.List;

public class MultiParamMethodProxyHandlerWriter extends MethodProxyHandlerWriter {
    public MultiParamMethodProxyHandlerWriter(Types types) {
        super(types);
    }

    @Override
    public void writeInvocation(CodeBlock.Builder codeBlockBuilder, ExecutableElement element) {
        codeBlockBuilder.beginControlFlow("case $S:", element.getSimpleName().toString());
        var params = element.getParameters();
        codeBlockBuilder.addStatement("var $L = ($T) $L.body()", Constant.ARGUMENTS_VARIABLE, Arguments.class, Constant.MESSAGE_VARIABLE);
        extractArguments(codeBlockBuilder, params);
        invoke(codeBlockBuilder, element);
        codeBlockBuilder.indent().indent()
                .beginControlFlow(".onComplete(res ->")
                .beginControlFlow("if (res.failed())")
                .addStatement("$T.manageFailure($L, res.cause(), $L)", HelperUtils.class, Constant.MESSAGE_VARIABLE, Constant.INCLUDE_DEBUG_INFO_VARIABLE)
                .nextControlFlow("else");

        var resultType = ((DeclaredType) element.getReturnType()).getTypeArguments().get(0);
        addReplyHandler(codeBlockBuilder, resultType);

        codeBlockBuilder.endControlFlow()
                .unindent().add("});\n")
                .unindent().unindent();
        codeBlockBuilder.addStatement("break");
        codeBlockBuilder.endControlFlow();
    }

    private void invoke(CodeBlock.Builder codeBlockBuilder, ExecutableElement element) {
        var params = element.getParameters();
        Object[] args = new Object[params.size() + 2];
        args[0] = Constant.SERVICE_VARIABLE;
        args[1] = element.getSimpleName();
        args[2] = params.get(0).getSimpleName();
        StringBuilder format = new StringBuilder("$L.$L($L");
        for (int i = 1; i < params.size(); ++i) {
            args[i + 2] = params.get(i).getSimpleName();
            format.append(", $L");
        }
        format.append(")\n");
        codeBlockBuilder.add(format.toString(), args);
    }

    void extractArguments(CodeBlock.Builder codeBlockBuilder, List<? extends VariableElement> params) {
        for (int i = 0; i < params.size(); ++i) {
            var param = params.get(i);
            var paramType = param.asType();
            boolean needUnwrap = SupportedTypes.isCollectionType(types, paramType);
            if (needUnwrap) {
                codeBlockBuilder.addStatement("var $L = $L.<$T>getObject($L).unwrap()", param.getSimpleName(),
                        Constant.ARGUMENTS_VARIABLE,
                        MethodProxyWriter.getRequestParamType(types, paramType), i);
            } else {
                codeBlockBuilder.addStatement("var $L = $L.<$T>getObject($L)", param.getSimpleName(),
                        Constant.ARGUMENTS_VARIABLE,
                        MethodProxyWriter.getRequestParamType(types, paramType), i);
            }
        }
    }
}
