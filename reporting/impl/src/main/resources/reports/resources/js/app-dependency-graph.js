var datasets = [];
function app_dependencies(dataset) {
	datasets.push(dataset);
}

var kinds = {
  Ear: '#vertex-Ear',
  War: '#vertex-War',
  WarApp: '#vertex-WarApp',
  Jar: '#vertex-Jar',
}
if (typeof showExternalJars !== 'undefined' && showExternalJars !== null && showExternalJars == true) {
  kinds.ExternalJar = '#vertex-ExternalJar';
}


angular.module('appDependencies', ['applicationUI'])
  .run(function($rootScope) {
        var index = 0;
        $rootScope.data = datasets[index];
        $rootScope.kinds = kinds;

        $rootScope.poke = function() {
           index += 1;
           $rootScope.data = datasets[index % datasets.length];
        };

        $rootScope.$on("select", function(ev, item) {
           var text = "";
           if (item)
               text = "Selected: " + item.metadata.name; //+ " - JDK: " + item.metadata.jdk + " - Size: " + item.metadata.size + " - Classes: "  + item.metadata.classes
           angular.element(document.getElementById("selected")).text(text);
        });
    });