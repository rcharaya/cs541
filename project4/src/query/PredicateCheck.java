package query;

import relop.Predicate;
import relop.Tuple;

public class PredicateCheck {
	// (A \/ B) /\ (C \/ D)
	static boolean check(Predicate[][] preds, Tuple tuple) {
		for(Predicate[] disPred : preds) {	// A \/ B
			boolean broke = false;
			for(Predicate literals : disPred) { // A
				if(literals.evaluate(tuple)) {
					broke = true;
					break;
				}
			}
			if(!broke) {
				// All predicates in A\/B were false
				return false;
			}
		}
		return true;
	}
}
