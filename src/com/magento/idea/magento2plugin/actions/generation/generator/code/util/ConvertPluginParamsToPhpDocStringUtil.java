/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.code.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.documentation.phpdoc.PhpDocUtil;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.jetbrains.php.lang.psi.elements.PhpReturnType;
import com.magento.idea.magento2plugin.magento.files.Plugin;
import com.magento.idea.magento2plugin.magento.packages.MagentoPhpClass;
import com.magento.idea.magento2plugin.magento.packages.Package;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

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
        final StringBuilder stringBuilder = new StringBuilder();//NOPMD
        final PhpReturnType returnType = myMethod.getReturnType();

        int increment = 0;
        for (final PsiElement parameter : parameters) {
            final PhpNamedElement element = (PhpNamedElement) parameter;
            if (stringBuilder.length() > 0) {
                stringBuilder.append('\n');
            }

            stringBuilder.append("* @param ");

            String typeStr;
            if (increment == 0) {
                typeStr = PhpDocUtil.getTypePresentation(project, element.getType(), null);
            } else {
                typeStr = PhpDocUtil.getTypePresentation(
                    project, element.getType(),
                    PhpCodeInsightUtil.findScopeForUseOperator(element)
                );
                if (typeStr.indexOf(Package.fqnSeparator, 1) > 0) {
                    final String[] fqnArray = typeStr.split("\\\\");
                    typeStr = fqnArray[fqnArray.length - 1];
                }
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
                    if (returnType.getText().equals(MagentoPhpClass.VOID_RETURN_TYPE)) {
                        stringBuilder.append("\n* @param null $result");
                    } else {
                        stringBuilder.append("\n* @param ").append(returnType.getText())
                                .append(" $result");
                    }
                    continue;
                }

                stringBuilder.append("\n* @param $result");
            }

            increment++;
        }

        if (!type.equals(Plugin.PluginType.before) && returnType != null) {
            if (returnType.getText().equals(MagentoPhpClass.VOID_RETURN_TYPE)) {
                stringBuilder.append("\n* @return ").append(MagentoPhpClass.VOID_RETURN_TYPE);
            } else {
                stringBuilder.append("\n* @return ").append(returnType.getText());
            }
        }
        return stringBuilder.toString();
    }
}
