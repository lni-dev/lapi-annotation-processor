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
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Set;

/**
 * processes the @Command annotation from lapi and automatically adds the corresponding service providers to the
 * META-INF/services folder
 */
@SupportedAnnotationTypes("me.linusdev.lapi.api.manager.command.Command")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class CommandAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "starting to process command annotations");

        for(TypeElement annotation : annotations) {
            if(!annotation.getQualifiedName().contentEquals("me.linusdev.lapi.api.manager.command.Command")) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "annotation " + annotation.getQualifiedName() + " is not @Command annotation!");
                return false;
            }

            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);
            ArrayList<String> qualifiedNames = new ArrayList<>(elements.size());

            for(Element element : elements) {
                if(element.getKind() != ElementKind.CLASS) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "element " + element.getSimpleName() + " is not a class!");
                    continue;
                }

                TypeElement typeElement = (TypeElement) element;
                if(!qualifiedNames.contains(typeElement.getQualifiedName().toString()))
                    qualifiedNames.add(typeElement.getQualifiedName().toString());
            }

            for(String name : qualifiedNames) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "correct element: " + name);
            }


            //TODO: first get resource and read the file, then add missing services.
            try {

                try {
                    FileObject file = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "",
                            "META-INF/services/me.linusdev.lapi.api.manager.command.BaseCommand");



                    Writer writer = file.openWriter();
                    for (String name : qualifiedNames) writer.append(name);
                    writer.close();

                } catch (IOException ex) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "cannot create Resources!");

                }

                /*try {
                    FileObject file = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "",
                            "META-INF/services/me.linusdev.lapi.api.manager.command.BaseCommand");

                    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "got file: " + file.getName());

                } catch (IOException e) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "cannot get Resources!");

                }*/
            }catch (Exception e) {
                e.printStackTrace();
            }

        }

        return true;
    }
}
