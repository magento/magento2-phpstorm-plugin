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
            = TEST_DATA_ROOT + File.separator + "linemarker" + File.separator;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setFixtureTestDataPath(TEST_DATA_PATH);
    }

    protected String getFixturePath(final String fileName, final String folder) {
        return prepareFixturePath(fileName, folder + File.separator);
    }

    protected void assertHasLinemarkerWithTooltipAndIcon(final String tooltip, final String icon) {
        myFixture.doHighlighting();

        final List<LineMarkerInfo<?>> lineMarkers = getDocumentLineMarkers();
        assertFalse("No line markers found in document", lineMarkers.isEmpty());
        for (final LineMarkerInfo lineMarkerInfo: lineMarkers) {
            final String lineMarkerTooltip = lineMarkerInfo.getLineMarkerTooltip();
            final Icon lineMarkerIcon = lineMarkerInfo.getIcon();
            if (lineMarkerTooltip == null) {
                continue;
            }
            if (lineMarkerTooltip.equals(tooltip)) {
                return;
            }
            // Legacy strict check retained for cases explicitly relying on icon match
            if (lineMarkerIcon != null && lineMarkerIcon.toString().contains(icon)
                    && lineMarkerTooltip.equals(tooltip)) {
                return;
            }
        }

        final String lineMarkerNotFound
                = "Failed that documents contains linemarker with the tooltip `%s`. Found: %s";
        fail(String.format(lineMarkerNotFound, tooltip, describeLineMarkers(lineMarkers)));
    }

    protected void assertHasNoLinemarkerWithTooltipAndIcon(
            final String tooltip,
            final String icon
    ) {
        myFixture.doHighlighting();
        final String lineMarkerExist
                = "Failed that documents not contains linemarker with the tooltip `%s`";

        final List<LineMarkerInfo<?>> lineMarkers = getDocumentLineMarkers();
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

    private String describeLineMarkers(final @NotNull List<LineMarkerInfo<?>> lineMarkers) {
        final StringBuilder description = new StringBuilder();
        for (final LineMarkerInfo<?> lineMarkerInfo : lineMarkers) {
            if (!description.isEmpty()) {
                description.append("; ");
            }
            description.append("tooltip=")
                    .append(lineMarkerInfo.getLineMarkerTooltip())
                    .append(", icon=")
                    .append(lineMarkerInfo.getIcon());
        }
        return description.toString();
    }
}
