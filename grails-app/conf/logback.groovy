import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.FileAppender
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy
import grails.util.BuildSettings
import grails.util.Environment

import static ch.qos.logback.classic.Level.DEBUG

// See http://logback.qos.ch/manual/groovy.html for details on configuration
appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%level %logger - %msg%n"
    }
}
List<String> appendersList = ['STDOUT']
def logToFile = true
if(logToFile) {
    appender("ROLLING_FILE", RollingFileAppender) {
        if (Environment.currentEnvironment == Environment.PRODUCTION) {
            file = "/opt/tomcat/logs/alexaVictorOPS.log"
        } else {
            file = "alexaVictorOPS.log"
        }
        rollingPolicy(TimeBasedRollingPolicy) {
            fileNamePattern = "app.%d{yyyy-MM-dd}.log"
            maxHistory = 30
        }
        encoder(PatternLayoutEncoder) {
            pattern = "%-5p %d{yyyy-MM-dd HH:mm:ss:SS} %c{2} %m %n"
        }
    }
    appendersList << "ROLLING_FILE"
}
root(WARN, appendersList)

[
        'com.com.com.vanderfox',
        'com.com.vanderfox',
        'devopsassistant'
        //'org.grails.plugins',
        //'org.springframework',

].each {
    logger(it, DEBUG, appendersList, false)
}

def targetDir = BuildSettings.TARGET_DIR
if (Environment.isDevelopmentMode() && targetDir) {
    appender("FULL_STACKTRACE", FileAppender) {
        file = "${targetDir}/stacktrace.log"
        append = true
        encoder(PatternLayoutEncoder) {
            pattern = "%level %logger - %msg%n"
        }
    }
    logger("StackTrace", ERROR, ['FULL_STACKTRACE'], false)
}
