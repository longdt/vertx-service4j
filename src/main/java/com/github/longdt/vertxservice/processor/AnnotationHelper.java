package com.github.longdt.vertxservice.processor;

import com.squareup.javapoet.ClassName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Supplier;

public class AnnotationHelper {
    public static List<VariableElement> getAllFields(Types types, TypeElement element) {
        var result = ElementFilter.fieldsIn(element.getEnclosedElements());
        TypeMirror superClass;
        while ((superClass = element.getSuperclass()).getKind() != TypeKind.NONE) {
            element = (TypeElement) types.asElement(superClass);
            result.addAll(ElementFilter.fieldsIn(element.getEnclosedElements()));
        }
        return result;
    }

    public static List<ExecutableElement> getAllInterfaceMethods(Types types, TypeElement interfaceElement) {
        Deque<TypeElement> interfaces = new ArrayDeque<>();
        interfaces.offer(interfaceElement);
        var result = new ArrayList<ExecutableElement>();
        while ((interfaceElement = interfaces.poll()) != null) {
            result.addAll(ElementFilter.methodsIn(interfaceElement.getEnclosedElements()));
            interfaceElement.getInterfaces().forEach(t -> interfaces.offer((TypeElement) types.asElement(t)));
        }
        return result;
    }

    public static TypeMirror getTypeMirror(Elements elements, Class<?> clazz) {
        return elements.getTypeElement(clazz.getCanonicalName()).asType();
    }

    public static TypeMirror getTypeMirror(Elements elements, Supplier<Class<?>> classSupplier) {
        try {
            Class<?> clazz = classSupplier.get();
            return elements.getTypeElement(clazz.getCanonicalName()).asType();
        } catch (MirroredTypeException e) {
            return e.getTypeMirror();
        }
    }

    public static TypeElement getTypeElement(Elements elements, Types types, Supplier<Class<?>> classSupplier) {
        try {
            Class<?> clazz = classSupplier.get();
            return elements.getTypeElement(clazz.getCanonicalName());
        } catch (MirroredTypeException e) {
            return (TypeElement) types.asElement(e.getTypeMirror());
        }
    }

    public static String getSimpleClassName(TypeMirror typeMirror) {
        return ((ClassName) ClassName.get(typeMirror)).simpleName();
    }
}
