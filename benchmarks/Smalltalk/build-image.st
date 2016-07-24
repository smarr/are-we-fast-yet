| loader runner starting |
'Scripting.st' asFileReference fileIn.
'SomLoader-Compiler.st' asFileReference fileIn.
'SomLoader-Tests.st' asFileReference fileIn.

runner := SubunitRunner runPackage: 'SomLoader-Tests'.

Author fullName: 'SomLoader'.
loader := SomLoader load: '../SOM'.
loader createClasses.
loader createMethods.

loader := SomLoader loadFile: 'Run.som'.
loader createClasses.
loader createMethods.
loader := SomLoader loadFile: 'Mandelbrot.som'.
loader createClasses.
loader createMethods.


starting := Smalltalk saveAs: 'AWFY'.
starting ifTrue: [ ^ self ].

runner hasPassed
  ifTrue:  [ Smalltalk exit: 0 ]
  ifFalse: [ Smalltalk exit: 1 ]!
