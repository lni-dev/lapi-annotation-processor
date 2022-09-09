/*
 * Copyright (c) 2022 Linus Andera All rights reserved
 *
 */

package me.linusdev.processor.command;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.Set;

@SupportedAnnotationTypes("me.linusdev.lapi.api.manager.command.Command")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class CommandAnnotationProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "hiii");
        return false;

        /*roundEnv.processingOver();

        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "hi");
        System.out.println("process");

        for(TypeElement annotation : annotations) {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);

            for( Element e : elements){
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, e.getSimpleName());
            }

        }

        try {
            processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, "main", "a");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "hi");

        return true;*/
    }

    public static void main(String[] args) {
        System.out.println("main1");
        System.out.println("mainsdad2");
    }
}
