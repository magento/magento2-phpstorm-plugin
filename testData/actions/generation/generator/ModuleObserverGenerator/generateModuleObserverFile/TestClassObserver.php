<?php

namespace Foo\Bar\Observer;

use Magento\Framework\Event\ObserverInterface;
use Magento\Framework\Event\Observer;

/**
 * Observes the `test_event_name` event.
 */
class TestClassObserver implements ObserverInterface
{
    /**
     * Observer for test_event_name.
     *
     * @param Observer $observer
     *
     * @return void
     */
    public function execute(Observer $observer)
    {
        $event = $observer->getEvent();
        // TODO: Implement observer method.
    }
}
