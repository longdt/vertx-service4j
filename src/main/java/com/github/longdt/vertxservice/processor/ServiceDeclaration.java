package com.github.longdt.vertxservice.processor;

import com.github.longdt.vertxservice.annotation.Service;
import com.google.auto.value.AutoValue;
import io.vertx.core.Future;

import javax.annotation.processing.Messager;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.tools.Diagnostic.Kind.ERROR;

@AutoValue
abstract class ServiceDeclaration {
    abstract TypeElement target();

    abstract List<ExecutableElement> methods();

    public static class Factory {
        private final Elements elements;
        private final Types types;
        private final Messager messager;

        Factory(Elements elements, Types types, Messager messager) {
            this.elements = elements;
            this.types = types;
            this.messager = messager;
        }

        Optional<ServiceDeclaration> createIfValid(Element element) {
            checkNotNull(element);
            Service service = element.getAnnotation(Service.class);
            if (service == null) {
                messager.printMessage(ERROR, element.getSimpleName() + " must be annotated with @Service", element);
                return Optional.empty();
            }
            if (element.getKind() != ElementKind.INTERFACE) {
                messager.printMessage(ERROR, element.getSimpleName() + " must be a interface", element);
                return Optional.empty();
            }
            var methods = ElementFilter.methodsIn(element.getEnclosedElements())
                    .stream()
                    .filter(e -> !e.isDefault() && !e.getModifiers().contains(Modifier.STATIC))
                    .collect(Collectors.toList());
            var invalidMethod = methods
                    .stream()
                    .filter(e -> !isValidMethod(e)).findAny();
            if (invalidMethod.isPresent()) {
                messager.printMessage(ERROR, element.getSimpleName() + "." + invalidMethod.get().getSimpleName() + " is not a valid method", invalidMethod.get());
                return Optional.empty();
            }
            return Optional.of(new AutoValue_ServiceDeclaration((TypeElement) element, methods));
        }

        private boolean isValidMethod(ExecutableElement element) {
            if (element.isDefault() || element.getModifiers().contains(Modifier.STATIC)) {
                return true;
            }
            var returnType = element.getReturnType();
            var returnElement = types.asElement(returnType);
            if (!returnElement.toString().equals(Future.class.getCanonicalName())) {
                return false;
            }
            var resultType = ((DeclaredType) returnType).getTypeArguments().get(0);
            return SupportedTypes.isSupport(elements, types, resultType) &&
                    element.getParameters().stream().allMatch(e -> SupportedTypes.isSupport(elements, types, e.asType()));
        }
    }
}
