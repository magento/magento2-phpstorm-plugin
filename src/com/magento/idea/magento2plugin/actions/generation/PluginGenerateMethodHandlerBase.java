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
import com.magento.idea.magento2plugin.actions.generation.data.code.PluginMethodData;
import com.magento.idea.magento2plugin.actions.generation.generator.code.PluginMethodsGenerator;
import com.magento.idea.magento2plugin.actions.generation.references.PhpClassReferenceResolver;
import com.magento.idea.magento2plugin.actions.generation.util.CodeStyleSettings;
import com.magento.idea.magento2plugin.actions.generation.util.CollectInsertedMethods;
import com.magento.idea.magento2plugin.actions.generation.util.FillTextBufferWithPluginMethods;
import com.magento.idea.magento2plugin.bundles.CommonBundle;
import com.magento.idea.magento2plugin.bundles.ValidatorBundle;
import com.magento.idea.magento2plugin.magento.files.Plugin;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import com.magento.idea.magento2plugin.util.magento.plugin.GetTargetClassNamesByPluginClassName;
import com.magento.idea.magento2plugin.util.magento.plugin.IsPluginAllowedForMethodUtil;
import gnu.trove.THashSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.JOptionPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({
        "PMD.GodClass",
        "PMD.ExcessiveImports",
        "PMD.CyclomaticComplexity"
})
public abstract class PluginGenerateMethodHandlerBase implements LanguageCodeInsightActionHandler {
    private final CollectInsertedMethods collectInsertedMethods;
    private final ValidatorBundle validatorBundle;
    private final CommonBundle commonBundle;
    public String type;
    public FillTextBufferWithPluginMethods fillTextBuffer;

    /**
     * Constructor.
     *
     * @param type Plugin.PluginType
     */
    public PluginGenerateMethodHandlerBase(final Plugin.PluginType type) {
        this.type = type.toString();
        this.fillTextBuffer = new FillTextBufferWithPluginMethods();
        this.collectInsertedMethods = CollectInsertedMethods.getInstance();
        this.validatorBundle = new ValidatorBundle();
        this.commonBundle = new CommonBundle();
    }

    @Override
    public boolean isValidFor(final Editor editor, final PsiFile file) {
        if (!(file instanceof PhpFile)) {
            return false;
        }
        final PhpClass phpClass = PhpCodeEditUtil.findClassAtCaret(editor, file);
        if (phpClass == null) {
            return false;
        }
        final GetTargetClassNamesByPluginClassName targetClassesService
                = new GetTargetClassNamesByPluginClassName(editor.getProject());
        final String currentClass = phpClass.getFQN().substring(1);
        final List<String> targetClassNames = targetClassesService.execute(currentClass);
        return !targetClassNames.isEmpty();
    }

    @Override
    public void invoke(
            final @NotNull Project project,
            final @NotNull Editor editor,
            final @NotNull PsiFile pluginFile
    ) {
        final PhpFile pluginPhpFile = (PhpFile)pluginFile;
        final PhpClass pluginClass = PhpCodeEditUtil.findClassAtCaret(editor, pluginPhpFile);
        if (pluginClass == null) {
            return;
        }
        final Key<Object> targetClassKey = Key.create(PluginMethodsGenerator.originalTargetKey);
        final PhpNamedElementNode[] fieldsToShow = this.targetMethods(pluginClass, targetClassKey);
        if (fieldsToShow.length == 0) {
            if (ApplicationManager.getApplication().isHeadlessEnvironment()) {
                return;
            }
            HintManager.getInstance().showErrorHint(editor, this.getErrorMessage());
            return;
        }
        final PhpNamedElementNode[] members = this.chooseMembers(
                fieldsToShow,
                true,
                pluginFile.getProject()
        );
        if (members == null || members.length == 0) {
            return;
        }
        final int insertPos = getSuitableEditorPosition(editor, pluginPhpFile);

        final CodeStyleSettings codeStyleSettings = new CodeStyleSettings(pluginPhpFile);
        codeStyleSettings.adjustBeforeWrite();
        ApplicationManager.getApplication().runWriteAction(() -> {
            final Set<CharSequence> insertedMethodsNames = new THashSet();
            final PhpClassReferenceResolver resolver = new PhpClassReferenceResolver();
            final StringBuffer textBuf = new StringBuffer();
            final PhpPsiElement scope = PhpCodeInsightUtil.findScopeForUseOperator(pluginClass);

            for (final PhpNamedElementNode member : members) {
                final PsiElement method = member.getPsiElement();
                final PluginMethodData[] pluginMethods = this.createPluginMethods(
                        pluginClass,
                        (Method) method,
                        targetClassKey
                );
                fillTextBuffer.execute(
                        targetClassKey,
                        insertedMethodsNames,
                        resolver,
                        textBuf,
                        pluginMethods
                );
            }

            insertPluginMethodsToFile(
                    project,
                    editor,
                    pluginFile,
                    pluginClass,
                    insertPos,
                    insertedMethodsNames,
                    resolver,
                    textBuf,
                    scope
            );
        });
        codeStyleSettings.restore();
    }

    private void insertPluginMethodsToFile(
            final @NotNull Project project,
            final @NotNull Editor editor,
            final @NotNull PsiFile pluginFile,
            final PhpClass pluginClass,
            final int insertPos,
            final Set<CharSequence> insertedMethodsNames,
            final PhpClassReferenceResolver resolver,
            final StringBuffer textBuf,
            final PhpPsiElement scope
    ) {
        if (textBuf.length() > 0 && insertPos >= 0) {
            editor.getDocument().insertString(insertPos, textBuf);
            final int endPos = insertPos + textBuf.length();
            CodeStyleManager.getInstance(project).reformatText(pluginFile, insertPos, endPos);
            PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
        }
        if (!insertedMethodsNames.isEmpty()) {
            final List<PsiElement> insertedMethods = collectInsertedMethods.execute(
                    pluginFile,
                    pluginClass.getNameCS(),
                    insertedMethodsNames
            );
            if (scope != null && insertedMethods != null) {
                resolver.importReferences(scope, insertedMethods);
            }
        }
    }

    protected abstract PluginMethodData[] createPluginMethods(
            PhpClass currentClass,
            Method method,
            Key<Object> targetClassKey
    );

    protected String getErrorMessage() {
        return "No methods to generate";
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Nullable
    protected PhpNamedElementNode[] chooseMembers(
            final PhpNamedElementNode[] members,
            final boolean allowEmptySelection,
            final Project project
    ) {
        final PhpNamedElementNode[] nodes = fixOrderToBeAsOriginalFiles(members).toArray(
                new PhpNamedElementNode[members.length]
        );
        if (!ApplicationManager.getApplication().isHeadlessEnvironment()) {
            final PluginGenerateMethodHandlerBase.MyMemberChooser chooser
                    = new PluginGenerateMethodHandlerBase.MyMemberChooser(
                            nodes,
                            allowEmptySelection,
                            project
                        );
            chooser.setTitle("Choose Methods");
            chooser.setCopyJavadocVisible(false);
            chooser.show();
            final List<PhpNamedElementNode> list = chooser.getSelectedElements();
            return list == null ? null : list.toArray(new PhpNamedElementNode[0]);
        }

        return nodes;
    }

    @NotNull
    protected PhpNamedElementNode[] targetMethods(
            final @NotNull PhpClass phpClass,
            final Key<Object> targetClassKey
    ) {
        final TreeMap<String, PhpNamedElementNode> nodes = new TreeMap();

        final GetTargetClassNamesByPluginClassName targetClassesService =
                new GetTargetClassNamesByPluginClassName(phpClass.getProject());
        final String currentClass = phpClass.getFQN().substring(1);
        final List<String> targetClassNames = targetClassesService.execute(currentClass);
        for (final String targetClassName : targetClassNames) {
            final PhpClass targetClass = GetPhpClassByFQN.getInstance(
                    phpClass.getProject()
            ).execute(targetClassName);

            if (targetClass == null) {
                final String errorMessage = validatorBundle.message(
                        "validator.class.targetClassNotFound"
                );
                JOptionPane.showMessageDialog(
                        null,
                        errorMessage,
                        commonBundle.message("common.error"),
                        JOptionPane.ERROR_MESSAGE
                );
                continue;
            }

            if (targetClass.isFinal()) {
                continue;
            }

            final Collection<Method> methods = targetClass.getMethods();
            final Iterator methodIterator = methods.iterator();

            while (methodIterator.hasNext()) {
                final Method method = (Method) methodIterator.next();
                if (IsPluginAllowedForMethodUtil.check(method)
                        && !pluginAlreadyHasMethod(phpClass, method)) {
                    method.putUserData(targetClassKey, targetClass);
                    nodes.put(method.getName(), new PhpNamedElementNode(method));//NOPMD
                }
            }
        }

        return nodes.values().toArray(new PhpNamedElementNode[0]);
    }

    /**
     * Plugin has a method check.
     *
     * @param currentClass PhpClass
     * @param method Method
     * @return boolean
     */
    protected boolean pluginAlreadyHasMethod(
            final @NotNull PhpClass currentClass,
            final @NotNull Method method
    ) {
        final Collection<Method> currentMethods = currentClass.getMethods();
        final String methodName = method.getName();
        final String methodPrefix = type;
        final String methodSuffix = methodName.substring(0, 1).toUpperCase(Locale.getDefault())
                + methodName.substring(1);
        final String pluginMethodName = methodPrefix.concat(methodSuffix);
        for (final Method currentMethod: currentMethods) {
            if (currentMethod.getName().equals(pluginMethodName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sort Order fix.
     *
     * @param selected PhpNamedElementNode
     * @return Collection
     */
    public static Collection<PhpNamedElementNode> fixOrderToBeAsOriginalFiles(
            final PhpNamedElementNode... selected
    ) {
        final List<PhpNamedElementNode> newSelected = ContainerUtil.newArrayList(selected);
        newSelected.sort((o1, o2) -> {
            final PsiElement psiElement = o1.getPsiElement();
            final PsiElement psiElement2 = o2.getPsiElement();
            final PsiFile containingFile = psiElement.getContainingFile();
            final PsiFile containingFile2 = psiElement2.getContainingFile();
            return containingFile.equals(containingFile2)
                ? psiElement.getTextOffset() - psiElement2.getTextOffset()
                : getDocumentPosition(containingFile, containingFile2);
        });
        return newSelected;
    }

    private static int getDocumentPosition(
            final PsiFile containingFile,
            final PsiFile containingFile2
    ) {
        return containingFile.getVirtualFile().getPresentableUrl().compareTo(
            containingFile2.getVirtualFile().getPresentableUrl()
        );
    }

    private static int getSuitableEditorPosition(final Editor editor, final PhpFile phpFile) {
        final PsiElement currElement = phpFile.findElementAt(editor.getCaretModel().getOffset());
        if (currElement != null) {
            PsiElement parent = currElement.getParent();

            for (PsiElement prevParent = currElement;
                    parent != null && !(parent instanceof PhpFile); parent = parent.getParent()) {
                if (isClassMember(parent)) {
                    return getNextPos(parent);
                }

                if (parent instanceof PhpClass) {
                    while (prevParent != null) {
                        if (isClassMember(prevParent) || PhpPsiUtil.isOfType(//NOPMD
                                prevParent, PhpTokenTypes.chLBRACE)) {
                            return getNextPos(prevParent);
                        }

                        prevParent = prevParent.getPrevSibling();//NOPMD
                    }

                    for (PsiElement classChild = parent.getFirstChild();
                                classChild != null; classChild = classChild.getNextSibling()) {
                        if (PhpPsiUtil.isOfType(classChild, PhpTokenTypes.chLBRACE)) { //NOPMD
                            return getNextPos(classChild);
                        }
                    }
                }

                prevParent = parent;//NOPMD
            }
        }

        return -1;
    }

    private static boolean isClassMember(final PsiElement element) {
        if (element == null) {
            return false;
        }
        final IElementType elementType = element.getNode().getElementType();
        return elementType.equals(PhpElementTypes.CLASS_FIELDS)
                    || elementType.equals(PhpElementTypes.CLASS_CONSTANTS)
                    || elementType.equals(PhpStubElementTypes.CLASS_METHOD);
    }

    private static int getNextPos(final PsiElement element) {
        final PsiElement next = element.getNextSibling();
        return next != null ? next.getTextOffset() : -1;//NOPMD
    }

    private static class MyMemberChooser extends MemberChooser<PhpNamedElementNode> {
        protected MyMemberChooser(
                final @NotNull PhpNamedElementNode[] nodes,
                final boolean allowEmptySelection,
                final @NotNull Project project) {
            super(nodes, allowEmptySelection, true, project, false);
        }
    }
}
