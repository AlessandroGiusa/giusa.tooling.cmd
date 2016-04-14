/***************************************************************************
 * Copyright 2016 GiusaSoftware
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/
package test.parser.parameter;

import org.junit.Assert;
import org.junit.Test;

import giusa.software.parser.parameter.ParameterParser;

/**
 * Test the {@link ParameterParser}.
 * @author Alessandro Giusa
 * @version 1.0
 *
 */
public final class TestParameterParser {

    /**
     * Test case.
     */
    @Test
    public void testParameterOptionsOff() {
        final String[] args = new String[]{
                "--name ", "Dieter",
                "-length", "2",
                "test",
                "45.98",
                "--exchange", "0.25",
                "-age=38",
                "--path",  "\"C:\\DevTools\""
        };

        final ParameterParser parser = new ParameterParser();
        parser.setParseOptions(false);
        // since this test case is testing the case where all is arguments
        parser.parse(args);

        final String name = parser.getNamedString("name");
        Assert.assertEquals("Dieter", name);

        final int length = parser.getNamedInt("length");
        Assert.assertEquals(2, length);

        final float exchange = parser.getNamedFloat("exchange");
        Assert.assertEquals(0.25f, exchange, 0);

        final String unnamedTest = parser.getUnnamedString(0);
        Assert.assertEquals("test", unnamedTest);

        final double unnamedNumber = parser.getUnnamedDouble(1);
        Assert.assertEquals(45.98, unnamedNumber, 0);

        final int age = parser.getNamedInt("age");
        Assert.assertEquals(38, age);

        final String path = parser.getNamedString("path");
        Assert.assertEquals("C:\\DevTools", path);
    }

    /**
     * Test case.
     */
    @Test
    public void testParameterOptionsOn() {
        final String[] args = new String[]{
                "--name=Dieter",
                "-length=2",
                "test",
                "45.98",
                "--exchange=0.25",
                "-age=38",
                "--parser",
                "--path=\"C:\\DevTools\"",
                "-f",
                "'test2'"
        };

        final ParameterParser parser = new ParameterParser();
        parser.setParseOptions(true);
        parser.parse(args);

        final String name = parser.getNamedString("name");
        Assert.assertEquals("Dieter", name);

        final int length = parser.getNamedInt("length");
        Assert.assertEquals(2, length);

        final float exchange = parser.getNamedFloat("exchange");
        Assert.assertEquals(0.25f, exchange, 0);

        final String unnamedTest = parser.getUnnamedString(0);
        Assert.assertEquals("test", unnamedTest);

        final double unnamedNumber = parser.getUnnamedDouble(1);
        Assert.assertEquals(45.98, unnamedNumber, 0);

        final String unnamedString = parser.getUnnamedString(2);
        Assert.assertEquals("test2", unnamedString);

        final int age = parser.getNamedInt("age");
        Assert.assertEquals(38, age);

        final String path = parser.getNamedString("path");
        Assert.assertEquals("C:\\DevTools", path);

        final boolean fOption = parser.hasOption("-f");
        Assert.assertTrue(fOption);

        final boolean parserOption = parser.hasOption("--parser");
        Assert.assertTrue(parserOption);
    }

}
