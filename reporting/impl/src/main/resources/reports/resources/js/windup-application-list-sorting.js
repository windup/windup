$(document).ready(function () {

    // TARGET_RUNTIME is defined in application_list.ftl
    var RUNTIME_TARGETS = TARGET_RUNTIME;
    RUNTIME_TARGETS.forEach(runtimeTarget => {
        runtimeTarget.supported.sort();
        runtimeTarget.unsuitable.sort();
        runtimeTarget.neutral.sort();
    });

    function runtimeConfig() {
        var runtimeLegendContentDiv = $('#runtimeLegendContent');

        function initialize() {
            // Create legend section
            var runtimeList = runtimeLegendContentDiv.find('dl');
            RUNTIME_TARGETS.forEach(runtimeTarget => {
                runtimeList.append(makeRuntimeLegend(runtimeTarget));
            });


            var apps = $('div.real div.appInfo');

            // Create Runtime labels on each application
            apps.each(function (idx, el) {
                $(this).find('.fileName').append(makeRuntimeLabel());
            });

            makeRuntimeLabelsClickable();

            // Check if application matches criteria
            apps.each(function (idx, el) {
                var tags = $(this).find('div.techs span.label').map(function () {
                    return $(this).text().trim();
                }).toArray();

                $(this).find('div.fileName span.label').each(function () {
                    evaluateRuntime($(this), tags);
                })

            });

        }

        /**
         * Takes an array of strings and convert all
         * elements with the pattern 'regex()' to an RexExp object.
         * E.g. given ['a', 'b', 'regex(myRegex)'], then return ['a', 'b', 'new RegExp("myRegex")']
         * @param array of strings
         **/
        function mapRegexValues(array) {
            var isRegex = (value) => value.startsWith("regex(") && value.endsWith(")");
            var getRegexValue = (value) => value.substring(value.indexOf("regex(") + 6, value.lastIndexOf(")"));

            return array.map(value => {
                if (isRegex(value)) {
                    const regex = getRegexValue(value);
                    return new RegExp(regex);
                } else if (value.endsWith("*")) {
                    return new RegExp("^" + value)
                } else if (value.startsWith("*")) {
                    return new RegExp(value.substr(1) + "$")
                } else {
                    return value;
                }
            })
        }

        /**
         * @param array of strings
         * @param text which will be tested
         * @return true if some array element matches the text
         **/
        function arrayMatchesText(array, text) {
            return array.some(regex => {
                if (regex instanceof RegExp) {
                    return regex.test(text);
                } else {
                    return text === regex;
                }
            })
        }

        /**
         * @param array of strings; some of them can contain regexp
         * @param texts which will be tested
         * @return subgroup of elements of 'texts' which matches 'array'
         **/
        function getMatchedTexts(array, texts) {
            const mappedArray = mapRegexValues(array);

            return texts.filter(text => {
                return arrayMatchesText(mappedArray, text);
            });
        }

        function makeRuntimeLegend(runtimeTarget) {
            var html = $('<dt></dt><dd></dd>');

            var title = runtimeTarget.name + (runtimeTarget.description ? (" - " + runtimeTarget.description) : "");

            var dt = html.filter('dt');
            var dtSpan = $('<span></span>');
            dtSpan.text(runtimeTarget.name);
            dtSpan.attr('title', title);
            dt.append(dtSpan);

            var dd = html.filter('dd');
            runtimeTarget.supported.forEach(label => {
                dd.append(makeLegendLabel('success', label));
            });
            runtimeTarget.unsuitable.forEach(label => {
                dd.append(makeLegendLabel('danger', label));
            });
            runtimeTarget.neutral.forEach(label => {
                dd.append(makeLegendLabel('info', label));
            });

            return html;
        }

        function makeLegendLabel(type, label) {
            var html = $('<span class="label"></span>');
            var span = html.filter('span');
            span.text(label);
            span.addClass('label-' + type);
            return html;
        }

        function makeRuntimeLabel() {
            var div = $('<div></div>');

            RUNTIME_TARGETS.forEach(runtimeTarget => {
                var label = $('<a href="#"><span class="label"></span></a>');
                var span = label.find('span');
                span.text(runtimeTarget.name);
                span.attr('title', runtimeTarget.description);
                span.data({
                    runtimeTarget: runtimeTarget,
                    active: false
                });

                div.append(label);
            });

            return div;
        }

        function makeRuntimeLabelsClickable() {
            $('div.real div.appInfo').each(function () {
                var appInfo = $(this);

                appInfo.find('div.fileName span.label').each(function () {
                    var targetRuntimeSpan = $(this);

                    var runtimeTargetData = targetRuntimeSpan.data().runtimeTarget;
                    targetRuntimeSpan.data('active', false);

                    targetRuntimeSpan.on('click', function (event) {
                        event.preventDefault();

                        var isTargetRuntimeActive = targetRuntimeSpan.data().active;
                        clearRuntimeSelection(appInfo);

                        isTargetRuntimeActive = !isTargetRuntimeActive;
                        targetRuntimeSpan.data('active', isTargetRuntimeActive);

                        if (isTargetRuntimeActive) {
                            targetRuntimeSpan.addClass('active');

                            const supported = mapRegexValues(runtimeTargetData.supported);
                            const neutral = mapRegexValues(runtimeTargetData.neutral);
                            const unsuitable = mapRegexValues(runtimeTargetData.unsuitable);

                            const labels = appInfo.find("div.techs span.label");

                            labels.filter(function () {
                                const text = $(this).text().trim();
                                return arrayMatchesText(supported, text);
                            }).removeClass().addClass('label label-success');

                            labels.filter(function () {
                                const text = $(this).text().trim();
                                return arrayMatchesText(neutral, text);
                            }).removeClass().addClass('label label-default');

                            labels.filter(function () {
                                const text = $(this).text().trim();
                                return arrayMatchesText(unsuitable, text);
                            }).removeClass().addClass('label label-danger');

                            const matchSpans = appInfo.find("div.techs span.label.label-info");
                            matchSpans.removeClass();
                            matchSpans.addClass('label label-warning')
                        }
                    });
                })
            });
        }

        function clearRuntimeSelection(appInfo) {
            appInfo.find('div.fileName span.label').each(function () {
                $(this).data('active', false);
                $(this).removeClass('active');

                var tags = appInfo.find('div.techs span.label');
                tags.removeClass();
                tags.addClass('label label-info');
            })
        }

        function evaluateRuntime(label, tags) {
            var runtimeTarget = label.data().runtimeTarget;

            var supportedTags = getMatchedTexts(runtimeTarget.supported, tags);
            var neutralTags = getMatchedTexts(runtimeTarget.neutral, tags);
            var unsuitableTags = getMatchedTexts(runtimeTarget.unsuitable, tags);

            if (unsuitableTags.length > 0) {
                label.addClass('label-danger');
                label.attr('title', "Unsuitable: " + (runtimeTarget.description || tags.name));
            } else if ((neutralTags.length + supportedTags.length) === tags.length) {
                label.addClass('label-success');
                label.attr('title', "Supported: " + (runtimeTarget.description || tags.name));
            } else {
                label.addClass('label-warning');
                label.attr('title', "Partially supported: " + (runtimeTarget.description || runtimeTarget.name));
            }
        }

        initialize();
    }

    /**
     * All filtering related code
     **/
    function filtering() {
        var resultsToolbar = $('.toolbar-pf-results');
        var countResults = $('#count-results');
        var activeFilters = $('#active-filters');
        var filterInput = $('#filter');
        var filterDiv = $('#filter-div');
        var filterOptionsList = filterDiv.find('ul.dropdown-menu').first();
        var filterByLabel = $('.filter-by, #filter-by'); // $('.filter-by');
        var clearFiltersButton = $('#clear-filters');
        var filterTypeDiv = $('#filter-type');

        /** Active filters */
        var filters = [];

        var hasItemInArrayCallback = function(element, filterOption) {
            var filteringData = $(element).data('filtering');

            if (!filteringData || !filteringData.hasOwnProperty('tags') || filteringData.tags.length === 0) {
                return false;
            }
            var searchText = filterOption.data.text;
            var regex = filterOption.data.isRegex ? new RegExp(filterOption.data.text, 'i') : null;

            var filtered = filteringData.tags.filter(function(element) {
                if (regex != null)
                    if (element.match(regex) !== null) return true;

                // Always try a literal match if there is no regex match
                if (element.toString().toLowerCase().indexOf(searchText.toLowerCase()) != -1) return true;

                return false;
            });

            return filtered.length > 0;
        };

        var checkNameMatchCallback = function(element, filterOption) {
            var name = $(element).data('name');
            var searchText = filterOption.data.text;

            if (filterOption.data.isRegex) {
                var regex = new RegExp(searchText, 'i');
                return name.match(regex) !== null;
            } else {
                if (name.toString().toLowerCase().indexOf(searchText.toLowerCase()) != -1) return true;
            }
        };

        /** Available filter options */
        var filterOptions = [
            { name: 'Name', value: 'name', hint: "Filter by name...", data: '', callback:  checkNameMatchCallback },
            { name: 'Tag', value: 'tags', hint: "Filter by tag...", callback: hasItemInArrayCallback, data: '' }
        ];

        var andReducer = function(prev, curr) { return prev && curr; };
        var orReducer  = function(prev, curr) { return prev || curr; };

        var filterTypes = [
            { name: 'Matches all filters (AND)', reducer: andReducer, default: true },
            { name: 'Matches any filter (OR)',  reducer: orReducer,  default: false }
        ];


        /** Currently selected filter option */
        var currentFilterConfiguration = {
            filterBy: filterOptions[0],
            type: filterTypes[0]
        };

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

            // initialise collapsable element, disabling toggle-on-init parameter
            $("#searchTermError").collapse({toggle:false});

            /**
             * Event handler for <enter> on filter-by input
             */
            $('#filter-form').on('submit', function(e) {
                e.preventDefault();
                var filterValue = filterInput.val().trim();
                if (addFilter(filterValue, currentFilterConfiguration.filterBy, true))
                {
                    filterInput.val('');
                    $("#searchTermError").collapse('hide');
                }
                else
                {
                    filterInput.addClass('alert alert-warning');
                    filterInput.attr('title', 'Invalid Input');
                    $("#searchTermError").collapse('show');
                }
            });

            filterInput.on('input', function(e) {
                e.preventDefault();
                filterInput.removeClass('alert alert-warning');
                filterInput.attr('title', '');
            });



            /** Event handler for clear filters action */
            clearFiltersButton.on('click', function() {
                filters = [];
                filterData();
            });

            /** Use first item from list for filtering */
            filterOptionsList.find('li a').first().click();

            filterTypes.forEach(function(filterType) {
                filterTypeDiv.find('ul.dropdown-menu').append(makeFilterType(filterType));
            });

            /** Use first filter-type (AND) for filtering */
            filterTypeDiv.find('li a').first().click();
        }

        /**
         * Makes tags in application row clickable and adds them to filter after clicking
         *
         */
        function makeTagsClickable() {
            $('div.techs span.label').each(function() {
                var anchored = $('<a href="#"></a>');
                anchored.append($(this).clone());
                anchored.on('click', function() {
                    addFilter($(this).text().trim(), filterOptions[1], false);
                });

                $(this).replaceWith(anchored);
            });
        }

        function makeFilterType(filterType) {
            var html = $('<li><a href="#"></li>');
            var a = html.find('a');
            a.text(filterType.name);
            a.data('filterBy', filterType);

            a.on('click', function() {
                $('#filter-type').find('li a').removeClass('selected');
                $(this).addClass('selected');
                currentFilterConfiguration.type = filterType;
                $('.filter-type').text(filterType.name);
                filterData();
            });

            return html;
        }

        /**
         * Checks if object has callback
         *
         * @param object
         * @returns {boolean}
         */
        function hasCallback(object) {
            return object.hasOwnProperty('callback') && typeof object.callback === 'function';
        }

        /**
         * Filters data by filters array
         *
         */
        function filterData() {
            var filterFailed = false;
            var filteredDivs = $('div.real div.appInfo').map(function(idx, element) {
                var show = true;

                if (filters.length > 0) {
                    var filterResults = filters.map(function (filterOption) {
                        if (hasCallback(filterOption)) {
                            try
                            {
                                return filterOption.callback(element, filterOption);
                            }
                            catch(e)
                            {
                                console.error('Invalid input regular expression');
                                filterFailed = true;
                                return true;
                            }
                        } else {
                            console.error('Expected callback to be defined on filterOption');
                        }
                    });

                    var reduceOptions = currentFilterConfiguration.type;
                    show = filterResults.reduce(reduceOptions.reducer, reduceOptions.default);
                }

                if(!filterFailed) {
                    if (!show) {
                        $(this).hide();
                    } else {
                        $(this).show();
                    }
                }
                return show;
            }).toArray();
            if (!filterFailed) {
                var countUnfiltered = filteredDivs.length;
                var countFiltered = filteredDivs.filter(function(show) { return show; }).length;

                countResults.text(countFiltered);

                refreshFilterPanel();
                return true;
            }
            else
            {
                return false;
            }
        }

        /**
         * Redraws filter panel with used filters labels
         *
         */
        function refreshFilterPanel() {
            activeFilters =  $('#active-filters');
            var newActiveFilters = activeFilters.clone(false);
            newActiveFilters.empty();

            filters.forEach(function(filter) {
                newActiveFilters.append(makeFilteredByLabel(filter));
            });

            activeFilters.replaceWith(newActiveFilters);

            if (filters.length === 0) {
                resultsToolbar.addClass('hidden');
            } else {
                resultsToolbar.removeClass('hidden');
            }
        }

        /**
         * Removes filter from filters array
         *
         * @param item
         * @param node
         */
        function removeFilter(item, node) {
            filters = filters.filter(function(currentItem) { return currentItem !== item; });
            node.remove();
            filterData();
        }

        /**
         * Makes new filtered-by label
         * (used in filtered-by toolbar)
         *
         * @param item {object}
         * @returns {jQuery|HTMLElement}
         */
        function makeFilteredByLabel(item) {
            var html = $('<li><span class="label label-info">\
                    <a href="#"><span class="glyphicon glyphicon-remove"></span></a>\
                    </span></li>');

            var a = html.find('a');
            html.find('span.label').prepend(item.name + ': ' + item.data.text);
            a.on('click', function() {
                removeFilter(item, html);
            });

            html.data('filter', item);

            return html;
        }

        /**
         * Adds filter to filters array and filters data by it
         *
         * @param value {string}
         * @param option {object}
         */
        function addFilter(value, option, isRegex) {
            var returnValue = true;
            var filter = $.extend({}, option);
            filter.data = { text: value, isRegex: isRegex };
            filters.push(filter);

            if (!filterData())
            {
                filters.pop();
                returnValue = false;
            }

            return returnValue;
        }

        /**
         * Creates HTML element for available filters list
         *
         * @param filterOption {object}
         * @returns {jQuery|HTMLElement}
         */
        function makeFilterOptionListItem(filterOption) {
            var html = $('<li><a href="#"></li>');
            var a = html.find('a');
            a.text(filterOption.name);
            a.data('filterBy', filterOption);

            a.on('click', function() {
                filterOptionsList.find('li a').removeClass('selected');
                $(this).addClass('selected');
                currentFilterConfiguration.filterBy = filterOption;
                filterInput.attr("placeholder", filterOption.hint).blur();
                filterByLabel.text(filterOption.name);
            });

            return html;
        }

        initialize();
    }

    /**
     * All sorting related code
     */
    function sorting() {
        var sortDiv = $('#sort');
        var sortBy = $('#sort-by');
        var sortOrder = $('#sort-order');
        var sortIcon = sortOrder.find('span.fa');
        var sortList = sortDiv.find('ul.dropdown-menu');

        var lowerCaseStringComparator = function(a, b) {
            return a.localeCompare(b);
        };

        var storyPointComparator = function(a, b) {
            a = parseInt(a.replace(/[^0-9]/g, ''));
            b = parseInt(b.replace(/[^0-9]/g, ''));
            return parseInt(a) - parseInt(b);
        };

        var sortOptions = [
            { name: 'Name', value: 'name', comparator:  lowerCaseStringComparator },
            { name: 'Story Points', value: 'storypoints', comparator: storyPointComparator }
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
                $(this).data('storypoints', $(this).find('.effortPoints.total .points').text().trim());
            });

            sortOptions.forEach(function(option) {
                sortList.append(makeSortListItem(option));
            });

            /** Sorts applications by name */
            sortList.find('li a').first().click();

            /**
             * On click handler for sort order button
             * Switches between ASC/DESC sorting
             */
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

                sortData();
            });
        }

        function makeSortListItem(sortOption) {
            var html = $('<li><a href="#"></li>');
            var a = html.find('a');
            a.text(sortOption.name);
            a.data('sortBy', sortOption);

            a.on('click', function() {
                sortList.find('li a').removeClass('selected');
                $(this).addClass('selected');
                currentSortConfiguration.sortBy = sortOption;
                sortBy.text(sortOption.name);
                sortData();
            });

            return html;
        }

        /**
         * Checks if object has comparator
         *
         * @param object
         * @returns {boolean}
         */
        function hasComparator(object) {
            return object.hasOwnProperty('comparator') && typeof object.comparator === 'function';
        }

        /**
         * Sorts applications by specified criteria
         */
        function sortData() {
            $('div.real div.appInfo').sortElements(function(elementA, elementB) {
                var result = 0;

                var a = $(elementA).data(currentSortConfiguration.sortBy.value);
                var b = $(elementB).data(currentSortConfiguration.sortBy.value);

                if (hasComparator(currentSortConfiguration.sortBy)) {
                    result = currentSortConfiguration.sortBy.comparator(a, b);
                } else {
                    result = a > b ? 1 : -1;
                }

                return result * currentSortConfiguration.order;
            });
        }

        initialize();
    }

    filtering();
    sorting();
    runtimeConfig();
});
