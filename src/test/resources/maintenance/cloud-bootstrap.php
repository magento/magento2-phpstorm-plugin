<?php
// Disposable test double for the installed Cloud patch pools. The selection comes from the environment.
return new class {
    public function get($class) {
        return new class($class) {
            public function __construct(private string $class) {}
            public function getQualityPatches() {
                return json_decode(getenv('QUALITY_PATCHES'), true, 512, JSON_THROW_ON_ERROR);
            }
            public function getList() {
                $ids = $this->class === Magento\CloudPatches\Patch\Pool\RequiredPool::class
                    ? ['CLOUD-1'] : ['Q-2', 'Q-DEP', 'Q-LEAF', 'OPTIONAL'];
                return array_map(static function ($id) {
                    return new class($id) {
                        public function __construct(private string $id) {}
                        public function getId() { return $this->id; }
                        public function getType() { return 'Optional'; }
                        public function getRequire() {
                            return ['Q-2' => ['Q-DEP'], 'Q-DEP' => ['Q-LEAF']][$this->id] ?? [];
                        }
                    };
                }, $ids);
            }
        };
    }
};
