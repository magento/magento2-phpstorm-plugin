/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.ide.util.MemberChooser;
import com.intellij.lang.LanguageCodeInsightActionHandler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.actions.PhpNamedElementNode;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.parser.PhpStubElementTypes;
import com.jetbrains.php.lang.psi.PhpCodeEditUtil;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.magento.idea.magento2plugin.actions.generation.ImportReferences.PhpClassReferenceResolver;
import com.magento.idea.magento2plugin.actions.generation.data.code.PluginMethodData;
import com.magento.idea.magento2plugin.actions.generation.generator.code.PluginMethodsGenerator;
import com.magento.idea.magento2plugin.actions.generation.util.CodeStyleSettings;
import com.magento.idea.magento2plugin.actions.generation.util.CollectInsertedMethods;
import com.magento.idea.magento2plugin.actions.generation.util.FillTextBufferWithPluginMethods;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.magento.files.Plugin;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import com.magento.idea.magento2plugin.util.magento.plugin.GetTargetClassNamesByPluginClassName;
import com.magento.idea.magento2plugin.util.magento.plugin.IsPluginAllowedForMethod;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;

public abstract class PluginGenerateMethodHandlerBase implements LanguageCodeInsightActionHandler {
    public String type;
    public FillTextBufferWithPluginMethods fillTextBuffer;
    private CollectInsertedMethods collectInsertedMethods;
    private ValidatorBundle validatorBundle;

    public PluginGenerateMethodHandlerBase(Plugin.PluginType type) {
        this.type = type.toString();
        this.fillTextBuffer = FillTextBufferWithPluginMethods.getInstance();
        this.collectInsertedMethods = CollectInsertedMethods.getInstance();
        this.validatorBundle = new ValidatorBundle();
    }

    public static Collection<PhpNamedElementNode> fixOrderToBeAsOriginalFiles(PhpNamedElementNode[] selected) {
        List<PhpNamedElementNode> newSelected = ContainerUtil.newArrayList(selected);
        Collections.sort(newSelected, (o1, o2) -> {
            PsiElement psiElement = o1.getPsiElement();
            PsiElement psiElement2 = o2.getPsiElement();
            PsiFile containingFile = psiElement.getContainingFile();
            PsiFile containingFile2 = psiElement2.getContainingFile();
            return containingFile == containingFile2 ? psiElement.getTextOffset() - psiElement2.getTextOffset() : containingFile.getName().compareTo(containingFile2.getName());
        });
        return newSelected;
    }

    private static int getSuitableEditorPosition(Editor editor, PhpFile phpFile) {
        PsiElement currElement = phpFile.findElementAt(editor.getCaretModel().getOffset());
        if (currElement != null) {
            PsiElement parent = currElement.getParent();

            for (PsiElement prevParent = currElement; parent != null && !(parent instanceof PhpFile); parent = parent.getParent()) {
                if (isClassMember(parent)) {
                    return getNextPos(parent);
                }

                if (parent instanceof PhpClass) {
                    while (prevParent != null) {
                        if (isClassMember(prevParent) || PhpPsiUtil.isOfType(prevParent, PhpTokenTypes.chLBRACE)) {
                            return getNextPos(prevParent);
                        }

                        prevParent = prevParent.getPrevSibling();
                    }

                    for (PsiElement classChild = parent.getFirstChild(); classChild != null; classChild = classChild.getNextSibling()) {
                        if (PhpPsiUtil.isOfType(classChild, PhpTokenTypes.chLBRACE)) {
                            return getNextPos(classChild);
                        }
                    }
                }

                prevParent = parent;
            }
        }

        return -1;
    }

    private static boolean isClassMember(PsiElement element) {
        if (element == null) {
            return false;
        }
        IElementType elementType = element.getNode().getElementType();
        return elementType == PhpElementTypes.CLASS_FIELDS || elementType == PhpElementTypes.CLASS_CONSTANTS || elementType == PhpStubElementTypes.CLASS_METHOD;
    }

    private static int getNextPos(PsiElement element) {
        PsiElement next = element.getNextSibling();
        return next != null ? next.getTextOffset() : -1;
    }

    public boolean isValidFor(Editor editor, PsiFile file) {
        if (!(file instanceof PhpFile)) {
            return false;
        }
        PhpClass phpClass = PhpCodeEditUtil.findClassAtCaret(editor, file);
        if (phpClass == null) {
            return false;
        }
        GetTargetClassNamesByPluginClassName targetClassesService = GetTargetClassNamesByPluginClassName.getInstance(editor.getProject());
        String currentClass = phpClass.getFQN().substring(1);
        ArrayList<String> targetClassNames = targetClassesService.execute(currentClass);
        return !targetClassNames.isEmpty();
    }

    public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile pluginFile) {
        PhpFile pluginPhpFile = (PhpFile) pluginFile;
        PhpClass pluginClass = PhpCodeEditUtil.findClassAtCaret(editor, pluginPhpFile);
        Key<Object> targetClassKey = Key.create(PluginMethodsGenerator.originalTargetKey);
        if (pluginClass == null) {
            return;
        }
        PhpNamedElementNode[] fieldsToShow = this.targetMethods(pluginClass, targetClassKey);
        if (fieldsToShow.length == 0) {
            if (ApplicationManager.getApplication().isHeadlessEnvironment()) {
                return;
            }
            HintManager.getInstance().showErrorHint(editor, this.getErrorMessage());
            return;
        }
        PhpNamedElementNode[] members = this.chooseMembers(fieldsToShow, true, pluginFile.getProject());
        if (members == null || members.length == 0) {
            return;
        }
        int insertPos = getSuitableEditorPosition(editor, pluginPhpFile);

        CodeStyleSettings codeStyleSettings = new CodeStyleSettings(pluginPhpFile);
        codeStyleSettings.adjustBeforeWrite();
        ApplicationManager.getApplication().runWriteAction(() -> {
            Set<CharSequence> insertedMethodsNames = new THashSet();
            PhpClassReferenceResolver resolver = new PhpClassReferenceResolver();
            StringBuffer textBuf = new StringBuffer();
            PhpPsiElement scope = PhpCodeInsightUtil.findScopeForUseOperator(pluginClass);

            for (PhpNamedElementNode member : members) {
                PsiElement method = member.getPsiElement();
                PluginMethodData[] pluginMethods = this.createPluginMethods(pluginClass, (Method) method, targetClassKey);
                fillTextBuffer.execute(targetClassKey, insertedMethodsNames, resolver, textBuf, pluginMethods);
            }

            insertPluginMethodsToFile(project, editor, pluginFile, pluginClass, insertPos, insertedMethodsNames, resolver, textBuf, scope);
        });
        codeStyleSettings.restore();
    }

    private void insertPluginMethodsToFile(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile pluginFile, PhpClass pluginClass, int insertPos, Set<CharSequence> insertedMethodsNames, PhpClassReferenceResolver resolver, StringBuffer textBuf, PhpPsiElement scope) {
        if (textBuf.length() > 0 && insertPos >= 0) {
            editor.getDocument().insertString(insertPos, textBuf);
            int endPos = insertPos + textBuf.length();
            CodeStyleManager.getInstance(project).reformatText(pluginFile, insertPos, endPos);
            PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
        }
        if (!insertedMethodsNames.isEmpty()) {
            List<PsiElement> insertedMethods = collectInsertedMethods.execute(pluginFile, pluginClass.getNameCS(), insertedMethodsNames);
            if (scope != null && insertedMethods != null) {
                resolver.importReferences(scope, insertedMethods);
            }
        }
    }

    protected abstract PluginMethodData[] createPluginMethods(PhpClass currentClass, Method method, Key<Object> targetClassKey);

    protected String getErrorMessage() {
        return "No methods to generate";
    }

    public boolean startInWriteAction() {
        return false;
    }

    @Nullable
    protected PhpNamedElementNode[] chooseMembers(PhpNamedElementNode[] members, boolean allowEmptySelection, Project project) {
        PhpNamedElementNode[] nodes = fixOrderToBeAsOriginalFiles(members).toArray(new PhpNamedElementNode[members.length]);
        if (!ApplicationManager.getApplication().isHeadlessEnvironment()) {
            PluginGenerateMethodHandlerBase.MyMemberChooser chooser = new PluginGenerateMethodHandlerBase.MyMemberChooser(nodes, allowEmptySelection, project);
            chooser.setTitle("Choose Methods");
            chooser.setCopyJavadocVisible(false);
            chooser.show();
            List<PhpNamedElementNode> list = chooser.getSelectedElements();
            return list == null ? null : list.toArray(new PhpNamedElementNode[0]);
        }

        return nodes;
    }

    @NotNull
    protected PhpNamedElementNode[] targetMethods(@NotNull PhpClass phpClass, Key<Object> targetClassKey) {
        TreeMap<String, PhpNamedElementNode> nodes = new TreeMap();

        GetTargetClassNamesByPluginClassName targetClassesService = GetTargetClassNamesByPluginClassName.getInstance(phpClass.getProject());
        String currentClass = phpClass.getFQN().substring(1);
        ArrayList<String> targetClassNames = targetClassesService.execute(currentClass);
        for (String targetClassName : targetClassNames) {
            PhpClass targetClass = GetPhpClassByFQN.getInstance(phpClass.getProject()).execute(targetClassName);

            if (targetClass == null) {
                String errorMessage = validatorBundle.message("validator.class.targetClassNotFound");
                JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            if (targetClass.isFinal()) {
                continue;
            }

            Collection<Method> methods = targetClass.getMethods();
            Iterator methodIterator = methods.iterator();

            while (methodIterator.hasNext()) {
                Method method = (Method) methodIterator.next();
                if (IsPluginAllowedForMethod.getInstance().check(method) && !pluginAlreadyHasMethod(phpClass, method)) {
                    method.putUserData(targetClassKey, targetClass);
                    nodes.put(method.getName(), new PhpNamedElementNode(method));
                }
            }
        }
        PhpNamedElementNode[] targetMethods = nodes.values().toArray(new PhpNamedElementNode[0]);

        return targetMethods;
    }

    protected boolean pluginAlreadyHasMethod(@NotNull PhpClass currentClass, @NotNull Method method) {
        Collection<Method> currentMethods = currentClass.getMethods();
        String methodName = method.getName();
        String methodPrefix = type;
        String methodSuffix = methodName.substring(0, 1).toUpperCase() + methodName.substring(1);
        String pluginMethodName = methodPrefix.concat(methodSuffix);
        for (Method currentMethod : currentMethods) {
            if (!currentMethod.getName().equals(pluginMethodName)) {
                continue;
            }
            return true;
        }
        return false;
    }

    private static class MyMemberChooser extends MemberChooser<PhpNamedElementNode> {
        protected MyMemberChooser(@NotNull PhpNamedElementNode[] nodes, boolean allowEmptySelection, @NotNull Project project) {
            super(nodes, allowEmptySelection, true, project, false);
        }
    }
}
