$(document).ready(function () {
    function filtering() {
        var resultsToolbar = $('.toolbar-pf-results');
        var countResults = $('#count-results');
        var activeFilters = $('#active-filters');
        var filterInput = $('#filter');
        var filterDiv = $('#filter-div');
        var filterOptionsList = filterDiv.find('ul.dropdown-menu');

        var filterByLabel = $('.filter-by');
        filterByLabel = $('.filter-by, #filter-by');

        var clearFiltersButton = $('#clear-filters');

        var filters = [];

        var hasItemInArrayCallback = function(element, filterOption) {
            var filteringData = $(element).data('filtering');

            if (!filteringData || !filteringData.hasOwnProperty('tags') || filteringData.tags.length === 0) {
                return false;
            }

            return filteringData.tags.indexOf(filterOption.data) !== -1;
        };

        var checkNameMatch = function(element, filterOption) {
            var name = $(element).data('name');
            var regex = new RegExp(filterOption.data, 'i');

            return name.match(regex) !== null;
        };

        var filterOptions = [
            { name: 'Name', value: 'name', data: '', callback:  checkNameMatch },
            { name: 'Tags', value: 'tags', callback: hasItemInArrayCallback, data: '' }
        ];

        var currentFilterConfiguration = {
            filterBy: filterOptions[0]
        };

        clearFiltersButton.on('click', function() {
            filters = [];
            filterData();
        });

        function initialize() {
            $('div.real div.appInfo').each(function () {
                var tags = $(this).find('div.techs span.label').map(function() {
                    return $(this).text().trim();
                }).toArray();

                var filtering = {
                    tags: tags
                };

                $(this).data('filtering', filtering);
            });

            filterOptions.forEach(function(option) {
                filterOptionsList.append(makeFilterOptionListItem(option));
            });

            filterByLabel.text(currentFilterConfiguration.filterBy.name);


            makeTagsClickable();
        }

        function makeTagsClickable() {
            $('div.techs span.label').each(function() {
                var anchored = $('<a href="javascript:null"></a>');
                anchored.append($(this).clone());
                anchored.on('click', function() {
                    addFilter($(this).text().trim(), filterOptions[1]);
                });

                $(this).replaceWith(anchored);
            });
        }

        function filterData() {
            var filteredDivs = $('div.real div.appInfo').map(function(idx, element) {
                var show = true;

                if (filters.length > 0) {
                    var filterResults = filters.map(function (filterOption) {
                        //var filterOption = currentFilterConfiguration.filterBy;
                        if (filterOption.hasOwnProperty('callback')) {
                            return filterOption.callback(element, filterOption);
                        }
                    });

                    // TODO: Use AND or OR?
                    show = filterResults.reduce(function (previous, current) {
                        return previous || current;
                    }, false);
                }

                if (!show) {
                    $(this).hide();
                } else {
                    $(this).show();
                }

                return show;
            }).toArray();

            var countUnfiltered = filteredDivs.length;
            var countFiltered = filteredDivs.filter(function(show) { return show; }).length;

            countResults.text(countFiltered);

            refreshFilterPanel();
        }

        function refreshFilterPanel() {
            activeFilters =  $('#active-filters');
            var newActiveFilters = activeFilters.clone(false);
            newActiveFilters.empty();

            filters.forEach(function(filter) {
                newActiveFilters.append(makeFilterItem(filter));
            });

            activeFilters.replaceWith(newActiveFilters);

            if (filters.length === 0) {
                resultsToolbar.addClass('hidden');
            } else {
                resultsToolbar.removeClass('hidden');
            }
        }

        function removeFromFilter(item, node) {
            filters = filters.filter(function(currentItem) { return currentItem !== item; });
            node.remove();
            filterData();
        }

        function makeFilterItem(item) {
            var html = $('<li><span class="label label-info">\
                    <a href="#"><span class="glyphicon glyphicon-remove"></span></a>\
                    </span></li>');

            var a = html.find('a');
            html.find('span.label').prepend(item.name + ': ' + item.data);
            a.on('click', function() {
                removeFromFilter(item, html);
            });

            html.data('filter', item);

            return html;
        }

        function addFilter(value, option) {
            var filter = $.extend({}, option);
            filter.data = value;
            filters.push(filter);

            filterData();
        }

        function makeFilterOptionListItem(filterOption) {
            var html = $('<li><a href="#"></li>');
            var a = html.find('a');
            a.text(filterOption.name);
            a.data('filterBy', filterOption);

            a.on('click', function() {
                filterOptionsList.find('li').removeClass('selected');
                $(this).addClass('selected');
                currentFilterConfiguration.filterBy = filterOption;
                filterByLabel.text(filterOption.name);
            });

            return html;
        }

        $('#filter-form').on('submit', function(e) {
            e.preventDefault();
            var filterValue = filterInput.val().trim();
            addFilter(filterValue, currentFilterConfiguration.filterBy);
            filterInput.val('');
        });

        initialize();
    }

    function sorting() {
        var sortDiv = $('#sort');
        var sortBy = $('#sort-by');
        var sortOrder = $('#sort-order');
        var sortIcon = sortOrder.find('span.fa');
        var sortList = sortDiv.find('ul.dropdown-menu');

        var lowerCaseStringComparator = function(a, b) {
            return a.localeCompare(b);
        };


        var sortOptions = [
            { name: 'Name', value: 'name', comparator:  lowerCaseStringComparator },
            { name: 'Story Points', value: 'storypoints' }
        ];

        var currentSortConfiguration = {
            sortBy: sortOptions[0],
            order: 1 // +1 ascending, -1 descending
        };

        var sortOrderClasses = {
            ASC: 'fa-sort-alpha-asc',
            DESC: 'fa-sort-alpha-desc'
        };

        function initialize() {
            $('div.real div.appInfo').each(function (idx, el) {
                $(this).data('name', $(this).find('.fileName').text().trim());
                $(this).data('storypoints', parseInt($(this).find('.effortPoints.total').text().trim()));
            });

            sortOptions.forEach(function(option) {
                sortList.append(makeSortListItem(option));
            });

            sortList.find('li a').first().click();
        }

        function makeSortListItem(sortOption) {
            var html = $('<li><a href="#"></li>');
            var a = html.find('a');
            a.text(sortOption.name);
            a.data('sortBy', sortOption);

            a.on('click', function() {
                sortList.find('li').removeClass('selected');
                $(this).addClass('selected');
                currentSortConfiguration.sortBy = sortOption;
                sortBy.text(sortOption.name);
                refresh();
            });

            return html;
        }

        function refresh() {
            $('div.real div.appInfo').sortElements(function(elementA, elementB) {
                var result = 0;

                var a = $(elementA).data(currentSortConfiguration.sortBy.value);
                var b = $(elementB).data(currentSortConfiguration.sortBy.value);

                if (currentSortConfiguration.sortBy.hasOwnProperty('comparator') && typeof currentSortConfiguration.sortBy.comparator === 'function') {
                    result = currentSortConfiguration.sortBy.comparator(a, b);
                } else {
                    result = a > b ? 1 : -1;
                }

                return result * currentSortConfiguration.order;
            });
        }

        sortOrder.on('click', function() {
            var currentClass;
            var newClass;

            if (currentSortConfiguration.order === 1) {
                currentClass = sortOrderClasses.ASC;
                newClass = sortOrderClasses.DESC;
            } else {
                currentClass = sortOrderClasses.DESC;
                newClass = sortOrderClasses.ASC;
            }

            currentSortConfiguration.order *= -1;

            sortIcon.removeClass(currentClass);
            sortIcon.addClass(newClass);

            refresh();
        });

        initialize();
    }

    filtering();
    sorting();



});
