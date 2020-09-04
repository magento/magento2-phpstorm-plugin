/*
 * Copyright © Magento, Inc. All rights reserved.
 * See COPYING.txt for license details.
 */

package com.magento.idea.magento2plugin.actions.generation.data;

@SuppressWarnings({"PMD.DataClass"})
public class UiComponentGridToolbarData {
    private final boolean addToolbar;
    private final boolean addBookmarks;
    private final boolean addColumnsControls;
    private final boolean addFulltextSearch;
    private final boolean addListingFilters;
    private final boolean addListingPaging;

    /**
     * UI component grid toolbar data constructor.
     *
     * @param addToolbar Add toolbar
     * @param addBookmarks Add bookmarks
     * @param addColumnsControls Add columns controls
     * @param addFulltextSearch Add fulltext search
     * @param addListingFilters Add listing filters
     * @param addListingPaging Add listing paging
     */
    public UiComponentGridToolbarData(
            final boolean addToolbar,
            final boolean addBookmarks,
            final boolean addColumnsControls,
            final boolean addFulltextSearch,
            final boolean addListingFilters,
            final boolean addListingPaging
    ) {
        this.addToolbar = addToolbar;
        this.addBookmarks = addBookmarks;
        this.addColumnsControls = addColumnsControls;
        this.addFulltextSearch = addFulltextSearch;
        this.addListingFilters = addListingFilters;
        this.addListingPaging = addListingPaging;
    }

    /**
     * Is add toolbar.
     *
     * @return Boolean
     */
    public boolean isAddToolbar() {
        return addToolbar;
    }

    /**
     * Is add bookmarks.
     *
     * @return Boolean
     */
    public boolean isAddBookmarks() {
        return addBookmarks;
    }

    /**
     * Is add listing paging.
     *
     * @return Boolean
     */
    public boolean isAddListingPaging() {
        return addListingPaging;
    }

    /**
     * Is add columns controls.
     *
     * @return Boolean
     */
    public boolean isAddColumnsControls() {
        return addColumnsControls;
    }

    /**
     * Is add fulltext search.
     *
     * @return Boolean
     */
    public boolean isAddFulltextSearch() {
        return addFulltextSearch;
    }

    /**
     * Is add listing filters.
     *
     * @return Boolean
     */
    public boolean isAddListingFilters() {
        return addListingFilters;
    }
}
