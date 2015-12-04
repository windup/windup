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



/**
 * Returns the keys of the given map.
 * TODO: Can be replaced by Object.keys() ?
 */
function getKeys(map){
    var keys = [];
    for (var key in map)
        if (map.hasOwnProperty(key))
            keys.push(key);
    return keys;
}

/**
 * Sorts the keys of given object.
 * In JavaScript, the object properties keys order is maintained. This can be used for an ordered map.
 * @param int topN  If set, returned map contains only the first topN items.
 */
function sortMapByValues(map, topN) {
    var newMap = {};
    var keysSorted = Object.keys(map).sort(function(a,b){return map[a]-map[b]});
    var key;
    if (topN == undefined || topN <= 0)
        topN = keysSorted.length;
    var count = Math.min(keysSorted.length, topN);
    for( var i = 0; i < count; i++ ){
        key = keysSorted[i];
        newMap[key] = map[key];
    }
    return newMap;
}
