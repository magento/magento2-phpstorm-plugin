/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */
package com.magento.idea.magento2uct.versioning;

/** Version information consumed by both editor and batch inspections. */
public interface UctVersionState {
    boolean isPresentInCodebase(String fqn);
    boolean isDeprecated(String fqn);
    String getDeprecatedInVersion(String fqn);
    boolean isExists(String fqn);
    String getRemovedInVersion(String fqn);
    boolean isApi(String fqn);
}
