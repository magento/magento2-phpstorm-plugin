<?php

namespace Foo\Bar\Block\Form\Book;

use Magento\Framework\View\Element\UiComponent\Control\ButtonProviderInterface;

/**
 * Custom button.
 */
class MyCustom extends GenericButton implements ButtonProviderInterface
{
    /**
     * Retrieve Custom Button button settings.
     *
     * @return array
     */
    public function getButtonData(): array
    {
        return $this->wrapButtonSettings(
            __('Custom Button')->getText(),
            'custom',
            '',
            [],
            0
        );
    }
}
