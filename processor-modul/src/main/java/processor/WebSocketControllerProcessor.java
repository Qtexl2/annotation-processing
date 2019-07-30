package processor;

import annotation.WebSocketController;
import com.squareup.javapoet.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes({"annotation.WebSocketController"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class WebSocketControllerProcessor extends AbstractProcessor {

    Filer filer;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        filer = processingEnv.getFiler();

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

        JavaFile javaFile = JavaFile
                .builder("", wsConfiguration)
                .indent("    ")
                .build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }



        return false;
    }


    private String getFieldName(String name){
        char firstLetter = Character.toLowerCase(name.charAt(0));
        return firstLetter + name.substring(1);
    }
}
