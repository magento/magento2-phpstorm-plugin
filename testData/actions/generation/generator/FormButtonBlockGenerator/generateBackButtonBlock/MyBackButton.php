<?php


namespace Foo\Bar\Block\Form;

use Magento\Framework\View\Element\UiComponent\Control\ButtonProviderInterface;

/**
 * @inheritdoc
 */
class MyBackButton implements ButtonProviderInterface
{
    /**
     * @inheritDoc
     */
    public function getButtonData()
    {
        return ['label' => __('Back Button'), 'on_click' => sprintf("location.href = '%s';", $this->getUrl('*/*/')), 'class' => 'back', 'sort_order' => 20];
    }
}
