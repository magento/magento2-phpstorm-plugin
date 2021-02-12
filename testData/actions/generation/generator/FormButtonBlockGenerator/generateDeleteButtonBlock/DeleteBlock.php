<?php


namespace Foo\Bar\Block\Form;

use Magento\Framework\View\Element\UiComponent\Control\ButtonProviderInterface;

/**
 * @inheritdoc
 */
class DeleteBlock implements ButtonProviderInterface
{
    /**
     * @inheritDoc
     */
    public function getButtonData()
    {
        return ['label' => __('Delete Entity'), 'class' => 'delete', 'on_click' => 'deleteConfirm(\'' . __('Are you sure you want to do this?') . '\', \'' . //TODO: adjust entity ID
            $this->getUrl('*/*/${BUTTON_ROUTE}', ['entity_id' => '']) . '\', {"data": {}})', 'sort_order' => 30];
    }
}
