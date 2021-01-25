<?php

namespace Foo\Bar\Ui\Listing;

use Foo\Bar\Query\GetListQuery;
use Magento\Framework\Api\FilterBuilder;
use Magento\Framework\Api\Search\ReportingInterface;
use Magento\Framework\Api\Search\SearchCriteriaBuilder;
use Magento\Framework\App\RequestInterface;
use Magento\Framework\View\Element\UiComponent\DataProvider\DataProvider;
use Magento\Ui\DataProvider\SearchResultFactory;

/**
 * DataProvider component.
 */
class GridDataProvider extends DataProvider
{
    /**
     * @var GetListQuery
     */
    private $getListQuery;

    /**
     * @var SearchResultFactory
     */
    private $searchResultFactory;

    /**
     * @param string $name
     * @param string $primaryFieldName
     * @param string $requestFieldName
     * @param ReportingInterface $reporting
     * @param SearchCriteriaBuilder $searchCriteriaBuilder
     * @param RequestInterface $request
     * @param FilterBuilder $filterBuilder
     * @param GetListQuery $getListQuery
     * @param SearchResultFactory $searchResultFactory
     * @param array $meta
     * @param array $data
     */
    public function __construct(
        $name,
        $primaryFieldName,
        $requestFieldName,
        ReportingInterface $reporting,
        SearchCriteriaBuilder $searchCriteriaBuilder,
        RequestInterface $request,
        FilterBuilder $filterBuilder,
        GetListQuery $getListQuery,
        SearchResultFactory $searchResultFactory,
        array $meta = [],
        array $data = []
    )
    {
        parent::__construct(
            $name,
            $primaryFieldName,
            $requestFieldName,
            $reporting,
            $searchCriteriaBuilder,
            $request,
            $filterBuilder,
            $meta,
            $data
        );
        $this->getListQuery = $getListQuery;
        $this->searchResultFactory = $searchResultFactory;
    }

    /**
     * @inheritDoc
     */
    public function getSearchResult()
    {
        $searchCriteria = $this->getSearchCriteria();
        $result = $this->getListQuery->execute($searchCriteria);

        return $this->searchResultFactory->create(
            $result->getItems(),
            $result->getTotalCount(),
            $searchCriteria,
            'entity_id'
        );
    }
}
