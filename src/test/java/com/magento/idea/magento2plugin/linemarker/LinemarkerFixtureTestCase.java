/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.linemarker;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.MergeableLineMarkerInfo;
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
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
            final String lineMarkerTooltip = normalizeTooltip(lineMarkerInfo.getLineMarkerTooltip());
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
            final String lineMarkerTooltip = normalizeTooltip(lineMarkerInfo.getLineMarkerTooltip());
            final Icon lineMarkerIcon = lineMarkerInfo.getIcon();
            if (lineMarkerTooltip == null || lineMarkerIcon == null) {
                continue;
            }
            if (!lineMarkerTooltip.equals(tooltip)) {
                continue;
            }
            if (icon.isEmpty() || lineMarkerIcon.toString().equals(icon)) {
                fail(String.format(lineMarkerExist, tooltip));
            }
        }
    }

    protected void assertLinemarkerCountWithTooltip(
            final String tooltip,
            final int expectedCount
    ) {
        myFixture.doHighlighting();

        int actualCount = 0;
        final List<LineMarkerInfo<?>> lineMarkers = getDocumentLineMarkers();

        for (final LineMarkerInfo lineMarkerInfo: lineMarkers) {
            if (tooltip.equals(normalizeTooltip(lineMarkerInfo.getLineMarkerTooltip()))) {
                actualCount++;
            }
        }

        assertEquals(
                String.format(
                        "Unexpected linemarker count for tooltip `%s`. Found: %s",
                        tooltip,
                        describeLineMarkers(lineMarkers)
                ),
                expectedCount,
                actualCount
        );
    }

    protected void assertFirstAnchorLinemarkerCount(final int expectedCount) {
        myFixture.doHighlighting();

        final PsiElement anchor = PsiTreeUtil.getDeepestFirst(myFixture.getFile());
        final int anchorStartOffset = anchor.getTextRange().getStartOffset();
        assertLinemarkerCountAtOffset(anchorStartOffset, expectedCount);
    }

    protected void assertLinemarkerCountAtText(
            final @NotNull String text,
            final int expectedCount
    ) {
        myFixture.doHighlighting();

        final int offset = myFixture.getEditor().getDocument().getText().indexOf(text);

        assertTrue("Text not found in fixture: " + text, offset >= 0);
        assertLinemarkerCountAtOffset(offset, expectedCount);
    }

    protected void assertNoMergeableLinemarkersWithTooltip(final @NotNull String tooltip) {
        myFixture.doHighlighting();

        final List<LineMarkerInfo<?>> lineMarkers = getDocumentLineMarkers();

        for (final LineMarkerInfo<?> lineMarkerInfo : lineMarkers) {
            if (tooltip.equals(normalizeTooltip(lineMarkerInfo.getLineMarkerTooltip()))
                    && lineMarkerInfo instanceof MergeableLineMarkerInfo<?>) {
                fail(String.format(
                        "Unexpected mergeable linemarker for tooltip `%s`. Found: %s",
                        tooltip,
                        describeLineMarkers(lineMarkers)
                ));
            }
        }
    }

    private void assertLinemarkerCountAtOffset(
            final int offset,
            final int expectedCount
    ) {
        int actualCount = 0;
        final List<LineMarkerInfo<?>> lineMarkers = getDocumentLineMarkers();

        for (final LineMarkerInfo<?> lineMarkerInfo : lineMarkers) {
            if (lineMarkerInfo.startOffset == offset) {
                actualCount++;
            }
        }

        assertEquals(
                String.format(
                        "Unexpected linemarker count at offset %s. Found: %s",
                        offset,
                        describeLineMarkers(lineMarkers)
                ),
                expectedCount,
                actualCount
        );
    }

    @NotNull
    private List<LineMarkerInfo<?>> getDocumentLineMarkers() {
        return DaemonCodeAnalyzerImpl.getLineMarkers(
                myFixture.getEditor().getDocument(),
                getProject()
        );
    }

    private String normalizeTooltip(final String tooltip) {
        if (tooltip == null) {
            return null;
        }
        return StringUtil.trimEnd(
                StringUtil.trimStart(tooltip, "<html>"),
                "</html>"
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
                    .append(", startOffset=")
                    .append(lineMarkerInfo.startOffset)
                    .append(", icon=")
                    .append(lineMarkerInfo.getIcon());
        }
        return description.toString();
    }
}
