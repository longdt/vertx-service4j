package com.github.longdt.vertxservice.processor;

import com.squareup.javapoet.CodeBlock;
import io.vertx.serviceproxy.HelperUtils;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Types;

public class NoParamMethodProxyHandlerWriter extends MethodProxyHandlerWriter {
    protected NoParamMethodProxyHandlerWriter(Types types) {
        super(types);
    }

    @Override
    public void writeInvocation(CodeBlock.Builder codeBlockBuilder, ExecutableElement element) {
        codeBlockBuilder.beginControlFlow("case $S:", element.getSimpleName().toString());
        codeBlockBuilder.add("$L.$L()\n", Constant.SERVICE_VARIABLE, element.getSimpleName().toString());
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
}
