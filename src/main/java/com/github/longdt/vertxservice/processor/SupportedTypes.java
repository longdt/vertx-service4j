package com.github.longdt.vertxservice.processor;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.Shareable;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SupportedTypes {
    private static Set<String> supportedTypes;
    private static Set<String> supportedMapKey;

    static {
        supportedTypes = Set.of(
                boolean.class.getCanonicalName(),
                Boolean.class.getCanonicalName(),
                byte.class.getCanonicalName(),
                Byte.class.getCanonicalName(),
                short.class.getCanonicalName(),
                Short.class.getCanonicalName(),
                int.class.getCanonicalName(),
                Integer.class.getCanonicalName(),
                long.class.getCanonicalName(),
                Long.class.getCanonicalName(),
                char.class.getCanonicalName(),
                Character.class.getCanonicalName(),
                float.class.getCanonicalName(),
                Float.class.getCanonicalName(),
                double.class.getCanonicalName(),
                Double.class.getCanonicalName(),
                JsonObject.class.getCanonicalName(),
                JsonArray.class.getCanonicalName()
        );

        supportedMapKey = Set.of(
                Boolean.class.getCanonicalName(),
                Byte.class.getCanonicalName(),
                Short.class.getCanonicalName(),
                Integer.class.getCanonicalName(),
                Long.class.getCanonicalName(),
                Character.class.getCanonicalName(),
                Float.class.getCanonicalName(),
                Double.class.getCanonicalName()
        );
    }

    public static boolean isSupport(Elements elements, Types types, TypeMirror typeMirror) {
        if (supportedTypes.contains(typeMirror.toString())
                || types.isAssignable(typeMirror, elements.getTypeElement(Shareable.class.getCanonicalName()).asType())) {
            return true;
        }
        TypeMirror paramType;
        if (types.asElement(typeMirror).toString().equals(List.class.getCanonicalName())) {
            paramType = ((DeclaredType) typeMirror).getTypeArguments().get(0);
        } else if (types.asElement(typeMirror).toString().equals(Set.class.getCanonicalName())) {
            paramType = ((DeclaredType) typeMirror).getTypeArguments().get(0);
        } else if (types.asElement(typeMirror).toString().equals(Map.class.getCanonicalName())) {
            TypeMirror keyType = ((DeclaredType) typeMirror).getTypeArguments().get(0);
            if (!supportedMapKey.contains(keyType.toString())) {
                return false;
            }
            paramType = ((DeclaredType) typeMirror).getTypeArguments().get(1);
        } else {
            return false;
        }
        return supportedTypes.contains(paramType.toString())
                || types.isAssignable(paramType, elements.getTypeElement(Shareable.class.getCanonicalName()).asType());
    }

    public static boolean needShareableCodec(TypeMirror typeMirror) {
        return !supportedTypes.contains(typeMirror.toString());
    }

    public static boolean isListType(Types types, TypeMirror typeMirror) {
        return types.asElement(typeMirror).toString().equals(List.class.getCanonicalName());
    }

    public static boolean isSetType(Types types, TypeMirror typeMirror) {
        return types.asElement(typeMirror).toString().equals(Set.class.getCanonicalName());
    }

    public static boolean isMapType(Types types, TypeMirror typeMirror) {
        return types.asElement(typeMirror).toString().equals(Map.class.getCanonicalName());
    }
}
