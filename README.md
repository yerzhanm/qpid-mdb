qpid-mdb
========

qpid-mdb example jboss-eap-6

Deployment
==========
To deploy the Qpid JCA adapter in the JBoss EAP 6 environment, copy the qpid-ra-<version>.rar file
to your JBoss deployment directory. By default this can be found at

JBOSS_HOME/`<server-config>`/deployments

--
qpid-jca-0.18 http://svn.apache.org/repos/asf/qpid/tags/0.18/qpid/java/jca/

Configuration
=============

JBOSS_HOME/`<server-config>`/configuration

The varying XML files are named

`<server-config>-full.xml`
`<server-config>-full-ha.xml`
`<server-config>.xml`


add or replace messaging provider in the EAP 6.x environment in

    <subsystem xmlns="urn:jboss:domain:ejb3:1.3">
    	...
    	<mdb>
    	    <resource-adapter-ref resource-adapter-name="qpid-ra-<version>.rar"/>
    	    <bean-instance-pool-ref pool-name="mdb-strict-max-pool"/>
    	</mdb>
    	...
    </subsystems>


The following XML fragment provides a minimal example configuration in the EAP 6 environment.

    <subsystem xmlns="urn:jboss:domain:resource-adapters:1.0">
        <resource-adapters>
            <resource-adapter>
                <archive>
                    qpid-ra-<version>.rar
                </archive>
                <transaction-support>XATransaction</transaction-support>
                <config-property name="connectionURL">
                    amqp://guest:guest@localhost/?brokerlist='tcp://localhost:5672?sasl_mechs='PLAIN''
                </config-property>
                <config-property name="TransactionManagerLocatorClass">
                    org.apache.qpid.ra.tm.JBoss7TransactionManagerLocator
                </config-property>
                <config-property name="TransactionManagerLocatorMethod">
                    getTm
                </config-property>
                <connection-definitions>
                    <connection-definition class-name="org.apache.qpid.ra.QpidRAManagedConnectionFactory" jndi-name="QpidJMSXA" pool-name="QpidJMSXA">
                        <config-property name="connectionURL">
                            amqp://guest:guest@localhost/?brokerlist='tcp://localhost:5672?sasl_mechs='PLAIN''
                        </config-property>
                        <config-property name="SessionDefaultType">
                            javax.jms.Queue
                        </config-property>
                    </connection-definition>
                </connection-definitions>
                <admin-objects>
                    <admin-object class-name="org.apache.qpid.ra.admin.QpidQueueImpl" jndi-name="java:jboss/exported/TestQueue" use-java-context="false" pool-name="TestQueue">
                        <config-property name="DestinationAddress">
                            test_queue;{create:always, node:{type:queue, x-declare:{auto-delete:true}}}
                        </config-property>
                    </admin-object>
                </admin-objects>
            </resource-adapter>
        </resource-adapters>
    </subsystem>
