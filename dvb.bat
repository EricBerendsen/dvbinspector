:: leave JVM_FLAGS empty to let java determine heap size (depends on java version and machine memory).
:: When you get an out of memory error (when parsing PES pids, or with large DSM-CC carrousels) you can use -Xmx to increase maximum heap size. 
:: Use -Xms to set initial Java heap size. This can also help performance for large files. Suggestion try something like 

set JVM_FLAGS=

:: uncomment next line to set maximum heap size to 1000 MByte
::set JVM_FLAGS=-Xmx1000m

:: uncomment next line to set initial and maximum heap size to 4 GByte
::set JVM_FLAGS=-Xmx4g -Xms4g

java -classpath .;.\lib\jfreechart-1.5.6.jar;.\lib\opencsv-5.11.2.jar;.\lib\jlayer-1.0.1.4.jar;.\lib\commons-text-1.13.1.jar;.\lib\commons-logging-1.3.5.jar;.\lib\commons-lang3-3.17.0.jar;.\lib\commons-collections4-4.5.jar;.\lib\commons-collections-3.2.2.jar;.\lib\commons-beanutils-1.11.0.jar;.\DVBinspector.jar %JVM_FLAGS% -Djava.util.logging.config.file=src/main/resources/logging.properties nl.digitalekabeltelevisie.main.DVBinspector
