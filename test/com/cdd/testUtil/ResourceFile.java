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
import java.nio.charset.*;

/*
	ResourceFile 
*/

public class ResourceFile
{
	private String resource;

	// ------------ public methods ------------

	public ResourceFile(String resource)
	{
		this.resource = resource;
	}

	public String getContent() throws IOException
	{
		return getContent("utf-8");
	}

	public String getContent(String charSet) throws IOException
	{
		try (InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream(resource), Charset.forName(charSet)))
		{
			char[] tmp = new char[4096];
			StringBuilder builder = new StringBuilder();
			while (true)
			{
				int len = reader.read(tmp);
				if (len < 0) break;
				builder.append(tmp, 0, len);
			}
			return builder.toString();
		}
		catch (NullPointerException e)
		{
			throw new IOException("Resource not found: " + this.resource);
		}
	}

	/**
	 * Write a byte array to the given file. Writing binary data is significantly simpler than
	 * reading it.
	 * @throws IOException 
	 */
	public void binaryToFile(String outputFilename) throws IOException
	{
		try (InputStream fin = getClass().getResourceAsStream(resource))
		{
			try (OutputStream fout = new FileOutputStream(outputFilename)) 
			{
				int c;
				while ((c = fin.read()) != -1)
				{
					fout.write(c);
				}
			}
		}
	}

	// ------------ private methods ------------

}
