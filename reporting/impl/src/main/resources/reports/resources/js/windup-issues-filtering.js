// WINDUP_TECHNOLOGIES[appId] = {
//   issuesBySource: {
//     springboot: ['497dbb09-3327-4e51-beb1-faa4f2f55cb8', 'cd1923a1-7267-41aa-85d1-7fb3bd1d1c89', ...],
//     weblogic: [ ... ]
//   },
//   issuesByTarget: { ...same as above },
//   sourceTechs: [ "springboot", "weblogic" ],
//   targetTechs: [ ...same as above ]
// }
$(document).ready(function () {
    let selectedSources = [];
    let selectedTargets = [];

    // AND/OR SELECTION CODE
    var selectedOperation = "OR";
    $("#op-and").click({}, () => {
        selectedOperation = "AND";
        $("#op-button").text("Matches all filters (AND)")
        filterCallback();
    });
    $("#op-or").click({}, () => {
        selectedOperation = "OR";
        $("#op-button").text("Matches any filter (OR)")
        filterCallback();
    });

    $("#selected-filters").hide();

    function createRemovableTag(args) {
        let techTagEl = $(`<div class="label label-info selected-item">${args.data.techId}<a href="#"><span class="glyphicon glyphicon-remove"></span></a> </div>`);
        $(techTagEl).find('span').click({}, function() {
            // remove item from list of selected techs
            args.data.selectedTechs.splice(args.data.selectedTechs.indexOf(args.data.techId), 1);
            // filter issues
            filterCallback();
            // remove tag itself
            $(techTagEl).remove();
        });
        args.data.selectedTechsTagsElement.append(techTagEl);
        return techTagEl;
    }

    /**
     * Filtering of issues by targets and sources
     */
    function filtering() {
        let sourcesDropdown = $("#dropdown-sources");
        let targetsDropdown = $("#dropdown-targets");

        sourcesDropdown.children().each(attachFilterBehaviour(selectedSources, $("#selected-sources")));
        targetsDropdown.children().each(attachFilterBehaviour(selectedTargets, $("#selected-targets")));

        $("#clear").click(clearAll);
    }

    function attachFilterBehaviour(selectedTechs, selectedTechsTagsElement) {
        function filterBehaviour() {
            let techId = this.textContent;
            // add tech to list of currently selected techs
            $(this).click(() => selectedTechs.push(techId));
            // filter
            $(this).click(filterCallback);
            // add tech tag to list
            $(this).click({ techId, selectedTechs, selectedTechsTagsElement }, createRemovableTag);
        }
        return filterBehaviour;
    }

    function filterCallback() {
        // get all issues elements
        let issueElements = $("tr[data-summary-id]");

        // pick ids for currently selected technologies in both sources and targets
        let issuesBySource = WINDUP_TECHNOLOGIES[appId].issuesBySource;
        let issuesByTarget = WINDUP_TECHNOLOGIES[appId].issuesByTarget;

        // gather all issue ids for all selected technologies
        let issueIdsForSelectedTechs = [];
        for (let source of selectedSources) {
            issueIdsForSelectedTechs.push(issuesBySource[source]);
        }
        for (let target of selectedTargets) {
            issueIdsForSelectedTechs.push(issuesByTarget[target]);
        }

        var filteredIssueIds = [];
        if (selectedOperation === "AND") {
            // do intersection of all ids
            filteredIssueIds = issueIdsForSelectedTechs.reduce((a, b) => a.filter(c => b.includes(c)), issueIdsForSelectedTechs[0] || []);
        } else {
            // do union of all ids
            filteredIssueIds = issueIdsForSelectedTechs.reduce((a, b) => [...new Set([...a, ...b])], [])
        }

        // clear previously selected issues
        clearIssues();

        if (filteredIssueIds.length !== 0 || (filteredIssueIds.length === 0 && (selectedSources.length !== 0 || selectedTargets.length !== 0))) {
            // hide issues which don't comply with the filtering
            issueElements.filter((i, e) => !filteredIssueIds.includes($(e).attr("data-summary-id")))
                .each((i, e) => {
                    $(e).hide();
                    $(e).prev().hide();
                });

            $("#selected-filters").show();

            // set numbers
            calculateNumbers(filteredIssueIds);
        } else {
            $("#selected-filters").hide();
            clearNumbers();
        }
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
        let allIssueIds   = $("tr[data-summary-id]").map((i, e) => $(e).attr("data-summary-id")).get();
        calculateNumbers(allIssueIds);
    }

    function clearAll() {
        clearNumbers();
        clearIssues();
        clearTechs();

        $("#selected-filters").hide()
    }

    function clearIssues() {
        // show all issues again
        let issueElements = $("tr[data-summary-id]");
        issueElements.show();
        issueElements.prev().show();
    }

    function clearTechs() {
        // remove all children
        $("#selected-targets").empty();
        $("#selected-sources").empty();

        // clear selected techs arrays
        selectedSources.length = 0;
        selectedTargets.length = 0

        $("#selected-filters").hide()
    }

    filtering();
});
