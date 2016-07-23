SystemOrganization addCategory: #'SomLoader-Tests'!

TestCase subclass: #SomLoaderTests
	instanceVariableNames: ''
	classVariableNames: ''
	poolDictionaries: ''
	category: 'SomLoader-Tests'!

!SomLoaderTests methodsFor: 'test' stamp: 'StefanMarr 7/21/2016 15:00'!
testLoadBall
	| result |
	result := SomCompiler new load: '/Users/smarr/Projects/PostDoc/FASTXX/are-we-fast-yet/benchmarks/SOM/Ball.som'.
	self deny: result isNil.
	self assert: result classFields size equals: 0.
	self assert: result classMethods size equals: 1.
	self assert: result instanceFields size equals: 4.
	self assert: result instanceMethods size equals: 2.! !

!SomLoaderTests methodsFor: 'test' stamp: 'StefanMarr 7/21/2016 16:18'!
testLoadCDRedBlackTree
	| result |
	result := SomCompiler new load: '/Users/smarr/Projects/PostDoc/FASTXX/are-we-fast-yet/benchmarks/SOM/CD/RedBlackTree.som'.
	self deny: result isNil.
	self assert: result classFields size equals: 0.
	self assert: result classMethods size equals: 1.
	self assert: result instanceFields size equals: 1.
	self assert: result instanceMethods size equals: 9.! !

!SomLoaderTests methodsFor: 'test' stamp: 'StefanMarr 7/21/2016 16:20'!
testLoadJson
	| result |
	result := SomCompiler new load: '/Users/smarr/Projects/PostDoc/FASTXX/are-we-fast-yet/benchmarks/SOM/Json/Json.som'.
	self deny: result isNil.
	self assert: result classFields size equals: 0.
	self assert: result classMethods size equals: 2.
	self assert: result instanceFields size equals: 0.
	self assert: result instanceMethods size equals: 2.! !

!SomLoaderTests methodsFor: 'test' stamp: 'StefanMarr 7/21/2016 16:15'!
testLoadMandelbrot
	| result |
	result := SomCompiler new load: '/Users/smarr/Projects/PostDoc/FASTXX/are-we-fast-yet/benchmarks/SOM/Mandelbrot.som'.
	self deny: result isNil.
	self assert: result classFields size equals: 0.
	self assert: result classMethods size equals: 0.
	self assert: result instanceFields size equals: 0.
	self assert: result instanceMethods size equals: 3.! !
