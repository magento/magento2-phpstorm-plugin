package com.magento.idea.magento2plugin.php.inspections;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.magento.idea.magento2plugin.php.module.ModuleManager;
import com.magento.idea.magento2plugin.php.util.MagentoTypes;
import org.jetbrains.annotations.NotNull;

/**
 * Created by dkvashnin on 12/18/15.
 */
public class ObjectManagerInspection extends PhpInspection {
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(ProblemsHolder problemsHolder, boolean b) {
        return new PhpElementVisitor() {
            @Override
            public void visitPhpMethodReference(MethodReference reference) {
                PsiElement referencedElement = reference.resolve();

                if(referencedElement instanceof Method) {
                    ModuleManager moduleManager = ModuleManager.getInstance(referencedElement.getProject());
                    if (moduleManager.getModuleForFile(reference.getContainingFile()) == null) {
                        return;
                    }

                    PhpClass phpClass = ((Method) referencedElement).getContainingClass();

                    verifyPhpClass(phpClass, reference);
                }
            }

            @Override
            public void visitPhpClassReference(ClassReference reference) {
                PsiElement referencedElement = reference.resolve();

                if(referencedElement instanceof PhpClass) {
                    ModuleManager moduleManager = ModuleManager.getInstance(referencedElement.getProject());
                    if (moduleManager.getModuleForFile(reference.getContainingFile()) == null) {
                        return;
                    }

                    verifyPhpClass((PhpClass)referencedElement, reference);
                }
            }

            private void verifyPhpClass(PhpClass phpClass, PsiReference reference) {
                if (phpClass == null) {
                    return;
                }

                if (phpClass.getPresentableFQN().equals(MagentoTypes.OBJECT_MANAGER_TYPE)) {
                    registerProblem(reference);
                    return;
                }

                for (PhpClass implementedInterface : phpClass.getImplementedInterfaces()) {
                    if (implementedInterface.getPresentableFQN().equals(MagentoTypes.OBJECT_MANAGER_TYPE)) {
                        registerProblem(reference);
                        return;
                    }
                }
            }

            private void registerProblem(PsiReference reference) {
                problemsHolder.registerProblem(reference, "ObjectManager is not recommended to use in module", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            }
        };
    }
}
