/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2uct.analysis

import com.intellij.psi.PsiFile
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocMethod
import com.jetbrains.php.lang.psi.elements.PhpAttributesOwner
import com.jetbrains.php.lang.psi.elements.PhpNamedElement
import com.magento.idea.magento2uct.versioning.processors.PhpClassLocatorUtil

/**
 * Deprecation snapshot of a release archive that is not part of the project. PhpNamedElement.isDeprecated()
 * resolves @inheritdoc parents through project indexes, so it fails while indexing and reads the installed
 * classes instead of the release; @inheritdoc is resolved here against parents in the same archive.
 */
internal class UctReleaseDeprecations {
    private val deprecated = hashSetOf<String>()
    private val documented = hashSetOf<String>()
    private val inheritsDoc = linkedSetOf<String>()
    private val parents = hashMapOf<String, List<String>>()

    fun process(file: PsiFile) {
        val phpClass = PhpClassLocatorUtil.locate(file) ?: return
        val classFqn = phpClass.fqn
        parents[classFqn] = (phpClass.extendsList.referenceElements + phpClass.implementsList.referenceElements).mapNotNull { it.fqn }
        if (isOwnDeprecated(phpClass)) deprecated += classFqn
        phpClass.ownFields.filter(::isOwnDeprecated).forEach { deprecated += it.fqn }
        for (method in phpClass.ownMethods) {
            val doc = method.docComment?.text
            when {
                isOwnDeprecated(method) -> deprecated += method.fqn
                doc == null -> {}
                doc.contains(INHERIT_DOC, ignoreCase = true) -> inheritsDoc += method.fqn
                else -> documented += method.fqn
            }
        }
    }

    fun snapshot(): Map<String, Boolean> =
        (deprecated + inheritsDoc.filter { inheritedDeprecation(it.substringBeforeLast('.'), it.substringAfterLast('.')) })
            .associateWith { true }

    /** The first parent declaration with its own documentation decides; undocumented and @inheritdoc ones are skipped. */
    private fun inheritedDeprecation(classFqn: String, name: String): Boolean {
        val queue = ArrayDeque(parents[classFqn].orEmpty())
        val visited = hashSetOf(classFqn)
        while (queue.isNotEmpty()) {
            val parent = queue.removeFirst()
            if (!visited.add(parent)) continue
            val method = "$parent.$name"
            when (method) {
                in deprecated -> return true
                in documented -> return false
                else -> queue.addAll(parents[parent].orEmpty())
            }
        }
        return false
    }

    /** A @method declaration has no markers of its own; its doc comment is the class's. */
    private fun isOwnDeprecated(element: PhpNamedElement): Boolean = element !is PhpDocMethod && (
        element.docComment?.getTagElementsByName(DEPRECATED_TAG)?.isNotEmpty() == true ||
            (element as? PhpAttributesOwner)?.getAttributes(DEPRECATED_ATTRIBUTE)?.isNotEmpty() == true
    )

    private companion object {
        const val INHERIT_DOC = "@inheritdoc"
        const val DEPRECATED_TAG = "@deprecated"
        const val DEPRECATED_ATTRIBUTE = "\\Deprecated"
    }
}
