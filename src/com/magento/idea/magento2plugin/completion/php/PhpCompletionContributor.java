/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.completion.php;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.magento.idea.magento2plugin.completion.provider.ModuleNameCompletionProvider;
import com.magento.idea.magento2plugin.util.php.PhpPatternsHelper;

@SuppressWarnings({
        "PMD.CallSuperInConstructor",
})
public class PhpCompletionContributor extends CompletionContributor {

    /**
     * Constructor.
     */
    public PhpCompletionContributor() {
        /*
          'modules' => [
            'Vendor_Module' => 1
          ]
         */
        extend(
                CompletionType.BASIC,
                PhpPatternsHelper.CONFIGPHP_COMPLETION,
                new ModuleNameCompletionProvider()
        );
    }
}
