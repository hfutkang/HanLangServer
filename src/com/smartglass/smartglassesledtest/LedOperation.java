package com.smartglass.smartglassesledtest;

public class LedOperation {
	
	public native boolean TurnRedLightOn();
	public native boolean TurnRedLightOff();
	public native boolean TurnRedLightBlinkOn();
	public native boolean TurnRedLightBlinkOff();
	public native boolean TurnGreenLightOn();
	public native boolean TurnGreenLightOff();
	public native boolean TurnGreenLightBlinkOn();
	public native boolean TurnGreenLightBlinkOff();
	public native boolean TurnMixerOn();
	public native boolean TurnMixerOff();
	public native boolean SetRedBlinkRate(float rate);
	public native boolean SetGreenBlinkRate(float rate);
	public native boolean SetMixerRate(float rate);
	
	static {
		System.loadLibrary("SmartGlassesLedTest");
	}
	
}
