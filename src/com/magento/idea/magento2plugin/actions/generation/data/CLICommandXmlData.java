/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.data;

import com.jetbrains.php.lang.psi.elements.PhpClass;

public class CLICommandXmlData {
    private final String cliCommandModule;
    private final String cliCommandClass;
    private final String cliCommandDiName;

    public CLICommandXmlData(
            String cliCommandModule,
            String cliCommandClass,
            String cliCommandDiName
    ) {
        this.cliCommandModule = cliCommandModule;
        this.cliCommandClass = cliCommandClass;
        this.cliCommandDiName = cliCommandDiName;
    }

    public String getCLICommandModule() {
        return cliCommandModule;
    }
    public String getCliCommandClass() { return cliCommandClass; }
    public String getCliCommandDiName() {
        return cliCommandDiName;
    }
}
