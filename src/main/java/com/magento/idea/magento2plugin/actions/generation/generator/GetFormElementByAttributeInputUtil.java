/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 *
 */

package com.magento.idea.magento2plugin.actions.generation.generator;

import com.magento.idea.magento2plugin.magento.packages.eav.AttributeInput;
import com.magento.idea.magento2plugin.magento.packages.uicomponent.FormElementType;

public final class GetFormElementByAttributeInputUtil {

    private GetFormElementByAttributeInputUtil(){}

    /**
     * Returns for available form element by attribute input type.
     *
     * @param inputType AttributeInput
     * @return String
     */
    public static String execute(final AttributeInput inputType) {
        switch (inputType) {
            case TEXT:
                return FormElementType.INPUT.getType();
            case TEXTAREA:
                return FormElementType.TEXTAREA.getType();
            case BOOLEAN:
            case SELECT:
                return FormElementType.SELECT.getType();
            case MULTISELECT:
                return FormElementType.MULTISELECT.getType();
            case DATE:
                return FormElementType.DATE.getType();
            case PRICE:
                return FormElementType.PRICE.getType();
            case HIDDEN:
                return FormElementType.HIDDEN.getType();
            default:
                return "";
        }
    }
}
