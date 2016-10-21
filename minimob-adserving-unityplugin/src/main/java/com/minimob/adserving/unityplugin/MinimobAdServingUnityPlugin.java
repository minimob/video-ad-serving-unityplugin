package com.minimob.adserving.unityplugin;
import java.util.ArrayList;

import android.app.Activity;
import android.util.Log;

import com.minimob.adserving.adzones.AdTag;
import com.minimob.adserving.adzones.AdZone;
import com.minimob.adserving.adzones.AdZoneVideo;
import com.minimob.adserving.adzones.AdZoneVideoPreloaded;
import com.minimob.adserving.controllers.MinimobAdController;
import com.minimob.adserving.interfaces.IAdZoneCreatedListener;
import com.minimob.adserving.interfaces.IAdsAvailableListener;
import com.minimob.adserving.interfaces.IAdsNotAvailableListener;
import com.minimob.adserving.interfaces.IVideoClosedListener;
import com.minimob.adserving.interfaces.IVideoFinishedListener;
import com.minimob.adserving.interfaces.IVideoLoadingListener;
import com.minimob.adserving.interfaces.IVideoLoadedListener;
import com.minimob.adserving.interfaces.IVideoPlayingListener;
import com.unity3d.player.UnityPlayer;

/**
 * Created by t.nikolopoulos on 06/8/2016.
 */
public class MinimobAdServingUnityPlugin
{
	String TAG = "MinimobUnityPlugin";

	private AdZone adZone = null;
	private ArrayList<String> postponedMessages = new ArrayList<String>();
	private boolean adZoneCreated = false;
	private boolean hasFocus = true;
	private static MinimobAdServingUnityPlugin _instance;
	
	public static MinimobAdServingUnityPlugin GetInstance()
	{
		if (_instance == null)
			_instance = new MinimobAdServingUnityPlugin();
		return _instance;
	}
	
	public void CreateAdZone(String adTagString, String customTrackingData, final boolean preloadVideo)
    {
		Activity activity = UnityPlayer.currentActivity;
		try
        {
        	//set the listener that gets called when the AdZone is created by MinimobAdController
            MinimobAdController.getInstance().setAdZoneCreatedListener(new IAdZoneCreatedListener()
            {
                @Override
                public void onAdZoneCreated(AdZone adZone)
                {
					OnAdZoneCreated(adZone);
			    }
			});
			//create the AdTag object
            AdTag adTag = new AdTag(activity, adTagString);
            //set the custom tracking data (optional)
            adTag.setCustomTrackingData(customTrackingData);
            //request the AdZone
            Log.d(TAG , "Requesting AdZone");
            SendUnityMessage("Requesting AdZone");
			if (preloadVideo) {
				MinimobAdController.getInstance().getVideoPreloaded(activity, adTag);
			}
			else {
				MinimobAdController.getInstance().getVideo(activity, adTag);
			}
        }
        catch (Exception ex)
        {
        	Log.e(TAG, "exception:" + ex.getMessage() , ex);
        }
    }

	private void OnAdZoneCreated(AdZone zone)
	{
		adZoneCreated = true;
        adZone = zone;

        if (adZone instanceof AdZoneVideo)
        {
            AdZoneVideo adZoneVideo = (AdZoneVideo) adZone;
            //set a listener for when the ad server returns the event that there are ads available
            adZoneVideo.setAdsAvailableListener(new IAdsAvailableListener() {
                @Override
                public void onAdsAvailable(AdZone adZone){
                    SendUnityMessage("OnAdsAvailable");
                }
            });
            //set a listener for when the ad server returns the event that there are NO ads available
            adZoneVideo.setAdsNotAvailableListener(new IAdsNotAvailableListener() {
                @Override
                public void onAdsNotAvailable(AdZone adZone) {
                    SendUnityMessage("OnAdsNotAvailable");
                }
            });
            //set a listener for when the video started playing
            adZoneVideo.setVideoPlayingListener(new IVideoPlayingListener() {
                @Override
                public void onVideoPlaying(AdZone adZone) {
                    SendUnityMessage("OnVideoPlaying");
                }
            });
            //set a listener for when the video finished playing
            adZoneVideo.setVideoFinishedListener(new IVideoFinishedListener() {
                @Override
                public void onVideoFinished(AdZone adZone) {
                    SendUnityMessage("OnVideoFinished");
                }
            });
            //set a listener for when the video was closed by the user
            adZoneVideo.setVideoClosedListener(new IVideoClosedListener() {
                //Log.d(Tag,"onVideoClosedListener called");
                @Override
                public void onVideoClosed(AdZone adZone) {
                    SendUnityMessage("OnVideoClosed");
                }
            });
        }
        else if (adZone instanceof AdZoneVideoPreloaded)
        {
            AdZoneVideoPreloaded adZoneVideoPreloaded = (AdZoneVideoPreloaded) adZone;
            //set a listener for when the ad server returns the event that there are ads available
            adZoneVideoPreloaded.setAdsAvailableListener(new IAdsAvailableListener() {
                @Override
                public void onAdsAvailable(AdZone adZone){
                    SendUnityMessage("OnAdsAvailable");
                }
            });
            //set a listener for when the ad server returns the event that there are NO ads available
            adZoneVideoPreloaded.setAdsNotAvailableListener(new IAdsNotAvailableListener() {
                @Override
                public void onAdsNotAvailable(AdZone adZone) {
                    SendUnityMessage("OnAdsNotAvailable");
                }
            });
            //set a listener for when the video started loading
            adZoneVideoPreloaded.setVideoLoadingListener(new IVideoLoadingListener() {
                @Override
                public void onVideoLoading(AdZone adZone) {
                    SendUnityMessage("OnVideoLoading");
                }
            });
            //set a listener for when the video finished loading
            adZoneVideoPreloaded.setVideoLoadedListener(new IVideoLoadedListener() {
                @Override
                public void onVideoLoaded(AdZone adZone) {
                    SendUnityMessage("OnVideoLoaded");
                }
            });
            //set a listener for when the video started playing
            adZoneVideoPreloaded.setVideoPlayingListener(new IVideoPlayingListener() {
                @Override
                public void onVideoPlaying(AdZone adZone) {
                    SendUnityMessage("OnVideoPlaying");
                }
            });
            //set a listener for when the video finished playing
            adZoneVideoPreloaded.setVideoFinishedListener(new IVideoFinishedListener() {
                @Override
                public void onVideoFinished(AdZone adZone) {
                    SendUnityMessage("OnVideoFinished");
                }
            });
            //set a listener for when the video was closed by the user
            adZoneVideoPreloaded.setVideoClosedListener(new IVideoClosedListener() {
                //Log.d(Tag,"onVideoClosedListener called");
                @Override
                public void onVideoClosed(AdZone adZone) {
                    SendUnityMessage("OnVideoClosed");
                }
            });
        }
		SendUnityMessage("OnAdZoneCreated");
	}

	public void OnApplicationFocus(boolean focus)
	{
		hasFocus = focus;
		if (focus)
		{
			for (String msg : postponedMessages)
				SendUnityMessage(msg);
			postponedMessages.clear();
		}
	}

	public void LoadVideo()
	{
        if (adZone == null || !adZoneCreated) {
            Log.d(TAG , "NO adZone");
            return;
        }

		if (!(adZone instanceof AdZoneVideoPreloaded)) {
            Log.d(TAG , "adZone not an instance of AdZoneVideoPreloaded");
            return;
        }

		UnityPlayer.currentActivity.runOnUiThread(
            new Runnable() {
                @Override
                public void run() {
                    if (adZone instanceof AdZoneVideoPreloaded)
                        ((AdZoneVideoPreloaded)adZone).load();
                    else
                        Log.e(TAG , "unknown AdZone object type");
                }
            }
        );
	}

	public void ShowVideo()
	{
		if (adZone == null || !adZoneCreated) {
            Log.d(TAG , "NO adZone");
            return;
        }

		UnityPlayer.currentActivity.runOnUiThread(
            new Runnable() {
                @Override
                public void run() {
                    if (adZone instanceof AdZoneVideo)
                        ((AdZoneVideo) adZone).show();
                    else if (adZone instanceof AdZoneVideoPreloaded)
                        ((AdZoneVideoPreloaded)adZone).show();
                    else
                        Log.e(TAG , "unknown AdZone object type");
                }
            }
        );
	}

	private void SendUnityMessage(String msg)
	{
		if (!hasFocus)
		{
			Log.d(TAG , "postponing unity message:" + msg);
			postponedMessages.add(msg);
			return;
		}
		Log.d(TAG , "sending unity message:"  + msg);
		UnityPlayer.UnitySendMessage("MinimobAdServing", msg, "");
	}


}