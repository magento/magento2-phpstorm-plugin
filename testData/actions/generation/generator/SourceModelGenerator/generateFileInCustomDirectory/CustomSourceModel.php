<?php

namespace Foo\Bar\Custom\Source\Directory;

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
