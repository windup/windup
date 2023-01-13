$(document).ready(function () {

    let selectedSources = [];
    let selectedTargets = [];

    // AND/OR SELECTION CODE
    // var selectedOperation = "OR";
    // $("#op-and").click({}, () => { selectedOperation = "AND"; });
    // $("#op-or").click({},  () => { selectedOperation = "OR"   });

    let eventifyPush = function(arr, callback) {
        arr.push = function(e) {
            if (!arr.includes(e)) {
                Array.prototype.push.call(arr, e);
                callback(arr);
            }
        };
    };

    eventifyPush(selectedSources, function(updatedSources) {
        let techAdded = updatedSources[updatedSources.length - 1];
        $("#selected-sources").append(createRemovableTag(techAdded, updatedSources, WINDUP_TECHNOLOGIES[appId].issuesBySource));
    });

    eventifyPush(selectedTargets, function(updatedTargets) {
        let techAdded = updatedTargets[updatedTargets.length - 1];
        $("#selected-targets").append(createRemovableTag(techAdded, updatedTargets, WINDUP_TECHNOLOGIES[appId].issuesByTarget));
    });

    function createRemovableTag(techId, selectedArray, issues) {
        let techTagEl = $(`<div class="label label-info selected-item">${techId}<a href="#"><span class="glyphicon glyphicon-remove"></span></a> </div>`);
        $(techTagEl).find('span').click({}, function() {
            // filter issues
            filterCallback({ data: { tech: techId, issueIdsByTech: issues, selectedTechs: selectedArray, op: "DEL" } });
            // remove tag itself
            $(techTagEl).remove();
        });
        return techTagEl;
    }

    /**
     * Filtering of issues by targets and sources
     */
    function filtering() {
        let sourcesDropdown = $("#dropdown-sources");
        let targetsDropdown = $("#dropdown-targets");

        // WINDUP_TECHNOLOGIES[appId] = {
        //   issuesBySource: {
        //     springboot: ['497dbb09-3327-4e51-beb1-faa4f2f55cb8', 'cd1923a1-7267-41aa-85d1-7fb3bd1d1c89', ...],
        //     weblogic: [ ... ]
        //   },
        //   issuesByTarget: { ...same as above },
        //   sourceTechs: [ "springboot", "weblogic" ],
        //   targetTechs: [ ...same as above ]
        // }
        sourcesDropdown.children().each(attachFilterBehaviour(WINDUP_TECHNOLOGIES[appId].issuesBySource, selectedSources));
        targetsDropdown.children().each(attachFilterBehaviour(WINDUP_TECHNOLOGIES[appId].issuesByTarget, selectedTargets));

        let clear = $("#clear");
        clear.click(clearAll);
    }

    function attachFilterBehaviour(issueIdsByTech, selectedArray) {
        function filterBehaviour() {
            let techId = this.textContent;
            $(this).click({tech: techId, issueIdsByTech: issueIdsByTech, selectedTechs: selectedArray, op: "ADD"}, filterCallback);
        }

        return filterBehaviour;
    }

    function filterCallback(args) {
        // get all issues elements
        let issueElements = $("tr[data-summary-id]");
        // get the chosen technology
        let chosenTech = args.data.tech;

        // add/remove chosen tech to list of selected techs
        if (args.data.op === "ADD") {
            args.data.selectedTechs.push(chosenTech);
        } else {
            args.data.selectedTechs = args.data.selectedTechs.filter(tech => tech !== chosenTech);
        }

        // get the IDs for the chosen techs
        var issueIdsForSelectedTechs = [];
        for (let tech of args.data.selectedTechs) {
            issueIdsForSelectedTechs = issueIdsForSelectedTechs.concat(args.data.issueIdsByTech[tech]);
        }

        // no issues filtered, show all
        if (issueIdsForSelectedTechs.length === 0) {
            issueIdsForSelectedTechs = issueElements.map((i, e) => $(e).attr("data-summary-id")).get();
        }

        // clear previously selected issues
        clearIssues();

        // hide issues according to selected technologies
        issueElements.filter((i, e) => !issueIdsForSelectedTechs.includes($(e).attr("data-summary-id")))
            .each((i, e) => {
                if (!$(e).hasClass("filtered-out")) {
                    $(e).addClass("filtered-out");
                    $(e).prev().addClass("filtered-out");
                }
            });

        // recalculate incidents found and story points
        let issuesLeft = issueElements
            .filter((i, e) => issueIdsForSelectedTechs.includes($(e).attr("data-summary-id")))
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
        let allIssueIds   = $("tr[data-summary-id]").map((i, e) => $(e).attr("data-summary-id")).get();
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
