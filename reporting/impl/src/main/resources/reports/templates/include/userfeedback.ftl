<li><button id="jiraFeedbackTrigger" class="btn btn-success btn-block">Send Feedback</button></li>
<script type="text/javascript" src="https://issues.jboss.org/s/f215932e68571747ac58d0f5d554396f-T/en_US-r7luaf/6346/82/1.4.16/_/download/batch/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector.js?locale=en-US&collectorId=8b9e338b"></script>
<script type="text/javascript">
	window.ATL_JQ_PAGE_PROPS = {
		"triggerFunction": function(showCollectorDialog) {
			jQuery("#jiraFeedbackTrigger").click(function(e) {
				e.preventDefault();
				showCollectorDialog();
			});
		}
	};
</script>