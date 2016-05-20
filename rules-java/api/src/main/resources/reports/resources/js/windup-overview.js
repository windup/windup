/* ========   Collapsible panels ========== */

function togglePanelSlide(event) {
    var $panelHeading = $(this);
    setPanelSlide($panelHeading, $panelHeading.hasClass("panel-collapsed"));
}

function setPanelSlide($panelHeading, expand) {
    var projectGuid = $panelHeading.parent().data("windup-projectguid");
    $.sessionStorage.set(projectGuid, expand ? "true" : "false");
    $panelHeading.parents(".panel").find(".panel-body")["slide" + (expand ? "Down" : "Up")]();
    $panelHeading.parents(".panel").toggleClass("panel-boarding", expand);
    $panelHeading.toggleClass("panel-collapsed", !expand);
    $panelHeading.find("i").toggleClass("glyphicon-expand", !expand).toggleClass("glyphicon-collapse-up", expand);
}

function expandMemory() {
    expandMemory.elements$ = [];
    var t0 = Date.now();
	$('.panel-heading').each(function() {
        expandMemory.elements$.push($(this));
    });
    console.log('PERF: $(".panel-heading") found ' + expandMemory.elements$.length + " elements in " + (Date.now() - t0) + " ms.");
    window.setTimeout(expandMemory_delayed, 500);
}

function expandMemory_delayed() {
    var BATCH_SIZE = 25;
    console.log("PERF: expandMemory_delayed(), next " + BATCH_SIZE + " projects.");///
    for (var i = BATCH_SIZE; i > 0; i--) {
        var $element = expandMemory.elements$.shift();
        //console.log($element);///
        if($element == undefined) {
            // All project boxes were processed.
            showCollapseExpandLinksAsNeeded();
            return;
        }
		var projectGuid = $element.parent().data("windup-projectguid");
		if($.sessionStorage.isSet(projectGuid)) {
			if($.sessionStorage.get(projectGuid)) {
				setPanelSlide($element, true);
			}
            continue;
		}

        var storyPoints = $element.parent().data("windup-project-storypoints");
		if(parseInt(storyPoints) > 0) {
			setPanelSlide($element, true);
			continue;
		}
	}
    window.setTimeout(expandMemory_delayed, 300);
}


//
function expandAll() {
    $('.panel-body').slideDown();
    $('.panel-heading').find('i').removeClass('glyphicon-expand').addClass('glyphicon-collapse-up');
    $('.panel-heading').addClass('panel-boarding').removeClass('panel-collapsed');
    $('.panel-heading').parents('.panel').addClass('panel-boarding');
    $('#expandAll').hide();
    $('#collapseAll').show();
}

function collapseAll() {
    $('.panel-body').slideUp();
    $('.panel-heading').find('i').removeClass('glyphicon-collapse-up').addClass('glyphicon-expand');
	$('.panel-heading').addClass('panel-collapsed').removeClass('panel-boarding');
	$('.panel-heading').parents('.panel').addClass('panel-collapsed').removeClass('panel-boarding');
	$('#expandAll').show();
	$('#collapseAll').hide();
}

function showCollapseExpandLinksAsNeeded() {
    $('#collapseAll').toggle( $('.panel-heading').find('.glyphicon-collapse-up').length > 0 );
    $('#expandAll').toggle( $('.panel-heading').find('.glyphicon-expand').length > 0 );
}


/* ========   Projects TreeView   ========== */

function renderAppTreeView(rootProject) {
    var start = t0 = Date.now();
    var jsTreeData = prepareJsTreeData(rootProject);
    console.log("PERF: prepareJsTreeData() took " + (Date.now() - start) + " ms.");

    $('#treeView-Projects').jstree({
        plugins : ["types"], // ["wholerow"], //"checkbox"
        core: {
            data: jsTreeData
        },
        types : {
            ok : { icon : "glyphicon glyphicon-ok" },
            alert : { icon : "glyphicon glyphicon-alert" }
        }
    });

    // Handler for selection change.
    $('#treeView-Projects').on("changed.jstree", function(event, data) {
        var nodeId = data.selected[0];
        console.log("Possible project id: " + nodeId);
        var projectId = data.selected[0].substring(nodeId.lastIndexOf("-")+1);
        console.log("calculated project id: " + projectId);
        $panel = $("#"+projectId);
        setPanelSlide($panel.find(".panel-heading"), true);
        var pageHeader = 60; // Should go global.
        $('html, body').animate({ scrollTop: $panel.offset().top - pageHeader }, 400);
        var flashColor = "gray";
        $panel.find(".panel-title")
            .animate( { color: flashColor }, 240)
            .animate( { color: "" }, 240)
            .animate( { color: flashColor }, 240)
            .animate( { color: "" }, 240)
            .animate( { color: flashColor }, 240)
            .animate( { color: "" }, 240)
            .animate( { color: flashColor }, 240)
            .animate( { color: "" }, 240)
            .animate( { color: flashColor }, 240)
            .animate( { color: "" }, 240);
    });

}



/* ========   Projects TreeView   ========== */

function createTagCharts() {
    window.chartObjects = {};
    window.projectBoxes$ = [];

    // For each project's file, count it's tags.
    window.rootProjCountMap = {};

    var t0 = Date.now();
    $("div.projectBox").each(function(iProj){
    ///$("body > div.container-fluid > div.row > div.theme-showcase > div.projectBox").each(function(iProj){
        window.projectBoxes$.push($(this));
    });
    console.log('PERF: $("div.projectBox") found ' + window.projectBoxes$.length + " elements in " + (Date.now() - t0) + " ms.");
    window.setTimeout(processNextChart, 50);
}

function processNextChart() {
    var BATCH_SIZE = 25;
    console.log("PERF: processNextChart(), next " + BATCH_SIZE + " projects.");
    for (var i = BATCH_SIZE; i > 0; i--) {
        var $projectBox = window.projectBoxes$.shift();
        if (!$projectBox) {
            summaryChart();
            $(".hideWhenComputed").remove();
            $(document.body).addClass("computingDone");
            return;
        }

        var projectId = $projectBox.attr("id");
        //console.log("PERF: " + Date.now() + " Processing projectBox: " + projectId);
        var curProjCountsMap = {};
        var rootProjCountMap = window.rootProjCountMap;

        // Create the tag -> count map.
        // This query is an optimized form of .find(".projectFile") / .find(".tech .tag")
        ///$projectBox.find(".projectFile").each(function(iFile){
        $projectBox.children("div.panel-body").children("table.subprojects").children("tbody").children("tr.projectFile").each(function(iFile) {
            ///$(this).find(".tech .tag").each(function(iTag){
            $(this).children("td.tech").children("span.tag").each(function(iTag) {
                var tagName = $(this).data("windup-tag");
                // Get the nearest root tag, as per definitions in *.tags.xml.
                if(!tagName)
                    return;
                var rootTags = window.tagService.getNearestRoots(tagName);
                if (rootTags == null)
                    return;
                for (var i = 0; i < rootTags.length; i++) {
                    tagName = rootTags[i].getTitleOrName();
                    rootProjCountMap[ tagName ] = ++rootProjCountMap[ tagName ] || 1; // Sum map.
                    curProjCountsMap[ tagName ] = ++curProjCountsMap[ tagName ] || 1;
                }
            });
        });

        // Don't draw the chart if there's just one tag.
        if (curProjCountsMap.length < 2)
            return;

        var chartHeight = Math.max(50, Math.min(400, Object.keys(curProjCountsMap).length * 22 + 20));

        // Render the bar chart for this project.
        // We need to render it somewhere where it is visible and then move to the collapsed subproject divs.
        $(document.body).append('<div class="tagChart" style="height: ' + chartHeight + 'px; width: 500px;"></div>');
        var chartDiv = $("body > .tagChart")[0];
        curProjCountsMap = sortMapByValues(curProjCountsMap);
        // Store the chart object for later use. Not used yet.
        chartObjects[projectId + "-tags"] = createChart(chartDiv, curProjCountsMap);
        $("body > .tagChart").appendTo( $projectBox.find(".summaryMargin .tagsBarChart") );
        $projectBox.find(".summaryMargin .tagsBarChart").append( $("body > .tagChart") );
    }

    // Queue processing of the next chart.
    window.setTimeout(processNextChart, 10);
}

/**
 * Creates the summary chart at the top.
 */
function summaryChart() {
    // Sum tags chart
    var rootProjCountMap = sortMapByValues(window.rootProjCountMap);
    createChart("#tagsChartContainer-sum", rootProjCountMap);

    // Substitutes yaxis: { font: } - Flot uses "smaller" which breaks alignment.
    $(".tagChart .flot-text").css("font-size", "");
}


var TagService = function() {
    this.tags = {};
}

TagService.prototype.registerTag = function(/*Tag*/ tag, /*String[]*/ parentTagNames) {
    var existing = this.tags[tag.name];
    if (existing == undefined)
        this.tags[tag.name] = tag;
    else {
        // Placeholder.
        existing.mergeFrom(tag);
        tag = existing;
    }

    for (var i = 0; i < parentTagNames.length; i++){
        var parentName = parentTagNames[i];
        var parent = this.getOrCreateTag(parentName);
        tag.parents.push(parent);
        //parent.addChild(tag);
    }
};

TagService.prototype.getOrCreateTag = /*Tag*/ function(/*String*/ name) {
    return this.tags[name] || (this.tags[name] = new Tag(name));
};

TagService.prototype.getNearestRoots = /*Tag*/ function(/*String*/ tagName) {
    var tag = this.tags[tagName];
    if (tag === undefined || tag === null) {
        console.warn("Undefined Windup tag name passed to getNearestRoots(tagName): " + tagName);
        return null;
    }

    var currentSet = [tag];
    var nextSet = [];
    var visitedParents = [tagName];
    var roots = tag.isRoot ? [tag] : [];

    // Follow the multiple possible parent paths to their roots.
    while(currentSet.length !== 0) {
        nextSet = [];
        for (var i = 0; i < currentSet.length; i++) {
            var curTag = currentSet[i];
            for (var j = 0; j < curTag.parents.length; j++) {
                var curParent = curTag.parents[j];
                if (-1 != visitedParents.indexOf(curParent)) {
                    console.warn("Already visited tag parent: " + curParent.name);
                    continue;
                }
                visitedParents.push(curParent);
                if (curParent.isRoot) {
                    roots.push(curParent);
                } else {
                    nextSet.push(curParent);
                }
            }
        }
        currentSet = nextSet;
    }
    return roots;
};

TagService.prototype.toString = function() {
    return "TagService{ " + (this.tags == null ? "null" : this.tags.size()) + " }";
};

/**
 * Windup Tag.
 * @param {string}  name     Mandatory. Short lowercase dash-separated.
 * @param {string}  title    May be null. Human reader friendly title.
 * @param {boolean} isRoot   Whether the tag is one which will aggregate.
 * @param {boolean} isPseudo
 * @param {string}  color    Color of the tag in reports.
 * @param {Tag[]}   parents  Tags which are "above" this one.
 * @returns {Tag}
 */
var Tag = function(name, title, isRoot, isPseudo, color, parents) {
    this.name = name;
    this.title = title;
    this.isRoot = isRoot;
    this.isPseudo = isPseudo;
    this.color = color;
    this.parents = parents instanceof Array || [];
};

Tag.prototype.getTitleOrName = function() {
    return this.title || this.name;
};

Tag.prototype.mergeFrom = function(/*Tag*/ tag) {
    this.color = tag.color;
    this.title = tag.title;
    this.isRoot = tag.isRoot;
    this.isPseudo = tag.isPseudo;
    if (tag.parents instanceof Array) {
        if ( this.parents instanceof Array)
            Array.prototype.push.apply(this.parents, tag.parents);
        else
            this.parents = tag.parents;
    }
};


// Prepare the data in the format [[value,index], ...].
function prepareFlotData(tagToCountMap) {
    var ticks = [];
    var values = [];
    var maxValue = 1;

    var keys = getKeys(tagToCountMap);
    for (var i = 0; i < keys.length; i++) {
        ticks[i] = [i, keys[i]];
        values[i] = [tagToCountMap[keys[i]], i];
        maxValue = Math.max(maxValue, values[i][0]);
    }
    return { ticks: ticks, values: values, maxValue: maxValue };
}

function createChart(divSelectorOrElement, tagToCountMap) {
    var flotData = prepareFlotData(tagToCountMap);
    return createFlotChart(divSelectorOrElement, flotData);
}

function createFlotChart(divSelectorOrElement, flotData, isLogarithmic) {
    var dataset = [{ data: flotData.values, color: "#5482FF" }];

    var options = {
        series: { bars: { horizontal: true, show: true } },
        bars: {
            align: "center",
            barWidth: 0.6,
            lineWidth: 1
        },
        grid: {
            hoverable: true,
            borderWidth: 1,
            borderColor: "#B0B0B0",
            backgroundColor: { colors: ["#FFFFFF", "#EDF5FF"] },
            margin: 3, // Doesn't work
            minBorderMargin: 3
        },
        xaxis: {
            axisLabel: "Count",
            axisLabelUseCanvas: true,
            axisLabelFontSizePixels: 12,
            axisLabelFontFamily: "Verdana, Arial",
            axisLabelPadding: 10,
            max: flotData.maxValue * 1.1, // Substitutes grid: { margin: ... }
            tickDecimals: 0,
            tickFormatter: function(value, axis){ return value + "x"; }
        },
        yaxis: {
            axisLabel: "Technology",
            axisLabelUseCanvas: true,
            axisLabelFontSizePixels: 12,
            axisLabelFontFamily: "Verdana, Arial",
            ticks: flotData.ticks,
            // Otherwise Flot uses "smaller" which breaks alignment.
            //font: { size: "14px", color: "black" }, // doesn't work
        }
    };

    if (isLogarithmic) {
        options.xaxis.ticks = [1,2,3,4,5,6,7,8,9,10,15,30,50,75,100,200,350,500,750,1000,5000,10000];
        options.xaxis.transform = function(v) { return Math.log(v+0.0001); /*move away from zero*/};
        options.xaxis.inverseTransform = function (v) { return Math.exp(v); };
        options.xaxis.min = 0.7;
    }

    return $.plot( $(divSelectorOrElement), dataset, options );
}
