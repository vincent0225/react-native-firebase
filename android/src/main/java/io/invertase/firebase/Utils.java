package io.invertase.firebase;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;

import javax.annotation.Nullable;


@SuppressWarnings("WeakerAccess")
public class Utils {
  private static final String TAG = "Utils";

  /**
   * send a JS event
   **/
  public static void sendEvent(final ReactContext context, final String eventName, Object body) {
    if (context != null) {
      context
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit(eventName, body);
    } else {
      Log.d(TAG, "Missing context - cannot send event!");
    }

  }

  /**
   * Takes a value and calls the appropriate setter for its type on the target map + key
   *
   * @param key   String key to set on target map
   * @param value Object value to set on target map
   * @param map   WritableMap target map to write the value to
   */
  @SuppressWarnings("unchecked")
  public static void mapPutValue(String key, @Nullable Object value, WritableMap map) {
    if (value == null) {
      map.putNull(key);
    } else {
      String type = value.getClass().getName();
      switch (type) {
        case "java.lang.Boolean":
          map.putBoolean(key, (Boolean) value);
          break;
        case "java.lang.Long":
          Long longVal = (Long) value;
          map.putDouble(key, (double) longVal);
          break;
        case "java.lang.Float":
          float floatVal = (float) value;
          map.putDouble(key, (double) floatVal);
          break;
        case "java.lang.Double":
          map.putDouble(key, (Double) value);
          break;
        case "java.lang.Integer":
          map.putInt(key, (int) value);
          break;
        case "java.lang.String":
          map.putString(key, (String) value);
          break;
        default:
          if (List.class.isAssignableFrom(value.getClass())) {
            map.putArray(key, Arguments.makeNativeArray((List<Object>) value));
          } else if (Map.class.isAssignableFrom(value.getClass())) {
            map.putMap(key, Arguments.makeNativeMap((Map<String, Object>) value));
          } else {
            Log.d(TAG, "utils:mapPutValue:unknownType:" + type);
            map.putNull(key);
          }
      }
    }
  }

  /**
   * Convert a ReadableMap to a WritableMap for the purposes of re-sending back to JS
   *
   * @param map ReadableMap
   * @return WritableMap
   */
  public static WritableMap readableMapToWritableMap(ReadableMap map) {
    // TODO Investigate why there were issues with: #1109
    // https://github.com/facebook/react-native/blob/master/ReactAndroid/src/main/java/com/facebook/react/bridge/WritableNativeMap.java#L54
    // WritableMap writableMap = Arguments.createMap();
    // writableMap.merge(map);
    // return writableMap;

    WritableMap writableMap = Arguments.createMap();

    ReadableMapKeySetIterator iterator = map.keySetIterator();
    while (iterator.hasNextKey()) {
      String key = iterator.nextKey();
      ReadableType type = map.getType(key);
      switch (type) {
        case Null:
          writableMap.putNull(key);
          break;
        case Boolean:
          writableMap.putBoolean(key, map.getBoolean(key));
          break;
        case Number:
          writableMap.putDouble(key, map.getDouble(key));
          break;
        case String:
          writableMap.putString(key, map.getString(key));
          break;
        case Map:
          writableMap.putMap(key, readableMapToWritableMap(map.getMap(key)));
          break;
        case Array:
          // TODO writableMap.putArray(key, readableArrayToWritableArray(map.getArray(key)));
          break;
        default:
          throw new IllegalArgumentException("Could not convert object with key: " + key + ".");
      }
    }

    return writableMap;

  }

  /**
   * Convert a ReadableMap into a native Java Map
   *
   * @param readableMap ReadableMap
   * @return Map
   */
  public static Map<String, Object> recursivelyDeconstructReadableMap(ReadableMap readableMap) {
    // TODO Investigate why there were issues with: #1109
    // https://github.com/facebook/react-native/blob/master/ReactAndroid/src/main/java/com/facebook/react/bridge/ReadableNativeMap.java#L216
    // return readableMap.toHashMap();

    Map<String, Object> deconstructedMap = new HashMap<>();
    if (readableMap == null) {
      return deconstructedMap;
    }

    ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
    while (iterator.hasNextKey()) {
      String key = iterator.nextKey();
      ReadableType type = readableMap.getType(key);
      switch (type) {
        case Null:
          deconstructedMap.put(key, null);
          break;
        case Boolean:
          deconstructedMap.put(key, readableMap.getBoolean(key));
          break;
        case Number:
          deconstructedMap.put(key, readableMap.getDouble(key));
          break;
        case String:
          deconstructedMap.put(key, readableMap.getString(key));
          break;
        case Map:
          deconstructedMap.put(key, Utils.recursivelyDeconstructReadableMap(readableMap.getMap(key)));
          break;
        case Array:
          deconstructedMap.put(key, Utils.recursivelyDeconstructReadableArray(readableMap.getArray(key)));
          break;
        default:
          throw new IllegalArgumentException("Could not convert object with key: " + key + ".");
      }

    }
    return deconstructedMap;

  }

  /**
   * Convert a ReadableArray into a native Java Map
   *
   * @param readableArray ReadableArray
   * @return List<Object>
   */
  public static List<Object> recursivelyDeconstructReadableArray(ReadableArray readableArray) {
    // TODO Investigate why there were issues with: #1109
    // https://github.com/facebook/react-native/blob/master/ReactAndroid/src/main/java/com/facebook/react/bridge/ReadableNativeArray.java#L175
    // return readableArray.toArrayList();

    List<Object> deconstructedList = new ArrayList<>(readableArray.size());
    for (int i = 0; i < readableArray.size(); i++) {
      ReadableType indexType = readableArray.getType(i);
      switch (indexType) {
        case Null:
          deconstructedList.add(i, null);
          break;
        case Boolean:
          deconstructedList.add(i, readableArray.getBoolean(i));
          break;
        case Number:
          deconstructedList.add(i, readableArray.getDouble(i));
          break;
        case String:
          deconstructedList.add(i, readableArray.getString(i));
          break;
        case Map:
          deconstructedList.add(i, Utils.recursivelyDeconstructReadableMap(readableArray.getMap(i)));
          break;
        case Array:
          deconstructedList.add(i, Utils.recursivelyDeconstructReadableArray(readableArray.getArray(i)));
          break;
        default:
          throw new IllegalArgumentException("Could not convert object at index " + i + ".");
      }
    }
    return deconstructedList;
  }

  /**
   * We need to check if app is in foreground otherwise the app will crash.
   * http://stackoverflow.com/questions/8489993/check-android-application-is-in-foreground-or-not
   *
   * @param context Context
   * @return boolean
   */
  public static boolean isAppInForeground(Context context) {
    ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    if (activityManager == null) return false;

    List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
    if (appProcesses == null) return false;

    final String packageName = context.getPackageName();
    for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
      if (
        appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
          && appProcess.processName.equals(packageName)
        ) {
        return true;
      }
    }

    return false;
  }
}
