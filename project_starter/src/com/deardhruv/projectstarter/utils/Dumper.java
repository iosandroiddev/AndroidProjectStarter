
package com.deardhruv.projectstarter.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;

public class Dumper {
	private static final Dumper instance = new Dumper();

	protected static Dumper getInstance() {
		return instance;
	}

	class DumpContext {
		int maxDepth = 0;
		int maxArrayElements = 0;
		int callCount = 0;
		HashMap<String, String> ignoreList = new HashMap<>();
		HashMap<Object, Integer> visited = new HashMap<>();
	}

	public static String dump(Object o) {
		return dump(o, 0, 0, null);
	}

	public static String dump(Object o, int maxDepth, int maxArrayElements, String[] ignoreList) {
		DumpContext ctx = Dumper.getInstance().new DumpContext();
		ctx.maxDepth = maxDepth;
		ctx.maxArrayElements = maxArrayElements;

		if (ignoreList != null) {
			for (int i = 0; i < Array.getLength(ignoreList); i++) {
				int colonIdx = ignoreList[i].indexOf(':');
				if (colonIdx == -1)
					ignoreList[i] = ignoreList[i] + ":";
				ctx.ignoreList.put(ignoreList[i], ignoreList[i]);
			}
		}

		return dump(o, ctx);
	}

	protected static String dump(Object o, DumpContext ctx) {
		if (o == null) {
			return "<null>";
		}

		ctx.callCount++;
		StringBuffer tabs = new StringBuffer();
		for (int k = 0; k < ctx.callCount; k++) {
			tabs.append("\t");
		}
		StringBuffer buffer = new StringBuffer();

		@SuppressWarnings("rawtypes")
		Class oClass = o.getClass();

		String oSimpleName = getSimpleNameWithoutArrayQualifier(oClass);

		if (ctx.ignoreList.get(oSimpleName + ":") != null)
			return "<Ignored>";

		if (oClass.isArray()) {
			buffer.append("\n");
			buffer.append(tabs.toString().substring(1));
			buffer.append("[\n");
			int rowCount = ctx.maxArrayElements == 0 ? Array.getLength(o) : Math.min(
					ctx.maxArrayElements, Array.getLength(o));
			for (int i = 0; i < rowCount; i++) {
				buffer.append(tabs.toString());
				try {
					Object value = Array.get(o, i);
					buffer.append(dumpValue(value, ctx));
				} catch (Exception e) {
					buffer.append(e.getMessage());
				}
				if (i < Array.getLength(o) - 1)
					buffer.append(",");
				buffer.append("\n");
			}
			if (rowCount < Array.getLength(o)) {
				buffer.append(tabs.toString());
				buffer.append(Array.getLength(o) - rowCount).append(" more array elements...");
				buffer.append("\n");
			}
			buffer.append(tabs.toString().substring(1));
			buffer.append("]");
		} else {
			buffer.append("\n");
			buffer.append(tabs.toString().substring(1));
			buffer.append("{\n");
			// buffer.append(tabs.toString());
			// buffer.append("hashCode: " + o.hashCode());
			// buffer.append("\n");
			while (oClass != null && oClass != Object.class) {
				Field[] fields = oClass.getDeclaredFields();

				if (ctx.ignoreList.get(oClass.getSimpleName()) == null) {
					if (oClass != o.getClass()) {
						buffer.append(tabs.toString().substring(1));
						buffer.append("  Inherited from superclass ").append(oSimpleName).append(":\n");
					}

                    for (Field field : fields) {

                        String fSimpleName = getSimpleNameWithoutArrayQualifier(field.getType());
                        String fName = field.getName();

                        field.setAccessible(true);
                        buffer.append(tabs.toString());
                        buffer.append(fName).append("(").append(fSimpleName).append(")");
                        buffer.append("=");

                        if (ctx.ignoreList.get(":" + fName) == null
                                && ctx.ignoreList.get(fSimpleName + ":" + fName) == null
                                && ctx.ignoreList.get(fSimpleName + ":") == null) {

                            try {
                                Object value = field.get(o);
                                buffer.append(dumpValue(value, ctx));
                            } catch (Exception e) {
                                buffer.append(e.getMessage());
                            }
                            buffer.append("\n");
                        } else {
                            buffer.append("<Ignored>");
                            buffer.append("\n");
                        }
                    }
					oClass = oClass.getSuperclass();
					oSimpleName = oClass.getSimpleName();
				} else {
					oClass = null;
					oSimpleName = "";
				}
			}
			buffer.append(tabs.toString().substring(1));
			buffer.append("}");
		}
		ctx.callCount--;
		return buffer.toString();
	}

	protected static String dumpValue(Object value, DumpContext ctx) {
		if (value == null) {
			return "<null>";
		}
		if (value.getClass().isPrimitive() || value.getClass() == java.lang.Short.class
				|| value.getClass() == java.lang.Long.class
				|| value.getClass() == java.lang.String.class
				|| value.getClass() == java.lang.Integer.class
				|| value.getClass() == java.lang.Float.class
				|| value.getClass() == java.lang.Byte.class
				|| value.getClass() == java.lang.Character.class
				|| value.getClass() == java.lang.Double.class
				|| value.getClass() == java.lang.Boolean.class
				|| value.getClass() == java.util.Date.class || value.getClass().isEnum()) {

			return value.toString();

		} else {

			Integer visitedIndex = ctx.visited.get(value);
			if (visitedIndex == null) {
				ctx.visited.put(value, ctx.callCount);
				if (ctx.maxDepth == 0 || ctx.callCount < ctx.maxDepth) {
					return dump(value, ctx);
				} else {
					return "<Reached max recursion depth>";
				}
			} else {
				return "<Previously visited - see hashCode " + value.hashCode() + ">";
			}
		}
	}

	private static String getSimpleNameWithoutArrayQualifier(
			@SuppressWarnings("rawtypes") Class clazz) {
		String simpleName = clazz.getSimpleName();
		int indexOfBracket = simpleName.indexOf('[');
		if (indexOfBracket != -1)
			return simpleName.substring(0, indexOfBracket);
		return simpleName;
	}
}
