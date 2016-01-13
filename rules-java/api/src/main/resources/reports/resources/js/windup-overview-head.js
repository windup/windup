
/*
 * This file needs to be loaded at the beginning of the page, because its code is used throughout the document.
 * With some effort it could be moved back to the end, if it's worth it.
 */


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


/**
 * Converts the ProjectNode-based tree to jsTree data structure.
 * @param {ProjectNode} windupTree The root ProjectNode of the root project.
 * @param {Object} parentObj  Used internally for recursive calls.
 * @returns {Object}  The resulting jsTree data object. Use in core: { data: ... }
 */
function prepareJsTreeData(windupTree, parentObj /* undefined for root */)
{
    var curNode = windupTree;
    var subNode = null;
    // Expected format of the node (there are no required fields)
    var curObj = {
        id          : "treeNode-" + curNode.id,
        text        : curNode.name,
        //icon:       : curNode.,/// TODO
        li_attr     : (curNode.tags.constructor !== Array) ? "" : ("tag-" + curNode.tags.join(" tag-")),
        state       : {
            opened    : true,
            //selected  : false,
        },
        children    : [],
    };

    if (parentObj != undefined)
        parentObj.children.push(curObj);

    for(var i = 0; i < curNode.children.length; i++) {
        prepareJsTreeData(curNode.children[i], curObj);
    }

    return curObj;
}




/* ========   General functions ========== */

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
