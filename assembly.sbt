// Any customized assembly settings may be written here.
// See https://github.com/sbt/sbt-assembly for available parameters.

// We do not want to run the test when assembling because we prefer to chain the various build steps manually, e.g.
// via `./sbt.sh clean test scoverage:test package packageDoc packageSrc doc assembly`.  Because, in this scenario, we
// have already run the tests before reaching the assembly step, we do not re-run the tests again.
//
// Comment the following line if you do want to (re-)run all the tests before building assembly.
test in assembly := {}

mergeStrategy in assembly <<= (mergeStrategy in assembly) {
  (old) => {
    case s if (s.endsWith(".class") || s.endsWith(".properties") || s.endsWith(".xsd") || s.endsWith(".dtd")) => MergeStrategy.last
    case x => old(x)
  }
}
