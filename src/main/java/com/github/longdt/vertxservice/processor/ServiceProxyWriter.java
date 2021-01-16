package com.github.longdt.vertxservice.processor;

import com.github.longdt.vertxservice.codecs.ArgumentsMessageCodec;
import com.github.longdt.vertxservice.codecs.MessageCodecs;
import com.github.longdt.vertxservice.codecs.ShareableMessageCodec;
import com.github.longdt.vertxservice.util.Arguments;
import com.github.longdt.vertxservice.util.ShareableList;
import com.github.longdt.vertxservice.util.ShareableMap;
import com.github.longdt.vertxservice.util.ShareableSet;
import com.squareup.javapoet.*;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.serviceproxy.ServiceException;
import io.vertx.serviceproxy.ServiceExceptionMessageCodec;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import java.io.IOException;
import java.util.stream.Collectors;

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
        addFields(builder, serviceDeclaration);
        addConstructor(builder, serviceDeclaration);
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
        MethodSpec.Builder methodBuilder = MethodSpec.overriding(element);
        methodBuilder.addStatement("var $L = new $T()", Constant.OPTIONS_VARIABLE, ClassName.get(DeliveryOptions.class));
        methodBuilder.addStatement("$L.addHeader($S, $S)", Constant.OPTIONS_VARIABLE, Constant.ACTION, element.getSimpleName());
        var params = element.getParameters();
        String messageVar;
        if (params.size() > 1) {
            methodBuilder.addStatement("$L.setCodecName($T.CODEC_NAME)", Constant.OPTIONS_VARIABLE, ClassName.get(ArgumentsMessageCodec.class));
            messageVar = "arguments";
            methodBuilder.addStatement("var $L = $T.of$L", messageVar, ClassName.get(Arguments.class),
                    params.stream().map(VariableElement::getSimpleName).collect(Collectors.joining(", ", "(", ")")));
        } else if (params.size() == 1 && SupportedTypes.needShareableCodec(params.get(0).asType())) {
            methodBuilder.addStatement("$L.setCodecName($T.CODEC_NAME)", Constant.OPTIONS_VARIABLE, ClassName.get(ShareableMessageCodec.class));
            var param = params.get(0);
            messageVar = param.getSimpleName().toString();
            if (SupportedTypes.isListType(types, param.asType())) {
            }
        } else {
            messageVar = "null";
        }
        var resultType = ((DeclaredType) element.getReturnType()).getTypeArguments().get(0);
        TypeName requestParamType;
        var collectionType = true;
        if (SupportedTypes.isListType(types, resultType)) {
            requestParamType = ClassName.get(ShareableList.class);
        } else if (SupportedTypes.isSetType(types, resultType)) {
            requestParamType = ClassName.get(ShareableSet.class);
        } else if (SupportedTypes.isMapType(types, resultType)) {
            requestParamType = ClassName.get(ShareableMap.class);
        } else {
            requestParamType = TypeName.get(resultType);
            collectionType = false;
        }

        methodBuilder.addCode("return $L.eventBus().<$T>request(address, $L, $L)\n",
                Constant.VERTX_VARIABLE,
                requestParamType,
                messageVar,
                Constant.OPTIONS_VARIABLE);
        if (collectionType) {
            methodBuilder.addCode("\t\t.map(msg -> {\n" +
                    "\t\t\tvar body = msg.body();\n" +
                    "\t\t\tif (body == null) {\n" +
                    "\t\t\t\treturn null;\n" +
                    "\t\t\t}\n" +
                    "\t\t\treturn body.getObject();\n" +
                    "\t\t});");
        } else {
            methodBuilder.addCode("\t\t.map($T::body);", ClassName.get(Message.class));
        }
        builder.addMethod(methodBuilder.build());
    }

    private void addConstructor(TypeSpec.Builder builder, ServiceDeclaration serviceDeclaration) {
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

    private void addFields(TypeSpec.Builder builder, ServiceDeclaration serviceDeclaration) {
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