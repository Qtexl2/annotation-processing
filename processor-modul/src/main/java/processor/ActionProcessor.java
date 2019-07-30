package processor;

import annotation.Action;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.List;
import java.util.Set;


@SupportedAnnotationTypes({"annotation.Action"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ActionProcessor extends AbstractProcessor {

    Messager messager;
    Filer filer;
    ProcessingEnvironment processingEnvironment;

    @Override
    public void init(ProcessingEnvironment env) {
        messager = env.getMessager();
        filer = env.getFiler();
        processingEnvironment = env;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(Action.class)) {
            ParameterSpec strings = ParameterSpec
                    .builder(
                            ParameterizedTypeName.get(ClassName.get(List.class), TypeName.get(String.class)),
                            "strings")
                    .build();
            FieldSpec name = FieldSpec
                    .builder(String.class, "name")
                    .addModifiers(Modifier.PRIVATE)
                    .build();
            CodeBlock sumOfTenImpl = CodeBlock
                    .builder()
                    .addStatement("int sum = 0")
                    .beginControlFlow("for (int i = 0; i <= 10; i++)")
                    .addStatement("sum += i")
                    .endControlFlow()
                    .build();

            MethodSpec sumOfTen = MethodSpec
                    .methodBuilder("sumOfTen")
                    .addParameter(int.class, "number")
                    .addParameter(strings)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addCode(sumOfTenImpl)
                    .build();

            TypeSpec person = TypeSpec
                    .classBuilder("Person")
                    .addModifiers(Modifier.PUBLIC)
                    .addField(name)
                    .addMethod(MethodSpec
                            .methodBuilder("getName")
                            .addModifiers(Modifier.PUBLIC)
                            .returns(String.class)
                            .addStatement("return this.name")
                            .build())
                    .addMethod(MethodSpec
                            .methodBuilder("setName")
                            .addParameter(String.class, "name")
                            .addModifiers(Modifier.PUBLIC)
                            .returns(void.class)
                            .addStatement("this.name = name")
                            .build())
                    .addMethod(sumOfTen)
                    .build();

            JavaFile javaFile = JavaFile
                    .builder("", person)
                    .indent("    ")
                    .build();

            try {
                javaFile.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        return false;
    }

}
