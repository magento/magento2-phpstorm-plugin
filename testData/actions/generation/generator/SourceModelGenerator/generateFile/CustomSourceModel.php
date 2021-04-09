<?php

namespace Foo\Bar\Setup\Patch\Data;

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
        // TODO: Implement getAllOptions() method.
    }
}
