/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.context;

import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolderBase;

public class EntityCreatorContext extends UserDataHolderBase implements GenerationContext {

    public static final Key<String> DTO_TYPE = Key.create("DTO_TYPE");
    public static final Key<String> ENTITY_ID = Key.create("ENTITY_ID");
}
