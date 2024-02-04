EpMonitor current disable.
Author fullName: 'SomLoader'.

PharoCommandLineHandler compile: 'runPreferences
    "Disabled preference loading for benchmarking"
    ^ self' classified: '*SomLoader'.

'Scripting.st' asFileReference fileIn.
'SomLoader-Compiler.st' asFileReference fileIn.
'SomLoader-Tests.st' asFileReference fileIn.
'build-image-part2.st' asFileReference fileIn.
