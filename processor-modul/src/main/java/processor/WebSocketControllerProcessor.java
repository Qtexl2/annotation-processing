package processor;

import annotation.WebSocketController;
import com.squareup.javapoet.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes({"annotation.WebSocketController"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class WebSocketControllerProcessor extends AbstractProcessor {

    Filer filer;
    Types typeUtils;
    Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        filer = processingEnv.getFiler();
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {


        String registry = "registry";
        List<FieldSpec> fields = new ArrayList<>();
        MethodSpec.Builder builderConstructor = MethodSpec.constructorBuilder();
        CodeBlock.Builder codeBuilder = CodeBlock.builder();
        for (Element element : roundEnv.getElementsAnnotatedWith(WebSocketController.class)) {

            TypeName typeFiled = TypeName.get(element.asType());
            String nameField = getFieldName(element.getSimpleName().toString());

            try {
                Class<?> aClass = Class.forName(typeFiled.toString());
                System.out.println(aClass);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }


            createProxyType(element, nameField);
            FieldSpec filed = FieldSpec.builder(typeFiled, nameField)
                    .addModifiers(Modifier.PRIVATE)
                    .build();
            fields.add(filed);

            builderConstructor.addParameter(
                    ParameterSpec.builder(typeFiled, nameField).build()
            );
            builderConstructor.addCode(
                    CodeBlock.builder()
                            .addStatement("this." + nameField + " = " + nameField)
                            .build()
            );

            WebSocketController annotation = element.getAnnotation(WebSocketController.class);
            String url = annotation.url();
            String format = registry + ".addHandler(" + nameField + ", \"" + url + "\")";
            codeBuilder.addStatement(format);

        }


        TypeSpec wsConfiguration = TypeSpec
                .classBuilder("WebSocketConfiguration")
                .addModifiers(Modifier.PUBLIC)
                .addFields(fields)
                .addMethod(MethodSpec
                        .methodBuilder("registerWebSocketHandlers")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(void.class)
                        .addParameter(WebSocketHandlerRegistry.class, registry)
                        .addCode(codeBuilder.build())
                        .build())
                .addAnnotation(Configuration.class)
                .addAnnotation(EnableWebSocket.class)
                .addMethod(builderConstructor.build())
                .addSuperinterface(WebSocketConfigurer.class)
                .build();

//        JavaFile javaFile = JavaFile
//                .builder("", wsConfiguration)
//                .indent("    ")
//                .build();
//        try {
//            javaFile.writeTo(filer);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return false;
    }

    private void createProxyType(Element element, String fieldName) {
        TypeName typeName = TypeName.get(element.asType());
        createFields(element);
        TypeSpec wsConfiguration = TypeSpec
                .classBuilder("Proxy" + typeName.toString())
                .addModifiers(Modifier.PUBLIC)
                .addField(FieldSpec.builder(typeName, fieldName, Modifier.PRIVATE)
                        .build())
                .addAnnotation(Component.class)
                .superclass(TextWebSocketHandler.class)
                .build();

        JavaFile javaFile = JavaFile
                .builder("", wsConfiguration)
                .indent("    ")
                .build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<FieldSpec> createFields(Element element){
        Set<VariableElement> variableElements = ElementFilter.fieldsIn(Set.of(element));
        for (VariableElement variableElement : variableElements) {
            System.out.println(variableElement.getSimpleName());
            System.out.println(variableElement.getConstantValue());
            System.out.println(variableElement.getEnclosingElement().asType());
        }

        return null;

    }

    private String getFieldName(String name){
        char firstLetter = Character.toLowerCase(name.charAt(0));
        return firstLetter + name.substring(1);
    }
}
