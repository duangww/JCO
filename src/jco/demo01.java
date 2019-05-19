package jco;

import java.awt.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoField;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.JCoTable;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.ext.DataProviderException;
import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.conn.jco.ext.Environment;
import com.sap.conn.jco.rt.StaticFunctionTemplates;
import org.json.*;

public class demo01 {

	static class MyDestinationDataProvider implements DestinationDataProvider {

		private DestinationDataEventListener eL;
		private HashMap<String, Properties> destinations;
		private static MyDestinationDataProvider provider = new MyDestinationDataProvider();

		private MyDestinationDataProvider() {// ����ģʽ
			if (provider == null) {
				destinations = new HashMap<String, Properties>();
			}
		}

		public static MyDestinationDataProvider getInstance() {
			return provider;
		}

		// ʵ�ֽӿڣ���ȡ������������

		public Properties getDestinationProperties(String destinationName) {

			if (destinations.containsKey(destinationName)) {

				return destinations.get(destinationName);

			} else {

				throw new RuntimeException("Destination " + destinationName + " is not available");

			}

		}

		public void setDestinationDataEventListener(DestinationDataEventListener eventListener) {
			this.eL = eventListener;
		}

		public boolean supportsEvents() {
			return true;
		}

		/**
		 * 
		 * Add new destination ���������������
		 *
		 * 
		 * 
		 * @param properties
		 * 
		 *                   holds all the required data for a destination
		 * 
		 **/
		void addDestination(String destinationName, Properties properties) {
			synchronized (destinations) {
				destinations.put(destinationName, properties);
			}
		}
	}

	public static void main(String[] args) throws Exception {

		// Test the destination with the name of "ABAP_AS"
		JCoDestination dest = getDestination();
		JCoFunction function = dest.getRepository().getFunction("ZJW_RFC01");// �Ӷ���ֿ��л�ȡ RFM ����

		if (function == null)
			throw new RuntimeException("RFC_SYSTEM_INFO not found in SAP.");
		try {
			// �������
			function.getImportParameterList().setValue("I_ROWS", 10);
			function.execute(dest);
		} catch (AbapException e) {
			System.out.println(e.toString());
			return;
		}

		ArrayList<spfli> ltSpfli = new ArrayList<spfli>();
		JCoTable etSpfli = function.getTableParameterList().getTable("ET_SPFLI");
		spfli lsSpfli = new spfli();
		for (int i = 0; i < etSpfli.getNumRows(); i++) {
			etSpfli.setRow(i);// ����ָ��ָ���ض���������
			lsSpfli.setCARRID(etSpfli.getValue("CARRID").toString());
			lsSpfli.setCONNID(etSpfli.getValue("CONNID").toString());
			lsSpfli.setCOUNTRYFR(etSpfli.getValue("COUNTRYFR").toString());
			lsSpfli.setCITYFROM(etSpfli.getValue("CITYFROM").toString());
			lsSpfli.setAIRPFROM(etSpfli.getValue("AIRPFROM").toString());
			ltSpfli.add(lsSpfli);
		}

		JSONArray json = new JSONArray();
		for (spfli ls : ltSpfli) {
			JSONObject jo = new JSONObject(ls);
			json.put(jo);
		}

		System.out.println(json.toString());

	}

	public static JCoDestination getDestination() throws Exception {

		// ��ȡ����

		MyDestinationDataProvider myProvider = MyDestinationDataProvider.getInstance();

		// Register the MyDestinationDataProvider ����ע��

		Environment.registerDestinationDataProvider(myProvider);

		// TEST 01��ֱ�Ӳ���
		// ABAP_AS is the test destination name ��ABAP_ASΪĿ��������������ֻ���߼��ϵ�������
		String destinationName = "ABAP_AS";
//		System.out.println("Test destination - " + destinationName);
		Properties connectProperties = new Properties();
		connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, "10.6.7.138");
		connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR, "01");
		connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, "100");
		connectProperties.setProperty(DestinationDataProvider.JCO_USER, "ABAP18");
		connectProperties.setProperty(DestinationDataProvider.JCO_PASSWD, "326866");
		connectProperties.setProperty(DestinationDataProvider.JCO_LANG, "zh");

		// Add a destination
		myProvider.addDestination(destinationName, connectProperties);

		// Get a destination with the name of "ABAP_AS"
		JCoDestination dest = JCoDestinationManager.getDestination(destinationName);
		try {
			dest.ping();
//			System.out.println("Destination - " + destinationName + " is ok");

		} catch (Exception ex) {

			ex.printStackTrace();
			System.out.println("Destination - " + destinationName + " is invalid");
		}
		return dest;

	}

}
