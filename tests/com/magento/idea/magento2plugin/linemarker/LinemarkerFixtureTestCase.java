/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.linemarker;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl;
import com.magento.idea.magento2plugin.BaseProjectTestCase;
import org.jetbrains.annotations.NotNull;
import javax.swing.*;
import com.magento.idea.magento2plugin.magento.packages.File;
import java.util.List;

abstract public class LinemarkerFixtureTestCase extends BaseProjectTestCase {

    private static final String testDataFolderPath = "testData" + File.separator + "linemarker" + File.separator;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.setTestDataPath(testDataFolderPath);
    }

    protected String getFixturePath(String fileName, String folder) {
        return prepareFixturePath(fileName, folder + File.separator);
    }

    protected void assertHasLinemarkerWithTooltipAndIcon(String tooltip, String icon) {
        myFixture.doHighlighting();
        String lineMarkerNotFound = "Failed that documents contains linemarker with the tooltip `%s`";

        List<LineMarkerInfo<?>> lineMarkers = getDocumentLineMarkers();
        assertNotEmpty(lineMarkers);
        for (LineMarkerInfo lineMarkerInfo: lineMarkers) {
            String lineMarkerTooltip = lineMarkerInfo.getLineMarkerTooltip();
            Icon lineMarkerIcon = lineMarkerInfo.getIcon();
            if (lineMarkerTooltip == null || lineMarkerIcon == null) {
                continue;
            }
            if (lineMarkerTooltip.equals(tooltip) &&
                    lineMarkerIcon.toString().equals(icon)) {
                return;
            }
        }

        fail(String.format(lineMarkerNotFound, tooltip));
    }

    protected void assertHasNoLinemarkerWithTooltipAndIcon(String tooltip, String icon) {
        myFixture.doHighlighting();
        String lineMarkerExist = "Failed that documents not contains linemarker with the tooltip `%s`";

        List<LineMarkerInfo<?>> lineMarkers = getDocumentLineMarkers();
        assertNotEmpty(lineMarkers);
        for (LineMarkerInfo lineMarkerInfo: lineMarkers) {
            String lineMarkerTooltip = lineMarkerInfo.getLineMarkerTooltip();
            Icon lineMarkerIcon = lineMarkerInfo.getIcon();
            if (lineMarkerTooltip == null || lineMarkerIcon == null) {
                continue;
            }
            if (lineMarkerTooltip.equals(tooltip) &&
                    lineMarkerIcon.toString().equals(icon)) {
                fail(String.format(lineMarkerExist, tooltip));
            }
        }
    }

    @NotNull
    private List<LineMarkerInfo<?>> getDocumentLineMarkers() {
        return DaemonCodeAnalyzerImpl.getLineMarkers(myFixture.getEditor().getDocument(), getProject());
    }
}
