package processor;

import annotation.AddMethod;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@SupportedAnnotationTypes({"annotation.AddMethod"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AddMethodProcessor extends AbstractProcessor {

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
        for (Element element : roundEnv.getElementsAnnotatedWith(AddMethod.class)) {
            PackageElement packageOf = processingEnvironment.getElementUtils().getPackageOf(element);

//            TypeSpec typeSpec = TypeSpec.classBuilder()


        }

        return false;
    }
}
