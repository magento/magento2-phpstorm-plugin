<?php


namespace Foo\Bar\Block\Form;

use Magento\Framework\View\Element\UiComponent\Control\ButtonProviderInterface;

/**
 * @inheritdoc
 */
class SaveBlock implements ButtonProviderInterface
{
    /**
     * @inheritDoc
     */
    public function getButtonData()
    {
        return ['label' => __('Save Entity'), 'class' => 'save primary', 'on_click' => '', 'data_attribute' => ['mage-init' => ['Magento_Ui/js/form/button-adapter' => ['actions' => [['targetName' => 'my_form.my_form', 'actionName' => 'save', 'params' => [true, //TODO: adjust entity ID
            ['entity_id' => ''],]]]]]]];
    }
}
