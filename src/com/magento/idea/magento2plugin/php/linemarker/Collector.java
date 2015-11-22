package com.magento.idea.magento2plugin.php.linemarker;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by dkvashnin on 11/20/15.
 */
interface Collector<T extends PsiElement, K> {
    List<K> collect(@NotNull T psiElement);
}
