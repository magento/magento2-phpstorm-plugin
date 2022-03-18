<?php

namespace Foo\Bar\Model\Data;

use Foo\Bar\Api\Data\SampleInterface;
use Magento\Framework\DataObject;

class Sample extends DataObject implements SampleInterface
{
    /**
     * Getter for IdProperty.
     *
     * @return int|null
     */
    public function getIdProperty(): ?int
    {
        return $this->getData(self::ID_PROPERTY) === null ? null
            : (int)$this->getData(self::ID_PROPERTY);
    }

    /**
     * Setter for IdProperty.
     *
     * @param int|null $idProperty
     *
     * @return void
     */
    public function setIdProperty(?int $idProperty): void
    {
        $this->setData(self::ID_PROPERTY, $idProperty);
    }

    /**
     * Getter for SampleProperty.
     *
     * @return string|null
     */
    public function getSampleProperty(): ?string
    {
        return $this->getData(self::SAMPLE_PROPERTY);
    }

    /**
     * Setter for SampleProperty.
     *
     * @param string|null $sampleProperty
     *
     * @return void
     */
    public function setSampleProperty(?string $sampleProperty): void
    {
        $this->setData(self::SAMPLE_PROPERTY, $sampleProperty);
    }
}
