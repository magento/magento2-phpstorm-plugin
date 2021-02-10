<?php

namespace Foo\Bar\Model\Data;

use Foo\Bar\Api\Data\SampleInterface;
use Magento\Framework\DataObject;

class Sample extends DataObject implements SampleInterface
{
    /**
     * @inheritDoc
     */
    public function getIdProperty(): ?int
    {
        return $this->getData(self::ID_PROPERTY) === null ? null
            : (int)$this->getData(self::ID_PROPERTY);
    }

    /**
     * @inheritDoc
     */
    public function setIdProperty(?int $idProperty): void
    {
        $this->setData(self::ID_PROPERTY, $idProperty);
    }

    /**
     * @inheritDoc
     */
    public function getSampleProperty(): ?string
    {
        return $this->getData(self::SAMPLE_PROPERTY);
    }

    /**
     * @inheritDoc
     */
    public function setSampleProperty(?string $sampleProperty): void
    {
        $this->setData(self::SAMPLE_PROPERTY, $sampleProperty);
    }
}
