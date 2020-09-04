<?php

namespace Foo\Bar\Ui\Listing;

use Magento\Framework\Api\FilterBuilder;
use Magento\Framework\Api\Search\ReportingInterface;
use Magento\Framework\Api\Search\SearchCriteriaBuilder;
use Magento\Framework\Api\Search\SearchResultInterface;
use Magento\Framework\Api\Search\SearchResultInterfaceFactory;
use Magento\Framework\Api\SearchCriteria\CollectionProcessorInterface;
use Magento\Framework\App\RequestInterface;
use Foo\Bar\Model\Resource\Entity\Collection;
use Foo\Bar\Model\Resource\Entity\CollectionFactory;
use Magento\Framework\View\Element\UiComponent\DataProvider\DataProvider;

class GridDataProvider extends DataProvider
{
    /**
     * @var CollectionFactory
     */
    private $collectionFactory;

    /**
     * @var CollectionProcessorInterface
     */
    private $collectionProcessor;

    /**
     * @var SearchResultInterfaceFactory
     */
    private $searchResultFactory;

    /**
     * GridDataProvider constructor.
     *
     * @param string $name
     * @param string $primaryFieldName
     * @param string $requestFieldName
     * @param ReportingInterface $reporting
     * @param SearchCriteriaBuilder $searchCriteriaBuilder
     * @param RequestInterface $request
     * @param FilterBuilder $filterBuilder
     * @param CollectionFactory $collectionFactory
     * @param CollectionProcessorInterface $collectionProcessor
     * @param SearchResultInterfaceFactory $searchResultFactory
     * @param array $meta
     * @param array $data
     */
    public function __construct(
        string $name = '',
        string $primaryFieldName = '',
        string $requestFieldName = '',
        ReportingInterface $reporting,
        SearchCriteriaBuilder $searchCriteriaBuilder,
        RequestInterface $request,
        FilterBuilder $filterBuilder,
        CollectionFactory $collectionFactory,
        CollectionProcessorInterface $collectionProcessor,
        SearchResultInterfaceFactory $searchResultFactory,
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

        $this->collectionFactory = $collectionFactory;
        $this->collectionProcessor = $collectionProcessor;
        $this->searchResultFactory = $searchResultFactory;
    }

    /**
     * Get search result
     *
     * @return SearchResultInterface
     */
    public function getSearchResult()
    {
        /** @var SearchResultInterface $searchResults */
        $searchResults = $this->searchResultFactory->create();
        $searchCriteria = $this->getSearchCriteria();
        /** @var Collection $collection */
        $collection = $this->collectionFactory->create();
        $this->collectionProcessor->process($this->getSearchCriteria(), $collection);
        $items = $collection->getItems();
        $searchResults->setItems($items);
        $searchResults->setTotalCount($collection->getSize());
        $searchResults->setSearchCriteria($searchCriteria);

        return $searchResults;
    }

    /**
     * Search result to output
     *
     * @param SearchResultInterface $searchResult
     *
     * @return array
     */
    public function searchResultToOutput(SearchResultInterface $searchResult)
    {
        $result = ['items' => []];
        $items = $searchResult->getItems();

        if (!$items || !count($items)) {
            $result['totalRecords'] = 0;

            return $result;
        }

        foreach ($items as $index => $item) {
            $result['items'][$index] = $item->getData();
        }

        $result['totalRecords'] = $searchResult->getTotalCount();

        return $result;
    }
}
