| loader runner starting |
'Scripting.st' asFileReference fileIn.
'SomLoader-Compiler.st' asFileReference fileIn.
'SomLoader-Tests.st' asFileReference fileIn.

ScriptConsole println: '== Run Tests'.

runner := SubunitRunner runPackage: 'SomLoader-Tests'.

ScriptConsole println: '== Load Code'.

Author fullName: 'SomLoader'.
loader := SomLoader load: '.'.
loader createClasses.
loader createMethods.

ScriptConsole println: '== Safe and Exit'.

starting := Smalltalk saveAs: 'AWFY'.
starting ifTrue: [ ^ self ].

runner hasPassed
  ifTrue:  [ Smalltalk exit: 0 ]
  ifFalse: [ Smalltalk exit: 1 ]!
