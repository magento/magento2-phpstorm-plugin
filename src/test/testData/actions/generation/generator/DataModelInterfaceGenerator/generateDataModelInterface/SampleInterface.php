<?php

namespace Foo\Bar\Api\Data;

interface SampleInterface
{
    /**
     * String constants for property names
     */
    public const ID_PROPERTY = "id_property";
    public const SAMPLE_PROPERTY = "sample_property";

    /**
     * Getter for IdProperty.
     *
     * @return int|null
     */
    public function getIdProperty(): ?int;

    /**
     * Setter for IdProperty.
     *
     * @param int|null $idProperty
     *
     * @return void
     */
    public function setIdProperty(?int $idProperty): void;

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
