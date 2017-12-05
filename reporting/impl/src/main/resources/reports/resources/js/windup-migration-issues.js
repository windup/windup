$(document).ready(function() {

    var $table = $('.tablesorter');

    // hide child rows & make draggable
    $table.find('.tablesorter-childRow')
        .find('td')
        .hide();

    // we need these parsers because we are using comma to separate thousands and are also sorting links
    $.tablesorter.addParser({
        id: 'thousands',
        is: function(s) { return true; },
        format: function(s) {
            return s.replace('$','').replace(/,/g,'');
        },
        type: 'numeric'
    });

    $.tablesorter.addParser({
        id: 'a-elements',
        is: function(s) { return true; },
        format: function(s) {
            // format your data for normalization
            return s.replace(new RegExp(/<.*?>/),"");
        },
        parsed: true,
        type: 'text'
    });

    $table
        .tablesorter({
            // this is the default setting
            cssChildRow: "tablesorter-childRow",
            sortList: [[1,1]],
            headers: {
                0: {sorter: 'a-elements'},
                1: {sorter: 'thousands'},
                2: {sorter: 'thousands'},
                3: {sorter: false},
                4: {sorter: 'thousands'}
            }
        })
        .delegate('.toggle', 'click' ,function() {
            $(this) // this is <a> which has been clicked
                .closest('tr') // closest <tr> is its parent
                .nextUntil('tr.tablesorter-hasChildRow') // find first following <tr> with hasChildRow class
                .find('td').first() // find first <td>
                .each(function(index, element) { // and execute showDetails on it (each will be called once)
                    showDetails(element);
                });

            return false;
        });

});

var issueDataLoaded = [];

function loadProblemSummaryScript(problemSummaryID) {
    var script = document.createElement("script");
    script.type = "text/javascript";
    script.src = "data/problem_summary_" + problemSummaryID + ".js";
    document.body.appendChild(script);
}


function showDetails(element) {
    var problemSummaryID = $(element).parent().attr("data-summary-id");

    if (!issueDataLoaded[problemSummaryID]) {
        // append it and try again in a second
        loadProblemSummaryScript(problemSummaryID);
        issueDataLoaded[problemSummaryID] = true;
    }  else {
        onProblemSummaryLoaded(problemSummaryID);
    }
}

/**
 * This will get executed from newly loaded script
 *
 * @param problemSummaryID
 */
function onProblemSummaryLoaded(problemSummaryID) {
    var tr = $('tr[data-summary-id="' + problemSummaryID + '"]').first();

    var wrappingTd = tr.find('td').first();

    function toggleRow () {
        wrappingTd.toggle();
        var issuesTable = tr.parent().parent();
        $(issuesTable).trigger("update", [true]);
    }

    var tbody = tr.find('tbody');

    if (wrappingTd.is(":visible")) {
        toggleRow();
        // TODO: Is it worth to remove it?
        tbody.children().remove();
    } else {
        var issueDataArray = MIGRATION_ISSUES_DETAILS[problemSummaryID];
        var source   = $("#detail-row-template").html();
        var template = Handlebars.compile(source);
        var html = template({problemSummaries: issueDataArray});

        tbody.append(html);
        toggleRow();
    }
}

// summary in JS should go here
var MIGRATION_ISSUES_DETAILS = [];
