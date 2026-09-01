package com.game.config;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CustomResultSet {

	public static <T> List<T> selectQuery(Class<T> type, DataSource dataSource, String query) {
		List<T> list = new ArrayList<T>();
		try {
			try (Connection conn = dataSource.getConnection()) {
				try (Statement stmt = conn.createStatement()) {
					try (ResultSet rst = stmt.executeQuery(query)) {
						while (rst.next()) {
							T t = type.newInstance();
							loadResultSetIntoObject(rst, t);// Point 4
							list.add(t);
						}
					}
				} catch (InstantiationException | IllegalAccessException e) {
					throw new RuntimeException("Unable to get the records: " + e.getMessage(), e);
				}
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		return list;
	}
	
	public static <T> T singleSelectQuery(Class<T> type, DataSource dataSource, String query) {
		
		try {
			
			try (Connection conn = dataSource.getConnection()) {
				try (Statement stmt = conn.createStatement()) {
					try (ResultSet rst = stmt.executeQuery(query)) {
						T t = type.newInstance();
						while (rst.next()) {
							
							loadResultSetIntoObject(rst, t);
						}
						return t;
					}
				} catch (InstantiationException | IllegalAccessException e) {
					throw new RuntimeException("Unable to get the records: " + e.getMessage(), e);
				}
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		return null;
	}

	public static void loadResultSetIntoObject(ResultSet rst, Object object)
			throws IllegalArgumentException, IllegalAccessException, SQLException {
		Class<?> zclass = object.getClass();
		for (Field field : zclass.getDeclaredFields()) {
			field.setAccessible(true);
			DBTable column = field.getAnnotation(DBTable.class);
			Object value = rst.getObject(column.columnName());
			Class<?> type = field.getType();
			if(column.columnDefinition().equals("numeric")) {
				if (type == double.class) {
					value = ((BigDecimal) value).doubleValue();
				} else if (type == float.class) {
					value = ((BigDecimal) value).floatValue();
				} else if (type == int.class) {
					value = ((BigDecimal) value).intValue();
				} else if (type == long.class) {
					value = ((BigDecimal) value).longValue();
				} else if (type == short.class) {
					value = ((BigDecimal) value).shortValue();
				} else if (type == byte.class) {
					value = ((BigDecimal) value).byteValue();
				}
			} else if (isPrimitive(type)) {// check primitive type(Point 5)
				Class<?> boxed = boxPrimitiveClass(type);// box if primitive(Point 6)
				value = boxed.cast(value);
			}
			field.set(object, value);
		}
	}

	public static boolean isPrimitive(Class<?> type) {
		return (type == int.class || type == long.class || type == double.class || type == float.class
				|| type == boolean.class || type == byte.class || type == char.class || type == short.class);
	}

	public static Class<?> boxPrimitiveClass(Class<?> type) {
		if (type == int.class) {
			return Integer.class;
		} else if (type == long.class) {
			return Long.class;
		} else if (type == double.class) {
			return Double.class;
		} else if (type == float.class) {
			return Float.class;
		} else if (type == boolean.class) {
			return Boolean.class;
		} else if (type == byte.class) {
			return Byte.class;
		} else if (type == char.class) {
			return Character.class;
		} else if (type == short.class) {
			return Short.class;
		} else {
			String string = "class '" + type.getName() + "' is not a primitive";
			throw new IllegalArgumentException(string);
		}
	}

	public static <T> ArrayList<T> convert(ResultSet rs, Class<T> obj) {
		try {
			ArrayList<T> arrayList = new ArrayList<T>();
			ResultSetMetaData metaData = rs.getMetaData();
			/**
			 * Get total columns
			 */
			int count = metaData.getColumnCount();
			while (rs.next()) {
				/**
				 * Create an object instance
				 */
				T newInstance = obj.newInstance();
				for (int i = 1; i <= count; i++) {
					/**
					 * Assign a value to an attribute of an object
					 */
					String name = metaData.getColumnName(i).toLowerCase();
					name = toJavaField(name);// Change column name format to java Naming format
					String substring = name.substring(0, 1);// title case
					String replace = name.replaceFirst(substring, substring.toUpperCase());
					Class<?> type = null;
					try {
						type = obj.getDeclaredField(name).getType();// Get field type
					} catch (NoSuchFieldException e) { // Class When the field is not defined by the object,skip
						continue;
					}

					Method method = obj.getMethod("set" + replace, type);
					/**
					 * Determine the type of data read
					 */
					if (type.isAssignableFrom(String.class)) {
						method.invoke(newInstance, rs.getString(i));
					} else if (type.isAssignableFrom(byte.class) || type.isAssignableFrom(Byte.class)) {
						method.invoke(newInstance, rs.getByte(i));// byte The data type is an 8-bit signed integer
																	// represented by a binary complement
					} else if (type.isAssignableFrom(short.class) || type.isAssignableFrom(Short.class)) {
						method.invoke(newInstance, rs.getShort(i));// short The data type is a 16 bit signed integer
																	// represented by a binary complement
					} else if (type.isAssignableFrom(int.class) || type.isAssignableFrom(Integer.class)) {
						method.invoke(newInstance, rs.getInt(i));// int The data type is a 32-bit signed integer
																	// represented by a binary complement
					} else if (type.isAssignableFrom(long.class) || type.isAssignableFrom(Long.class)) {
						method.invoke(newInstance, rs.getLong(i));// long The data type is a 64 bit signed integer
																	// represented by a binary complement
					} else if (type.isAssignableFrom(float.class) || type.isAssignableFrom(Float.class)) {
						method.invoke(newInstance, rs.getFloat(i));// float Data type is single precision, 32-bit,
																	// compliant IEEE 754 Standard floating point number
					} else if (type.isAssignableFrom(double.class) || type.isAssignableFrom(Double.class)) {
						method.invoke(newInstance, rs.getDouble(i));// double Data type is double, 64 bit, compliant
																	// IEEE 754 Standard floating point number
					} else if (type.isAssignableFrom(BigDecimal.class)) {
						method.invoke(newInstance, rs.getBigDecimal(i));
					} else if (type.isAssignableFrom(boolean.class) || type.isAssignableFrom(Boolean.class)) {
						method.invoke(newInstance, rs.getBoolean(i));// boolean Data type represents one bit of
																		// information
					} else if (type.isAssignableFrom(Date.class)) {
						method.invoke(newInstance, rs.getDate(i));
					}
				}
				arrayList.add(newInstance);
			}
			return arrayList;

		} catch (InstantiationException | IllegalAccessException | SQLException | SecurityException
				| NoSuchMethodException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new ArrayList<T>();
	}

	public static String toJavaField(String str) {

		String[] split = str.split("_");
		StringBuilder builder = new StringBuilder();
		builder.append(split[0]);// Concatenate first character

		// If the array has more than one word
		if (split.length > 1) {
			for (int i = 1; i < split.length; i++) {
				// Remove underscores and capitalize initial
				String string = split[i];
				String substring = string.substring(0, 1);
				split[i] = string.replaceFirst(substring, substring.toUpperCase());
				builder.append(split[i]);
			}
		}

		return builder.toString();
	}
}
