<?php

namespace Foo\Bar\Mapper;

use Foo\Bar\Api\Data\UnicornDataInterface;
use Foo\Bar\Api\Data\UnicornDataInterfaceFactory;
use Foo\Bar\Model\UnicornModel;
use Magento\Framework\DataObject;
use Magento\Framework\Model\ResourceModel\Db\Collection\AbstractCollection;

/**
 * Converts a collection of Unicorn entities to an array of data transfer objects.
 */
class UnicornDataMapper
{
    /**
     * @var UnicornDataInterfaceFactory
     */
    private UnicornDataInterfaceFactory $entityDtoFactory;

    /**
     * @param UnicornDataInterfaceFactory $entityDtoFactory
     */
    public function __construct(
        UnicornDataInterfaceFactory $entityDtoFactory
    )
    {
        $this->entityDtoFactory = $entityDtoFactory;
    }

    /**
     * Map magento models to DTO array.
     *
     * @param AbstractCollection $collection
     *
     * @return array|UnicornDataInterface[]
     */
    public function map(AbstractCollection $collection): array
    {
        $results = [];
        /** @var UnicornModel $item */
        foreach ($collection->getItems() as $item) {
            /** @var UnicornDataInterface|DataObject $entityDto */
            $entityDto = $this->entityDtoFactory->create();
            $entityDto->addData($item->getData());

            $results[] = $entityDto;
        }

        return $results;
    }
}
