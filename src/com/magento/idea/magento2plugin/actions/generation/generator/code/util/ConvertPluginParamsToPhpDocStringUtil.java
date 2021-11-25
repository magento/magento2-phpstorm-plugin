/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.code.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.documentation.phpdoc.PhpDocUtil;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.magento.idea.magento2plugin.actions.generation.generator.util.PhpClassGeneratorUtil;
import com.magento.idea.magento2plugin.magento.files.Plugin;
import com.magento.idea.magento2plugin.magento.packages.MagentoPhpClass;
import com.magento.idea.magento2plugin.util.php.PhpTypeMetadataParserUtil;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ConvertPluginParamsToPhpDocStringUtil {

    private ConvertPluginParamsToPhpDocStringUtil() {}

    /**
     * Converts parameters to PHP Doc.
     *
     * @param parameters Collection
     * @param type PluginType
     * @param project Project
     * @param myMethod Method
     * @return String
     */
    @SuppressWarnings({"PMD.NPathComplexity", "PMD.CyclomaticComplexity"})
    public static String execute(
            final Collection<PsiElement> parameters,
            final @NotNull Plugin.PluginType type,
            final Project project,
            final Method myMethod
    ) {
        final StringBuilder stringBuilder = new StringBuilder(155);
        String returnType;

        if (type.equals(Plugin.PluginType.before)) {
            returnType = MagentoPhpClass.ARRAY_TYPE;
        } else {
            returnType = PhpTypeMetadataParserUtil.getMethodReturnType(myMethod);

            if (returnType != null && PhpClassGeneratorUtil.isValidFqn(returnType)) {
                returnType = PhpClassGeneratorUtil.getNameFromFqn(returnType);
            }
        }

        final PhpType returnPhpType = new PhpType().add(returnType);
        int increment = 0;

        for (final PsiElement parameter : parameters) {
            final PhpNamedElement element = (PhpNamedElement) parameter;

            if (stringBuilder.length() > 0) {
                stringBuilder.append('\n');
            }
            stringBuilder.append("* @param ");
            String typeStr;

            if (increment == 0) {
                typeStr = getStringTypePresentation(project, element.getType(), null);
            } else {
                typeStr = getStringTypePresentation(
                        project,
                        element.getType(),
                        PhpCodeInsightUtil.findScopeForUseOperator(element)
                );
            }

            if (!typeStr.isEmpty()) {
                stringBuilder.append(typeStr).append(' ');
            }
            String paramName = element.getName();

            if (increment == 0) {
                paramName = "subject";
            }
            stringBuilder.append('$').append(paramName);

            if (type.equals(Plugin.PluginType.around) && increment == 0) {
                stringBuilder.append("\n* @param callable $proceed");
            }

            if (type.equals(Plugin.PluginType.after) && increment == 0) {
                if (returnType != null) {
                    if (returnType.equals(MagentoPhpClass.VOID_RETURN_TYPE)) {
                        stringBuilder.append("\n* @param null $result");
                    } else {
                        final String returnTypeStr = getStringTypePresentation(
                                project,
                                returnPhpType,
                                null
                        );
                        stringBuilder.append("\n* @param ")
                                .append(returnTypeStr)
                                .append(" $result");
                    }
                    increment++;
                    continue;
                }
                stringBuilder.append("\n* @param $result");
            }

            increment++;
        }

        if (returnType != null) {
            if (returnType.equals(MagentoPhpClass.VOID_RETURN_TYPE)) {
                stringBuilder.append("\n* @return ").append(MagentoPhpClass.VOID_RETURN_TYPE);
            } else {
                final String returnTypeStr = getStringTypePresentation(
                        project,
                        returnPhpType,
                        null
                );
                stringBuilder.append("\n* @return ").append(returnTypeStr);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Get string type representation in the DOC format.
     *
     * @param project Project
     * @param type PhpType
     *
     * @return String
     */
    private static String getStringTypePresentation(
            final @NotNull Project project,
            final @NotNull PhpType type,
            final @Nullable PhpPsiElement scope
    ) {
        String typeStr = PhpDocUtil.getTypePresentation(project, type, scope);

        if (PhpLangUtil.isFqn(typeStr)) {
            typeStr = PhpClassGeneratorUtil.getNameFromFqn(typeStr);
        }

        return typeStr;
    }
}
