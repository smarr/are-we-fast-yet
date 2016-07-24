| args |
"Adapt array to expectation of harness"
args := Array with: nil withAll: Smalltalk arguments.

Harness new run: args.!
