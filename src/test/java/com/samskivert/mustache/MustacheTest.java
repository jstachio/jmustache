//
// $Id$

package com.samskivert.mustache;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Various unit tests.
 */
public class MustacheTest
{
    @Test public void testSimpleVariable () {
        test("bar", "{{foo}}", context("foo", "bar"));
    }

    @Test public void testFieldVariable () {
        test("bar", "{{foo}}", new Object() {
            String foo = "bar";
        });
    }

    @Test public void testMethodVariable () {
        test("bar", "{{foo}}", new Object() {
            String foo () { return "bar"; }
        });
    }

    @Test public void testPropertyVariable () {
        test("bar", "{{foo}}", new Object() {
            String getFoo () { return "bar"; }
        });
    }

    @Test public void testSkipVoidReturn () {
        test("bar", "{{foo}}", new Object() {
            void foo () {}
            String getFoo () { return "bar"; }
        });
    }

    @Test public void testCallSiteReuse () {
        Template tmpl = Mustache.compiler().compile("{{foo}}");
        Object ctx = new Object() {
            String getFoo () { return "bar"; }
        };
        for (int ii = 0; ii < 50; ii++) {
            assertEquals("bar", tmpl.execute(ctx));
        }
    }

    @Test public void testCallSiteChange () {
        Template tmpl = Mustache.compiler().compile("{{foo}}");
        assertEquals("bar", tmpl.execute(new Object() {
            String getFoo () { return "bar"; }
        }));
        assertEquals("bar", tmpl.execute(new Object() {
            String foo = "bar";
        }));
    }

    @Test public void testOneShotSection () {
        test("baz", "{{#foo}}{{bar}}{{/foo}}", context("foo", context("bar", "baz")));
    }

    @Test public void testListSection () {
        test("bazbif", "{{#foo}}{{bar}}{{/foo}}", context(
                 "foo", Arrays.asList(context("bar", "baz"), context("bar", "bif"))));
    }

    @Test public void testArraySection () {
        test("bazbif", "{{#foo}}{{bar}}{{/foo}}",
             context("foo", new Object[] {
                     context("bar", "baz"), context("bar", "bif") }));
    }

    @Test public void testIteratorSection () {
        test("bazbif", "{{#foo}}{{bar}}{{/foo}}",
             context("foo", Arrays.asList(context("bar", "baz"),
                                          context("bar", "bif")).iterator()));
    }

    @Test public void testEmptyListSection () {
        test("", "{{#foo}}{{bar}}{{/foo}}", context("foo", Collections.emptyList()));
    }

    @Test public void testEmptyArraySection () {
        test("", "{{#foo}}{{bar}}{{/foo}}", context("foo", new Object[0]));
    }

    @Test public void testEmptyIteratorSection () {
        test("", "{{#foo}}{{bar}}{{/foo}}", context("foo", Collections.emptyList().iterator()));
    }

    @Test public void testFalseSection () {
        test("", "{{#foo}}{{bar}}{{/foo}}", context("foo", false));
    }

    @Test public void testNestedListSection () {
        test("1234", "{{#a}}{{#b}}{{c}}{{/b}}{{#d}}{{e}}{{/d}}{{/a}}",
             context("a", new Object[] {
                     context("b", new Object[] { context("c", "1"), context("c", "2") }),
                     context("d", new Object[] { context("e", "3"), context("e", "4") }) }));
    }

    @Test public void testComment () {
        test("foobar", "foo{{! nothing to see here}}bar", new Object());
    }

    @Test public void testEscapeHTML () {
        assertEquals("<b>", Mustache.compiler().compile("{{a}}").execute(context("a", "<b>")));
        assertEquals("&lt;b&gt;", Mustache.compiler().escapeHTML(true).
                     compile("{{a}}").execute(context("a", "<b>")));
    }

    protected void test (String expected, String template, Object ctx)
    {
        assertEquals(expected, Mustache.compiler().compile(template).execute(ctx));
    }

    protected static Object context (Object... data)
    {
        Map<String, Object> ctx = new HashMap<String, Object>();
        for (int ii = 0; ii < data.length; ii += 2) {
            ctx.put(data[ii].toString(), data[ii+1]);
        }
        return ctx;
    }
}