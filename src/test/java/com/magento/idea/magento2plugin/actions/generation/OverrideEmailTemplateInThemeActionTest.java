/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation;

import com.intellij.psi.PsiFile;
import com.magento.idea.magento2plugin.BaseProjectTestCase;

public class OverrideEmailTemplateInThemeActionTest extends BaseProjectTestCase {
    public void testFrontendEmailIsAllowed() {
        assertAllowed("view/frontend/email/shipping.html", true);
    }

    public void testNestedAdminEmailIsAllowed() {
        assertAllowed("view/adminhtml/email/order/shipping.html", true);
    }

    public void testBaseEmailIsAllowed() {
        assertAllowed("view/base/email/shipping.html", true);
    }

    public void testRegularHtmlTemplateIsNotAllowed() {
        assertAllowed("view/frontend/web/template/shipping.html", false);
    }

    public void testPhpTemplateIsNotAllowed() {
        assertAllowed("view/frontend/email/shipping.phtml", false);
    }

    public void testUnrelatedEmailDirectoryIsNotAllowed() {
        assertAllowed("frontend/email/shipping.html", false);
    }

    public void testUnsupportedAreaIsNotAllowed() {
        assertAllowed("view/graphql/email/shipping.html", false);
    }

    public void testHtmlOutsideModuleIsNotAllowed() {
        final PsiFile file = myFixture.addFileToProject("misc/view/frontend/email/shipping.html", "<p>Hello</p>");
        assertFalse(new OverrideEmailTemplateInThemeAction().isOverrideAllowed(file, getProject()));
    }

    private void assertAllowed(final String relativePath, final boolean expected) {
        // Resolve the module through module.xml, without registration.php or PHP PSI.
        myFixture.addFileToProject(
                "vendor/example/email-module/etc/module.xml",
                "<config><module name=\"Example_Email\"/></config>"
        );
        final PsiFile file = myFixture.addFileToProject(
                "vendor/example/email-module/" + relativePath, "<p>Hello {{var name}}</p>"
        );
        assertEquals(expected, new OverrideEmailTemplateInThemeAction().isOverrideAllowed(file, getProject()));
    }
}
