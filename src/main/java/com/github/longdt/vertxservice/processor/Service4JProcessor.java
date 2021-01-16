package com.github.longdt.vertxservice.processor;

import com.github.longdt.vertxservice.annotation.Service;
import com.google.auto.service.AutoService;
import com.google.common.base.Throwables;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

@AutoService(Processor.class)
public class Service4JProcessor extends AbstractProcessor {
    private Messager messager;
    private ServiceDeclaration.Factory serviceDF;

    @Override
    public void init(ProcessingEnvironment env) {
        super.init(env);
        var elements = processingEnv.getElementUtils();
        var types = processingEnv.getTypeUtils();
        messager = env.getMessager();
        serviceDF = new ServiceDeclaration.Factory(elements, types, messager);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            doProcess(roundEnv);
        } catch (Throwable e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Failed to process @Service annotations:\n"
                    + Throwables.getStackTraceAsString(e));
        }
        return false;
    }

    private void doProcess(RoundEnvironment roundEnv) throws IOException {
        // Iterate over the classes and methods that are annotated with @Service.
        for (Element element : roundEnv.getElementsAnnotatedWith(Service.class)) {
            var declaration = serviceDF.createIfValid(element);
            if (declaration.isPresent()) {
                var serviceDeclaration = declaration.get();
                new ServiceProxyWriter(processingEnv).writeServiceProxy(serviceDeclaration);
                new ServiceProxyHandlerWriter(processingEnv).writeServiceProxyHandler(serviceDeclaration);
            }

            System.out.println(declaration);
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(Service.class.getName());
    }


    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
