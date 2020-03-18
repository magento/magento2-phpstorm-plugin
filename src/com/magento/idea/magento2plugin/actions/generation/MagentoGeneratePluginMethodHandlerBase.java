/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2plugin.actions.generation;

import com.intellij.application.options.CodeStyle;
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
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.php.codeInsight.PhpCodeInsightUtil;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.actions.PhpNamedElementNode;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.parser.PhpStubElementTypes;
import com.jetbrains.php.lang.psi.PhpCodeEditUtil;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.*;
import java.util.*;
import com.magento.idea.magento2plugin.actions.generation.ImportReferences.PhpClassReferenceResolver;
import com.magento.idea.magento2plugin.actions.generation.data.MagentoPluginMethodData;
import com.magento.idea.magento2plugin.util.GetPhpClassByFQN;
import com.magento.idea.magento2plugin.util.magento.plugin.GetTargetClassNamesByPluginClassName;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class MagentoGeneratePluginMethodHandlerBase implements LanguageCodeInsightActionHandler {
    public String type;

    public MagentoGeneratePluginMethodHandlerBase(MagentoPluginMethodData.Type type) {
        this.type = type.toString();
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

    public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        PhpFile phpFile = (PhpFile)file;
        PhpClass currentClass = PhpCodeEditUtil.findClassAtCaret(editor, phpFile);
        Key<Object> targetClassKey = Key.create("original.target");
        if (currentClass == null) {
            return;
        }
        PhpNamedElementNode[] fieldsToShow = this.targetMethods(currentClass, targetClassKey);
        if (fieldsToShow.length == 0) {
            if (ApplicationManager.getApplication().isHeadlessEnvironment()) {
                return;
            }
            HintManager.getInstance().showErrorHint(editor, this.getErrorMessage());
            return;
        }
        PhpNamedElementNode[] members = this.chooseMembers(fieldsToShow, true, file.getProject());
        if (members == null || members.length == 0) {
            return;
        }
        int insertPos = getSuitableEditorPosition(editor, phpFile);
        CommonCodeStyleSettings settings = CodeStyle.getLanguageSettings(file, PhpLanguage.INSTANCE);
        boolean currLineBreaks = settings.KEEP_LINE_BREAKS;
        int currBlankLines = settings.KEEP_BLANK_LINES_IN_CODE;
        settings.KEEP_LINE_BREAKS = false;
        settings.KEEP_BLANK_LINES_IN_CODE = 0;
        ApplicationManager.getApplication().runWriteAction(() -> {
            Set<CharSequence> insertedMethodsNames = new THashSet();
            PhpClassReferenceResolver resolver = new PhpClassReferenceResolver();
            StringBuffer textBuf = new StringBuffer();
            PhpPsiElement scope = PhpCodeInsightUtil.findScopeForUseOperator(currentClass);

            for (PhpNamedElementNode member : members) {
                PsiElement method = member.getPsiElement();
                MagentoPluginMethodData[] pluginMethods = this.createPluginMethods(currentClass, (Method) method, targetClassKey);
                for (MagentoPluginMethodData pluginMethod : pluginMethods) {
                    insertedMethodsNames.add(pluginMethod.getMethod().getName());
                    PhpDocComment comment = pluginMethod.getDocComment();
                    if (comment != null) {
                        textBuf.append(comment.getText());
                    }
                    Method targetMethod = pluginMethod.getTargetMethod();
                    Parameter[] parameters = targetMethod.getParameters();
                    Collection<PsiElement> processElements = new ArrayList<>(Arrays.asList(parameters));
                    resolver.processElements(processElements);
                    PsiElement targetClass = (PsiElement) pluginMethod.getTargetMethod().getUserData(targetClassKey);
                    resolver.processElement(targetClass);
                    PhpReturnType returnType = targetMethod.getReturnType();
                    if (returnType != null) {
                        resolver.processElement(returnType);
                    }

                    textBuf.append('\n');
                    textBuf.append(pluginMethod.getMethod().getText());
                }
            }

            if (textBuf.length() > 0 && insertPos >= 0) {
                editor.getDocument().insertString(insertPos, textBuf);
                int endPos = insertPos + textBuf.length();
                CodeStyleManager.getInstance(project).reformatText(phpFile, insertPos, endPos);
                PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
            }
            if (!insertedMethodsNames.isEmpty()) {
                List<PsiElement> insertedMethods = collectInsertedMethods(file, currentClass.getNameCS(), insertedMethodsNames);
                if (scope != null && insertedMethods != null) {
                    resolver.importReferences(scope, insertedMethods);
                }
            }
        });
        settings.KEEP_LINE_BREAKS = currLineBreaks;
        settings.KEEP_BLANK_LINES_IN_CODE = currBlankLines;
    }

    @Nullable
    private static List<PsiElement> collectInsertedMethods(@NotNull PsiFile file, @NotNull CharSequence className, @NotNull Set<CharSequence> methodNames) {
        if (!(file instanceof PhpFile)) {
            return null;
        }
        PhpClass phpClass = PhpPsiUtil.findClass((PhpFile) file, (aClass) -> PhpLangUtil.equalsClassNames(aClass.getNameCS(), className));
        if (phpClass == null) {
            return null;
        } else {
            List<PsiElement> insertedMethods = new ArrayList();
            Method[] ownMethods = phpClass.getOwnMethods();

            for (Method method : ownMethods) {
                if (methodNames.contains(method.getNameCS())) {
                    insertedMethods.add(method);
                }
            }

            return insertedMethods;
        }
    }

    protected abstract MagentoPluginMethodData[] createPluginMethods(PhpClass currentClass, Method method, Key<Object> targetClassKey);

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
            MagentoGeneratePluginMethodHandlerBase.MyMemberChooser chooser = new MagentoGeneratePluginMethodHandlerBase.MyMemberChooser(nodes, allowEmptySelection, project);
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
            if (targetClass.isFinal()) {
                continue;
            }
            Collection<Method> methods = targetClass.getMethods();
            Iterator methodIterator = methods.iterator();

            while(methodIterator.hasNext()) {
                Method method = (Method) methodIterator.next();
                if (method.getAccess().isPublic() && !method.isStatic() && !method.isFinal() && !pluginAlreadyHasMethod(phpClass, method)) {
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
        String methodPrefix = type.toLowerCase();
        String methodSuffix = methodName.substring(0, 1).toUpperCase() + methodName.substring(1);
        String pluginMethodName = methodPrefix.concat(methodSuffix);
        for (Method currentMethod: currentMethods) {
            if(!currentMethod.getName().equals(pluginMethodName)) {
                continue;
            }
            return true;
        }
        return false;
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

            for(PsiElement prevParent = currElement; parent != null && !(parent instanceof PhpFile); parent = parent.getParent()) {
                if (isClassMember(parent)) {
                    return getNextPos(parent);
                }

                if (parent instanceof PhpClass) {
                    while(prevParent != null) {
                        if (isClassMember(prevParent) || PhpPsiUtil.isOfType(prevParent, PhpTokenTypes.chLBRACE)) {
                            return getNextPos(prevParent);
                        }

                        prevParent = prevParent.getPrevSibling();
                    }

                    for(PsiElement classChild = parent.getFirstChild(); classChild != null; classChild = classChild.getNextSibling()) {
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

    private static class MyMemberChooser extends MemberChooser<PhpNamedElementNode> {
        protected MyMemberChooser(@NotNull PhpNamedElementNode[] nodes, boolean allowEmptySelection, @NotNull Project project) {
            super(nodes, allowEmptySelection, true, project, false);
        }
    }
}
