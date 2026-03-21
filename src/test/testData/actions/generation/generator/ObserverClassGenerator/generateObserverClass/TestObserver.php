<?php


namespace Foo\Bar\Observer;

use Magento\Framework\Event\ObserverInterface;
use Magento\Framework\Event\Observer;

class TestObserver implements ObserverInterface
{
    /**
     * Observer for test_event
     *
     * @param Observer $observer
     * @return void
     */
    public function execute(Observer $observer)
    {
        $event = $observer->getEvent();
        // TODO: Implement observer method.
    }
}
