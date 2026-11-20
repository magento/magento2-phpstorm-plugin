<?php
require 'app/bootstrap.php';
$bootstrap = Magento\Framework\App\Bootstrap::create(BP, $_SERVER);
$types = $bootstrap->getObjectManager()->get(Magento\Framework\App\Cache\TypeListInterface::class);
echo json_encode(array_keys($types->getInvalidated()), JSON_THROW_ON_ERROR);
