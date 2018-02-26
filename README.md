#CAMT File Reporter
Scala language is used for the task.
 - `Scopt` used for command line parameters parsing
 - `Scala-logging` used together with logback slf4j impl
 - `sbt-scalaxb` (Scalaxb or known as jaxb in java) is used. Generated classes are under `camt-reporter\target\scala-2.12\src_managed\main\sbt-scalaxb`  

Scala is chosen as it makes to play with data easier
Objects are used so everything is kept as functional as possible

TODOs 
 - Metrics could be added
 - Template engines could be used to generate the report
 - Benchmark test could be added

##Prerequisites 
 - Install scala 2.12
 - Install sbt 1.1.1 or newer
##Test with Coverage and Report
```bash
sbt clean coverage test coverageReport
```
##Build
```bash
sbt assembly
```
##Docker Build 
```bash
sbt docker:publishLocal
```
##Run
```bash
java -jar target\scala-2.12\camt-reporter.jar -c 3 -f src/test/resources/camt.xml 
```
- Point logback file with `-Dlogback.configurationFile=path/to/logback.xml` standard jvm parameter argument before jar name

##Docker Run
docker run -v ${localPath}/camt.xml:/opt/camt.xml -it camt-reporter:0.1 -c 3 -f /opt/camt.xml