package com.magento.idea.magento2plugin.actions.generation.provider;

import com.intellij.psi.PsiDirectory;
import com.magento.idea.magento2plugin.actions.generation.data.NewBlockData;
import com.magento.idea.magento2plugin.magento.packages.Package;

public class NewBlockDataProvider {

    /**
     * Suggest information about new block based on directory context
     */
    public static NewBlockData get(PsiDirectory directory) {
        String dirPath = directory.toString();

        // case1. click was out of app/code directory - sample name can be suggested only
        if (!dirPath.contains(Package.PACKAGES_ROOT)) {
            return new NewBlockData("SampleBlock", null, null, null);
        }

        String[] directoryParts = dirPath.split("/" + Package.PACKAGES_ROOT + "/");

        // case2. click was on the app/code/vendor directory - we can suggest vendor of module
        // case3. click was on the app/code/vendor/module directory - we can suggest module name
        // case4. click was on the app/code/vendor/module/Block directory - we can suggest module name
        // case4. click was on the app/code/vendor/module/Block/Adminhtml directory - we can suggest module name, area
        // case4. click was on the app/code/vendor/module/Block/Something/Something directory - we can suggest module name, area and parent path

        return new NewBlockData("SampleBlock", "", "", "");
    }
}
