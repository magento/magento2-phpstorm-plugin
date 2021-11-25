/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.generator.php;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.PhpFile;
import com.magento.idea.magento2plugin.actions.generation.data.php.WebApiInterfaceData;
import com.magento.idea.magento2plugin.actions.generation.data.xml.WebApiXmlRouteData;
import com.magento.idea.magento2plugin.actions.generation.generator.xml.WebApiDeclarationGenerator;
import org.jetbrains.annotations.NotNull;

public final class WebApiInterfaceWithDeclarationGenerator {

    private final Project project;
    private final WebApiInterfaceData interfaceData;
    private final WebApiXmlRouteData routeData;

    /**
     * Web API interface and declaration generator.
     *
     * @param interfaceData WebApiInterfaceData
     * @param routeData WebApiXmlRouteData
     * @param project Project
     */
    public WebApiInterfaceWithDeclarationGenerator(
            final @NotNull WebApiInterfaceData interfaceData,
            final @NotNull WebApiXmlRouteData routeData,
            final @NotNull Project project
    ) {
        this.project = project;
        this.interfaceData = interfaceData;
        this.routeData = routeData;
    }

    /**
     * Generate Web API interface and declaration.
     *
     * @param actionName String
     * @param shouldOpenFiles boolean
     */
    public void generate(final @NotNull String actionName, final boolean shouldOpenFiles) {
        final PsiFile webApiInterfaceFile = new WebApiInterfaceGenerator(
                interfaceData,
                project
        ).generate(actionName, shouldOpenFiles);

        if (webApiInterfaceFile instanceof PhpFile) {
            new WebApiDeclarationGenerator(
                    routeData,
                    project
            ).generate(actionName, shouldOpenFiles);
        }
    }
}
