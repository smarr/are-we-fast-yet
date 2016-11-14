/*
 * BSD License
 *
 * Copyright (c) 2007, The University of Manchester (UK)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     - Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *     - Neither the name of the University of Manchester nor the names
 *       of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written
 *       permission.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package leetm;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XMLHelper {

	static Document doc;

	static void initializeXMLReport(int numThreads, int experiment, int sampleInterval, String managerClassName, String benchmarkClassName, String adapterClassName) {
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			doc = builder.newDocument();

			Element root = doc.createElement("Statistics");
			doc.appendChild(root);

			Element element = doc.createElement("Benchmark");
			element.setTextContent(benchmarkClassName);
			root.appendChild(element);

			element = doc.createElement("Adapter");
			element.setTextContent(adapterClassName);
			root.appendChild(element);

			element = doc.createElement("ContentionManager");
			element.setTextContent(managerClassName);
			root.appendChild(element);

			element = doc.createElement("Threads");
			element.setTextContent(Integer.toString(numThreads));
			root.appendChild(element);

			element = doc.createElement("Mix");
			element.setTextContent(Integer.toString(experiment));
			root.appendChild(element);

			element = doc.createElement("SampleInterval");
			element.setTextContent(Long.toString(sampleInterval));
			root.appendChild(element);

			String name = System.getProperty("user.name");
			if (name == null)
				name = "";
			element = doc.createElement("Owner");
			element.setTextContent(name);
			root.appendChild(element);

			java.util.Calendar cal = java.util.Calendar
					.getInstance(java.util.TimeZone.getDefault());
			String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
					DATE_FORMAT);
			sdf.setTimeZone(java.util.TimeZone.getDefault());
			element = doc.createElement("Date");
			element.setTextContent(sdf.format(cal.getTime()));
			root.appendChild(element);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	static void generateXMLReportSummary( boolean timeout, boolean xmlreport, double elapsed) throws TransformerFactoryConfigurationError {
		try {
			// get final stats
			LeeRouter.obtainStats(null, elapsed, xmlreport);

			// get benchmark to insert it's output into the doc
			LeeRouter.xmlReport(doc);

			Element root = doc.getDocumentElement();
			Element element = doc.createElement("ElapsedTime");
			element.setTextContent(Double.toString(elapsed));
			root.appendChild(element);

			element = doc.createElement("Timeout");
			element.setTextContent(Boolean.toString(timeout));
			root.appendChild(element);

			// transform the Document into a String
			DOMSource domSource = new DOMSource(doc);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();

			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer
					.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
			transformer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "4");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			StringWriter sw = new StringWriter();
			StreamResult sr = new StreamResult(sw);
			transformer.transform(domSource, sr);
			System.out.println(sw.toString());
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}

	static void generateXMLIntervalSample(double time, long commits, long transactions, long commitMemRefs, long totalMemRefs, String hardware, boolean finalStats) {
		Element element;
		Element root = doc.getDocumentElement();

		Element me;
		if (finalStats)
			me = doc.createElement("FinalSample");
		else
			me = doc.createElement("Sample");
		root.appendChild(me);
		root = me;

		element = doc.createElement("Timestamp");
		element.setTextContent(Double.toString(time));
		root.appendChild(element);

		element = doc.createElement("Hardware");
		element.setTextContent(hardware);
		root.appendChild(element);

		// if (transactions > 0) {
		element = doc.createElement("DataAvailable");
		element.setTextContent("True");
		root.appendChild(element);

		element = doc.createElement("Transactions");
		element.setTextContent(Long.toString(transactions));
		root.appendChild(element);

		element = doc.createElement("Commits");
		element.setTextContent(Long.toString(commits));
		root.appendChild(element);

		String pc;
		if (commits != 0 && transactions != 0) {
			pc = Long.toString(100 * commits / transactions);
		} else {
			pc = "0";
		}
		element = doc.createElement("PercentCommits");
		element.setTextContent(pc);
		root.appendChild(element);

		element = doc.createElement("MemRefs");
		element.setTextContent(Long.toString(totalMemRefs));
		root.appendChild(element);

		element = doc.createElement("CommitMemRefs");
		element.setTextContent(Long.toString(commitMemRefs));
		root.appendChild(element);

		String pm;
		if (commitMemRefs != 0 && totalMemRefs != 0) {
			pm = Long.toString(100 * commitMemRefs / totalMemRefs);
		} else {
			pm = "0";
		}
		element = doc.createElement("PercentCommitMemRefs");
		element.setTextContent(pm);
		root.appendChild(element);
	}


        static void generateXMLIntervalSampleCoarse(double time, long tracks, String hardware, boolean finalStats) {
		Element element;
		Element root = doc.getDocumentElement();

		Element me;
		if (finalStats)
			me = doc.createElement("FinalSample");
		else
			me = doc.createElement("Sample");
		root.appendChild(me);
		root = me;

		element = doc.createElement("Timestamp");
		element.setTextContent(Double.toString(time));
		root.appendChild(element);

		element = doc.createElement("Hardware");
		element.setTextContent(hardware);
		root.appendChild(element);

		// if (transactions > 0) {
		element = doc.createElement("DataAvailable");
		element.setTextContent("True");
		root.appendChild(element);


		element = doc.createElement("LaidTracks");
		element.setTextContent(Long.toString(tracks));
		root.appendChild(element);


	}
}
