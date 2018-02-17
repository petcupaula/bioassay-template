/*
 * BioAssay Ontology Annotator Tools
 * 
 * (c) 2014-2016 Collaborative Drug Discovery Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 2.0
 * as published by the Free Software Foundation:
 * 
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.cdd.bao.template;

import com.cdd.testUtil.*;

import static org.junit.Assert.*;

import java.io.*;

import org.junit.*;
import org.junit.rules.*;

/*
	Test for ModelSchema
*/

public class ModelSchemaTest
{
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void testSchemaIO() throws IOException
	{
		TestResourceFile testSchemaFile = new TestResourceFile("/testData/template/schema.ttl");
		File schemaFile = testSchemaFile.getAsFile(folder);
		Schema schema = ModelSchema.deserialise(schemaFile);
		assertNotNull(schema.getRoot());
		
		File newSchemaFile = new File(folder.getRoot(), "newSchema.ttl");
		assertFalse(newSchemaFile.exists());
		ModelSchema.serialise(schema, newSchemaFile);
		assertTrue(newSchemaFile.exists());
		
		Schema newSchema = ModelSchema.deserialise(newSchemaFile);
		assertNotNull(newSchema.getRoot());
		
		assertEquals(schema.toString(), newSchema.toString());
	}
}
