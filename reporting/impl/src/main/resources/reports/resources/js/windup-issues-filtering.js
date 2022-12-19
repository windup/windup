$(document).ready(function () {

    let selectedSources = [];
    let selectedTargets = [];

    let eventifyPush = function(arr, callback) {
        arr.push = function(e) {
            if (!arr.includes(e)) {
                Array.prototype.push.call(arr, e);
                callback(arr);
            }
        };
    };

    eventifyPush(selectedSources, function(updatedSources) {
        let elementAdded = updatedSources[updatedSources.length - 1];
        $("#selected-sources").append(`<div class="label label-info selected-item">${elementAdded}</div>`);
    });

    eventifyPush(selectedTargets, function(updatedTargets) {
        let elementAdded = updatedTargets[updatedTargets.length - 1];
        $("#selected-targets").append(`<div class="label label-info selected-item">${elementAdded}</div>`);
    });

    /**
     * Filtering of issues by targets and sources
     */
    function filtering() {
        let targetsDropdown = $("#dropdown-targets");
        let sourcesDropdown = $("#dropdown-sources");

        targetsDropdown.children().each(attachFilterBehaviour(WINDUP_TECHNOLOGIES[appId].issuesByTarget, selectedTargets));
        sourcesDropdown.children().each(attachFilterBehaviour(WINDUP_TECHNOLOGIES[appId].issuesBySource, selectedSources));

        let clear = $("#clear");
        clear.click(clearAll);
    }

    function attachFilterBehaviour(techOpts, selectedArray) {
        function filterBehaviour() {
            let techId = this.textContent;
            $(this).click({tech: techId, opts: techOpts, selected: selectedArray}, filterCallback);
        }

        return filterBehaviour;
    }

    function filterCallback(data) {
        // get all issue IDs
        let issueIds = $("tr[data-summary-id]");
        // get the chosen technology
        let chosenTech = data.data.tech;
        // add chosen tech to list of techs
        data.data.selected.push(chosenTech);
        // get the IDs for the chosen techs
        var filteredIssues = [];
        for (tech of data.data.selected) {
            filteredIssues = filteredIssues.concat(data.data.opts[tech]);
        }
        // clear previously selected issues
        clearIssues();
        // add display:none to the ones left out
        issueIds.filter((i, e) => !filteredIssues.includes($(e).attr("data-summary-id")))
            .each((i, e) => {
                if (!$(e).hasClass("filtered-out")) {
                    $(e).addClass("filtered-out");
                    $(e).prev().addClass("filtered-out");
                }
            });

        // recalculate incidents found and story points
        let issuesLeft = issueIds
            .filter((i, e) => filteredIssues.includes($(e).attr("data-summary-id")))
            .map((i, e) => $(e).attr("data-summary-id"))
            .get();

        // set numbers
        calculateNumbers(issuesLeft);
    }

    function calculateNumbers(issues) {
        // get all severity IDs
        let severities = Object.values(categories);

        // for each severity type, get issue IDs of the ones left out from filtering
        // and calculate number + effort
        for (let severity of severities) {
            let numbersBySeverity = problemSummaryNumbers[severity];
            if (numbersBySeverity) {
                let issuesLeftForSeverity = Object.keys(numbersBySeverity)
                    .filter(ps => issues.includes(ps))
                    .map(issue => problemSummaryNumbers[severity][issue]);
                let totalNumber = issuesLeftForSeverity.map(issue => issue.numberFound).reduce((acc, val) => acc + val, 0);
                let totalEffort = issuesLeftForSeverity.map(issue => issue.storyPoints).reduce((acc, val) => acc + val, 0);
                $(`#table-${severity} > tfoot > tr > td[data-column='1']`).text(totalNumber);
                $(`#table-${severity} > tfoot > tr > td[data-column='4']`).text(totalEffort);
            }
        }

    }

    function clearNumbers() {
        let issues = $("tr[data-summary-id]");
        let allIssueIds = issues.map((i, e) => $(e).attr("data-summary-id"))
            .get();
        calculateNumbers(allIssueIds);
    }

    function clearAll() {
        clearNumbers();
        clearIssues();
        clearTechs();
    }

    function clearIssues() {
        // show all issues again
        $(".filtered-out").removeClass("filtered-out");
    }

    function clearTechs() {
        // remove all children
        $("#selected-targets").empty();
        $("#selected-sources").empty();

        // clear selected techs arrays
        selectedSources.length = 0;
        selectedTargets.length = 0
    }

    filtering();
});
