<li>
    <a href="#" class="feedback-nav-btn jiraFeedbackTrigger"><i class="glyphicon glyphicon-comment"></i> Send Feedback
    </a>
</li>

<#if !NAVBAR_JS_ALREADY_DISPLAYED??>
    <#assign NAVBAR_JS_ALREADY_DISPLAYED = 1>

    <script type="text/javascript"
            src="https://issues.redhat.com/s/f215932e68571747ac58d0f5d554396f-T/en_US-r7luaf/6346/82/1.4.16/_/download/batch/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector.js?locale=en-US&amp;collectorId=8b9e338b"></script>

    <script type="text/javascript">

        var FEEDBACK_JS_ADDED = false;
        var FEEDBACK_FORM_TRIGGER = null;

        function displayFeedbackForm() {
            FEEDBACK_FORM_TRIGGER();
        }

        window.ATL_JQ_PAGE_PROPS = {
            "triggerFunction": function (showCollectorDialog) {
                FEEDBACK_FORM_TRIGGER = showCollectorDialog;
            }
        };

        document.addEventListener("DOMContentLoaded", function (event) {
            jQuery(".jiraFeedbackTrigger").click(function (e) {
                e.preventDefault();
                displayFeedbackForm();
            });
        });
    </script>
</#if>
