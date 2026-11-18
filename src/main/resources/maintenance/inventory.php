<?php
// Return only identity fields. Composer repositories and download URLs can contain credentials.
function readManifest($path) {
    if (!is_readable($path) || filesize($path) > 64 * 1024 * 1024) {
        throw new RuntimeException('Unreadable or oversized Composer manifest.');
    }
    return json_decode(file_get_contents($path), true, 512, JSON_THROW_ON_ERROR);
}
function records($packages) {
    if (!is_array($packages) || array_values($packages) !== $packages) {
        throw new RuntimeException('Invalid Composer package list.');
    }
    return array_map(static function ($p) {
        if (!is_string($p['name'] ?? null) || !is_string($p['version'] ?? null)) {
            throw new RuntimeException('Incomplete package record.');
        }
        return [
            'name' => $p['name'], 'version' => $p['version'],
            'source' => ['reference' => $p['source']['reference'] ?? null],
            'dist' => ['reference' => $p['dist']['reference'] ?? null]
        ];
    }, $packages);
}
$vendor = $argv[1];
$lock = readManifest('composer.lock');
$path = $vendor . '/composer/installed.json';
foreach (['autoload.php', 'composer/installed.json'] as $required) {
    if (!file_exists($vendor . '/' . $required)) {
        if ((is_dir($vendor) && !is_readable($vendor)) ||
            (is_dir(dirname($path)) && !is_readable(dirname($path)))) {
            throw new RuntimeException('Cannot inspect the vendor directory.');
        }
        echo json_encode(['missing' => true, 'missingFile' => $required], JSON_THROW_ON_ERROR);
        return;
    }
}
$installed = readManifest($path);
echo json_encode([
    'locked' => records($lock['packages']), 'lockedDev' => records($lock['packages-dev'] ?? []),
    'installed' => records($installed['packages'] ?? $installed)
], JSON_THROW_ON_ERROR);
