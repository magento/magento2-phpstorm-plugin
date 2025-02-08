/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.code.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.util.text.Strings;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.config.PhpLanguageFeature;
import com.jetbrains.php.lang.PhpCodeUtil;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.documentation.phpdoc.PhpDocUtil;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.magento.files.Plugin;
import com.magento.idea.magento2plugin.magento.packages.MagentoPhpClass;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2plugin.util.php.PhpTypeMetadataParserUtil;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ConvertPluginParamsToString {

    private ConvertPluginParamsToString() {}

    /**
     * Converts parameters to string.
     *
     * @param parameters Collection
     * @param type       PluginType
     * @param myMethod   Method
     *
     * @return String
     */
    @SuppressWarnings({
            "PMD.NPathComplexity",
            "PMD.CyclomaticComplexity",
            "PMD.CognitiveComplexity",
            "PMD.ConfusingTernary"
    })
    public static String execute(
            final Collection<PsiElement> parameters,
            final @NotNull Plugin.PluginType type,
            final Method myMethod
    ) {
        final StringBuilder buf = new StringBuilder(155);
        String returnType = PhpTypeMetadataParserUtil.getMethodReturnType(myMethod);

        if (returnType != null && PhpClassGeneratorUtil.isValidFqn(returnType)) {
            if (Strings.endsWith(returnType, "[]")) {
                returnType = "array";
            } else {
                returnType = PhpClassGeneratorUtil.getNameFromFqn(returnType);
            }
        }
        final Iterator<PsiElement> parametersIterator = parameters.iterator();
        int iterator = 0;

        while (parametersIterator.hasNext()) {
            final PhpNamedElement element = (PhpNamedElement) parametersIterator.next();

            if (iterator != 0) {
                buf.append(',');
            }
            if (element instanceof Parameter) {
                String parameterText = PhpCodeUtil.paramToString(element);

                // Parameter has default value.
                if (parameterText.contains("=")) {
                    final String[] paramParts = parameterText.split("=");
                    parameterText = paramParts[0];
                    parameterText += " = ";//NOPMD
                    String defaultValue = paramParts[1];

                    if (defaultValue.contains(Package.fqnSeparator)) {
                        final String[] fqnArray = defaultValue.split("\\\\");
                        defaultValue = fqnArray[fqnArray.length - 1];
                    }
                    parameterText += defaultValue;//NOPMD
                } else if (parameterText.contains(Package.fqnSeparator)) {
                    final String[] fqnArray = parameterText.split("\\\\");
                    parameterText = fqnArray[fqnArray.length - 1];
                }
                buf.append(parameterText);
            } else {
                final Boolean globalType = iterator != 0;
                String typeHint = getNonPrimitiveTypeForElement(
                        element,
                        globalType,
                        myMethod.getProject()
                );

                if (typeHint != null && !typeHint.isEmpty()) {
                    if (PhpLangUtil.isFqn(typeHint)) {
                        typeHint = PhpClassGeneratorUtil.getNameFromFqn(typeHint);
                    }
                    buf.append(typeHint).append(' ');
                }

                String paramName = element.getName();

                if (iterator == 0) {
                    paramName = "subject";
                }
                buf.append('$').append(paramName);
            }

            if (type.equals(Plugin.PluginType.after) && iterator == 0) {
                if (returnType != null
                        && !returnType.equals(MagentoPhpClass.VOID_RETURN_TYPE)) {
                    buf.append(", ").append(returnType).append(" $result");
                } else {
                    buf.append(", $result");
                }
            }
            if (type.equals(Plugin.PluginType.around) && iterator == 0) {
                buf.append(", callable $proceed");
            }
            iterator++;
        }

        return buf.toString();
    }

    /**
     * Get type hint for the specified element.
     *
     * @param element PhpNamedElement
     * @param globalType Boolean
     * @param project Project
     *
     * @return String
     */
    private static @Nullable String getNonPrimitiveTypeForElement(
            final @NotNull PhpNamedElement element,
            final Boolean globalType,
            final Project project
    ) {
        final PhpType filedType = element.getType().global(project);
        final Set<String> typeStrings = filterNullType(filedType).getTypes();
        String typeString = convertNonPrimitiveTypeToString(element, typeStrings, globalType);

        if (typeString != null
                && filedType.getTypes().size() != typeStrings.size()
                && PhpLanguageFeature.NULLABLES.isSupported(element.getProject())) {
            typeString = "?".concat(typeString);
        }

        return typeString;
    }

    /**
     * Convert non primitive PHP type to string for specified PhpNamedElement.
     *
     * @param element PhpNamedElement
     * @param typeStrings Set[String]
     * @param globalType Boolean
     *
     * @return String
     */
    private static @Nullable String convertNonPrimitiveTypeToString(
            final @NotNull PhpNamedElement element,
            final Set<String> typeStrings,
            final Boolean globalType
    ) {
        String simpleType = typeStrings.iterator().next();
        simpleType = StringUtil.trimStart(simpleType, "\\");

        if (!PhpType.isPrimitiveType(simpleType)
                || PhpLanguageFeature.SCALAR_TYPE_HINTS.isSupported(element.getProject())
                || MagentoPhpClass.ARRAY_TYPE.equalsIgnoreCase(simpleType)
                || Plugin.CALLABLE_PARAM.equalsIgnoreCase(simpleType)) {

            final String typeString = simpleType.endsWith("]")
                    ? MagentoPhpClass.ARRAY_TYPE
                    : convertPhpTypeToString(
                            element,
                            filterNullType(element.getType()),
                            globalType
                    );

            if (!typeString.isEmpty()) {
                return typeString;
            }
        }

        return null;
    }

    /**
     * Filter null type.
     *
     * @param filedType PhpType
     *
     * @return PhpType
     */
    private static PhpType filterNullType(final PhpType filedType) {
        if (filedType.getTypes().isEmpty()) {
            return PhpType.EMPTY;
        }
        final PhpType phpType = new PhpType();

        for (final String type : filedType.getTypes()) {
            if ("\\null".equalsIgnoreCase(type)) {
                continue;
            }
            phpType.add(type);
        }

        return phpType;
    }

    /**
     * Convert PHP type to string for specified PhpNamedElement.
     *
     * @param element PhpNamedElement
     * @param type PhpType
     * @param globalType Boolean
     *
     * @return String
     */
    private static String convertPhpTypeToString(
            final PhpNamedElement element,
            final @NotNull PhpType type,
            final Boolean globalType
    ) {
        final PhpPsiElement scope = globalType
                ? PhpCodeInsightUtil.findScopeForUseOperator(element)
                : null;

        return PhpDocUtil.getTypePresentation(element.getProject(), type, scope);
    }
}
