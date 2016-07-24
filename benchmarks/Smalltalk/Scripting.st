SystemOrganization addCategory: #'Scripting-Examples'!
SystemOrganization addCategory: #Scripting!
SystemOrganization addCategory: #'Scripting-Tests'!

TestCase subclass: #SubunitTestExamples
	instanceVariableNames: ''
	classVariableNames: ''
	poolDictionaries: ''
	category: 'Scripting-Tests'!

!SubunitTestExamples methodsFor: 'as yet unclassified' stamp: 'StefanMarr 8/11/2012 12:54'!
expectedFailures
	^#(testExpectedFail testUnexpectedPass)! !

!SubunitTestExamples methodsFor: 'as yet unclassified' stamp: 'StefanMarr 8/11/2012 12:52'!
testAllFine
	self assert: true.! !

!SubunitTestExamples methodsFor: 'as yet unclassified' stamp: 'StefanMarr 8/11/2012 12:53'!
testException
	self foobarNotUnderstood.! !

!SubunitTestExamples methodsFor: 'as yet unclassified' stamp: 'StefanMarr 8/11/2012 12:53'!
testExpectedFail
	self fail.! !

!SubunitTestExamples methodsFor: 'as yet unclassified' stamp: 'StefanMarr 8/11/2012 12:53'!
testFail
	self fail.! !

!SubunitTestExamples methodsFor: 'as yet unclassified' stamp: 'StefanMarr 8/11/2012 12:54'!
testUnexpectedPass
	self assert: true.! !

TestResult subclass: #SubunitRunner
	instanceVariableNames: 'suite suitePosition suiteTime suiteFailures suiteErrors stream'
	classVariableNames: 'DefaultOutputTarget'
	poolDictionaries: ''
	category: 'Scripting'!

!SubunitRunner class methodsFor: 'defaults' stamp: 'StefanMarr 8/11/2012 13:59'!
defaultOutputTarget
	^ DefaultOutputTarget ifNil: [
		(Smalltalk hasClassNamed: #ScriptConsole) ifTrue: [^Smalltalk classNamed: #ScriptConsole].
		Transcript
		] ifNotNil: [DefaultOutputTarget]! !

!SubunitRunner class methodsFor: 'defaults' stamp: 'sm 5/25/2011 14:21'!
defaultOutputTarget: someStreamishThing
	"The output target should understand some parts of the standard stream protocol.
	 For details, see the actual usage. A Transcript is good, and most streams, too."
	DefaultOutputTarget := someStreamishThing! !

!SubunitRunner class methodsFor: 'scripting' stamp: 'StefanMarr 3/10/2012 18:06'!
run: arguments
	"This is the main entry when started as a script from the command line"
	| sel  |
	(arguments size < 2) ifTrue: [
		^ self usage.].

	sel := (arguments at: 2) asSymbol.
	sel = #runAllTests ifTrue: [
		^ self runAllTests.].
	
	(arguments size < 3) ifTrue: [
		^ self usage.].
	
	self perform: sel with: (arguments at: 3).
	! !

!SubunitRunner class methodsFor: 'running' stamp: 'sm 5/25/2011 16:05'!
runAllTests
	self runClasses: Smalltalk allClasses named: 'All'.! !

!SubunitRunner class methodsFor: 'running' stamp: ''!
runCategories: aCollectionOfStrings
	^ aCollectionOfStrings do: [ :each | self runCategory: each ]! !

!SubunitRunner class methodsFor: 'running' stamp: ''!
runCategory: aString
	^ self runClasses: (Smalltalk organization classesInCategory: aString) named: aString! !

!SubunitRunner class methodsFor: 'running' stamp: ''!
runClasses: aCollectionOfClasses named: aString
	| suite classes |
	suite := TestSuite named: aString.
	classes := (aCollectionOfClasses
		select: [ :each | (each includesBehavior: TestCase) and: [ each isAbstract not ] ])
			asSortedCollection: [ :a :b | a name <= b name ].
	classes isEmpty
		ifTrue: [ ^ self ].
	classes
		do: [ :each | each addToSuiteFromSelectors: suite ].
	^ self runSuite: suite! !

!SubunitRunner class methodsFor: 'running' stamp: ''!
runPackage: aString
	^ self runClasses: (RPackage organizer packageNamed: aString) classes
              named: aString! !

!SubunitRunner class methodsFor: 'running' stamp: ''!
runPackages: aCollectionOfStrings
	^ aCollectionOfStrings do: [ :each | self runPackage: each ]! !

!SubunitRunner class methodsFor: 'running' stamp: ''!
runSuite: aTestSuite
	^ self new 
		initializeOn: aTestSuite; 
		run! !

!SubunitRunner class methodsFor: 'scripting' stamp: 'StefanMarr 3/10/2012 17:48'!
usage
	ScriptConsole println: 'TestConsoleRunner <action> [argument]'.
	ScriptConsole println: ''.
	
	ScriptConsole println: '   <action> = runAllTests | runCategory: | runPackage: '.! !

!SubunitRunner methodsFor: 'reporting' stamp: 'StefanMarr 8/12/2012 08:23'!
beforeTestCase: aTestCase
	stream nextPut: Character lf.
	
	stream	nextPutAll: 'test: '.
	self   reportTestName: aTestCase.
	stream nextPut: Character lf.
	stream flush.! !

!SubunitRunner methodsFor: 'reporting' stamp: 'StefanMarr 8/12/2012 08:23'!
beforeTestSuite: aSuite
	stream nextPut: Character lf.
	
	stream nextPutAll: 'suite: ';    nextPutAll: aSuite name; nextPut: Character lf.
	stream nextPutAll: 'progress: '; nextPutAll: aSuite tests size asString; nextPut: Character lf.
	stream nextPutAll: 'time: '; nextPutAll: DateAndTime now asString; nextPut: Character lf.
	
	stream flush.
! !

!SubunitRunner methodsFor: 'reporting' stamp: 'StefanMarr 8/12/2012 08:23'!
errorTestCase: aTestCase with: error stack: stack
	stream nextPut: Character lf.
	
	stream nextPutAll: 'error: '.
	self   reportTestName: aTestCase.
	self   reportCause: error stack: stack.
	stream flush.! !

!SubunitRunner methodsFor: 'reporting' stamp: 'StefanMarr 8/12/2012 08:23'!
expectedFailTestCase: aTestCase
	stream nextPut: Character lf.
	
	stream nextPutAll: 'xfail: '.
	self   reportTestName: aTestCase.
	stream nextPut:    Character lf.
	stream flush.! !

!SubunitRunner methodsFor: 'reporting' stamp: 'StefanMarr 8/12/2012 08:24'!
failedTestCase: aTestCase with: error stack: stack
	stream nextPut: Character lf.

	stream nextPutAll: 'failure: '.
	self   reportTestName: aTestCase.
	self   reportCause: error stack: stack.
	stream flush.! !

!SubunitRunner methodsFor: 'initialization' stamp: 'StefanMarr 7/14/2011 17:18'!
initialize
	super initialize.
	stream := self class defaultOutputTarget! !

!SubunitRunner methodsFor: 'initialization' stamp: ''!
initializeOn: aTestSuite
	suite := aTestSuite.
	suitePosition := suiteTime := suiteFailures := suiteErrors := 0! !

!SubunitRunner methodsFor: 'reporting' stamp: 'StefanMarr 8/11/2012 13:12'!
reportCause: error stack: stack.	
	stream nextPutAll: ' ['; nextPut: Character lf.
	stream
		tab; nextPutAll: 'type: '; nextPutAll: (error class name); nextPut: Character lf;
		tab; nextPutAll: 'message:'; nextPutAll: (error messageText ifNil: [ error description ]); nextPut: Character lf;
		tab; nextPutAll: 'stack trace:'; nextPut: Character lf;
		nextPutAll: stack;
		nextPut: Character lf;
		nextPutAll: ']'; nextPut: Character lf! !

!SubunitRunner methodsFor: 'reporting' stamp: 'StefanMarr 8/11/2012 13:14'!
reportTestName: aTestCase
  stream
	nextPutAll: (aTestCase class category); nextPut: $.;
	nextPutAll: (aTestCase class name); nextPut: $.;
	nextPutAll: (aTestCase selector)! !

!SubunitRunner methodsFor: 'running' stamp: 'StefanMarr 8/11/2012 13:15'!
run
	| execBlock |
	execBlock :=  [ [ 
			self setUp.
			suiteTime := [ self runAll ]
				timeToRun ]
					ensure: [ self tearDown. ] ].
	
	(Smalltalk at: #Author ifAbsent: [ 
		execBlock value.
		^ self ])
			uniqueInstance
				ifUnknownAuthorUse: 'TestConsoleRunner'
				during: execBlock.! !

!SubunitRunner methodsFor: 'running' stamp: ''!
runAll
	suite tests do: [ :each | each run: self ]! !

!SubunitRunner methodsFor: 'running' stamp: 'StefanMarr 8/11/2012 13:17'!
runCase: aTestCase
	| error stack didSucceed |
	didSucceed := false.
	
	self beforeTestCase: aTestCase.
	
	[	aTestCase announce: TestCaseStarted withResult: self.
		aTestCase runCase.
		aTestCase announce: TestCaseEnded  withResult: self.
		self addPass: aTestCase.
		didSucceed := true.] 
	on: Halt , Error, TestFailure, self class failure, self class error
	do: [ :err |
			error := err.
			stack := self stackTraceString: err of: aTestCase.
			err sunitAnnounce: aTestCase toResult: self ].

	didSucceed ifTrue: [
		self successfulTestCase: aTestCase.
		^ self.].
	
	(error isNil or: [ aTestCase expectedFailures includes: aTestCase selector ]) ifTrue: [
		self expectedFailTestCase: aTestCase.
		^ self.
	].


	(error isKindOf: TestFailure)
		ifTrue: [self failedTestCase: aTestCase with: error stack: stack]
		ifFalse:[self errorTestCase:  aTestCase with: error stack: stack].
! !

!SubunitRunner methodsFor: 'running' stamp: 'StefanMarr 8/11/2012 12:30'!
setUp
	self beforeTestSuite: suite.
	
	"Initialize the test resources."
	suite resources do: [ :each |
		each isAvailable
			ifFalse: [ each signalInitializationError ] ]! !

!SubunitRunner methodsFor: 'private' stamp: ''!
stackTraceString: err of: aTestCase
	^ String streamContents: [ :str | 
		| context |
		context := err signalerContext.
		[ context isNil or: [ context receiver == aTestCase and: [ context methodSelector == #runCase ] ] ] whileFalse: [
			str print: context; nextPut: Character lf.
			context := context sender ] ] ! !

!SubunitRunner methodsFor: 'reporting' stamp: 'StefanMarr 8/12/2012 08:25'!
successfulTestCase: aTestCase
	stream nextPut: Character lf.
	
	stream 
	  nextPutAll: 'success: ';
	  nextPutAll: (aTestCase class category); nextPut: $.;
	  nextPutAll: (aTestCase class name); nextPut: $.;
	  nextPutAll: (aTestCase selector);
	  nextPut:    Character lf.
	  stream flush.! !

!SubunitRunner methodsFor: 'running' stamp: 'StefanMarr 8/11/2012 13:14'!
tearDown
	suite resources 
		do: [ :each | each reset ]! !

Object subclass: #HeadlessSave
	instanceVariableNames: ''
	classVariableNames: ''
	poolDictionaries: ''
	category: 'Scripting'!

!HeadlessSave class methodsFor: 'script entry' stamp: 'StefanMarr 4/5/2012 21:47'!
run: arguments 
	| proc |
	proc := [
		| basicName newName |
		ScriptConsole println: 'Do wait, let system run non-busy, and then save and quit'.
		(Delay forSeconds: 10) wait.
		ScriptConsole println: 'Do headless save now.'.
		basicName := SmalltalkImage current imageName withoutLast: '.image' size.
		newName := basicName, '-headless.image'.
		SmalltalkImage current saveAs: newName.
		SmalltalkImage current snapshot: false andQuit: true.
		] newProcess.
	
	proc priority: Processor userBackgroundPriority.
	proc resume.
! !

Object subclass: #HelloWorld
	instanceVariableNames: ''
	classVariableNames: ''
	poolDictionaries: ''
	category: 'Scripting-Examples'!

!HelloWorld commentStamp: 'StefanMarr 5/14/2011 16:42' prior: 0!
Simple example for how to use ScriptStarter.

HelloWorld prints all arguments given to #run:.!

!HelloWorld class methodsFor: 'script entry' stamp: ''!
run: arguments 
	ScriptConsole print: 'Hello World!!'.
	arguments
		do: [:elem | ScriptConsole print: elem].
	SmalltalkImage current snapshot: false andQuit: true! !

Object subclass: #Run
	instanceVariableNames: ''
	classVariableNames: ''
	poolDictionaries: ''
	category: 'Scripting'!

!Run commentStamp: 'StefanMarr 3/19/2012 21:24' prior: 0!
Run is a class that will take the command line arguments and will try to evaluate it as Smalltalk.!

!Run class methodsFor: 'script entry' stamp: 'StefanMarr 3/19/2012 21:47'!
run: args
	| str cmdClass |
	ScriptStarter quitVMAfterScriptExecution: false.
	
	cmdClass := args removeFirst.
	str := args reduce: [:a :b | a asString, ' ', b asString ].
	ScriptConsole println: str.
	ScriptConsole println: args asString.
	ScriptConsole println: 'Going to evaluate the string'.
	[ Compiler evaluate: str. ]
		ifError: [:err :rcvr | ScriptConsole println: 'Error: ', err].! !

Object subclass: #ScriptConsole
	instanceVariableNames: ''
	classVariableNames: 'IsRVM LastPrintedChar OutputToTranscript Position PrintTarget'
	poolDictionaries: ''
	category: 'Scripting'!

!ScriptConsole class methodsFor: 'streaming' stamp: ''!
<< aString
	self print: aString! !

!ScriptConsole class methodsFor: 'printing' stamp: ''!
cr
	self println: ''.! !

!ScriptConsole class methodsFor: 'initialization' stamp: 'StefanMarr 4/5/2012 22:08'!
detectAvailableMechanismForStdout
	"The prefered means is to use the stdout file handle.
	 Will try to fall back on the RoarVM primitive if necessary."
	| cls |
	(FileStream respondsTo: #stdout)
		ifTrue: [
			PrintTarget := FileStream stdout.
			IsRVM := false. "Not needed when stdout is used"
			^ self.
		].
		
	cls := Smalltalk at: #RVMOperations ifAbsent: [nil].
	IsRVM := (cls notNil and: [cls perform: #isRVM]).
	IsRVM 
		ifTrue: [ 
			PrintTarget := cls.
			^ self.
		]
		ifFalse: [
			"Squeak 4.2 provides a stream at FileStream>>stdout"
			
		].
	
	PrintTarget := Transcript.
	^ Transcript! !

!ScriptConsole class methodsFor: 'as yet unclassified' stamp: 'StefanMarr 8/11/2012 14:03'!
flush
	PrintTarget ifNotNil: [PrintTarget flush].! !

!ScriptConsole class methodsFor: 'initialization' stamp: 'StefanMarr 12/31/2011 01:27'!
initialize
	"ScriptConsole initialize"
	OutputToTranscript := false.
	self detectAvailableMechanismForStdout.
	Position := 0.! !

!ScriptConsole class methodsFor: 'streaming' stamp: 'StefanMarr 12/5/2011 18:11'!
nextPut: somethingAsString
	self print: somethingAsString! !

!ScriptConsole class methodsFor: 'streaming' stamp: 'StefanMarr 12/31/2011 01:23'!
nextPutAll: somethingAsString
	self print: somethingAsString! !

!ScriptConsole class methodsFor: 'printing' stamp: ''!
outputToTranscript
	^ OutputToTranscript! !

!ScriptConsole class methodsFor: 'printing' stamp: ''!
outputToTranscript: aBool
	OutputToTranscript := aBool! !

!ScriptConsole class methodsFor: 'read, write, position' stamp: 'StefanMarr 12/31/2011 01:29'!
peekLast
	^ LastPrintedChar! !

!ScriptConsole class methodsFor: 'read, write, position' stamp: 'StefanMarr 12/31/2011 01:27'!
position
	^ Position! !

!ScriptConsole class methodsFor: 'printing' stamp: ''!
print: somethingAsString
	self print: somethingAsString withLineEnding: ''.! !

!ScriptConsole class methodsFor: 'printing' stamp: 'StefanMarr 12/31/2011 01:30'!
print: somethingAsString withLineEnding: lineEnd
	"Will output the somethingAsString to stdout using one of the available mechansims and to the transcript if requested"
	| output |
	output := somethingAsString asString.
	Position := Position + output size.
	output size > 0 ifTrue: [
		LastPrintedChar := output last].
	
	PrintTarget ifNotNil: [
		IsRVM ifTrue: [ PrintTarget print: output. ]
			   ifFalse: [ PrintTarget nextPutAll: (output, lineEnd) ].
	].

	(OutputToTranscript or: [PrintTarget isNil])
		ifTrue: [
			Transcript show: output, lineEnd.
		].! !

!ScriptConsole class methodsFor: 'printing' stamp: ''!
println: somethingAsString
	self print: somethingAsString withLineEnding: String crlf.! !

!ScriptConsole class methodsFor: 'printing' stamp: 'StefanMarr 12/31/2011 01:22'!
space
	"Append a space character to the receiver."

	self nextPut: Character space! !

!ScriptConsole class methodsFor: 'printing' stamp: 'StefanMarr 12/31/2011 01:27'!
tab
	"Append a tab character to the receiver."

	self nextPut: Character tab! !

Object subclass: #ScriptStarter
	instanceVariableNames: ''
	classVariableNames: 'IsResuming QuitVM SafeBeforeQuit'
	poolDictionaries: ''
	category: 'Scripting'!

!ScriptStarter commentStamp: 'StefanMarr 5/14/2011 16:29' prior: 0!
ScriptStarter provides a startUp method and registers itself on the startUpList to enable scripting using command-line arguments.

The first argument on the command-line is used to identify a class on which #run: is invoked with the arguments as an array.

#run: can be used similar to classic main(String[] args) methods in other languages. !

!ScriptStarter class methodsFor: 'helper' stamp: 'StefanMarr 3/19/2012 21:44'!
executeRunFor: scriptClassName with: arguments
	| scriptClass |
	scriptClass := Smalltalk classNamed: scriptClassName.
	scriptClass ifNotNil: [
		scriptClass run: arguments.
		QuitVM ifTrue: [
			Smalltalk snapshot: SafeBeforeQuit andQuit: true.
		].
	].! !

!ScriptStarter class methodsFor: 'helper' stamp: 'StefanMarr 8/6/2012 18:37'!
getSystemAttributes
	| arguments arg i |
	arguments := OrderedCollection new.
	i := 2.
	[[arg := Smalltalk vm getSystemAttribute: i] value == nil]
		whileFalse: [arguments addLast: arg.
			i := i + 1].
	^ arguments! !

!ScriptStarter class methodsFor: 'initialization' stamp: 'sm 5/30/2011 22:33'!
initialize
	"Automatically install ScriptStarter when the code is filed in."
	self install.
	
	QuitVM := true.
	SafeBeforeQuit := false.
	IsResuming := false.! !

!ScriptStarter class methodsFor: 'system startup entry' stamp: 'sm 5/25/2011 12:41'!
install
	"Will register ScriptStarter on the StartUpList to activate it on VM startup."
	| pref |
	Smalltalk addToStartUpList: ScriptStarter.
	
	"Disable the loading of documents on startup"
	pref := Smalltalk at: #Preferences ifAbsent: [nil].
	pref	ifNotNil: [
			(pref respondsTo: #disable:)
				ifTrue: [	pref disable: #readDocumentAtStartup. ]
			]
		ifNil: [
			(Smalltalk globals at: #AbstractLauncer ifAbsent: [nil])
				ifNotNilDo: [:launcher |
					launcher readDocumentAtStartup: false
				]
			].! !

!ScriptStarter class methodsFor: 'configuration' stamp: ''!
quitVMAfterScriptExecution
	^ QuitVM! !

!ScriptStarter class methodsFor: 'configuration' stamp: ''!
quitVMAfterScriptExecution: aBool
	QuitVM := aBool! !

!ScriptStarter class methodsFor: 'configuration' stamp: ''!
saveImageBeforeQuit
	^ SafeBeforeQuit! !

!ScriptStarter class methodsFor: 'configuration' stamp: ''!
saveImageBeforeQuit: aBool
	SafeBeforeQuit := aBool! !

!ScriptStarter class methodsFor: 'system startup entry' stamp: 'sm 5/30/2011 22:35'!
startUp
	"startUp is invoked during startup.
	 ScriptStarter has to be registered in the SystemDirectory's StartUpList
	 See ScriptStarter>>install"
	
	| arguments scriptClassName |
	"Ok, only, and really only during startup"
	IsResuming ifFalse: [ Transcript show: 'ScriptStarter did not execute a given script, since it assumes to be executed after a snapshot and not during startup.'.
		^ self. ].
	
	"First make sure that stdout output can be used if available."
	ScriptConsole detectAvailableMechanismForStdout.
	
	arguments := self getSystemAttributes.
	arguments size > 0
		ifTrue: [scriptClassName := arguments at: 1.
			self executeRunFor: scriptClassName with: arguments.]! !

!ScriptStarter class methodsFor: 'system startup entry' stamp: 'sm 5/30/2011 22:30'!
startUp: resuming
	IsResuming := resuming.
	^ super startUp: resuming.! !

!ScriptStarter class methodsFor: 'transporter' stamp: 'StefanMarr 4/5/2012 21:48'!
transportersForFileOutMenu
	^ { (Smalltalk at: #Transporter ifAbsent: [^#()])
			forPackage: (PackageInfo named: 'Scripting') }! !

!ScriptStarter class methodsFor: 'system startup entry' stamp: ''!
uninstall
	"Will remove ScriptStarter from the StartUpList"
	Smalltalk removeFromStartUpList: ScriptStarter.
	
	self flag: 'TODO: restore original setting for readDocumentAtStartup, see >>install.'.
	Transcript show: 'You might need to restore the original setting for readDocumentAtStartup'.! !

Object subclass: #UpdateImage
	instanceVariableNames: ''
	classVariableNames: ''
	poolDictionaries: ''
	category: 'Scripting'!

!UpdateImage class methodsFor: 'script entry' stamp: 'sm 5/30/2011 22:41'!
run: args
	| proc |

	"The loading needs to be done after everything in startup has been done.
	 Otherwise the snaphost routine gets confused.
	 Thus, we do that ourselfs."
	ScriptStarter quitVMAfterScriptExecution: false.
	
	"Use a delayed process to do the actual work"
	proc := [ (Delay forSeconds: 5) wait.
	  "Create an up-to-date image"
	  (FileStream readOnlyFileNamed: 'FileInAllRenaissanceCode.st') fileIn.
	Transcript show: 'Snapshot and Quit now...'. 
	 Smalltalk snapshot: true andQuit: true. ] newProcess.

	proc name: 'Update Image'.
	proc resume.
	! !
ScriptStarter initialize!
ScriptConsole initialize!
