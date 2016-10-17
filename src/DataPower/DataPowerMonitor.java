package DataPower;
//Version 9001

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
import java.util.Arrays;

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
    private static final double MILLIS = 0.000001;
    private static final double SECS = 0.000000001;
    private static final String FIRMWARE_7 = "7";
    // configuration constants
    private static final String CONFIG_PROTOCOL = "protocol";
    private static final String CONFIG_PATH = "path";
    private static final String CONFIG_HTTP_PORT = "httpPort";
    private static final String CONFIG_HTTPS_PORT = "httpsPort";
    private static final String CONFIG_POST_DATA = "postData";
    private static final String CONFIG_USER_AGENT = "userAgent";
    private static final String CONFIG_HTTP_VERSION = "httpVersion";
    private static final String CONFIG_MAX_REDIRECTS = "maxRedirects";
    private static final String CONFIG_DT_TAGGING = "dtTagging";
    private static final String CONFIG_DT_TIMER_NAME = "dtTimerName";

    private static final String CONFIG_SERVER_AUTH = "serverAuth";
    private static final String CONFIG_SERVER_USERNAME = "serverUsername";
    private static final String CONFIG_SERVER_PASSWORD = "serverPassword";
    private static final String CONFIG_USE_PROXY = "useProxy";
    private static final String CONFIG_PROXY_HOST = "proxyHost";
    private static final String CONFIG_PROXY_PORT = "proxyPort";
    private static final String CONFIG_PROXY_AUTH = "proxyAuth";
    private static final String CONFIG_PROXY_USERNAME = "proxyUsername";
    private static final String CONFIG_PROXY_PASSWORD = "proxyPassword";
    private static final String CONFIG_FIRMWARE_VERSION = "firmwareVersion";
    // measure constants
    private static final String AVAILABILITY_METRIC_GROUP = "DataPowerAvailabilityMeasures";
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
    private static final String CONNECTIONSACCEPTED_METRIC_GROUP = "DataPowerConnectionsAcceptedMeasures";
    private static final String MSR_CONNECTIONSACCEPTED_ONEDAY = "ConnectionsAccepted_oneDay";
    private static final String MSR_CONNECTIONSACCEPTED_ONEHOUR = "ConnectionsAccepted_oneHour";
    private static final String MSR_CONNECTIONSACCEPTED_ONEMINUTE = "ConnectionsAccepted_oneMinute";
    private static final String MSR_CONNECTIONSACCEPTED_TENMINUTES = "ConnectionsAccepted_tenMinutes";
    private static final String MSR_CONNECTIONSACCEPTED_TENSECONDS = "ConnectionsAccepted_tenSeconds";
    private static final String CPUUSAGE_METRIC_GROUP = "DataPowerCPUUsageMeasures";
    private static final String MSR_CPUUSAGE_ONEDAY = "CPUUsage_oneDay";
    private static final String MSR_CPUUSAGE_ONEHOUR = "CPUUsage_oneHour";
    private static final String MSR_CPUUSAGE_ONEMINUTE = "CPUUsage_oneMinute";
    private static final String MSR_CPUUSAGE_TENMINUTES = "CPUUsage_tenMinutes";
    private static final String MSR_CPUUSAGE_TENSECONDS = "CPUUsage_tenSeconds";
    private static final String DOCUMENTCACHINGSUMMARY_METRIC_GROUP = "DataPowerDocumentCachingSummaryMeasures";
    private static final String MSR_DOCUMENTCACHINGSUMMARY_CACHECOUNT = "DocumentCachingSummary_CacheCount";
    private static final String MSR_DOCUMENTCACHINGSUMMARY_DOCCOUNT = "DocumentCachingSummary_DocCount";
    private static final String MSR_DOCUMENTCACHINGSUMMARY_CACHESIZE = "DocumentCachingSummary_CacheSize";
    private static final String MSR_DOCUMENTCACHINGSUMMARY_BYTECOUNT = "DocumentCachingSummary_ByteCount";
    private static final String STYLESHEETCACHINGSUMMARY_METRIC_GROUP = "DataPowerStylesheetCachingSummaryMeasures";
    private static final String MSR_STYLESHEETCACHINGSUMMARY_CACHECOUNT = "StylesheetCachingSummary_CacheCount";
    private static final String MSR_STYLESHEETCACHINGSUMMARY_READYCOUNT = "StylesheetCachingSummary_ReadyCount";
    private static final String MSR_STYLESHEETCACHINGSUMMARY_CACHESIZE = "StylesheetCachingSummary_CacheSize";
    private static final String MSR_STYLESHEETCACHINGSUMMARY_PENDINGCOUNT = "StylesheetCachingSummary_PendingCount";
    private static final String MSR_STYLESHEETCACHINGSUMMARY_BADCOUNT = "StylesheetCachingSummary_BadCount";
    private static final String MSR_STYLESHEETCACHINGSUMMARY_DUPCOUNT = "StylesheetCachingSummary_DupCount";
    private static final String MSR_STYLESHEETCACHINGSUMMARY_CACHEKBCOUNT = "StylesheetCachingSummary_CacheKBCount";
    private static final String ENVIRONMENTALSENSORS_METRIC_GROUP = "DataPowerEnvironmentalSensorsMeasures";
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
    private static final String HTTPCONNECTIONS_METRIC_GROUP = "DataPowerHTTPConnectionsMeasures";
    private static final String MSR_HTTPCONNECTIONS_REQTENSEC = "HTTPConnections_ReqTenSec";
    private static final String MSR_HTTPCONNECTIONS_REQSEC = "HTTPConnections_ReqSec";
    private static final String MSR_HTTPCONNECTIONS_REQONEMIN = "HTTPConnections_ReqOneMin";
    private static final String MSR_HTTPCONNECTIONS_REQTENMIN = "HTTPConnections_ReqTenMin";
    private static final String MSR_HTTPCONNECTIONS_REQONEHR = "HTTPConnections_ReqOneHr";
    private static final String MSR_HTTPCONNECTIONS_REQONEDAY = "HTTPConnections_ReqOneDay";
    private static final String MSR_HTTPCONNECTIONS_REUSETENSEC = "HTTPConnections_ReuseTenSec";
    private static final String MSR_HTTPCONNECTIONS_REUSESEC = "HTTPConnections_ReuseSec";
    private static final String MSR_HTTPCONNECTIONS_REUSEONEMIN = "HTTPConnections_ReuseOneMin";
    private static final String MSR_HTTPCONNECTIONS_REUSETENMIN = "HTTPConnections_ReuseTenMin";
    private static final String MSR_HTTPCONNECTIONS_REUSEONEHR = "HTTPConnections_ReuseOneHr";
    private static final String MSR_HTTPCONNECTIONS_REUSEONEDAY = "HTTPConnections_ReuseOneDay";
    private static final String MSR_HTTPCONNECTIONS_CREATETENSEC = "HTTPConnections_CreateTenSec";
    private static final String MSR_HTTPCONNECTIONS_CREATESEC = "HTTPConnections_CreateSec";
    private static final String MSR_HTTPCONNECTIONS_CREATEONEMIN = "HTTPConnections_CreateOneMin";
    private static final String MSR_HTTPCONNECTIONS_CREATETENMIN = "HTTPConnections_CreateTenMin";
    private static final String MSR_HTTPCONNECTIONS_CREATEONEHR = "HTTPConnections_CreateOneHr";
    private static final String MSR_HTTPCONNECTIONS_CREATEONEDAY = "HTTPConnections_CreateOneDay";
    private static final String MSR_HTTPCONNECTIONS_RETURNTENSEC = "HTTPConnections_ReturnTenSec";
    private static final String MSR_HTTPCONNECTIONS_RETURNSEC = "HTTPConnections_ReturnSec";
    private static final String MSR_HTTPCONNECTIONS_RETURNONEMIN = "HTTPConnections_ReturnOneMin";
    private static final String MSR_HTTPCONNECTIONS_RETURNTENMIN = "HTTPConnections_ReturnTenMin";
    private static final String MSR_HTTPCONNECTIONS_RETURNONEHR = "HTTPConnections_ReturnOneHr";
    private static final String MSR_HTTPCONNECTIONS_RETURNONEDAY = "HTTPConnections_ReturnOneDay";
    private static final String MSR_HTTPCONNECTIONS_OFFERTENSEC = "HTTPConnections_OfferTenSec";
    private static final String MSR_HTTPCONNECTIONS_OFFERSEC = "HTTPConnections_OfferSec";
    private static final String MSR_HTTPCONNECTIONS_OFFERONEMIN = "HTTPConnections_OfferOneMin";
    private static final String MSR_HTTPCONNECTIONS_OFFERTENMIN = "HTTPConnections_OfferTenMin";
    private static final String MSR_HTTPCONNECTIONS_OFFERONEHR = "HTTPConnections_OfferOneHr";
    private static final String MSR_HTTPCONNECTIONS_OFFERONEDAY = "HTTPConnections_OfferOneDay";
    private static final String MSR_HTTPCONNECTIONS_DESTROYTENSEC = "HTTPConnections_DestroyTenSec";
    private static final String MSR_HTTPCONNECTIONS_DESTROYSEC = "HTTPConnections_DestroySec";
    private static final String MSR_HTTPCONNECTIONS_DESTROYONEMIN = "HTTPConnections_DestroyOneMin";
    private static final String MSR_HTTPCONNECTIONS_DESTROYTENMIN = "HTTPConnections_DestroyTenMin";
    private static final String MSR_HTTPCONNECTIONS_DESTROYONEHR = "HTTPConnections_DestroyOneHr";
    private static final String MSR_HTTPCONNECTIONS_DESTROYONEDAY = "HTTPConnections_DestroyOneDay";
    private static final String MEMORYSTATUS_METRIC_GROUP = "DataPowerMemoryStatusMeasures";
    private static final String MSR_MEMORYSTATUS_FREEMEMORY = "MemoryStatus_FreeMemory";
    private static final String MSR_MEMORYSTATUS_HOLDMEMORY = "MemoryStatus_HoldMemory";
    private static final String MSR_MEMORYSTATUS_REQMEMORY = "MemoryStatus_ReqMemory";
    private static final String MSR_MEMORYSTATUS_TOTALMEMORY = "MemoryStatus_TotalMemory";
    private static final String MSR_MEMORYSTATUS_USAGE = "MemoryStatus_Usage";
    private static final String MSR_MEMORYSTATUS_USEDMEMORY = "MemoryStatus_UsedMemory";
    private static final String FILESYSTEMSTATUS_METRIC_GROUP = "DataPowerFileSystemStatusMeasures";
    private static final String MSR_FILESYSTEMSTATUS_FREEENCRYPTED = "FileSystemStatus_FreeEncrypted";
    private static final String MSR_FILESYSTEMSTATUS_TOTALENCRYPTED = "FileSystemStatus_TotalEncrypted";
    private static final String MSR_FILESYSTEMSTATUS_FREETEMPORARY = "FileSystemStatus_FreeTemporary";
    private static final String MSR_FILESYSTEMSTATUS_TOTALTEMPORARY = "FileSystemStatus_TotalTemporary";
    private static final String MSR_FILESYSTEMSTATUS_FREEINTERNAL = "FileSystemStatus_FreeInternal";
    private static final String MSR_FILESYSTEMSTATUS_TOTALINTERNAL = "FileSystemStatus_TotalInternal";

    private static final String WSOPERATIONMETRICSSIMPLEINDEX_METRIC_GROUP = "DataPowerWSOperationMetricsSimpleIndexMeasures";

    private String[] SERVICE_METRICS = {"WSOperationMetricsSimpleIndex_NumberOfRequests", "WSOperationMetricsSimpleIndex_NumberOfFailedRequests", "WSOperationMetricsSimpleIndex_NumberOfSuccessfulRequests", "WSOperationMetricsSimpleIndex_ServiceTime", "WSOperationMetricsSimpleIndex_MaxResponseTime", "WSOperationMetricsSimpleIndex_LastResponseTime",
        "WSOperationMetricsSimpleIndex_MaxRequestSize", "WSOperationMetricsSimpleIndex_LastRequestSize", "WSOperationMetricsSimpleIndex_MaxResponseSize", "WSOperationMetricsSimpleIndex_LastResponseSize"};

    private static final String ETHERNETINTERFACESTATUS_METRIC_GROUP = "DataPowerEthernetInterfaceStatusMeasures";

    private static final String MSR_ETHERNETINTERFACESTATUS_STATUS = "EthernetInterfaceStatus_Status";
    private static final String MSR_ETHERNETINTERFACESTATUS_COLLISIONS = "EthernetInterfaceStatus_Collisions";
    private static final String MSR_ETHERNETINTERFACESTATUS_COLLISIONS2 = "EthernetInterfaceStatus_Collisions2";
    private static final String MSR_ETHERNETINTERFACESTATUS_RXPACKETS = "EthernetInterfaceStatus_RxPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_RXPACKETS2 = "EthernetInterfaceStatus_RxPackets2";
    private static final String MSR_ETHERNETINTERFACESTATUS_RXKBYTES = "EthernetInterfaceStatus_RxKBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_RXKBYTES2 = "EthernetInterfaceStatus_RxKBytes2";
    private static final String MSR_ETHERNETINTERFACESTATUS_RXHCPACKETS = "EthernetInterfaceStatus_RxHCPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_RXHCPACKETS2 = "EthernetInterfaceStatus_RxHCPackets2";
    private static final String MSR_ETHERNETINTERFACESTATUS_RXHCBYTES = "EthernetInterfaceStatus_RxHCBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_RXHCBYTES2 = "EthernetInterfaceStatus_RxHCBytes2";
    private static final String MSR_ETHERNETINTERFACESTATUS_RXERRORS = "EthernetInterfaceStatus_RxErrors";
    private static final String MSR_ETHERNETINTERFACESTATUS_RXERRORS2 = "EthernetInterfaceStatus_RxErrors2";
    private static final String MSR_ETHERNETINTERFACESTATUS_RXDROPS = "EthernetInterfaceStatus_RxDrops";
    private static final String MSR_ETHERNETINTERFACESTATUS_RXDROPS2 = "EthernetInterfaceStatus_RxDrops2";
    private static final String MSR_ETHERNETINTERFACESTATUS_TXPACKETS = "EthernetInterfaceStatus_TxPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_TXPACKETS2 = "EthernetInterfaceStatus_TxPackets2";
    private static final String MSR_ETHERNETINTERFACESTATUS_TXKBYTES = "EthernetInterfaceStatus_TxKBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_TXKBYTES2 = "EthernetInterfaceStatus_TxKBytes2";
    private static final String MSR_ETHERNETINTERFACESTATUS_TXHCPACKETS = "EthernetInterfaceStatus_TxHCPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_TXHCPACKETS2 = "EthernetInterfaceStatus_TxHCPackets2";
    private static final String MSR_ETHERNETINTERFACESTATUS_TXHCBYTES = "EthernetInterfaceStatus_TxHCBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_TXHCBYTES2 = "EthernetInterfaceStatus_TxHCBytes2";
    private static final String MSR_ETHERNETINTERFACESTATUS_TXERRORS = "EthernetInterfaceStatus_TxErrors";
    private static final String MSR_ETHERNETINTERFACESTATUS_TXERRORS2 = "EthernetInterfaceStatus_TxErrors2";
    private static final String MSR_ETHERNETINTERFACESTATUS_TXDROPS = "EthernetInterfaceStatus_TxDrops";
    private static final String MSR_ETHERNETINTERFACESTATUS_TXDROPS2 = "EthernetInterfaceStatus_TxDrops2";

    /*private static final String MSR_ETHERNETINTERFACESTATUS_ETH0_COLLISIONS = "EthernetInterfaceStatus_eth0_Collisions";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH0_COLLISIONS2 = "EthernetInterfaceStatus_eth0_Collisions2";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH0_STATUS = "EthernetInterfaceStatus_eth0_Status";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH0_RXPACKETS = "EthernetInterfaceStatus_eth0_RxPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH0_RXKBYTES = "EthernetInterfaceStatus_eth0_RxKBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH0_RXHCPACKETS = "EthernetInterfaceStatus_eth0_RxHCPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH0_RXHCBYTES = "EthernetInterfaceStatus_eth0_RxHCBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH0_RXERRORS = "EthernetInterfaceStatus_eth0_RxErrors";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH0_RXDROPS = "EthernetInterfaceStatus_eth0_RxDrops";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH0_RXERRORS2 = "EthernetInterfaceStatus_eth0_RxErrors2";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH0_RXDROPS2 = "EthernetInterfaceStatus_eth0_RxDrops2";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH0_TXPACKETS = "EthernetInterfaceStatus_eth0_TxPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH0_TXKBYTES = "EthernetInterfaceStatus_eth0_TxKBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH0_TXHCPACKETS = "EthernetInterfaceStatus_eth0_TxHCPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH0_TXHCBYTES = "EthernetInterfaceStatus_eth0_TxHCBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH0_TXERRORS = "EthernetInterfaceStatus_eth0_TxErrors";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH0_TXDROPS = "EthernetInterfaceStatus_eth0_TxDrops";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH0_TXERRORS2 = "EthernetInterfaceStatus_eth0_TxErrors2";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH0_TXDROPS2 = "EthernetInterfaceStatus_eth0_TxDrops2";

    private static final String MSR_ETHERNETINTERFACESTATUS_ETH1_COLLISIONS = "EthernetInterfaceStatus_eth1_Collisions";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH1_COLLISIONS2 = "EthernetInterfaceStatus_eth1_Collisions2";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH1_STATUS = "EthernetInterfaceStatus_eth1_Status";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH1_RXPACKETS = "EthernetInterfaceStatus_eth1_RxPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH1_RXKBYTES = "EthernetInterfaceStatus_eth1_RxKBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH1_RXHCPACKETS = "EthernetInterfaceStatus_eth1_RxHCPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH1_RXHCBYTES = "EthernetInterfaceStatus_eth1_RxHCBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH1_RXERRORS = "EthernetInterfaceStatus_eth1_RxErrors";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH1_RXDROPS = "EthernetInterfaceStatus_eth1_RxDrops";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH1_RXERRORS2 = "EthernetInterfaceStatus_eth1_RxErrors2";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH1_RXDROPS2 = "EthernetInterfaceStatus_eth1_RxDrops2";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH1_TXPACKETS = "EthernetInterfaceStatus_eth1_TxPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH1_TXKBYTES = "EthernetInterfaceStatus_eth1_TxKBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH1_TXHCPACKETS = "EthernetInterfaceStatus_eth1_TxHCPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH1_TXHCBYTES = "EthernetInterfaceStatus_eth1_TxHCBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH1_TXERRORS = "EthernetInterfaceStatus_eth1_TxErrors";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH1_TXDROPS = "EthernetInterfaceStatus_eth1_TxDrops";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH1_TXERRORS2 = "EthernetInterfaceStatus_eth1_TxErrors2";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH1_TXDROPS2 = "EthernetInterfaceStatus_eth1_TxDrops2";

    private static final String MSR_ETHERNETINTERFACESTATUS_ETH10_COLLISIONS = "EthernetInterfaceStatus_eth10_Collisions";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH10_COLLISIONS2 = "EthernetInterfaceStatus_eth10_Collisions2";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH10_STATUS = "EthernetInterfaceStatus_eth10_Status";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH10_RXPACKETS = "EthernetInterfaceStatus_eth10_RxPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH10_RXKBYTES = "EthernetInterfaceStatus_eth10_RxKBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH10_RXHCPACKETS = "EthernetInterfaceStatus_eth10_RxHCPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH10_RXHCBYTES = "EthernetInterfaceStatus_eth10_RxHCBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH10_RXERRORS = "EthernetInterfaceStatus_eth10_RxErrors";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH10_RXDROPS = "EthernetInterfaceStatus_eth10_RxDrops";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH10_RXERRORS2 = "EthernetInterfaceStatus_eth10_RxErrors2";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH10_RXDROPS2 = "EthernetInterfaceStatus_eth10_RxDrops2";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH10_TXPACKETS = "EthernetInterfaceStatus_eth10_TxPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH10_TXKBYTES = "EthernetInterfaceStatus_eth10_TxKBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH10_TXHCPACKETS = "EthernetInterfaceStatus_eth10_TxHCPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH10_TXHCBYTES = "EthernetInterfaceStatus_eth10_TxHCBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH10_TXERRORS = "EthernetInterfaceStatus_eth10_TxErrors";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH10_TXDROPS = "EthernetInterfaceStatus_eth10_TxDrops";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH10_TXERRORS2 = "EthernetInterfaceStatus_eth10_TxErrors2";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH10_TXDROPS2 = "EthernetInterfaceStatus_eth10_TxDrops2";

    private static final String MSR_ETHERNETINTERFACESTATUS_ETH11_COLLISIONS = "EthernetInterfaceStatus_eth11_Collisions";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH11_COLLISIONS2 = "EthernetInterfaceStatus_eth11_Collisions2";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH11_STATUS = "EthernetInterfaceStatus_eth11_Status";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH11_RXPACKETS = "EthernetInterfaceStatus_eth11_RxPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH11_RXKBYTES = "EthernetInterfaceStatus_eth11_RxKBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH11_RXHCPACKETS = "EthernetInterfaceStatus_eth11_RxHCPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH11_RXHCBYTES = "EthernetInterfaceStatus_eth11_RxHCBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH11_RXERRORS = "EthernetInterfaceStatus_eth11_RxErrors";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH11_RXDROPS = "EthernetInterfaceStatus_eth11_RxDrops";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH11_RXERRORS2 = "EthernetInterfaceStatus_eth11_RxErrors2";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH11_RXDROPS2 = "EthernetInterfaceStatus_eth11_RxDrops2";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH11_TXPACKETS = "EthernetInterfaceStatus_eth11_TxPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH11_TXKBYTES = "EthernetInterfaceStatus_eth11_TxKBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH11_TXHCPACKETS = "EthernetInterfaceStatus_eth11_TxHCPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH11_TXHCBYTES = "EthernetInterfaceStatus_eth11_TxHCBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH11_TXERRORS = "EthernetInterfaceStatus_eth11_TxErrors";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH11_TXDROPS = "EthernetInterfaceStatus_eth11_TxDrops";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH11_TXERRORS2 = "EthernetInterfaceStatus_eth11_TxErrors2";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH11_TXDROPS2 = "EthernetInterfaceStatus_eth11_TxDrops2";

    private static final String MSR_ETHERNETINTERFACESTATUS_ETH12_COLLISIONS = "EthernetInterfaceStatus_eth12_Collisions";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH12_COLLISIONS2 = "EthernetInterfaceStatus_eth12_Collisions2";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH12_STATUS = "EthernetInterfaceStatus_eth12_Status";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH12_RXPACKETS = "EthernetInterfaceStatus_eth12_RxPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH12_RXKBYTES = "EthernetInterfaceStatus_eth12_RxKBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH12_RXHCPACKETS = "EthernetInterfaceStatus_eth12_RxHCPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH12_RXHCBYTES = "EthernetInterfaceStatus_eth12_RxHCBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH12_RXERRORS = "EthernetInterfaceStatus_eth12_RxErrors";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH12_RXDROPS = "EthernetInterfaceStatus_eth12_RxDrops";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH12_RXERRORS2 = "EthernetInterfaceStatus_eth12_RxErrors2";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH12_RXDROPS2 = "EthernetInterfaceStatus_eth12_RxDrops2";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH12_TXPACKETS = "EthernetInterfaceStatus_eth12_TxPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH12_TXKBYTES = "EthernetInterfaceStatus_eth12_TxKBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH12_TXHCPACKETS = "EthernetInterfaceStatus_eth12_TxHCPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH12_TXHCBYTES = "EthernetInterfaceStatus_eth12_TxHCBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH12_TXERRORS = "EthernetInterfaceStatus_eth12_TxErrors";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH12_TXDROPS = "EthernetInterfaceStatus_eth12_TxDrops";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH12_TXERRORS2 = "EthernetInterfaceStatus_eth12_TxErrors2";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH12_TXDROPS2 = "EthernetInterfaceStatus_eth12_TxDrops2";

    private static final String MSR_ETHERNETINTERFACESTATUS_ETH13_COLLISIONS = "EthernetInterfaceStatus_eth13_Collisions";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH13_COLLISIONS2 = "EthernetInterfaceStatus_eth13_Collisions2";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH13_STATUS = "EthernetInterfaceStatus_eth13_Status";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH13_RXPACKETS = "EthernetInterfaceStatus_eth13_RxPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH13_RXKBYTES = "EthernetInterfaceStatus_eth13_RxKBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH13_RXHCPACKETS = "EthernetInterfaceStatus_eth13_RxHCPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH13_RXHCBYTES = "EthernetInterfaceStatus_eth13_RxHCBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH13_RXERRORS = "EthernetInterfaceStatus_eth13_RxErrors";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH13_RXDROPS = "EthernetInterfaceStatus_eth13_RxDrops";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH13_RXERRORS2 = "EthernetInterfaceStatus_eth13_RxErrors2";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH13_RXDROPS2 = "EthernetInterfaceStatus_eth13_RxDrops2";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH13_TXPACKETS = "EthernetInterfaceStatus_eth13_TxPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH13_TXKBYTES = "EthernetInterfaceStatus_eth13_TxKBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH13_TXHCPACKETS = "EthernetInterfaceStatus_eth13_TxHCPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH13_TXHCBYTES = "EthernetInterfaceStatus_eth13_TxHCBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH13_TXERRORS = "EthernetInterfaceStatus_eth13_TxErrors";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH13_TXDROPS = "EthernetInterfaceStatus_eth13_TxDrops";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH13_TXERRORS2 = "EthernetInterfaceStatus_eth13_TxErrors2";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH13_TXDROPS2 = "EthernetInterfaceStatus_eth13_TxDrops2";

    private static final String MSR_ETHERNETINTERFACESTATUS_ETH20_COLLISIONS = "EthernetInterfaceStatus_eth20_Collisions";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH20_COLLISIONS2 = "EthernetInterfaceStatus_eth20_Collisions2";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH20_STATUS = "EthernetInterfaceStatus_eth20_Status";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH20_RXPACKETS = "EthernetInterfaceStatus_eth20_RxPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH20_RXKBYTES = "EthernetInterfaceStatus_eth20_RxKBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH20_RXHCPACKETS = "EthernetInterfaceStatus_eth20_RxHCPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH20_RXHCBYTES = "EthernetInterfaceStatus_eth20_RxHCBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH20_RXERRORS = "EthernetInterfaceStatus_eth20_RxErrors";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH20_RXDROPS = "EthernetInterfaceStatus_eth20_RxDrops";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH20_RXERRORS2 = "EthernetInterfaceStatus_eth20_RxErrors2";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH20_RXDROPS2 = "EthernetInterfaceStatus_eth20_RxDrops2";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH20_TXPACKETS = "EthernetInterfaceStatus_eth20_TxPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH20_TXKBYTES = "EthernetInterfaceStatus_eth20_TxKBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH20_TXHCPACKETS = "EthernetInterfaceStatus_eth20_TxHCPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH20_TXHCBYTES = "EthernetInterfaceStatus_eth20_TxHCBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH20_TXERRORS = "EthernetInterfaceStatus_eth20_TxErrors";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH20_TXDROPS = "EthernetInterfaceStatus_eth20_TxDrops";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH20_TXERRORS2 = "EthernetInterfaceStatus_eth20_TxErrors2";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH20_TXDROPS2 = "EthernetInterfaceStatus_eth20_TxDrops2";

    private static final String MSR_ETHERNETINTERFACESTATUS_ETH21_COLLISIONS = "EthernetInterfaceStatus_eth21_Collisions";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH21_COLLISIONS2 = "EthernetInterfaceStatus_eth21_Collisions2";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH21_STATUS = "EthernetInterfaceStatus_eth21_Status";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH21_RXPACKETS = "EthernetInterfaceStatus_eth21_RxPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH21_RXKBYTES = "EthernetInterfaceStatus_eth21_RxKBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH21_RXHCPACKETS = "EthernetInterfaceStatus_eth21_RxHCPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH21_RXHCBYTES = "EthernetInterfaceStatus_eth21_RxHCBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH21_RXERRORS = "EthernetInterfaceStatus_eth21_RxErrors";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH21_RXDROPS = "EthernetInterfaceStatus_eth21_RxDrops";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH21_RXERRORS2 = "EthernetInterfaceStatus_eth21_RxErrors2";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH21_RXDROPS2 = "EthernetInterfaceStatus_eth21_RxDrops2";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH21_TXPACKETS = "EthernetInterfaceStatus_eth21_TxPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH21_TXKBYTES = "EthernetInterfaceStatus_eth21_TxKBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH21_TXHCPACKETS = "EthernetInterfaceStatus_eth21_TxHCPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH21_TXHCBYTES = "EthernetInterfaceStatus_eth21_TxHCBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH21_TXERRORS = "EthernetInterfaceStatus_eth21_TxErrors";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH21_TXDROPS = "EthernetInterfaceStatus_eth21_TxDrops";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH21_TXERRORS2 = "EthernetInterfaceStatus_eth21_TxErrors2";
    private static final String MSR_ETHERNETINTERFACESTATUS_ETH21_TXDROPS2 = "EthernetInterfaceStatus_eth21_TxDrops2";

    private static final String MSR_ETHERNETINTERFACESTATUS_MGT0_COLLISIONS = "EthernetInterfaceStatus_mgt0_Collisions";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT0_COLLISIONS2 = "EthernetInterfaceStatus_mgt0_Collisions2";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT0_STATUS = "EthernetInterfaceStatus_mgt0_Status";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT0_RXPACKETS = "EthernetInterfaceStatus_mgt0_RxPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT0_RXKBYTES = "EthernetInterfaceStatus_mgt0_RxKBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT0_RXHCPACKETS = "EthernetInterfaceStatus_mgt0_RxHCPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT0_RXHCBYTES = "EthernetInterfaceStatus_mgt0_RxHCBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT0_RXERRORS = "EthernetInterfaceStatus_mgt0_RxErrors";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT0_RXDROPS = "EthernetInterfaceStatus_mgt0_RxDrops";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT0_RXERRORS2 = "EthernetInterfaceStatus_mgt0_RxErrors2";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT0_RXDROPS2 = "EthernetInterfaceStatus_mgt0_RxDrops2";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT0_TXPACKETS = "EthernetInterfaceStatus_mgt0_TxPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT0_TXKBYTES = "EthernetInterfaceStatus_mgt0_TxKBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT0_TXHCPACKETS = "EthernetInterfaceStatus_mgt0_TxHCPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT0_TXHCBYTES = "EthernetInterfaceStatus_mgt0_TxHCBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT0_TXERRORS = "EthernetInterfaceStatus_mgt0_TxErrors";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT0_TXDROPS = "EthernetInterfaceStatus_mgt0_TxDrops";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT0_TXERRORS2 = "EthernetInterfaceStatus_mgt0_TxErrors2";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT0_TXDROPS2 = "EthernetInterfaceStatus_mgt0_TxDrops";

    private static final String MSR_ETHERNETINTERFACESTATUS_MGT1_COLLISIONS = "EthernetInterfaceStatus_mgt1_Collisions";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT1_COLLISIONS2 = "EthernetInterfaceStatus_mgt1_Collisions2";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT1_STATUS = "EthernetInterfaceStatus_mgt1_Status";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT1_RXPACKETS = "EthernetInterfaceStatus_mgt1_RxPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT1_RXKBYTES = "EthernetInterfaceStatus_mgt1_RxKBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT1_RXHCPACKETS = "EthernetInterfaceStatus_mgt1_RxHCPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT1_RXHCBYTES = "EthernetInterfaceStatus_mgt1_RxHCBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT1_RXERRORS = "EthernetInterfaceStatus_mgt1_RxErrors";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT1_RXDROPS = "EthernetInterfaceStatus_mgt1_RxDrops";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT1_RXERRORS2 = "EthernetInterfaceStatus_mgt1_RxErrors2";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT1_RXDROPS2 = "EthernetInterfaceStatus_mgt1_RxDrops2";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT1_TXPACKETS = "EthernetInterfaceStatus_mgt1_TxPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT1_TXKBYTES = "EthernetInterfaceStatus_mgt1_TxKBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT1_TXHCPACKETS = "EthernetInterfaceStatus_mgt1_TxHCPackets";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT1_TXHCBYTES = "EthernetInterfaceStatus_mgt1_TxHCBytes";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT1_TXERRORS = "EthernetInterfaceStatus_mgt1_TxErrors";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT1_TXDROPS = "EthernetInterfaceStatus_mgt1_TxDrops";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT1_TXERRORS2 = "EthernetInterfaceStatus_mgt1_TxErrors2";
    private static final String MSR_ETHERNETINTERFACESTATUS_MGT1_TXDROPS2 = "EthernetInterfaceStatus_mgt1_TxDrops2";
     */
    private static final String OBJECTSTATUS_METRIC_GROUP = "DataPowerObjectStatusMeasures";
    private static final String MSR_OBJECTSTATUS_OPSTATE = "ObjectStatus_OpState";
    private static final String MSR_OBJECTSTATUS_ADMINSTATE = "ObjectStatus_AdminState";
    private static final String SYSTEMUSAGE_METRIC_GROUP = "DataPowerSystemUsageMeasures";
    private static final String MSR_SYSTEMUSAGE_INTERVAL = "SystemUsage_Interval";
    private static final String MSR_SYSTEMUSAGE_LOAD = "SystemUsage_Load";
    private static final String MSR_SYSTEMUSAGE_WORKLIST = "SystemUsage_WorkList";
    private static final String TCPSUMMARY_METRIC_GROUP = "DataPowerTCPSummaryMeasures";
    private static final String MSR_TCPSUMMARY_ESTABLISHED = "TCPSummary_Established";
    private static final String MSR_TCPSUMMARY_SYN_SENT = "TCPSummary_Syn_Sent";
    private static final String MSR_TCPSUMMARY_SYN_RECEIVED = "TCPSummary_Syn_Received";
    private static final String MSR_TCPSUMMARY_FIN_WAIT_1 = "TCPSummary_Fin_Wait_1";
    private static final String MSR_TCPSUMMARY_FIN_WAIT_2 = "TCPSummary_Fin_Wait_2";
    private static final String MSR_TCPSUMMARY_TIME_WAIT = "TCPSummary_Time_Wait";
    private static final String MSR_TCPSUMMARY_CLOSED = "TCPSummary_Closed";
    private static final String MSR_TCPSUMMARY_CLOSE_WAIT = "TCPSummary_Close_Wait";
    private static final String MSR_TCPSUMMARY_LAST_ACK = "TCPSummary_Last_Ack";
    private static final String MSR_TCPSUMMARY_LISTEN = "TCPSummary_Listen";
    private static final String MSR_TCPSUMMARY_CLOSING = "TCPSummary_Closing";
    private static final String STYLESHEETEXECUTIONS_METRIC_GROUP = "DataPowerStylesheetExecutionsMeasures";
    private static final String MSR_STYLESHEETEXECUTIONS_TENSECONDS = "StylesheetExecutions_TenSeconds";
    private static final String MSR_STYLESHEETEXECUTIONS_ONEMINUTE = "StylesheetExecutions_OneMinute";
    private static final String MSR_STYLESHEETEXECUTIONS_TENMINUTES = "StylesheetExecutions_TenMinutes";
    private static final String MSR_STYLESHEETEXECUTIONS_ONEHOUR = "StylesheetExecutions_OneHour";
    private static final String MSR_STYLESHEETEXECUTIONS_ONEDAY = "StylesheetExecutions_OneDay";
    private static final String DOMAINSTATUS_METRIC_GROUP = "DataPowerDomainStatusMeasures";
    private static final String MSR_DOMAINSTATUS_SAVENEEDED = "DomainStatus_SaveNeeded";
    private static final String MSR_DOMAINSTATUS_TRACEENABLED = "DomainStatus_TraceEnabled";
    private static final String MSR_DOMAINSTATUS_DEBUGENABLED = "DomainStatus_DebugEnabled";
    private static final String MSR_DOMAINSTATUS_PROBEENABLED = "DomainStatus_ProbeEnabled";
    private static final String MSR_DOMAINSTATUS_DIAGENABLED = "DomainStatus_DiagEnabled";
    private static final String HTTPTRANSACTIONS_METRIC_GROUP = "DataPowerHTTPTransactionsMeasures";
    private static final String MSR_HTTPTRANSACTIONS_TENSECONDS = "HTTPTransactions_TenSeconds";
    private static final String MSR_HTTPTRANSACTIONS_ONEMINUTE = "HTTPTransactions_OneMinute";
    private static final String MSR_HTTPTRANSACTIONS_TENMINUTES = "HTTPTransactions_TenMinutes";
    private static final String MSR_HTTPTRANSACTIONS_ONEHOUR = "HTTPTransactions_OneHour";
    private static final String MSR_HTTPTRANSACTIONS_ONEDAY = "HTTPTransactions_OneDay";

    private static final String HTTPMEANTRANSACTIONTIME_METRIC_GROUP = "DataPowerHTTPMeanTransactionTimeMeasures";

    private static final String MSR_HTTPMEANTRANSACTIONTIME_TENSECONDS = "HTTPMeanTransactionTime_TenSeconds";
    private static final String MSR_HTTPMEANTRANSACTIONTIME_ONEMINUTE = "HTTPMeanTransactionTime_OneMinute";
    private static final String MSR_HTTPMEANTRANSACTIONTIME_TENMINUTES = "HTTPMeanTransactionTime_TenMinutes";
    private static final String MSR_HTTPMEANTRANSACTIONTIME_ONEHOUR = "HTTPMeanTransactionTime_OneHour";
    private static final String MSR_HTTPMEANTRANSACTIONTIME_ONEDAY = "HTTPMeanTransactionTime_OneDay";

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
        String firmwareVersion;
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

        log.log(Level.INFO, "Executing DataPowerMonitor version 6.2.0.9103 ..." + env.getHost() + " ->" + config.url);
        log.log(Level.INFO, "*******************************************************************1");
        log.log(Level.INFO, "*******************************************************************2");
        log.log(Level.INFO, "*******************************************************************3");

        status = executeConnectionsAccepted(env);
        status = executeAvailability(env);
        status = executeCPUUsage(env);
        status = executeDocumentCachingSummary(env);
        status = executeStylesheetCachingSummary(env);
        status = executeEnvironmentalSensors(env);
        status = executeHTTPConnections(env);
        status = executeWSOperationMetricsSimpleIndex(env);
        status = executeMemoryStatus(env);
        status = executeFilesystemStatus(env);
        status = executeEthernetInterfaceStatus(env);

        status = executeObjectStatus(env);
        status = executeSystemUsage(env);
        status = executeTCPSummary(env);
        status = executeStylesheetExecutions(env);
        status = executeDomainStatus(env);
        status = executeHTTPTransactions(env);
        status = executeHTTPMeanTransactionTime(env);
        return status;
    }

    private Status executeAvailability(MonitorEnvironment env) {
        //Go ahead and set availability measures after first SOMA call
        // measurement variables

        /*try {
            config.url = new URL("http://localhost:8080/JMXTestServletApp/ConnectionsAccepted.xml");
        } catch (MalformedURLException ex) {
            Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        log.log(Level.INFO, "Populating Availability Measures for {0} ->{1}", new Object[]{env.getHost(), config.url});

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
                    log.log(Level.FINE, "Entering Header Size and Header Size is {0}", headerSize);
                    measure.setValue(headerSize);
                }

            }
            if ((measures = env.getMonitorMeasures(AVAILABILITY_METRIC_GROUP, MSR_FIRST_RESPONSE_DELAY)) != null) {
                double firstResponseTimeMillis = firstResponseTime * MILLIS;
                for (MonitorMeasure measure : measures) {
                    log.log(Level.FINE, "Entering firstResponseTimeMillis is {0}", firstResponseTimeMillis);
                    measure.setValue(firstResponseTimeMillis);
                }

            }

            if ((measures = env.getMonitorMeasures(AVAILABILITY_METRIC_GROUP, MSR_RESPONSE_COMPLETE_TIME)) != null) {
                double responseCompleteTimeMillis = responseCompleteTime * MILLIS;
                for (MonitorMeasure measure : measures) {
                    log.log(Level.FINE, "Entering responseCompleteTimeMillis is {0}", responseCompleteTimeMillis);
                    measure.setValue(responseCompleteTimeMillis);
                }

            }
            if ((measures = env.getMonitorMeasures(AVAILABILITY_METRIC_GROUP, MSR_RESPONSE_SIZE)) != null) {
                for (MonitorMeasure measure : measures) {
                    log.log(Level.FINE, "Entering inputSize is {0}", inputSize);
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
                    log.log(Level.FINE, "Entering throughput is {0}", throughput);
                    measure.setValue(throughput);
                }

            }
            if ((measures = env.getMonitorMeasures(AVAILABILITY_METRIC_GROUP, MSR_HTTP_STATUS_CODE)) != null) {
                for (MonitorMeasure measure : measures) {
                    log.log(Level.FINE, "Entering httpStatusCode is {0}", httpStatusCode);
                    measure.setValue(httpStatusCode);
                }

            }
            if ((measures = env.getMonitorMeasures(AVAILABILITY_METRIC_GROUP, MSR_CONN_CLOSE_DELAY)) != null) {
                double connectionCloseDelayMillis = connectionCloseDelay * MILLIS;
                for (MonitorMeasure measure : measures) {
                    log.log(Level.FINE, "Entering connectionCloseDelayMillis is {0}", connectionCloseDelayMillis);
                    measure.setValue(connectionCloseDelayMillis);
                }

            }
            if ((measures = env.getMonitorMeasures(AVAILABILITY_METRIC_GROUP, MSR_CONTENT_VERIFIED)) != null) {
                for (MonitorMeasure measure : measures) {
                    log.log(Level.FINE, "Entering verified is {0}", verified);
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

        //config.url = new URL("http://localhost:8080//service/mgmt/current/curl.xml");
        config.method = "POST";
        //config.method = "GET";

        config.postData = env.getConfigString(CONFIG_POST_DATA);
        config.httpVersion = env.getConfigString(CONFIG_HTTP_VERSION);
        config.userAgent = env.getConfigString(CONFIG_USER_AGENT);
        config.firmwareVersion = env.getConfigString(CONFIG_FIRMWARE_VERSION); // ET: firmware 7 change
        log.log(Level.FINE, "readConfig method: firmwareVersion is {0}", new Object[]{config.firmwareVersion});
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
        //httpClient.getParams().setParameter(HttpClientParams.USER_AGENT, config.userAgent);
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

        URI testURI = null;
        /*try {
            testURI = new URI("http://localhost:8080/JMXTestServletApp/ConnectionsAccepted.xml");
        } catch (URIException ex) {
            Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }*/

        if (testURI != null) {
            log.log(Level.INFO, "Populating Connections Accepted Measures for {0} ->{1}", new Object[]{env.getHost(), testURI});
        } else {
            log.log(Level.INFO, "Populating Connections Accepted Measures for {0} ->{1}", new Object[]{env.getHost(), config.url});
        }

        Document doc = null;
        NodeList measureList = null;

        status = new Status();

        String connectionsAcceptedResponse = callDPSOMAMethod(env, "ConnectionsAccepted", testURI);

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

        URI testURI = null;
        /*try {
            testURI = new URI("http://localhost:8080/JMXTestServletApp/CPUUsage.xml");
        } catch (URIException ex) {
            Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }*/

        if (testURI != null) {
            log.log(Level.INFO, "Populating CPU Usage Measures for {0} ->{1}", new Object[]{env.getHost(), testURI});
        } else {
            log.log(Level.INFO, "Populating CPU Usage Measures for {0} ->{1}", new Object[]{env.getHost(), config.url});
        }

        Document doc = null;
        NodeList measureList = null;

        status = new Status();

        String CPUUsageResponse = callDPSOMAMethod(env, "CPUUsage", testURI);

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

        URI testURI = null;
        /*try {
            testURI = new URI("http://localhost:8080/JMXTestServletApp/DocumentCachingSummary.xml");
        } catch (URIException ex) {
            Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }*/

        if (testURI != null) {
            log.log(Level.INFO, "Populating Document Caching Measures for {0} ->{1}", new Object[]{env.getHost(), testURI});
        } else {
            log.log(Level.INFO, "Populating Document Caching Measures for {0} ->{1}", new Object[]{env.getHost(), config.url});
        }

        Document doc = null;
        NodeList measureList = null;

        status = new Status();

        String DocumentCachingSummaryResponse = callDPSOMAMethod(env, "DocumentCachingSummary", testURI);

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
//                addDoubleMeasure(env, element, DOCUMENTCACHINGSUMMARY_METRIC_GROUP, MSR_DOCUMENTCACHINGSUMMARY_CACHESIZE, "CacheSize");
//                addDoubleMeasure(env, element, DOCUMENTCACHINGSUMMARY_METRIC_GROUP, MSR_DOCUMENTCACHINGSUMMARY_BYTECOUNT, "ByteCount");
                if (config.firmwareVersion.equals(FIRMWARE_7)) {
                    addDoubleMeasure(env, element, DOCUMENTCACHINGSUMMARY_METRIC_GROUP, MSR_DOCUMENTCACHINGSUMMARY_CACHESIZE, "CacheSizeKiB"); 	// ET: firmware 7 change from CacheSize to CacheSizeKiB
                    addDoubleMeasure(env, element, DOCUMENTCACHINGSUMMARY_METRIC_GROUP, MSR_DOCUMENTCACHINGSUMMARY_BYTECOUNT, "KiByteCount");	// ET: firmware 7 change from ByteCount to KiByteCount
                } else {
                    addDoubleMeasure(env, element, DOCUMENTCACHINGSUMMARY_METRIC_GROUP, MSR_DOCUMENTCACHINGSUMMARY_CACHESIZE, "CacheSize");
                    addDoubleMeasure(env, element, DOCUMENTCACHINGSUMMARY_METRIC_GROUP, MSR_DOCUMENTCACHINGSUMMARY_BYTECOUNT, "ByteCount");

                }

            }
        } else {
            status.setMessage("SOMA call unsuccessful: DocumentCachingSummary response is null");
            status.setStatusCode(Status.StatusCode.ErrorInternal);
        }

        return status;
    }

    private Status executeStylesheetCachingSummary(MonitorEnvironment env) {

        URI testURI = null;
        /*try {
            testURI = new URI("http://localhost:8080/JMXTestServletApp/StylesheetCachingSummary.xml");
        } catch (URIException ex) {
            Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }*/

        if (testURI != null) {
            log.log(Level.INFO, "Populating Stylesheet Caching Summary Measures for {0} ->{1}", new Object[]{env.getHost(), testURI});
        } else {
            log.log(Level.INFO, "Populating Stylesheet Caching Summary Measures for {0} ->{1}", new Object[]{env.getHost(), config.url});
        }

        Document doc = null;
        NodeList measureList = null;

        status = new Status();

        String StylesheetCachingSummaryResponse = callDPSOMAMethod(env, "StylesheetCachingSummary", testURI);

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

        URI testURI = null;
        /*try {
            testURI = new URI("http://localhost:8080/JMXTestServletApp/EnvironmentalSensors.xml");
        } catch (URIException ex) {
            Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }*/

        if (testURI != null) {
            log.log(Level.INFO, "Populating Environmental Sensors Measures for {0} ->{1}", new Object[]{env.getHost(), testURI});
        } else {
            log.log(Level.INFO, "Populating Environmental Sensors Measures for {0} ->{1}", new Object[]{env.getHost(), config.url});
        }

        Document doc = null;
        NodeList measureList = null;

        status = new Status();

        String EnvironmentalSensorsResponse = callDPSOMAMethod(env, "EnvironmentalSensors", testURI);

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
                        log.log(Level.FINE, "Entering caseopen is {0}", caseopenString);
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
                        log.log(Level.FINE, "Entering powersupplyok is {0}", powersupplyokString);
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

        URI testURI = null;
        /*try {
            testURI = new URI("http://localhost:8080/JMXTestServletApp/HTTPConnections.xml");
        } catch (URIException ex) {
            Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }*/

        if (testURI != null) {
            log.log(Level.INFO, "Populating HTTP Connections Measures for {0} ->{1}", new Object[]{env.getHost(), testURI});
        } else {
            log.log(Level.INFO, "Populating HTTP Connections Measures for {0} ->{1}", new Object[]{env.getHost(), config.url});
        }

        Document doc = null;
        NodeList measureList = null;

        status = new Status();

        String HTTPConnectionsResponse = callDPSOMAMethod(env, "HTTPConnections", testURI);

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

                addDoubleMeasureDivideByTen(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_REQSEC, "reqTenSec");

                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_REQONEMIN, "reqOneMin");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_REQTENMIN, "reqTenMin");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_REQONEHR, "reqOneHr");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_REQONEDAY, "reqOneDay");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_REUSETENSEC, "reuseTenSec");

                addDoubleMeasureDivideByTen(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_REUSESEC, "reuseTenSec");

                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_REUSEONEMIN, "reuseOneMin");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_REUSETENMIN, "reuseTenMin");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_REUSEONEHR, "reuseOneHr");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_REUSEONEDAY, "reuseOneDay");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_CREATETENSEC, "createTenSec");

                addDoubleMeasureDivideByTen(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_CREATESEC, "createTenSec");

                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_CREATEONEMIN, "createOneMin");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_CREATETENMIN, "createTenMin");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_CREATEONEHR, "createOneHr");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_CREATEONEDAY, "createOneDay");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_RETURNTENSEC, "returnTenSec");

                addDoubleMeasureDivideByTen(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_RETURNSEC, "returnTenSec");

                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_RETURNONEMIN, "returnOneMin");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_RETURNTENMIN, "returnTenMin");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_RETURNONEHR, "returnOneHr");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_RETURNONEDAY, "returnOneDay");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_OFFERTENSEC, "offerTenSec");

                addDoubleMeasureDivideByTen(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_OFFERSEC, "offerTenSec");

                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_OFFERONEMIN, "offerOneMin");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_OFFERTENMIN, "offerTenMin");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_OFFERONEHR, "offerOneHr");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_OFFERONEDAY, "offerOneDay");
                addDoubleMeasure(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_DESTROYTENSEC, "destroyTenSec");

                addDoubleMeasureDivideByTen(env, element, HTTPCONNECTIONS_METRIC_GROUP, MSR_HTTPCONNECTIONS_DESTROYSEC, "destroyTenSec");

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

    private Status executeWSOperationMetricsSimpleIndex(MonitorEnvironment env) {

        URI testURI = null;
        /*try {
            testURI = new URI("http://localhost:8080/JMXTestServletApp/output.xml");
        } catch (URIException ex) {
            Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }*/

        if (testURI != null) {
            log.log(Level.INFO, "Populating WS Operations Measures for {0} ->{1}", new Object[]{env.getHost(), testURI});
        } else {
            log.log(Level.INFO, "Populating WS Operations Measures for {0} ->{1}", new Object[]{env.getHost(), config.url});
        }

        Document doc = null;
        NodeList measureList = null;
        String WSOperationMetricsSimpleIndexResponse = null;

        status = new Status();

        WSOperationMetricsSimpleIndexResponse = callDPSOMAMethod(env, "WSOperationMetricsSimpleIndex", testURI);

        log.log(Level.FINE, "The response string is {0}", WSOperationMetricsSimpleIndexResponse);

        if (WSOperationMetricsSimpleIndexResponse != null) {

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = null;
            try {
                docBuilder = docBuilderFactory.newDocumentBuilder();
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }

            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(WSOperationMetricsSimpleIndexResponse));
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

            measureList = doc.getElementsByTagName("WSOperationMetricsSimpleIndex");

            for (int i = 0; i < measureList.getLength(); i++) {
                log.log(Level.FINE, "Inside measureList loop -> Measure List Metric is {0}", measureList.item(i));
                for (String SERVICE_METRICS1 : SERVICE_METRICS) {
                    log.log(Level.FINE, "Inside SERVICE_METRICS loop -> Service Metric is {0}", SERVICE_METRICS1);
                    log.log(Level.FINE, "Getting Monitor Measures -> Metric Group is {0}  -> Service Metric is {1}", new Object[]{WSOPERATIONMETRICSSIMPLEINDEX_METRIC_GROUP, SERVICE_METRICS1});
                    Collection<MonitorMeasure> monitorMeasures = env.getMonitorMeasures(WSOPERATIONMETRICSSIMPLEINDEX_METRIC_GROUP, SERVICE_METRICS1);
                    log.log(Level.FINE, "monitorMeasures size is {0}", monitorMeasures.size());
                    for (MonitorMeasure subscribedMonitorMeasure : monitorMeasures) {

                        Element element = (Element) measureList.item(i);

                        switch (SERVICE_METRICS1) {
                            case "WSOperationMetricsSimpleIndex_NumberOfRequests":
                                addDynamicDoubleMeasureWSOperationMetricsSimpleIndex(env, element, WSOPERATIONMETRICSSIMPLEINDEX_METRIC_GROUP, subscribedMonitorMeasure, "NumberOfRequests");
                                break;
                            case "WSOperationMetricsSimpleIndex_NumberOfFailedRequests":
                                addDynamicDoubleMeasureWSOperationMetricsSimpleIndex(env, element, WSOPERATIONMETRICSSIMPLEINDEX_METRIC_GROUP, subscribedMonitorMeasure, "NumberOfFailedRequests");
                                break;
                            case "WSOperationMetricsSimpleIndex_NumberOfSuccessfulRequests":
                                addDynamicDoubleMeasureWSOperationMetricsSimpleIndex(env, element, WSOPERATIONMETRICSSIMPLEINDEX_METRIC_GROUP, subscribedMonitorMeasure, "NumberOfSuccessfulRequests");
                                break;
                            case "WSOperationMetricsSimpleIndex_ServiceTime":
                                addDynamicDoubleMeasureWSOperationMetricsSimpleIndex(env, element, WSOPERATIONMETRICSSIMPLEINDEX_METRIC_GROUP, subscribedMonitorMeasure, "ServiceTime");
                                break;
                            case "WSOperationMetricsSimpleIndex_MaxResponseTime":
                                addDynamicDoubleMeasureWSOperationMetricsSimpleIndex(env, element, WSOPERATIONMETRICSSIMPLEINDEX_METRIC_GROUP, subscribedMonitorMeasure, "MaxResponseTime");
                                break;
                            case "WSOperationMetricsSimpleIndex_LastResponseTime":
                                addDynamicDoubleMeasureWSOperationMetricsSimpleIndex(env, element, WSOPERATIONMETRICSSIMPLEINDEX_METRIC_GROUP, subscribedMonitorMeasure, "LastResponseTime");
                                break;
                            case "WSOperationMetricsSimpleIndex_MaxRequestSize":
                                addDynamicDoubleMeasureWSOperationMetricsSimpleIndex(env, element, WSOPERATIONMETRICSSIMPLEINDEX_METRIC_GROUP, subscribedMonitorMeasure, "MaxRequestSize");
                                break;
                            case "WSOperationMetricsSimpleIndex_LastRequestSize":
                                addDynamicDoubleMeasureWSOperationMetricsSimpleIndex(env, element, WSOPERATIONMETRICSSIMPLEINDEX_METRIC_GROUP, subscribedMonitorMeasure, "LastRequestSize");
                                break;
                            case "WSOperationMetricsSimpleIndex_MaxResponseSize":
                                addDynamicDoubleMeasureWSOperationMetricsSimpleIndex(env, element, WSOPERATIONMETRICSSIMPLEINDEX_METRIC_GROUP, subscribedMonitorMeasure, "MaxResponseSize");
                                break;
                            case "WSOperationMetricsSimpleIndex_LastResponseSize":
                                addDynamicDoubleMeasureWSOperationMetricsSimpleIndex(env, element, WSOPERATIONMETRICSSIMPLEINDEX_METRIC_GROUP, subscribedMonitorMeasure, "LastResponseSize");
                                break;
                            default:
                                log.log(Level.WARNING, "Monitor Measure {0} is unkown", SERVICE_METRICS1);

                        }
                    }
                }
            }
        } else {
            status.setMessage("SOMA call unsuccessful: WSOperationMetricsSimpleIndex response is null");
            status.setStatusCode(Status.StatusCode.ErrorInternal);
        }

        return status;
    }

    private Status executeMemoryStatus(MonitorEnvironment env) {

        URI testURI = null;
        /*try {
            testURI = new URI("http://localhost:8080/JMXTestServletApp/MemoryStatus.xml");
        } catch (URIException ex) {
            Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }*/

        if (testURI != null) {
            log.log(Level.INFO, "Populating Memory Status Measures for {0} ->{1}", new Object[]{env.getHost(), testURI});
        } else {
            log.log(Level.INFO, "Populating Memory Status Measures for {0} ->{1}", new Object[]{env.getHost(), config.url});
        }

        Document doc = null;
        NodeList measureList = null;

        status = new Status();

        String MemoryStatusResponse = callDPSOMAMethod(env, "MemoryStatus", testURI);

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

        URI testURI = null;
        /*try {
            testURI = new URI("http://localhost:8080/JMXTestServletApp/FileSystemStatus.xml");
        } catch (URIException ex) {
            Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }*/

        if (testURI != null) {
            log.log(Level.INFO, "Populating Filesystem Status Measures for {0} ->{1}", new Object[]{env.getHost(), testURI});
        } else {
            log.log(Level.INFO, "Populating Filesystem Status Measures for {0} ->{1}", new Object[]{env.getHost(), config.url});
        }

        Document doc = null;
        NodeList measureList = null;

        status = new Status();

        String FilesystemStatusResponse = callDPSOMAMethod(env, "FilesystemStatus", testURI);

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

        URI testURI = null;
        /*try {
            testURI = new URI("http://localhost:8080/JMXTestServletApp/EthernetInterfaceStatus.xml");
        } catch (URIException ex) {
            Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }*/

        if (testURI != null) {
            log.log(Level.INFO, "Populating Ethernet Interface Measures for {0} ->{1}", new Object[]{env.getHost(), testURI});
        } else {
            log.log(Level.INFO, "Populating Ethernet Interface Measures for {0} ->{1}", new Object[]{env.getHost(), config.url});
        }

        Document doc = null;
        NodeList measureList = null;

        status = new Status();

        String EthernetInterfaceStatusResponse = callDPSOMAMethod(env, "EthernetInterfaceStatus", testURI);

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
            MonitorMeasure dynamicMeasure;

            for (int i = 0; i < measureList.getLength(); i++) {
                Element element = (Element) measureList.item(i); //This is the ith node in that node list

                String elementPrint = element.getTextContent();
                log.log(Level.FINE, "elementPrint_{0} is : {1}", new Object[]{i, elementPrint});

                NodeList NameList = element.getElementsByTagName("Name");
                Element NameElement = (Element) NameList.item(0);
                String NameString = NameElement.getTextContent();

                NodeList IPList = element.getElementsByTagName("IP");
                Element IPElement = (Element) IPList.item(0);
                String IPString = IPElement.getTextContent();

                String InterfaceName = NameString + "_" + IPString;

                Element InterfaceStatusElement = null;
                String InterfaceStatusString = null;
                int InterfaceStatusInt = 0;

                Element CollisionsElement = null;
                String CollisionsString = null;
                Double CollisionsDBL = null;
                Element Collisions2Element = null;
                String Collisions2String = null;
                Double Collisions2DBL = null;

                Element RxHCBytesElement = null;
                String RxHCBytesString = null;
                Double RxHCBytesDBL = null;
                Element RxHCBytes2Element = null;
                String RxHCBytes2String = null;
                Double RxHCBytes2DBL = null;

                Element RxHCPacketsElement = null;
                String RxHCPacketsString = null;
                Double RxHCPacketsDBL = null;
                Element RxHCPackets2Element = null;
                String RxHCPackets2String = null;
                Double RxHCPackets2DBL = null;

                Element RxErrorsElement = null;
                String RxErrorsString = null;
                Double RxErrorsDBL = null;
                Element RxErrors2Element = null;
                String RxErrors2String = null;
                Double RxErrors2DBL = null;

                Element RxDropsElement = null;
                String RxDropsString = null;
                Double RxDropsDBL = null;
                Element RxDrops2Element = null;
                String RxDrops2String = null;
                Double RxDrops2DBL = null;

                Element TxHCBytesElement = null;
                String TxHCBytesString = null;
                Double TxHCBytesDBL = null;
                Element TxHCBytes2Element = null;
                String TxHCBytes2String = null;
                Double TxHCBytes2DBL = null;

                Element TxHCPacketsElement = null;
                String TxHCPacketsString = null;
                Double TxHCPacketsDBL = null;
                Element TxHCPackets2Element = null;
                String TxHCPackets2String = null;
                Double TxHCPackets2DBL = null;

                Element TxErrorsElement = null;
                String TxErrorsString = null;
                Double TxErrorsDBL = null;
                Element TxErrors2Element = null;
                String TxErrors2String = null;
                Double TxErrors2DBL = null;

                Element TxDropsElement = null;
                String TxDropsString = null;
                Double TxDropsDBL = null;
                Element TxDrops2Element = null;
                String TxDrops2String = null;
                Double TxDrops2DBL = null;

                NodeList InterfaceStatusList = element.getElementsByTagName("Status");
                if (InterfaceStatusList.getLength() > 0) {
                    InterfaceStatusElement = (Element) InterfaceStatusList.item(0);
                    InterfaceStatusString = InterfaceStatusElement.getTextContent();
                    InterfaceStatusInt = (InterfaceStatusString.equalsIgnoreCase("ok")) ? 1 : 0;
                }

                NodeList CollisionList = element.getElementsByTagName("Collisions");
                if (CollisionList.getLength() > 0) {
                    CollisionsElement = (Element) CollisionList.item(0);
                    CollisionsString = CollisionsElement.getTextContent();
                    CollisionsDBL = Double.parseDouble(CollisionsString);
                }

                NodeList Collisions2List = element.getElementsByTagName("Collisions2");
                if (Collisions2List.getLength() > 0) {
                    Collisions2Element = (Element) Collisions2List.item(0);
                    Collisions2String = Collisions2Element.getTextContent();
                    Collisions2DBL = Double.parseDouble(Collisions2String);
                }

                NodeList RxHCBytesList = element.getElementsByTagName("RxHCBytes");
                if (RxHCBytesList.getLength() > 0) {
                    RxHCBytesElement = (Element) RxHCBytesList.item(0);
                    RxHCBytesString = RxHCBytesElement.getTextContent();
                    RxHCBytesDBL = Double.parseDouble(RxHCBytesString);
                }

                NodeList RxHCBytes2List = element.getElementsByTagName("RxHCBytes2");
                if (RxHCBytes2List.getLength() > 0) {
                    RxHCBytes2Element = (Element) RxHCBytes2List.item(0);
                    RxHCBytes2String = RxHCBytes2Element.getTextContent();
                    RxHCBytes2DBL = Double.parseDouble(RxHCBytes2String);
                }

                NodeList RxHCPacketsList = element.getElementsByTagName("RxHCPackets");
                if (RxHCPacketsList.getLength() > 0) {
                    RxHCPacketsElement = (Element) RxHCPacketsList.item(0);
                    RxHCPacketsString = RxHCPacketsElement.getTextContent();
                    RxHCPacketsDBL = Double.parseDouble(RxHCPacketsString);
                }

                NodeList RxHCPackets2List = element.getElementsByTagName("RxHCPackets2");
                if (RxHCPackets2List.getLength() > 0) {
                    RxHCPackets2Element = (Element) RxHCPackets2List.item(0);
                    RxHCPackets2String = RxHCPackets2Element.getTextContent();
                    RxHCPackets2DBL = Double.parseDouble(RxHCPackets2String);
                }

                NodeList RxErrorsList = element.getElementsByTagName("RxErrors");
                if (RxErrorsList.getLength() > 0) {
                    RxErrorsElement = (Element) RxErrorsList.item(0);
                    RxErrorsString = RxErrorsElement.getTextContent();
                    RxErrorsDBL = Double.parseDouble(RxErrorsString);
                }

                NodeList RxErrors2List = element.getElementsByTagName("RxErrors2");
                if (RxErrors2List.getLength() > 0) {
                    RxErrors2Element = (Element) RxErrors2List.item(0);
                    RxErrors2String = RxErrors2Element.getTextContent();
                    RxErrors2DBL = Double.parseDouble(RxErrors2String);
                }

                NodeList RxDropsList = element.getElementsByTagName("RxDrops");
                if (RxDropsList.getLength() > 0) {
                    RxDropsElement = (Element) RxDropsList.item(0);
                    RxDropsString = RxDropsElement.getTextContent();
                    RxDropsDBL = Double.parseDouble(RxDropsString);
                }

                NodeList RxDrops2List = element.getElementsByTagName("RxDrops2");
                if (RxDrops2List.getLength() > 0) {
                    RxDrops2Element = (Element) RxDrops2List.item(0);
                    RxDrops2String = RxDrops2Element.getTextContent();
                    RxDrops2DBL = Double.parseDouble(RxDrops2String);
                }

                NodeList TxHCBytesList = element.getElementsByTagName("TxHCBytes");
                if (TxHCBytesList.getLength() > 0) {
                    TxHCBytesElement = (Element) TxHCBytesList.item(0);
                    TxHCBytesString = TxHCBytesElement.getTextContent();
                    TxHCBytesDBL = Double.parseDouble(TxHCBytesString);
                }

                NodeList TxHCBytes2List = element.getElementsByTagName("TxHCBytes2");
                if (TxHCBytes2List.getLength() > 0) {
                    TxHCBytes2Element = (Element) TxHCBytes2List.item(0);
                    TxHCBytes2String = TxHCBytes2Element.getTextContent();
                    TxHCBytes2DBL = Double.parseDouble(TxHCBytes2String);
                }

                NodeList TxHCPacketsList = element.getElementsByTagName("TxHCPackets");
                if (TxHCPacketsList.getLength() > 0) {
                    TxHCPacketsElement = (Element) TxHCBytesList.item(0);
                    TxHCPacketsString = TxHCPacketsElement.getTextContent();
                    TxHCPacketsDBL = Double.parseDouble(TxHCPacketsString);
                }

                NodeList TxHCPackets2List = element.getElementsByTagName("TxHCPackets2");
                if (TxHCPackets2List.getLength() > 0) {
                    TxHCPackets2Element = (Element) TxHCBytes2List.item(0);
                    TxHCPackets2String = TxHCPackets2Element.getTextContent();
                    TxHCPackets2DBL = Double.parseDouble(TxHCPackets2String);
                }

                NodeList TxErrorsList = element.getElementsByTagName("TxErrors");
                if (TxErrorsList.getLength() > 0) {
                    TxErrorsElement = (Element) TxErrorsList.item(0);
                    TxErrorsString = TxErrorsElement.getTextContent();
                    TxErrorsDBL = Double.parseDouble(TxErrorsString);
                }

                NodeList TxErrors2List = element.getElementsByTagName("TxErrors2");
                if (TxErrors2List.getLength() > 0) {
                    TxErrors2Element = (Element) TxErrors2List.item(0);
                    TxErrors2String = TxErrors2Element.getTextContent();
                    TxErrors2DBL = Double.parseDouble(TxErrors2String);
                }

                NodeList TxDropsList = element.getElementsByTagName("TxDrops");
                if (TxDropsList.getLength() > 0) {
                    TxDropsElement = (Element) TxDropsList.item(0);
                    TxDropsString = TxDropsElement.getTextContent();
                    TxDropsDBL = Double.parseDouble(TxDropsString);
                }

                NodeList TxDrops2List = element.getElementsByTagName("TxDrops2");
                if (TxDrops2List.getLength() > 0) {
                    TxDrops2Element = (Element) TxDrops2List.item(0);
                    TxDrops2String = TxDrops2Element.getTextContent();
                    TxDrops2DBL = Double.parseDouble(TxDrops2String);
                }

                log.log(Level.FINER, "NameString is : {0}", NameString);
                log.log(Level.FINER, "IPString is : {0}", IPString);
                log.log(Level.FINER, "InterfaceStatusInt is : {0}", InterfaceStatusInt);
                log.log(Level.FINER, "CollisionDBL is : {0}", CollisionsDBL);
                log.log(Level.FINER, "Collision2DBL is : {0}", Collisions2DBL);
                log.log(Level.FINER, "RxHCBytesDBL is : {0}", RxHCBytesDBL);
                log.log(Level.FINER, "RxHCBytes2DBL is : {0}", RxHCBytes2DBL);
                log.log(Level.FINER, "RxHCPacketsDBL is : {0}", RxHCPacketsDBL);
                log.log(Level.FINER, "RxHCPackets2DBL is : {0}", RxHCPackets2DBL);
                log.log(Level.FINER, "RxErrorsDBL is : {0}", RxErrorsDBL);
                log.log(Level.FINER, "RxErrors2DBL is : {0}", RxErrors2DBL);
                log.log(Level.FINER, "RxDropsDBL is : {0}", RxDropsDBL);
                log.log(Level.FINER, "RxDrops2DBL is : {0}", RxDrops2DBL);
                log.log(Level.FINER, "TxHCBytesDBL is : {0}", TxHCBytesDBL);
                log.log(Level.FINER, "TxHCBytes2DBL is : {0}", TxHCBytes2DBL);
                log.log(Level.FINER, "TxHCPacketsDBL is : {0}", TxHCPacketsDBL);
                log.log(Level.FINER, "TxHCPackets2DBL is : {0}", TxHCPackets2DBL);
                log.log(Level.FINER, "TxErrorsDBL is : {0}", TxErrorsDBL);
                log.log(Level.FINER, "TxErrors2DBL is : {0}", TxErrors2DBL);
                log.log(Level.FINER, "TxDropsDBL is : {0}", TxDropsDBL);
                log.log(Level.FINER, "TxDrops2DBL is : {0}", TxDrops2DBL);

                if ((measures = env.getMonitorMeasures(ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_STATUS)) != null) {
                    for (MonitorMeasure measure : measures) {
                        dynamicMeasure = env.createDynamicMeasure(measure, "Interface", InterfaceName);
                        dynamicMeasure.setValue(InterfaceStatusInt);
                    }
                }

                if (CollisionList.getLength() > 0) {
                    if ((measures = env.getMonitorMeasures(ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_COLLISIONS)) != null) {
                        for (MonitorMeasure measure : measures) {
                            dynamicMeasure = env.createDynamicMeasure(measure, "Interface Name", InterfaceName);
                            dynamicMeasure.setValue(CollisionsDBL);
                        }
                    }
                }

                if (Collisions2List.getLength() > 0) {
                    if ((measures = env.getMonitorMeasures(ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_COLLISIONS2)) != null) {
                        for (MonitorMeasure measure : measures) {
                            dynamicMeasure = env.createDynamicMeasure(measure, "Interface Name", InterfaceName);
                            dynamicMeasure.setValue(Collisions2DBL);
                        }
                    }
                }
                if (RxHCBytesList.getLength() > 0) {
                    if ((measures = env.getMonitorMeasures(ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_RXHCBYTES)) != null) {
                        for (MonitorMeasure measure : measures) {
                            dynamicMeasure = env.createDynamicMeasure(measure, "Interface Name", InterfaceName);
                            dynamicMeasure.setValue(RxHCBytesDBL);
                        }
                    }
                }
                if (RxHCBytes2List.getLength() > 0) {
                    if ((measures = env.getMonitorMeasures(ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_RXHCBYTES2)) != null) {
                        for (MonitorMeasure measure : measures) {
                            dynamicMeasure = env.createDynamicMeasure(measure, "Interface Name", InterfaceName);
                            dynamicMeasure.setValue(RxHCBytes2DBL);
                        }
                    }
                }

                if (RxHCPacketsList.getLength() > 0) {
                    if ((measures = env.getMonitorMeasures(ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_RXHCPACKETS)) != null) {
                        for (MonitorMeasure measure : measures) {
                            dynamicMeasure = env.createDynamicMeasure(measure, "Interface Name", InterfaceName);
                            dynamicMeasure.setValue(RxHCPacketsDBL);
                        }
                    }
                }

                if (RxHCPackets2List.getLength() > 0) {
                    if ((measures = env.getMonitorMeasures(ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_RXHCPACKETS2)) != null) {
                        for (MonitorMeasure measure : measures) {
                            dynamicMeasure = env.createDynamicMeasure(measure, "Interface Name", InterfaceName);
                            dynamicMeasure.setValue(RxHCPackets2DBL);
                        }
                    }
                }

                if (RxErrorsList.getLength() > 0) {
                    if ((measures = env.getMonitorMeasures(ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_RXERRORS)) != null) {
                        for (MonitorMeasure measure : measures) {
                            dynamicMeasure = env.createDynamicMeasure(measure, "Interface Name", InterfaceName);
                            dynamicMeasure.setValue(RxErrorsDBL);
                        }
                    }
                }

                if (RxErrors2List.getLength() > 0) {
                    if ((measures = env.getMonitorMeasures(ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_RXERRORS2)) != null) {
                        for (MonitorMeasure measure : measures) {
                            dynamicMeasure = env.createDynamicMeasure(measure, "Interface Name", InterfaceName);
                            dynamicMeasure.setValue(RxErrors2DBL);
                        }
                    }
                }

                if (RxDropsList.getLength() > 0) {
                    if ((measures = env.getMonitorMeasures(ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_RXDROPS)) != null) {
                        for (MonitorMeasure measure : measures) {
                            dynamicMeasure = env.createDynamicMeasure(measure, "Interface Name", InterfaceName);
                            dynamicMeasure.setValue(RxDropsDBL);
                        }
                    }
                }

                if (RxDrops2List.getLength() > 0) {
                    if ((measures = env.getMonitorMeasures(ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_RXDROPS2)) != null) {
                        for (MonitorMeasure measure : measures) {
                            dynamicMeasure = env.createDynamicMeasure(measure, "Interface Name", InterfaceName);
                            dynamicMeasure.setValue(RxDrops2DBL);
                        }
                    }
                }

                if (TxHCBytesList.getLength() > 0) {
                    if ((measures = env.getMonitorMeasures(ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_TXHCBYTES)) != null) {
                        for (MonitorMeasure measure : measures) {
                            dynamicMeasure = env.createDynamicMeasure(measure, "Interface Name", InterfaceName);
                            dynamicMeasure.setValue(TxHCBytesDBL);
                        }
                    }
                }

                if (TxHCBytes2List.getLength() > 0) {
                    if ((measures = env.getMonitorMeasures(ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_TXHCBYTES2)) != null) {
                        for (MonitorMeasure measure : measures) {
                            dynamicMeasure = env.createDynamicMeasure(measure, "Interface Name", InterfaceName);
                            dynamicMeasure.setValue(TxHCBytes2DBL);
                        }
                    }
                }

                if (TxHCPacketsList.getLength() > 0) {
                    if ((measures = env.getMonitorMeasures(ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_TXHCPACKETS)) != null) {
                        for (MonitorMeasure measure : measures) {
                            dynamicMeasure = env.createDynamicMeasure(measure, "Interface Name", InterfaceName);
                            dynamicMeasure.setValue(TxHCPacketsDBL);
                        }
                    }
                }

                if (TxHCPackets2List.getLength() > 0) {
                    if ((measures = env.getMonitorMeasures(ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_TXHCPACKETS2)) != null) {
                        for (MonitorMeasure measure : measures) {
                            dynamicMeasure = env.createDynamicMeasure(measure, "Interface Name", InterfaceName);
                            dynamicMeasure.setValue(TxHCPackets2DBL);
                        }
                    }
                }

                if (TxErrorsList.getLength() > 0) {
                    if ((measures = env.getMonitorMeasures(ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_TXERRORS)) != null) {
                        for (MonitorMeasure measure : measures) {
                            dynamicMeasure = env.createDynamicMeasure(measure, "Interface Name", InterfaceName);
                            dynamicMeasure.setValue(TxErrorsDBL);
                        }
                    }
                }

                if (TxErrors2List.getLength() > 0) {
                    if ((measures = env.getMonitorMeasures(ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_TXERRORS2)) != null) {
                        for (MonitorMeasure measure : measures) {
                            dynamicMeasure = env.createDynamicMeasure(measure, "Interface Name", InterfaceName);
                            dynamicMeasure.setValue(TxErrors2DBL);
                        }
                    }
                }

                if (TxDropsList.getLength() > 0) {
                    if ((measures = env.getMonitorMeasures(ETHERNETINTERFACESTATUS_METRIC_GROUP, MSR_ETHERNETINTERFACESTATUS_TXDROPS)) != null) {
                        for (MonitorMeasure measure : measures) {
                            dynamicMeasure = env.createDynamicMeasure(measure, "Interface Name", InterfaceName);
                            dynamicMeasure.setValue(TxDropsDBL);
                        }
                    }
                }

            }
        } else {
            status.setMessage("SOMA call unsuccessful: EthernetInterfaceStatus response is null");
            status.setStatusCode(Status.StatusCode.ErrorInternal);
        }

        return status;
    }

    private Status executeObjectStatus(MonitorEnvironment env) {

        URI testURI = null;
        /*try {
            testURI = new URI("http://localhost:8080/JMXTestServletApp/ObjectStatus.xml");
        } catch (URIException ex) {
            Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }*/

        if (testURI != null) {
            log.log(Level.INFO, "Populating Object Status Measures for {0} ->{1}", new Object[]{env.getHost(), testURI});
        } else {
            log.log(Level.INFO, "Populating Object Status Measures for {0} ->{1}", new Object[]{env.getHost(), config.url});
        }

        Document doc = null;
        NodeList measureList = null;

        status = new Status();

        String ObjectStatusResponse = callDPSOMAMethod(env, "ObjectStatus", testURI);

        log.log(Level.FINE, "The ObjectStatus response string is : {0}", ObjectStatusResponse);

        if (ObjectStatusResponse != null) {

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = null;
            try {
                docBuilder = docBuilderFactory.newDocumentBuilder();
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }

            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(ObjectStatusResponse));
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

            measureList = doc.getElementsByTagName("ObjectStatus"); //This is the node list of all the ObjectStatus

            Collection<MonitorMeasure> measures = null;
            MonitorMeasure dynamicMeasure;

            for (int i = 0; i < measureList.getLength(); i++) {
                Element element = (Element) measureList.item(i); //This is the ith node in that node list

                String elementPrint = element.getTextContent();
                log.fine("elementPrint_" + i + " is : " + elementPrint);

                NodeList ClassList = element.getElementsByTagName("Class");
                Element ClassElement = (Element) ClassList.item(0);
                String ClassString = ClassElement.getTextContent();

                NodeList NameList = element.getElementsByTagName("Name");
                Element NameElement = (Element) NameList.item(0);
                String NameString = NameElement.getTextContent();

                String ClassName = ClassString + "_" + NameString;

                NodeList OpStateList = element.getElementsByTagName("OpState");
                Element OpStateElement = (Element) OpStateList.item(0);
                String OpStateString = OpStateElement.getTextContent();
                int OpStateInt = (OpStateString.equalsIgnoreCase("up")) ? 1 : 0;

                NodeList AdminStateList = element.getElementsByTagName("AdminState");
                Element AdminStateElement = (Element) AdminStateList.item(0);
                String AdminStateString = AdminStateElement.getTextContent();
                int AdminStateInt = (AdminStateString.equalsIgnoreCase("enabled")) ? 1 : 0;

                log.finer("ClassString is : " + ClassString);
                log.finer("NameString is : " + NameString);
                log.finer("ClassName is : " + ClassName);
                log.finer("OpStateInt is : " + OpStateInt);
                log.finer("AdminStateInt is : " + AdminStateInt);

                if ((measures = env.getMonitorMeasures(OBJECTSTATUS_METRIC_GROUP, MSR_OBJECTSTATUS_OPSTATE)) != null) {
                    for (MonitorMeasure measure : measures) {
                        dynamicMeasure = env.createDynamicMeasure(measure, "Object Name", ClassName);
                        dynamicMeasure.setValue(OpStateInt);
                    }
                }

                if ((measures = env.getMonitorMeasures(OBJECTSTATUS_METRIC_GROUP, MSR_OBJECTSTATUS_ADMINSTATE)) != null) {
                    for (MonitorMeasure measure : measures) {
                        dynamicMeasure = env.createDynamicMeasure(measure, "Object Name", ClassName);
                        dynamicMeasure.setValue(AdminStateInt);
                    }
                }
            }
        } else {
            status.setMessage("SOMA call unsuccessful: ObjectStatus response is null");
            status.setStatusCode(Status.StatusCode.ErrorInternal);
        }

        return status;
    }

    private Status executeSystemUsage(MonitorEnvironment env) {

        URI testURI = null;
        /*try {
            testURI = new URI("http://localhost:8080/JMXTestServletApp/SystemUsage.xml");
        } catch (URIException ex) {
            Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }*/

        if (testURI != null) {
            log.log(Level.INFO, "Populating System Usage Measures for {0} ->{1}", new Object[]{env.getHost(), testURI});
        } else {
            log.log(Level.INFO, "Populating System Usage Measures for {0} ->{1}", new Object[]{env.getHost(), config.url});
        }

        Document doc = null;
        NodeList measureList = null;

        status = new Status();

        String SystemUsageResponse = callDPSOMAMethod(env, "SystemUsage", testURI);

        log.log(Level.FINE, "The response string is {0}", SystemUsageResponse);

        if (SystemUsageResponse != null) {

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = null;
            try {
                docBuilder = docBuilderFactory.newDocumentBuilder();
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }

            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(SystemUsageResponse));
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

            measureList = doc.getElementsByTagName("SystemUsage");

            Collection<MonitorMeasure> measures = null;

            for (int i = 0; i < measureList.getLength(); i++) {
                Element element = (Element) measureList.item(i);

                addDoubleMeasure(env, element, SYSTEMUSAGE_METRIC_GROUP, MSR_SYSTEMUSAGE_INTERVAL, "Interval");
                addDoubleMeasure(env, element, SYSTEMUSAGE_METRIC_GROUP, MSR_SYSTEMUSAGE_LOAD, "Load");
                addDoubleMeasure(env, element, SYSTEMUSAGE_METRIC_GROUP, MSR_SYSTEMUSAGE_WORKLIST, "WorkList");

            }
        } else {
            status.setMessage("SOMA call unsuccessful: SystemUsage response is null");
            status.setStatusCode(Status.StatusCode.ErrorInternal);
        }

        return status;
    }

    private Status executeTCPSummary(MonitorEnvironment env) {

        URI testURI = null;
        /*try {
            testURI = new URI("http://localhost:8080/JMXTestServletApp/TCPSummary.xml");
        } catch (URIException ex) {
            Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }*/

        if (testURI != null) {
            log.log(Level.INFO, "Populating TCP Summary Measures for {0} ->{1}", new Object[]{env.getHost(), testURI});
        } else {
            log.log(Level.INFO, "Populating TCP Summary Measures for {0} ->{1}", new Object[]{env.getHost(), config.url});
        }

        Document doc = null;
        NodeList measureList = null;

        status = new Status();

        String TCPSummaryResponse = callDPSOMAMethod(env, "TCPSummary", testURI);

        log.log(Level.FINE, "The response string is {0}", TCPSummaryResponse);

        if (TCPSummaryResponse != null) {

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = null;
            try {
                docBuilder = docBuilderFactory.newDocumentBuilder();
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }

            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(TCPSummaryResponse));
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

            measureList = doc.getElementsByTagName("TCPSummary");

            Collection<MonitorMeasure> measures = null;

            for (int i = 0; i < measureList.getLength(); i++) {
                Element element = (Element) measureList.item(i);

                addDoubleMeasure(env, element, TCPSUMMARY_METRIC_GROUP, MSR_TCPSUMMARY_ESTABLISHED, "established");
                addDoubleMeasure(env, element, TCPSUMMARY_METRIC_GROUP, MSR_TCPSUMMARY_SYN_SENT, "syn_sent");
                addDoubleMeasure(env, element, TCPSUMMARY_METRIC_GROUP, MSR_TCPSUMMARY_SYN_RECEIVED, "syn_received");
                addDoubleMeasure(env, element, TCPSUMMARY_METRIC_GROUP, MSR_TCPSUMMARY_FIN_WAIT_1, "fin_wait_1");
                addDoubleMeasure(env, element, TCPSUMMARY_METRIC_GROUP, MSR_TCPSUMMARY_FIN_WAIT_2, "fin_wait_2");
                addDoubleMeasure(env, element, TCPSUMMARY_METRIC_GROUP, MSR_TCPSUMMARY_TIME_WAIT, "time_wait");
                addDoubleMeasure(env, element, TCPSUMMARY_METRIC_GROUP, MSR_TCPSUMMARY_CLOSED, "closed");
                addDoubleMeasure(env, element, TCPSUMMARY_METRIC_GROUP, MSR_TCPSUMMARY_CLOSE_WAIT, "close_wait");
                addDoubleMeasure(env, element, TCPSUMMARY_METRIC_GROUP, MSR_TCPSUMMARY_LAST_ACK, "last_ack");
                addDoubleMeasure(env, element, TCPSUMMARY_METRIC_GROUP, MSR_TCPSUMMARY_LISTEN, "listen");
                addDoubleMeasure(env, element, TCPSUMMARY_METRIC_GROUP, MSR_TCPSUMMARY_CLOSING, "closing");

            }
        } else {
            status.setMessage("SOMA call unsuccessful: TCPSummary response is null");
            status.setStatusCode(Status.StatusCode.ErrorInternal);
        }

        return status;
    }

    private Status executeStylesheetExecutions(MonitorEnvironment env) {

        URI testURI = null;
        /*try {
            testURI = new URI("http://localhost:8080/JMXTestServletApp/StyleSheetExecutions.xml");
        } catch (URIException ex) {
            Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }*/

        if (testURI != null) {
            log.log(Level.INFO, "Populating Stylesheet Executions Measures for {0} ->{1}", new Object[]{env.getHost(), testURI});
        } else {
            log.log(Level.INFO, "Populating Stylesheet Executions Measures for {0} ->{1}", new Object[]{env.getHost(), config.url});
        }

        Document doc = null;
        NodeList measureList = null;

        status = new Status();

        String StylesheetExecutionsResponse = callDPSOMAMethod(env, "StylesheetExecutions", testURI);

        //change back to fine
        log.log(Level.FINE, "The StylesheetExecutions response string is : {0}", StylesheetExecutionsResponse);

        if (StylesheetExecutionsResponse != null) {

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = null;
            try {
                docBuilder = docBuilderFactory.newDocumentBuilder();
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }

            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(StylesheetExecutionsResponse));
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

            measureList = doc.getElementsByTagName("StylesheetExecutions"); //This is the node list of all the ObjectStatus

            Collection<MonitorMeasure> measures = null;
            MonitorMeasure dynamicMeasure;

            for (int i = 0; i < measureList.getLength(); i++) {
                Element element = (Element) measureList.item(i); //This is the ith node in that node list

                String elementPrint = element.getTextContent();

                //change back to fine
                log.fine("elementPrint_" + i + " is : " + elementPrint);

                NodeList URLList = element.getElementsByTagName("URL");
                Element URLElement = (Element) URLList.item(0);
                String URLString = URLElement.getTextContent();

                NodeList tenSecondsList = element.getElementsByTagName("tenSeconds");
                Element tenSecondsElement = (Element) tenSecondsList.item(0);
                int tenSecondsInt = Integer.parseInt(tenSecondsElement.getTextContent());

                NodeList oneMinuteList = element.getElementsByTagName("oneMinute");
                Element oneMinuteElement = (Element) oneMinuteList.item(0);
                int oneMinuteInt = Integer.parseInt(oneMinuteElement.getTextContent());

                NodeList tenMinutesList = element.getElementsByTagName("tenMinutes");
                Element tenMinutesElement = (Element) tenMinutesList.item(0);
                int tenMinutesInt = Integer.parseInt(tenMinutesElement.getTextContent());

                NodeList oneHourList = element.getElementsByTagName("oneHour");
                Element oneHourElement = (Element) oneHourList.item(0);
                int oneHourInt = Integer.parseInt(oneHourElement.getTextContent());

                NodeList oneDayList = element.getElementsByTagName("oneDay");
                Element oneDayElement = (Element) oneDayList.item(0);
                int oneDayInt = Integer.parseInt(oneDayElement.getTextContent());

                //change back to finer
                log.finer("URLString is : " + URLString);
                log.finer("tenSecondsInt is : " + tenSecondsInt);
                log.finer("oneMinuteInt is : " + oneMinuteInt);
                log.finer("tenMinutesInt is : " + tenMinutesInt);
                log.finer("oneHourInt is : " + oneHourInt);
                log.finer("oneDayInt is : " + oneDayInt);

                if ((measures = env.getMonitorMeasures(STYLESHEETEXECUTIONS_METRIC_GROUP, MSR_STYLESHEETEXECUTIONS_TENSECONDS)) != null) {
                    for (MonitorMeasure measure : measures) {
                        dynamicMeasure = env.createDynamicMeasure(measure, "Stylesheet URL", URLString);
                        dynamicMeasure.setValue(tenSecondsInt);
                    }
                }

                if ((measures = env.getMonitorMeasures(STYLESHEETEXECUTIONS_METRIC_GROUP, MSR_STYLESHEETEXECUTIONS_ONEMINUTE)) != null) {
                    for (MonitorMeasure measure : measures) {
                        dynamicMeasure = env.createDynamicMeasure(measure, "Stylesheet URL", URLString);
                        dynamicMeasure.setValue(oneMinuteInt);
                    }
                }

                if ((measures = env.getMonitorMeasures(STYLESHEETEXECUTIONS_METRIC_GROUP, MSR_STYLESHEETEXECUTIONS_TENMINUTES)) != null) {
                    for (MonitorMeasure measure : measures) {
                        dynamicMeasure = env.createDynamicMeasure(measure, "Stylesheet URL", URLString);
                        dynamicMeasure.setValue(tenMinutesInt);
                    }
                }

                if ((measures = env.getMonitorMeasures(STYLESHEETEXECUTIONS_METRIC_GROUP, MSR_STYLESHEETEXECUTIONS_ONEHOUR)) != null) {
                    for (MonitorMeasure measure : measures) {
                        dynamicMeasure = env.createDynamicMeasure(measure, "Stylesheet URL", URLString);
                        dynamicMeasure.setValue(oneHourInt);
                    }
                }

                if ((measures = env.getMonitorMeasures(STYLESHEETEXECUTIONS_METRIC_GROUP, MSR_STYLESHEETEXECUTIONS_ONEDAY)) != null) {
                    for (MonitorMeasure measure : measures) {
                        dynamicMeasure = env.createDynamicMeasure(measure, "Stylesheet URL", URLString);
                        dynamicMeasure.setValue(oneDayInt);
                    }
                }

            }
        } else {
            status.setMessage("SOMA call unsuccessful: StylesheetExecutions response is null");
            status.setStatusCode(Status.StatusCode.ErrorInternal);
        }

        return status;
    }

    private Status executeDomainStatus(MonitorEnvironment env) {

        URI testURI = null;
        /*try {
            testURI = new URI("http://localhost:8080/JMXTestServletApp/DomainStatus.xml");
        } catch (URIException ex) {
            Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }*/

        if (testURI != null) {
            log.log(Level.INFO, "Populating Domain Status Measures for {0} ->{1}", new Object[]{env.getHost(), testURI});
        } else {
            log.log(Level.INFO, "Populating Domain Status Measures for {0} ->{1}", new Object[]{env.getHost(), config.url});
        }

        Document doc = null;
        NodeList measureList = null;

        status = new Status();

        String DomainStatusResponse = callDPSOMAMethod(env, "DomainStatus", testURI);

        log.fine("The DomainStatus response string is : " + DomainStatusResponse);

        if (DomainStatusResponse != null) {

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = null;
            try {
                docBuilder = docBuilderFactory.newDocumentBuilder();
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }

            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(DomainStatusResponse));
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

            measureList = doc.getElementsByTagName("DomainStatus"); //This is the node list of all the ObjectStatus

            Collection<MonitorMeasure> measures = null;
            MonitorMeasure dynamicMeasure;

            for (int i = 0; i < measureList.getLength(); i++) {
                Element element = (Element) measureList.item(i); //This is the ith node in that node list

                String elementPrint = element.getTextContent();
                log.fine("elementPrint_" + i + " is : " + elementPrint);

                NodeList DomainList = element.getElementsByTagName("Domain");
                Element DomainElement = (Element) DomainList.item(0);
                String DomainString = DomainElement.getTextContent();

                NodeList SaveNeededList = element.getElementsByTagName("SaveNeeded");
                Element SaveNeededElement = (Element) SaveNeededList.item(0);
                String SaveNeededString = SaveNeededElement.getTextContent();
                int SaveNeededInt = (SaveNeededString.equalsIgnoreCase("on")) ? 1 : 0;

                NodeList TraceEnabledList = element.getElementsByTagName("TraceEnabled");
                Element TraceEnabledElement = (Element) TraceEnabledList.item(0);
                String TraceEnabledString = TraceEnabledElement.getTextContent();
                int TraceEnabledInt = (TraceEnabledString.equalsIgnoreCase("on")) ? 1 : 0;

                NodeList DebugEnabledList = element.getElementsByTagName("DebugEnabled");
                Element DebugEnabledElement = (Element) DebugEnabledList.item(0);
                String DebugEnabledString = DebugEnabledElement.getTextContent();
                int DebugEnabledInt = (DebugEnabledString.equalsIgnoreCase("on")) ? 1 : 0;

                NodeList ProbeEnabledList = element.getElementsByTagName("ProbeEnabled");
                Element ProbeEnabledElement = (Element) ProbeEnabledList.item(0);
                String ProbeEnabledString = ProbeEnabledElement.getTextContent();
                int ProbeEnabledInt = (ProbeEnabledString.equalsIgnoreCase("on")) ? 1 : 0;

                NodeList DiagEnabledList = element.getElementsByTagName("DiagEnabled");
                Element DiagEnabledElement = (Element) DiagEnabledList.item(0);
                String DiagEnabledString = DiagEnabledElement.getTextContent();
                int DiagEnabledInt = (DiagEnabledString.equalsIgnoreCase("on")) ? 1 : 0;

                log.finer("DomainString is : " + DomainString);
                log.finer("SaveNeededInt is : " + SaveNeededInt);
                log.finer("TraceEnabledInt is : " + TraceEnabledInt);
                log.finer("DebugEnabledInt is : " + DebugEnabledInt);
                log.finer("ProbeEnabledInt is : " + ProbeEnabledInt);
                log.finer("DiagEnabledInt is : " + DiagEnabledInt);

                if ((measures = env.getMonitorMeasures(DOMAINSTATUS_METRIC_GROUP, MSR_DOMAINSTATUS_SAVENEEDED)) != null) {
                    for (MonitorMeasure measure : measures) {
                        dynamicMeasure = env.createDynamicMeasure(measure, "Domain Name", DomainString);
                        dynamicMeasure.setValue(SaveNeededInt);
                    }
                }

                if ((measures = env.getMonitorMeasures(DOMAINSTATUS_METRIC_GROUP, MSR_DOMAINSTATUS_TRACEENABLED)) != null) {
                    for (MonitorMeasure measure : measures) {
                        dynamicMeasure = env.createDynamicMeasure(measure, "Domain Name", DomainString);
                        dynamicMeasure.setValue(TraceEnabledInt);
                    }
                }

                if ((measures = env.getMonitorMeasures(DOMAINSTATUS_METRIC_GROUP, MSR_DOMAINSTATUS_DEBUGENABLED)) != null) {
                    for (MonitorMeasure measure : measures) {
                        dynamicMeasure = env.createDynamicMeasure(measure, "Domain Name", DomainString);
                        dynamicMeasure.setValue(DebugEnabledInt);
                    }
                }

                if ((measures = env.getMonitorMeasures(DOMAINSTATUS_METRIC_GROUP, MSR_DOMAINSTATUS_PROBEENABLED)) != null) {
                    for (MonitorMeasure measure : measures) {
                        dynamicMeasure = env.createDynamicMeasure(measure, "Domain Name", DomainString);
                        dynamicMeasure.setValue(ProbeEnabledInt);
                    }
                }

                if ((measures = env.getMonitorMeasures(DOMAINSTATUS_METRIC_GROUP, MSR_DOMAINSTATUS_DIAGENABLED)) != null) {
                    for (MonitorMeasure measure : measures) {
                        dynamicMeasure = env.createDynamicMeasure(measure, "Domain Name", DomainString);
                        dynamicMeasure.setValue(DiagEnabledInt);
                    }
                }

            }
        } else {
            status.setMessage("SOMA call unsuccessful: DomainStatus response is null");
            status.setStatusCode(Status.StatusCode.ErrorInternal);
        }

        return status;
    }

    private Status executeHTTPTransactions(MonitorEnvironment env) {

        URI testURI = null;
        /*try {
            testURI = new URI("http://localhost:8080/JMXTestServletApp/HTTPTransactions.xml");
        } catch (URIException ex) {
            Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }*/

        if (testURI != null) {
            log.log(Level.INFO, "Populating HTTP Transactions Measures for {0} ->{1}", new Object[]{env.getHost(), testURI});
        } else {
            log.log(Level.INFO, "Populating HTTP Transactions Measures for {0} ->{1}", new Object[]{env.getHost(), config.url});
        }

        Document doc = null;
        NodeList measureList = null;

        status = new Status();

        String HTTPTransactionsResponse = callDPSOMAMethod(env, "HTTPTransactions", testURI);

        log.fine("The HTTPTransactionsResponse response string is : " + HTTPTransactionsResponse);

        if (HTTPTransactionsResponse != null) {

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = null;
            try {
                docBuilder = docBuilderFactory.newDocumentBuilder();
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }

            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(HTTPTransactionsResponse));
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

            measureList = doc.getElementsByTagName("HTTPTransactions"); //This is the node list of all the ObjectStatus

            Collection<MonitorMeasure> measures = null;
            MonitorMeasure dynamicMeasure;

            for (int i = 0; i < measureList.getLength(); i++) {
                Element element = (Element) measureList.item(i); //This is the ith node in that node list

                String elementPrint = element.getTextContent();
                log.fine("elementPrint_" + i + " is : " + elementPrint);

                NodeList ProxyList = element.getElementsByTagName("proxy");
                Element ProxyElement = (Element) ProxyList.item(0);
                String ProxyString = ProxyElement.getTextContent();

                NodeList tenSecondsList = element.getElementsByTagName("tenSeconds");
                Element tenSecondsElement = (Element) tenSecondsList.item(0);
                int tenSecondsInt = Integer.parseInt(tenSecondsElement.getTextContent());

                NodeList oneMinuteList = element.getElementsByTagName("oneMinute");
                Element oneMinuteElement = (Element) oneMinuteList.item(0);
                int oneMinuteInt = Integer.parseInt(oneMinuteElement.getTextContent());

                NodeList tenMinutesList = element.getElementsByTagName("tenMinutes");
                Element tenMinutesElement = (Element) tenMinutesList.item(0);
                int tenMinutesInt = Integer.parseInt(tenMinutesElement.getTextContent());

                NodeList oneHourList = element.getElementsByTagName("oneHour");
                Element oneHourElement = (Element) oneHourList.item(0);
                int oneHourInt = Integer.parseInt(oneHourElement.getTextContent());

                NodeList oneDayList = element.getElementsByTagName("oneDay");
                Element oneDayElement = (Element) oneDayList.item(0);
                int oneDayInt = Integer.parseInt(oneDayElement.getTextContent());

                log.finer("ProxyString is : " + ProxyString);
                log.finer("tenSecondsInt is : " + tenSecondsInt);
                log.finer("oneMinuteInt is : " + oneMinuteInt);
                log.finer("tenMinutesInt is : " + tenMinutesInt);
                log.finer("oneHourInt is : " + oneHourInt);
                log.finer("oneDayInt is : " + oneDayInt);

                if ((measures = env.getMonitorMeasures(HTTPTRANSACTIONS_METRIC_GROUP, MSR_HTTPTRANSACTIONS_TENSECONDS)) != null) {
                    for (MonitorMeasure measure : measures) {
                        dynamicMeasure = env.createDynamicMeasure(measure, "Proxy", ProxyString);
                        dynamicMeasure.setValue(tenSecondsInt);
                    }
                }

                if ((measures = env.getMonitorMeasures(HTTPTRANSACTIONS_METRIC_GROUP, MSR_HTTPTRANSACTIONS_ONEMINUTE)) != null) {
                    for (MonitorMeasure measure : measures) {
                        dynamicMeasure = env.createDynamicMeasure(measure, "Proxy", ProxyString);
                        dynamicMeasure.setValue(oneMinuteInt);
                    }
                }

                if ((measures = env.getMonitorMeasures(HTTPTRANSACTIONS_METRIC_GROUP, MSR_HTTPTRANSACTIONS_TENMINUTES)) != null) {
                    for (MonitorMeasure measure : measures) {
                        dynamicMeasure = env.createDynamicMeasure(measure, "Proxy", ProxyString);
                        dynamicMeasure.setValue(tenMinutesInt);
                    }
                }

                if ((measures = env.getMonitorMeasures(HTTPTRANSACTIONS_METRIC_GROUP, MSR_HTTPTRANSACTIONS_ONEHOUR)) != null) {
                    for (MonitorMeasure measure : measures) {
                        dynamicMeasure = env.createDynamicMeasure(measure, "Proxy", ProxyString);
                        dynamicMeasure.setValue(oneHourInt);
                    }
                }

                if ((measures = env.getMonitorMeasures(HTTPTRANSACTIONS_METRIC_GROUP, MSR_HTTPTRANSACTIONS_ONEDAY)) != null) {
                    for (MonitorMeasure measure : measures) {
                        dynamicMeasure = env.createDynamicMeasure(measure, "Proxy", ProxyString);
                        dynamicMeasure.setValue(oneDayInt);
                    }
                }

            }
        } else {
            status.setMessage("SOMA call unsuccessful: HTTPTransactions response is null");
            status.setStatusCode(Status.StatusCode.ErrorInternal);
        }

        return status;
    }

    private Status executeHTTPMeanTransactionTime(MonitorEnvironment env) {

        URI testURI = null;
        /*try {
            testURI = new URI("http://localhost:8080/JMXTestServletApp/HTTPMeanTransactionTime.xml");
        } catch (URIException ex) {
            Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }*/

        if (testURI != null) {
            log.log(Level.INFO, "Populating HTTP Mean Transaction Time Measures for {0} ->{1}", new Object[]{env.getHost(), testURI});
        } else {
            log.log(Level.INFO, "Populating HTTP Mean Transaction Time Measures for {0} ->{1}", new Object[]{env.getHost(), config.url});
        }

        Document doc = null;
        NodeList measureList = null;

        status = new Status();

        String HTTPMeanTransactionTimeResponse = callDPSOMAMethod(env, "HTTPMeanTransactionTime", testURI);

        log.fine("The HTTPMeanTransactionTimeResponse response string is : " + HTTPMeanTransactionTimeResponse);

        if (HTTPMeanTransactionTimeResponse != null) {

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = null;
            try {
                docBuilder = docBuilderFactory.newDocumentBuilder();
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }

            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(HTTPMeanTransactionTimeResponse));
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

            measureList = doc.getElementsByTagName("HTTPMeanTransactionTime"); //This is the node list of all the ObjectStatus

            Collection<MonitorMeasure> measures = null;
            MonitorMeasure dynamicMeasure;

            for (int i = 0; i < measureList.getLength(); i++) {
                Element element = (Element) measureList.item(i); //This is the ith node in that node list

                String elementPrint = element.getTextContent();
                log.log(Level.FINE, "elementPrint_{0} is : {1}", new Object[]{i, elementPrint});

                NodeList ProxyList = element.getElementsByTagName("proxy");
                Element ProxyElement = (Element) ProxyList.item(0);
                String ProxyString = ProxyElement.getTextContent();

                NodeList tenSecondsList = element.getElementsByTagName("tenSeconds");
                Element tenSecondsElement = (Element) tenSecondsList.item(0);
                int tenSecondsInt = Integer.parseInt(tenSecondsElement.getTextContent());

                NodeList oneMinuteList = element.getElementsByTagName("oneMinute");
                Element oneMinuteElement = (Element) oneMinuteList.item(0);
                int oneMinuteInt = Integer.parseInt(oneMinuteElement.getTextContent());

                NodeList tenMinutesList = element.getElementsByTagName("tenMinutes");
                Element tenMinutesElement = (Element) tenMinutesList.item(0);
                int tenMinutesInt = Integer.parseInt(tenMinutesElement.getTextContent());

                NodeList oneHourList = element.getElementsByTagName("oneHour");
                Element oneHourElement = (Element) oneHourList.item(0);
                int oneHourInt = Integer.parseInt(oneHourElement.getTextContent());

                NodeList oneDayList = element.getElementsByTagName("oneDay");
                Element oneDayElement = (Element) oneDayList.item(0);
                int oneDayInt = Integer.parseInt(oneDayElement.getTextContent());

                log.log(Level.FINER, "ProxyString is : {0}", ProxyString);
                log.log(Level.FINER, "tenSecondsInt is : {0}", tenSecondsInt);
                log.log(Level.FINER, "oneMinuteInt is : {0}", oneMinuteInt);
                log.log(Level.FINER, "tenMinutesInt is : {0}", tenMinutesInt);
                log.log(Level.FINER, "oneHourInt is : {0}", oneHourInt);
                log.log(Level.FINER, "oneDayInt is : {0}", oneDayInt);

                if ((measures = env.getMonitorMeasures(HTTPMEANTRANSACTIONTIME_METRIC_GROUP, MSR_HTTPMEANTRANSACTIONTIME_TENSECONDS)) != null) {
                    for (MonitorMeasure measure : measures) {
                        dynamicMeasure = env.createDynamicMeasure(measure, "Proxy", ProxyString);
                        dynamicMeasure.setValue(tenSecondsInt);
                    }
                }

                if ((measures = env.getMonitorMeasures(HTTPMEANTRANSACTIONTIME_METRIC_GROUP, MSR_HTTPMEANTRANSACTIONTIME_ONEMINUTE)) != null) {
                    for (MonitorMeasure measure : measures) {
                        dynamicMeasure = env.createDynamicMeasure(measure, "Proxy", ProxyString);
                        dynamicMeasure.setValue(oneMinuteInt);
                    }
                }

                if ((measures = env.getMonitorMeasures(HTTPMEANTRANSACTIONTIME_METRIC_GROUP, MSR_HTTPMEANTRANSACTIONTIME_TENMINUTES)) != null) {
                    for (MonitorMeasure measure : measures) {
                        dynamicMeasure = env.createDynamicMeasure(measure, "Proxy", ProxyString);
                        dynamicMeasure.setValue(tenMinutesInt);
                    }
                }

                if ((measures = env.getMonitorMeasures(HTTPMEANTRANSACTIONTIME_METRIC_GROUP, MSR_HTTPMEANTRANSACTIONTIME_ONEHOUR)) != null) {
                    for (MonitorMeasure measure : measures) {
                        dynamicMeasure = env.createDynamicMeasure(measure, "Proxy", ProxyString);
                        dynamicMeasure.setValue(oneHourInt);
                    }
                }

                if ((measures = env.getMonitorMeasures(HTTPMEANTRANSACTIONTIME_METRIC_GROUP, MSR_HTTPMEANTRANSACTIONTIME_ONEDAY)) != null) {
                    for (MonitorMeasure measure : measures) {
                        dynamicMeasure = env.createDynamicMeasure(measure, "Proxy", ProxyString);
                        dynamicMeasure.setValue(oneDayInt);
                    }
                }

            }
        } else {
            status.setMessage("SOMA call unsuccessful: HTTPMeanTransactionTime response is null");
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

    public String callDPSOMAMethod(MonitorEnvironment env, String SOMAMethod, URI uri) {

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

        if (config.url.getProtocol().equals("https")) {
            try {
                con = new HttpConnection(env.getHost().getAddress(), port, new Protocol("easyhttps", (ProtocolSocketFactory) new EasySSLProtocolSocketFactory(), port));
            } catch (Exception ex) {
                status.setStatusCode(Status.StatusCode.ErrorInternal);
                status.setMessage("Unable to create HttpConnection ...");
                status.setShortMessage(ex == null ? "" : ex.getClass().getSimpleName());
                status.setMessage(ex == null ? "" : ex.getMessage());
                status.setException(ex);
            }
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

        if (uri != null) {
            try {
                httpMethod.setURI(uri);
            } catch (URIException ex) {
                Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

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

            if (config.url.getProtocol().equals("https")) {
                con.open();
                httpStatusCode = httpMethod.execute(myState, con);
            } else {
                httpStatusCode = httpClient.executeMethod(httpMethod);
            }

            firstResponseTime = System.nanoTime() - time;

            if (httpStatusCode < 200 || httpStatusCode > 200) {
                status.setMessage("SOMA call unsuccessful: " + SOMAMethod);
                status.setStatusCode(Status.StatusCode.ErrorInternal);
            } else {
                status.setStatusCode(Status.StatusCode.Success);
            }

        } catch (IOException ex) {
            Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
            log.log(Level.SEVERE, "Setting response code to 503 due to IOException: ", ex);
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
            //log.log(Level.INFO, "Response body for {0}is: {1}", new Object[]{SOMAMethod, response});

        } catch (IOException ex) {
            status.setMessage("SOMA call unsuccessful: " + SOMAMethod);
            status.setStatusCode(Status.StatusCode.ErrorInternal);
            Logger.getLogger(DataPowerMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }

        inputSize = mergedSOAPEnvelope.length();

        if (config.url.getProtocol().equals("https")) {
            con.close();
        }

        return (response);

    }

    private void addDoubleMeasure(MonitorEnvironment env, Element element, String metricGroup, String counter, String tagName) {
        log.log(Level.INFO, "Entering  addDoubleMeasure method: metricGroup is {0}, counter is {1}, tagName is {2}", new Object[]{metricGroup, counter, tagName});
        status = new Status();
        Collection<MonitorMeasure> measures = null;
        Double tagDBL = new Double(0);
        String tagDBLString = null;
        Element tag = null;
        try {
            NodeList Taglist = element.getElementsByTagName(tagName);
            log.log(Level.FINE, "NodeList size for tag: {0} is {1}", new Object[]{tagName, Taglist.getLength()});
            if (Taglist.getLength() > 0) {
                tag = (Element) Taglist.item(0);
                tagDBLString = tag.getTextContent();
                log.log(Level.FINE, " and string value is: {0}", tagDBLString);
                tagDBL = Double.parseDouble(tagDBLString);
                log.log(Level.FINE, "tagDBL is: {0}", tagDBL);

                measures = env.getMonitorMeasures(metricGroup, counter);
                log.log(Level.FINE, "Measure collection size for group {0}and counter {1} is {2}", new Object[]{metricGroup, counter, measures.size()});
                if (measures.size() >= 1) {
                    for (MonitorMeasure measure : measures) {
                        log.log(Level.FINE, "Entering metric group " + metricGroup + ", tag is: " + tag + " and setting value to ", tagDBL);
                        measure.setValue(tagDBL);
                    }
                }
            }

        } catch (Exception e) {
            String errorMsg = "Measure population failed, exception is: " + e + " element is: " + element + " and tagName is: " + tagName + " ";
            status.setMessage(errorMsg);
            status.setStatusCode(Status.StatusCode.PartialSuccess);
            log.log(Level.WARNING, errorMsg);
        }
    }

    private void addDynamicDoubleMeasureWSOperationMetricsSimpleIndex(MonitorEnvironment env, Element element, String metricGroup, MonitorMeasure metric, String tagName) {
        log.log(Level.FINE, "Entering addDynamicDoubleMeasureWSOperationMetricsSimpleIndex method: metricGroup is {0}, metric is {1}, tagName is {2}", new Object[]{metricGroup, metric, tagName});
        status = new Status();
        Collection<MonitorMeasure> measures = null;
        Double tagDBL = new Double(0);
        String ServiceName = null;
        Element ServiceNameTag = null;
        String metricValueString = null;
        Element metricTagElement = null;
        try {

            NodeList Taglist = element.getElementsByTagName("ServiceEndpoint");
            log.log(Level.FINE, "TagList size for tag: {0} is {1}", new Object[]{"ServiceEndpoint", Taglist.getLength()});
            if (Taglist.getLength() > 0) {
                ServiceNameTag = (Element) Taglist.item(0);
                ServiceName = ServiceNameTag.getTextContent();
                log.log(Level.FINE, " and Service Endpoint Name is: {0}", ServiceName);
                //log.log(Level.INFO, "tagDBL is: {0}", tagDBL);

                log.log(Level.FINE, "Creating dynamic measure with subscribedMonitorMeasure {0}, Service Endpoint Name {1}", new Object[]{metric.getMetricName(), ServiceName});
                MonitorMeasure dynamicMeasure = env.createDynamicMeasure(metric, "Service Endpoint Name", ServiceName);

                log.log(Level.FINE, "After create dynamic measure and working on tagName {0}, Service Endpoint Name {1}", new Object[]{tagName, ServiceName});
                NodeList metricList = element.getElementsByTagName(tagName);
                log.log(Level.FINE, "metricList size for tag: {0} is {1}", new Object[]{tagName, Taglist.getLength()});
                if (metricList.getLength() > 0) {
                    metricTagElement = (Element) metricList.item(0);
                    metricValueString = metricTagElement.getTextContent();
                    log.log(Level.FINE, "Metric Value String is {0}", metricValueString);
                    if (metricValueString != null) {
                        tagDBL = Double.parseDouble(metricValueString);
                    } else {
                        log.log(Level.WARNING, "Metric Value String was null {0} setting to 0", metricValueString);
                        tagDBL = 0.0;
                    }

                    log.log(Level.FINE, "Entering metric group " + metricGroup + ", tag is: " + tagName + " and setting value to ", tagDBL);
                    dynamicMeasure.setValue(tagDBL);
                }

            }

        } catch (Exception e) {
            String errorMsg = "Measure population failed, exception is: " + e + " element is: " + element + " and tagName is: " + tagName + " ";
            status.setMessage(errorMsg);
            status.setStatusCode(Status.StatusCode.PartialSuccess);
            log.log(Level.WARNING, errorMsg);
        }
    }

    private void addDoubleMeasureDivideByTen(MonitorEnvironment env, Element element, String metricGroup, String metric, String tagName) {
        log.log(Level.INFO, "Entering addDoubleMeasureDivideByTen method: metricGroup is {0}, counter is {1}, tagName is {2}", new Object[]{metricGroup, metric, tagName});
        status = new Status();
        Collection<MonitorMeasure> measures = null;
        Double tagDBL = new Double(0);
        String tagDBLString = null;
        Element tag = null;
        try {
            NodeList Taglist = element.getElementsByTagName(tagName);
            log.log(Level.FINE, "NodeList size for tag: {0} is {1}", new Object[]{tagName, Taglist.getLength()});
            if (Taglist.getLength() > 0) {
                tag = (Element) Taglist.item(0);
                tagDBLString = tag.getTextContent();
                log.log(Level.FINE, " and string value is: {0}", tagDBLString);
                tagDBL = (Double.parseDouble(tagDBLString)) / 10;
                log.log(Level.FINE, "tagDBL is: {0}", tagDBL);

                measures = env.getMonitorMeasures(metricGroup, metric);
                log.log(Level.FINE, "Measure collection size for group {0}and metric {1} is {2}", new Object[]{metricGroup, metric, measures.size()});
                if (measures.size() >= 1) {
                    for (MonitorMeasure measure : measures) {
                        log.log(Level.FINE, "Entering metric group " + metricGroup + ", tag is: " + tag + " and setting value to ", tagDBL);
                        measure.setValue(tagDBL);
                    }
                }
            }

        } catch (Exception e) {
            String errorMsg = "Measure population failed, exception is: " + e + " element is: " + element + " and tagName is: " + tagName + " ";
            status.setMessage(errorMsg);
            status.setStatusCode(Status.StatusCode.PartialSuccess);
            log.log(Level.WARNING, errorMsg);
        }
    }
}
