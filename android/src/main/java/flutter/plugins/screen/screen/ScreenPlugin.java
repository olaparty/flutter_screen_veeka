package flutter.plugins.screen.screen;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;
import android.view.WindowManager;

import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import androidx.annotation.NonNull;
import io.flutter.plugin.common.BinaryMessenger;

/**
 * ScreenPlugin
 */
public class ScreenPlugin implements MethodCallHandler, FlutterPlugin, ActivityAware {

  public ScreenPlugin(){}

  /** update plugin to implement FlutterPlugin : start */
  /**
   * * import io.flutter.embedding.engine.plugins.FlutterPlugin;
   * * import androidx.annotation.NonNull;
   * * import io.flutter.plugin.common.BinaryMessenger;
   * */
  private MethodChannel channel;
  public void initPlugin(BinaryMessenger binaryMessenger, Context context){
    channel = new MethodChannel(binaryMessenger, "github.com/clovisnicolas/flutter_screen");
    channel.setMethodCallHandler(this);
  }
  public void deInitPlugin(){
    if( null != channel ){
      channel.setMethodCallHandler(null);
      channel = null;
    }
  }
  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
    initPlugin(binding.getBinaryMessenger(), binding.getApplicationContext());
  }
  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    deInitPlugin();
  }
  /** update plugin to implement FlutterPlugin : end */

/** update plugin to implement ActivityAware : start */
/**  implement PluginRegistry.RequestPermissionsResultListener for addRequestPermissionsResultListener */
/**  implement PluginRegistry.ActivityResultListener for addActivityResultListener */
/** import io.flutter.embedding.engine.plugins.activity.ActivityAware; */
/** import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding; */

private Activity activity;
@Override
public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
  activity = binding.getActivity();
}

@Override
public void onDetachedFromActivityForConfigChanges() {
  activity = null;
}
@Override
public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
  onAttachedToActivity(binding);
}
@Override
public void onDetachedFromActivity() {
  dispose();
}
private void dispose() {
  deInitPlugin();
  this.activity = null;
}
private boolean runOnUiThread(Runnable runnable) {
  if (activity != null) {
    activity.runOnUiThread(runnable);
    return true;
  }
  return false;
} 
/** update plugin to implement ActivityAware : end */


  @Override
  public void onMethodCall(MethodCall call, Result result) {
    switch(call.method){
      case "brightness":
        result.success(getBrightness());
        break;
      case "setBrightness":
        double brightness = call.argument("brightness");
        WindowManager.LayoutParams layoutParams = activity.getWindow().getAttributes();
        layoutParams.screenBrightness = (float)brightness;
        activity.getWindow().setAttributes(layoutParams);
        result.success(null);
        break;
      case "isKeptOn":
        int flags = activity.getWindow().getAttributes().flags;
        result.success((flags & WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) != 0) ;
        break;
      case "keepOn":
        Boolean on = call.argument("on");
        if (on) {
          System.out.println("Keeping screen on ");
          activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        else{
          System.out.println("Not keeping screen on");
          activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        result.success(null);
        break;

      default:
        result.notImplemented();
        break;
    }
  }

  private float getBrightness(){
    float result = activity.getWindow().getAttributes().screenBrightness;
    if (result < 0) { // the application is using the system brightness
      try {
        result = Settings.System.getInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS) / (float)255;
      } catch (Settings.SettingNotFoundException e) {
        result = 1.0f;
        e.printStackTrace();
      }
    }
    return result;
  }

}
