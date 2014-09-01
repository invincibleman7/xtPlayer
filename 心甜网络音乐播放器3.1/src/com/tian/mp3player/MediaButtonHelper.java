package com.tian.mp3player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.ComponentName;
import android.media.AudioManager;

public class MediaButtonHelper {

	static {
		initComponentMethod();
	}

	static Method sMethodRegisterMediaButtonEventReceiver;
	static Method sMethodUnregisterMediaButtonEventReceiver;

	private static void initComponentMethod() {
		// TODO Auto-generated method stub
		try {
			sMethodRegisterMediaButtonEventReceiver = AudioManager.class
					.getMethod("registerMediaButtonEventReceiver",
							new Class[] { ComponentName.class });
			sMethodUnregisterMediaButtonEventReceiver = AudioManager.class
					.getMethod("unregisterMediaButtonEventReceiver",
							new Class[] { ComponentName.class });
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void registerMediaButtonEventReceiverCompat(AudioManager audioManager,ComponentName componentName){
		if(sMethodRegisterMediaButtonEventReceiver==null) return;
		try {
			sMethodRegisterMediaButtonEventReceiver.invoke(audioManager, componentName);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			Throwable cause = e.getCause();
			if(cause instanceof RuntimeException){
				throw (RuntimeException)cause;
			}else if(cause instanceof Error){
				throw (Error) cause;
			}else{
				throw new RuntimeException(e);
			}
		}
	}
	
	public static void unregisterMediaButtonEventReceiverCompat(AudioManager audioManager,ComponentName componentName){
		if(sMethodUnregisterMediaButtonEventReceiver==null) return;
		try {
			sMethodUnregisterMediaButtonEventReceiver.invoke(audioManager, componentName);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			Throwable cause = e.getCause();
			if(cause instanceof RuntimeException){
				throw (RuntimeException)cause;
			}else if(cause instanceof Error){
				throw (Error) cause;
			}else{
				throw new RuntimeException(e);
			}
		}
	}
	
}
