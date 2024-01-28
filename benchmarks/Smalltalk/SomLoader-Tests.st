SystemOrganization addCategory: #'SomLoader-Tests'!

TestCase subclass: #SomLoaderTests
	instanceVariableNames: ''
	classVariableNames: ''
	poolDictionaries: ''
	category: 'SomLoader-Tests'!

!SomLoaderTests methodsFor: 'test all files' stamp: 'StefanMarr 7/22/2016 11:01'!
testLoadAll
	| classes loader |
	loader := SomLoader load: '.'.
	classes := loader classDefinitions.

	"Test that none of the loaded classes is in the system"
	classes do: [:c |
	   self deny: (Smalltalk hasClassNamed: c theName) description: 'Class already present with name:', c theName.
	].! !

!SomLoaderTests methodsFor: 'test' stamp: 'StefanMarr 7/21/2016 15:00'!
testLoadBall
	| result |
	result := SomCompiler new load: 'Ball.som'.
	self deny: result isNil.
	self assert: result classFields size equals: 0.
	self assert: result classMethods size equals: 1.
	self assert: result instanceFields size equals: 4.
	self assert: result instanceMethods size equals: 2.! !

!SomLoaderTests methodsFor: 'test' stamp: 'StefanMarr 7/21/2016 16:18'!
testLoadCDRedBlackTree
	| result |
	result := SomCompiler new load: 'CD', DiskStore delimiter asString, 'RedBlackTree.som'.
	self deny: result isNil.
	self assert: result classFields size equals: 0.
	self assert: result classMethods size equals: 1.
	self assert: result instanceFields size equals: 1.
	self assert: result instanceMethods size equals: 9.! !

!SomLoaderTests methodsFor: 'test' stamp: 'StefanMarr 7/21/2016 16:20'!
testLoadJson
	| result |
	result := SomCompiler new load: 'Json', DiskStore delimiter asString, 'Json.som'.
	self deny: result isNil.
	self assert: result classFields size equals: 0.
	self assert: result classMethods size equals: 2.
	self assert: result instanceFields size equals: 0.
	self assert: result instanceMethods size equals: 2.! !

!SomLoaderTests methodsFor: 'test' stamp: 'StefanMarr 7/21/2016 16:15'!
testLoadMandelbrot
	| result |
	result := SomCompiler new load: 'Mandelbrot.som'.
	self deny: result isNil.
	self assert: result classFields size equals: 0.
	self assert: result classMethods size equals: 0.
	self assert: result instanceFields size equals: 0.
	self assert: result instanceMethods size equals: 3.! !
