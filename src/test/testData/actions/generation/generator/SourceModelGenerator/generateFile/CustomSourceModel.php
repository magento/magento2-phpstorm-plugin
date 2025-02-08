<?php

namespace Foo\Bar\Model\Source;

use Magento\Eav\Model\Entity\Attribute\Source\AbstractSource;

class CustomSourceModel extends AbstractSource
{
    /**
     * Retrieve All options
     *
     * @return array
     */
    public function getAllOptions(): array
    {
        return [];
    }
}
