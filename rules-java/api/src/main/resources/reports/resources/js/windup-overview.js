$(document).on('click', '.panel-heading', function(e){
    var $this = $(this);
	if(!$this.hasClass('panel-collapsed')) {
		$this.parents('.panel').find('.panel-body').slideUp();
      $this.parents('.panel').removeClass('panel-boarding').addClass('panel-collapsed');
		$this.addClass('panel-collapsed');
		$this.find('i').removeClass('glyphicon-chevron-down').addClass('glyphicon-chevron-up');
	} else {
		$this.parents('.panel').find('.panel-body').slideDown();
      $this.parents('.panel').addClass('panel-boarding');
		$this.removeClass('panel-collapsed').addClass('panel-boarding');;
		$this.find('i').removeClass('glyphicon-chevron-up').addClass('glyphicon-chevron-down');
	}
})
$('#collapseAll').toggle();

//
function expandAll(){
	$('.panel-body').slideDown();
	$('.panel-heading').find('i').removeClass('glyphicon-chevron-up').addClass('glyphicon-chevron-down');
	$('.panel-heading').addClass('panel-boarding').removeClass('panel-collapsed');
	$('.panel-heading').parents('.panel').addClass('panel-boarding');	
	$('#expandAll').toggle();	
	$('#collapseAll').toggle();
}

function collapseAll(){
	$('.panel-body').slideUp();
	$('.panel-heading').find('i').removeClass('glyphicon-chevron-down').addClass('glyphicon-chevron-up');
	$('.panel-heading').addClass('panel-collapsed').removeClass('panel-boarding');
	$('.panel-heading').parents('.panel').addClass('panel-collapsed').removeClass('panel-boarding');
	$('#expandAll').toggle();
	$('#collapseAll').toggle();
}