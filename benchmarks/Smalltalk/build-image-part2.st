| loader runner starting |
ScriptConsole println: '== Run Tests'.

runner := SubunitRunner runPackage: 'SomLoader'.

ScriptConsole println: '== Load Code'.

loader := SomLoader load: '.'.
loader createClasses.
loader createMethods.

ScriptConsole println: '== Safe and Exit'.

starting := Smalltalk saveAs: 'AWFY_Pharo'.
starting ifTrue: [ ^ self ].

runner hasPassed
  ifTrue:  [ Smalltalk exit: 0 ]
  ifFalse: [ Smalltalk exit: 1 ]!
