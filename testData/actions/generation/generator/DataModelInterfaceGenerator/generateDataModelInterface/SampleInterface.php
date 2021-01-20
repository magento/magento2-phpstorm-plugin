<?php
declare(strict_types=1);

namespace Foo\Bar\Api\Data;

interface SampleInterface
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
    public function getSampleProperty(): ?string;

    /**
     * Setter for SampleProperty.
     *
     * @param string|null $sampleProperty
     *
     * @return void
     */
    public function setSampleProperty(?string $sampleProperty): void;
}
