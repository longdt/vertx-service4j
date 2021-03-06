package com.github.longdt.vertxservice.processor;

import com.github.longdt.vertxservice.codecs.ArgumentsMessageCodec;
import com.github.longdt.vertxservice.codecs.MessageCodecs;
import com.github.longdt.vertxservice.codecs.ShareableMessageCodec;
import com.squareup.javapoet.*;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ServiceException;
import io.vertx.serviceproxy.ServiceExceptionMessageCodec;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import java.io.IOException;

import static com.google.auto.common.GeneratedAnnotationSpecs.generatedAnnotationSpec;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static com.squareup.javapoet.TypeSpec.interfaceBuilder;
import static javax.lang.model.element.Modifier.PUBLIC;

public class ServiceProxyWriter {
    private final Filer filer;
    private final Elements elements;
    private final SourceVersion sourceVersion;
    private final Types types;

    ServiceProxyWriter(ProcessingEnvironment processingEnv) {
        this.filer = processingEnv.getFiler();
        this.elements = processingEnv.getElementUtils();
        this.sourceVersion = processingEnv.getSourceVersion();
        this.types = processingEnv.getTypeUtils();
    }

    void writeServiceProxy(ServiceDeclaration serviceDeclaration) throws IOException {
        String serviceProxy = serviceDeclaration.target().getSimpleName().toString() + Constant.PROXY;
        TypeSpec.Builder builder =
                classBuilder(serviceProxy)
                        .addOriginatingElement(serviceDeclaration.target());
        generatedAnnotationSpec(
                elements,
                sourceVersion,
                Service4JProcessor.class,
                "Do not edit this file")
                .ifPresent(builder::addAnnotation);
        builder.addModifiers(Modifier.PUBLIC);
        addSuperinterface(builder, serviceDeclaration);
        addFields(builder);
        addConstructor(builder);
        addMethods(builder, serviceDeclaration);
        JavaFile.builder(elements.getPackageOf(serviceDeclaration.target()).getQualifiedName().toString(), builder.build())
                .skipJavaLangImports(true)
                .build()
                .writeTo(filer);
    }

    private void addMethods(TypeSpec.Builder builder, ServiceDeclaration serviceDeclaration) {
        serviceDeclaration.methods().forEach(e -> addMethod(builder, e));
    }

    private void addMethod(TypeSpec.Builder builder, ExecutableElement element) {
        var params = element.getParameters();
        if (params.isEmpty()) {
            new NoParamMethodProxyWriter(types).writeMethod(builder, element);
        } else if (params.size() == 1) {
            new SingleParamMethodProxyWriter(types).writeMethod(builder, element);
        } else {
            new MultiParamMethodProxyWriter(types).writeMethod(builder, element);
        }
    }

    private void addConstructor(TypeSpec.Builder builder) {
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder();
        constructor.addModifiers(PUBLIC);
        constructor.addParameter(TypeName.get(Vertx.class), Constant.VERTX_VARIABLE);
        constructor.addParameter(TypeName.get(String.class), Constant.ADDRESS_VARIABLE);
        constructor.addStatement("this.$1L = $1L", Constant.VERTX_VARIABLE);
        constructor.addStatement("this.$1L = $1L", Constant.ADDRESS_VARIABLE);
        constructor.addStatement("$1T.registerDefaultCodec($2L, $3T.class, new $4T())",
                ClassName.get(MessageCodecs.class),
                Constant.VERTX_VARIABLE,
                ClassName.get(ServiceException.class),
                ClassName.get(ServiceExceptionMessageCodec.class));
        constructor.addStatement("$1T.registerCodec($2L, new $3T())",
                ClassName.get(MessageCodecs.class),
                Constant.VERTX_VARIABLE,
                ClassName.get(ArgumentsMessageCodec.class));
        constructor.addStatement("$1T.registerCodec($2L, new $3T())",
                ClassName.get(MessageCodecs.class),
                Constant.VERTX_VARIABLE,
                ClassName.get(ShareableMessageCodec.class));
        builder.addMethod(constructor.build());
    }

    private void addFields(TypeSpec.Builder builder) {
        builder.addField(
                FieldSpec.builder(Vertx.class, Constant.VERTX_VARIABLE, Modifier.PRIVATE, Modifier.FINAL)
                        .build());
        builder.addField(
                FieldSpec.builder(String.class, Constant.ADDRESS_VARIABLE, Modifier.PRIVATE, Modifier.FINAL)
                        .build());
    }

    private void addSuperinterface(TypeSpec.Builder builder, ServiceDeclaration serviceDeclaration) {
        builder.addSuperinterface(serviceDeclaration.target().asType());
    }
}
