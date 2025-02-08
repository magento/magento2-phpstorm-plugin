/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.magento.files;

import com.intellij.lang.Language;

public interface ModuleFileInterface {
    String getFileName();
    String getTemplate();
    Language getLanguage();
}
