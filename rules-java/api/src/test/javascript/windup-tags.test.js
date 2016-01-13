var res;

res = tagService.getNearestRoots("orion").map(function(arg){return arg.name})
//Array [ "java-ee" ]
Assert.assertEquals(res.length, 1);
Assert.assertEquals(res[0], "java-ee");

res = tagService.getNearestRoots("jms").map(function(arg){return arg.name})
//Array [ "messaging" ]
Assert.assertEquals(res.length, 1);
Assert.assertEquals(res[0], "messaging");

res = tagService.getNearestRoots("wsdl").map(function(arg){return arg.name})
//Array [ "web-service" ]
Assert.assertEquals(res.length, 1);
Assert.assertEquals(res[0], "web-service");
