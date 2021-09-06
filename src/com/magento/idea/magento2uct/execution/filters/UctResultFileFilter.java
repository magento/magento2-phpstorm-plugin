/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2uct.execution.filters;

import com.intellij.execution.filters.FileHyperlinkInfoBase;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UctResultFileFilter implements Filter {

    private static final String UCT_RESULT_FILE_FORMAT_PREFIX = "Result exported to '";
    private static final String UCT_RESULT_FILE_FORMAT_REGEX = "Result exported to '(.*)'";
    private final Project project;

    /**
     * UCT result file filter constructor.
     *
     * @param project Project
     */
    public UctResultFileFilter(
            final @NotNull Project project
    ) {
        this.project = project;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    @Override
    public @Nullable Result applyFilter(final @NotNull String line, final int entireLength) {
        if (!canContainUctResult(line)) {
            return null;
        }

        final int textStartOffset = entireLength - line.length();
        final Pattern pattern = Pattern.compile(UCT_RESULT_FILE_FORMAT_REGEX);
        final Matcher matcher = pattern.matcher(line);
        ResultItem item = null;

        while (matcher.find()) {
            if (item == null) {
                try {
                    final String filePathCandidate = matcher.group(1);
                    final int matchedStart = line.indexOf(filePathCandidate);
                    final int matchedEnd = matchedStart + filePathCandidate.length();

                    item = new ResultItem(
                            textStartOffset + matchedStart,
                            textStartOffset + matchedEnd,
                            buildHyperLinkInfo(filePathCandidate)
                    );
                } catch (IllegalStateException | IndexOutOfBoundsException exception) {
                    continue;
                }
            }
        }

        if (item != null) {
            return new Result(
                    item.getHighlightStartOffset(),
                    item.getHighlightEndOffset(),
                    item.getHyperlinkInfo()
            );
        }

        return null;
    }

    /**
     * Build hyper link info.
     *
     * @param filePathCandidate String
     *
     * @return HyperlinkInfo
     */
    protected HyperlinkInfo buildHyperLinkInfo(final @NotNull String filePathCandidate) {
        return new FileHyperlinkInfoBase(project, 0, 0) {

            @Override
            protected @Nullable VirtualFile getVirtualFile() {
                return VfsUtil.findFile(Path.of(filePathCandidate), false);
            }
        };
    }

    /**
     * Check if line can contain UCT result file link.
     *
     * @param line String
     *
     * @return boolean
     */
    private boolean canContainUctResult(final @NotNull String line) {
        return line.contains(UCT_RESULT_FILE_FORMAT_PREFIX);
    }
}
