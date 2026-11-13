package org.junit;
import java.util.Objects;
/** Minimal API-compatible stand-in for org.junit.Assert semantics used by this project. */
public final class Assert {
  private Assert() {}
  public static void fail(String m) { throw new AssertionError(m); }
  public static void assertTrue(boolean c) { if (!c) fail(null); }
  public static void assertTrue(String m, boolean c) { if (!c) fail(m); }
  public static void assertFalse(boolean c) { if (c) fail(null); }
  public static void assertFalse(String m, boolean c) { if (c) fail(m); }
  public static void assertEquals(Object e, Object a) { assertEquals(null, e, a); }
  public static void assertEquals(String m, Object e, Object a) { if (!Objects.equals(e, a)) fail((m == null ? "" : m + " ") + "expected:<" + e + "> but was:<" + a + ">"); }
  public static void assertEquals(long e, long a) { assertEquals(null, e, a); }
  public static void assertEquals(String m, long e, long a) { if (e != a) fail((m == null ? "" : m + " ") + "expected:<" + e + "> but was:<" + a + ">"); }
  public static void assertEquals(double e, double a, double d) { assertEquals(null, e, a, d); }
  public static void assertEquals(String m, double e, double a, double d) { if (Double.compare(e, a) != 0 && !(Math.abs(e - a) <= d)) fail((m == null ? "" : m + " ") + "expected:<" + e + "> but was:<" + a + ">"); }
  public static void assertEquals(float e, float a, float d) { assertEquals(null, (double) e, (double) a, (double) d); }
  public static void assertNotEquals(Object e, Object a) { assertNotEquals(null, e, a); }
  public static void assertNotEquals(String m, Object e, Object a) { if (Objects.equals(e, a)) fail((m == null ? "" : m + " ") + "Values should be different. Actual: " + a); }
  public static void assertNotEquals(long e, long a) { if (e == a) fail("Values should be different. Actual: " + a); }
  public static void assertNull(Object o) { assertNull(null, o); }
  public static void assertNull(String m, Object o) { if (o != null) fail((m == null ? "" : m + " ") + "expected null, but was:<" + o + ">"); }
  public static void assertNotNull(Object o) { assertNotNull(null, o); }
  public static void assertNotNull(String m, Object o) { if (o == null) fail(m); }
}
