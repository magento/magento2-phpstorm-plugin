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
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.magento.idea.magento2plugin.magento.files.ComposerJson;
import com.magento.idea.magento2plugin.magento.files.ThemeXml;
import com.magento.idea.magento2plugin.magento.packages.Package;
import com.magento.idea.magento2uct.execution.scanner.data.ComponentData;
import com.magento.idea.magento2uct.execution.scanner.filter.ModuleScannerFilter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public final class ThemeScanner implements Iterable<ComponentData> {

    private static final String FRAMEWORK_LIBRARY_NAME = "magento/framework";

    private final PsiDirectory rootDirectory;
    private final List<ComponentData> componentDataList;
    private final List<ModuleScannerFilter> filters;

    /**
     * Magento 2 module components scanner constructor.
     *
     * @param rootDirectory PsiDirectory
     * @param filters ModuleScannerFilter[]
     */
    public ThemeScanner(
            final @NotNull PsiDirectory rootDirectory,
            final @NotNull ModuleScannerFilter... filters

    ) {
        this.rootDirectory = rootDirectory;
        this.filters = Arrays.asList(filters);
        componentDataList = new ArrayList<>();
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
    public int getThemeCount() {
        return componentDataList.size();
    }

    /**
     * Run scanner.
     *
     * @return List[ComponentData]
     */
    private List<ComponentData> run() {
        componentDataList.clear();
        findThemeComponent(rootDirectory);

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
    private void findThemeComponent(final @NotNull PsiDirectory directory) {
        String name = null;
        String composerBasedName = null;
        String composerType;
        String type = "magento2-theme";

        for (final PsiFile file : directory.getFiles()) {
            if (file instanceof XmlFile && ThemeXml.FILE_NAME.equals(file.getName())) {
                final XmlTag rootTag = ((XmlFile) file).getRootTag();

                if (rootTag != null && rootTag.getSubTags().length > 0) {
                    final XmlTag themeTag = rootTag.getSubTags()[0];
                    name = themeTag.getValue().getText();
                }
                break;
            }
        }

        for (final PsiFile file : directory.getFiles()) {
            if (file instanceof JsonFile && ComposerJson.FILE_NAME.equals(file.getName())) {
                final Pair<String, String> meta = scanModuleComposerMeta((JsonFile) file);
                composerBasedName = meta.getFirst();
                composerType = meta.getSecond();

                if (composerBasedName == null || composerType == null) {
                    return;
                }

                if (name == null && FRAMEWORK_LIBRARY_NAME.equals(composerBasedName)) {
                    name = meta.getFirst();
                    type = composerType;
                }

                if (!type.equals(composerType) && !"project".equals(composerType)) {
                    return;
                }
                break;
            }
        }

        if (name != null && composerBasedName != null) {
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
                componentDataList.add(component);
            }
            return;
        }

        for (final PsiDirectory subDirectory : directory.getSubdirectories()) {
            findThemeComponent(subDirectory);
        }
    }

    /**
     * Scan module metadata from the composer.json file.
     *
     * @param composerFile JsonFile
     *
     * @return Pair[String, String]
     */
    @SuppressWarnings({
            "PMD.NPathComplexity",
            "PMD.CyclomaticComplexity",
            "PMD.CognitiveComplexity"
    })
    private Pair<String, String> scanModuleComposerMeta(final JsonFile composerFile) {
        final JsonValue topNode = composerFile.getTopLevelValue();
        String name = null;
        String type = null;

        if (topNode instanceof JsonObject) {
            for (final JsonProperty property : ((JsonObject) topNode).getPropertyList()) {
                switch (property.getName()) {
                    case "name":
                        if (property.getValue() != null) {
                            name = property.getValue().getText().replace("\"", "");
                        }
                        break;
                    case "type":
                        if (property.getValue() != null) {
                            type = property.getValue().getText().replace("\"", "");
                        }
                        break;
                    default:
                        break;
                }
                if (name != null && type != null) {
                    break;
                }
            }
        }

        return new Pair<>(name, type);
    }
}
