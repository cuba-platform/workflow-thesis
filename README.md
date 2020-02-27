# Cuba Workflow Subsystem
# Special version for THESIS ECM


[![license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
[![Build Status](https://travis-ci.org/cuba-platform/reports.svg?branch=master)](https://travis-ci.org/cuba-platform/workflow-thesis)
[![Documentation](https://img.shields.io/badge/documentation-online-03a9f4.svg)](https://docs.cuba-platform.com/cuba/5.6/workflow/ru/html-single/workflow.html)

Cuba Workflow Subsystem is to create applications with business processes management functionality in CUBA applications.

For more information see [github.com/cuba-platform/cuba-thesis](https://github.com/cuba-platform/cuba-thesis).

## Build and install

In order to build the add-on from source, you need to install the following:
* Java 8 Development Kit (JDK)
* [CUBA Gradle Plugin](https://github.com/cuba-platform/cuba-gradle-plugin-thesis)
* [CUBA](https://github.com/cuba-platform/cuba-thesis)

Let's assume that you have cloned sources into the following directories:
```
work/
    cuba/
    cuba-gradle-plugin/
    workflow/
```

Open terminal in the `work` directory and run the following command to build and install the plugin into your local Maven repository (`~/.m2`):
```
cd cuba-gradle-plugin
gradlew install
```

After that, go to the cuba directory and build and install it with the same command:
```
cd ../cuba
gradlew install
```

Finally, go to the workflow directory and build and install it with the same command:
```
cd ../workflow
gradlew install
```
