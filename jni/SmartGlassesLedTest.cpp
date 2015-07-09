#include <jni.h>
//#include <com_smartglass_smartglassesledtest_LedOperation.h>
#include <stdio.h>
#include <sys/ioctl.h>
#include <stdlib.h>
#include <fcntl.h>
#include <android/log.h>
#include <errno.h>

#define SC_LEDS_RED        "/sys/ingenic_leds/red"
#define SC_LEDS_GREEN      "/sys/ingenic_leds/green"
#define SC_LEDS_MIXER      "/sys/ingenic_leds/mixer"
#define TURN_LED_ON           "0"
#define TURN_LED_OFF          "1"
#define TURN_LED_FLICKER_ON   "2"
#define TURN_LED_FLICKER_OFF  "3"
#define TURN_LED_FLICKER_RATE "4"

#define DEBUG_ON					1
// red status   : on  off  flicker_on  flicker_off
// green status : on  off  flicker_on  flicker_off
// mixer status : flicker_on  flicker_off
// rate status  : set flicker rate for red, green, mixer
//                format ==> "red 50", unit 0.01 s

#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_smartglass_smartglassesledtest_LedOperation
 * Method:    TurnRedLightOn
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_smartglass_smartglassesledtest_LedOperation_TurnRedLightOn
  (JNIEnv *, jobject) {

	if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST", "red on");
	int fd = open(SC_LEDS_RED, O_RDWR);

	if(fd < 0) {
		if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST1", "error: %s", strerror(errno));
		close(fd);
		return false;
	}

	int res = write(fd, TURN_LED_ON, 1);
	if(res == -1) {
		if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST2", "error: %s", strerror(errno));
		close(fd);
		return false;
	}
	close(fd);
	return true;
}

/*
 * Class:     com_smartglass_smartglassesledtest_LedOperation
 * Method:    TurnRedLightoff
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_smartglass_smartglassesledtest_LedOperation_TurnRedLightOff
  (JNIEnv *, jobject) {
	if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST", "red off");
	int fd = open(SC_LEDS_RED, O_RDWR);

	if(fd < 0) {
		if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST1", "error: %s", strerror(errno));
		close(fd);
		return false;
	}

	int res = write(fd, TURN_LED_OFF, 1);
	if(res == -1) {
		if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST2", "error: %s", strerror(errno));
		close(fd);
		return false;
	}

	close(fd);
	return true;
}

/*
 * Class:     com_smartglass_smartglassesledtest_LedOperation
 * Method:    TurnRedLightBlink
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_smartglass_smartglassesledtest_LedOperation_TurnRedLightBlinkOn
  (JNIEnv *, jobject) {
	if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST", "red blink on");
	int fd = open(SC_LEDS_RED, O_RDWR);

	if(fd < 0) {
		if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST1", "error: %s", strerror(errno));
		close(fd);
		return false;
	}

	int res = write(fd, TURN_LED_FLICKER_ON, 1);
	if(res == -1) {
		if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST2", "error: %s", strerror(errno));
		close(fd);
		return false;
	}

	close(fd);
	return true;
}

JNIEXPORT jboolean JNICALL Java_com_smartglass_smartglassesledtest_LedOperation_TurnRedLightBlinkOff
  (JNIEnv *, jobject) {

	if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST", "red blink off");

	int fd = open(SC_LEDS_RED, O_RDWR);

	if(fd < 0) {
		if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST1", "error: %s", strerror(errno));
		close(fd);
		return false;
	}

	int res = write(fd, TURN_LED_FLICKER_OFF, 1);
	if(res == -1) {
		if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST2", "error: %s", strerror(errno));
		close(fd);
		return false;
	}

	close(fd);
	return true;

}

/*
 * Class:     com_smartglass_smartglassesledtest_LedOperation
 * Method:    TurnGreenLightOn
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_smartglass_smartglassesledtest_LedOperation_TurnGreenLightOn
  (JNIEnv *, jobject) {
	if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST", "green on");
	int fd = open(SC_LEDS_GREEN, O_RDWR);

	if(fd < 0) {
		if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST1", "error: %s", strerror(errno));
		close(fd);
		return false;
	}

	int res = write(fd, TURN_LED_ON, 1);
	if(res == -1) {
		if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST2", "error: %s", strerror(errno));
		close(fd);
		return false;
	}

	close(fd);
	return true;
}

/*
 * Class:     com_smartglass_smartglassesledtest_LedOperation
 * Method:    TurnGreenLightoff
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_smartglass_smartglassesledtest_LedOperation_TurnGreenLightOff
  (JNIEnv *, jobject) {

	if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST", "green off");

	int fd = open(SC_LEDS_GREEN, O_RDWR);

	if(fd < 0) {
		if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST1", "error: %s", strerror(errno));
		close(fd);
		return false;
	}

	int res = write(fd, TURN_LED_OFF, 1);
	if(res == -1) {
		if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST2", "error: %s", strerror(errno));
		close(fd);
		return false;
	}

	close(fd);
	return true;
}

/*
 * Class:     com_smartglass_smartglassesledtest_LedOperation
 * Method:    TurnGreenLightBlink
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_com_smartglass_smartglassesledtest_LedOperation_TurnGreenLightBlinkOn
  (JNIEnv *, jobject) {

	if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST", "green blink on");

	int fd = open(SC_LEDS_GREEN, O_RDWR);

	if(fd < 0) {
		if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST1", "error: %s", strerror(errno));
		close(fd);
		return false;
	}

	int res = write(fd, TURN_LED_FLICKER_ON, 1);
	if(res == -1) {
		if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST2", "error: %s", strerror(errno));
		close(fd);
		return false;
	}

	close(fd);
	return true;

}

JNIEXPORT jboolean JNICALL Java_com_smartglass_smartglassesledtest_LedOperation_TurnGreenLightBlinkOff
  (JNIEnv *, jobject) {

	if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST", "green blink off");

	int fd = open(SC_LEDS_GREEN, O_RDWR);

	if(fd < 0) {
		if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST1", "error: %s", strerror(errno));
		close(fd);
		return false;
	}

	int res = write(fd, TURN_LED_FLICKER_OFF, 1);
	if(res == -1) {
		if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST2", "error: %s", strerror(errno));
		close(fd);
		return false;
	}

	close(fd);
	return true;

}

JNIEXPORT jboolean JNICALL Java_com_smartglass_smartglassesledtest_LedOperation_TurnMixerOn
  (JNIEnv *, jobject) {

	if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST", "mixer on");

	int fd = open(SC_LEDS_MIXER, O_RDWR);

	if(fd < 0) {
		if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST1", "error: %s", strerror(errno));
		close(fd);
		return false;
	}

	int res = write(fd, TURN_LED_FLICKER_ON, 1);
	if(res == -1) {
		if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST2", "error: %s", strerror(errno));
		close(fd);
		return false;
	}

	close(fd);
	return true;

}

JNIEXPORT jboolean JNICALL Java_com_smartglass_smartglassesledtest_LedOperation_TurnMixerOff
  (JNIEnv *, jobject) {

	if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST", "Mixer Off");

	int fd = open(SC_LEDS_GREEN, O_RDWR);

	if(fd < 0) {
		if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST1", "error: %s", strerror(errno));
		close(fd);
		return false;
	}

	int res = write(fd, TURN_LED_FLICKER_OFF, 1);
	if(res == -1) {
		if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST2", "error: %s", strerror(errno));
		close(fd);
		return false;
	}

	close(fd);
	return true;

}

JNIEXPORT jboolean JNICALL Java_com_smartglass_smartglassesledtest_LedOperation_SetRedBlinkRate
  (JNIEnv *, jobject, jfloat rate) {

	if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST", "set red blink rate");

	int fd = open(SC_LEDS_RED, O_RDWR);

	if(fd < 0) {
		if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST1", "error: %s", strerror(errno));
		close(fd);
		return false;
	}

	char ratestr[10];
	sprintf(ratestr, "4 %d", (int)(100/rate));
	int res = write(fd, ratestr, sizeof(char)*strlen(ratestr));
	if(res == -1) {
		if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST2", "error: %s", strerror(errno));
		close(fd);
		return false;
	}

	close(fd);
	return true;

}

JNIEXPORT jboolean JNICALL Java_com_smartglass_smartglassesledtest_LedOperation_SetGreenBlinkRate
  (JNIEnv *, jobject, jfloat rate) {

	if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST", "set green blink rate");

	int fd = open(SC_LEDS_GREEN, O_RDWR);

	if(fd < 0) {
		if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST1", "error: %s", strerror(errno));
		close(fd);
		return false;
	}

	char ratestr[10];
	sprintf(ratestr, "4 %d", (int)(100/rate));
	int res = write(fd, ratestr, sizeof(char)*strlen(ratestr));
	if(res == -1) {
		if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST2", "error: %s", strerror(errno));
		close(fd);
		return false;
	}

	close(fd);
	return true;

}

JNIEXPORT jboolean JNICALL Java_com_smartglass_smartglassesledtest_LedOperation_SetMixerRate
  (JNIEnv *, jobject, jfloat rate) {

	if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST", "set mixer rate");

	int fd = open(SC_LEDS_MIXER, O_RDWR);

	if(fd < 0) {
		if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST1", "error: %s", strerror(errno));
		close(fd);
		return false;
	}

	char ratestr[10];
	sprintf(ratestr, "4 %d", (int)(100/rate));
	int res = write(fd, ratestr, sizeof(char)*strlen(ratestr));
	if(res == -1) {
		if(DEBUG_ON) __android_log_print(ANDROID_LOG_INFO, "LEDTEST2", "error: %s", strerror(errno));
		close(fd);
		return false;
	}

	close(fd);
	return true;

}

#ifdef __cplusplus
}
#endif
