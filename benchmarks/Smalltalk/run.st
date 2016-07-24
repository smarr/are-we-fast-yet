| args len |
"Adapt array to expectation of harness"
len := Smalltalk arguments size + 1.
args := Array new: len.
args replaceFrom: 2 to: len with: Smalltalk arguments startingAt: 1.

Harness new run: args.!
