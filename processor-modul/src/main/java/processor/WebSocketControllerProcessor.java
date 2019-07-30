package processor;

import annotation.WebSocketController;
import com.squareup.javapoet.*;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.util.Name;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
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

            String nameField = getFieldName(element.getSimpleName().toString());
            String proxyType = createProxyType(element, nameField);

            ClassName type = ClassName.bestGuess(proxyType);
            FieldSpec filed = FieldSpec.builder(type, nameField)
                    .addModifiers(Modifier.PRIVATE)
                    .build();
            fields.add(filed);

            builderConstructor.addParameter(
                    ParameterSpec.builder(type, nameField).build()
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

    private String createProxyType(Element element, String fieldName) {
        TypeName typeName = TypeName.get(element.asType());
        String proxyType = "Proxy" + typeName.toString();
        TypeSpec wsConfiguration = TypeSpec
                .classBuilder(proxyType)
                .addModifiers(Modifier.PUBLIC)
                .addField(FieldSpec.builder(typeName, fieldName, Modifier.PRIVATE, Modifier.FINAL)
                        .build())
                .addAnnotation(Component.class)
                .superclass(TextWebSocketHandler.class)
                .addMethod(createConstructor(element, fieldName, typeName))
                .build();

        JavaFile javaFile = JavaFile
                .builder("", wsConfiguration)
                .indent("    ")
                .build();
        try {
            javaFile.writeTo(filer);
            return proxyType;
        } catch (IOException e) {
            return null;
        }
    }

    private MethodSpec createConstructor(Element element, String fieldName, TypeName typeName) {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addAnnotation(Autowired.class);

        StringBuilder code = new StringBuilder("this." + fieldName + " = new " + typeName + "(");
        for (Element enclosedElement : element.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.CONSTRUCTOR) {
                com.sun.tools.javac.util.List<Symbol.VarSymbol> parameters = ((Symbol.MethodSymbol) enclosedElement).getParameters();
                for (Symbol.VarSymbol parameter : parameters) {
                    Type type = parameter.asType();
                    Name simpleName = parameter.getSimpleName();
                    builder.addParameter(TypeName.get(type), simpleName.toString());
                    code.append(simpleName.toString()).append(",");
                }
                if (parameters.size() != 0) {
                    code.deleteCharAt(code.length() - 1);
                }
                break;
            }
        }
        code.append(");");
        builder.addCode(code.toString());
        return builder.build();
    }

    private String getFieldName(String name) {
        char firstLetter = Character.toLowerCase(name.charAt(0));
        return firstLetter + name.substring(1);
    }
}
