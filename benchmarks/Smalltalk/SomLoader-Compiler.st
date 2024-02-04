SystemOrganization addCategory: #'SomLoader-Compiler'!

Object subclass: #SomClassGenerationContext
	instanceVariableNames: 'superName isClassSide instanceFields instanceMethods classFields classMethods theName'
	classVariableNames: ''
	poolDictionaries: ''
	category: 'SomLoader-Compiler'!

Object subclass: #SomParser
	instanceVariableNames: 'cgenc sym text lexer shouldReadSources'
	classVariableNames: 'BinaryOpSyms KeywordSelectorSyms SingleOpSyms TextStreamCls'
	poolDictionaries: ''
	category: 'SomLoader-Compiler'!

SomParser subclass: #SomClassParser
	instanceVariableNames: ''
	classVariableNames: ''
	poolDictionaries: ''
	category: 'SomLoader-Compiler'!

SomParser subclass: #SomMethodParser
	instanceVariableNames: ''
	classVariableNames: ''
	poolDictionaries: ''
	category: 'SomLoader-Compiler'!

!SomClassGenerationContext commentStamp: 'StefanMarr 7/21/2016 13:09' prior: 0!
I represent a class that is currently parsed and I will create the final class object from this information.!

!SomClassGenerationContext methodsFor: 'adding' stamp: 'StefanMarr 5/13/2012 17:56'!
addField: field
	self isClassSide
		ifTrue:  [classFields add: field]
		ifFalse: [instanceFields add: field].! !

!SomClassGenerationContext methodsFor: 'adding' stamp: 'rkrk 4/25/2009 23:45'!
addFields: fieldNames

	^ self isClassSide
		ifTrue: [self classFields: fieldNames]
		ifFalse: [self instanceFields: fieldNames]! !

!SomClassGenerationContext methodsFor: 'adding' stamp: 'StefanMarr 5/13/2012 12:26'!
addInstanceField: field
	instanceFields add: field.! !

!SomClassGenerationContext methodsFor: 'adding' stamp: 'StefanMarr 7/21/2016 14:51'!
addMethod: methodSource
	^ self isClassSide
		ifFalse: [self instanceMethods add: methodSource]
		ifTrue: [	self classMethods add: methodSource]! !

!SomClassGenerationContext methodsFor: 'variables' stamp: 'rkrk 5/29/2009 01:32'!
allClassFieldsIn: universe

	| superClass |
	self superName = 'nil' ifTrue: [^ self classFields].
	superClass := universe
		globalAt: self superName asSymbol
		ifAbsent: ["self error: 'Field lookup: Cannot find my superclass ' , self superName , '!!'"
			"Transcript
				cr;
				show: 'Field lookup: Cannot find my superclass ' , self superName , '!!'."
			^ self classFields].
	^ superClass somClass allInstanceFields , self classFields.! !

!SomClassGenerationContext methodsFor: 'variables' stamp: 'rkrk 5/29/2009 01:32'!
allInstanceFieldsIn: universe

	| superClass |
	self superName = 'nil' ifTrue: [^ self instanceFields].
	superClass := universe
		globalAt: self superName asSymbol
		ifAbsent: ["self error: 'Field lookup: Cannot find my superclass ' , self superName , '!!'"
			"Transcript
				cr;
				show: 'Field lookup: Cannot find my superclass ' , self superName , '!!'."
			^ self instanceFields].
	^ superClass allInstanceFields , self instanceFields.! !

!SomClassGenerationContext methodsFor: 'assembling' stamp: 'rkrk 6/5/2009 01:28'!
assembleIn: universe

	^ self
		assembleIn: universe
		stub: (self createEmptyClassIn: universe).! !

!SomClassGenerationContext methodsFor: 'assembling' stamp: 'rkrk 6/5/2009 01:25'!
assembleIn: universe stub: classStub

	self
		initInstanceSideOf: classStub in: universe;
		initClassSideOf: classStub in: universe;
		initFieldsOf: classStub in: universe.
	^ classStub! !

!SomClassGenerationContext methodsFor: 'accessing' stamp: 'rkrk 4/25/2009 18:50'!
classFields
	^ classFields! !

!SomClassGenerationContext methodsFor: 'accessing' stamp: 'rkrk 4/25/2009 18:50'!
classFields: anObject
	classFields := anObject! !

!SomClassGenerationContext methodsFor: 'accessing' stamp: 'rkrk 4/25/2009 18:50'!
classMethods
	^ classMethods! !

!SomClassGenerationContext methodsFor: 'accessing' stamp: 'rkrk 4/25/2009 18:50'!
classMethods: anObject
	classMethods := anObject! !

!SomClassGenerationContext methodsFor: 'assembling' stamp: 'StefanMarr 5/19/2012 21:39'!
createEmptyClassIn: universe

	| superclass |
	superclass := universe
		globalAt: self superName
		ifAbsent: [universe loadSOMClass: self superName].
	^ universe classClass
		inUniverse: universe
		withSomSuperClass: superclass.! !

!SomClassGenerationContext methodsFor: 'initialization' stamp: 'rkrk 4/25/2009 22:04'!
defaultSuperClassName

	^ 'Object'! !

!SomClassGenerationContext methodsFor: 'testing' stamp: 'rkrk 4/25/2009 23:01'!
hasField: fieldName

	^ self isClassSide
		ifTrue: [self classFields includes: fieldName]
		ifFalse: [self instanceFields includes: fieldName]! !

!SomClassGenerationContext methodsFor: 'assembling' stamp: 'StefanMarr 5/18/2012 17:12'!
initClassSideOf: somClass in: universe

	| fields methods |
	self flag: #rkrk. "Duplication with #initInstanceSideOf:in:"
	somClass somClass somName: (universe newSymbol: self theName , ' class').
	fields := self classFields collect: [:each | universe newSymbol: each].
	somClass somClass somInstanceFields: (universe newArrayWithAll: fields).
	methods := self classMethods collect: [:each | each assembleIn: universe].
	somClass somClass somInstanceInvokables: (universe newArrayWithAll: methods).! !

!SomClassGenerationContext methodsFor: 'assembling' stamp: 'StefanMarr 5/18/2012 17:17'!
initFieldsOf: somClass in: universe

	| nAdditionalFields nilObject |
	nAdditionalFields := somClass somClass somInstanceFields size.
	nilObject := universe globalNil.
	somClass fields: (Array new: nAdditionalFields).
	somClass numberOfFields - nAdditionalFields + 1
		to: somClass numberOfFields
		do: [:i | somClass fieldAtIndex: i put: nilObject].! !

!SomClassGenerationContext methodsFor: 'assembling' stamp: 'StefanMarr 5/18/2012 00:06'!
initInstanceSideOf: somClass in: universe

	| fields methods |
	somClass somName: (universe newSymbol: self theName).
	fields := self instanceFields collect: [:each | universe newSymbol: each].
	somClass somInstanceFields: (universe newArrayWithAll: fields).
	methods := self instanceMethods collect: [:each | each assembleIn: universe].
	somClass somInstanceInvokables: (universe newArrayWithAll: methods).! !

!SomClassGenerationContext methodsFor: 'initialization' stamp: 'mkl 4/29/2009 13:50'!
initialize

	super initialize.
	self
		isClassSide: false;
		superName: self defaultSuperClassName;
		instanceFields: OrderedCollection new;
		classFields: OrderedCollection new;
		instanceMethods: OrderedCollection new;
		classMethods: OrderedCollection new;
		theName: ''.! !

!SomClassGenerationContext methodsFor: 'accessing' stamp: 'rkrk 4/25/2009 18:50'!
instanceFields
	^ instanceFields! !

!SomClassGenerationContext methodsFor: 'accessing' stamp: 'rkrk 4/25/2009 18:50'!
instanceFields: anObject
	instanceFields := anObject! !

!SomClassGenerationContext methodsFor: 'accessing' stamp: 'rkrk 4/25/2009 18:50'!
instanceMethods
	^ instanceMethods! !

!SomClassGenerationContext methodsFor: 'accessing' stamp: 'StefanMarr 5/17/2012 23:48'!
instanceMethods: anObject
	instanceMethods := anObject! !

!SomClassGenerationContext methodsFor: 'accessing' stamp: 'rkrk 4/25/2009 18:50'!
isClassSide
	^ isClassSide! !

!SomClassGenerationContext methodsFor: 'accessing' stamp: 'rkrk 4/25/2009 18:50'!
isClassSide: anObject
	isClassSide := anObject! !

!SomClassGenerationContext methodsFor: 'accessing' stamp: 'rkrk 4/25/2009 18:50'!
superName
	^ superName! !

!SomClassGenerationContext methodsFor: 'accessing' stamp: 'rkrk 4/25/2009 18:50'!
superName: anObject
	superName := anObject! !

!SomClassGenerationContext methodsFor: 'accessing' stamp: 'rkrk 4/25/2009 18:51'!
theName
	^ theName! !

!SomClassGenerationContext methodsFor: 'accessing' stamp: 'rkrk 4/25/2009 18:52'!
theName: string
	^ theName := string! !

!SomClassGenerationContext methodsFor: 'accessing' stamp: 'rkrk 4/29/2009 00:32'!
toggleClassSide
	self isClassSide: self isClassSide not! !

Object subclass: #SomCompiler
	instanceVariableNames: ''
	classVariableNames: ''
	poolDictionaries: ''
	category: 'SomLoader-Compiler'!

!SomCompiler methodsFor: 'compiler' stamp: 'StefanMarr 7/21/2016 10:47'!
load: aFileName
	| sourceStream parser |
	sourceStream := self sourceOfClassFile: aFileName.
	parser := SomClassParser on: sourceStream.
	^ parser start! !

!SomCompiler methodsFor: 'file access' stamp: 'StefanMarr 1/28/2024 01:08'!
sourceOfClassFile: aFileName
	(Smalltalk hasClassNamed: #Stdio)
		ifTrue: [^ aFileName asFileReference readStream]
		ifFalse: [
			| source |
			source := nil.
			(Smalltalk classNamed: #FileStream)
				fileNamed: aFileName
				do: [:s | source := s contents].
			^ source readStream ]! !

Object subclass: #SomLexer
	instanceVariableNames: 'stream text nextText lineNumber peekDone sym nextSym symChr nextSymChr buf bufP prevStreamPosition lastNonWhiteChar prevLastNonWhiteChar'
	classVariableNames: ''
	poolDictionaries: ''
	category: 'SomLoader-Compiler'!

!SomLexer class methodsFor: 'instance creation' stamp: 'StefanMarr 5/10/2012 21:22'!
on: aReadStream
	| lexer |
	lexer := self new.
	lexer stream: aReadStream.
	^ lexer! !

!SomLexer methodsFor: 'private' stamp: 'StefanMarr 5/12/2012 22:50'!
bufchar: p
	^ p > buf size
		ifTrue:  [nil]
		ifFalse: [buf at: p]! !

!SomLexer methodsFor: 'private' stamp: 'StefanMarr 5/10/2012 23:50'!
currentChar
	^ self bufchar: bufP! !

!SomLexer methodsFor: 'accessing' stamp: 'StefanMarr 5/9/2012 23:32'!
currentLineNumber
	^ lineNumber! !

!SomLexer methodsFor: 'private' stamp: 'StefanMarr 5/12/2012 22:47'!
endOfBuffer
	^ bufP > (buf size)! !

!SomLexer methodsFor: 'accessing' stamp: 'StefanMarr 5/13/2012 17:14'!
fillBuffer
	stream atEnd ifTrue: [^ -1].

	buf := stream nextLine.
	buf ifNil: [^ -1].

	lineNumber := lineNumber + 1.

	bufP := 1.
	^ buf size.! !

!SomLexer methodsFor: 'private' stamp: 'StefanMarr 5/13/2012 17:11'!
hasMoreInput
	self readBufferIfEmpty: [^ false].

	^ true! !

!SomLexer methodsFor: 'initialize-release' stamp: 'StefanMarr 5/13/2012 10:04'!
initialize
	lineNumber := 0.
	peekDone := false.
	text := SomParser textStreamCls on: String new.
	buf := ''.
	bufP := 1.! !

!SomLexer methodsFor: 'helper' stamp: 'StefanMarr 5/10/2012 20:23'!
isOperator: char
	^ char == $~ or: [
	  char == $& or: [
	  char == $| or: [
	  char == $* or: [
	  char == $/ or: [
	  char == $\ or: [
	  char == $+ or: [
	  char == $= or: [
	  char == $> or: [
	  char == $< or: [
	  char == $, or: [
	  char == $@ or: [
	  char == $%
	]]]]]]]]]]]].! !

!SomLexer methodsFor: 'accessing' stamp: 'StefanMarr 7/22/2016 23:45'!
lastNonWhiteCharIdx
	^ lastNonWhiteChar! !

!SomLexer methodsFor: 'lexing' stamp: 'StefanMarr 5/13/2012 10:04'!
match: aSymbol
	sym := aSymbol.
	symChr := self currentChar.
	text := SomParser textStreamCls with: symChr asString.
	self readNext.
	^ sym.! !

!SomLexer methodsFor: 'lexing' stamp: 'StefanMarr 5/13/2012 12:50'!
matchColonOrAssignment
	(self bufchar: bufP + 1) == $= ifTrue: [
		sym := #assign.
		bufP := bufP + 2.
		symChr := nil.
		text := SomParser textStreamCls with: ':='.
		^ sym
	].

	^ self match: #colon.! !

!SomLexer methodsFor: 'lexing' stamp: 'StefanMarr 5/13/2012 18:33'!
matchLetter
	symChr := nil.
	text := SomParser textStreamCls on: String new.

	[self currentChar notNil and: [
		 self currentChar isLetter or: [
		 self currentChar isDigit  or: [
		 self currentChar == $_]]]]
		whileTrue: [
			text nextPut: self currentChar.
			self readNext.
		].

	sym := #identifier.

	self currentChar == $: ifTrue: [
		sym := #keyword.
		self readNext.
		text nextPut: $:.

		(self currentChar notNil and: [self currentChar isLetter]) ifTrue: [
			sym := #keywordSequence.
			[self currentChar isLetter or: [self currentChar == $:]]
				whileTrue: [
					text nextPut: self currentChar.
					self readNext.
				]
		]
	].

	^ sym! !

!SomLexer methodsFor: 'lexing' stamp: 'StefanMarr 5/13/2012 17:51'!
matchMinusOrSeparator
	(self nextInBufIs: self seperator) ifTrue: [
		text := SomParser textStreamCls on: String new.
		[self currentChar == $-] whileTrue: [
			text nextPut: $-.
			self readNext.
		].
		sym := #seperator.
		^ sym.
	].

	^ self match: #minus.
	! !

!SomLexer methodsFor: 'lexing' stamp: 'StefanMarr 7/21/2016 16:08'!
matchNumber
	| sawDecimalMark |
	sym    := #integer.
	symChr := nil.
	text   := SomParser textStreamCls on: String new.

	sawDecimalMark := false.

	[ text nextPut: self currentChar.
	  self readNext.
	  (sawDecimalMark not and: [
			self currentChar = $. and: [
				| nextChar |
				nextChar := self bufchar: bufP + 1.
				nextChar notNil and: [ nextChar isDigit]]]) ifTrue: [
					sym := #double.
					sawDecimalMark := true.
					text nextPut: self currentChar.
					self readNext ]
	] doWhileTrue: [self currentChar notNil and: [ self currentChar isDigit ]].

	^ sym! !

!SomLexer methodsFor: 'lexing' stamp: 'StefanMarr 5/11/2012 18:03'!
matchOperator
	| cur |
	(self isOperator: (self bufchar: bufP + 1)) ifTrue: [
		sym := #operatorSequence.
		symChr := nil.
		text := SomParser textStreamCls on: String new.

		[self isOperator: self currentChar] whileTrue: [
			text nextPut: self currentChar.
			self readNext.
		].
		^ sym.
	].

	cur := self currentChar.
	cur == $~ ifTrue: [ ^ self match: #not ].
	cur == $& ifTrue: [ ^ self match: #and ].
	cur == $| ifTrue: [ ^ self match: #or  ].
	cur == $* ifTrue: [ ^ self match: #star].
	cur == $/ ifTrue: [ ^ self match: #div ].
	cur == $\ ifTrue: [ ^ self match: #mod ].
	cur == $+ ifTrue: [ ^ self match: #plus].
	cur == $= ifTrue: [ ^ self match: #equal].
	cur == $> ifTrue: [ ^ self match: #more].
	cur == $< ifTrue: [ ^ self match: #less].
	cur == $, ifTrue: [ ^ self match: #comma].
	cur == $@ ifTrue: [ ^ self match: #at  ].
	cur == $% ifTrue: [ ^ self match: #per ].

	Error signal: 'STEFAN: this is unexpected, I think...'.! !

!SomLexer methodsFor: 'lexing' stamp: 'StefanMarr 7/22/2016 23:42'!
matchPrimitive
	self readNext: self primitive size.
	sym := #primitive.
	symChr := nil.
	text := SomParser textStreamCls with: self primitive.
	^ sym
	! !

!SomLexer methodsFor: 'lexing' stamp: 'StefanMarr 5/13/2012 10:06'!
matchString
	sym := #STString.
	symChr := nil.
	text := SomParser textStreamCls on: String new.

	self readNext. "skip start $' "
	[ self currentChar == $' ]
		whileFalse: [
			text nextPut: self currentChar.
			self readNext.
		].

	self readNext. "skip the ending $'"

	^ sym! !

!SomLexer methodsFor: 'helper' stamp: 'StefanMarr 5/13/2012 17:48'!
nextInBufIs: aString
	| p |
	p := buf findString: aString startingAt: bufP.
	^ p == bufP! !

!SomLexer methodsFor: 'accessing' stamp: 'StefanMarr 5/10/2012 18:08'!
nextText
	^ nextText asString! !

!SomLexer methodsFor: 'helper' stamp: 'StefanMarr 5/19/2012 20:14'!
nextWordInBufIs: aString
   (self nextInBufIs: aString) ifTrue: [
     | nextPos nextChar |
      nextPos := bufP + aString size.

      nextPos > buf size ifTrue: [^true].

      nextChar := buf at: nextPos.
      ^ (nextChar isLetter or: [nextChar isDigit]) not.
	].

	^ false.! !

!SomLexer methodsFor: 'lexing' stamp: 'StefanMarr 7/23/2016 18:35'!
peek
	| oldSym oldSymChr oldText oldPrevStream |
	peekDone
		ifTrue: [ Error signal: 'SOM Lexer: cannot peek twice!!' ].

	oldSym    := sym.
	oldSymChr := symChr.
	oldText   := SomParser textStreamCls with: text contents.
	oldPrevStream := prevStreamPosition.

	nextSym   := self symbol.
	nextSymChr:= symChr.
	nextText  := text.

	sym   := oldSym.
	symChr:= oldSymChr.
	text  := oldText.

	peekDone := true.

	prevStreamPosition := oldPrevStream.
	^ nextSym.! !

!SomLexer methodsFor: 'accessing' stamp: 'StefanMarr 7/22/2016 14:20'!
peekDone
	^ peekDone! !

!SomLexer methodsFor: 'accessing' stamp: 'StefanMarr 7/22/2016 23:52'!
prevLastNonWhiteCharIdx
	^ prevLastNonWhiteChar! !

!SomLexer methodsFor: 'constant' stamp: 'StefanMarr 5/10/2012 16:10'!
primitive
	^ 'primitive'! !

!SomLexer methodsFor: 'accessing' stamp: 'StefanMarr 5/10/2012 23:55'!
rawBuffer
	^ buf! !

!SomLexer methodsFor: 'helper' stamp: 'StefanMarr 5/13/2012 17:21'!
readBufferIfEmpty: aBlock
	[self endOfBuffer] whileTrue: [
		self fillBuffer == -1
			ifTrue: aBlock]! !

!SomLexer methodsFor: 'private' stamp: 'StefanMarr 7/22/2016 23:44'!
readNext
	bufP := bufP + 1.
	lastNonWhiteChar := stream position - buf size + bufP.
	! !

!SomLexer methodsFor: 'helper' stamp: 'StefanMarr 7/22/2016 23:45'!
readNext: val
	bufP := bufP + val.
	lastNonWhiteChar := stream position - buf size + bufP.! !

!SomLexer methodsFor: 'constant' stamp: 'StefanMarr 5/10/2012 16:10'!
seperator
	^ '----'! !

!SomLexer methodsFor: 'helper' stamp: 'StefanMarr 5/13/2012 17:12'!
skipComment
	self currentChar == $" ifTrue: [
		[
			self readNext.
			self readBufferIfEmpty: [^ self].
		] doWhileTrue: [self currentChar ~= $"].
		self readNext.
	].
! !

!SomLexer methodsFor: 'helper' stamp: 'StefanMarr 5/13/2012 17:12'!
skipWhitespace
	[self currentChar isSeparator]
		whileTrue: [
			self readNext.
			self readBufferIfEmpty: [^ self]]! !

!SomLexer methodsFor: 'helper' stamp: 'StefanMarr 5/10/2012 16:46'!
skipWhitespaceAndCommentsOrEnd: atEndBlock
	[	self hasMoreInput ifFalse: atEndBlock.
		self skipWhitespace.
		self skipComment.
	] doWhileTrue: [
		self endOfBuffer 				or: [
		self currentChar isSeparator 	or: [
		self currentChar == $" ]]].! !

!SomLexer methodsFor: 'accessing' stamp: 'StefanMarr 7/21/2016 14:13'!
stream
	^ stream! !

!SomLexer methodsFor: 'accessing' stamp: 'StefanMarr 5/9/2012 23:29'!
stream: aReadStream
	stream := aReadStream.! !

!SomLexer methodsFor: 'lexing' stamp: 'StefanMarr 7/22/2016 23:53'!
symbol
	| currentChar |
	peekDone ifTrue: [^ self symbolAfterPeek].
	prevLastNonWhiteChar := lastNonWhiteChar.

	self skipWhitespaceAndCommentsOrEnd: [
		sym := #none.
		symChr := nil.
		text := SomParser textStreamCls on: String new.
		^ sym
	].

	currentChar := self currentChar.

	currentChar == $' ifTrue: [ ^ self matchString ].
	currentChar == $[ ifTrue: [ ^ self match: #newBlock ].
	currentChar == $] ifTrue: [ ^ self match: #endBlock ].
	currentChar == $: ifTrue: [ ^ self matchColonOrAssignment ].
	currentChar == $( ifTrue: [ ^ self match: #newTerm ].
	currentChar == $) ifTrue: [ ^ self match: #endTerm ].
	currentChar == $# ifTrue: [ ^ self match: #pound   ].
	currentChar == $^ ifTrue: [ ^ self match: #exit    ].
	currentChar == $. ifTrue: [ ^ self match: #period  ].
	currentChar == $- ifTrue: [ ^ self matchMinusOrSeparator ].
	(self isOperator: currentChar) ifTrue: [ ^ self matchOperator ].

	(self nextWordInBufIs: self primitive) ifTrue: [ ^ self matchPrimitive ].

	currentChar isLetter ifTrue: [ ^ self matchLetter ].
	currentChar isDigit  ifTrue: [ ^ self matchNumber  ].

	"else"
	sym    := #none.
	symChr := currentChar.
	text   := SomParser textStreamCls with: currentChar asString.

	^ sym! !

!SomLexer methodsFor: 'lexing' stamp: 'StefanMarr 5/12/2012 23:18'!
symbolAfterPeek
	peekDone := false.
	sym      := nextSym.
	symChr   := nextSymChr.
	text     := nextText.
	^ sym! !

!SomLexer methodsFor: 'accessing' stamp: 'StefanMarr 5/13/2012 10:01'!
text
	^ text contents! !

Object subclass: #SomLoader
	instanceVariableNames: 'classDefinitions'
	classVariableNames: ''
	poolDictionaries: ''
	category: 'SomLoader-Compiler'!

!SomLoader commentStamp: 'StefanMarr 7/22/2016 11:03' prior: 0!
I load SOM Smalltalk into Squeak/Pharo images.!

!SomLoader class methodsFor: 'actions' stamp: 'StefanMarr 7/22/2016 12:45'!
load: aDirectorName
	^ self new load: aDirectorName; yourself! !

!SomLoader class methodsFor: 'actions' stamp: 'StefanMarr 7/22/2016 12:45'!
loadFile: aFileName
	^ self new loadFile: aFileName; yourself! !

!SomLoader methodsFor: 'actions' stamp: 'StefanMarr 7/22/2016 11:07'!
checkClassesNotInSystem
	| inSystem |
	inSystem := classDefinitions collect: [:c | Smalltalk hasClassNamed: c theName ].
	^ inSystem anySatisfy: [:b | b ]! !

!SomLoader methodsFor: 'actions' stamp: 'StefanMarr 7/22/2016 12:56'!
createClasses
	| postPoned |
	postPoned := classDefinitions.
	[ postPoned isEmpty ] whileFalse: [
		postPoned := self createClasses: postPoned ]! !

!SomLoader methodsFor: 'actions' stamp: 'StefanMarr 7/22/2016 12:56'!
classDefinitions
	^ classDefinitions! !

!SomLoader methodsFor: 'actions' stamp: 'StefanMarr 7/22/2016 12:57'!
createClasses: classes
	| postPoned |
	postPoned := OrderedCollection new.
	classes do: [:c |
		(Smalltalk hasClassNamed: c superName)
			ifTrue: [ self createClass: c ]
			ifFalse: [ postPoned add: c ]].
	^ postPoned! !

!SomLoader methodsFor: 'actions' stamp: 'StefanMarr 2/02/2024 12:15'!
createClass: cls
	| instVars classVars superCls |
	superCls := Smalltalk classNamed: cls superName.
	instVars := self varString: cls instanceFields.
	classVars := self varString: cls classFields.

	(superCls respondsTo: #classInstaller)
	   ifTrue: [
			superCls classInstaller make: [ :builder |
			     builder
					superclass: superCls;
					name: cls theName asSymbol;
					slotsFromString: instVars;
					sharedVariablesFromString: classVars;
					package: 'AWFY-Benchmarks';
					environment: superCls environment ]]
	   ifFalse: [
			superCls
				subclass: cls theName asSymbol
				instanceVariableNames: instVars
				classVariableNames: classVars
				package: 'AWFY-Benchmarks' ]
! !

!SomLoader methodsFor: 'actions' stamp: 'StefanMarr 7/22/2016 13:04'!
createMethods
	classDefinitions do: [:c |
		| class |
		class := Smalltalk classNamed: c theName.
		c instanceMethods do: [:methodCode |
			class compile: methodCode ].
		c classMethods do: [:methodCode |
			class class compile: methodCode ] ]! !

!SomLoader methodsFor: 'actions' stamp: 'StefanMarr 7/22/2016 12:43'!
load: aDirectorName
	| dir results |
	results := OrderedCollection new.
	dir := aDirectorName asFileReference.
	dir entries do: [:e |
		e isDirectory
			ifTrue: [
				| result |
				result := self load: e fullName.
				results addAll: result ]
			ifFalse: [
				e extension = 'som' ifTrue: [
					results add: (SomCompiler new load: e fullName) ]]].
	classDefinitions := results.
	^ results! !

!SomLoader methodsFor: 'actions' stamp: 'StefanMarr 7/22/2016 12:43'!
loadFile: aFileName
	| file results |
	results := OrderedCollection new.
	file := aFileName asFileReference.
	results add: (SomCompiler new load: file).
	classDefinitions := results.
	^ results! !

!SomLoader methodsFor: 'helper methods' stamp: 'StefanMarr 7/22/2016 12:47'!
varString: collectionOfSymbols
	^ collectionOfSymbols inject: '' into: [:res :sym | res, ' ', sym asString ]! !

!SomClassParser class methodsFor: 'instance creation' stamp: 'StefanMarr 7/21/2016 11:05'!
on: readStream
	| parser |
	parser := self new.
	parser stream: readStream.
	^ parser! !

!SomClassParser methodsFor: 'patterns' stamp: 'StefanMarr 5/13/2012 17:54'!
classBody
	self expect: #newTerm.

	self classSide.

	self classInstanceSwitch ifTrue: [
		cgenc toggleClassSide.
		self classSide.].

	self expect: #endTerm.! !

!SomClassParser methodsFor: 'patterns' stamp: 'StefanMarr 5/13/2012 12:13'!
classInstanceSwitch
	^ self accept: #seperator.! !

!SomClassParser methodsFor: 'patterns' stamp: 'StefanMarr 7/21/2016 13:30'!
classSide
	self vars.

	[self symIsIdentifier     or: [
	 sym == #keyword          or: [
	 sym == #operatorSequence or: [
	 self symIn: BinaryOpSyms]]]]
		whileTrue: [
			self parseMethod].! !

!SomClassParser methodsFor: 'patterns' stamp: 'StefanMarr 5/17/2012 15:23'!
classStart
	cgenc theName: self identifier.

	self expect: #equal.

	sym == #identifier
		ifTrue:  [cgenc superName: self identifier.].
		! !

!SomClassParser methodsFor: 'patterns' stamp: 'StefanMarr 5/13/2012 18:38'!
classdef
	self classStart.
	self classBody.! !

!SomClassParser methodsFor: 'initialize-release' stamp: 'StefanMarr 7/21/2016 13:07'!
initialize
	super initialize.

	self cgenc: SomClassGenerationContext new.! !

!SomClassParser methodsFor: 'patterns' stamp: 'StefanMarr 7/21/2016 14:49'!
parseMethod
	| parser result newParserState |
	parser := SomMethodParser with: lexer sym: sym text: text.
	result := parser method.
	newParserState := parser parserState.
	sym  := newParserState first.
	text := newParserState second.

	cgenc addMethod: result.
	^ result! !

!SomClassParser methodsFor: 'parsing' stamp: 'StefanMarr 5/13/2012 18:39'!
start
	self classdef.
	^ cgenc! !

!SomClassParser methodsFor: 'accessing' stamp: 'StefanMarr 7/21/2016 11:07'!
stream: aReadStream
	lexer := SomLexer on: aReadStream.
	self getSym.! !

!SomClassParser methodsFor: 'patterns' stamp: 'StefanMarr 5/17/2012 20:27'!
vars
	(self accept: #or) ifTrue: [
		[self symIsIdentifier] whileTrue: [
			cgenc addField: self variable.
		].

		self expect: #or.
	]! !

!SomMethodParser class methodsFor: 'instance creation' stamp: 'StefanMarr 7/21/2016 13:35'!
with: lexer sym: sym text: text
	| parser |
	parser := self new.
	parser lexer: lexer sym: sym text: text.
	^ parser! !

!SomMethodParser methodsFor: 'accept and expect' stamp: 'StefanMarr 5/10/2012 22:21'!
acceptOneOf: symbols
	(self symIn: symbols) ifTrue: [
		self getSym.
		^ true ].

	^ false! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 5/13/2012 11:27'!
argument
	^ self variable! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 7/21/2016 13:40'!
assignation
	| list |
	list := OrderedCollection new.
	self assignments: list.
	self evaluation.
! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 7/21/2016 13:39'!
assignment
	| v |
	v := self variable.
	self expect: #assign.
	^ v! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 7/21/2016 13:18'!
assignments: vars
	self symIsIdentifier ifTrue: [
		| var nextSym |
		var := self assignment.

		nextSym := self peek.
		nextSym == #assign ifTrue: [
			self assignments: vars.
		].

		vars add: var ]! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 7/21/2016 13:42'!
binaryMessage: superSend
	self binarySelector.
	self binaryOperand: false.! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 5/17/2012 20:24'!
binaryOperand: superSend
	| doSuper |
	doSuper := self primary: superSend.

	[self symIsIdentifier] whileTrue:
		[self unaryMessage: doSuper].

	^ doSuper! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 7/21/2016 11:31'!
binaryPattern
   | sig |
	sig := self binarySelector asString.
	^ sig, ' ', self argument asString.! !

!SomMethodParser methodsFor: 'selector patterns' stamp: 'StefanMarr 5/15/2012 22:28'!
binarySelector
	| s |
	s := text asSymbol.

	(self accept: #or)		ifFalse: [
	(self accept: #comma)	ifFalse: [
	(self accept: #minus)	ifFalse: [
	(self accept: #equal)	ifFalse: [
	(self acceptOneOf: SingleOpSyms) ifFalse: [
	(self accept: #operatorSequence) ifFalse: [
	 self expect: #none    ]]]]]].

	^ s! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 5/13/2012 23:28'!
blockArgPattern
	| blockSig |
	sym == #colon ifTrue: [self blockPattern ].! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 7/21/2016 16:37'!
blockArguments
	[ self expect: #colon.
	  self argument
	] doWhileTrue: [sym == #colon].! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 7/21/2016 14:50'!
blockBody
	(self accept: #exit) ifTrue: [
		^ self result.
	].

	sym == #endBlock ifTrue: [
		^ self
	].

	sym == #endTerm ifTrue: [
		^ self
	].

	self expression.

	(self accept: #period) ifTrue: [
		self blockBody.
	].
! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 7/21/2016 13:39'!
blockContents
	(self accept: #or) ifTrue: [
		self locals.
		self expect: #or.
	].

	self blockBody.! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 5/13/2012 11:46'!
blockPattern
	self blockArguments.
	self expect: #or.! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 5/17/2012 20:25'!
evaluation
	| superSend |
	superSend := self primary: false.

	(self symIsIdentifier     or: [
	 sym == #keyword          or: [
	 sym == #operatorSequence or: [
	 self symIn: BinaryOpSyms]]]) ifTrue: [
		self messages: superSend.
	]! !

!SomMethodParser methodsFor: 'accept and expect' stamp: 'StefanMarr 5/13/2012 10:54'!
expectOneOf: syms
	| msg |
	(self acceptOneOf: syms)
		ifTrue: [	^ true. ].

	msg := SomParser textStreamCls on: String new.
	msg nextPutAll: 'unexpected symbol in line '.
	msg nextPutAll: lexer currentLineNumber asString.
	msg nextPutAll: '. Expected one of '.

	syms do: [:s |
		msg nextPutAll: s.
		msg nextPutAll: ', '].

	msg nextPutAll: 'but found '.
	msg nextPutAll: sym asString.

	self printableSymbol ifTrue: [
		msg nextPutAll: ' (', text contents, ')'].

	msg nextPutAll: ': '.
	msg nextPutAll: lexer rawBuffer.

	Error signal: msg.! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 7/21/2016 13:17'!
expression
	| nextSym |
	nextSym := self peek.
	nextSym == #assign
		ifTrue:  [self assignation.]
		ifFalse: [self evaluation. ]! !

!SomMethodParser methodsFor: 'helper' stamp: 'StefanMarr 7/21/2016 13:07'!
fakeCgenc

	cgenc := SomClassGenerationContext new
			theName: #Dummy;
			yourself.! !

!SomMethodParser methodsFor: 'helper' stamp: 'StefanMarr 5/17/2012 16:28'!
fakeMgenc

	mgenc := (universe methodGenClass named: #dummy)
		somHolder: cgenc;
		yourself! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 5/13/2012 10:28'!
formula
	| superSend |
	superSend := self binaryOperand: false.

	(sym == #operatorSequence or: [self symIn: BinaryOpSyms])
		ifTrue: [self binaryMessage: superSend ].

	"only the first message in a sequence can be a super send"
	[sym == #operatorSequence or: [self symIn: BinaryOpSyms]]
		whileTrue: [self binaryMessage: false ].! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 5/13/2012 16:34'!
keyword
	| s |
	s := text.
	self expect: #keyword.
	^ s! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 7/21/2016 13:45'!
keywordMessage: superSend
	| kw sel |
	kw := SomParser textStreamCls on: String new.

	[ kw nextPutAll: self keyword.
	  self formula.
	] doWhileTrue: [sym == #keyword].

	sel := kw contents asSymbol! !

!SomMethodParser methodsFor: 'selector patterns' stamp: 'StefanMarr 7/22/2016 13:08'!
keywordPattern
	| methodDef first |
	first := true.
	methodDef := SomParser textStreamCls on: String new.
	[ first ifTrue: [ first := false ] ifFalse: [ methodDef nextPutAll: ' ' ].
	  methodDef nextPutAll: self keyword.
	  methodDef nextPutAll: ' '.
	  methodDef nextPutAll: self argument.
	] doWhileTrue: [sym == #keyword].

	^ methodDef contents asString! !

!SomMethodParser methodsFor: 'selector patterns' stamp: 'StefanMarr 5/15/2012 22:29'!
keywordSelector
	| s |
	s := text asSymbol.

	self expectOneOf: KeywordSelectorSyms.

	^ s! !

!SomMethodParser methodsFor: 'accessing' stamp: 'StefanMarr 7/21/2016 13:33'!
lexer: aLexer
	lexer := aLexer! !

!SomMethodParser methodsFor: 'accessing' stamp: 'StefanMarr 7/21/2016 13:35'!
lexer: aLexer sym: aSym text: aText
	lexer := aLexer.
	sym   := aSym.
	text  := aText.! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 5/13/2012 09:47'!
literal
	sym == #pound    ifTrue: [ ^ self literalSymbol. ].
	sym == #STString ifTrue: [ ^ self literalString. ].

	self literalNumber.! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 7/21/2016 16:11'!
literalDecimal
	sym == #integer ifTrue: [ ^ self literalInteger ].
	[ sym == #double ] assert.
	^ self literalDouble.! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 7/21/2016 16:14'!
literalDouble
	| val |
	val := text contents asNumber.
	self expect: #double.
	^ val! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 5/12/2012 23:12'!
literalInteger
	| val |
	val := text contents asInteger.
	self expect: #integer.
	^ val! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 7/21/2016 13:40'!
literalNumber
	sym == #minus	ifTrue: [self negativeDecimal]
						ifFalse:[self literalDecimal ].
! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 7/21/2016 15:02'!
literalString
	^ self string! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 7/21/2016 16:17'!
literalSymbol
	| symbol |
	self expect: #pound.
	symbol := sym == #STString
		ifTrue:  [self string]
		ifFalse: [self selector].
! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 7/21/2016 13:38'!
locals
	[self symIsIdentifier] whileTrue: [
		self variable ]! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 5/17/2012 20:26'!
messages: superSend
	| doSuper |
	doSuper := superSend.

	self symIsIdentifier ifTrue: [
		[ self unaryMessage: doSuper.
		  doSuper := false.
		] doWhileTrue: [self symIsIdentifier].

		[sym == #operatorSequence or: [self symIn: BinaryOpSyms]] whileTrue: [
			self binaryMessage: false.
		].

		sym == #keyword ifTrue: [
			self keywordMessage: false.
		].
		^ doSuper.
	].

	(sym == #operatorSequence or: [self symIn: BinaryOpSyms]) ifTrue: [
		[ self binaryMessage: doSuper.
		  doSuper := false.
		] doWhileTrue: [sym == #operatorSequence or: [self symIn: BinaryOpSyms]].

		sym == #keyword ifTrue: [
			self keywordMessage: false.
		].
		^ doSuper.
	].

	self keywordMessage: doSuper.
! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 7/21/2016 14:52'!
method
   | name body |
	name := self methodName.
	self expect: #equal.

	body := self methodBody.
	^ name, Character lf asString, body
! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 7/22/2016 23:54'!
methodBlock
	| methodSource startIdx endIdx len stream trueEndIdx |
	startIdx := lexer prevLastNonWhiteCharIdx.

	self expect: #newTerm.
	self blockContents.

	endIdx := lexer prevLastNonWhiteCharIdx - 1.
	stream := lexer stream.
	trueEndIdx := stream position.

	len := endIdx - startIdx.
	methodSource := String new: len.
	stream position: startIdx.
	stream readInto: methodSource startingAt: 1 count: len.
	stream position: trueEndIdx.
	self expect: #endTerm.

	^ methodSource! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 7/21/2016 11:37'!
methodBody
	sym == #primitive
		ifTrue:  [ ^ self primitiveBlock ]
		ifFalse: [ ^ self methodBlock ].
! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 7/21/2016 11:33'!
methodName
	self symIsIdentifier ifTrue: [ ^ self unaryPattern   ].
	sym == #keyword      ifTrue: [ ^ self keywordPattern ].

	^ self binaryPattern.! !

!SomMethodParser methodsFor: 'accessing' stamp: 'StefanMarr 5/13/2012 15:31'!
mgenc
	^ mgenc! !

!SomMethodParser methodsFor: 'accessing' stamp: 'StefanMarr 5/13/2012 15:31'!
mgenc: anMgenc
	mgenc := anMgenc! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 7/21/2016 16:10'!
negativeDecimal
	self expect: #minus.
	^ self literalDecimal negated ! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 7/21/2016 13:43'!
nestedBlock
	self expect: #newBlock.
	self blockArgPattern.
	self blockContents.
	self expect: #endBlock.
! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 5/13/2012 10:30'!
nestedTerm
	self expect: #newTerm.

	self expression.

	self expect: #endTerm.! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 7/21/2016 13:13'!
parseNestedBlock
	^ self nestedBlock.
! !

!SomMethodParser methodsFor: 'accessing' stamp: 'StefanMarr 7/21/2016 14:48'!
parserState
	^ { sym. text }! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 7/21/2016 13:41'!
primary: doSuper
	| superSend |
	superSend := doSuper.

	self symIsIdentifier ifTrue: [
		| v |
		v := self variable.
		v = #super ifTrue: [
			superSend := true.
		].
		^ superSend.
	].

	sym == #newTerm ifTrue: [
		self nestedTerm.
		^ superSend.
	].

	sym == #newBlock ifTrue: [
		self parseNestedBlock.
		^ superSend
	].

	self literal.
	^ superSend! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 7/21/2016 11:37'!
primitiveBlock
	self expect: #primitive.
	^ '^ Error signal: ''Primitives are not supported.'' '! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 7/21/2016 13:45'!
result
	self expression.
	self accept: #period.! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 5/13/2012 10:36'!
selector
	(sym == #operatorSequence or: [self symIn: SingleOpSyms])
		ifTrue: [^ self binarySelector ].

	(sym == #keyword or: [sym == #keywordSequence])
		ifTrue: [^ self keywordSelector].

	^ self unarySelector! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 5/13/2012 11:53'!
string
	| s |
	s := text contents.
	self expect: #STString.
	^ s! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 7/21/2016 13:43'!
unaryMessage: superSend
	self unarySelector.
! !

!SomMethodParser methodsFor: 'patterns' stamp: 'StefanMarr 7/21/2016 11:30'!
unaryPattern
	^ self unarySelector asString! !

!SomMethodParser methodsFor: 'selector patterns' stamp: 'StefanMarr 5/15/2012 22:29'!
unarySelector
	^ self identifier asSymbol! !

!SomParser class methodsFor: 'class initialization' stamp: 'StefanMarr 5/10/2012 22:35'!
initialize
	"self initialize"
	SingleOpSyms := #(                     not and or star div mod plus equal more less comma at per none).
	BinaryOpSyms := #(or comma minus equal not and or star div mod plus equal more less comma at per none).
	KeywordSelectorSyms := #(keyword keywordSequence).
	TextStreamCls := (Smalltalk classNamed: #TextStream)
		ifNotNil: [:cls | cls]
		ifNil: [Smalltalk classNamed: #RWBinaryOrTextStream].
	! !

SomParser initialize!

!SomParser methodsFor: 'accept and expect' stamp: 'StefanMarr 5/13/2012 12:16'!
accept: aSymbol
	sym == aSymbol ifTrue: [
		self getSym.
		^ true ].

	^ false! !

!SomParser methodsFor: 'accessing' stamp: 'StefanMarr 5/9/2012 23:15'!
cgenc
	^ cgenc! !

!SomParser methodsFor: 'accessing' stamp: 'StefanMarr 5/9/2012 23:16'!
cgenc: anObject
	cgenc := anObject! !

!SomParser methodsFor: 'accept and expect' stamp: 'StefanMarr 5/13/2012 12:15'!
expect: aSymbol
	| msg |
	(self accept: aSymbol) ifTrue: [ ^ true ].

	msg := SomParser textStreamCls on: String new.
	msg nextPutAll: 'unexpected symbol in line '.
	msg nextPutAll: lexer currentLineNumber asString.
	msg nextPutAll: '. Expected '.
	msg nextPutAll: aSymbol asString.
	msg nextPutAll: ', but found '.
	msg nextPutAll: sym asString.

	self printableSymbol ifTrue: [
		msg nextPutAll: ' (', text contents, ')'.].

	msg nextPutAll: ': '.
	msg nextPutAll: lexer rawBuffer.

	Error signal: msg contents.! !

!SomParser methodsFor: 'private' stamp: 'StefanMarr 5/10/2012 23:03'!
getSym
	sym  := lexer symbol.
	text := lexer text.! !

!SomParser methodsFor: 'patterns' stamp: 'StefanMarr 5/15/2012 22:28'!
identifier
	| s |
	s := text asSymbol.

	(self accept: #primitive) ifFalse: [
		self expect: #identifier].

	^ s! !

!SomParser methodsFor: 'initialize-release' stamp: 'StefanMarr 7/21/2016 13:16'!
initialize
	super initialize.
	sym := #none.
	shouldReadSources := false.! !

!SomParser methodsFor: 'private' stamp: 'StefanMarr 7/21/2016 13:15'!
peek
	^ lexer peek.! !

!SomParser methodsFor: 'private' stamp: 'StefanMarr 5/17/2012 20:27'!
printableSymbol
	^ sym == #integer         or: [
	  sym == #STString        or: [
	  self symIsIdentifier    or: [
	  sym == #keyword         or: [
	  sym == #keywordSequence or: [
	  sym == #operatorSequence    ]]]]]! !

!SomParser methodsFor: 'accessing' stamp: 'StefanMarr 5/13/2012 14:31'!
shouldReadSources
	^ shouldReadSources! !

!SomParser methodsFor: 'accessing' stamp: 'StefanMarr 5/13/2012 14:31'!
shouldReadSources: aBool
	shouldReadSources := aBool! !

!SomParser methodsFor: 'helper' stamp: 'StefanMarr 5/13/2012 12:27'!
symIn: symbols
	^ symbols includes: sym ! !

!SomParser methodsFor: 'helper' stamp: 'StefanMarr 5/17/2012 20:23'!
symIsIdentifier
	^ sym == #identifier or: [sym == #primitive]! !

!SomParser methodsFor: 'patterns' stamp: 'StefanMarr 5/13/2012 12:27'!
variable
	^ self identifier! !

!SomParser class methodsFor: 'patterns' stamp: 'StefanMarr 2/02/2024 20:16'!
textStreamCls
	^ TextStreamCls! !

SomParser initialize!
