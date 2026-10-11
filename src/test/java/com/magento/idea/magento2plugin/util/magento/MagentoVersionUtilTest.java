/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento;

import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.json.psi.JsonObject;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.EdtTestUtil;
import com.magento.idea.magento2plugin.BaseProjectTestCase;
import com.magento.idea.magento2plugin.project.util.GetMagentoVersionUtil;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

public class MagentoVersionUtilTest extends BaseProjectTestCase {
    public void testSharedReaderDetectsTheBasePackageWithoutAssumingAnEdition() {
        final PsiFile lock = myFixture.addFileToProject("installed/composer.lock",
                "{\"packages\":[{\"name\":\"magento/magento2-base\",\"version\":\"2.4.8-p5\"}]}");
        final Pair<String, String> version = ApplicationManager.getApplication().runReadAction((Computable<Pair<String, String>>) () ->
                GetMagentoVersionUtil.getVersion(PsiTreeUtil.getChildOfType(lock, JsonObject.class)));
        assertNotNull(version);
        assertEquals("2.4.8-p5", version.getFirst());
        assertNull(version.getSecond());
    }

    public void testSharedReaderPrefersTheProductEditionOverTheBasePackage() {
        final PsiFile lock = myFixture.addFileToProject("installed/composer.lock",
                "{\"packages\":[{\"name\":\"magento/magento2-base\",\"version\":\"2.4.8-p5\"},"
                        + "{\"name\":\"magento/product-enterprise-edition\",\"version\":\"2.4.9\"}]}");
        final Pair<String, String> version = ApplicationManager.getApplication().runReadAction((Computable<Pair<String, String>>) () ->
                GetMagentoVersionUtil.getVersion(PsiTreeUtil.getChildOfType(lock, JsonObject.class)));
        assertNotNull(version);
        assertEquals("2.4.9", version.getFirst());
        assertEquals("Adobe Commerce", version.getSecond());
    }

    public void testGetVersionDataCanReadComposerLockOnEdtWithoutExplicitReadAction() throws Exception {
        final String magentoPath = Path.of(
                getTestDataPath(),
                "project",
                "magento2"
        ).toAbsolutePath().normalize().toString();

        final AtomicReference<Pair<String, String>> versionHolder = new AtomicReference<>();
        EdtTestUtil.runInEdtAndWait(() -> versionHolder.set(MagentoVersionUtil.getVersionData(
                getProject(),
                magentoPath
        )));

        assertNotNull(versionHolder.get());
        assertEquals("2.4.7", versionHolder.get().getFirst());
        assertEquals("Magento Open Source", versionHolder.get().getSecond());
    }
}
