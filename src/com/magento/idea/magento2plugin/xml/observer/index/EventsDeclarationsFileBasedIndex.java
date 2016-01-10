package com.magento.idea.magento2plugin.xml.observer.index;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.patterns.PhpPatterns;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.magento.idea.magento2plugin.Settings;
import com.magento.idea.magento2plugin.xml.observer.PhpPatternsHelper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dkvashnin on 11/5/15.
 */
public class EventsDeclarationsFileBasedIndex extends ScalarIndexExtension<String> {
    public static final ID<String, Void> NAME = ID.create("com.magento.idea.magento2plugin.xml.observer.index.events");
    private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();

    @NotNull
    @Override
    public ID<String, Void> getName() {
        return NAME;
    }

    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return new DataIndexer<String, Void, FileContent>() {
            @NotNull
            @Override
            public Map<String, Void> map(@NotNull FileContent fileContent) {
                Map<String, Void> map = new HashMap<>();
                PhpFile phpFile = (PhpFile)fileContent.getPsiFile();

                if (!Settings.isEnabled(phpFile.getProject())) {
                    return map;
                }

                List<String> results = new ArrayList<String>();
                recursiveFill(results, phpFile);
                for (String result: results) {
                    map.put(result, null);
                }

                return map;
            }

            private void recursiveFill(List<String> results, PsiElement psiElement) {
                if (PhpPatternsHelper.STRING_METHOD_ARGUMENT.accepts(psiElement) && isInContextOfDispatchMethod(psiElement)) {
                    String eventName= StringUtil.unquoteString(psiElement.getText());
                    if (eventName.length() > 0) {
                        results.add(eventName);
                    }

                    return;
                }

                for(PsiElement child: psiElement.getChildren()) {
                    recursiveFill(results, child);
                }
            }

            private boolean isInContextOfDispatchMethod(PsiElement psiElement) {
                ParameterList parameterList = PsiTreeUtil.getParentOfType(psiElement, ParameterList.class);

                if (parameterList == null) {
                    return false;
                }

                if(!(parameterList.getContext() instanceof MethodReference)) {
                    return false;
                }

                MethodReference methodReference = (MethodReference)parameterList.getContext();
                if (!methodReference.getName().equals("dispatch")) {
                    return false;
                }

                return true;
            }
        };
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return myKeyDescriptor;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return new FileBasedIndex.InputFilter() {
            @Override
            public boolean acceptInput(@NotNull VirtualFile file) {
                return file.getFileType() == PhpFileType.INSTANCE;
            }
        };
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Override
    public int getVersion() {
        return 1;
    }
}
