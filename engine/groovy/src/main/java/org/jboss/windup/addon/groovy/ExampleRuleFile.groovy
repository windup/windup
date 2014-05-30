
// ---- A bit of script initialization ----------------------------

// use the script binding for silent sentence words like "to", "the"
binding = new CustomBinding()


rule "XYZ371" when {
} perform {
}



def r = rule "ruleid1234" when {
    println "evaluating"; return true
} perform { println "performing" };

r.ex();

class RuleBuilder {
    String id;
    Closure c;
    Closure p;

    RuleBuilder(String id) {
        this.id = id;
    }

    def when (Closure c) {
        this.c = c;
        return this;
    }

    def perform (Closure p) {
        this.p = p;
        return this;
    }

    def ex()
    {
        if( c() )
            p();
    }

    def rule (Closure c) {
        c()
        return this
    }

    String toString() {
        "ID: $id, Closure: $c"
    }
}

class CustomBinding extends Binding {
    def getVariable(String word) {
        // return System.out when the script requests to write to 'out'
        if (word == "out") System.out

        // don't thrown an exception and return null
        // when a silent sentence word is used,
        // like "to" and "the" in our DSL
        null
    }
}

def rule(ruleID) { return new RuleBuilder(ruleID) }
