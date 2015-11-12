$(document).on('click', '.panel-heading', function(e){
    var $this = $(this);
	if(!$this.hasClass('panel-collapsed')) {
		$this.parents('.panel').find('.panel-body').slideUp();
        $this.parents('.panel').removeClass('panel-boarding');
		$this.addClass('panel-collapsed');
		$this.find('i').removeClass('glyphicon-chevron-down').addClass('glyphicon-chevron-up');
	} else {
		$this.parents('.panel').find('.panel-body').slideDown();
        $this.parents('.panel').addClass('panel-boarding');
		$this.removeClass('panel-collapsed');
		$this.find('i').removeClass('glyphicon-chevron-up').addClass('glyphicon-chevron-down');
	}
})
