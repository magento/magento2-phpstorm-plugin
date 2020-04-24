/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation.util;

import com.intellij.application.options.CodeStyle;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.psi.PhpFile;

public class CodeStyleSettings {
    private PhpFile phpFile;
    private boolean currLineBreaks;
    private int currBlankLines;
    private CommonCodeStyleSettings settings;

    public CodeStyleSettings(PhpFile phpFile){
        this.phpFile = phpFile;
        this.settings = CodeStyle.getLanguageSettings(this.phpFile, PhpLanguage.INSTANCE);
    }

    public void adjustBeforeWrite() {
        this.currLineBreaks = settings.KEEP_LINE_BREAKS;
        this.currBlankLines = settings.KEEP_BLANK_LINES_IN_CODE;
        settings.KEEP_LINE_BREAKS = false;
        settings.KEEP_BLANK_LINES_IN_CODE = 0;
    };

    public void restore() {
        settings.KEEP_LINE_BREAKS = currLineBreaks;
        settings.KEEP_BLANK_LINES_IN_CODE = currBlankLines;
    }
}
