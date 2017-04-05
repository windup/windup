function createTagCharts() {

    var count = 0;
    for (var severity in getWindupIssueSummaries()) {
        if (!getWindupIssueSummaries().hasOwnProperty(severity))
            continue;
        count++;
    }

    var incidentsBySeverityChart = $("#incidentsBySeverityChart");
    incidentsBySeverityChart.css("display", "inline-block")
    incidentsBySeverityChart.css("height", "250px");
    incidentsBySeverityChart.css("width", "100%");
    createBarChart(incidentsBySeverityChart, issuesBySeverityChartData());

    var effortAndSeverityChart = $("#effortAndSeverityChart");
    effortAndSeverityChart.css("display", "inline-block");
    effortAndSeverityChart.css("height", "250px");
    effortAndSeverityChart.css("width", "100%");
    createLineAndBarChart(effortAndSeverityChart, effortBySeverityChartData(), issuesBySeverityChartData());

    var mandatoryIncidentsByEffortChart = $("#mandatoryIncidentsByEffort");
    mandatoryIncidentsByEffortChart.css("display", "inline-block")
    mandatoryIncidentsByEffortChart.css("height", "250px");
    mandatoryIncidentsByEffortChart.css("width", "100%");
    createBarChart(mandatoryIncidentsByEffortChart, mandatoryIncidentsByTypeChartData());

    var mandatoryIncidentsByEffortAndPointsChart = $("#mandatoryIncidentsByEffortAndStoryPoints");
    mandatoryIncidentsByEffortAndPointsChart.css("display", "inline-block");
    mandatoryIncidentsByEffortAndPointsChart.css("height", "250px");
    mandatoryIncidentsByEffortAndPointsChart.css("width", "100%");
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

function effortBySeverityChartData() {
    var ticks = [];
    var values = [];
    var maxValue = 1;

    var index = 0;
    for (var idx in severityOrder) {
        var severity = severityOrder[idx];
        var issueSummaries = getWindupIssueSummaries()[severity];
        if (issueSummaries == null)
            issueSummaries = [];

        var totalEffort = 0;
        var numberFound = 0;
        issueSummaries.forEach(function(problemSummary) {
            totalEffort += (problemSummary.numberFound * problemSummary.effortPerIncident);
            numberFound += problemSummary.numberFound;
        });

        if (numberFound == 0)
            continue;

        ticks[index] = [index, severity];
        values[index] = [index, totalEffort];
        maxValue = Math.max(maxValue, totalEffort);

        index++;
    }

    return { ticks: ticks, values: values, maxValue: maxValue };
}

function issuesBySeverityChartData() {
    var ticks = [];
    var values = [];
    var maxValue = 1;

    var index = 0;
    for (var idx in severityOrder) {
        var severity = severityOrder[idx];
        var issueSummaries = getWindupIssueSummaries()[severity];
        if (issueSummaries == null)
            issueSummaries = [];


        var numberFound = 0;
        issueSummaries.forEach(function(problemSummary) {
            numberFound += problemSummary.numberFound;
        });

        if (numberFound == 0)
            continue;

        ticks[index] = [index, severity];

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

function createBarChart(divSelectorOrElement, flotData) {
    if (flotData == null)
        return null;

    var dataset = [{
        data: flotData.values,
        color: "#5482FF",
        valueLabels: {
            show: true,
            showAsHtml: true,
            xoffset: 3,
            plotAxis: 'x'
        },
    }];

    var options = {
        series: { bars: { horizontal: true, show: true } },
        bars: {
            align: "center",
            barWidth: .6,
            lineWidth: 1,
        },
        grid: {
            hoverable: false,
            borderWidth: 1,
            borderColor: "#B0B0B0",
            backgroundColor: { colors: ["#FFFFFF", "#EDF5FF"] },
        },
        xaxis: {
            axisLabel: "Number of incidents",
            axisLabelPadding: 1,
            axisLabelUseCanvas: false,
            axisLabelFontFamily: 'Verdana, Arial',
            max: flotData.maxValue * 1.1,
            tickDecimals: 0,
        },
        yaxis: {
            ticks: flotData.ticks,

            // reverse the order (needed since we want top to bottom, but flot horizontal charts are bottom to top)
            transform: function(v) { return -v; },
            inverseTransform: function(v) { return -v; }
        },
    };

    return $.plot( $(divSelectorOrElement), dataset, options );
    //return $.plot( $("#incidentsBySeverityChart"), dataset, options );
}

function createIncidentsByCategoryTable() {
    var tbodyElement = $("#incidentsByTypeTBody");

    var rows = "";
    for (var idx in severityOrder) {
        var severity = severityOrder[idx];
        var issueSummaries = getWindupIssueSummaries()[severity];
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
        row += "<td>" + severity + "</td>";
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
    createTagCharts();
    createDataTables();
});
