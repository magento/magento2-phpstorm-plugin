/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

public class CLICommandXmlData {
    private final String cliCommandModule;
    private final String cliCommandClass;
    private final String cliCommandDiName;

    /**
     * CLI Command xml data class.
     *
     * @param cliCommandModule CLI Command module
     * @param cliCommandClass CLI Command PHP class
     * @param cliCommandDiName CLI Command di.xml item name
     */
    public CLICommandXmlData(
            final String cliCommandModule,
            final String cliCommandClass,
            final String cliCommandDiName
    ) {
        this.cliCommandModule = cliCommandModule;
        this.cliCommandClass = cliCommandClass;
        this.cliCommandDiName = cliCommandDiName;
    }

    public String getCLICommandModule() {
        return cliCommandModule;
    }

    public String getCliCommandClass() {
        return cliCommandClass;
    }

    public String getCliCommandDiName() {
        return cliCommandDiName;
    }
}
