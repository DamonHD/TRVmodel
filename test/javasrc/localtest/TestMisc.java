/*
Copyright (c) 2023, Damon Hart-Davis

Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package localtest;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.hd.d.TRVmodel.hg.ShowComputations;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

/**Miscellaneous tests.
 */
public final class TestMisc extends TestCase
    {
    /**Verify that test harness is sane... */
    public static void testSanity() { }

    /**Ensure that showCalcs() can run without crashing! */
    public static void testShowCalcs() throws IOException { ShowComputations.showCalcs(); }

    /**Test HTML table generation basics.
     * <ul>
     * <li>Does it look like a table?</li>
     * <li>Does it parse as (X)HTML, ie XML?</li>
     * </ul>
     */
    public static void testHTMLTableBasics() throws IOException, ParserConfigurationException, SAXException
	    {
	    final String table1 = ShowComputations.generateHTMLMainSummaryTable();
		assertTrue(table1.startsWith("<table"));
	    assertTrue(table1.endsWith("</table>"));
	    final DocumentBuilder builder1 = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	    final Document doc1 = builder1.parse(new InputSource(new StringReader(table1)));
	    assertTrue(doc1.hasChildNodes());

	    final String table2 = ShowComputations.generateHTMLMainFullTable();
		assertTrue(table2.startsWith("<table"));
	    assertTrue(table2.endsWith("</table>"));
	    final DocumentBuilder builder2 = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	    final Document doc2 = builder2.parse(new InputSource(new StringReader(table2)));
	    assertTrue(doc2.hasChildNodes());
	    }
    }
