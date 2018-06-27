// Reinhold P. Weicker,  CACM Vol 27, No 10, 10/84 pg. 1013.
//
// Translated from ADA to C by Rick Richardson.
// Every method to preserve ADA-likeness has been used,
// at the expense of C-ness.
//
// Translated from C to Python by Guido van Rossum.
//
//
// Adapted for Grace by Richard Roberts
//   2018, June
//

import "harness" as harness

var IDENT_1: Number := 1.asInteger
var IDENT_2: Number := 2.asInteger
var IDENT_3: Number := 3.asInteger
var IDENT_4: Number := 4.asInteger
var IDENT_5: Number := 5.asInteger

type Record = interface {
    ptrComp
    discr
    enumComp
    intComp
    stringComp
    copy
    asString
}

class Record(ptrComp': Record, discr': Number, enumComp': Number, intComp': Number, stringComp': String) -> Record {
    var    ptrComp: Record := ptrComp'
    var      discr: Number := discr'
    var   enumComp: Number := enumComp'
    var    intComp: Number := intComp'
    var stringComp: String := stringComp'

    method copy -> Record {
        Record(ptrComp, discr, enumComp, intComp, stringComp)
    }

    method asString -> String {
        "Record:  ptrComp: {ptrComp}\n  discr: {discr}\n  enumComp: {enumComp}\n  intComp: {intComp}\n  stringComp: {stringComp}\n"
    }
}

method Record -> Record {
    Record(Done, 0, 0, 0, "")
}

var intGlob: Number := 0.asInteger
var boolGlob: Boolean := false
var char1Glob: String := "\\0"
var char2_glob: String := "\\0"
var array1Glob: List := platform.kernel.Array. new (51.asInteger) withAll { 0.asInteger }
var array2Glob: List := platform.kernel.Array. new (51.asInteger) withAll { array1Glob }
var ptrGlb: Record

// --------------------------------------------------------------------------------------------------------------
// Procedures

method proc0(innerIterations: Number) -> Done {
    ptrGlb := Record
    ptrGlb.ptrComp := Record
    ptrGlb.discr := IDENT_1
    ptrGlb.enumComp := IDENT_3
    ptrGlb.intComp := 40.asInteger
    ptrGlb.stringComp := "DHRYSTONE PROGRAM, SOME STRING"

    var string1Loc: String
    var string2Loc: String
    var intLoc1: Number
    var intLoc2: Number
    var intLoc3: Number
    var enumLoc: Number

    string1Loc := "DHRYSTONE PROGRAM, 1'ST STRING"
    array2Glob.at(9.asInteger).at (8.asInteger) put (10.asInteger)

    1.asInteger.to(innerIterations) do { i: Number ->
        proc5
        proc4
        intLoc1 := 2.asInteger
        intLoc2 := 3.asInteger
        string2Loc := "DHRYSTONE PROGRAM, 2'ND STRING"

        enumLoc := IDENT_2
        boolGlob := !(func2(string1Loc, string2Loc))
        { intLoc1 < intLoc2 }.whileTrue {
            intLoc3 := 5.asInteger * intLoc1 - intLoc2
            intLoc3 := proc7(intLoc1, intLoc2)
            intLoc1 := intLoc1 + 1.asInteger
        }

        proc8(array1Glob, array2Glob, intLoc1, intLoc3)
        ptrGlb := proc1(ptrGlb)

        var charIndex: String := "A"
        { charIndex <= char2_glob }. whileTrue {
            (enumLoc == func1(charIndex, "C")). ifTrue {
                enumLoc := proc6(IDENT_1)
            }
            charIndex := (charIndex.codepointAt(1.asInteger) + 1.asInteger).asCodepointString
        }

        intLoc3 := intLoc2 * intLoc1
        intLoc2 := intLoc3 / intLoc1
        intLoc2 := 7.asInteger * (intLoc3 - intLoc2) - intLoc1
        intLoc1 := proc2(intLoc1)
    }
}

method proc1(ptrParIn': Record) -> Record {
    var ptrParIn: Record := ptrParIn'
    var tmpPtr: Record := ptrGlb.copy()

    ptrParIn.ptrComp := tmpPtr
    var nextRecord: Record := tmpPtr

    ptrParIn.intComp := 5.asInteger
    nextRecord.intComp := ptrParIn.intComp
    nextRecord.ptrComp := ptrParIn.ptrComp
    nextRecord.ptrComp := proc3(nextRecord.ptrComp)

    (nextRecord.discr == IDENT_1).ifTrue {
        nextRecord.intComp  := 6.asInteger
        nextRecord.enumComp := proc6(ptrParIn.enumComp)
        nextRecord.ptrComp  := ptrGlb.ptrComp
        nextRecord.intComp  := proc7(nextRecord.intComp, 10.asInteger)
    } ifFalse {
        ptrParIn := nextRecord.copy()
    }

    nextRecord.ptrComp := Done
    ptrParIn
}

method proc2(intParIO': Number) -> Number {
    var intParIO: Number := intParIO'
    var intLoc: Number := intParIO + 10.asInteger
    var enumLoc: Number

    { true }. whileTrue {
        (char1Glob == "A").ifTrue {
            intLoc := intLoc - 1
            intParIO := intLoc - intGlob
            enumLoc := IDENT_1
        }
        (enumLoc == IDENT_1).ifTrue { return intParIO }
    }
}

method proc3(ptrParOut': Record) -> Record {
    var ptrParOut: Record := ptrParOut'

    (!ptrGlb.isNil).ifTrue {
        ptrParOut := ptrGlb.ptrComp
    } ifFalse {
        intGlob := 100.asInteger
    }
    ptrGlb.intComp := proc7(10.asInteger, intGlob)
    ptrParOut
}


method proc4 -> Done {
    var boolLoc: Boolean := char1Glob == "A"
    var boolLoc: Boolean := boolLoc || boolGlob
    char2_glob := "B"
    Done
}

method proc5 -> Done {
    char1Glob := "A"
    boolGlob := false
    Done
}

method proc6(enumParIn: Number) -> Number {
    var enumParOut: Number := enumParIn
    func3(enumParIn).ifFalse { enumParOut := IDENT_4 }

    (enumParIn == IDENT_1).ifTrue { return IDENT_1 }
    (enumParIn == IDENT_2).ifTrue { return (intGlob > 100).ifTrue { IDENT_1 } ifFalse { IDENT_4 } }
    (enumParIn == IDENT_3).ifTrue { return IDENT_2 }
    (enumParIn == IDENT_4).ifTrue { }
    (enumParIn == IDENT_5).ifTrue { return IDENT_3 }

    enumParOut
}

method proc7(intParI1: Number, intParI2: Number) -> Number {
    var intLoc: Number := intParI1 + 2.asInteger
    var intParOut: Number := intParI2 + intLoc

    intParOut
}

method proc8(array1Par: List, array2Par: List, intParI1: Number, intParI2: Number) -> Done {
    var intLoc: Number := intParI1 + 5.asInteger

    array1Par .at (intLoc + 1.asInteger  ) put( intParI2 )
    array1Par .at (intLoc + 2.asInteger  ) put( array1Par.at(intLoc + 1.asInteger) )
    array1Par .at (intLoc + 31.asInteger ) put( intLoc )

    intLoc.to(intLoc + 1.asInteger) do { intIndex: Number ->
        array2Par.at (intLoc + 1.asInteger).at (intIndex + 1.asInteger) put (intLoc)
    }

    array2Par.at ( intLoc +  1.asInteger ) .at (intLoc)               put( array2Par.at (intLoc + 1.asInteger).at(intLoc) + 1.asInteger )
    array2Par.at ( intLoc + 21.asInteger ) .at (intLoc + 1.asInteger) put( array1Par.at (intLoc + 1.asInteger)                          )

    intGlob := 5.asInteger
    Done
}

//
// --------------------------------------------------------------------------------------------------------------

// --------------------------------------------------------------------------------------------------------------
// Functions

method func1(charPar1: String, charPar2: String) -> Number {
    var charLoc1: String := charPar1
    var charLoc2: String := charLoc1
    (charLoc2 != charPar2).ifTrue {
        return IDENT_1
    } ifFalse {
        return IDENT_2
    }
}

method func2(strParI1: String, strParI2: String) -> Boolean {
    var intLoc: Number := 1.asInteger
    var charLoc: String

    {intLoc <= 1}. whileTrue {
        (func1(strParI1.charAt(intLoc + 1.asInteger), strParI2.charAt(intLoc + 2.asInteger)) == IDENT_1).ifTrue {
            charLoc := "A"
            intLoc := intLoc + 1.asInteger
        }

    }

    ((charLoc >= "W") && (charLoc <= "Z")).ifTrue { intLoc := 7.asInteger }

    (charLoc == "X"). ifTrue {
        return true
    } ifFalse {
        (strParI1 > strParI2). ifTrue {
            intLoc := intLoc + 7.asInteger
            return true
        } ifFalse {
            return false
        }
    }
}

method func3(enumParIn: Number) -> Boolean {
    var enumLoc: Number := enumParIn
    return (enumLoc == IDENT_3).ifTrue { true } ifFalse { false }
}

class newPyStone -> Benchmark {
    inherit harness.newBenchmark

    method innerBenchmarkLoop(innerIterations: Number) -> Boolean {
        proc0(innerIterations)
        // print("intGlob: {intGlob}")
        // print("boolGlob: {boolGlob}")
        // print("char1Glob: {char1Glob}")
        // print("char2_glob: {char2_glob}")
        // print("array1Glob: {array1Glob}")
        // print("array2Glob: {array2Glob}")
        // print("ptrGlb: {ptrGlb}")
        true
    }
}

method newInstance -> Benchmark { newPyStone }
