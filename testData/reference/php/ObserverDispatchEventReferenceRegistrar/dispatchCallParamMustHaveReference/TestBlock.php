<?php
/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

namespace Magento\Catalog\Block;

class TestBlock extends \Magento\Framework\View\Element\Template implements
    \Magento\Framework\DataObject\IdentityInterface
{
    public function someMethod()
    {
        $this->_eventManager->dispatch('test_event_in_test_class<caret>', ['response_object' => "test"]);
    }
}
