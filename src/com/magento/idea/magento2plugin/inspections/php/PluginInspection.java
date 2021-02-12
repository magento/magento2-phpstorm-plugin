/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.php;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpBundle;
import com.jetbrains.php.PhpClassHierarchyUtils;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.magento.idea.magento2plugin.bundles.InspectionBundle;
import com.magento.idea.magento2plugin.inspections.php.util.PhpClassImplementsInterfaceUtil;
import com.magento.idea.magento2plugin.inspections.php.util.PhpClassImplementsNoninterceptableInterfaceUtil;
import com.magento.idea.magento2plugin.magento.files.Plugin;
import com.magento.idea.magento2plugin.magento.packages.MagentoPhpClass;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import com.magento.idea.magento2plugin.util.magento.plugin.GetTargetClassNamesByPluginClassName;
import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"PMD.ExcessiveMethodLength", "PMD.NPathComplexity"})
public class PluginInspection extends PhpInspection {

    private static final String WRONG_PARAM_TYPE = "inspection.wrong_param_type";

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(
            final @NotNull ProblemsHolder problemsHolder,
            final boolean isOnTheFly
    ) {
        return new PhpElementVisitor() {
            private final Integer beforePluginExtraParamsStart = 2;//NOPMD
            private final Integer afterAndAroundPluginExtraParamsStart = 3;//NOPMD
            private final InspectionBundle inspectionBundle = new InspectionBundle();

            private String getPluginPrefix(final Method pluginMethod) {
                final String pluginMethodName = pluginMethod.getName();
                if (pluginMethodName.startsWith(Plugin.PluginType.around.toString())) {
                    return Plugin.PluginType.around.toString();
                }
                if (pluginMethodName.startsWith(Plugin.PluginType.before.toString())) {
                    return Plugin.PluginType.before.toString();
                }
                if (pluginMethodName.startsWith(Plugin.PluginType.after.toString())) {
                    return Plugin.PluginType.after.toString();
                }

                return null;
            }

            @Override
            public void visitPhpMethod(final Method pluginMethod) {
                final String pluginPrefix = getPluginPrefix(pluginMethod);
                if (pluginPrefix == null) {
                    return;
                }

                final PsiElement parentClass = pluginMethod.getParent();
                if (!(parentClass instanceof PhpClass)) {
                    return;
                }
                final PsiElement currentClassNameIdentifier =
                        ((PhpClass) parentClass).getNameIdentifier();
                final String currentClass = ((PhpClass) parentClass).getFQN().substring(1);
                final GetTargetClassNamesByPluginClassName targetClassesService =
                        GetTargetClassNamesByPluginClassName.getInstance(
                                problemsHolder.getProject()
                        );
                final ArrayList<String> targetClassNames =
                        targetClassesService.execute(currentClass);

                for (final String targetClassName : targetClassNames) {

                    final PhpClass target = GetPhpClassByFQN.getInstance(
                            problemsHolder.getProject()
                    ).execute(targetClassName);
                    if (target == null) {
                        return;
                    }
                    checkTargetClass(currentClassNameIdentifier, target);

                    final String targetClassMethodName = getTargetMethodName(
                            pluginMethod,
                            pluginPrefix
                    );
                    if (targetClassMethodName == null) {
                        return;
                    }
                    final Method targetMethod = target.findMethodByName(targetClassMethodName);
                    if (targetMethod == null) {
                        return;
                    }
                    checkTargetMethod(pluginMethod, targetClassMethodName, targetMethod);
                    checkParametersCompatibility(
                            pluginMethod,
                            pluginPrefix,
                            targetClassName,
                            targetMethod
                    );
                }
            }

            private void checkTargetClass(
                    final PsiElement currentClassNameIdentifier,
                    final PhpClass target
            ) {
                if (target.isFinal()) {
                    final ProblemDescriptor[] currentResults = problemsHolder.getResultsArray();
                    final int finalClassProblems = getFinalClassProblems(currentResults);
                    if (finalClassProblems == 0) {
                        problemsHolder.registerProblem(
                                currentClassNameIdentifier,
                                inspectionBundle.message("inspection.plugin.error.finalClass"),
                                ProblemHighlightType.ERROR
                        );
                    }
                }


                if (PhpClassImplementsNoninterceptableInterfaceUtil.execute(target)) {
                    problemsHolder.registerProblem(
                            currentClassNameIdentifier,
                            inspectionBundle.message(
                                    "inspection.plugin.error.noninterceptableInterface"
                            ),
                            ProblemHighlightType.ERROR
                    );
                }
            }

            private void checkTargetMethod(
                    final Method pluginMethod,
                    final String targetClassMethodName,
                    final Method targetMethod
            ) {
                if (targetClassMethodName.equals(Plugin.CONSTRUCT_METHOD_NAME)) {
                    problemsHolder.registerProblem(
                            pluginMethod.getNameIdentifier(),
                            inspectionBundle.message("inspection.plugin.error.constructMethod"),
                            ProblemHighlightType.ERROR
                    );
                }
                if (targetMethod.isFinal()) {
                    problemsHolder.registerProblem(
                            pluginMethod.getNameIdentifier(),
                            inspectionBundle.message("inspection.plugin.error.finalMethod"),
                            ProblemHighlightType.ERROR
                    );
                }
                if (targetMethod.isStatic()) {
                    problemsHolder.registerProblem(
                            pluginMethod.getNameIdentifier(),
                            inspectionBundle.message("inspection.plugin.error.staticMethod"),
                            ProblemHighlightType.ERROR
                    );
                }
                if (!targetMethod.getAccess().toString().equals(Plugin.PUBLIC_ACCESS)) {
                    problemsHolder.registerProblem(
                            pluginMethod.getNameIdentifier(),
                            inspectionBundle.message("inspection.plugin.error.nonPublicMethod"),
                            ProblemHighlightType.ERROR
                    );
                }
            }

            private void checkParametersCompatibility(
                    final Method pluginMethod,
                    final String pluginPrefix,
                    final String targetClassName,
                    final Method targetMethod
            ) {
                final Parameter[] targetMethodParameters = targetMethod.getParameters();
                final Parameter[] pluginMethodParameters = pluginMethod.getParameters();

                int index = 0;
                for (final Parameter pluginMethodParameter : pluginMethodParameters) {
                    index++;
                    String declaredType = pluginMethodParameter.getDeclaredType().toString();

                    if (declaredType.isEmpty()) {
                        declaredType = pluginMethodParameter.getDocType().toString();
                    }

                    if (index == 1) { //NOPMD
                        final String targetClassFqn = Package.fqnSeparator.concat(targetClassName);
                        if (!checkTypeIncompatibility(targetClassFqn, declaredType)) {
                            problemsHolder.registerProblem(
                                    pluginMethodParameter,
                                    PhpBundle.message(
                                            WRONG_PARAM_TYPE,
                                            new Object[]{declaredType, targetClassFqn}
                                            ),
                                    ProblemHighlightType.ERROR
                                );
                        }
                        if (!checkPossibleTypeIncompatibility(targetClassFqn, declaredType)) {
                            problemsHolder.registerProblem(
                                    pluginMethodParameter,
                                    inspectionBundle.message(
                                            "inspection.plugin.error.typeIncompatibility"
                                    ),
                                    ProblemHighlightType.WEAK_WARNING
                            );
                        }
                        continue;
                    }
                    if (index == 2 && pluginPrefix.equals(Plugin.PluginType.around.toString())) {
                        if (!checkTypeIncompatibility(Plugin.CALLABLE_PARAM, declaredType)
                                && !checkTypeIncompatibility(
                                    Package.fqnSeparator.concat(Plugin.CLOSURE_PARAM),
                                    declaredType)
                        ) {
                            problemsHolder.registerProblem(
                                    pluginMethodParameter,
                                    PhpBundle.message(
                                        WRONG_PARAM_TYPE,
                                        new Object[]{declaredType, "callable"}
                                    ),
                                    ProblemHighlightType.ERROR);
                        }
                        continue;
                    }
                    if (index == 2 && pluginPrefix.equals(Plugin.PluginType.after.toString())
                            && !targetMethod.getDeclaredType().toString().equals("void")) {
                        if (declaredType.isEmpty() || targetMethod.getDeclaredType()
                                .toString().isEmpty()) {
                            continue;
                        }
                        if (!checkTypeIncompatibility(targetMethod.getDeclaredType()
                                .toString(), declaredType)) {
                            problemsHolder.registerProblem(
                                    pluginMethodParameter,
                                    PhpBundle.message(
                                            WRONG_PARAM_TYPE,
                                                new Object[]{declaredType,
                                                targetMethod.getDeclaredType().toString()}
                                                ),
                                    ProblemHighlightType.ERROR
                            );
                        }
                        if (!checkPossibleTypeIncompatibility(
                                targetMethod.getDeclaredType().toString(),
                                declaredType)
                        ) {
                            problemsHolder.registerProblem(
                                    pluginMethodParameter,
                                    inspectionBundle.message(
                                            "inspection.plugin.error.typeIncompatibility"
                                    ),
                                    ProblemHighlightType.WEAK_WARNING
                            );
                        }
                        continue;
                    }
                    if (index == 2 && pluginPrefix.equals(Plugin.PluginType.after.toString())
                            && targetMethod.getDeclaredType().toString().equals("void")) {
                        if (declaredType.isEmpty()) {
                            continue;
                        }
                        if (!declaredType.equals(MagentoPhpClass.PHP_NULL)) {
                            problemsHolder.registerProblem(
                                    pluginMethodParameter,
                                    PhpBundle.message(
                                            WRONG_PARAM_TYPE,
                                            new Object[]{declaredType, "null"}
                                            ),
                                    ProblemHighlightType.ERROR);
                        }
                        continue;
                    }
                    final int targetParameterKey = index
                            - (pluginPrefix.equals(Plugin.PluginType.before.toString())
                                    ? beforePluginExtraParamsStart
                                    : afterAndAroundPluginExtraParamsStart);
                    if (targetMethodParameters.length <= targetParameterKey) {
                        problemsHolder.registerProblem(
                                pluginMethodParameter,
                                inspectionBundle.message(
                                        "inspection.plugin.error.redundantParameter"
                                ),
                                ProblemHighlightType.ERROR
                        );
                        continue;
                    }
                    final Parameter targetMethodParameter =
                            targetMethodParameters[targetParameterKey];
                    final String targetMethodParameterDeclaredType =
                            targetMethodParameter.getDeclaredType().toString();

                    if (!checkTypeIncompatibility(targetMethodParameterDeclaredType, declaredType)
                            && !pluginMethodParameter.getText().contains("...$")
                    ) {
                        problemsHolder.registerProblem(
                                pluginMethodParameter,
                                PhpBundle.message(
                                        WRONG_PARAM_TYPE,
                                        new Object[]{declaredType,
                                                targetMethodParameterDeclaredType}
                                            ),
                                ProblemHighlightType.ERROR);
                    }
                    if (!checkPossibleTypeIncompatibility(
                            targetMethodParameterDeclaredType,
                            declaredType
                    )) {
                        problemsHolder.registerProblem(
                                pluginMethodParameter,
                                inspectionBundle.message(
                                        "inspection.plugin.error.typeIncompatibility"
                                ),
                                ProblemHighlightType.WEAK_WARNING
                        );
                    }
                }
            }

            private String getTargetMethodName(
                    final Method pluginMethod,
                    final String pluginPrefix
            ) {
                final String pluginMethodName = pluginMethod.getName();
                final String targetClassMethodName = pluginMethodName
                        .replace(pluginPrefix, "");
                if (targetClassMethodName.isEmpty()) {
                    return null;
                }
                final char firstCharOfTargetName = targetClassMethodName.charAt(0);
                final int charType = Character.getType(firstCharOfTargetName);
                if (charType == Character.LOWERCASE_LETTER) {
                    return null;
                }
                return Character.toLowerCase(firstCharOfTargetName)
                        + targetClassMethodName.substring(1);
            }

            private int getFinalClassProblems(final ProblemDescriptor[] currentResults) {
                int finalClassProblems = 0;
                for (final ProblemDescriptor currentProblem : currentResults) {
                    if (currentProblem.getDescriptionTemplate().equals(
                            inspectionBundle.message("inspection.plugin.error.finalClass"))) {
                        finalClassProblems++;
                    }
                }
                return finalClassProblems;
            }


            private boolean checkTypeIncompatibility(
                    final String targetType,
                    final String declaredType
            ) {
                if (targetType.isEmpty() || declaredType.isEmpty()) {
                    return true;
                }

                if (declaredType.equals(targetType)) {
                    return true;
                }

                final boolean isDeclaredTypeClass = PhpLangUtil.isFqn(declaredType);
                final boolean isTargetTypeClass = PhpLangUtil.isFqn(targetType);
                if (!isTargetTypeClass && isDeclaredTypeClass) {
                    return false;
                }

                final GetPhpClassByFQN getPhpClassByFQN =
                        GetPhpClassByFQN.getInstance(problemsHolder.getProject());
                final PhpClass targetClass = getPhpClassByFQN.execute(targetType);
                final PhpClass declaredClass = getPhpClassByFQN.execute(declaredType);
                if (targetClass == null || declaredClass == null) {
                    return false;
                }

                if (declaredClass.isInterface() && PhpClassImplementsInterfaceUtil.execute(
                        declaredClass,
                        targetClass
                )) {
                    return true;
                }

                if (PhpClassHierarchyUtils.classesEqual(targetClass, declaredClass)) {
                    return true;
                }

                if (PhpClassHierarchyUtils.isSuperClass(targetClass, declaredClass, false)) {
                    return true;
                }

                if (targetClass.isInterface() && PhpClassImplementsInterfaceUtil
                        .execute(targetClass, declaredClass)) {
                    return true;
                }

                if (PhpClassHierarchyUtils.isSuperClass(declaredClass, targetClass, false)) {
                    return true;
                }

                return false;
            }

            private boolean checkPossibleTypeIncompatibility(
                    final String targetType,
                    final String declaredType
            ) {
                if (targetType.isEmpty() || declaredType.isEmpty()) {
                    return true;
                }

                final GetPhpClassByFQN getPhpClassByFQN = GetPhpClassByFQN.getInstance(
                        problemsHolder.getProject()
                );
                final PhpClass targetClass = getPhpClassByFQN.execute(targetType);
                final PhpClass declaredClass = getPhpClassByFQN.execute(declaredType);
                if (targetClass == null || declaredClass == null) {
                    return true;
                }
                if (PhpClassHierarchyUtils.isSuperClass(declaredClass, targetClass, false)) {
                    return false;
                }
                if (targetClass.isInterface() && PhpClassImplementsInterfaceUtil.execute(
                        targetClass,
                        declaredClass
                )) {
                    return false;
                }

                return true;
            }
        };
    }
}
