package com.github.longdt.vertxservice.processor;

import com.github.longdt.vertxservice.util.*;
import com.squareup.javapoet.*;
import io.vertx.core.eventbus.Message;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

public abstract class MethodProxyWriter {
    protected final Types types;

    protected MethodProxyWriter(Types types) {
        this.types = types;
    }

    public abstract void writeMethod(TypeSpec.Builder builder, ExecutableElement element);

    protected void addResultHandler(MethodSpec.Builder methodBuilder, TypeMirror resultType) {
        var collectionType = SupportedTypes.isCollectionType(types, resultType);
        if (collectionType) {
            methodBuilder.addCode("\t\t.map(msg -> {\n" +
                    "\t\t\tvar body = msg.body();\n" +
                    "\t\t\tif (body == null) {\n" +
                    "\t\t\t\treturn null;\n" +
                    "\t\t\t}\n" +
                    "\t\t\treturn body.unwrap();\n" +
                    "\t\t});");
        } else {
            methodBuilder.addCode("\t\t.map($T::body);", ClassName.get(Message.class));
        }
    }

    public static TypeName getRequestParamType(Types types, TypeMirror resultType) {
        if (SupportedTypes.isListType(types, resultType)) {
            var elementType = ((DeclaredType) resultType).getTypeArguments().get(0);
            return SupportedTypes.isImmutable(elementType) ?
                    ParameterizedTypeName.get(ClassName.get(ImmutableList.class), ClassName.get(elementType)) :
                    ParameterizedTypeName.get(ClassName.get(ShareableList.class), ClassName.get(elementType));
        } else if (SupportedTypes.isSetType(types, resultType)) {
            var elementType = ((DeclaredType) resultType).getTypeArguments().get(0);
            return SupportedTypes.isImmutable(elementType) ?
                    ParameterizedTypeName.get(ClassName.get(ImmutableSet.class), ClassName.get(elementType)) :
                    ParameterizedTypeName.get(ClassName.get(ShareableSet.class), ClassName.get(elementType));
        } else if (SupportedTypes.isMapType(types, resultType)) {
            var keyType = ((DeclaredType) resultType).getTypeArguments().get(0);
            var valueType = ((DeclaredType) resultType).getTypeArguments().get(1);
            if (SupportedTypes.isImmutable(keyType) && SupportedTypes.isImmutable(valueType)) {
                return ParameterizedTypeName.get(ClassName.get(ImmutableMap.class), ClassName.get(keyType), ClassName.get(valueType));
            } else if (!SupportedTypes.isImmutable(keyType) && !SupportedTypes.isImmutable(valueType)) {
                return ParameterizedTypeName.get(ClassName.get(ShareableMap.class), ClassName.get(keyType), ClassName.get(valueType));
            } else if (SupportedTypes.isImmutable(keyType)) {
                return ParameterizedTypeName.get(ClassName.get(ImmutableKeyMap.class), ClassName.get(keyType), ClassName.get(valueType));
            } else {
                return ParameterizedTypeName.get(ClassName.get(ShareableKeyMap.class), ClassName.get(keyType), ClassName.get(valueType));
            }
        } else {
            return TypeName.get(resultType);
        }
    }

    public static TypeName getRawParamType(Types types, TypeMirror typeMirror) {
        if (SupportedTypes.isListType(types, typeMirror)) {
            var elementType = ((DeclaredType) typeMirror).getTypeArguments().get(0);
            return SupportedTypes.isImmutable(elementType) ?
                    ClassName.get(ImmutableList.class) :
                    ClassName.get(ShareableList.class);
        } else if (SupportedTypes.isSetType(types, typeMirror)) {
            var elementType = ((DeclaredType) typeMirror).getTypeArguments().get(0);
            return SupportedTypes.isImmutable(elementType) ?
                    ClassName.get(ImmutableSet.class) :
                    ClassName.get(ShareableSet.class);
        } else if (SupportedTypes.isMapType(types, typeMirror)) {
            var keyType = ((DeclaredType) typeMirror).getTypeArguments().get(0);
            var valueType = ((DeclaredType) typeMirror).getTypeArguments().get(1);
            if (SupportedTypes.isImmutable(keyType) && SupportedTypes.isImmutable(valueType)) {
                return ClassName.get(ImmutableMap.class);
            } else if (!SupportedTypes.isImmutable(keyType) && !SupportedTypes.isImmutable(valueType)) {
                return ClassName.get(ShareableMap.class);
            } else if (SupportedTypes.isImmutable(keyType)) {
                return ClassName.get(ImmutableKeyMap.class);
            } else {
                return ClassName.get(ShareableKeyMap.class);
            }
        } else {
            return TypeName.get(typeMirror);
        }
    }
}
