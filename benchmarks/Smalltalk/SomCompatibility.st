!ArrayedCollection methodsFor: '*SomCompatibility' stamp: 'StefanMarr 7/23/2016 00:06'!
length
	"Answer how many elements the receiver contains."

	<primitive: 62>
	^ self basicSize! !

!String methodsFor: '*SomCompatibility' stamp: 'StefanMarr 7/23/2016 00:06'!
println
	ScriptConsole println: self! !
