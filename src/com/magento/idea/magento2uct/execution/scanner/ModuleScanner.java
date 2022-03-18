/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.execution.scanner;

import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonValue;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.ClassConstantReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.elements.impl.ClassConstImpl;
import com.magento.idea.magento2plugin.magento.files.ComposerJson;
import com.magento.idea.magento2plugin.magento.files.RegistrationPhp;
import com.magento.idea.magento2plugin.magento.packages.ComponentType;
import com.magento.idea.magento2uct.execution.scanner.data.ComponentData;
import com.magento.idea.magento2uct.execution.scanner.filter.ModuleScannerFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public final class ModuleScanner implements Iterable<ComponentData> {

    private final List<PsiDirectory> rootDirectories;
    private final List<ComponentData> componentDataList;
    private final List<ModuleScannerFilter> filters;
    private int modulesQty;
    private int themesQty;

    /**
     * Magento 2 module components scanner constructor.
     *
     * @param rootDirectory PsiDirectory
     * @param filters ModuleScannerFilter[]
     */
    public ModuleScanner(
            final @NotNull PsiDirectory rootDirectory,
            final @NotNull ModuleScannerFilter... filters

    ) {
        this(List.of(rootDirectory), filters);
    }

    /**
     * Magento 2 module components scanner constructor.
     *
     * @param directories List[PsiDirectory]
     * @param filters ModuleScannerFilter[]
     */
    public ModuleScanner(
            final List<PsiDirectory> directories,
            final @NotNull ModuleScannerFilter... filters

    ) {
        this.rootDirectories = new ArrayList<>(directories);
        this.filters = Arrays.asList(filters);
        componentDataList = new ArrayList<>();
        modulesQty = 0;
        themesQty = 0;
    }

    @Override
    public @NotNull Iterator<ComponentData> iterator() {
        return run().iterator();
    }

    /**
     * Get found modules qty.
     *
     * @return int
     */
    public int getModuleCount() {
        return modulesQty;
    }

    /**
     * Get found themes qty.
     *
     * @return int
     */
    public int getThemeCount() {
        return themesQty;
    }

    /**
     * Run scanner.
     *
     * @return List[ComponentData]
     */
    private List<ComponentData> run() {
        componentDataList.clear();

        for (final PsiDirectory rootDirectory : rootDirectories) {
            findModuleComponent(rootDirectory);
        }

        return componentDataList;
    }

    /**
     * Look up magento 2 module components.
     *
     * @param directory PsiDirectory
     */
    @SuppressWarnings({
            "PMD.NPathComplexity",
            "PMD.CyclomaticComplexity",
            "PMD.CognitiveComplexity",
            "PMD.AvoidDeeplyNestedIfStmts"
    })
    private void findModuleComponent(final @NotNull PsiDirectory directory) {
        String name = null;
        String composerBasedName = null;
        ComponentType type = null;

        final PsiFile registration = directory.findFile(RegistrationPhp.FILE_NAME);

        if (registration instanceof PhpFile) {
            final Pair<String, ComponentType> registrationMeta = scanRegistrationMeta(
                    (PhpFile) registration
            );

            if (registrationMeta != null) {
                name = registrationMeta.getFirst();
                type = registrationMeta.getSecond();
            }

            if (name != null) {
                final PsiFile composerFile = directory.findFile(ComposerJson.FILE_NAME);

                if (composerFile instanceof JsonFile) {
                    composerBasedName = getComposerComponentName((JsonFile) composerFile);
                }
                final ComponentData component = new ComponentData(
                        name,
                        composerBasedName,
                        type,
                        directory
                );
                boolean isExcluded = false;

                for (final ModuleScannerFilter filter : filters) {
                    if (filter.isExcluded(component)) {
                        isExcluded = true;
                        break;
                    }
                }

                if (!isExcluded) {
                    if (component.getType().equals(ComponentType.theme)) {
                        themesQty++;
                    } else {
                        modulesQty++;
                    }
                    componentDataList.add(component);
                }
                return;
            }
        }

        for (final PsiDirectory subDirectory : directory.getSubdirectories()) {
            findModuleComponent(subDirectory);
        }
    }

    /**
     * Scan module name from the composer.json file.
     *
     * @param composerFile JsonFile
     *
     * @return String
     */
    private String getComposerComponentName(final JsonFile composerFile) {
        final JsonValue topNode = composerFile.getTopLevelValue();
        String name = null;

        if (topNode instanceof JsonObject) {
            for (final JsonProperty property : ((JsonObject) topNode).getPropertyList()) {
                if ("name".equals(property.getName())) {
                    if (property.getValue() != null) {
                        name = property.getValue().getText().replace("\"", "");
                    }
                    break;
                }
            }
        }

        return name;
    }

    @SuppressWarnings("PMD.AvoidBranchingStatementAsLastInLoop")
    private Pair<String, ComponentType> scanRegistrationMeta(final PhpFile registrationFile) {
        for (final MethodReference reference
                : PsiTreeUtil.findChildrenOfType(registrationFile, MethodReference.class)) {

            if (!RegistrationPhp.REGISTER_METHOD_NAME.equals(reference.getName())) {
                continue;
            }
            final PsiElement typeHolder = reference.getParameter(0);
            final PsiElement nameHolder = reference.getParameter(1);

            if (typeHolder == null || nameHolder == null) {
                return null;
            }
            final String type = unwrapParameterValue(typeHolder);
            final String name = unwrapParameterValue(nameHolder);

            if (name == null || type == null) {
                return null;
            }
            final ComponentType resolvedType = ComponentType.getByValue(type);

            if (resolvedType == null) {
                return null;
            }

            return new Pair<>(name, resolvedType);
        }

        return null;
    }

    private String unwrapParameterValue(final @NotNull PsiElement parameterValue) {
        if (parameterValue instanceof ClassConstantReference) {
            final PsiElement resolvedValue = ((ClassConstantReference) parameterValue).resolve();

            if (!(resolvedValue instanceof ClassConstImpl)) {
                return null;
            }
            final ClassConstImpl resolvedConst = (ClassConstImpl) resolvedValue;
            final PsiElement value = resolvedConst.getDefaultValue();

            if (value == null) {
                return null;
            }

            return unwrapParameterValue(value);
        } else if (parameterValue instanceof StringLiteralExpression) {
            return ((StringLiteralExpression) parameterValue).getContents();
        }

        return null;
    }
}
