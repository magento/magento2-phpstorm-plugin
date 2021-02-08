<?php

declare(strict_types=1);

namespace PHPSTORM_META {

    override(\Magento\Framework\ObjectManagerInterface::get(0), map(['' => '@']));
    override(\Magento\Framework\ObjectManagerInterface::create(0), map(['' => '@']));

    override(\Magento\Framework\View\LayoutInterface::createBlock(0), map(['' => '@']));
    override(\Magento\Framework\View\TemplateEngine\Php::helper(0), map(['' => '@']));

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

    registerArgumentsSet(
        'scope_types',
        \Magento\Framework\App\Config\ScopeConfigInterface::SCOPE_TYPE_DEFAULT,
        \Magento\Store\Model\ScopeInterface::SCOPE_STORE,
        \Magento\Store\Model\ScopeInterface::SCOPE_WEBSITE
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
        'notnull',
        'null'
    );
    expectedArguments(\Magento\Framework\Api\SearchCriteriaBuilder::addFilter(), 2, argumentsSet('condition_types'));
    expectedArguments(\Magento\Framework\Api\FilterBuilder::setConditionType(), 0, argumentsSet('condition_types'));

    expectedArguments(
        \Magento\Framework\Api\SearchCriteriaBuilder::addSortOrder(),
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

}
