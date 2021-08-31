<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN">
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="[%d{ISO8601}] [%-5p] [%t#%T] %c#%L - %msg%n"/>
        </Console>

        <RollingFile name="AppRollingFile" fileName="{{ app_log_dir }}/app.log" filePattern="{{ app_log_dir }}/app-%d{MM-dd-yyyy}-%i.log.gz">
            <PatternLayout pattern="[%d{ISO8601}] [%-5p] [%t#%T] %c#%L - %msg%n"/>
            <Policies>
                <CronTriggeringPolicy schedule="0 0 23 * * ?"/>
            </Policies>
        </RollingFile>

        <RollingFile name="access" fileName="{{ app_log_dir }}/access.log" filePattern="{{ app_log_dir }}/access-%d{MM-dd-yyyy}-%i.log.gz">
            <PatternLayout pattern="%msg%n"/>
            <Policies>
                <CronTriggeringPolicy schedule="0 0 23 * * ?"/>
            </Policies>
        </RollingFile>
    </Appenders>
    <Loggers>
        <Root level="info">
            <AppenderRef ref="Console"/>
            <AppenderRef ref="AppRollingFile"/>
        </Root>
        <Logger name="access" level="trace" additivity="false">
            <AppenderRef ref="access"/>
        </Logger>
    </Loggers>
</Configuration>
