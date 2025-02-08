/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.php;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.documentation.phpdoc.PhpDocUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.inspections.phpdoc.PhpDocCommentGenerator;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("PMD.GodClass")
public final class PhpTypeMetadataParserUtil {

    private static final String PHP_IMPORTS_REGEX =
            "use ((?:\\\\{1,2}\\w+|\\w+\\\\{0,2})(?:\\w+\\\\{0,2})+)";

    private PhpTypeMetadataParserUtil() {}

    /**
     * Get class/interface name.
     *
     * @param phpClass PhpClass
     *
     * @return String
     */
    public static String getName(final @NotNull PhpClass phpClass) {
        return phpClass.getName();
    }

    /**
     * Get class/interface FQN.
     *
     * @param phpClass PhpClass
     *
     * @return String
     */
    public static String getFqn(final @NotNull PhpClass phpClass) {
        return phpClass.getPresentableFQN();
    }

    /**
     * Get short description for the php class/interface.
     *
     * @param phpClass PhpClass
     *
     * @return String
     */
    public static String getShortDescription(final @NotNull PhpClass phpClass) {
        return getShortDescriptionForPhpNamedElement(phpClass);
    }

    /**
     * Get short description for the php class/interface method.
     *
     * @param method Method
     *
     * @return String
     */
    public static String getShortDescription(final @NotNull Method method) {
        return getShortDescriptionForPhpNamedElement(method);
    }

    /**
     * Get public methods for class|interface.
     *
     * @param phpClass PhpClass
     *
     * @return List[Method]
     */
    public static List<Method> getPublicMethods(final @NotNull PhpClass phpClass) {
        final List<Method> methods = new LinkedList<>();

        for (final Method method : phpClass.getMethods()) {
            if (!method.getAccess().isPublic() || method.getName().contains("__")) {
                continue;
            }
            methods.add(method);
        }
        methods.sort(Comparator.comparingInt(PsiElement::getTextOffset));

        return methods;
    }

    /**
     * Get methods by names.
     *
     * @param phpClass PhpClass
     * @param names String
     *
     * @return List[Method]
     */
    public static List<Method> getMethodsByNames(
            final @NotNull PhpClass phpClass,
            final @NotNull String... names
    ) {
        final List<Method> methods = new LinkedList<>();
        final List<String> methodNames = Arrays.asList(names);

        for (final Method method : phpClass.getMethods()) {
            if (methodNames.contains(method.getName())) {
                methods.add(method);
            }
        }

        return methods;
    }

    /**
     * Get method declaration for interface.
     *
     * @param method Method
     * @param defaultMethodDescription String
     *
     * @return String
     */
    @SuppressWarnings({
            "PMD.CyclomaticComplexity",
            "PMD.CognitiveComplexity",
            "PMD.NPathComplexity",
            "PMD.ConfusingTernary"
    })
    public static String getMethodDefinitionForInterface(
            final @NotNull Method method,
            final String defaultMethodDescription
    ) {
        String methodText = method.getText();
        final int bodyStartIndex = methodText.indexOf('{');
        final PhpDocComment methodDoc = method.getDocComment();
        String methodDescription = "";
        String methodDocReturn = null;

        if (bodyStartIndex != -1) {
            methodText = methodText.substring(0, bodyStartIndex).trim();
        }

        if (methodDoc != null) {
            methodDescription = getShortDescription(method);
            methodDocReturn = methodDoc.getReturnTag() == null
                    ? null
                    : methodDoc.getReturnTag().getText();
        }

        if (methodDescription.isEmpty() && defaultMethodDescription != null) {
            methodDescription = defaultMethodDescription;
        }

        String methodDocAnnotation;
        final PhpDocComment newMethodDoc =
                PhpDocCommentGenerator.constructDocComment(method.getProject(), method, false);
        methodDocAnnotation = newMethodDoc == null ? "" : newMethodDoc.getText();

        if (newMethodDoc != null && !methodDocAnnotation.isEmpty()
                && methodDocReturn != null
                && newMethodDoc.getReturnTag() == null) {
            int insertionPos = -1;
            final int docEndPos = methodDocAnnotation.indexOf("*/");
            final int lastThrowPos = methodDocAnnotation.lastIndexOf("@throws");

            if (lastThrowPos != -1) {
                insertionPos = lastThrowPos;
            } else if (docEndPos != -1) {
                insertionPos = docEndPos;
            }

            if (insertionPos != -1) {
                methodDocAnnotation = new StringBuffer(methodDocAnnotation)
                        .insert(
                                insertionPos,
                                docEndPos == insertionPos
                                        ? "\n".concat(methodDocReturn).concat("\n")
                                        : methodDocReturn.concat("\n")
                        ).toString();
            }
        }

        if (!methodDocAnnotation.isEmpty()) {
            final int openingSymbolPosition = methodDocAnnotation.indexOf("/**");

            if (!methodDescription.isEmpty() && openingSymbolPosition != -1) {
                methodDocAnnotation = new StringBuffer(methodDocAnnotation)
                        .insert(
                                openingSymbolPosition + 3,
                                "\n".concat("* ").concat(methodDescription)
                        ).toString();
            }

            for (final String type : getMethodDefinitionPhpTypes(method)) {
                final String typeName = PhpClassGeneratorUtil.getNameFromFqn(type);
                final List<String> exceptionTypes = getMethodExceptionsTypes(method);

                if (!exceptionTypes.contains(type)
                        && methodDocAnnotation.contains(typeName)
                        && !methodDocAnnotation.contains(type)) {
                    methodDocAnnotation = methodDocAnnotation.replaceAll(
                            typeName,
                            "\\\\" + type.replace("\\", "\\\\")
                    );
                }
            }
            methodText = methodDocAnnotation.concat("\n").concat(methodText);
        }

        return methodText;
    }

    /**
     * Get PHP types that accessible in the method parameters or DOCs throws part.
     *
     * @param method Method
     *
     * @return List[String]
     */
    public static List<String> getMethodDefinitionPhpTypes(final @NotNull Method method) {
        final List<String> types = new ArrayList<>(getMethodParametersTypes(method));

        final String returnType = getMethodReturnType(method);

        if (returnType != null && PhpClassGeneratorUtil.isValidFqn(returnType)
                && !PhpLangUtil.isReturnTypeHint(returnType, method.getProject())) {
            types.add(StringUtils.stripStart(returnType, "\\"));
        }
        types.addAll(getMethodExceptionsTypes(method));

        return types;
    }

    /**
     * Get method parameters types.
     *
     * @param method Method
     *
     * @return List[String]
     */
    public static List<String> getMethodParametersTypes(final @NotNull Method method) {
        final List<String> types = new ArrayList<>();

        for (final Parameter parameter : method.getParameters()) {
            final List<String> typesInTypeResult =
                    extractMultipleTypesFromString(parameter.getDeclaredType().toString());

            for (final String phpType : typesInTypeResult) {
                if (PhpLangUtil.isFqn(phpType)) {
                    final String type = StringUtils.stripStart(phpType, "\\");

                    if (!types.contains(type)) {
                        types.add(type);
                    }
                }
            }
        }

        return types;
    }

    /**
     * Get main type for the specified parameter (?int -> main type is int).
     *
     * @param parameter Parameter
     *
     * @return String
     */
    public static String getMainType(final @NotNull Parameter parameter) {
        final List<String> types = extractMultipleTypesFromString(
                parameter.getDeclaredType().toString()
        );

        for (final String type : types) {
            if (PhpLangUtil.isFqn(type)) {
                return type;
            } else if (PhpLangUtil.isParameterTypeHint(type, parameter.getProject())) {
                return type;
            }
        }

        return null;
    }

    /**
     * Get method return type.
     *
     * @param method Method
     *
     * @return String
     */
    public static String getMethodReturnType(final @NotNull Method method) {
        if (method.getContainingClass() == null) {
            return null;
        }
        final List<String> returnedTypes =
                extractMultipleTypesFromString(method.getType().toString());

        for (final String returnedType : returnedTypes) {
            if (PhpLangUtil.isReturnTypeHint(returnedType, method.getProject())) {
                return returnedType;
            }

            if (PhpLangUtil.isFqn(returnedType)) {
                return returnedType;
            }
            final String guessedByName =
                    getUsedInPhpClassTypeByName(returnedType, method.getContainingClass());

            if (guessedByName != null) {
                return guessedByName;
            }
        }

        return null;
    }

    /**
     * Get method return type.
     *
     * @param method Method
     *
     * @return List[String]
     */
    @SuppressWarnings("PMD.CognitiveComplexity")
    public static List<String> getMethodExceptionsTypes(final @NotNull Method method) {
        final List<String> types = new ArrayList<>();

        final PhpDocComment methodComment;

        if (method.getDocComment() == null) {
            methodComment = PhpDocCommentGenerator.constructDocComment(
                    method.getProject(),
                    method,
                    false
            );
        } else {
            methodComment = method.getDocComment();
        }

        if (methodComment != null) {
            for (final PhpType exceptionType : methodComment.getExceptionClasses()) {
                final List<String> typesInTypeResult =
                        extractMultipleTypesFromString(exceptionType.toString());

                for (final String phpType : typesInTypeResult) {
                    String type = phpType;

                    if (!PhpLangUtil.isFqn(type) && method.getContainingClass() != null) {
                        final String exceptionFqn = getUsedInPhpClassTypeByName(
                                type,
                                method.getContainingClass()
                        );

                        if (exceptionFqn != null) {
                            type = exceptionFqn;
                        }
                    }
                    type = StringUtils.stripStart(type, "\\");

                    if (!types.contains(type)) {
                        types.add(type);
                    }
                }
            }
        }

        return types;
    }

    /**
     * Get used in php class type (class|interface) by its name.
     *
     * @param name String
     * @param phpClass PhpClass
     *
     * @return String
     */
    private static String getUsedInPhpClassTypeByName(
            final @NotNull String name,
            final @NotNull PhpClass phpClass
    ) {
        final PhpIndex phpIndex = PhpIndex.getInstance(phpClass.getProject());
        final Collection<PhpClass> phpClasses = phpIndex.getClassesByName(name);

        if (!phpClasses.isEmpty()) {
            final List<String> typeVariants = phpClasses
                    .stream()
                    .map(PhpClass::getPresentableFQN)
                    .collect(Collectors.toList());

            return chooseTypeUsedInPhpClass(phpClass, typeVariants);
        }

        final Collection<PhpClass> interfacesByName = phpIndex.getInterfacesByName(name);

        if (!interfacesByName.isEmpty()) {
            final List<String> typeVariants = interfacesByName
                    .stream()
                    .map(PhpClass::getPresentableFQN)
                    .collect(Collectors.toList());

            return chooseTypeUsedInPhpClass(phpClass, typeVariants);
        }

        return null;
    }

    /**
     * Detect import used in the class from variants.
     *
     * @param phpClass PhpClass
     * @param importVariants List[String]
     *
     * @return String
     */
    private static String chooseTypeUsedInPhpClass(
            final @NotNull PhpClass phpClass,
            final @NotNull List<String> importVariants
    ) {
        final List<String> imports = getPhpClassImports(phpClass);

        for (final String importVariant : importVariants) {
            if (imports.contains(importVariant)) {
                return importVariant;
            }
        }

        return null;
    }

    /**
     * Get php class|interface imports.
     *
     * @param phpClass PhpClass
     *
     * @return List[String]
     */
    private static List<String> getPhpClassImports(final @NotNull PhpClass phpClass) {
        final List<String> imports = new ArrayList<>();

        final String fileText = phpClass.getContainingFile().getText();
        final Pattern searchImportsPattern = Pattern.compile(PHP_IMPORTS_REGEX);
        final Matcher importsMatcher = searchImportsPattern.matcher(fileText);

        while (importsMatcher.find() && importsMatcher.groupCount() >= 1) {
            imports.add(importsMatcher.group(1));
        }

        return imports;
    }

    /**
     * Extract multiple types from string.
     *
     * @param typeString String
     *
     * @return List[String]
     */
    private static List<String> extractMultipleTypesFromString(final @NotNull String typeString) {
        return Arrays.asList(typeString.split("\\|"));
    }

    /**
     * Get short description for the php named element.
     *
     * @param phpNamedElement PhpNamedElement
     *
     * @return String
     */
    private static String getShortDescriptionForPhpNamedElement(
            final @NotNull PhpNamedElement phpNamedElement
    ) {
        final String description = PhpDocUtil.getDescription(phpNamedElement.getDocComment());

        return StringUtil.stripHtml(description, true);
    }
}
