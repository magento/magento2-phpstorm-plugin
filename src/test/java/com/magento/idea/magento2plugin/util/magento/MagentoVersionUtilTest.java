/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.util.magento;

import com.intellij.openapi.util.Pair;
import com.intellij.testFramework.EdtTestUtil;
import com.magento.idea.magento2plugin.BaseProjectTestCase;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

public class MagentoVersionUtilTest extends BaseProjectTestCase {
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
