## Purpose ##

This library allows you to tabulate ballots using different voting methods.

Currently only Condorcet Ranked Pairs is implemented.  Other methods are welcome.

## Example ##

For detailed examples of how to use, check out the unit tests.

Here's a quick example to whet your appetite.

    ; votes are seq of maps
    ;   each map represents a single ranked ballot
    ;     key in the map represents a candidate
    ;     value is the candidate's rank (lower is better)
    user=> (def votes '({"a" 1 "b" 2 "c" 3} {"a" 1 "c" 2 "b" 3}))
    ;
    ; candidates is a seq that represents the set of candidates
    user=> (def candidates '("a" "b" "c"))
    ;
    ; tab-ranked-pairs will give you the winner
    user=> (vote.condorcet/tab-ranked-pairs candidates votes)
    "a"

