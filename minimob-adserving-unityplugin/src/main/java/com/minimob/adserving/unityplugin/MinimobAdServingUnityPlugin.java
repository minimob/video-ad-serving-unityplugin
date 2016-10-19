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
	
	public void CreateVideo(String adTagString , String customTrackingData , final boolean preloaded)
    {
		Activity activity = UnityPlayer.currentActivity;
		try
        {
        	//set the listener that gets called when the AdZone is created by MinimobAdController
            MinimobAdController.getInstance().setAdZoneCreatedListener(new IAdZoneCreatedListener()
            {
                @Override
                public void onAdZoneCreated(AdZone adZone1)
                {
					OnAdZoneCreated(adZone1);
			    }
			});
			//create the AdTag object
            AdTag adTag = new AdTag(activity, adTagString);
            //set the custom tracking data (optional)
            adTag.setCustomTrackingData(customTrackingData);
            //request the AdZone
            Log.d(TAG , "requesting adzone");
			if (preloaded)
				MinimobAdController.getInstance().getVideoPreloaded(activity,adTag);
			else
				MinimobAdController.getInstance().getVideo(activity,adTag);
        }
        catch (Exception ex)
        {
        	Log.e(TAG, "exception:" + ex.getMessage() , ex);
        }
    }

	private void OnAdZoneCreated(AdZone adZone1)
	{
		adZoneCreated = true;
		adZone = adZone1;

		//set a listener for when the ad server returns the event that there are ads available
		adZone.setAdsAvailableListener(new IAdsAvailableListener() {
			@Override
			public void onAdsAvailable(AdZone adZone){
				SendUnityMessage("OnAdsAvailable");
			}
		});

		//set a listener for when the ad server returns the event that there are NO ads available
		adZone.setAdsNotAvailableListener(new IAdsNotAvailableListener() {
			@Override
			public void onAdsNotAvailable(AdZone adZone) {
				SendUnityMessage("OnAdsNotAvailable");
			}
		});

		if (adZone instanceof AdZoneVideoPreloaded)
		{
			AdZoneVideoPreloaded adZoneVideoPreloaded = (AdZoneVideoPreloaded)adZone;
			adZoneVideoPreloaded.setVideoLoadingListener(new IVideoLoadingListener() {
				@Override
				public void onVideoLoading(AdZone adZone) {
					SendUnityMessage("OnVideoLoading");
				}
			});
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
		else if (adZone instanceof AdZoneVideo)
		{
			AdZoneVideo adZoneVideo = (AdZoneVideo)adZone;
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
		SendUnityMessage("OnVideoCreated");
	}

	public void OnApplicationFocus(boolean focus)
	{
		hasFocus = focus;
		if (focus)
		{
			for (String methodName : postponedMessages)
				SendUnityMessage(methodName);
			postponedMessages.clear();
		}
	}

	public void LoadVideo()
	{
		if (adZone == null || !adZoneCreated)
			return;
		if (! (adZone instanceof AdZoneVideoPreloaded))
			return;

		UnityPlayer.currentActivity.runOnUiThread(
				new Runnable() {
					@Override
					public void run() {
						AdZoneVideoPreloaded adZoneVideoPreloaded = (AdZoneVideoPreloaded) adZone;
						adZoneVideoPreloaded.load();
					}
				});
	}

	public void ShowVideo()
	{
		if (adZone == null || !adZoneCreated)
			return;

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
		});
	}

	private void SendUnityMessage(String methodName)
	{
		if (!hasFocus)
		{
			Log.d(TAG , "postponing unity message:"  + methodName);
			postponedMessages.add(methodName);
			return;
		}
		Log.d(TAG , "sending unity message:"  + methodName);
		UnityPlayer.UnitySendMessage("MinimobVideoAdPlayer:", methodName, "");
	}


}