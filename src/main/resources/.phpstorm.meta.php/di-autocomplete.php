<?php

declare(strict_types=1);

namespace PHPSTORM_META {

    override(\Magento\Framework\ObjectManagerInterface::get(0), map(['' => '@']));
    override(\Magento\Framework\ObjectManagerInterface::create(0), map(['' => '@']));

    override(\Magento\Framework\View\LayoutInterface::createBlock(0), map(['' => '@']));
    override(\Magento\Framework\View\TemplateEngine\Php::helper(0), map(['' => '@']));
    override(\Magento\Framework\Validator\UniversalFactory::create(0), map(['' => '@']));
    override(
        \Magento\Framework\TestFramework\Unit\Helper\ObjectManager::getObject(0),
        map(['' => '@'])
    );

    override(
        \Magento\Framework\Controller\ResultFactory::create(0),
        map(
            [
                \Magento\Framework\Controller\ResultFactory::TYPE_FORWARD => \Magento\Framework\Controller\Result\Forward::class,
                \Magento\Framework\Controller\ResultFactory::TYPE_JSON => \Magento\Framework\Controller\Result\Json::class,
                \Magento\Framework\Controller\ResultFactory::TYPE_LAYOUT => \Magento\Framework\View\Result\Layout::class,
                \Magento\Framework\Controller\ResultFactory::TYPE_PAGE => \Magento\Framework\View\Result\Page::class,
                \Magento\Framework\Controller\ResultFactory::TYPE_RAW => \Magento\Framework\Controller\Result\Raw::class,
                \Magento\Framework\Controller\ResultFactory::TYPE_REDIRECT => \Magento\Framework\Controller\Result\Redirect::class,
            ]
        )
    );

    expectedArguments(
        \Magento\Framework\Controller\ResultFactory::create(),
        0,
        \Magento\Framework\Controller\ResultFactory::TYPE_FORWARD,
        \Magento\Framework\Controller\ResultFactory::TYPE_JSON,
        \Magento\Framework\Controller\ResultFactory::TYPE_LAYOUT,
        \Magento\Framework\Controller\ResultFactory::TYPE_PAGE,
        \Magento\Framework\Controller\ResultFactory::TYPE_RAW,
        \Magento\Framework\Controller\ResultFactory::TYPE_REDIRECT
    );

    override(
        \Magento\Framework\Message\Factory::create(0),
        map(
            [
                'error' => \Magento\Framework\Message\Error::class,
                'warning' => \Magento\Framework\Message\Warning::class,
                'notice' => \Magento\Framework\Message\Notice::class,
                'success' => \Magento\Framework\Message\Success::class,
            ]
        )
    );

    expectedArguments(
        \Magento\Framework\Message\Factory::create(),
        0,
        \Magento\Framework\Message\MessageInterface::TYPE_ERROR,
        \Magento\Framework\Message\MessageInterface::TYPE_WARNING,
        \Magento\Framework\Message\MessageInterface::TYPE_NOTICE,
        \Magento\Framework\Message\MessageInterface::TYPE_SUCCESS
    );

    registerArgumentsSet(
        'scope_types',
        \Magento\Framework\App\Config\ScopeConfigInterface::SCOPE_TYPE_DEFAULT,
        \Magento\Store\Model\ScopeInterface::SCOPE_STORE,
        \Magento\Store\Model\ScopeInterface::SCOPE_STORES,
        \Magento\Store\Model\ScopeInterface::SCOPE_WEBSITE,
        \Magento\Store\Model\ScopeInterface::SCOPE_WEBSITES
    );
    expectedArguments(
        \Magento\Framework\App\Config\ScopeConfigInterface::getValue(),
        1,
        argumentsSet('scope_types')
    );
    expectedArguments(
        \Magento\Framework\App\Config\ScopeConfigInterface::isSetFlag(),
        1,
        argumentsSet('scope_types')
    );
    expectedArguments(
        \Magento\Framework\App\Config\MutableScopeConfigInterface::setValue(),
        2,
        argumentsSet('scope_types')
    );

    registerArgumentsSet(
        'condition_types',
        'eq',
        'in',
        'is',
        'to',
        'finset',
        'from',
        'gt',
        'gteq',
        'like',
        'lt',
        'lteq',
        'moreq',
        'neq',
        'nin',
        'nlike',
        'notnull',
        'ntoa',
        'null',
        'regexp',
        'seq',
        'sneq'
    );
    expectedArguments(\Magento\Framework\Api\SearchCriteriaBuilder::addFilter(), 2, argumentsSet('condition_types'));
    expectedArguments(\Magento\Framework\Api\FilterBuilder::setConditionType(), 0, argumentsSet('condition_types'));

    expectedArguments(
        \Magento\Framework\Api\SearchCriteriaBuilder::addSortOrder(),
        0,
        \Magento\Framework\Api\SortOrder::SORT_ASC,
        \Magento\Framework\Api\SortOrder::SORT_DESC
    );
    expectedArguments(
        \Magento\Framework\Api\SortOrder::setDirection(),
        0,
        \Magento\Framework\Api\SortOrder::SORT_ASC,
        \Magento\Framework\Api\SortOrder::SORT_DESC
    );

    registerArgumentsSet(
        'field_types',
        'button',
        'checkbox',
        'checkboxes',
        'column',
        'date',
        'editablemultiselect',
        'editor',
        'fieldset',
        'file',
        'gallery',
        'hidden',
        'image',
        'imagefile',
        'label',
        'link',
        'multiline',
        'multiselect',
        'note',
        'obscure',
        'password',
        'radio',
        'radios',
        'reset',
        'select',
        'submit',
        'text',
        'textarea',
        'time'
    );
    expectedArguments(\Magento\Framework\Data\Form\AbstractForm::addField(), 1, argumentsSet('field_types'));
    expectedArguments(\Magento\Framework\Data\Form\Element\Fieldset::addField(), 1, argumentsSet('field_types'));

    registerArgumentsSet(
        'area_codes',
        \Magento\Framework\App\Area::AREA_GLOBAL,
        \Magento\Framework\App\Area::AREA_FRONTEND,
        \Magento\Framework\App\Area::AREA_ADMINHTML,
        \Magento\Framework\App\Area::AREA_DOC,
        \Magento\Framework\App\Area::AREA_CRONTAB,
        \Magento\Framework\App\Area::AREA_WEBAPI_REST,
        \Magento\Framework\App\Area::AREA_WEBAPI_SOAP,
        \Magento\Framework\App\Area::AREA_GRAPHQL
    );
    expectedArguments(\Magento\Framework\App\State::setAreaCode(), 0, argumentsSet('area_codes'));
    expectedArguments(\Magento\Framework\App\State::emulateAreaCode(), 0, argumentsSet('area_codes'));

    registerArgumentsSet(
        'directory_codes',
        \Magento\Framework\App\Filesystem\DirectoryList::ROOT,
        \Magento\Framework\App\Filesystem\DirectoryList::APP,
        \Magento\Framework\App\Filesystem\DirectoryList::CONFIG,
        \Magento\Framework\App\Filesystem\DirectoryList::LIB_INTERNAL,
        \Magento\Framework\App\Filesystem\DirectoryList::LIB_WEB,
        \Magento\Framework\App\Filesystem\DirectoryList::PUB,
        \Magento\Framework\App\Filesystem\DirectoryList::MEDIA,
        \Magento\Framework\App\Filesystem\DirectoryList::STATIC_VIEW,
        \Magento\Framework\App\Filesystem\DirectoryList::VAR_DIR,
        \Magento\Framework\App\Filesystem\DirectoryList::VAR_EXPORT,
        \Magento\Framework\App\Filesystem\DirectoryList::VAR_IMPORT_EXPORT,
        \Magento\Framework\App\Filesystem\DirectoryList::TMP,
        \Magento\Framework\App\Filesystem\DirectoryList::CACHE,
        \Magento\Framework\App\Filesystem\DirectoryList::LOG,
        \Magento\Framework\App\Filesystem\DirectoryList::SESSION,
        \Magento\Framework\App\Filesystem\DirectoryList::SETUP,
        \Magento\Framework\App\Filesystem\DirectoryList::DI,
        \Magento\Framework\App\Filesystem\DirectoryList::GENERATION,
        \Magento\Framework\App\Filesystem\DirectoryList::UPLOAD,
        \Magento\Framework\App\Filesystem\DirectoryList::COMPOSER_HOME,
        \Magento\Framework\App\Filesystem\DirectoryList::TMP_MATERIALIZATION_DIR,
        \Magento\Framework\App\Filesystem\DirectoryList::TEMPLATE_MINIFICATION_DIR,
        \Magento\Framework\App\Filesystem\DirectoryList::GENERATED,
        \Magento\Framework\App\Filesystem\DirectoryList::GENERATED_CODE,
        \Magento\Framework\App\Filesystem\DirectoryList::GENERATED_METADATA
    );
    expectedArguments(\Magento\Framework\Filesystem::getDirectoryRead(), 0, argumentsSet('directory_codes'));
    expectedArguments(\Magento\Framework\Filesystem::getDirectoryWrite(), 0, argumentsSet('directory_codes'));

}
