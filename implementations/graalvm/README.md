# Obtaining GraalVM

To run benchmarks on the GraalVM, please download a copy from 
the Oracle Technology Network:

  http://www.oracle.com/technetwork/oracle-labs/program-languages/overview/index.html

We tested with
  http://download.oracle.com/otn/utilities_drivers/oracle-labs/graalvm-0.24-linux-x86_64-dk.tar.gz
and
  http://download.oracle.com/otn/utilities_drivers/oracle-labs/graalvm-0.24-macosx-x86_64-dk.tar.gz

To extract the GraalVM into this folder, you can use a command like this:

```bash
tar xvf graalvm-0.24-linux-x86_64-dk.tar.gz --strip-components 1
```

Afterwards, the Java binary is expected to be in
`implementations/graalvm/bin/java`.
