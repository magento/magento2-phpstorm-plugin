/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.generator.code;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.config.PhpLanguageFeature;
import com.jetbrains.php.lang.PhpCodeUtil;
import com.jetbrains.php.lang.documentation.phpdoc.PhpDocUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.magento.idea.magento2plugin.actions.generation.data.code.PluginMethodData;
import com.magento.idea.magento2plugin.magento.files.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.regex.Pattern;

public class PluginMethodsGenerator {
    public static String originalTargetKey = "original.target";
    @NotNull
    private final PhpClass pluginClass;
    @NotNull
    private final Method myMethod;
    @NotNull
    private final PhpClass myTargetClass;

    public PluginMethodsGenerator(@NotNull PhpClass pluginClass, @NotNull Method method, Key<Object> targetClassKey) {
        super();
        this.pluginClass = pluginClass;
        this.myMethod = method;
        this.myTargetClass = (PhpClass)Objects.requireNonNull(method.getUserData(targetClassKey));
    }

    @NotNull
    public PluginMethodData[] createPluginMethods(@NotNull Plugin.PluginType type) {
        List<PluginMethodData> pluginMethods = new ArrayList();
        String templateName = Plugin.getMethodTemplateByPluginType(type);
        PhpClass currentClass = this.pluginClass;
        if (currentClass != null) {
            Properties attributes = this.getAccessMethodAttributes(PhpCodeInsightUtil.findScopeForUseOperator(this.myMethod), type);
            String methodTemplate = PhpCodeUtil.getCodeTemplate(templateName, attributes, this.pluginClass.getProject());
            PhpClass dummyClass = PhpCodeUtil.createClassFromMethodTemplate(currentClass, currentClass.getProject(), methodTemplate);
            if (dummyClass != null) {
                PhpDocComment currDocComment = null;

                for(PsiElement child = dummyClass.getFirstChild(); child != null; child = child.getNextSibling()) {
                    if (child instanceof PhpDocComment) {
                        currDocComment = (PhpDocComment)child;
                    } else if (child instanceof Method) {
                        pluginMethods.add(new PluginMethodData(myMethod, currDocComment, (Method)child));
                        currDocComment = null;
                    }
                }
            }
        }

        return pluginMethods.toArray(new PluginMethodData[0]);
    }

    private Properties getAccessMethodAttributes(@Nullable PhpPsiElement scopeForUseOperator, @NotNull Plugin.PluginType type) {
        Properties attributes = new Properties();
        String typeHint = this.fillAttributes(scopeForUseOperator, attributes, type);
        this.addTypeHintsAndReturnType(attributes, typeHint);
        return attributes;
    }

    @NotNull
    private String fillAttributes(@Nullable PhpPsiElement scopeForUseOperator, Properties attributes, @NotNull Plugin.PluginType type) {
        String fieldName = this.myMethod.getName();
        String methodSuffix = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        String pluginMethodName = type.toString().concat(methodSuffix);

        attributes.setProperty("NAME", pluginMethodName);
        PhpReturnType targetMethodReturnType = myMethod.getReturnType();
        String typeHint = "";
        if (targetMethodReturnType != null) {
            typeHint = targetMethodReturnType.getText();
        }
        Collection<PsiElement> parameters = new ArrayList();
        parameters.add(myTargetClass);
        parameters.addAll(Arrays.asList(myMethod.getParameters()));
        attributes.setProperty("PARAM_DOC", this.getParameterDoc(parameters, type, scopeForUseOperator.getProject()));
        attributes.setProperty("PARAM_LIST", this.getParameterList(parameters, type));
        String returnVariables = this.getReturnVariables(type);
        if (returnVariables != null) {
            attributes.setProperty("RETURN_VARIABLES", returnVariables);
        }

        return typeHint;
    }

    private void addTypeHintsAndReturnType(Properties attributes, String typeHint) {
        Project project = this.pluginClass.getProject();
        if (PhpLanguageFeature.SCALAR_TYPE_HINTS.isSupported(project) && isDocTypeConvertable(typeHint)) {
            attributes.setProperty("SCALAR_TYPE_HINT", convertDocTypeToHint(project, typeHint));
        }

        boolean hasFeatureVoid = PhpLanguageFeature.RETURN_VOID.isSupported(project);
        if (hasFeatureVoid) {
            attributes.setProperty("VOID_RETURN_TYPE", "void");
        }

        if (PhpLanguageFeature.RETURN_TYPES.isSupported(project) && isDocTypeConvertable(typeHint)) {
            attributes.setProperty("RETURN_TYPE", convertDocTypeToHint(project, typeHint));
        }

    }

    private static boolean isDocTypeConvertable(String typeHint) {
        return !typeHint.equalsIgnoreCase("mixed") && !typeHint.equalsIgnoreCase("static") && !typeHint.equalsIgnoreCase("true") && !typeHint.equalsIgnoreCase("false") && !typeHint.equalsIgnoreCase("null") && (!typeHint.contains("|") || typeWithNull(typeHint));
    }

    private static boolean typeWithNull(String typeHint) {
        return typeHint.split(Pattern.quote("|")).length == 2 && StringUtil.toUpperCase(typeHint).contains("NULL");
    }

    private static String convertNullableType(Project project, String typeHint) {
        String[] split = typeHint.split(Pattern.quote("|"));
        boolean hasNullableTypeFeature = PhpLanguageFeature.NULLABLES.isSupported(project);
        return split[0].equalsIgnoreCase("null") ? (hasNullableTypeFeature ? "?" : "") + split[1] : (hasNullableTypeFeature ? "?" : "") + split[0];
    }

    @NotNull
    private static String convertDocTypeToHint(Project project, String typeHint) {
        String hint = typeHint.contains("[]") ? "array" : typeHint;
        hint = hint.contains("boolean") ? "bool" : hint;
        if (typeWithNull(typeHint)) {
            hint = convertNullableType(project, hint);
        }

        return hint;
    }

    private String getParameterDoc(Collection<PsiElement> parameters, @NotNull Plugin.PluginType type, Project project) {
        StringBuilder sb = new StringBuilder();
        PhpReturnType returnType = myMethod.getReturnType();

        PhpNamedElement element;
        int i = 0;
        for(Iterator iterator = parameters.iterator(); iterator.hasNext(); i++) {
            element = (PhpNamedElement)iterator.next();

            if (sb.length() > 0) {
                sb.append("\n");
            }

            sb.append("* @param ");
            String typeStr = PhpDocUtil.getTypePresentation(project, element.getType(), PhpCodeInsightUtil.findScopeForUseOperator(element));

            if (!typeStr.isEmpty()) {
                sb.append(typeStr).append(' ');
            }
            String paramName = element.getName();
            if (i == 0) {
                paramName = "subject";
            }
            sb.append('$').append(paramName);

            if (type.equals(Plugin.PluginType.around) && i == 0) {
                sb.append("\n* @param callable $proceed");
            }
            if (type.equals(Plugin.PluginType.after) && i == 0) {
                if (returnType != null) {
                    if (returnType.getText().equals("void")) {
                        sb.append("\n* @param null $result");
                    } else {
                        sb.append("\n* @param ").append(returnType.getText()).append(" $result");
                    }
                } else {
                    sb.append("\n* @param $result");
                }
            }
        }

        if (!type.equals(Plugin.PluginType.before) && returnType != null) {
            if (returnType.getText().equals("void")) {
                sb.append("\n* @return void");
            } else {
                sb.append("\n* @return ").append(returnType.getText());
            }
        }
        return sb.toString();
    }

    protected String getParameterList(Collection<PsiElement> parameters, @NotNull Plugin.PluginType type) {
        StringBuilder buf = new StringBuilder();
        PhpReturnType returnType = myMethod.getReturnType();
        Iterator iterator = parameters.iterator();

        Integer i = 0;
        while(iterator.hasNext()) {
            PhpNamedElement element = (PhpNamedElement)iterator.next();
            if (i != 0) {
                buf.append(',');
            }

            if (element instanceof Parameter) {
                buf.append(PhpCodeUtil.paramToString(element));
            } else {
                String typeHint = this.getTypeHint(element);
                if (typeHint != null && !typeHint.isEmpty()) {
                    buf.append(typeHint).append(' ');
                }

                String paramName = element.getName();
                if (i == 0) {
                    paramName = "subject";
                }
                buf.append("$").append(paramName);
            }
            if (type.equals(Plugin.PluginType.after) && i == 0){
                if (returnType != null && !returnType.getText().equals("void")) {
                    buf.append(", ").append(returnType.getText()).append(" $result");
                } else {
                    buf.append(", $result");
                }
            }
            if (type.equals(Plugin.PluginType.around) && i == 0){
                buf.append(", callable $proceed");
            }
            i++;
        }

        return buf.toString();
    }

    protected String getReturnVariables(@NotNull Plugin.PluginType type) {
        StringBuilder buf = new StringBuilder();
        if (type.equals(Plugin.PluginType.after)) {
            return buf.append("$result").toString();
        }
        Parameter[] parameters = myMethod.getParameters();
        if (parameters.length == 0) {
            return null;
        }
        boolean isFirst = true;

        for (Parameter parameter: parameters) {
            if (isFirst) {
                isFirst = false;
            } else {
                buf.append(", ");
            }

            String paramName = parameter.getName();
            buf.append("$").append(paramName);
        }

        return buf.toString();
    }

    @Nullable
    private String getTypeHint(@NotNull PhpNamedElement element) {
        PhpType filedType = element.getType().global(this.pluginClass.getProject());
        Set<String> typeStrings = filedType.getTypes();
        String typeString = null;
        if (typeStrings.size() == 1) {
            typeString = this.convertTypeToString(element, typeStrings);
        }

        if (typeStrings.size() == 2) {
            PhpType filteredNullType = filterNullCaseInsensitive(filedType);
            if (filteredNullType.getTypes().size() == 1) {
                if (PhpLanguageFeature.NULLABLES.isSupported(element.getProject())) {
                    typeString = "?" + this.convertTypeToString(element, filteredNullType.getTypes());
                } else {
                    typeString = this.convertTypeToString(element, filteredNullType.getTypes());
                }
            }
        }

        return typeString;
    }

    @Nullable
    private String convertTypeToString(@NotNull PhpNamedElement element, Set<String> typeStrings) {
        String simpleType = typeStrings.iterator().next();
        simpleType = StringUtil.trimStart(simpleType, "\\");
        if (!PhpType.isPrimitiveType(simpleType) || PhpLanguageFeature.SCALAR_TYPE_HINTS.isSupported(element.getProject()) || "array".equalsIgnoreCase(simpleType) || "callable".equalsIgnoreCase(simpleType)) {
            String typeString = simpleType.endsWith("]") ? "array" : this.getFieldTypeString(element, filterNullCaseInsensitive(element.getType()));
            if (!typeString.isEmpty()) {
                return typeString;
            }
        }

        return null;
    }

    private String getFieldTypeString(PhpNamedElement element, @NotNull PhpType type) {
        return PhpDocUtil.getTypePresentation(this.pluginClass.getProject(), type, PhpCodeInsightUtil.findScopeForUseOperator(element));
    }

    private static PhpType filterNullCaseInsensitive(PhpType filedType) {
        if (filedType.getTypes().isEmpty()) {
            return PhpType.EMPTY;
        } else {
            PhpType phpType = new PhpType();
            Iterator iterator = filedType.getTypes().iterator();

            while(iterator.hasNext()) {
                String type = (String)iterator.next();
                if (!type.equalsIgnoreCase("\\null")) {
                    phpType.add(type);
                }
            }

            return phpType;
        }
    }
}
