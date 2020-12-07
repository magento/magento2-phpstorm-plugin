/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl;
import com.magento.idea.magento2plugin.BaseProjectTestCase;
import com.magento.idea.magento2plugin.magento.packages.File;
import java.util.List;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

public abstract class LinemarkerFixtureTestCase extends BaseProjectTestCase {

    private static final String TEST_DATA_PATH
            = "testData" + File.separator + "linemarker" + File.separator;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.setTestDataPath(TEST_DATA_PATH);
    }

    protected String getFixturePath(final String fileName, final String folder) {
        return prepareFixturePath(fileName, folder + File.separator);
    }

    protected void assertHasLinemarkerWithTooltipAndIcon(final String tooltip, final String icon) {
        myFixture.doHighlighting();

        final List<LineMarkerInfo<?>> lineMarkers = getDocumentLineMarkers();
        assertNotEmpty(lineMarkers);
        for (final LineMarkerInfo lineMarkerInfo: lineMarkers) {
            final String lineMarkerTooltip = lineMarkerInfo.getLineMarkerTooltip();
            final Icon lineMarkerIcon = lineMarkerInfo.getIcon();
            if (lineMarkerTooltip == null || lineMarkerIcon == null) {
                continue;
            }
            if (lineMarkerTooltip.equals(tooltip)
                    && lineMarkerIcon.toString().contains(icon)) {
                return;
            }
        }

        final String lineMarkerNotFound
                = "Failed that documents contains linemarker with the tooltip `%s`";
        fail(String.format(lineMarkerNotFound, tooltip));
    }

    protected void assertHasNoLinemarkerWithTooltipAndIcon(
            final String tooltip,
            final String icon
    ) {
        myFixture.doHighlighting();
        final String lineMarkerExist
                = "Failed that documents not contains linemarker with the tooltip `%s`";

        final List<LineMarkerInfo<?>> lineMarkers = getDocumentLineMarkers();
        assertNotEmpty(lineMarkers);
        for (final LineMarkerInfo lineMarkerInfo: lineMarkers) {
            final String lineMarkerTooltip = lineMarkerInfo.getLineMarkerTooltip();
            final Icon lineMarkerIcon = lineMarkerInfo.getIcon();
            if (lineMarkerTooltip == null || lineMarkerIcon == null) {
                continue;
            }
            if (lineMarkerTooltip.equals(tooltip)
                    && lineMarkerIcon.toString().equals(icon)) {
                fail(String.format(lineMarkerExist, tooltip));
            }
        }
    }

    @NotNull
    private List<LineMarkerInfo<?>> getDocumentLineMarkers() {
        return DaemonCodeAnalyzerImpl.getLineMarkers(
                myFixture.getEditor().getDocument(),
                getProject()
        );
    }
}
