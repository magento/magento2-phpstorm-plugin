<?php
/**
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

namespace Magento\Catalog\Block;

class Navigation extends \Magento\Framework\View\Element\Template implements
    \Magento\Framework\DataObject\IdentityInterface
{
    public function someMethod()
    {
        $this->_eventManager->dispatch('test_event_in_block', ['response_object' => "test"]);
    }
}
