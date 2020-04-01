/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.completion.provider;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.magento.idea.magento2plugin.util.RegExUtil;
import com.magento.idea.magento2plugin.util.VfsUtil;
import com.magento.idea.magento2plugin.util.magento.FileBasedIndexUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilePathCompletionProvider extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  ProcessingContext context,
                                  @NotNull CompletionResultSet result) {
        PsiElement position = parameters.getPosition().getOriginalElement();
        if (position == null) {
            return;
        }

        String prefix = result.getPrefixMatcher().getPrefix();

        String filePath;
        String filePathPrefix;
        boolean removeFileExtension = false;

        Pattern pattern = Pattern.compile(
                "(" + RegExUtil.Magento.MODULE_NAME + "(\\W+))(" + RegExUtil.FILE_PATH + ")?"
        );
        Matcher matcher = pattern.matcher(prefix);
        if (matcher.find()) {
            filePathPrefix = matcher.group(1);
            removeFileExtension = matcher.group(2).equals("/");
            filePath = matcher.group(3) != null ? matcher.group(3) : "";
        } else {
            pattern = Pattern.compile("(" + RegExUtil.FILE_PATH + ")+");
            matcher = pattern.matcher(prefix);
            if (!matcher.find()) {
                return;
            }
            filePathPrefix = "";
            filePath = matcher.group(1);
        }

        // find all view virtual files
        Collection<VirtualFile> viewVfs = findViewVfs(parameters, result);

        for (VirtualFile vf:viewVfs) {
            for (VirtualFile file : VfsUtil.getAllSubFiles(vf)) {
                String label = file.getPath().replace(vf.getPath(), "");

                pattern = label.matches("(/[\\w-]+){2}/template/.*")
                    //e.g. (adminhtml|frontend|base|*)/(web|layout|*)/template/{whatever client typed}
                    ? Pattern.compile("^((/[\\w-]+){3}/)" + filePath)
                    //e.g. (adminhtml|frontend|base|*)/(web|layout|*)/{whatever client typed}
                    : Pattern.compile("^((/[\\w-]+){2}/)" + filePath);

                matcher = pattern.matcher(label);
                if (!matcher.find()) {
                    continue;
                }

                //remove prefix
                label = label.replace(matcher.group(0), "");
                boolean lastPathSegment = !(label.indexOf("/", 1) > 0);
                label = lastPathSegment ? label : label.substring(0, label.indexOf("/", 1));
                label = filePathPrefix + filePath + label;
                label = !removeFileExtension ? label : (label.lastIndexOf(".") > 0
                        ? label.substring(0, label.lastIndexOf("."))
                        : label
                );

                result.addElement(
                        LookupElementBuilder
                                .create(label)
                                .withIcon(lastPathSegment ? file.getFileType().getIcon() : AllIcons.Nodes.Folder)
                );
            }
        }
    }

    private Collection<VirtualFile> findViewVfs(CompletionParameters parameters, CompletionResultSet result)
    {
        Collection<VirtualFile> viewVfs = new ArrayList<>();

        Pattern pattern = Pattern.compile(RegExUtil.Magento.MODULE_NAME);
        Matcher matcher = pattern.matcher(result.getPrefixMatcher().getPrefix());

        if (matcher.find()) {
            viewVfs.addAll(
                FileBasedIndexUtil.findViewVfsByModuleName(matcher.group(0), parameters.getPosition().getProject())
            );
        } else {
            VirtualFile tf = parameters.getOriginalFile().getVirtualFile();
            if (tf != null) {
                VirtualFile moduleVf =
                        VfsUtil.findVfUp(tf, "registration.php");

                if (moduleVf != null) {
                    viewVfs.addAll(
                            FileBasedIndexUtil.findViewVfsByModuleVf(moduleVf, parameters.getPosition().getProject())
                    );
                }
            }
        }

        return viewVfs;
    }
}
