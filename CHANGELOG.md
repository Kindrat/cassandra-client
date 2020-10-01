# Changelog

## 1.0.3
* Migrated from Java to Kotlin
* New cassandra driver **4.6.1** without dependency on old Guava
* Some UI widgets are now created via factory methods instead of Spring prototypes
* More straightforward application init by using Spring events instead of tricky Context-Application crosslinks
* Updated OpenFX from **12** to **16**


* *Added test Gradle plugin to setup cassandra DB*
* *Updated Gradle to 6.6.1*
* *Functional tests with test framework to validate UI state*

### Known issues
* Widgets creation and component initialization is slow and visible to user

