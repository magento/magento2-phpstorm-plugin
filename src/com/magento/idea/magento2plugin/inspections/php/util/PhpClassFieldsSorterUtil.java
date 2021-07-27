/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.inspections.php.util;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.jetbrains.php.lang.psi.elements.impl.PhpClassFieldsListImpl;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public final class PhpClassFieldsSorterUtil {

    private PhpClassFieldsSorterUtil() {
    }

    /**
     * Get php class fields.
     *
     * @param clazz PhpClass
     *
     * @return List[Field]
     */
    public static List<Field> getClassFields(final @NotNull PhpClass clazz) {
        return fixFieldsOrder(new LinkedList<>(clazz.getFields()));
    }

    /**
     * Check if fields of the specified class sorted in the alphabetical order.
     *
     * @param clazz PhpClass
     *
     * @return boolean
     */
    public static boolean isSortedAlphabetically(final @NotNull PhpClass clazz) {
        return isSortedAlphabetically(getClassFields(clazz));
    }

    /**
     * Check if specified fields are sorted in the alphabetical order.
     *
     * @param fields List[Field]
     *
     * @return boolean
     */
    public static boolean isSortedAlphabetically(final List<Field> fields) {
        return fields.equals(sortFieldsAlphabetically(fields));
    }

    /**
     * Sort specified fields in the alphabetical order.
     *
     * @param fields List[Field]
     *
     * @return List[Field]
     */
    public static List<Field> sortFieldsAlphabetically(final @NotNull List<Field> fields) {
        return fields
                .stream()
                .sorted((field1, field2) -> {
                    final int access1 = field1.getModifier().getAccess().getLevel();
                    final int access2 = field2.getModifier().getAccess().getLevel();

                    if (access1 == access2) {
                        return Comparator.comparing(
                                PhpNamedElement::getName,
                                String.CASE_INSENSITIVE_ORDER
                        ).compare(field1, field2);
                    }

                    return access1 < access2 ? 1 : -1;
                })
                .collect(Collectors.toList());
    }

    /**
     * By default fields returned not in the order they exists in the class.
     *
     * @param fields List[Field]
     *
     * @return List[Field]
     */
    private static List<Field> fixFieldsOrder(final @NotNull List<Field> fields) {
        return fields
                .stream()
                .filter((field) -> field.getParent() instanceof PhpClassFieldsListImpl)
                .sorted(Comparator.comparing(PsiElement::getTextOffset))
                .collect(Collectors.toList());
    }
}
