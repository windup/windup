
/* ========   Collapsible panels ========== */

function togglePanelSlide(event)
{
    var $panelHeading = $(this);
    setPanelSlide($panelHeading, $panelHeading.hasClass("panel-collapsed"));
}

function setPanelSlide($panelHeading, expand){
    $panelHeading.parents(".panel").find(".panel-body")["slide" + (expand ? "Down" : "Up")]();
    $panelHeading.parents(".panel").toggleClass("panel-boarding", expand);
    $panelHeading.toggleClass("panel-collapsed", !expand);
    $panelHeading.find("i").toggleClass("glyphicon-expand", !expand).toggleClass("glyphicon-collapse-up", expand);
}

function expandAll(){
	$('.panel-body').slideDown();
	$('.panel-heading').find('i').removeClass('glyphicon-expand').addClass('glyphicon-collapse-up');
	$('.panel-heading').addClass('panel-boarding').removeClass('panel-collapsed');
	$('.panel-heading').parents('.panel').addClass('panel-boarding');
	$('#expandAll').toggle();
	$('#collapseAll').toggle();
}

function collapseAll(){
	$('.panel-body').slideUp();
	$('.panel-heading').find('i').removeClass('glyphicon-collapse-up').addClass('glyphicon-expand');
	$('.panel-heading').addClass('panel-collapsed').removeClass('panel-boarding');
	$('.panel-heading').parents('.panel').addClass('panel-collapsed').removeClass('panel-boarding');
	$('#expandAll').toggle();
	$('#collapseAll').toggle();
}


/* ========   Projects TreeView   ========== */

function renderAppTreeView(rootProject)
{
    var jsTreeData = prepareJsTreeData(rootProject);
    $('#treeView-Projects').jstree({
        plugins : ["types"], // ["wholerow"], //"checkbox"
        core: {
            data: jsTreeData
        },
        types : {
            ok : { icon : "glyphicon glyphicon-ok" },
            alert : { icon : "glyphicon glyphicon-alert" }
        },
    });

    // Handler for selection change.
    $('#treeView-Projects').on("changed.jstree", function(event, data) {
        var projectId = data.selected[0].substring("treeNode-".length);
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
    // For each project's file, count it's tags.
    var rootProjCountMap = {};
    $(".projectBox").each(function(iProj){
        var projectId = $(this).attr("id");
        curProjCountsMap = {};

        // Create the tag -> count map.
        $(this).find(".projectFile").each(function(iFile){
            $(this).find(".tech .label").each(function(iTag){
                var tagName = $(this).text().trim();
                rootProjCountMap[ tagName ] = ++rootProjCountMap[ tagName ] || 1; // Sum map.
                curProjCountsMap[ tagName ] = ++curProjCountsMap[ tagName ] || 1;
            })
        })

        // Don't draw the chart if there's just one tag.
        if (curProjCountsMap.length < 2)
            return;

        chartHeight = Math.max(50, Math.min(400, Object.keys(curProjCountsMap).length * 22 + 20));

        // Render the bar chart for this project.
        // We need to render it somewhere where it is visible and then move to the collapsed subproject divs.
        $(document.body).append('<div class="tagChart" style="height: ' + chartHeight + 'px; width: 500px;"></div>'); // Returns body.
        var chartDiv = $("body > .tagChart")[0];
        curProjCountsMap = sortMapByValues(curProjCountsMap);
        // Store the chart object for later use.
        chartObjects[projectId + "-tags"] =
                createChart(chartDiv, curProjCountsMap);
        $("body > .tagChart").appendTo( $(this).find(".summaryMargin .tagsBarChart") );
        $(this).find(".summaryMargin .tagsBarChart").append( $("body > .tagChart") );
    });

    // Sum tags chart
    rootProjCountMap = sortMapByValues(rootProjCountMap);
    createChart("#tagsChartContainer-sum", rootProjCountMap);

    // Substitutes yaxis: { font: } - Flot uses "smaller" which breaks alignment.
    $(".tagChart .flot-text").css("font-size", "");
}


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

function createFlotChart(divSelectorOrElement, flotData) {
    var dataset = [{ data: flotData.values, color: "#5482FF" }];

    var options = {
        series: { bars: { horizontal: true, show: true } },
        bars: {
            align: "center",
            barWidth: 0.6,
            lineWidth: 1,
        },
        grid: {
            hoverable: true,
            borderWidth: 1,
            borderColor: "#B0B0B0",
            backgroundColor: { colors: ["#FFFFFF", "#EDF5FF"] },
            margin: 3, // Doesn't work
            minBorderMargin: 3,
        },
        xaxis: {
            axisLabel: "Count",
            axisLabelUseCanvas: true,
            axisLabelFontSizePixels: 12,
            axisLabelFontFamily: "Verdana, Arial",
            axisLabelPadding: 10,
            // Logarithmic
            //ticks: [1,2,3,4,5,6,7,8,9,10,15,30,50,75,100,200,350,500,750,1000,5000,10000],
            //transform: function(v) { return Math.log(v+0.0001); /*move away from zero*/},
            //inverseTransform: function (v) { return Math.exp(v); }
            //min: 0.7,
            max: flotData.maxValue * 1.1, // Substitutes grid: { margin: ... }
            tickDecimals: 0,
            tickFormatter: function(value, axis){ return value + "x"; },
        },
        yaxis: {
            axisLabel: "Technology",
            axisLabelUseCanvas: true,
            axisLabelFontSizePixels: 12,
            axisLabelFontFamily: "Verdana, Arial",
            ticks: flotData.ticks,
            // Otherwise Flot uses "smaller" which breaks alignment.
            //font: { size: "14px", color: "black" }, // doesn't work
        },
    };

    return $.plot( $(divSelectorOrElement), dataset, options );
}
