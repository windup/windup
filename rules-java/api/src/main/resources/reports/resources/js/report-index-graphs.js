var CHART_HEIGHT = "280px";
var CHART_WIDTH = "100%";
var CHART_DISPLAY = "inline-block";

function createCharts() {

    // Category and Effort data chart
    var effortAndCategoryChart = $("#effortAndCategoryChart");
    effortAndCategoryChart.css("display", CHART_DISPLAY);
    effortAndCategoryChart.css("height", CHART_HEIGHT);
    effortAndCategoryChart.css("width", CHART_WIDTH);
    createLineAndBarChart(effortAndCategoryChart, effortByCategoryChartData(), issuesByCategoryChartData());

    // Effort  and incidents for Mandatory category data chart
    var mandatoryIncidentsByEffortAndPointsChart = $("#mandatoryIncidentsByEffortChart");
    mandatoryIncidentsByEffortAndPointsChart.css("display", CHART_DISPLAY);
    mandatoryIncidentsByEffortAndPointsChart.css("height", CHART_HEIGHT);
    mandatoryIncidentsByEffortAndPointsChart.css("width", CHART_WIDTH);
    createLineAndBarChart(mandatoryIncidentsByEffortAndPointsChart, mandatoryEffortByTypeChartData(), mandatoryIncidentsByTypeChartData());

}

function mandatoryIncidentsByTypeChartData() {
    var mandatoryData = getWindupIssueSummaries()["mandatory"];
    if (mandatoryData == null)
        mandatoryData = [];

    var ticks = [];
    var values = [];
    var maxValue = 1;

    var maxEffort = 13;
    var byEffortCount = [];

    mandatoryData.forEach(function(problemSummary) {
        maxEffort = Math.max(maxEffort, problemSummary.effortPerIncident);
        if (!(problemSummary.effortPerIncident in byEffortCount)) {
            byEffortCount[problemSummary.effortPerIncident] = problemSummary.numberFound;
        } else {
            byEffortCount[problemSummary.effortPerIncident] += problemSummary.numberFound;
        }
    });

    var index = 0;
    for (var effort = 0; effort <= maxEffort; effort++) {
        if (byEffortCount[effort] == null) {
            if (effortToDescription[effort])
                byEffortCount[effort] = 0;
            else
                continue;
        }

        var effortDescription = getDescriptionForEffort(effort)
        ticks[index] = [index, effortDescription];

        var incidentCount = byEffortCount[effort];

        values[index] = [incidentCount, index];
        maxValue = Math.max(maxValue, incidentCount);

        index++;
    }

    return { ticks: ticks, values: values, maxValue: maxValue };
}

function mandatoryEffortByTypeChartData() {
    var mandatoryData = getWindupIssueSummaries()["mandatory"];
    if (mandatoryData == null)
        mandatoryData = [];

    var ticks = [];
    var values = [];
    var maxValue = 1;

    var maxEffort = 13;
    var byEffortCount = [];

    mandatoryData.forEach(function(problemSummary) {
        maxEffort = Math.max(maxEffort, problemSummary.effortPerIncident);
        if (!(problemSummary.effortPerIncident in byEffortCount)) {
            byEffortCount[problemSummary.effortPerIncident] = problemSummary.numberFound;
        } else {
            byEffortCount[problemSummary.effortPerIncident] += problemSummary.numberFound;
        }
    });

    var index = 0;
    for (var effort = 0; effort <= maxEffort; effort++) {
        if (byEffortCount[effort] == null) {
            if (effortToDescription[effort])
                byEffortCount[effort] = 0;
            else
                continue;
        }
        ticks[index] = [index, getDescriptionForEffort(effort)];

        var totalEffort = byEffortCount[effort] * effort;

        values[index] = [index, totalEffort];
        maxValue = Math.max(maxValue, totalEffort);

        index++;
    }

    return { ticks: ticks, values: values, maxValue: maxValue };
}

function effortByCategoryChartData() {
    var ticks = [];
    var values = [];
    var maxValue = 1;

    var index = 0;
    for (var idx in categoryOrder) {
        var category = categoryOrder[idx];
        var issueSummaries = getWindupIssueSummaries()[category];
        if (issueSummaries == null)
            issueSummaries = [];

        var totalEffort = 0;
        var incidents = 0; 
        var numberFound = 0;
        issueSummaries.forEach(function(problemSummary) {
            incidents += problemSummary.numberFound;
            totalEffort += (problemSummary.numberFound * problemSummary.effortPerIncident);
            numberFound += problemSummary.numberFound;
        });

        if (numberFound == 0)
            continue;

        ticks[index] = [index, category];
        values[index] = [index, totalEffort];
        maxValue = Math.max(maxValue, totalEffort);

        index++;
    }

    return { ticks: ticks, values: values, maxValue: maxValue };
}

function issuesByCategoryChartData() {
    var ticks = [];
    var values = [];
    var maxValue = 1;

    var index = 0;
    for (var idx in categoryOrder) {
        var category = categoryOrder[idx];
        var issueSummaries = getWindupIssueSummaries()[category];
        if (issueSummaries == null)
            issueSummaries = [];


        var numberFound = 0;
        issueSummaries.forEach(function(problemSummary) {
            numberFound += problemSummary.numberFound;
        });

        if (numberFound == 0)
            continue;

        ticks[index] = [index, category];

        values[index] = [numberFound, index];
        maxValue = Math.max(maxValue, numberFound);

        index++;
    }

    return { ticks: ticks, values: values, maxValue: maxValue };
}

function createLineAndBarChart(divSelectorOrElement, lineChartData, barChartData) {
    if (lineChartData == null || barChartData == null)
        return;

    var barChartDataArray = [];
    for (var i = 0; i < barChartData.values.length; i++) {
        barChartDataArray[i] = [barChartData.values[i][1], barChartData.values[i][0]];
    }

    var dataset = [
        {
            data: barChartDataArray,
            color: "#5482FF",
            bars: {
                show: true,
                align: "center",
                barWidth: .6,
                lineWidth:1
            },
            valueLabels: {
                show: true
            }
        },
        {
            data: lineChartData.values,
            color: "#FF0000",
            points: {
                symbol: "circle",
                show: true
            },
            lines: {
                show: true
            },
            valueLabels: {
                show: true
            }
        }
    ];

    var options = {
        xaxis: {
            ticks: lineChartData.ticks
        },
        yaxes: {
            axisLabelPadding: 3
        },
        legend: {
            noColumns: 0,
            labelBoxBorderColor: "#000000",
            position: "nw"
        },
        grid: {
            hoverable: false,
            borderWidth: 1,
            borderColor: "#B0B0B0",
            backgroundColor: { colors: ["#FFFFFF", "#EDF5FF"] },
        },
        colors: ["#FF0000", "#0022FF"]
    };

    var plot = $.plot($(divSelectorOrElement), dataset, options);
}


function createIncidentsByCategoryTable() {
    var tbodyElement = $("#incidentsByTypeTBody");

    var rows = "";
    for (var idx in categoryOrder) {
        var category = categoryOrder[idx];
        var issueSummaries = getWindupIssueSummaries()[category];
        if (issueSummaries == null)
            issueSummaries = [];

        var row = "";
        var incidentCount = 0;
        var totalEffort = 0;
        issueSummaries.forEach(function(problemSummary) {
            incidentCount += problemSummary.numberFound;
            totalEffort += (problemSummary.numberFound * problemSummary.effortPerIncident);
        });

        row += "<tr>"
        row += "<td>" + category + "</td>";
        row += "<td class='numeric-column'>" + incidentCount + "</td>";
        row += "<td class='numeric-column'>" + totalEffort + "</td>";
        row += "</tr>";

        rows += row;
    }
    tbodyElement.prepend(rows);
}

function getDescriptionForEffort(effort) {
    var result;
    if (!(effort in effortToDescription)) {
        result = "Unknown";
    } else {
        result = effortToDescription[effort];
    }
    return result;
}

function createIncidentsByEffortTable() {
    var tbodyElement = $("#mandatoryIncidentsByEffortTBody");

    var maxEffort = 13;
    var byEffortCount = [];

    var mandatoryIncidents = getWindupIssueSummaries()["mandatory"];
    if (mandatoryIncidents == null)
        mandatoryIncidents = [];

    mandatoryIncidents.forEach(function(problemSummary) {
        maxEffort = Math.max(maxEffort, problemSummary.effortPerIncident);
        if (!(problemSummary.effortPerIncident in byEffortCount)) {
            byEffortCount[problemSummary.effortPerIncident] = problemSummary.numberFound;
        } else {
            byEffortCount[problemSummary.effortPerIncident] += problemSummary.numberFound;
        }
    });

    var rows = "";
    for (var effort = 0; effort <= maxEffort; effort++) {
        if (byEffortCount[effort] == null) {
            if (effortToDescription[effort])
                byEffortCount[effort] = 0;
            else
                continue;
        }

        var totalEffort = effort * byEffortCount[effort];
        var effortDescription = getDescriptionForEffort(effort);

        var row = "";

        row += "<tr>";

        row += "<td>";
        row += effortDescription;
        row += "</td>";

        row += "<td class='numeric-column'>";
        row += byEffortCount[effort];
        row += "</td>";

        row += "<td class='numeric-column'>";
        row += totalEffort;
        row += "</td>";

        row += "</tr>";

        rows += row;
    }
    tbodyElement.prepend(rows);
}

function createDataTables() {
    createIncidentsByCategoryTable();
    createIncidentsByEffortTable();
}

$(document).ready(function() {
    createCharts();
    createDataTables();
});
