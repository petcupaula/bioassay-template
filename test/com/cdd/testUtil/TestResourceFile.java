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

package com.cdd.testUtil;

import java.io.*;

import org.junit.rules.*;

/*
	TestResourceFile
	
	Access test data in unit tests
*/

public class TestResourceFile extends ExternalResource
{
	private String resource;

	// ------------ public methods ------------

	public TestResourceFile(String resource)
	{
		this.resource = resource;
	}

	public String getContent() throws IOException
	{
		return getContent("utf-8");
	}

	public String getContent(String charSet) throws IOException
	{
		ResourceFile resourceFile = new ResourceFile(this.resource);
		return resourceFile.getContent(charSet);
	}
	
	public File getAsFile(TemporaryFolder folder) throws IOException
	{
		return getAsFile(folder.newFile());
	}

	public File getAsFile(File file) throws IOException
	{
		try (FileWriter out = new FileWriter(file))
		{
			out.write(getContent());
		}
		return file;
	}

	public File getAsFileBinary(TemporaryFolder folder) throws IOException
	{
		return getAsFileBinary(folder.newFile());
	}

	public File getAsFileBinary(File file) throws IOException
	{
		ResourceFile resourceFile = new ResourceFile(this.resource);
		resourceFile.binaryToFile(file.getCanonicalPath()); 
		return file;
	}
	
	// returns the content of the file as a stream; the caller is responsible for closing it
	public InputStream getAsStream() throws IOException
	{
		return getClass().getResourceAsStream(resource);
	}
	
	// reads in everything, and returns the bytes
	public byte[] getAsBytes() throws IOException
	{
		try (InputStream stream = new BufferedInputStream(getAsStream()))
		{
			ByteArrayOutputStream buff = new ByteArrayOutputStream();
			int b;
			while ((b = stream.read()) >= 0) buff.write(b);
			return buff.toByteArray();
		}
	}	
}
