$(document).ready(function() {

    var $table = $('.tablesorter');

    // hide child rows & make draggable
    $table.find('.tablesorter-childRow')
        .find('td')
        .droppable({
            accept: '.draggingSiblings',
            drop: function(event, ui) {
                if ($(this).closest('tr').length) {
                    $(this).closest('tr').before(
                        ui.draggable
                            .css({ left: 0, top: 0 })
                            .parent()
                            .removeClass('draggingRow')
                    );
                    $table
                        .find('.draggingSiblingsRow')
                        .removeClass('draggingSiblingsRow')
                        .find('.draggingSiblings')
                        .removeClass('draggingSiblings');
                    $table.trigger('update');
                } else {
                    return false;
                }
            }
        })
        .draggable({
            revert: "invalid",
            start: function( event, ui ) {
                $(this)
                    .parent()
                    .addClass('draggingRow')
                    .prevUntil('.tablesorter-hasChildRow')
                    .nextUntil('tr:not(.tablesorter-childRow)')
                    .addClass('draggingSiblingsRow')
                    .find('td')
                    .addClass('draggingSiblings');
            }
        })
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

function resizeTables() {
    var tableArr = document.getElementsByClassName('migration-issues-table');
    var cellWidths = [];

    // get widest
    for(var i = 0; i < tableArr.length; i++)
    {
        for(var j = 0; j < tableArr[i].rows[0].cells.length; j++)
        {
            var cell = tableArr[i].rows[0].cells[j];

            if(!cellWidths[j] || cellWidths[j] < cell.clientWidth)
                cellWidths[j] = cell.clientWidth;
        }
    }

    // set all columns to the widest width found
    for(i = 0; i < tableArr.length; i++)
    {
        for(j = 0; j < tableArr[i].rows[0].cells.length; j++)
        {
            tableArr[i].rows[0].cells[j].style.width = cellWidths[j]+'px';
        }
    }

    console.log(cellWidths);

    var styles = '';

    for (var index = 0; index < cellWidths.length; index++) {
        styles += 'table thead tr:nth-child(1) td:nth-child(' + (index + 1) + '),\n'
               +  'table thead tr:nth-child(1) th:nth-child(' + (index + 1) + ') { width: ' + cellWidths[index] + 'px; }\n\n';
    }

    var styleEl = $('<style></style>');
    styleEl.append(styles);
    $('body').append(styleEl);
}

window.onload = resizeTables;


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

    $(".fileSummary_id_" + problemSummaryID).remove();
    if (wrappingTd.is(":visible")) {
        toggleRow();
        return;
    }

    var issueDataArray = MIGRATION_ISSUES_DETAILS[problemSummaryID];
    var source   = $("#detail-row-template").html();
    var template = Handlebars.compile(source);
    var html = template({problemSummaries: issueDataArray});

    function replaceTr() {
        tr.children().remove();

        var html = $('<td colspan="5" style="display:none" class="table-inner-wrapping-td">\n' +
            '            <table class="table-inner table table-bordered table-condensed migration-issues-table">\n' +
            '                <thead>\n' +
            '                    <tr><th style="padding: 10px 15px">omg</th><th></th><th></th><th></th><th></th></tr>\n' +
            '                    <tr class="bg-info">\n' +
            '                        <th><div class="indent"><strong>File</strong></div></th>\n' +
            '                        <th class="text-right"><strong>Incidents Found</strong></th>\n' +
            '                        <th colspan="3"><strong>Hint</strong></th>\n' +
            '                    </tr>\n' +
            '                </thead>\n' +
            '                <tbody>\n' +
            '                </tbody>\n' +
            '            </table>\n' +
            '        </td>');

        tr.append(html);
    }

    replaceTr();
    tr.find('tbody').append(html);
    wrappingTd = tr.find('td').first(); // old wrapping td is deleted, find new one

    toggleRow();
}

// summary in JS should go here
var MIGRATION_ISSUES_DETAILS = [];
