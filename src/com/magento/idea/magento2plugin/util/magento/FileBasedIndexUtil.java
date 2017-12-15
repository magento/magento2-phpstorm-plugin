package com.magento.idea.magento2plugin.util.magento;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.PhpFileType;
import com.magento.idea.magento2plugin.stubs.indexes.ModuleNameIndex;
import com.magento.idea.magento2plugin.util.RegExUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileBasedIndexUtil {

    public static Collection<VirtualFile> findViewVfsByModuleName(String moduleName, Project project)
    {
        Collection<VirtualFile> viewVfs = new ArrayList<>();

        Pattern pattern = Pattern.compile(RegExUtil.Magento.MODULE_NAME);
        Matcher matcher = pattern.matcher(moduleName);
        if (!matcher.find()) {
            return viewVfs;
        }

        Collection<VirtualFile> moduleVfs =
                FileBasedIndex.getInstance().getContainingFiles(ModuleNameIndex.KEY, moduleName,
                    GlobalSearchScope.getScopeRestrictedByFileTypes(
                        GlobalSearchScope.allScope(project),
                        PhpFileType.INSTANCE
                )
        );

        for (VirtualFile moduleVf : moduleVfs) {
            viewVfs.addAll(getValues(moduleName, moduleVf, project));
        }
        return viewVfs;
    }

    public static Collection<VirtualFile> findViewVfsByModuleVf(VirtualFile moduleVf, Project project)
    {
        Collection<VirtualFile> viewVfs = new ArrayList<>();

        for (String moduleName : FileBasedIndex.getInstance().getAllKeys(ModuleNameIndex.KEY, project)) {
            viewVfs.addAll(getValues(moduleName, moduleVf, project));
        }
        return viewVfs;
    }

    private static Collection<VirtualFile> getValues(String moduleName, VirtualFile moduleVf, Project project)
    {
        Collection<VirtualFile> viewVfs = new ArrayList<>();
        FileBasedIndex.getInstance()
                .processValues(
                        ModuleNameIndex.KEY, moduleName, moduleVf,
                        (file, value) -> {
                            VirtualFile viewVf = file.getParent().findFileByRelativePath(value.concat("/view"));
                            if (viewVf != null) {
                                viewVfs.add(viewVf);
                            }
                            return false;
                        },
                        GlobalSearchScope.fileScope(project, moduleVf)
                );
        return viewVfs;
    }
}
