<?php
declare(strict_types=1);

namespace Foo\Bar\Model\Data;

use Magento\Framework\DataObject;

class Sample extends DataObject
{
    /**
     * String constants for property names
     */
    const SAMPLE_PROPERTY = "sample_property";

    /**
     * Getter for SampleProperty.
     *
     * @return string|null
     */
    public function getSampleProperty(): ?string
    {
        return $this->getData(self::SAMPLE_PROPERTY) === null ? null
            : (string)$this->getData(self::SAMPLE_PROPERTY);
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
