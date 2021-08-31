<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN">
    <Properties>
        <Property name="audit_log_path">{{ app_log_dir }}</Property>
    </Properties>
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="[%d{ISO8601}] [%-5p] [%t#%T] %c#%L - %msg%n"/>
        </Console>

        <RollingFile name="AppRollingFile" fileName="{{ app_log_dir }}/app.log" filePattern="{{ app_log_dir }}/logs/app-%d{MM-dd-yyyy}-%i.log.gz">
            <PatternLayout pattern="[%d{ISO8601}] [%-5p] [%t#%T] %c#%L - %msg%n"/>
            <Policies>
                <CronTriggeringPolicy schedule="0 0 23 * * ?"/>
            </Policies>
        </RollingFile>

        <RollingFile name="access" fileName="{{ app_log_dir }}/access.log" filePattern="{{ app_log_dir }}/logs/access-%d{MM-dd-yyyy}-%i.log.gz">
            <PatternLayout pattern="%msg%n"/>
            <Policies>
                <CronTriggeringPolicy schedule="0 0 23 * * ?"/>
            </Policies>
        </RollingFile>
        <!--<RollingFile name="ErrorRollingFile" fileName="logs/errors.log"-->
        <!--filePattern="logs/errors-%d{MM-dd-yyyy}-%i.log.gz">-->
        <!--<PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>-->
        <!--<Policies>-->
        <!--<CronTriggeringPolicy schedule="0 0 23 * * ?"/>-->
        <!--</Policies>-->
        <!--</RollingFile>-->
    </Appenders>
    <Loggers>
        <Logger name="graphql" level="info" additivity="false">
            <AppenderRef ref="Console"/>
            <AppenderRef ref="AppRollingFile"/>
        </Logger>
        <Logger name="io.growing" level="debug" additivity="false">
            <AppenderRef ref="Console"/>
            <AppenderRef ref="AppRollingFile"/>
        </Logger>
        <Logger name="com.zaxxer.hikari" level="warn" additivity="false">
            <AppenderRef ref="Console"/>
            <AppenderRef ref="AppRollingFile"/>
        </Logger>
        <Logger name="org.asynchttpclient" level="debug" additivity="false">
            <AppenderRef ref="Console"/>
            <AppenderRef ref="AppRollingFile"/>
        </Logger>
        <Logger name="org.asynchttpclient.netty" level="warn" additivity="false">
            <AppenderRef ref="Console"/>
            <AppenderRef ref="AppRollingFile"/>
        </Logger>
        <Root level="INFO">
            <AppenderRef ref="Console"/>
            <AppenderRef ref="AppRollingFile"/>
        </Root>
        <Logger name="access" level="trace" additivity="false">
            <AppenderRef ref="access"/>
        </Logger>
    </Loggers>
</Configuration>
