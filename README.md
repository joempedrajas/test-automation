
After initial checkout run this command to build:
-------------------------------------------------
        $ mvn install

Options for command line:
------------------------
        -DskipTests=<true|false> <--- usually during installing and you don't want to test right away
        -DsuiteXmlFile=<path to XML file>
        -DtestResultDirectory=<folder destination name>
        -DdriverBrowser=<CHROME|IE|FIREFOX|OPERA>
        -DenableGrid=<true|false> default is false
        -DgridPlatform=<check selenium supported platform> default WIN8
        -Djira.username=
        -Djira.password=
        -Djira.create.execution=<true|false> default false
        -Djira.defect.logging=<true|false> default is false
        -Djira.execution.summary= default value "EXECUTE-yyyyMMMdd-hh:mm:ss.S"
        -Denvironment=<DEV|UT|SIT>

        $ mvn install -DskipTests=true
        $ mvn test -DsuiteXmlFile=suites\testNG.xml -DtestResultDirectory -DtestResultDirectory=test-folder -DdriverBrowser=FIREFOX

        or use

        $ mvn test <--- use default testNG.xml, Results directory and driver browser

When running in local using webdriver:
--------------------------------------
		-Install web driver executable files in "C:\Selenium WebDriver\" e.g. "C:\Selenium WebDriver\chromedriver.exe"