function highlightLine(line, posS, posE, clzName) {
	var posI = 0;

    $($(line).contents(), this).each(function(j) {
    	var text = $(this).text();
    	var textLen = text.length;
    	var prevPos = posI;
    	posI += textLen;

    	if(posI <= posE && posI >= posS) {
    		if(this.nodeType == 3) {
    			var tagSpan = "<span class='"+clzName+"'></span>";
    			$(this).wrap(tagSpan);	
    		}
    		else {
    			$(this).addClass(clzName);
    		}
    	}
    	else if(posI > posE) {
    		return false;
    	}
    });

    var clzSelector = "span."+clzName;
    $(clzSelector).css("background", "#DDE");
    //console.log(clzSelector);
    $(clzSelector).hover(
    	function() {
    		$(clzSelector).css("background", "#FFE066");
    		$(clzSelector).css("cursor", "pointer");

    	},
    	function() {
    		$(clzSelector).css("background", "#DDF");
    	}

    );
    return false;
}

function processEnd(line, posE, clzName) {
	var posI = 0;
	
	
    $($(line).contents(), this).each(function(j) {
    	var prevClass = "";

    	if($(this).attr("class")) {
			prevClass = ($(this).attr("class"));
			prevClass = prevClass.replace(clzName, "");
		}
    	var text = $(this).text();
    	var textLen = text.length;
    	var prevPos = posI;
    	posI += textLen;

    	var diffE = (posE-posI);
    	if(diffE < 0) {
    		var splitLen = textLen + diffE;
    		var before = ($(this).text().substring(0, splitLen));
    		var after = ($(this).text().substring(splitLen));
    		$(this).replaceWith("<span class='sE "+clzName+" "+prevClass+"'>"+before+"</span><span class='sE "+prevClass+"'>"+after+"</span>");
    		return false;
    	}	

    	if(posI > posE) {
    		return false;
    	}
    });
}

function processStart(line, posS, clzName) {
	var posI = 0;
	
    $($(line).contents(), this).each(function(j) {
    	var prevClass = "";

    	if($(this).attr("class")) {
			prevClass = ($(this).attr("class"));
			prevClass = prevClass.replace(clzName, "");
    	}
    	var text = $(this).text();
    	var textLen = text.length;
    	var prevPos = posI;
    	posI += textLen;

    	var diffS = (posS-prevPos)-1;
    	if(diffS > 0 && diffS < textLen) {
    		var before = ($(this).text().substring(0, diffS));
    		var after = ($(this).text().substring(diffS));

    		$(this).replaceWith("<span class='sS "+prevClass+"'>"+before+"</span><span class='sS "+clzName+" "+prevClass+"''>"+after+"</span>");
    		return false;
    	}

    	if(posI > posS) {
    		return false;
    	}
    });
}

var highlightPosition = function (lineI, posS, posE, uniqueClz) {
	if(posS < 1) {
		posS = 1;
	}
	if(posE < posS) {
		posE = posS;
	}

	var selector = "ol.snippet-num > li:nth-child("+lineI+")";

	line = $(selector);
	if(!line) {
		return false;
	}

	processStart(line, posS, uniqueClz);
	processEnd(line, posE, uniqueClz);
	highlightLine(line, posS, posE, uniqueClz);
}

$(document).ready(function() {
    //highlightPosition(6, 8, 34, "uniqueExample");
});
