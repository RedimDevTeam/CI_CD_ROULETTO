package com.game.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.game.enums.StatusCode;
import com.game.response.APIResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FunctionHelper {

    public static String getAlphaNumericString(int length)
    {
        String alphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvxyz";

        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = (int)(alphaNumericString.length() * Math.random());
            sb.append(alphaNumericString.charAt(index));
        }

        return sb.toString();
    }

    public static BigDecimal getBigDecimal(Object value) {
        BigDecimal returnValue = null;
        if (value != null) {
            if (value instanceof BigDecimal) {
                returnValue = (BigDecimal) value;
            } else if (value instanceof String) {
                returnValue = new BigDecimal((String) value);
            } else if (value instanceof BigInteger) {
                returnValue = new BigDecimal((BigInteger) value);
            } else if (value instanceof Number) {
                returnValue = BigDecimal.valueOf(((Number) value).doubleValue());
            } else {
                throw new ClassCastException("Not possible to convert [" + value + "] from class " + value.getClass() + " into a BigDecimal.");
            }
        }
        return returnValue;
    }

    public static BigDecimal add(BigDecimal value1, BigDecimal value2) {
        return value1.add(value2);
    }

    public static BigDecimal subtract(BigDecimal value1, BigDecimal value2) {
        return value1.subtract(value2);
    }

    public static boolean isEqual(BigDecimal value1, BigDecimal value2) {
        int res = value1.compareTo(value2);
        return res == 0;
    }

    public static boolean isLessThan(BigDecimal value1, BigDecimal value2) {
        int res = value1.compareTo(value2);
        return res == -1;
    }

    public static boolean isLessThanOrEqual(BigDecimal value1, BigDecimal value2) {
        int res = value1.compareTo(value2);
        return res == -1 || res == 0;
    }

    public static boolean isGreaterThan(BigDecimal value1, BigDecimal value2) {
        int res = value1.compareTo(value2);
        return res == 1;
    }

    public static boolean isGreaterThanOrEqual(BigDecimal value1, BigDecimal value2) {
        int res = value1.compareTo(value2);
        return res == 1 || res == 0;
    }


    public static boolean isNullOrEmpty(String value) {
        if (value == null) {
            return true;
        }

        return value.isEmpty();
    }

    public static boolean isNullOrTrimEmpty(String value) {
        if (value == null) {
            return true;
        }

        return value.trim().isEmpty();
    }

    public static String commaSeparated(Collection<Integer> values) {
        if (values == null) {
            return "";
        }
        return values.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    public static String commaSeparated(ArrayList<Integer> values) {
        if (values == null) {
            return "";
        }
        return values.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    public static String commaSeparated(List<Integer> values) {
        if (values == null) {
            return "";
        }
        return values.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    public static String joinString(List<String> values, String value) {
        if (values == null) {
            return "";
        }
        return values.stream().map(String::valueOf).collect(Collectors.joining(value));
    }

    public static String joinInteger(List<Integer> values, String value) {
        if (values == null) {
            return "";
        }
        return values.stream().map(String::valueOf).collect(Collectors.joining(value));
    }

    public static <T> T Deserialize(String jsonString, Class<T> cls) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(jsonString, cls);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String Serialize(Object object) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            return mapper.writer().writeValueAsString(object);
        } catch (Exception ignored) {
        }
        return null;
    }
    public static String SerializeWithOutIndent(Object object) {
        try {
            ObjectMapper mapper = new ObjectMapper();
//            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            return mapper.writer().writeValueAsString(object);
        } catch (Exception ignored) {
        }
        return null;
    }

	public static String Serialize(Object object, boolean removeSpace) {
		String jsonString = Serialize(object);

		if(removeSpace && jsonString != null) {
			jsonString = jsonString.replace("\r", "");
            jsonString = jsonString.replace("\n", "");
			jsonString = jsonString.replace(" ", "");
            jsonString = jsonString.replace("\\", "");
		}

		return jsonString;
	}
	


    public static int getRemainingTimerSeconds(Date startTimer, int timerSeconds) {
        long seconds = ((new Date()).getTime() / 1000) - (startTimer.getTime() / 1000);
        // System.out.println(seconds);
        // System.out.println(timerSeconds);
        if (seconds < timerSeconds) {
            return (timerSeconds - (int) seconds);
        }

        return 0;
    }

    public static RestTemplate getRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setConnectTimeout(25000);
        ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setReadTimeout(25000);

        return restTemplate;
    }

    public static double getBetAmountFromPercentage(double tableAmount, double percentage) {
        return Math.round(tableAmount * (percentage / 100.0));
    }

    public static double getPercentage(double value, double totalValue) {
        if(totalValue == 0) {
            return 0;
        }
        return (value / totalValue) * 100.0;
    }

    public static int toInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }


    public static String getJsonString(APIResponse response) {
        String json = "";
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            json = objectMapper.writeValueAsString(response);
            // System.out.println(json);
        } catch (Exception ignored) {

        }

        return json;
    }

    public static String getJsonString(Object object) {
        String json = "";
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            json = objectMapper.writeValueAsString(object);
            // System.out.println(json);
        } catch (Exception ignored) {

        }

        return json;
    }

    public static String getJsonString(StatusCode code, Object data) {
        return getJsonString(code.value(), code.toString(), data);
    }

    public static String getJsonString(int code, String status, Object data) {
        String json = "";
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            HashMap<String, Object> keyValues = new HashMap<>();
            keyValues.put("code", code);
            keyValues.put("status", status);
            keyValues.put("result", data);

            json = objectMapper.writeValueAsString(keyValues);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return json;
    }

    public static <T, U> List<U> convertStringList(List<T> listOfString, Function<T, U> function)
    {
        return listOfString.stream()
                .map(function)
                .collect(Collectors.toList());
    }
    
    
    public static String getEncryptedValue( String encryptedValue) {
		try { 
			MessageDigest md = MessageDigest.getInstance("MD5"); 
			byte[] messageDigest = md.digest(encryptedValue.getBytes()); 
			BigInteger no = new BigInteger(1, messageDigest); 
			String hashtext = no.toString(16); 
			while (hashtext.length() < 32) { 
				hashtext = "0" + hashtext; 
			} 
			return hashtext; 
		} catch (Exception e) { 
			throw new RuntimeException(e); 
		} 
	} 
    
    public static String getStringData(Object obj) {
		ObjectMapper mapper = new ObjectMapper();
		String jsonResponse = null;
		try {
			jsonResponse = mapper.writeValueAsString(obj);
		} catch (Exception e) {
		}
		return jsonResponse;
	}

    public static BigDecimal divide(BigDecimal amount, BigDecimal divided) {
        return amount.divide(divided, 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal roundDownToTwoDecimalPlaces(BigDecimal amount, int scale) {
        BigDecimal formattedAmount = amount;
        if (formattedAmount.stripTrailingZeros().scale() >= scale) {
            formattedAmount = amount.setScale(scale, RoundingMode.DOWN);
        }
        return formattedAmount;
    }

    public static String getDateFormat(long currentTime) {
        Date date = new Date(currentTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String gmtDate = sdf.format(date);
        return gmtDate;
    }

}
