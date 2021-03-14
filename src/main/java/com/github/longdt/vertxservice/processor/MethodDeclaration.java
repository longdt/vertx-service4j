package com.github.longdt.vertxservice.processor;

import com.google.auto.value.AutoValue;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeParameterElement;
import java.util.List;

@AutoValue
abstract class MethodDeclaration {
    abstract ExecutableElement method();
    abstract String action();
    abstract Class<?> requestMessageClass();
    abstract String requestCodec();
    abstract List<TypeParameterElement> parameters();
    abstract String replyCodec();
    abstract Class<?> replyMessageClass();
}
