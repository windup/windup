$(document).ready(function () {

    /**
     * Filtering of issues by targets and sources
     */
    function filtering() {
        let targetsDropdown = $("#dropdown-targets");
        let sourcesDropdown = $("#dropdown-sources");

        targetsDropdown.children().each(attachFilterBehaviour(ISSUES_BY_TARGET));
        sourcesDropdown.children().each(attachFilterBehaviour(ISSUES_BY_SOURCE));

        let clear = $("#clear");
        clear.click(clearCallback);
    }

    function attachFilterBehaviour(techOpts) {
        function filterBehaviour() {
            let techId = this.textContent;
            $(this).click({tech: techId, opts: techOpts}, filterCallback);
        }

        return filterBehaviour;
    }

    function filterCallback(data) {
        // get all issue IDs
        let issueIds = $("tr[data-summary-id]");
        // get the chosen technology
        let chosenTech = data.data.tech;
        // get the IDs for the chosen tech
        let filteredIssues = data.data.opts[chosenTech];
        // add display:none to the ones left out
        issueIds.filter((i, e) => !filteredIssues.includes($(e).attr("data-summary-id")))
            .each((i, e) => {
                if (!$(e).hasClass("filtered-out")) {
                    $(e).addClass("filtered-out");
                    $(e).prev().addClass("filtered-out");
                }
            });
    }

    function clearCallback() {
        $(".filtered-out").removeClass("filtered-out");
    }

    filtering();
});
