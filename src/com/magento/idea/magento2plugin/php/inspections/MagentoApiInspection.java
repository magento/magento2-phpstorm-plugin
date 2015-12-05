package com.magento.idea.magento2plugin.php.inspections;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.magento.idea.magento2plugin.php.module.MagentoModule;
import com.magento.idea.magento2plugin.php.module.ModuleManager;
import org.jetbrains.annotations.NotNull;

/**
 * Created by dkvashnin on 12/4/15.
 */
public class MagentoApiInspection extends PhpInspection {
    public static final String API_TAG = "@api";

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder problemsHolder, boolean isOnTheFly) {
        return new PhpElementVisitor() {
            @Override
            public void visitPhpMethodReference(MethodReference reference) {
                MagentoApiInspection.check(reference, "Method #ref is not in module API", problemsHolder);
            }

            @Override
            public void visitPhpClassReference(ClassReference classReference) {
                MagentoApiInspection.check(classReference, "Class #ref is not in module API", problemsHolder);
            }
        };
    }

    private static void check(PhpReference reference, String desc, ProblemsHolder holder) {
        PsiElement element = reference.resolve();

        if(element instanceof PhpNamedElement) {
            MagentoModule referenceSourceModule = getMagentoModule((PhpPsiElement) element);
            MagentoModule currentModule = getMagentoModule((PhpPsiElement) (reference.getElement()));


            if (!areDifferentModules(referenceSourceModule, currentModule)) {
                return;
            }

            PhpDocComment docComment = ((PhpNamedElement)element).getDocComment();
            if(docComment == null) {
                holder.registerProblem(reference, desc, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                return;
            }

            PhpDocTag[] elements = docComment.getTagElementsByName(API_TAG);
            if(elements.length == 0) {
                holder.registerProblem(reference, desc, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            }
        }
    }

    private static MagentoModule getMagentoModule(PhpPsiElement element) {
        ModuleManager moduleManager = ModuleManager.getInstance(element.getProject());
        return moduleManager.getModuleForFile(element.getContainingFile());
    }

    private static boolean areDifferentModules(MagentoModule magentoModule1, MagentoModule magentoModule2) {
        if (magentoModule1 == null) {
            return false;
        }

        if (magentoModule2 == null) {
            return false;
        }

        return magentoModule1 != magentoModule2;
    }
}
