<?php
use Magento\CloudPatches\Environment\Config;
use Magento\CloudPatches\Patch\Pool\OptionalPool;
use Magento\CloudPatches\Patch\Pool\RequiredPool;

$bootstrap = $argv[1] . '/magento/magento-cloud-patches/bootstrap.php';
$paths = glob('m2-hotfixes/*.patch');
if ($paths === false || (is_dir('m2-hotfixes') && !is_readable('m2-hotfixes'))) {
    throw new RuntimeException('Cannot inspect m2-hotfixes.');
}
$custom = array_map(static fn ($p) => 'm2-hotfixes/' . basename($p), $paths);
$environment = getenv('MAGENTO_CLOUD_ENVIRONMENT') ?: '';
if (!is_file($bootstrap)) {
    $cloud = file_exists('.magento.env.yaml') || file_exists('.magento.app.yaml')
        || getenv('MAGENTO_CLOUD_PROJECT') || $custom;
    echo json_encode(['available' => false, 'environment' => $environment, 'cloud' => (bool)$cloud, 'items' => $custom], JSON_THROW_ON_ERROR);
    return;
}
define('IS_CLOUD', true);
define('BP', getcwd());
$container = require $bootstrap;
$expected = $custom;
foreach ($container->get(RequiredPool::class)->getList() as $patch) {
    $expected[] = $patch->getId();
}
$selection = $container->get(Config::class)->getQualityPatches();
$optional = $container->get(OptionalPool::class);
$all = reset($selection) === '*';
if (!$all) {
    $expected = array_merge($expected, $selection);
}
// Retain selected IDs missing from this release. Expand transitive dependencies even when
// the installed pool cannot resolve every selected ID.
$dependencies = [];
foreach ($optional->getList() as $patch) {
    $id = $patch->getId();
    $dependencies[$id] = array_merge($dependencies[$id] ?? [], $patch->getRequire());
    if ($all && $patch->getType() === 'Optional') {
        $expected[] = $id;
    }
}
$seen = [];
while ($expected) {
    $id = array_pop($expected);
    if (!isset($seen[$id])) {
        $seen[$id] = true;
        foreach ($dependencies[$id] ?? [] as $dependency) {
            $expected[] = $dependency;
        }
    }
}
echo json_encode(['available' => true, 'environment' => $environment, 'items' => array_keys($seen)], JSON_THROW_ON_ERROR);
