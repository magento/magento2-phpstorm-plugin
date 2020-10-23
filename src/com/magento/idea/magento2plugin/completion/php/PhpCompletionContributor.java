/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.completion.php;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.magento.idea.magento2plugin.completion.provider.ModuleNameCompletionProvider;

import static com.magento.idea.magento2plugin.util.php.PhpPatternsHelper.CONFIGPHP_COMPLETION;

public class PhpCompletionContributor extends CompletionContributor {
    public PhpCompletionContributor() {
        /*
          'modules' => [
            'Vendor_Module' => 1
          ]
         */
        extend(
            CompletionType.BASIC,
            CONFIGPHP_COMPLETION,
            new ModuleNameCompletionProvider()
        );
    }
}
