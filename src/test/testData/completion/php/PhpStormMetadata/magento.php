<?php
/*
 * Minimal API declarations for metadata tests. Empty bodies deliberately prevent
 * PhpStorm from inferring the expected result from the implementation.
 * Signatures/constants checked against magento/magento2 tag 2.4.9.
 */

namespace Magento\Framework {
    interface ObjectManagerInterface {
        public function get($type);
        public function create($type, array $arguments = []);
    }
    class Filesystem {
        public function getDirectoryRead($directoryCode, $driverCode = 'file') {}
        public function getDirectoryWrite($directoryCode, $driverCode = 'file') {}
    }
}

namespace Magento\Framework\Validator {
    class Builder {}
    class UniversalFactory {
        /** @return Builder */
        public function create($className, array $arguments = []) {}
    }
}

namespace Magento\Framework\TestFramework\Unit\Helper {
    class ObjectManager {
        /** @return object */
        public function getObject($className, array $arguments = []) {}
    }
}

namespace Magento\Framework\Message {
    interface MessageInterface {
        const TYPE_ERROR = 'error';
        const TYPE_WARNING = 'warning';
        const TYPE_NOTICE = 'notice';
        const TYPE_SUCCESS = 'success';
    }
    class Error implements MessageInterface {}
    class Warning implements MessageInterface {}
    class Notice implements MessageInterface {}
    class Success implements MessageInterface {}
    class Factory {
        /** @return MessageInterface */
        public function create($type, $text = null) {}
    }
}

namespace Magento\Framework\Api {
    class SearchCriteriaBuilder {
        public function addFilter($field, $value, $conditionType = 'eq') {}
    }
    class FilterBuilder {
        public function setConditionType($conditionType) {}
    }
    class SortOrder {
        const SORT_ASC = 'ASC';
        const SORT_DESC = 'DESC';
        public function setDirection($direction) {}
    }
}

namespace Magento\Framework\App\Config {
    interface ScopeConfigInterface {
        const SCOPE_TYPE_DEFAULT = 'default';
        public function getValue($path = null, $scopeType = 'default', $scopeCode = null);
        public function isSetFlag($path, $scopeType = 'default', $scopeCode = null);
    }
}

namespace Magento\Store\Model {
    interface ScopeInterface {
        const SCOPE_STORE = 'store';
        const SCOPE_STORES = 'stores';
        const SCOPE_WEBSITE = 'website';
        const SCOPE_WEBSITES = 'websites';
    }
}

namespace Magento\Framework\App {
    class Area {
        const AREA_GLOBAL = 'global';
        const AREA_FRONTEND = 'frontend';
        const AREA_ADMINHTML = 'adminhtml';
        const AREA_DOC = 'doc';
        const AREA_CRONTAB = 'crontab';
        const AREA_WEBAPI_REST = 'webapi_rest';
        const AREA_WEBAPI_SOAP = 'webapi_soap';
        const AREA_GRAPHQL = 'graphql';
    }
    class State {
        public function setAreaCode($code) {}
        public function emulateAreaCode($areaCode, $callback, $params = []) {}
    }
}

namespace Magento\Framework\App\Filesystem {
    class DirectoryList {
        const ROOT = 'base';
        const APP = 'app';
        const CONFIG = 'etc';
        const LIB_INTERNAL = 'lib_internal';
        const LIB_WEB = 'lib_web';
        const PUB = 'pub';
        const MEDIA = 'media';
        const STATIC_VIEW = 'static';
        const VAR_DIR = 'var';
        const VAR_EXPORT = 'var_export';
        const VAR_IMPORT_EXPORT = 'import_export';
        const TMP = 'tmp';
        const CACHE = 'cache';
        const LOG = 'log';
        const SESSION = 'session';
        const SETUP = 'setup';
        const DI = 'di';
        const GENERATION = 'generation';
        const UPLOAD = 'upload';
        const COMPOSER_HOME = 'composer_home';
        const TMP_MATERIALIZATION_DIR = 'view_preprocessed';
        const TEMPLATE_MINIFICATION_DIR = 'html';
        const GENERATED = 'generated';
        const GENERATED_CODE = 'code';
        const GENERATED_METADATA = 'metadata';
    }
}

namespace Example {
    class ViewModel {}
    class Collection {}
}
