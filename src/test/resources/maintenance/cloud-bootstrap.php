<?php
namespace Magento\CloudPatches\Patch\Collector {
    class SupportCollector {
        const ORIGIN = 'Adobe Commerce Support';
    }
}

namespace {
    use Magento\CloudPatches\Patch\Collector\SupportCollector;
    use Magento\CloudPatches\Patch\Pool\RequiredPool;

    // Disposable test double for the installed Cloud patch pools.
    class FixturePatch {
        public function __construct(
            private string $id,
            private string $origin = SupportCollector::ORIGIN,
            private bool $deprecated = false,
            private array $requires = []
        ) {}

        public function getId() { return $this->id; }
        public function getType() { return 'Optional'; }
        public function getOrigin() { return $this->origin; }
        public function isDeprecated() { return $this->deprecated; }
        public function getRequire() { return $this->requires; }
    }

    return new class {
        public function get($class) {
            return new class($class) {
                public function __construct(private string $class) {}

                public function getQualityPatches() {
                    return json_decode(getenv('QUALITY_PATCHES'), true, 512, JSON_THROW_ON_ERROR);
                }

                public function getList() {
                    if ($this->class === RequiredPool::class) {
                        return [new FixturePatch('CLOUD-1')];
                    }
                    return [
                        new FixturePatch('Q-2', requires: ['Q-DEP']),
                        new FixturePatch('Q-DEP', 'Magento Open Source Community', true, ['Q-LEAF']),
                        new FixturePatch('Q-LEAF', 'Magento Open Source Community'),
                        new FixturePatch('DEPRECATED', deprecated: true),
                        new FixturePatch('OPTIONAL', 'Magento Open Source Community'),
                    ];
                }

                public function getOptionalListByOrigin(array $origins): array {
                    $byId = [];
                    foreach ($this->getList() as $patch) {
                        $byId[$patch->getId()] = $patch;
                    }
                    $visit = static function (FixturePatch $patch) use (&$visit, $byId): array {
                        $result = [];
                        foreach ($patch->getRequire() as $id) {
                            $result = array_merge($result, $visit($byId[$id]));
                        }
                        return [...$result, $patch];
                    };
                    $result = [];
                    foreach ($byId as $patch) {
                        if (in_array($patch->getOrigin(), $origins, true)) {
                            $result = array_merge($result, $visit($patch));
                        }
                    }
                    return $result;
                }
            };
        }
    };
}
