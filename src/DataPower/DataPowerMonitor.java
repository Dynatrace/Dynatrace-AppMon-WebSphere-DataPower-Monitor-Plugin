package DataPower;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.dynatrace.diagnostics.global.Constants;
import com.dynatrace.diagnostics.pdk.Migrator;
import com.dynatrace.diagnostics.pdk.Monitor;
import com.dynatrace.diagnostics.pdk.MonitorEnvironment;
import com.dynatrace.diagnostics.pdk.MonitorMeasure;
import com.dynatrace.diagnostics.pdk.Property;
import com.dynatrace.diagnostics.pdk.PropertyContainer;
import com.dynatrace.diagnostics.pdk.Status;

public class DataPowerMonitor implements Monitor, Migrator {

    public int httpStatusCode = 0;
    public int headerSize = 0;
    public long firstResponseTime = 0;
    public long responseCompleteTime = 0;
    public int inputSize = 0;
    public long connectionCloseDelay = 0;
    public boolean verified = false;
    public long time;
    public int oneday;
    public int onehour;
    public int oneminute;
    public int tenminutes;
    public int tenseconds;
    private static final int READ_CHUNK_SIZE = 1024;
    private static final double MILLIS = 0.000001;
    private static final double SECS = 0.000000001;
    // configuration constants
    private static final String CONFIG_PROTOCOL = "protocol";
    private static final String CONFIG_PATH = "path";
    private static final String CONFIG_HTTP_PORT = "httpPort";
    private static final String CONFIG_HTTPS_PORT = "httpsPort";
    private static final String CONFIG_METHOD = "method";
    private static final String CONFIG_POST_DATA = "postData";
    private static final String CONFIG_USER_AGENT = "userAgent";
    private static final String CONFIG_HTTP_VERSION = "httpVersion";
    private static final String CONFIG_MAX_REDIRECTS = "maxRedirects";
    private static final String CONFIG_DT_TAGGING = "dtTagging";
    private static final String CONFIG_DT_TIMER_NAME = "dtTimerName";
    private static final String CONFIG_MATCH_CONTENT = "matchContent";
    private static final String CONFIG_SEARCH_STRING = "searchString";
    private static final String CONFIG_COMPARE_BYTES = "compareBytes";
    private static final String CONFIG_SERVER_AUTH = "serverAuth";
    private static final String CONFIG_SERVER_USERNAME = "serverUsername";
    private static final String CONFIG_SERVER_PASSWORD = "serverPassword";
    private static final String CONFIG_USE_PROXY = "useProxy";
    private static final String CONFIG_PROXY_HOST = "proxyHost";
    private static final String CONFIG_PROXY_PORT = "proxyPort";
    private static final String CONFIG_PROXY_AUTH = "proxyAuth";
    private static final String CONFIG_PROXY_USERNAME = "proxyUsername";
    private static final String CONFIG_PROXY_PASSWORD = "proxyPassword";
    private static final String CONFIG_IGNORE_CERTIFICATE = "ignoreCertificate";
    // measure constants
    private static final String AVAILABILITY_METRIC_GROUP = "AvailabilityMeasures";
    private static final String MSR_HOST_REACHABLE = "HostReachable";
    private static final String MSR_HEADER_SIZE = "HeaderSize";
    private static final String MSR_FIRST_RESPONSE_DELAY = "FirstResponseDelay";
    private static final String MSR_RESPONSE_COMPLETE_TIME = "ResponseCompleteTime";
    private static final String MSR_RESPONSE_SIZE = "ResponseSize";
    private static final String MSR_THROUGHPUT = "Throughput";
    private static final String MSR_HTTP_STATUS_CODE = "HttpStatusCode";
    private static final String MSR_CONN_CLOSE_DELAY = "ConnectionCloseDelay";
    private static final String MSR_CONTENT_VERIFIED = "ContentVerified";
    private static final Logger log = Logger.getLogger(DataPowerMonitor.class.getName());
    private static final String PROTOCOL_HTTPS = "https";
    private static final String PROTOCOL_HTTP = "http";
    private static final String CONNECTIONSACCEPTED_METRIC_GROUP = "ConnectionsAcceptedMeasures";
    private static final String MSR_CONNECTIONSACCEPTED_ONEDAY = "ConnectionsAccepted_oneDay";
    private static final String MSR_CONNECTIONSACCEPTED_ONEHOUR = "ConnectionsAccepted_oneHour";
    private static final String MSR_CONNECTIONSACCEPTED_ONEMINUTE = "ConnectionsAccepted_oneMinute";
    private static final String MSR_CONNECTIONSACCEPTED_TENMINUTES = "ConnectionsAccepted_tenMinutes";
    private static final String MSR_CONNECTIONSACCEPTED_TENSECONDS = "ConnectionsAccepted_tenSeconds";
    private static final String CPUUSAGE_METRIC_GROUP = "CPUUsageMeasures";
    private static final String MSR_CPUUSAGE_ONEDAY = "CPUUsage_oneDay";
    private static final String MSR_CPUUSAGE_ONEHOUR = "CPUUsage_oneHour";
    private static final String MSR_CPUUSAGE_ONEMINUTE = "CPUUsage_oneMinute";
    private static final String MSR_CPUUSAGE_TENMINUTES = "CPUUsage_tenMinutes";
    private static final String MSR_CPUUSAGE_TENSECONDS = "CPUUsage_tenSeconds";
    private static final String DOCUMENTCACHINGSUMMARY_METRIC_GROUP = "DocumentCachingSummaryMeasures";
    private static final String MSR_DOCUMENTCACHINGSUMMARY_CACHECOUNT = "DocumentCachingSummary_CacheCount";
    private static final String MSR_DOCUMENTCACHINGSUMMARY_DOCCOUNT = "DocumentCachingSummary_DocCount";
    private static final String MSR_DOCUMENTCACHINGSUMMARY_CACHESIZE = "DocumentCachingSummary_CacheSize";
    private static final String MSR_DOCUMENTCACHINGSUMMARY_BYTECOUNT = "DocumentCachingSummary_ByteCount";
    private static final String STYLESHEETCACHINGSUMMARY_METRIC_GROUP = "StylesheetCachingSummaryMeasures";
    private static final String MSR_STYLESHEETCACHINGSUMMARY_CACHECOUNT = "StylesheetCachingSummary_CacheCount";
    private static final String MSR_STYLESHEETCACHINGSUMMARY_READYCOUNT = "StylesheetCachingSummary_ReadyCount";
    private static final String MSR_STYLESHEETCACHINGSUMMARY_CACHESIZE = "StylesheetCachingSummary_CacheSize";
    private static final String MSR_STYLESHEETCACHINGSUMMARY_PENDINGCOUNT = "StylesheetCachingSummary_PendingCount";
    private static final String MSR_STYLESHEETCACHINGSUMMARY_BADCOUNT = "StylesheetCachingSummary_BadCount";
    private static final String MSR_STYLESHEETCACHINGSUMMARY_DUPCOUNT = "StylesheetCachingSummary_DupCount";
    private static final String MSR_STYLESHEETCACHINGSUMMARY_CACHEKBCOUNT = "StylesheetCachingSummary_CacheKBCount";
    private static final String ENVIRONMENTALSENSORS_METRIC_GROUP = "EnvironmentalSensorsMeasures";
    private static final String MSR_ENVIRONMENTALSENSORS_SYSTEMTEMP = "EnvironmentalSensors_systemTemp";
    private static final String MSR_ENVIRONMENTALSENSORS_CPU1TEMP = "EnvironmentalSensors_CPU1Temp";
    private static final String MSR_ENVIRONMENTALSENSORS_CPU2TEMP = "EnvironmentalSensors_CPU2Temp";
    private static final String MSR_ENVIRONMENTALSENSORS_CPU1RPM = "EnvironmentalSensors_CPU1RPM";
    private static final String MSR_ENVIRONMENTALSENSORS_CPU2RPM = "EnvironmentalSensors_CPU2RPM";
    private static final String MSR_ENVIRONMENTALSENSORS_CHASSIS1RPM = "EnvironmentalSensors_Chassis1RPM";
    private static final String MSR_ENVIRONMENTALSENSORS_CHASSIS2RPM = "EnvironmentalSensors_Chassis2RPM";
    private static final String MSR_ENVIRONMENTALSENSORS_CHASSIS3RPM = "EnvironmentalSensors_Chassis3RPM";
    private static final String MSR_ENVIRONMENTALSENSORS_CASEOPEN = "EnvironmentalSensors_CaseOpen";
    private static final String MSR_ENVIRONMENTALSENSORS_VOLT33 = "EnvironmentalSensors_Volt33";
    private static final String MSR_ENVIRONMENTALSENSORS_VOLT5 = "EnvironmentalSensors_Volt5";
    private static final String MSR_ENVIRONMENTALSENSORS_VOLT12 = "EnvironmentalSensors_Volt12";
    private static final String MSR_ENVIRONMENTALSENSORS_POWERSUPPLYOK = "EnvironmentalSensors_PowerSupplyOk";
    private static final String HTTPCONNECTIONS_METRIC_GROUP = "HTTPConnectionsMeasures";
    private static final String MSR_HTTPCONNECTIONS_REQTENSEC = "HTTPConnections_ReqTenSec";
    private static final String MSR_HTTPCONNECTIONS_REQONEMIN = "HTTPConnections_ReqOneMin";
    private static final String MSR_HTTPCONNECTIONS_REQTENMIN = "HTTPConnections_ReqTenMin";
    private static final String MSR_HTTPCONNECTIONS_REQONEHR = "HTTPConnections_ReqOneHr";
    private static final String MSR_HTTPCONNECTIONS_REQONEDAY = "HTTPConnections_ReqOneDay";
    private static final String MSR_HTTPCONNECTIONS_REUSETENSEC = "HTTPConnections_ReuseTenSec";
    private static final String MSR_HTTPCONNECTIONS_REUSEONEMIN = "HTTPConnections_ReuseOneMin";
    private static final String MSR_HTTPCONNECTIONS_REUSETENMIN = "HTTPConnections_ReuseTenMin";
    private static final String MSR_HTTPCONNECTIONS_REUSEONEHR = "HTTPConnections_ReuseOneHr";
    private static final String MSR_HTTPCONNECTIONS_REUSEONEDAY = "HTTPConnections_ReuseOneDay";
    private static final String MSR_HTTPCONNECTIONS_CREATETENSEC = "HTTPConnections_CreateTenSec";
    private static final String MSR_HTTPCONNECTIONS_CREATEONEMIN = "HTTPConnections_CreateOneMin";
    private static final String MSR_HTTPCONNECTIONS_CREATETENMIN = "HTTPConnections_CreateTenMin";
    private static final String MSR_HTTPCONNECTIONS_CREATEONEHR = "HTTPConnections_CreateOneHr";
    private static final String MSR_HTTPCONNECTIONS_CREATEONEDAY = "HTTPConnections_CreateOneDay";
    private static final String MSR_HTTPCONNECTIONS_RETURNTENSEC = "HTTPConnections_ReturnTenSec";
    private static final String MSR_HTTPCONNECTIONS_RETURNONEMIN = "HTTPConnections_ReturnOneMin";
    private static final String MSR_HTTPCONNECTIONS_RETURNTENMIN = "HTTPConnections_ReturnTenMin";
    private static final String MSR_HTTPCONNECTIONS_RETURNONEHR = "HTTPConnections_ReturnOneHr";
    private static final String MSR_HTTPCONNECTIONS_RETURNONEDAY = "HTTPConnections_ReturnOneDay";
    private static final String MSR_HTTPCONNECTIONS_OFFERTENSEC = "HTTPConnections_OfferTenSec";
    private static final String MSR_HTTPCONNECTIONS_OFFERONEMIN = "HTTPConnections_OfferOneMin";
    private static final String MSR_HTTPCONNECTIONS_OFFERTENMIN = "HTTPConnections_OfferTenMin";
    private static final String MSR_HTTPCONNECTIONS_OFFERONEHR = "HTTPConnections_OfferOneHr";
    private static final String MSR_HTTPCONNECTIONS_OFFERONEDAY = "HTTPConnections_OfferOneDay";
    private static final String MSR_HTTPCONNECTIONS_DESTROYTENSEC = "HTTPConnections_DestroyTenSec";
    private static final String MSR_HTTPCONNECTIONS_DESTROYONEMIN = "HTTPConnections_DestroyOneMin";
    private static final String MSR_HTTPCONNECTIONS_DESTROYTENMIN = "HTTPConnections_DestroyTenMin";
    private static final String MSR_HTTPCONNECTIONS_DESTROYONEHR = "HTTPConnections_DestroyOneHr";
    private static final String MSR_HTTPCONNECTIONS_DESTROYONEDAY = "HTTPConnections_DestroyOneDay";
    private static final String MEMORYSTATUS_METRIC_GROUP = "MemoryStatusMeasures";
    private static final String MSR_MEMORYSTATUS_FREEMEMORY = "MemoryStatus_FreeMemory";
    private static final String MSR_MEMORYSTATUS_HOLDMEMORY = "MemoryStatus_HoldMemory";
    private static final String MSR_MEMORYSTATUS_REQMEMORY = "MemoryStatus_ReqMemory";
    private static final String MSR_MEMORYSTATUS_TOTALMEMORY = "MemoryStatus_TotalMemory";
    private static final String MSR_MEMORYSTATUS_USAGE = "MemoryStatus_Usage";
    private static final String MSR_MEMORYSTATUS_USEDMEMORY = "MemoryStatus_UsedMemory";
    private static final String FILESYSTEMSTATUS_METRIC_GROUP = "FileSystemStatusMeasures";
    private static final String MSR_FILESYSTEMSTATUS_FREEENCRYPTED = "FileSystemStatus_FreeEncrypted";
    private static final String MSR_FILESYSTEMSTATUS_TOTALENCRYPTED = "FileSystemStatus_TotalEncrypted";
    private static final String MSR_FILESYSTEMSTATUS_FREETEMPORARY = "FileSystemStatus_FreeTemporary";
    private static final String MSR_FILESYSTEMSTATUS_TOTALTEMPORARY = "FileSystemStatus_TotalTemporary";
    private static final String MSR_FILESYSTEMSTATUS_FREEINTERNAL = "FileSystemStatus_FreeInternal";
    private static final String MSR_FILESYSTEMSTATUS_TOTALINTERNAL = "FileSystemStatus_TotalInternal";
    private static final String ETHERNETINTERFACESTATUS_METRIC_GROUP = "EthernetInterfaceStatusMeasures";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH0_COLLISIONS = "EthernetInterfaceStatus_eth0_Collisions";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH0_STATUS = "EthernetInterfaceStatus_eth0_Status";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH0_RXPACKETS = "EthernetInterfaceStatus_eth0_RxPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH0_RXKBYTES = "EthernetInterfaceStatus_eth0_RxKbytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH0_RXERRORS = "EthernetInterfaceStatus_eth0_RxErrors";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH0_RXDROPS = "EthernetInterfaceStatus_eth0_RxDrops";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH0_TXPACKETS = "EthernetInterfaceStatus_eth0_TxPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH0_TXKBYTES = "EthernetInterfaceStatus_eth0_TxKbytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH0_TXERRORS = "EthernetInterfaceStatus_eth0_TxErrors";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH0_TXDROPS = "EthernetInterfaceStatus_eth0_TxDrops";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH1_COLLISIONS = "EthernetInterfaceStatus_eth1_Collisions";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH1_STATUS = "EthernetInterfaceStatus_eth1_Status";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH1_RXPACKETS = "EthernetInterfaceStatus_eth1_RxPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH1_RXKBYTES = "EthernetInterfaceStatus_eth1_RxKbytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH1_RXERRORS = "EthernetInterfaceStatus_eth1_RxErrors";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH1_RXDROPS = "EthernetInterfaceStatus_eth1_RxDrops";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH1_TXPACKETS = "EthernetInterfaceStatus_eth1_TxPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH1_TXKBYTES = "EthernetInterfaceStatus_eth1_TxKbytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH1_TXERRORS = "EthernetInterfaceStatus_eth1_TxErrors";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH1_TXDROPS = "EthernetInterfaceStatus_eth1_TxDrops";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH2_COLLISIONS = "EthernetInterfaceStatus_eth2_Collisions";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH2_STATUS = "EthernetInterfaceStatus_eth2_Status";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH2_RXPACKETS = "EthernetInterfaceStatus_eth2_RxPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH2_RXKBYTES = "EthernetInterfaceStatus_eth2_RxKbytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH2_RXERRORS = "EthernetInterfaceStatus_eth2_RxErrors";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH2_RXDROPS = "EthernetInterfaceStatus_eth2_RxDrops";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH2_TXPACKETS = "EthernetInterfaceStatus_eth2_TxPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH2_TXKBYTES = "EthernetInterfaceStatus_eth2_TxKbytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH2_TXERRORS = "EthernetInterfaceStatus_eth2_TxErrors";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH2_TXDROPS = "EthernetInterfaceStatus_eth2_TxDrops";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT0_COLLISIONS = "EthernetInterfaceStatus_mgt0_Collisions";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT0_STATUS = "EthernetInterfaceStatus_mgt0_Status";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT0_RXPACKETS = "EthernetInterfaceStatus_mgt0_RxPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT0_RXKBYTES = "EthernetInterfaceStatus_mgt0_RxKbytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT0_RXERRORS = "EthernetInterfaceStatus_mgt0_RxErrors";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT0_RXDROPS = "EthernetInterfaceStatus_mgt0_RxDrops";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT0_TXPACKETS = "EthernetInterfaceStatus_mgt0_TxPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT0_TXKBYTES = "EthernetInterfaceStatus_mgt0_TxKbytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT0_TXERRORS = "EthernetInterfaceStatus_mgt0_TxErrors";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT0_TXDROPS = "EthernetInterfaceStatus_mgt0_TxDrops";

    private enum MatchContent {

        disabled, successIfMatch, errorIfMatch, bytesMatch
    }

    private static class Config {

        URL url;
        String method;
        String postData;
        String httpVersion;
        String userAgent;
        int maxRedirects;
        boolean tagging;
        boolean ignorecert;
        String timerName;
        boolean serverAuth;
        String serverUsername;
        String serverPassword;
        boolean useProxy;
        String proxyHost;
        int proxyPort;
        boolean proxyAuth;
        String proxyUsername;
        String proxyPassword;
        long compareBytes;
    }
    private DataPowerMonitor.Config config;
    private HttpClient httpClient;
    Status status;

    @Override
    public Status setup(MonitorEnvironment env) throws Exception {
        status = new Status(Status.StatusCode.Success);
        httpClient = new HttpClient(new SimpleHttpConnectionManager());

        /*
         * try { Protocol.getProtocol(PROTOCOL_HTTPS_IGNORECERT); } catch
         * (IllegalStateException e) { Protocol protocol = new
         * Protocol(PROTOCOL_HTTPS_IGNORECERT, new
         * EasySSLProtocolSocketFactory(), 443);
         * Protocol.registerProtocol(PROTOCOL_HTTPS_IGNORECERT, protocol); }
         */
        config = readConfig(env);
        return status;
    }

    @Override
    public void teardown(MonitorEnvironment env) throws Exception {
        HttpConnectionManager httpConnectionManager = httpClient.getHttpConnectionManager();
        if (httpConnectionManager instanceof SimpleHttpConnectionManager) {
            ((SimpleHttpConnectionManager) httpConnectionManager).closeIdleConnections(30000);
        }
    }

    @Override
    public Status execute(MonitorEnvironment env) throws Exception {
        status = new Status();

        status = executeConnectionsAccepted(env);
        status = executeAvailability(env);
        status = executeCPUUsage(env);
        status = executeDocumentCachingSummary(env);
        status = executeStylesheetCachingSummary(env);
        status = executeEnvironmentalSensors(env);
        status = executeHTTPConnections(env);
        status = executeMemoryStatus(env);
        status = executeFilesystemStatus(env);
        status = executeEthernetInterfaceStatus(env);

        return status;
    }

	private Status executeAvailability(MonitorEnvironment env) {
		//Go ahead and set availability measures after first SOMA call
        // measurement variables
        Collection<MonitorMeasure> hostReachableMeasures = env.getMonitorMeasures(AVAILABILITY_METRIC_GROUP, MSR_HOST_REACHABLE);
        if (status.getStatusCode().getBaseCode() == Status.StatusCode.Success.getBaseCode() && hostReachableMeasures != null) {
            for (MonitorMeasure measure : hostReachableMeasures) {
                measure.setValue(0);
            }
        }

        // calculate and set the measurements
        Collection<MonitorMeasure> measures = null;
        measures = env.getMonitorMeasures(AVAILABILITY_METRIC_GROUP, MSR_HOST_REACHABLE);
        log.log(Level.FINE, "Measure collection size MSR_HOST_REACHABLE is {0}", measures.size());
        if (status.getStatusCode().getBaseCode() == Status.StatusCode.Success.getBaseCode() && (measures = env.getMonitorMeasures(AVAILABILITY_METRIC_GROUP, MSR_HOST_REACHABLE)) != null) {
            for (MonitorMeasure measure : measures) {
                if (httpStatusCode == 503) {
                    measure.setValue(0);
                } else {
                    measure.setValue(httpStatusCode > 0 ? 1 : 0);
                }
            }
        }
        log.log(Level.FINE, "Measure collection size after httpstatuscode is {0}", measures.size());

        if (status.getStatusCode().getCode() == Status.StatusCode.Success.getCode()) {
            if ((measures = env.getMonitorMeasures(AVAILABILITY_METRIC_GROUP, MSR_HEADER_SIZE)) != null) {
                log.log(Level.FINE, "Measure collection size after httpstatuscode is {0}", measures.size());

                for (MonitorMeasure measure : measures) {
                    log.log(Level.FINE, "You made it into Header Size and Header Size is {0}", headerSize);
                    measure.setValue(headerSize);
                }

            }
            if ((measures = env.getMonitorMeasures(AVAILABILITY_METRIC_GROUP, MSR_FIRST_RESPONSE_DELAY)) != null) {
                double firstResponseTimeMillis = firstResponseTime * MILLIS;
                for (MonitorMeasure measure : measures) {
                    log.log(Level.FINE, "You made it into firstResponseTimeMillis is {0}", firstResponseTimeMillis);
                    measure.setValue(firstResponseTimeMillis);
                }

            }
            if ((measures = env.getMonitorMeasures(AVAILABILITY_METRIC_GROUP, MSR_RESPONSE_COMPLETE_TIME)) != null) {
                double responseCompleteTimeMillis = responseCompleteTime * MILLIS;
                for (MonitorMeasure measure : measures) {
                    log.log(Level.FINE, "You made it into responseCompleteTimeMillis is {0}", responseCompleteTimeMillis);
                    measure.setValue(responseCompleteTimeMillis);
                }

            }
            if ((measures = env.getMonitorMeasures(AVAILABILITY_METRIC_GROUP, MSR_RESPONSE_SIZE)) != null) {
                for (MonitorMeasure measure : measures) {
                    log.log(Level.FINE, "You made it into inputSize is {0}", inputSize);
                    measure.setValue(inputSize);
                }

            }
            if ((measures = env.getMonitorMeasures(AVAILABILITY_METRIC_GROUP, MSR_THROUGHPUT)) != null) {
                double throughput = 0;
                if (responseCompleteTime > 0) {
                    double responseCompleteTimeSecs = responseCompleteTime * SECS;
                    double contentSizeKibiByte = inputSize / 1024.0;
                    throughput = contentSizeKibiByte / responseCompleteTimeSecs;
                }
                for (MonitorMeasure measure : measures) {
                    log.log(Level.FINE, "You made it into throughput is {0}", throughput);
                    measure.setValue(throughput);
                }

            }
            if ((measures = env.getMonitorMeasures(AVAILABILITY_METRIC_GROUP, MSR_HTTP_STATUS_CODE)) != null) {
                for (MonitorMeasure measure : measures) {
                    log.log(Level.FINE, "You made it into httpStatusCode is {0}", httpStatusCode);
                    measure.setValue(httpStatusCode);
                }

            }
            if ((measures = env.getMonitorMeasures(AVAILABILITY_METRIC_GROUP, MSR_CONN_CLOSE_DELAY)) != null) {
                double connectionCloseDelayMillis = connectionCloseDelay * MILLIS;
                for (MonitorMeasure measure : measures) {
                    log.log(Level.FINE, "You made it into connectionCloseDelayMillis is {0}", connectionCloseDelayMillis);
                    measure.setValue(connectionCloseDelayMillis);
                }

            }
            if ((measures = env.getMonitorMeasures(AVAILABILITY_METRIC_GROUP, MSR_CONTENT_VERIFIED)) != null) {
                for (MonitorMeasure measure : measures) {
                    log.log(Level.FINE, "You made it into verified is {0}", verified);
                    measure.setValue(verified ? 1 : 0);
                }
            }
        }
        return status;
	}

    private DataPowerMonitor.Config readConfig(MonitorEnvironment env) throws MalformedURLException {
        DataPowerMonitor.Config config = new DataPowerMonitor.Config();

        String protocol = env.getConfigString(CONFIG_PROTOCOL);
        int port;
        if (protocol != null && protocol.contains("https")) {
            port = env.getConfigLong(CONFIG_HTTPS_PORT).intValue();
            protocol = PROTOCOL_HTTPS;
        } else {
            port = env.getConfigLong(CONFIG_HTTP_PORT).intValue();
            protocol = PROTOCOL_HTTP;
        }
        String path = fixPath(env.getConfigString(CONFIG_PATH));
        
        //config.url = new URL("http://localhost:9999/DataPowerSOAPSimulator/ConnectionsAcceptedServlet");


        config.method = "POST";
        


        config.postData = env.getConfigString(CONFIG_POST_DATA);
        config.httpVersion = env.getConfigString(CONFIG_HTTP_VERSION);
        config.userAgent = env.getConfigString(CONFIG_USER_AGENT);
        config.tagging = env.getConfigBoolean(CONFIG_DT_TAGGING) == null ? false : env.getConfigBoolean(CONFIG_DT_TAGGING);
        if (config.tagging) {
            config.timerName = env.getConfigString(CONFIG_DT_TIMER_NAME) == null ? "" : env.getConfigString(CONFIG_DT_TIMER_NAME);
        }
        config.maxRedirects = env.getConfigLong(CONFIG_MAX_REDIRECTS) == null ? 0 : env.getConfigLong(CONFIG_MAX_REDIRECTS).intValue();

        config.serverAuth = env.getConfigBoolean(CONFIG_SERVER_AUTH) == null ? false : env.getConfigBoolean(CONFIG_SERVER_AUTH);
        if (config.serverAuth) {
            config.serverUsername = env.getConfigString(CONFIG_SERVER_USERNAME);
            config.serverPassword = env.getConfigPassword(CONFIG_SERVER_PASSWORD);
        }

        config.useProxy = env.getConfigBoolean(CONFIG_USE_PROXY) == null ? false : env.getConfigBoolean(CONFIG_USE_PROXY);
        if (config.useProxy) {
            config.proxyHost = env.getConfigString(CONFIG_PROXY_HOST);
            config.proxyPort = env.getConfigLong(CONFIG_PROXY_PORT) == null ? 0 : env.getConfigLong(CONFIG_PROXY_PORT).intValue();
        }
        config.proxyAuth = env.getConfigBoolean(CONFIG_PROXY_AUTH) == null ? false : env.getConfigBoolean(CONFIG_PROXY_AUTH);
        if (config.proxyAuth) {
            config.proxyUsername = env.getConfigString(CONFIG_PROXY_USERNAME);
            config.proxyPassword = env.getConfigPassword(CONFIG_PROXY_PASSWORD);
        }
        return config;
    }

    private String fixPath(String path) {
        if (path == null) {
            return "/";
        }
        if (!path.startsWith("/")) {
            return "/" + path;
        }
        return path;
    }

    private HttpMethodBase createHttpMethod(DataPowerMonitor.Config config) {
        String url = config.url.toString();

        /*
         * if (config.ignorecert && url.startsWith(PROTOCOL_HTTPS)) { url =
         * PROTOCOL_HTTPS_IGNORECERT + url.substring(PROTOCOL_HTTPS.length()); }
         */

        HttpMethodBase httpMethod = null;
        if ("GET".equals(config.method)) {
            httpMethod = new GetMethod(url);
        } else if ("HEAD".equals(config.method)) {
            httpMethod = new HeadMethod(url);
        } else if ("POST".equals(config.method)) {
            httpMethod = new PostMethod(url); // set the POST data

            String authString = config.serverUsername + ":" + config.serverPassword;
            log.log(Level.FINE, "auth string: {0}", authString);
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            String authStringEnc = new String(authEncBytes);
            log.log(Level.FINE, "Base64 encoded auth string: {0}", authStringEnc);


            httpMethod.addRequestHeader("Authorization", "Basic " + authStringEnc);



            if (config.postData != null && config.postData.length() > 0) {
                try {
                    StringRequestEntity requestEntity = new StringRequestEntity(config.postData, "application/soap+xml", "UTF-8");
                    ((PostMethod) httpMethod).setRequestEntity(requestEntity);
                } catch (UnsupportedEncodingException uee) {
                    if (log.isLoggable(Level.WARNING)) {
                        log.log(Level.WARNING, "Encoding POST data failed: {0}", uee);
                    }
                }
            }
        }
        return httpMethod;
    }

    private void setHttpParameters(HttpMethodBase httpMethod, DataPowerMonitor.Config config) throws URIException, IllegalStateException {
        HttpVersion httpVersion = HttpVersion.HTTP_1_1;


        try {
            httpVersion = HttpVersion.parse("HTTP/" + config.httpVersion);
        } catch (Exception ex) {
            if (log.isLoggable(Level.WARNING)) {
                log.log(Level.WARNING, "Parsing httpVersion failed, using default: {0}", HttpVersion.HTTP_1_1);
            }
        }

        httpClient.getParams().setParameter(HttpClientParams.PROTOCOL_VERSION, httpVersion);
        httpClient.getParams().setParameter(HttpClientParams.USER_AGENT, config.userAgent);
        httpClient.getParams().setParameter(HttpClientParams.MAX_REDIRECTS, config.maxRedirects);



        // set server authentication credentials
        if (config.serverAuth) {

            URI uri = httpMethod.getURI();
            String host = uri.getHost();
            int port = uri.getPort();
            if (port <= 0) {
                Protocol protocol = Protocol.getProtocol(uri.getScheme());
                port = protocol.getDefaultPort();
            }
        }

        // set proxy and credentials
        if (config.useProxy) {
            httpClient.getHostConfiguration().setProxy(config.proxyHost, config.proxyPort);

            if (config.proxyAuth) {

                String authString = config.serverUsername + ":" + config.serverPassword;
                log.log(Level.FINE, "auth string: {0}", authString);
                byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
                String authStringEnc = new String(authEncBytes);
                log.log(Level.FINE, "Base64 encoded auth string: {0}", authStringEnc);
                //encryptedServerUsername = (String) myEncoder.encode(config.serverUsername);
                //encryptedServerPassword = (String) myEncoder.encode(config.serverPassword);
                UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(authStringEnc);


                httpClient.getState().setProxyCredentials(new AuthScope(config.proxyHost, config.proxyPort, AuthScope.ANY_REALM), credentials);
            }
        }

        // set dynaTrace tagging header (only timer name)
        if (config.tagging) {
            httpMethod.addRequestHeader(Constants.HEADER_DYNATRACE, "NA=" + config.timerName);
        }

        // use a custom retry handler
        HttpMethodRetryHandler retryHandler = new HttpMethodRetryHandler() {

            @Override
            public boolean retryMethod(final HttpMethod method, final IOException exception, int executionCount) {
                // we don't want to retry
                return false;
            }
        };
        httpMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, retryHandler);

        boolean followRedirects = true;
        if (config.maxRedirects > 0) {
            followRedirects = true;
        } else {
            followRedirects = false;
        }

        httpMethod.setFollowRedirects(followRedirects);

        //httpMethod.setFollowRedirects((config.maxRedirects > 0));
    }

    private int calculateHeaderSize(Header[] headers) {
        int headerLength = 0;
        for (Header header : headers) {
            headerLength += header.getName().getBytes().length;
            headerLength += header.getValue().getBytes().length;
        }
        return headerLength;
    }

    private Status executeConnectionsAccepted(MonitorEnvironment env) {

        Document doc = null;
        NodeList measureList = null;

        status = new Status();

        String connectionsAcceptedResponse = callDPSOMAMethod(env, "ConnectionsAccepted");

        log.log(Level.FINE, "The response string is {0}", connectionsAcceptedResponse);

        if (connectionsAcceptedResponse != null) {

            StringBuilder buf = new StringBuilder();

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = null;
            try {
                docBuilder = docBuilderFactory.newDocumentBuilder();
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }

            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(connectionsAcceptedResponse));
            try {
                try {
                    doc = docBuilder.parse(is);
                } catch (IOException ex) {
                    Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (SAXException ex) {
                Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }
            doc.getDocumentElement().normalize();
            measureList = doc.getElementsByTagName("ConnectionsAccepted");

            for (int i = 0; i < measureList.getLength(); i++) {
                Element element = (Element) measureList.item(i);
                
                addDoubleMeasure(env, element, CONNECTIONSACCEPTED_METRIC_GROUP, MSR_CONNECTIONSACCEPTED_TENSECONDS, "tenSeconds");
                addDoubleMeasure(env, element, CONNECTIONSACCEPTED_METRIC_GROUP, MSR_CONNECTIONSACCEPTED_ONEMINUTE, "oneMinute");
                addDoubleMeasure(env, element, CONNECTIONSACCEPTED_METRIC_GROUP, MSR_CONNECTIONSACCEPTED_TENMINUTES, "tenMinutes");
                addDoubleMeasure(env, element, CONNECTIONSACCEPTED_METRIC_GROUP, MSR_CONNECTIONSACCEPTED_ONEHOUR, "oneHour");
            	addDoubleMeasure(env, element, CONNECTIONSACCEPTED_METRIC_GROUP, MSR_CONNECTIONSACCEPTED_ONEDAY, "oneDay");

            }
        } else {
            status.setMessage("SOMA call unsuccessful: ConnectionsAccepted response is null");
            status.setStatusCode(Status.StatusCode.ErrorInternal);
        }

        return status;
    }

    private Status executeCPUUsage(MonitorEnvironment env) {

        Document doc = null;
        NodeList measureList = null;

        status = new Status();

        String CPUUsageResponse = callDPSOMAMethod(env, "CPUUsage");

        log.log(Level.FINE, "The response string is {0}", CPUUsageResponse);

        if (CPUUsageResponse != null) {

            StringBuilder buf = new StringBuilder();

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = null;
            try {
                docBuilder = docBuilderFactory.newDocumentBuilder();
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }

            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(CPUUsageResponse));
            try {
                try {
                    doc = docBuilder.parse(is);
                } catch (IOException ex) {
                    Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (SAXException ex) {
                Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }
            doc.getDocumentElement().normalize();

            measureList = doc.getElementsByTagName("CPUUsage");

            for (int i = 0; i < measureList.getLength(); i++) {
                Element element = (Element) measureList.item(i);

                addDoubleMeasure(env, element, CPUUSAGE_METRIC_GROUP, MSR_CPUUSAGE_TENSECONDS, "tenSeconds");
                addDoubleMeasure(env, element, CPUUSAGE_METRIC_GROUP, MSR_CPUUSAGE_ONEMINUTE, "oneMinute");
                addDoubleMeasure(env, element, CPUUSAGE_METRIC_GROUP, MSR_CPUUSAGE_TENMINUTES, "tenMinutes");
                addDoubleMeasure(env, element, CPUUSAGE_METRIC_GROUP, MSR_CPUUSAGE_ONEHOUR, "oneHour");
                addDoubleMeasure(env, element, CPUUSAGE_METRIC_GROUP, MSR_CPUUSAGE_ONEDAY, "oneDay");

            }
        } else {
            status.setMessage("SOMA call unsuccessful: CPUUsage response is null");
            status.setStatusCode(Status.StatusCode.ErrorInternal);
        }

        return status;
    }

    private Status executeDocumentCachingSummary(MonitorEnvironment env) {

        Document doc = null;
        NodeList measureList = null;

        status = new Status();

        String DocumentCachingSummaryResponse = callDPSOMAMethod(env, "DocumentCachingSummary");

        log.log(Level.FINE, "The response string is {0}", DocumentCachingSummaryResponse);

        if (DocumentCachingSummaryResponse != null) {

            StringBuilder buf = new StringBuilder();

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = null;
            try {
                docBuilder = docBuilderFactory.newDocumentBuilder();
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }

            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(DocumentCachingSummaryResponse));
            try {
                try {
                    doc = docBuilder.parse(is);
                } catch (IOException ex) {
                    Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (SAXException ex) {
                Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }
            doc.getDocumentElement().normalize();

            measureList = doc.getElementsByTagName("DocumentCachingSummary");

            for (int i = 0; i < measureList.getLength(); i++) {
                Element element = (Element) measureList.item(i);

                addDoubleMeasure(env, element, DOCUMENTCACHINGSUMMARY_METRIC_GROUP, MSR_DOCUMENTCACHINGSUMMARY_CACHECOUNT, "CacheCount");
                addDoubleMeasure(env, element, DOCUMENTCACHINGSUMMARY_METRIC_GROUP, MSR_DOCUMENTCACHINGSUMMARY_DOCCOUNT, "DocCount");
                addDoubleMeasure(env, element, DOCUMENTCACHINGSUMMARY_METRIC_GROUP, MSR_DOCUMENTCACHINGSUMMARY_CACHESIZE, "CacheSize");
                addDoubleMeasure(env, element, DOCUMENTCACHINGSUMMARY_METRIC_GROUP, MSR_DOCUMENTCACHINGSUMMARY_BYTECOUNT, "ByteCount");

            }
        } else {
            status.setMessage("SOMA call unsuccessful: DocumentCachingSummary response is null");
            status.setStatusCode(Status.StatusCode.ErrorInternal);
        }

        return status;
    }

    private Status executeStylesheetCachingSummary(MonitorEnvironment env) {

        Document doc = null;
        NodeList measureList = null;

        status = new Status();

        String StylesheetCachingSummaryResponse = callDPSOMAMethod(env, "StylesheetCachingSummary");

        log.log(Level.FINE, "The response string is {0}", StylesheetCachingSummaryResponse);

        if (StylesheetCachingSummaryResponse != null) {

            StringBuilder buf = new StringBuilder();

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = null;
            try {
                docBuilder = docBuilderFactory.newDocumentBuilder();
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }

            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(StylesheetCachingSummaryResponse));
            try {
                try {
                    doc = docBuilder.parse(is);
                } catch (IOException ex) {
                    Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (SAXException ex) {
                Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }
            doc.getDocumentElement().normalize();

            measureList = doc.getElementsByTagName("StylesheetCachingSummary");

            for (int i = 0; i < measureList.getLength(); i++) {
                Element element = (Element) measureList.item(i);

                addDoubleMeasure(env, element, STYLESHEETCACHINGSUMMARY_METRIC_GROUP, MSR_STYLESHEETCACHINGSUMMARY_CACHESIZE, "CacheSize");
                addDoubleMeasure(env, element, STYLESHEETCACHINGSUMMARY_METRIC_GROUP, MSR_STYLESHEETCACHINGSUMMARY_CACHECOUNT, "CacheCount");
                addDoubleMeasure(env, element, STYLESHEETCACHINGSUMMARY_METRIC_GROUP, MSR_STYLESHEETCACHINGSUMMARY_READYCOUNT, "ReadyCount");
                addDoubleMeasure(env, element, STYLESHEETCACHINGSUMMARY_METRIC_GROUP, MSR_STYLESHEETCACHINGSUMMARY_PENDINGCOUNT, "PendingCount");
                addDoubleMeasure(env, element, STYLESHEETCACHINGSUMMARY_METRIC_GROUP, MSR_STYLESHEETCACHINGSUMMARY_BADCOUNT, "BadCount");
                addDoubleMeasure(env, element, STYLESHEETCACHINGSUMMARY_METRIC_GROUP, MSR_STYLESHEETCACHINGSUMMARY_DUPCOUNT, "DupCount");
                addDoubleMeasure(env, element, STYLESHEETCACHINGSUMMARY_METRIC_GROUP, MSR_STYLESHEETCACHINGSUMMARY_CACHEKBCOUNT, "CacheKBCount");
            }
        } else {
            status.setMessage("SOMA call unsuccessful: StylesheetCachingSummary response is null");
            status.setStatusCode(Status.StatusCode.ErrorInternal);
        }

        return status;
    }

    private Status executeEnvironmentalSensors(MonitorEnvironment env) {

        Document doc = null;
        NodeList measureList = null;

        status = new Status();

        String EnvironmentalSensorsResponse = callDPSOMAMethod(env, "EnvironmentalSensors");

        log.log(Level.FINE, "The response string is {0}", EnvironmentalSensorsResponse);

        if (EnvironmentalSensorsResponse != null) {

            StringBuilder buf = new StringBuilder();

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = null;
            try {
                docBuilder = docBuilderFactory.newDocumentBuilder();
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }

            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(EnvironmentalSensorsResponse));
            try {
                try {
                    doc = docBuilder.parse(is);
                } catch (IOException ex) {
                    Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (SAXException ex) {
                Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }
            doc.getDocumentElement().normalize();

            measureList = doc.getElementsByTagName("EnvironmentalSensors");

            Collection<MonitorMeasure> measures = null;

            for (int i = 0; i < measureList.getLength(); i++) {
                Element element = (Element) measureList.item(i);

                addDoubleMeasure(env, element, ENVIRONMENTALSENSORS_METRIC_GROUP, MSR_ENVIRONMENTALSENSORS_SYSTEMTEMP, "systemTemp");
                addDoubleMeasure(env, element, ENVIRONMENTALSENSORS_METRIC_GROUP, MSR_ENVIRONMENTALSENSORS_CPU1TEMP, "cpu1Temp");
                addDoubleMeasure(env, element, ENVIRONMENTALSENSORS_METRIC_GROUP, MSR_ENVIRONMENTALSENSORS_CPU2TEMP, "cpu2Temp");
                addDoubleMeasure(env, element, ENVIRONMENTALSENSORS_METRIC_GROUP, MSR_ENVIRONMENTALSENSORS_CPU1RPM, "cpu1rpm");
                addDoubleMeasure(env, element, ENVIRONMENTALSENSORS_METRIC_GROUP, MSR_ENVIRONMENTALSENSORS_CPU2RPM, "cpu2rpm");
                addDoubleMeasure(env, element, ENVIRONMENTALSENSORS_METRIC_GROUP, MSR_ENVIRONMENTALSENSORS_CHASSIS1RPM, "chassis1rpm");
                addDoubleMeasure(env, element, ENVIRONMENTALSENSORS_METRIC_GROUP, MSR_ENVIRONMENTALSENSORS_CHASSIS2RPM, "chassis2rpm");
                addDoubleMeasure(env, element, ENVIRONMENTALSENSORS_METRIC_GROUP, MSR_ENVIRONMENTALSENSORS_CHASSIS3RPM, "chassis3rpm");


                NodeList caseopenlist = element.getElementsByTagName("caseopen");
                Element caseopen = (Element) caseopenlist.item(0);
                log.log(Level.FINE, "caseopen is {0}", caseopen.getTextContent());

                String caseopenString = caseopen.getTextContent();
                measures = env.getMonitorMeasures(ENVIRONMENTALSENSORS_METRIC_GROUP, MSR_ENVIRONMENTALSENSORS_CASEOPEN);
                log.log(Level.FINE, "Measure collection size ENVIRONMENTALSENSORS_METRIC_GROUP is {0}", measures.size());
                if (status.getStatusCode().getBaseCode() == Status.StatusCode.Success.getBaseCode() && (measures = env.getMonitorMeasures(ENVIRONMENTALSENSORS_METRIC_GROUP, MSR_ENVIRONMENTALSENSORS_CASEOPEN)) != null) {
                    for (MonitorMeasure measure : measures) {
                        log.log(Level.FINE, "You made it into caseopen is {0}", caseopenString);
                        int caseopenInt = 0;
                        if (caseopenString == null ? "yes" == null : caseopenString.equals("yes")) {
                            caseopenInt = 1;
                        } else {
                            caseopenInt = 0;
                        }
                        measure.setValue(caseopenInt);
                    }
                }
                              
                addDoubleMeasure(env, element, ENVIRONMENTALSENSORS_METRIC_GROUP, MSR_ENVIRONMENTALSENSORS_VOLT5, "volt5");
                addDoubleMeasure(env, element, ENVIRONMENTALSENSORS_METRIC_GROUP, MSR_ENVIRONMENTALSENSORS_VOLT12, "volt12");
                addDoubleMeasure(env, element, ENVIRONMENTALSENSORS_METRIC_GROUP, MSR_ENVIRONMENTALSENSORS_VOLT33, "volt33");

                
                NodeList powersupplyoklist = element.getElementsByTagName("powersupply");
                Element powersupplyok = (Element) powersupplyoklist.item(0);
                log.log(Level.FINE, "powersupplyok is {0}", powersupplyok.getTextContent());

                String powersupplyokString = powersupplyok.getTextContent();
                measures = env.getMonitorMeasures(ENVIRONMENTALSENSORS_METRIC_GROUP, MSR_ENVIRONMENTALSENSORS_POWERSUPPLYOK);
                log.log(Level.FINE, "Measure collection size ENVIRONMENTALSENSORS_METRIC_GROUP is {0}", measures.size());
                if (status.getStatusCode().getBaseCode() == Status.StatusCode.Success.getBaseCode() && (measures = env.getMonitorMeasures(ENVIRONMENTALSENSORS_METRIC_GROUP, MSR_ENVIRONMENTALSENSORS_POWERSUPPLYOK)) != null) {
                    for (MonitorMeasure measure : measures) {
                        log.log(Level.FINE, "You made it into powersupplyok is {0}", powersupplyokString);
                        int powersupplyokInt = 0;
                        if (powersupplyokString == null ? "ok" == null : powersupplyokString.equals("ok")) {
                            powersupplyokInt = 1;
                        } else {
                            powersupplyokInt = 0;
                        }
                        measure.setValue(powersupplyokInt);
                    }
                }


            }
        } else {
            status.setMessage("SOMA call unsuccessful: EnvironmentalSensors response is null");
            status.setStatusCode(Status.StatusCode.ErrorInternal);
        }

        return status;
    }

    private Status executeHTTPConnections(MonitorEnvironment env) {

        Document doc = null;
        NodeList measureList = null;

        status = new Status();

        String HTTPConnectionsResponse = callDPSOMAMethod(env, "HTTPConnections");

        log.log(Level.FINE, "The response string is {0}", HTTPConnectionsResponse);

        if (HTTPConnectionsResponse != null) {


            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = null;
            try {
                docBuilder = docBuilderFactory.newDocumentBuilder();
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }

            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(HTTPConnectionsResponse));
            try {
                try {
                    doc = docBuilder.parse(is);
                } catch (IOException ex) {
                    Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (SAXException ex) {
                Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }
            doc.getDocumentElement().normalize();

            measureList = doc.getElementsByTagName("HTTPConnections");


            for (int i = 0; i < measureList.getLength(); i++) {
                Element element = (Element) measureList.item(i);
                
                
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_REQTENSEC, "reqTenSec");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_REQONEMIN, "reqOneMin");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_REQTENMIN, "reqTenMin");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_REQONEHR, "reqOneHr");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_REQONEDAY, "reqOneDay");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_REUSETENSEC, "reuseTenSec");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_REUSEONEMIN, "reuseOneMin");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_REUSETENMIN, "reuseTenMin");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_REUSEONEHR, "reuseOneHr");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_REUSEONEDAY, "reuseOneDay");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_CREATETENSEC, "createTenSec");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_CREATEONEMIN, "createOneMin");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_CREATETENMIN, "createTenMin");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_CREATEONEHR, "createOneHr");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_CREATEONEDAY, "createOneDay");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_RETURNTENSEC, "returnTenSec");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_RETURNONEMIN, "returnOneMin");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_RETURNTENMIN, "returnTenMin");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_RETURNONEHR, "returnOneHr");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_RETURNONEDAY, "returnOneDay");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_OFFERTENSEC, "offerTenSec");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_OFFERONEMIN, "offerOneMin");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_OFFERTENMIN, "offerTenMin");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_OFFERONEHR, "offerOneHr");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_OFFERONEDAY, "offerOneDay");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_DESTROYTENSEC, "destroyTenSec");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_DESTROYONEMIN, "destroyOneMin");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_DESTROYTENMIN, "destroyTenMin");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_DESTROYONEHR, "destroyOneHr");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_DESTROYONEDAY, "destroyOneDay");

            }
        } else {
            status.setMessage("SOMA call unsuccessful: HTTPConnections response is null");
            status.setStatusCode(Status.StatusCode.ErrorInternal);
        }



        return status;
    }

    private Status executeMemoryStatus(MonitorEnvironment env) {

        Document doc = null;
        NodeList measureList = null;

        status = new Status();

        String MemoryStatusResponse = callDPSOMAMethod(env, "MemoryStatus");

        log.log(Level.FINE, "The response string is {0}", MemoryStatusResponse);

        if (MemoryStatusResponse != null) {

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = null;
            try {
                docBuilder = docBuilderFactory.newDocumentBuilder();
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }

            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(MemoryStatusResponse));
            try {
                try {
                    doc = docBuilder.parse(is);
                } catch (IOException ex) {
                    Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (SAXException ex) {
                Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }
            doc.getDocumentElement().normalize();

            measureList = doc.getElementsByTagName("MemoryStatus");

            Collection<MonitorMeasure> measures = null;

            for (int i = 0; i < measureList.getLength(); i++) {
                Element element = (Element) measureList.item(i);

                addDoubleMeasure(env, element, MEMORYSTATUS_METRIC_GROUP, MSR_MEMORYSTATUS_TOTALMEMORY, "TotalMemory");
                addDoubleMeasure(env, element, MEMORYSTATUS_METRIC_GROUP, MSR_MEMORYSTATUS_USEDMEMORY, "UsedMemory");
                addDoubleMeasure(env, element, MEMORYSTATUS_METRIC_GROUP, MSR_MEMORYSTATUS_FREEMEMORY, "FreeMemory");
                addDoubleMeasure(env, element, MEMORYSTATUS_METRIC_GROUP, MSR_MEMORYSTATUS_REQMEMORY, "ReqMemory");
                addDoubleMeasure(env, element, MEMORYSTATUS_METRIC_GROUP, MSR_MEMORYSTATUS_HOLDMEMORY, "HoldMemory");
                addDoubleMeasure(env, element, MEMORYSTATUS_METRIC_GROUP, MSR_MEMORYSTATUS_USAGE, "Usage");

            }
        } else {
            status.setMessage("SOMA call unsuccessful: MemoryStatus response is null");
            status.setStatusCode(Status.StatusCode.ErrorInternal);
        }

        return status;
    }

    private Status executeFilesystemStatus(MonitorEnvironment env) {

        Document doc = null;
        NodeList measureList = null;

        status = new Status();

        String FilesystemStatusResponse = callDPSOMAMethod(env, "FilesystemStatus");

        log.log(Level.FINE, "The response string is {0}", FilesystemStatusResponse);

        if (FilesystemStatusResponse != null) {


            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = null;
            try {
                docBuilder = docBuilderFactory.newDocumentBuilder();
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }

            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(FilesystemStatusResponse));
            try {
                try {
                    doc = docBuilder.parse(is);
                } catch (IOException ex) {
                    Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (SAXException ex) {
                Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }
            doc.getDocumentElement().normalize();

            measureList = doc.getElementsByTagName("FilesystemStatus");

            for (int i = 0; i < measureList.getLength(); i++) {
                Element element = (Element) measureList.item(i);

                addDoubleMeasure(env, element, FILESYSTEMSTATUS_METRIC_GROUP, MSR_FILESYSTEMSTATUS_FREEENCRYPTED, "FreeEncrypted");
                addDoubleMeasure(env, element, FILESYSTEMSTATUS_METRIC_GROUP, MSR_FILESYSTEMSTATUS_TOTALENCRYPTED, "TotalEncrypted");
                addDoubleMeasure(env, element, FILESYSTEMSTATUS_METRIC_GROUP, MSR_FILESYSTEMSTATUS_FREETEMPORARY, "FreeTemporary");
                addDoubleMeasure(env, element, FILESYSTEMSTATUS_METRIC_GROUP, MSR_FILESYSTEMSTATUS_TOTALTEMPORARY, "TotalTemporary");
                addDoubleMeasure(env, element, FILESYSTEMSTATUS_METRIC_GROUP, MSR_FILESYSTEMSTATUS_FREEINTERNAL, "FreeInternal");
                addDoubleMeasure(env, element, FILESYSTEMSTATUS_METRIC_GROUP, MSR_FILESYSTEMSTATUS_TOTALINTERNAL, "TotalInternal");


            }
        } else {
            status.setMessage("SOMA call unsuccessful: FilesystemStatus response is null");
            status.setStatusCode(Status.StatusCode.ErrorInternal);
        }

        return status;
    }

    private Status executeEthernetInterfaceStatus(MonitorEnvironment env) {

        Document doc = null;
        NodeList measureList = null;

        status = new Status();

        String EthernetInterfaceStatusResponse = callDPSOMAMethod(env, "EthernetInterfaceStatus");

        log.log(Level.FINE, "The response string is {0}", EthernetInterfaceStatusResponse);

        if (EthernetInterfaceStatusResponse != null) {


            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = null;
            try {
                docBuilder = docBuilderFactory.newDocumentBuilder();
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }

            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(EthernetInterfaceStatusResponse));
            try {
                try {
                    doc = docBuilder.parse(is);
                } catch (IOException ex) {
                    Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (SAXException ex) {
                Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }
            doc.getDocumentElement().normalize();

            measureList = doc.getElementsByTagName("EthernetInterfaceStatus");

            Collection<MonitorMeasure> measures = null;

            for (int i = 0; i < measureList.getLength(); i++) {
                Element element = (Element) measureList.item(i);

                NodeList Namelist = element.getElementsByTagName("Name");
                Element Name = (Element) Namelist.item(0);
                String NameString = Name.getTextContent();
                log.log(Level.FINE, "Name is {0}", Name.getTextContent());

                if (NameString.equals("eth0")) {

                    NodeList Statusoklist = element.getElementsByTagName("Status");
                    Element Statusok = (Element) Statusoklist.item(0);
                    log.log(Level.FINE, "Statusok is {0}", Statusok.getTextContent());

                    String StatusokString = Statusok.getTextContent();



                    measures = env.getMonitorMeasures(ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_ETH0_STATUS);
                    log.log(Level.FINE, "Measure collection size ETHERNETINTERFACESTATUS_METRIC_GROUP is {0}", measures.size());
                    if (status.getStatusCode().getBaseCode() == Status.StatusCode.Success.getBaseCode() && (measures = env.getMonitorMeasures(ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_ETH0_STATUS)) != null) {
                        for (MonitorMeasure measure : measures) {
                            log.log(Level.FINE, "You made it into Statusok is {0}", StatusokString);
                            int StatusokInt = 0;
                            if (StatusokString == null ? "ok" == null : StatusokString.equals("ok")) {
                                StatusokInt = 1;
                            } else {
                                StatusokInt = 0;
                            }
                            measure.setValue(StatusokInt);
                        }
                    }

                    
                    addDoubleMeasure(env, element, ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_ETH0_RXKBYTES, "RxKbytes");
                    addDoubleMeasure(env, element, ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_ETH0_RXPACKETS, "RxPackets");
                    addDoubleMeasure(env, element, ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_ETH0_RXERRORS, "RxErrors");
                    addDoubleMeasure(env, element, ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_ETH0_RXDROPS, "RxDrops");
                    addDoubleMeasure(env, element, ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_ETH0_TXKBYTES, "TxKbytes");
                    addDoubleMeasure(env, element, ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_ETH0_TXPACKETS, "TxPackets");
                    addDoubleMeasure(env, element, ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_ETH0_TXERRORS, "TxErrors");
                    addDoubleMeasure(env, element, ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_ETH0_TXDROPS, "TxDrops");
                    addDoubleMeasure(env, element, ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_ETH0_COLLISIONS, "Collisions");

                }


                if (NameString.equals("eth1")) {

                    NodeList Statusoklist = element.getElementsByTagName("Status");
                    Element Statusok = (Element) Statusoklist.item(0);
                    log.log(Level.FINE, "Statusok is {0}", Statusok.getTextContent());

                    String StatusokString = Statusok.getTextContent();



                    measures = env.getMonitorMeasures(ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_ETH1_STATUS);
                    log.log(Level.FINE, "Measure collection size ETHERNETINTERFACESTATUS_METRIC_GROUP is {0}", measures.size());
                    if (status.getStatusCode().getBaseCode() == Status.StatusCode.Success.getBaseCode() && (measures = env.getMonitorMeasures(ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_ETH1_STATUS)) != null) {
                        for (MonitorMeasure measure : measures) {
                            log.log(Level.FINE, "You made it into Statusok is {0}", StatusokString);
                            int StatusokInt = 0;
                            if (StatusokString == null ? "ok" == null : StatusokString.equals("ok")) {
                                StatusokInt = 1;
                            } else {
                                StatusokInt = 0;
                            }
                            measure.setValue(StatusokInt);
                        }
                    }

                    addDoubleMeasure(env, element, ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_ETH1_RXKBYTES, "RxKbytes");
                    addDoubleMeasure(env, element, ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_ETH1_RXPACKETS, "RxPackets");
                    addDoubleMeasure(env, element, ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_ETH1_RXERRORS, "RxErrors");
                    addDoubleMeasure(env, element, ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_ETH1_RXDROPS, "RxDrops");
                    addDoubleMeasure(env, element, ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_ETH1_TXKBYTES, "TxKbytes");
                    addDoubleMeasure(env, element, ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_ETH1_TXPACKETS, "TxPackets");
                    addDoubleMeasure(env, element, ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_ETH1_TXERRORS, "TxErrors");
                    addDoubleMeasure(env, element, ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_ETH1_TXDROPS, "TxDrops");
                    addDoubleMeasure(env, element, ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_ETH1_COLLISIONS, "Collisions");



                }

                if (NameString.equals("eth2")) {

                    NodeList Statusoklist = element.getElementsByTagName("Status");
                    Element Statusok = (Element) Statusoklist.item(0);
                    log.log(Level.FINE, "Statusok is {0}", Statusok.getTextContent());

                    String StatusokString = Statusok.getTextContent();



                    measures = env.getMonitorMeasures(ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_ETH2_STATUS);
                    log.log(Level.FINE, "Measure collection size ETHERNETINTERFACESTATUS_METRIC_GROUP is {0}", measures.size());
                    if (status.getStatusCode().getBaseCode() == Status.StatusCode.Success.getBaseCode() && (measures = env.getMonitorMeasures(ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_ETH2_STATUS)) != null) {
                        for (MonitorMeasure measure : measures) {
                            log.log(Level.FINE, "You made it into Statusok is {0}", StatusokString);
                            int StatusokInt = 0;
                            if (StatusokString == null ? "ok" == null : StatusokString.equals("ok")) {
                                StatusokInt = 1;
                            } else {
                                StatusokInt = 0;
                            }
                            measure.setValue(StatusokInt);
                        }
                    }

                    addDoubleMeasure(env, element, ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_ETH2_RXKBYTES, "RxKbytes");
                    addDoubleMeasure(env, element, ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_ETH2_RXPACKETS, "RxPackets");
                    addDoubleMeasure(env, element, ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_ETH2_RXERRORS, "RxErrors");
                    addDoubleMeasure(env, element, ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_ETH2_RXDROPS, "RxDrops");
                    addDoubleMeasure(env, element, ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_ETH2_TXKBYTES, "TxKbytes");
                    addDoubleMeasure(env, element, ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_ETH2_TXPACKETS, "TxPackets");
                    addDoubleMeasure(env, element, ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_ETH2_TXERRORS, "TxErrors");
                    addDoubleMeasure(env, element, ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_ETH2_TXDROPS, "TxDrops");
                    addDoubleMeasure(env, element, ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_ETH2_COLLISIONS, "Collisions");


                }

                if (NameString.equals("mgt0")) {

                    NodeList Statusoklist = element.getElementsByTagName("Status");
                    Element Statusok = (Element) Statusoklist.item(0);
                    log.log(Level.FINE, "Statusok is {0}", Statusok.getTextContent());

                    String StatusokString = Statusok.getTextContent();



                    measures = env.getMonitorMeasures(ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_MGT0_STATUS);
                    log.log(Level.FINE, "Measure collection size ETHERNETINTERFACESTATUS_METRIC_GROUP is {0}", measures.size());
                    if (status.getStatusCode().getBaseCode() == Status.StatusCode.Success.getBaseCode() && (measures = env.getMonitorMeasures(ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_MGT0_STATUS)) != null) {
                        for (MonitorMeasure measure : measures) {
                            log.log(Level.FINE, "You made it into Statusok is {0}", StatusokString);
                            int StatusokInt = 0;
                            if (StatusokString == null ? "ok" == null : StatusokString.equals("ok")) {
                                StatusokInt = 1;
                            } else {
                                StatusokInt = 0;
                            }
                            measure.setValue(StatusokInt);
                        }
                    }

                    addDoubleMeasure(env, element, ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_MGT0_RXKBYTES, "RxKbytes");
                    addDoubleMeasure(env, element, ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_MGT0_RXPACKETS, "RxPackets");
                    addDoubleMeasure(env, element, ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_MGT0_RXERRORS, "RxErrors");
                    addDoubleMeasure(env, element, ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_MGT0_RXDROPS, "RxDrops");
                    addDoubleMeasure(env, element, ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_MGT0_TXKBYTES, "TxKbytes");
                    addDoubleMeasure(env, element, ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_MGT0_TXPACKETS, "TxPackets");
                    addDoubleMeasure(env, element, ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_MGT0_TXERRORS, "TxErrors");
                    addDoubleMeasure(env, element, ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_MGT0_TXDROPS, "TxDrops");
                    addDoubleMeasure(env, element, ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_MGT0_COLLISIONS, "Collisions");
                }

            }
        } else {
            status.setMessage("SOMA call unsuccessful: EthernetInterfaceStatus response is null");
            status.setStatusCode(Status.StatusCode.ErrorInternal);
        }

        return status;
    }

    @Override
    public void migrate(PropertyContainer properties, int major, int minor, int micro, String qualifier) {
        //JLT-41859: change protocol value from http:// and https:// to http and https
        Property prop = properties.getProperty(CONFIG_PROTOCOL);

        if (prop != null) {
            if (prop.getValue() != null && prop.getValue().contains("https")) {
                prop.setValue("https");




            } else {
                prop.setValue("http");


            }
        }
    }

    public String callDPSOMAMethod(MonitorEnvironment env, String SOMAMethod) {


        HttpConnection con = null;
        String response = null;

        status = new Status();

        String protocol = env.getConfigString(CONFIG_PROTOCOL);
        int port;
        if (protocol != null && protocol.contains("https")) {
            port = env.getConfigLong(CONFIG_HTTPS_PORT).intValue();
            protocol = PROTOCOL_HTTPS;
        } else {
            port = env.getConfigLong(CONFIG_HTTP_PORT).intValue();
            protocol = PROTOCOL_HTTP;
        }


        String path = fixPath(env.getConfigString(CONFIG_PATH));

        try {

            config.url = new URL(protocol, env.getHost().getAddress(), port, path);
        } catch (MalformedURLException ex) {
            Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }


        if (config.url == null || (!config.url.getProtocol().equals("http") && !config.url.getProtocol().equals("https"))) {
            status.setShortMessage("only protocols http and https are allowed.");
            status.setMessage("only protocols http and https are allowed.");
            status.setStatusCode(Status.StatusCode.PartialSuccess);

        }

        // create a HTTP client and method
        HttpMethodBase httpMethod = createHttpMethod(config);
        if (httpMethod == null) {
            status.setMessage("Unknown HTTP method: " + config.method);
            status.setStatusCode(Status.StatusCode.ErrorInternal);

        }

        try {
            con = new HttpConnection(env.getHost().getAddress(), port, new Protocol("easyhttps", (ProtocolSocketFactory) new EasySSLProtocolSocketFactory(), port));
        } catch (Exception ex) {
            status.setStatusCode(Status.StatusCode.ErrorInternal);
            status.setMessage("Unable to create HttpConnection ...");
            status.setShortMessage(ex == null ? "" : ex.getClass().getSimpleName());
            status.setMessage(ex == null ? "" : ex.getMessage());
            status.setException(ex);
        }



        // try to set parameters
        try {
            setHttpParameters(httpMethod, config);
        } catch (Exception ex) {
            status.setStatusCode(Status.StatusCode.ErrorInternal);
            status.setMessage("Setting HTTP client parameters failed");
            status.setShortMessage(ex == null ? "" : ex.getClass().getSimpleName());
            status.setMessage(ex == null ? "" : ex.getMessage());
            status.setException(ex);

        }

        StringBuilder messageBuffer = new StringBuilder("URL: ");
        messageBuffer.append(config.url).append("\r\n");

        String mergedSOAPEnvelope;
        String SOAPEnvelopeTemplate = config.postData;

        mergedSOAPEnvelope = SOAPEnvelopeTemplate.replaceAll("@SOMAMONITORCLASS@", SOMAMethod);

        if (log.isLoggable(Level.INFO)) {
            try {
                log.log(Level.INFO, "Executing method: {0}, URI: {1}, SOAP Envelope: {2}", new Object[]{config.method, httpMethod.getURI(), mergedSOAPEnvelope});
            } catch (URIException ex) {
                Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // connect
        time = System.nanoTime();
        try {
            String authString = config.serverUsername + ":" + config.serverPassword;
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            String authStringEnc = new String(authEncBytes);
            httpMethod.addRequestHeader("Authorization", "Basic " + authStringEnc);

            StringRequestEntity requestEntity = new StringRequestEntity(mergedSOAPEnvelope, "application/soap+xml", "UTF-8");
            ((PostMethod) httpMethod).setRequestEntity(requestEntity);
            HttpState myState = new HttpState();
            con.open();

            httpStatusCode = httpMethod.execute(myState, con);
            firstResponseTime = System.nanoTime() - time;
            
            if (httpStatusCode < 200 || httpStatusCode > 200) {
                status.setMessage("SOMA call unsuccessful: " + SOMAMethod);
                status.setStatusCode(Status.StatusCode.ErrorInternal);
            } else {
                status.setStatusCode(Status.StatusCode.Success);
            }

        } catch (IOException ex) {
            Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
            log.log(Level.INFO, "Setting response code to 503 due to IOException: ", ex);
            httpStatusCode = 503;
            status.setMessage("SOMA call unsuccessful: " + SOMAMethod);
            status.setStatusCode(Status.StatusCode.ErrorInternal);
        }

        firstResponseTime = System.nanoTime() - time;

        // calculate header size
        headerSize = calculateHeaderSize(httpMethod.getResponseHeaders());
        try {
            response = httpMethod.getResponseBodyAsString();
            responseCompleteTime = System.nanoTime() - time;
        } catch (IOException ex) {
            status.setMessage("SOMA call unsuccessful: " + SOMAMethod);
            status.setStatusCode(Status.StatusCode.ErrorInternal);
            Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        inputSize = mergedSOAPEnvelope.length();

        con.close();

        return (response);

    }
    
    private void addDoubleMeasure(MonitorEnvironment env, Element element, String metricGroup, String counter, String tagName) {
    	status = new Status();
    	Collection<MonitorMeasure> measures = null;
    	Double tagDBL = new Double(0);
    	Element tag = null;
    	try {
    		NodeList Taglist = element.getElementsByTagName(tagName);
    		tag = (Element) Taglist.item(0);
    		log.log(Level.FINE, tag + " is {0}", tag.getTextContent());
    		tagDBL = Double.parseDouble(tag.getTextContent());
    	} catch (Exception e) {
    		e.printStackTrace();
    	}


    	measures = env.getMonitorMeasures(metricGroup, counter);
    	log.log(Level.FINE, "Measure collection size” + MetricGroup +” is {0}", measures.size());
    	if ((measures = env.getMonitorMeasures(metricGroup, counter)) != null) {
    		for (MonitorMeasure measure : measures) {
    			log.log(Level.FINE, "You made it into “ + Counter + “ is {0}", tag);
    			measure.setValue(tagDBL);
    		}
    	}    	
    }
}