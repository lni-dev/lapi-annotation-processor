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
import java.io.*;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Set;

/**
 * processes the @Command annotation from lapi and automatically adds the corresponding service providers to the
 * META-INF/services folder
 */
@SupportedAnnotationTypes(CommandAnnotationProcessor.COMMAND_ANNOTATION_QUALIFIED_NAME)
public class CommandAnnotationProcessor extends AbstractProcessor {

    public static final String COMMAND_ANNOTATION_QUALIFIED_NAME = "me.linusdev.lapi.api.manager.command.Command";
    public static final String BASE_COMMAND_QUALIFIED_NAME = "me.linusdev.lapi.api.manager.command.BaseCommand";
    public static final String SERVICES_FOLDER = "META-INF/services/";

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if(roundEnv.processingOver()){
            //TODO: check if processing worked fine. see https://stackoverflow.com/questions/47779403/annotation-processing-roundenvironment-processingover
            return true;
        }

        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Starting to process @Command annotations");

        for(TypeElement annotation : annotations) {
            if(!annotation.getQualifiedName().contentEquals(COMMAND_ANNOTATION_QUALIFIED_NAME)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Annotation " + annotation.getQualifiedName() + " is not @Command annotation!");
                return false;
            }

            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);
            ArrayList<String> qualifiedNames = new ArrayList<>(elements.size());

            for(Element element : elements) {
                if(element.getKind() != ElementKind.CLASS) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Element " + element.getSimpleName() + " is not a class!");
                    continue;
                }
                TypeElement typeElement = (TypeElement) element;
                TypeElement baseCommandType = processingEnv.getElementUtils().getTypeElement(BASE_COMMAND_QUALIFIED_NAME);

                if(baseCommandType == null) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Project is missing BaseCommand class!");
                    throw new RuntimeException("Project is missing BaseCommand class!");
                }

                if(!processingEnv.getTypeUtils().isSubtype(typeElement.asType(), baseCommandType.asType())) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "Element '" + typeElement.getQualifiedName()
                    + "' is no subtype of BaseCommand. It will be skipped.");
                    continue;
                }


                if(!qualifiedNames.contains(typeElement.getQualifiedName().toString()))
                    qualifiedNames.add(typeElement.getQualifiedName().toString());
            }

            for(String name : qualifiedNames) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Found element: " + name);
            }

            ArrayList<String> alreadyContained = new ArrayList<>();
            BufferedReader reader = null;
            try {
                FileObject file = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "",
                        SERVICES_FOLDER + BASE_COMMAND_QUALIFIED_NAME);

                reader = new BufferedReader(new InputStreamReader(file.openInputStream()));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    if (!line.isBlank()) alreadyContained.add(line);
                }
            } catch (FileNotFoundException | NoSuchFileException exception) {
                //File does not exist.

            } catch (IOException exception) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Cannot read resource: " + exception);

            } finally {
                if(reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {throw new RuntimeException(e);}
                }

            }

            Writer writer = null;
            try {
                FileObject file = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "",
                        SERVICES_FOLDER + BASE_COMMAND_QUALIFIED_NAME);



                writer = file.openWriter();
                for (String name : qualifiedNames) {
                    if(!alreadyContained.contains(name)){
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Adding element: " + name);
                        writer.append(name);
                    }
                }
                writer.close();

            } catch (IOException exception) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Cannot write resource: " + exception);
                throw new RuntimeException(exception);

            } finally {
                if(writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {throw new RuntimeException(e);}
                }

            }

        }

        return true;
    }
}
