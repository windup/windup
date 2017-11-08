$(document).ready(function () {
    function filtering() {
        var resultsToolbar = $('.toolbar-pf-results');
        var countResults = $('#count-results');
        var activeFilters = $('#active-filters');
        var filterInput = $('#filter');
        var filterDiv = $('#filter-div');
        var filterOptionsList = filterDiv.find('ul.dropdown-menu');

        var filters = [];

        var filterOptions = [
            { name: 'Name', value: 'name' },
            { name: 'Tags', value: 'tags' }
        ];

        var clearFiltersButton = $('#clear-filters');

        clearFiltersButton.on('click', function() {
            filters = [];
            refreshFilters();
        });

        function initialize() {
            $('div.real div.appInfo').each(function () {
                var tags = $(this).find('div.techs span.label').map(function() {
                    return $(this).text().trim();
                });

                var filtering = {
                    tags: tags
                };

                $(this).data('filtering', filtering);
            });
        }

        function refreshFilters() {
            var newActiveFilters = activeFilters.cloneNode(false);
            newActiveFilters.empty();

            filters.forEach(function(filter) {
                newActiveFilters.append(makeFilterItem(filter));
            });

            activeFilters.replaceWith(newActiveFilters);
        }

        function removeFromFilter(item, node) {
            filters = filters.filter(function(currentItem) { return currentItem !== item; });
            node.remove();
        }

        function makeFilterItem(item) {
            var html = $('<li><span class="label label-info">\
                    <a href="#"><span class="pficon pficon-close"></span></a>\
                    </span></li>');

            var a = html.find('a');
            a.prepend(item.key + ': ' + item.value);
            a.on('click', function() {
                removeFromFilter(item, html);
            });

            html.data('filter', item);

            return html;
        }

        function makeFilterOptionListItem(filterOption) {
            var html = $('<li><a href="#"></li>');
            var a = html.find('a');
            a.text(filterOption.name);
            a.data('sortBy', filterOption);

            a.on('click', function() {
                filterOptionsList.find('li').removeClass('selected');
                $(this).addClass('selected');
                currentSortConfiguration.sortBy = sortOption;
                sortBy.text(sortOption.name);
                refresh();
            });

            return html;
        }

        $('#filter-form').on('submit', function(e) {
            e.preventDefault();
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
