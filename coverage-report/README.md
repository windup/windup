Windup Coverage Report
======================

This Maven module produces an aggregate JaCoCo coverage report
for all of `org.jboss.windup`. It relies on being built as a _last_
step of the Maven reactor. All other modules produce JaCoCo coverage
data files that this module consumes.

To enable collecting coverage data and producing the report,
just enable the `jacoco` Maven profile and execute the Maven build
at least up to the `package` phase from the top-level directory:

    mvn clean package -Pjacoco

The coverage report will then be present in

    coverage-report/target/coverage-report

The aggregate coverage report is produced using JaCoCo Ant task,
because the JaCoCo Maven plugin doesn't support aggregation yet.
