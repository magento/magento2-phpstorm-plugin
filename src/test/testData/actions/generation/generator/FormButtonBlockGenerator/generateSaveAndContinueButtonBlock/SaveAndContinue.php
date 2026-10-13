<?php

namespace Foo\Bar\Block\Form\Book;

use Magento\Framework\View\Element\UiComponent\Control\ButtonProviderInterface;

/**
 * Save entity and continue edit button.
 */
class SaveAndContinue extends GenericButton implements ButtonProviderInterface
{
    /**
     * Retrieve Save and Continue Edit button settings.
     *
     * @return array
     */
    public function getButtonData(): array
    {
        return $this->wrapButtonSettings(
            __('Save and Continue Edit')->getText(),
            'save',
            '',
            [
                'mage-init' => ['button' => ['event' => 'saveAndContinueEdit']]
            ],
            25
        );
    }
}
