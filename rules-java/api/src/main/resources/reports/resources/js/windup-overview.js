
/* ========   Collapsible panels ========== */

function togglePanelSlide(event)
{
    var element = $(this);
	if(element.hasClass("panel-collapsed")) {
        element.parents(".panel").find(".panel-body").slideDown();
        element.parents(".panel").addClass("panel-boarding");
		element.removeClass("panel-collapsed");
		element.find("i").removeClass("glyphicon-expand").addClass("glyphicon-collapse-up");
	}
    else {
		element.parents(".panel").find(".panel-body").slideUp();
        element.parents(".panel").removeClass("panel-boarding");
		element.addClass("panel-collapsed");
		element.find("i").removeClass("glyphicon-collapse-up").addClass("glyphicon-expand");
	}
}

function expandAll(){
	$('.panel-body').slideDown();
	$('.panel-heading').find('i').removeClass('glyphicon-expand').addClass('glyphicon-collapse-up');
	$('.panel-heading').addClass('panel-boarding').removeClass('panel-collapsed');
	$('.panel-heading').parents('.panel').addClass('panel-boarding');
	$('#expandAll').toggle();
	$('#collapseAll').toggle();
}

function collapseAll(){
	$('.panel-body').slideUp();
	$('.panel-heading').find('i').removeClass('glyphicon-collapse-up').addClass('glyphicon-expand');
	$('.panel-heading').addClass('panel-collapsed').removeClass('panel-boarding');
	$('.panel-heading').parents('.panel').addClass('panel-collapsed').removeClass('panel-boarding');
	$('#expandAll').toggle();
	$('#collapseAll').toggle();
}



/* ========   TreeView stuff        ========== */

var ProjectNode = function(name, id) {
    this.name = name;
    this.id = id;
    this.children = [];
    this.parent = null;
};
ProjectNode.prototype.addSubproject = function(subproject) {
    this.children.push(subproject);
    subproject.parent = this;
};

ProjectNode.prototype.getParent = function() {
    return this.parent;
};

ProjectNode.prototype.getDepth = function() {
    var d = 0;
    var cur = this.parent;
    var visited = [];
    while (cur != null) {
        d++;
        cur = cur.parent;
        if(d > 20){
            if (visited.contains(cur))
                throw "Cycle found in project relations, involving: " + cur.toString();
            visited.push(cur);
        }
    }
    return d;
};

String.prototype.repeat = function( num ) {
    return new Array(num + 1).join( this );
}

ProjectNode.prototype.toString = function() {
    return "    ".repeat(this.getDepth()) + "ProjectNode{ #" + this.id + ' "' + this.name + '" }';
};
