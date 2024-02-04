FileStream startUp: true.

[| parentDir runner loader starting |
Utilities setAuthorInitials: 'SomLoader'.

FileStream stdout class compile: 'println: aString
    ^ self nextPutAll: aString; cr; flush' classified: '*SomLoader'.

FileStream stdout println: 'Installing SOM benchmarks'.

parentDir := (FileDirectory default fileNamed: Smalltalk documentPath)
	directoryEntry containingDirectory.

#('Scripting.st' 'SomLoader-Compiler.st' 'SomLoader-Tests.st') do: [:ea |
	(parentDir fileNamed: ea) fileIn].

ByteString compile: 'asFileReference
    ^ FileDirectory default on: self' classified: '*SomLoader'.

DosFileDirectory compile: 'entries
    ^ super entries reject: [:e | e fullName includes: $#]' classified: '*SomLoader'.

Behavior compile: 'subclass: a instanceVariableNames: b classVariableNames: c package: d
    ^ self subclass: a instanceVariableNames: b classVariableNames: c poolDictionaries: '''' category: d' classified: '*SomLoader'.

Character compile: 'canBeGlobalVarInitial
    ^ self isLetter' classified: '*SomLoader-override'.

Time class methodDict at: #primUTCMicrosecondsClock put: (Time class >> #utcMicrosecondClock).

SmallInteger methodDict at: #'&' put: (SmallInteger >> #bitAnd:).
SmallInteger methodDict at: #'%' put: (SmallInteger >> #\\).

Smalltalk class compile: 'exit: code
    ^ Smalltalk quitPrimitive' classified: '*SomLoader'.

Smalltalk at: #DiskStore put: FileDirectory.

"We don't need it, but we'd like the Json name for the benchmark."
Json rename: 'SqueakJson'.
JsonObject rename: 'SqueakJsonObject'.

FileDirectory class methodDict at: #delimiter put: (FileDirectory class >> #slash).

FileStream stdout println: '== Run Tests'.
runner := (Smalltalk classNamed: 'SomLoaderTests') suite run.

FileStream stdout println: '== Load Code'.
loader := (Smalltalk classNamed: 'SomLoader') load: parentDir fullName.
loader createClasses.
loader createMethods.

(runner hasPassed)
	ifFalse: [FileStream stderr println: '==== Test errors ===='; println: runner printString.].

FileStream stdout println: '== Save and Exit'.

PreferenceWizardMorph allInstances do: #delete.
SystemWindow allSubInstances do: #delete.

Smalltalk saveAs: 'AWFY_Squeak'.

] on: Error do: [:e |
    FileStream stdout nextPutAll: e printString; cr; flush.
].

Project current addDeferredUIMessage: [
  Smalltalk snapshot: true andQuit: true].
